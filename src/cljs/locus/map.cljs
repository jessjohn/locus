;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns locus.map
  (:refer-clojure :exclude [map])
  (:require [reagent.core :as r]))

;;; Forward Declarations

(declare leaflet-map ->circle
         ->rectangle ->polygon
         ->polyline  ->marker)

;;; Public

(defn base-map
  "A Leaflet map wrapped in React capabilities."
  ([] (base-map nil))
  ([opts]
   [leaflet-map opts]))

(defn add!
  "Adds 'shape' to the 'map'. If 'shape' is a marker, and markers are expected
  to be added to a specific cluster layer, it can be provided in 'marker-layer'."
  ([map shape] (add! map shape nil))
  ([map {:as shape :keys [type popup on-click]} marker-layer]
   (let [type (:type shape)
         layer (case type
                 :circle (->circle shape)
                 :rectangle (->rectangle shape)
                 :polygon (->polygon shape)
                 :polyline (->polyline shape)
                 :marker (->marker shape))]
     (.addLayer (or (and (= type :marker) marker-layer) map)
                (cond-> layer
                  popup (.bindPopup popup)
                  on-click (.on "click" on-click)))
     (merge shape {:ref layer}))))

(defn remove!
  "Removes a 'shape' from the 'map' or 'marker-layer' if provided."
  ([map shape] (remove! map shape nil))
  ([map shape marker-layer]
   (.removeLayer (or (and (= type :marker) marker-layer) map) (:ref shape))))

(defn set-viewport
  "Change the viewport of the 'map' to the new 'bounds'."
  [map bounds]
  (.fitBounds map (clj->js bounds)))

;;; Implementation

(defn- js-obj->cljs-map
  "Takes a JavaScript object and returns it as a Clojure map, with the keys as
  keywords or strings if 'keywordize?' is false."
  ([js-obj] (js-obj->cljs-map js-obj nil))
  ([js-obj keywordize?]
   (js->clj js-obj :keywordize-keys (or (boolean keywordize?) true))))

(defn ->circle
  "Creates a Leaflet circle."
  [{:as shape-map :keys [latlons radius opts]}]
  (js/L.circle (clj->js latlons)
               radius
               (clj->js opts)))

(defn ->rectangle
  "Creates a Leaflet rectangle."
  [{:as shape-map :keys [latlons opts]}]
  (js/L.rectangle (clj->js latlons)
                  (clj->js opts)))

(defn ->polygon
  "Creates a Leaflet polygon."
  [{:as shape-map :keys [latlons opts]}]
  (js/L.polygon (clj->js latlons)
                (clj->js opts)))

(defn ->polyline
  "Creates a Leaflet polyline."
  [{:as shape-map :keys [latlons arc? opts]}]
  (if arc?
    (js/L.Polyline.Arc (clj->js (first latlons))
                       (clj->js (second latlons))
                       (clj->js opts))
    (js/L.polyline (clj->js latlons)
                   (clj->js opts))))

(defn ->marker
  "Creates a Leaflet marker."
  [{:as shape-map :keys [latlons opts]}]
  (let [color (:color opts)
        icon (or (:icon opts) (js/L.mapbox.marker.icon
                               (clj->js {:marker-color color})))]
    (js/L.marker (js/L.latLng (clj->js latlons))
                 (clj->js (merge opts {:icon icon})))))

(defn send-viewport
  "Returns information about the current map: viewport center, zoom level, viewport bounds,
  and current time. Automatically called whenever the map is panned or zoomed."
  [map]
  (let [center (js-obj->cljs-map (.getCenter map))
        bounds (.getBounds map)
        nw (js-obj->cljs-map (.getNorthWest bounds))
        se (js-obj->cljs-map (.getSouthEast bounds))]
    {:bounds {:north-west {:lat (:lat nw) :lon (:lng nw)}
              :south-east {:lat (:lat se) :lon (:lng se)}}
     :zoom (js-obj->cljs-map (.getZoom map))
     :center {:lat (:lat center)
              :lon (:lng center)}
     :time (.getTime (js/Date.))}))

(defn outside-idl?
  "Are the 'bounds' outside the international date line?"
  [bounds]
  (let [nw (:lng (js-obj->cljs-map (.getNorthWest bounds)))
        se (:lng (js-obj->cljs-map (.getSouthEast bounds)))]
    (> (max (js/Math.abs nw) (js/Math.abs se)) 360)))

(defn- duplicate-shapes
  "Adds copies of all shapes with their longitudes in increments of 360 degrees,
  either east or west depending on which direction the 'bounds' have been panned."
  [this bounds]
  (let [nw (:lng (js-obj->cljs-map (.getNorthWest bounds)))
        se (:lng (js-obj->cljs-map (.getSouthEast bounds)))
        state (r/state this)
        map (:map-ref state)
        marker-layer (get-in state [:layers :marker-layer])
        shapes (:shapes state)]
    (dotimes [n (quot (max (js/Math.abs nw) (js/Math.abs se)) 360)]
      (mapv #(case (:type %)
               (:rectangle :polyline :polygon)
               (add! map (merge % {:latlons (mapv (fn [x]
                                                    [(first x)
                                                     (+ ((if (or (> nw (- 360)) (> se 360)) + -)
                                                         (* (inc n) 360))
                                                        (second x))])
                                                  (:latlons %))}))
               (:circle :marker)
               (add! map (merge % {:latlons [(first (:latlons %))
                                             (+ ((if (or (> nw (- 360)) (> se 360)) + -)
                                                 (* (inc n) 360))
                                                (second (:latlons %)))]})
                     marker-layer))
            @shapes))))

