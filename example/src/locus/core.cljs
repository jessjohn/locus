;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns ^:figwheel-always locus.core
  (:require [reagent.core :as r]
            [locus.map :as locus]
            [locus.db :as db]))

(enable-console-print!)

(defonce map-ref (r/atom nil))
(defonce added-shape (r/atom nil))

(defn add-shape []
  (when-not @added-shape
    (reset! added-shape
            (locus/add! @map-ref (db/random-shape)))))

(defn remove-shape []
  (when @added-shape
    (locus/remove! @map-ref @added-shape)
    (reset! added-shape nil)))

(defn map-example []
  (let [teal "#27B99C"
        dark-teal "#008369"
        pink "#D70075"
        yellow "#FFCF36"
        button-panel-style {:display "flex"
                            :flex-direction "column"
                            :position "relative"
                            :width 80
                            :margin 20
                            :zIndex 9999}
        map-opts {:shapes db/tiny-db
                  :map-ref map-ref
                  :style {:position "absolute"
                          :top 0
                          :bottom 0
                          :width "100%"}
                  :theme {:shape-opts {:marker {:color teal}
                                       :cluster {:color dark-teal}
                                       :polygon {:color yellow
                                                 :fillColor yellow}
                                       :polyline {:color yellow}
                                       :rectangle {:color pink}
                                       :circle {:color pink}
                                       :trail {:color teal}}}}]
    [:div
     [:div
      {:style button-panel-style}
      [:input {:type "button"
               :value "add!"
               :disabled (boolean @added-shape)
               :onClick #(add-shape)}]
      [:input {:type "button"
               :value "remove!"
               :disabled (not (boolean @added-shape))
               :onClick #(remove-shape)}]]
     (locus/base-map map-opts)]))

(r/render-component [map-example] (. js/document (getElementById "app")))
