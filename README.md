# 7theta/locus

A Leaflet map sprinkled with some reagent voodoo.

## Usage

Include `locus` in your `project.clj` dependencies.

[![Current Version](https://img.shields.io/clojars/v/com.7theta/locus.svg)](https://clojars.org/com.7theta/locus)
[![Circle CI](https://circleci.com/gh/7theta/locus.svg?style=shield)](https://circleci.com/gh/7theta/locus)
[![Dependencies Status](https://jarkeeper.com/7theta/locus/status.svg)](https://jarkeeper.com/7theta/locus)

Add `locus.map` to your file's namespace `:require`

```clojure
(:require [locus.map :as locus])
```

Take a look in `examples/` to see `locus` in use.

## Map

FYI: "locus map" means the graphical representation of the earth, 
      and "map" means a Clojure hash map.

```clojure
(locus/base-map map-opts)
```

### Map-Opts
```clojure
{;; id for the div element containing the locus map
 :id "locus-map"
 ;; JSON format for describing tilesets
 :tile-json nil
 ;; initial zoom and center
 :init-zoom 2
 :init-center #js [35 0]
 ;; any options passed to the locus map upon creation
 :options {:zoomControl false
           :attributionControl false
           :minZoom 2}
 ;; url for where tile-layer is served from
 :tile-layer-url "https://www.awesome-maps.com/awesome-tiles"
 ;; any events you want the locus map to handle
 ;; use key as the event name, and the value as the callback fn
 :map-events {:zoomend (fn [_] ...)
              :dragend (fn [_] ...)
              :etc ...}
 ;; what shapes are drawn on the locus map (more details below)
 ;; must be a collection of maps, wrapped in an r/atom
 :map-shapes (r/atom [{woo-shapes}])
 ;; a r/atom that will be assigned the reference to the map as there
 ;; could be multiple maps that you want to add/remove shapes from
 :map-ref (r/atom nil)
 ;; options applied to every locus map shape
 :theme {:shape-opts {:marker {:color ...
 							   :popup ...
 							   :on-click (fn [e] ...)}
	                  :cluster {:color ...
	                  			:popup ...
 							    :on-click (fn [e] ...)}
	                  :polygon {...you}
	                  :polyline {...get}
	                  :rectangle {...the}
	                  :circle {...idea}}}
 ;; style options applied to the div element containing the locus map
 :style {:width "100%"
         ...}}
```

### Map Shapes

Your locus map shapes should look similar to this:
```clojure
{:type ;; one of :circle :rectangle :polyline :polygon :marker
 :latlons ;; [lat lon] or [[lat lon] [lat lon] ...]
 :radius ;; int, for circles only
 :arc? ;; boolean, for polylines only
 ;; opts are basically anything found on leaflet vector layers
 ;; http://leafletjs.com/reference.html#path
 :opts {:color ...
 	    :popup ...
	    :on-click (fn [e] ...)
	    ...}}
```

#### Adding Shapes

Each shape on the locus map can be added in 2 different ways:

**(1) Explicitly** by calling 
```clojure
(locus/add! map-ref shape)
;; or
(locus/add! map-ref shape marker-layer)
```
This requires a reference to the locus map, and a shape map as detailed above. `marker-layer` lets you add markers to a special layer, in case you want to do [clustering](https://github.com/Leaflet/Leaflet.markercluster) or something like that. This will return the same shape map merged with an additional kv pair, `:ref` containing the leaflet reference to the added shape. By explicitly adding shapes to the locus map you have to handle the themeing, updating, etc., yourself.

**(2) Implicitly** by updating the contents of `:shapes` passed into the locus map map-opts when it's created. Since it's wrapped in a `r/atom`, this will cause a re-render due to [reagent voodoo](https://github.com/reagent-project/reagent#examples). Some nifty features are already implemented when shapes are implicitly added, like themeing, updating, and replicating past IDL boundaries.

#### Removing Shapes

Each shape on the locus map can be removed in 2 different ways:

**(1) Explicitly** by calling 
```clojure
(locus/remove! map-ref shape)
;; or
(locus/remove! map-ref shape marker-layer)
```
This requires a reference to the locus map, and a shape map containing a `:ref`.

**(2) Implicitly** by updating the contents of `:shapes` passed into the locus map when it's created. Since it's wrapped in a `r/atom`, this will cause a re-render.

### Contributing

Unfortunately pull requests are not being accepted at this time, but
please feel free to submit bug reports and requests by raising issues.

## Copyright and License

Copyright © 2015, 2016 7theta

Distributed under the Eclipse Public License.

