(ns ultramar.notebooks.vitoria
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

;; ## Quadro I

(def q1 (get data-files "avt77_58.edn"))

(clerk/table (:data q1))

(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   "Comparticipação de Portugal (Europa) nas despesas com as Forças Armadas"
               :anchor "middle"
               :offset 20}
   :height    300
   :data      {:values (:data q1)}
   :config    {:legend {:labelLimit 1000}
               :axis   {:labelAngle 0}}
   :transform [{:calculate "datum['Despesas Públicas de Portugal (Europa)'] - datum['Comparticipação de Portugal (Europa) nas despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar']"
                :as        "Restante despesa pública de Portugal (Europa)"}

               {:fold ["Comparticipação de Portugal (Europa) nas despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar"
                       "Restante despesa pública de Portugal (Europa)"]
                :as   ["Parcela" "Valor"]}]
   :hconcat   [{:encoding {:x     {:field "Ano"}
                           :y     {:field "Valor",
                                   :type  :quantitative
                                   :stack :normalize
                                   :axis  {:format ".1~%"}
                                   :title nil},
                           :order {:field "Valor"}
                           :color {:field  "Parcela"
                                   :scale  {:range ["#e45756"
                                                    "#f2f2f2"]}
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Anual"
                :width    450
                :mark     {:type    "bar"
                           :tooltip true}}

               {:encoding {:y     {:field     "Valor",
                                   :type      :quantitative
                                   :aggregate "sum"
                                   :stack     :normalize
                                   :axis      {:format ".1~%"}
                                   :title     nil},
                           :order {:field     "Valor"
                                   :aggregate "sum"}
                           :color {:field  "Parcela"
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Total"
                :mark     {:type    "bar"
                           :tooltip true}}]})


(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   "Comparticipação da Guiné, Angola e Moçambique nas despesas com as Forças Armadas"
               :anchor "middle"
               :offset 20}
   :data      {:values (:data q1)}
   :config    {:legend {:labelLimit 1000}
               :axis   {:labelAngle 0}}
   :transform [{:calculate "datum['Despesas Públicas de Guiné, Angola e Moçambique'] - datum['Comparticipação da Guiné, Angola e Moçambique nas despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar']"
                :as        "Restante despesa pública de Guiné, Angola e Moçambique"}

               {:fold ["Comparticipação da Guiné, Angola e Moçambique nas despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar"
                       "Restante despesa pública de Guiné, Angola e Moçambique"]
                :as   ["Parcela" "Valor"]}]
   :hconcat   [{:encoding {:x     {:field "Ano"}
                           :y     {:field "Valor",
                                   :type  :quantitative
                                   :stack :normalize
                                   :axis  {:format ".1~%"}
                                   :title nil},
                           :order {:field "Valor"}
                           :color {:field  "Parcela"
                                   :scale  {:range ["#d6616b" ;"#e45756"
                                                    "#f2f2f2"]}
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Anual"
                :width    450
                :mark     {:type    "bar"
                           :tooltip true}}

               {:encoding {:y     {:field     "Valor",
                                   :type      :quantitative
                                   :aggregate "sum"
                                   :stack     :normalize
                                   :axis      {:format ".1~%"}
                                   :title     nil},
                           :order {:field     "Valor"
                                   :aggregate "sum"}
                           :color {:field  "Parcela"
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Total"
                :mark     {:type    "bar"
                           :tooltip true}}]})


(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   "Despesas com as Forças Armadas"
               :anchor "middle"
               :offset 20}
   :data      {:values (:data q1)}
   :config    {:legend {:labelLimit 1000}
               :axis   {:labelAngle 0}}
   :transform [{:calculate "datum['Despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar'] - datum['Despesas com as Forças Armadas de Portugal (Europa) e todo o Ultramar se não tivesse havido Guerra']"
                :as        "Despesa imputável à situação de guerra"}
               {:calculate "datum['Despesas Públicas de Portugal (Europa)'] + datum['Despesas Públicas de Guiné, Angola e Moçambique']"
                :as        "Despesa pública total"}
               {:calculate "datum['Despesa pública total'] - datum['Despesa imputável à situação de guerra']"
                :as        "Restante despesa pública"}

               {:fold ["Despesa imputável à situação de guerra"
                       "Restante despesa pública"]
                :as   ["Parcela" "Valor"]}]
   :hconcat   [{:encoding {:x     {:field "Ano"}
                           :y     {:field "Valor",
                                   :type  :quantitative
                                   :stack :normalize
                                   :axis  {:format ".1~%"}
                                   :title nil},
                           :order {:field "Valor"}
                           :color {:field  "Parcela"
                                   :scale  {:range ["#e45756"
                                                    "#f2f2f2"]}
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Anual"
                :width    450
                :mark     {:type    "bar"
                           :tooltip true}}

               {:encoding {:y     {:field     "Valor",
                                   :type      :quantitative
                                   :aggregate "sum"
                                   :stack     :normalize
                                   :axis      {:format ".1~%"}
                                   :title     nil},
                           :order {:field     "Valor"
                                   :aggregate "sum"}
                           :color {:field  "Parcela"
                                   :legend {:orient    :bottom
                                            :direction :vertical}}},
                :title    "Total"
                :mark     {:type    "bar"
                           :tooltip true}}]})

;; ***

;; ## Quadro II

(def q2 (get data-files "avt77_61.edn"))

(clerk/table (:data q2))


;; ## Quadro III

(def q3i (get data-files "avt77_64_i.edn"))
(def q3ii (get data-files "avt77_64_ii.edn"))

(clerk/table (:data q3i))

(clerk/vl
  {:$schema  "https://vega.github.io/schema/vega-lite/v5.json",
   :title    {:text   "Efectivos das Forças Armadas"
              :anchor "middle"
              :offset 20}
   :width    450
   :data     {:values (:data q3i)}
   :config   {:legend {:labelLimit 1000}
              :axis   {:labelAngle 0}}
   :encoding {:x     {:field "Ano"}
              :y     {:field "Efectivos",
                      :type  :quantitative
                      ;:stack :normalize
                      ;:axis  {:format ".1~%"}
                      :title nil},
              :order {:field "Valor"}
              :color {:field  "Teatro"
                      :scale  {:range  ["#d6616b"
                                        "#e7ba94"
                                        "#843c39"]}
                      ;:legend {:orient    :bottom
                      ;         :direction :vertical}
                      }},
   :mark     {:type    "area"
              :tooltip true}})

(clerk/table (:data q3ii))

;; ## Quadro IV

(def q4 (get data-files "avt77_68.edn"))
(clerk/table (:data q4))

;; ## Quadro V

(def q5i (get data-files "avt77_70_i.edn"))
(clerk/table (:data q5i))

(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   ""
               :anchor "middle"
               :offset 20}
   :data      {:values (:data q5i)}
   :config    {:legend {:labelLimit 1000}
               :axisX  {:labels false
                        :title  false
                        :ticks  false}}
   :transform [{:calculate "datum['Mortos em combate'] + datum['Mortos por outros motivos']"
                :as        "Mortos"}
               {:calculate "datum['Feridos em combate'] + datum['Feridos por acidente']"
                :as        "Feridos"}
               {:calculate "datum['Feridos por acidente'] + datum['Doentes']"
                :as        "Feridos por acidente e Doentes"}
               {:calculate "datum['Feridos'] + datum['Doentes']"
                :as        "Feridos e Doentes"}
               {:fold ["Mortos em combate"
                       "Mortos por outros motivos"
                       "Mortos"
                       "Feridos em combate"
                       "Feridos por acidente"
                       "Feridos"
                       "Doentes"
                       "Feridos por acidente e Doentes"
                       "Feridos e Doentes"]
                :as   ["Tipo" "Baixas"]}]
   :resolve   {:scale {:x     :independent
                       :y     :independent
                       :color :independent}}
   :vconcat   [{:width    200
                :encoding {:x      {:field "Tipo"
                                    :type  :nominal
                                    :scale {:domain ["Mortos em combate"
                                                     "Mortos por outros motivos"
                                                     "Mortos"]}}
                           :y      {:field "Baixas",
                                    :type  :quantitative}
                           :column {:field "Teatro"}
                           :color  {:field "Tipo"
                                    :scale {:domain ["Mortos em combate"
                                                     "Mortos por outros motivos"
                                                     "Mortos"]}}},
                :mark     {:type    "bar"
                           :tooltip true}}

               {:width    200
                :encoding {:x      {:field "Tipo"
                                    :type  :nominal
                                    :scale {:domain ["Feridos em combate"
                                                     "Feridos por acidente"
                                                     "Feridos"]}}
                           :y      {:field "Baixas",
                                    :type  :quantitative}
                           :column {:field "Teatro" :title nil :header nil}
                           :color  {:field "Tipo"
                                    :scale {:domain ["Feridos em combate"
                                                     "Feridos por acidente"
                                                     "Feridos"]}}},
                :mark     {:type    "bar"
                           :tooltip true}}

               {:width    200
                :encoding {:x      {:field "Tipo"
                                    :type  :nominal
                                    :scale {:domain ["Doentes"
                                                     "Feridos por acidente e Doentes"
                                                     "Feridos e Doentes"]}}
                           :y      {:field "Baixas",
                                    :type  :quantitative}
                           :column {:field "Teatro" :title nil :header nil}
                           :color  {:field "Tipo"
                                    :scale {:domain ["Doentes"
                                                     "Feridos por acidente e Doentes"
                                                     "Feridos e Doentes"]}}},
                :mark     {:type    "bar"
                           :tooltip true}}

               ]})

;; ***


(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   "Mortos e Feridos"
               :anchor "middle"
               :offset 20}
   :width     200
   :data      {:values (:data q5i)}
   :config    {:legend {:labelLimit 1000}
               :axisX  {:labelAngle 0
                        :title      false}}
   :transform [{:fold ["Mortos em combate"
                       "Mortos por outros motivos"
                       "Feridos em combate"
                       "Feridos por acidente"]
                :as   ["Tipo" "Baixas"]}
               {:lookup "Tipo"
                :from   {:data   {:values [{"Tipo"  "Mortos em combate"
                                            "Grupo" ["Mortos"]}
                                           {"Tipo"  "Mortos por outros motivos"
                                            "Grupo" ["Mortos"]}
                                           {"Tipo"  "Feridos em combate"
                                            "Grupo" ["Feridos"]}
                                           {"Tipo"  "Feridos por acidente"
                                            "Grupo" ["Feridos"]}]}
                         :key    "Tipo"
                         :fields ["Grupo"]}}
               {:flatten ["Grupo"]}]
   :encoding  {:x      {:field "Grupo"
                        :type  :nominal
                        :scale {:paddingInner 0.3
                                :paddingOuter 0.5}}
               :y      {:field "Baixas",
                        :type  :quantitative
                        :stack true}
               :column {:field "Teatro"}
               :color  {:field  "Tipo"
                        :scale  {:range ["#843c39"
                                         "#e7ba94"
                                         "#ad494a"
                                         "#e7969c"]}
                        :legend {:orient :bottom}}},
   :mark      {:type    "bar"
               :tooltip true}})

(clerk/vl
  {:$schema   "https://vega.github.io/schema/vega-lite/v5.json",
   :title     {:text   "Feridos e Doentes"
               :anchor "middle"
               :offset 20
               }
   :width     200
   :data      {:values (:data q5i)}
   :config    {:legend {:labelLimit 1000}
               :axisX  {:labelAngle 0}
               }
   :transform [{:fold ["Doentes"
                       "Feridos por acidente"
                       "Feridos em combate"]
                :as   ["Tipo" "Baixas"]}
               {:calculate "indexof(['Doentes', 'Feridos por acidente', 'Feridos em combate'], datum.Tipo)",
                :as        "order"}]
   :encoding  {:x     {:field "Teatro"
                       :type  :nominal
                       :scale {:paddingInner 0.3
                               :paddingOuter 0.5}}
               :y     {:field "Baixas",
                       :type  :quantitative
                       :stack true}
               :color {:field "Tipo"
                       :scale {:range  ["#d6616b"
                                        "#e7ba94"
                                        "#843c39"]
                               :domain ["Doentes"
                                        "Feridos por acidente"
                                        "Feridos em combate"]}}
               :order {:field "order"
                       :type  "ordinal"}
               },
   :mark      {:type    "bar"
               :tooltip true}})




;; ***


(def q5ii (get data-files "avt77_70_ii.edn"))
(clerk/table (:data q5ii))

;; ## Quadro VI

(def q6 (get data-files "avt77_74.edn"))
(clerk/table (:data q6))

(clerk/vl
  {:$schema  "https://vega.github.io/schema/vega-lite/v5.json",
   :title    {:text   "Mortos por mil efectivos"
              :anchor "middle"
              :offset 20}
   :width    450
   :data     {:values (:data q6)}
   :config   {:legend {:labelLimit 1000}
              :axis   {:labelAngle 0}}
   :encoding {:x     {:field "Ano"}
              :y     {:field "Mortos por mil efectivos",
                      :type  :quantitative
                      ;:stack :normalize
                      ;:axis  {:format ".1~%"}
                      :title nil},
              :order {:field "Valor"}
              :color {:field  "Teatro"
                      :scale  {:range  ["#d6616b"
                                        "#e7ba94"
                                        "#843c39"]}
                      ;:legend {:orient    :bottom
                      ;         :direction :vertical}
                      }},
   :mark     {:type    "line"
              :tooltip true}})

;; ## Quadro VII

(def q7 (get data-files "avt77_79.edn"))
(clerk/table (:data q7))


;; ## Quadro VIII

(def q8 (get data-files "avt77_84.edn"))
(clerk/table (:data q8))

(clerk/vl
  {:$schema  "https://vega.github.io/schema/vega-lite/v5.json",
   :title    {:text   "Desertores"
              :anchor "middle"
              :offset 20}
   :width    450
   :data     {:values (:data q8)}
   :config   {:legend {:labelLimit 1000}
              :axis   {:labelAngle 0}}
   :encoding {:x     {:field "Ano"}
              :y     {:field "Desertores",
                      :type  :quantitative
                      ;:stack :normalize
                      ;:axis  {:format ".1~%"}
                      :title nil},
              :color {:field  "Teatro"
                      :scale  {:range  ["#d6616b"
                                        "#e7ba94"
                                        "#843c39"]}
                      ;:legend {:orient    :bottom
                      ;         :direction :vertical}
                      }},
   :mark     {:type    "line"
              :point true
              :tooltip true}})