(ns aoc-2022.utils 
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn input
  ([day]
   (input day {:lines? true}))
  ([day {:keys [actual? lines?]}]
   (let [actual? (if (nil? actual?) false actual?)
         lines? (if (nil? lines?) true lines?)]
     (cond-> (slurp (str "resources/" (if actual? "actual" "test") "/day" day))
       lines? (str/split-lines)))))