(defn- update-shapes
  "Update map shapes by adding any new shapes and removing old shapes."
  [this new-shapes theme]
  (let [state (r/state this)
        old-shapes (:shapes state)
        marker-layer (get-in state [:layers :marker-layer])
        map (:map-ref state)
        map-bounds (.getBounds map)
        shape-opts (:shape-opts theme)]
    (doseq [shape @old-shapes]
      (remove! map shape marker-layer))
    (.clearLayers marker-layer)
    (r/set-state this {:shapes (r/atom [])})
    (when new-shapes
      (doseq [shape @new-shapes
              :let [opts (merge ((:type shape) shape-opts) (:opts shape))
                    added-shape (add! map (assoc shape :opts opts) marker-layer)]]
        (r/set-state this {:shapes (r/atom (conj @(:shapes (r/state this)) added-shape))})))
    (when (outside-idl? map-bounds) (duplicate-shapes this map-bounds))))

(defn- mount-map
  "Initialize map, shapes, layers, and controls."
  [this & [map-opts]]
  (let [state (r/state this)
        shapes (or (:shapes map-opts))
        shape-opts (get-in map-opts [:theme :shape-opts])
        default-map-opts {:id "locus-map"
                          :tile-json nil
                          :options {:zoomControl false
                                    :attributionControl false
                                    :minZoom 2}
                          :tile-layer-url "mapbox://styles/mapbox/streets-v8"
                          :init-zoom 2
                          :init-center #js [35 0]
                          :map-events {:zoomend (fn [_]
                                                  (update-shapes this shapes (:theme map-opts)))
                                       :dragend (fn [_]
                                                  (update-shapes this shapes (:theme map-opts)))}}
        {:keys [init-zoom init-center
                id tile-json options
                tile-layer-url map-events
                shapes theme]} (merge default-map-opts map-opts)
        map (js/L.mapbox.map id tile-json (clj->js options))
        cluster-color (get-in theme [:shape-opts :cluster :color])
        marker-layer (js/L.markerClusterGroup.
                      (clj->js {:animate false
                                :iconCreateFunction (fn [cluster]
                                                      (js/L.mapbox.marker.icon
                                                       (clj->js {:marker-color cluster-color
                                                                 :marker-symbol (.getChildCount cluster)})))}))
        tile-layer (js/L.mapbox.styleLayer tile-layer-url)
        zoom-control (js/L.Control.Zoom. (clj->js {:position "bottomright"}))]
    (.setView map init-center init-zoom)
    (.addTo tile-layer map)
    (.addTo zoom-control map)
    (.addLayer map marker-layer)
    (doseq [[event f] map-events]
      (.on map (name event) f))
    (r/set-state this {:map-ref map
                       :layers {:tile-layer tile-layer
                                :marker-layer marker-layer}
                       :controls {:zoom zoom-control}
                       :shapes (r/atom [])})
    (when shapes
      (doseq [shape @shapes
              :let [opts (merge ((:type shape) shape-opts) (:opts shape))
                    added-shape (add! map (assoc shape :opts opts) marker-layer)]]
        (r/set-state this {:shapes (r/atom (conj @(:shapes (r/state this)) added-shape))})))
    (reset! (:map-ref map-opts) map)))

(defn- destroy!
  "Removes all map shapes, layers, and the Leaflet map object."
  [this]
  (let [state (r/state this)
        map (:map-ref state)
        shapes (:shapes state)
        marker-layer (get-in state [:layers :marker-layer])
        tile-layer (get-in state [:layers :tile-layer])]
    (.clearLayers marker-layer)
    (doseq [shape @shapes]
      (remove! map shape marker-layer))
    (reset! shapes [])
    (doto map
      (.removeLayer marker-layer)
      (.removeLayer tile-layer)
      (.removeControl (get-in state [:controls :zoom]))
      (.remove))))

(defn- leaflet-map
  "React class wrapper for Leaflet map component."
  [{:keys [shapes theme style id] :as map-opts}]
  (let [div-style (merge {:position "absolute"
                          :top 0
                          :bottom 0
                          :width "100%"}
                         style)]
    (r/create-class {:display-name "LocusMap"
                     :get-initial-state (fn [] {:map-ref nil
                                               :layers {}})
                     :component-did-mount (fn [this] (mount-map this map-opts))
                     :render (fn [] (when shapes @shapes)
                               [:div {:style div-style
                                      :id (or id "locus-map")}])
                     :component-did-update (fn [this] (update-shapes this shapes theme))
                     :component-will-unmount destroy!})))
