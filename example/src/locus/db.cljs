;;   Copyright (c) 7theta. All rights reserved.
;;   The use and distribution terms for this software are covered by the
;;   Eclipse Public License 1.0 (http://www.eclipse.org/legal/epl-v10.html)
;;   which can be found in the LICENSE file at the root of this
;;   distribution.
;;
;;   By using this software in any fashion, you are agreeing to be bound by
;;   the terms of this license.
;;   You must not remove this notice, or any others, from this software.

(ns locus.db
  (:require [reagent.core :as r]))

(defn random-shape []
  (let [lat (+ (rand-int (- (- 60) 75)) 75)
        lng (+ (rand-int (- (- 181) 180)) 180)
        shapes [{:type :marker
                 :latlons [(+ lat (/ (rand-int 100) 10)) (+ lng (/ (rand-int 100) 10))]
                 :opts {:color "#27B99C"}
                 :id (gensym)}
                {:type :polygon
                 :latlons (into [] (take 7 (repeatedly #(vector (+ lat (/ (rand-int 100) 10))
                                                                (+ lng (/ (rand-int 100) 10))))))
                 :opts {:fillColor "#9D00FF"
                        :color "#9D00FF"
                        :weight 2}
                 :id (gensym)}
                {:type :polyline
                 :arc? true
                 :latlons [[(+ lat (/ (rand-int 100) 1)) (+ lng (/ (rand-int 100) 1))]
                           [(+ lat (/ (rand-int 100) 1)) (+ lng (/ (rand-int 100) 1))]]
                 :opts {:color "#FF8C66"
                        :weight 2}
                 :id (gensym)}
                {:type :circle
                 :latlons [lat lng]
                 :radius 90000
                 :opts {:color "#FF7700"
                        :weight 2}
                 :id (gensym)}
                {:type :rectangle
                 :latlons [[(+ lat (/ (rand-int 100) 10)) (+ lng (/ (rand-int 100) 10))]
                           [(+ lat (/ (rand-int 100) 10)) (+ lng (/ (rand-int 100) 10))]]
                 :opts {:color "#FF00CC"
                        :weight 2}
                 :id (gensym)}]]
    (rand-nth shapes)))

(defonce tiny-db (r/atom (into [] (repeatedly 15 #(random-shape)))))
