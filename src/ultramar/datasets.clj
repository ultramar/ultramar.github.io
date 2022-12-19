(ns ultramar.datasets
  (:require [charred.api :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [stasis.core :as stasis]))


(defn ->json-dataset [edn-str]
  (-> edn-str
      (edn/read-string)
      :data
      (json/write-json-str)))

(defn gather-datasets [src]
  (-> src
      (stasis/slurp-directory #".*\.edn$")
      (update-keys #(str/replace % #".edn" ".json"))))

(def gather
  {:enter (fn [{:keys [datasets-dir] :as ctx}]
            (let [datasets (->> (gather-datasets datasets-dir)
                                (map (fn [[url doc]]
                                       (let [new-url (str "/dataset" url)
                                             doc (->json-dataset doc)]
                                         [new-url doc])))
                                (into {}))]
              (-> ctx
                  (assoc :datasets datasets)
                  (update :page-map merge datasets))))})