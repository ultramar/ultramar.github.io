(ns ultramar.notebooks.data
  {:nextjournal.clerk/visibility {:code :hide}}
  (:require [tablecloth.api :as tc]
            [babashka.fs :as fs]
            [tech.v3.io :as io]
            [nextjournal.clerk :as clerk]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [nextjournal.clerk.viewer :as viewer]))

(def data-filepaths (->> (io/resource "data")
                         (fs/list-dir)
                         (map str)
                         (remove #(str/ends-with? % ".DS_Store"))
                         (sort)))

(def data-files (->> data-filepaths
                     (map (fn [path]
                            [(fs/file-name path)
                             (-> path
                                 slurp
                                 edn/read-string)]))
                     (into {})))


(defn convert-csv [path]
  (let [csv-files (-> (io/resource path)
                      (fs/list-dir))]
    (doseq [csv-file csv-files
            :when (= "resources/data/vitoria/Q3A-Table 1.csv" (str csv-file))
            :let [fname (fs/file-name csv-file)
                  dset (tc/dataset (str csv-file))]]


      (spit (-> (fs/parent csv-file)
                (fs/path (str fname ".edn"))
                (str))
            (with-out-str
              (pprint
                {:source {:title     "A Vitória Traída"
                          :author    ["Joaquim da Luz Cunha"
                                      "Kaúlza de Arriaga"
                                      "Bethencourt Rodrigues"
                                      "Silvino Silvério Marques"]
                          :publisher "Intervenção"
                          :year      "1977"
                          :page      0}
                 :notes  []
                 :data   (vec (tc/rows dset :as-maps))}
                ))
            )
      )))

;; Select
^{::clerk/viewer {:transform-fn (comp clerk/mark-presented (clerk/update-val (fn [{:as x ::clerk/keys [var-from-def]}]
                                                                               {:var-name (symbol var-from-def)
                                                                                :data     @@var-from-def
                                                                                })))
                  :render-fn    '(fn [{:as x :keys [var-name data]}]
                                   (v/html (into [:select {:value     (:value data)
                                                           :on-change #(v/clerk-eval `(swap! ~var-name assoc :value ~(.. % -target -value)))}]
                                                 (->> (:options data)
                                                      (mapv (fn [o]
                                                              [:option o]))))))
                  }}

(defonce select-state (atom {:options (keys data-files)
                             :value   nil}))


(let [{:keys [source title notes data]} (data-files (:value @select-state))]
  (clerk/table (-> source
                   (seq))))

(let [{:keys [source title notes data] :as fd} (data-files (:value @select-state))]
  (clerk/md (str/join "\n\n" notes)))

(let [{:keys [source title notes data]} (data-files (:value @select-state))]
  (clerk/table data))
