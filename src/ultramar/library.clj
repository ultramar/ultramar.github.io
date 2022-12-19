(ns ultramar.library
  (:require [babashka.fs :as fs]
            [charred.api :as json]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.zip :as zip]
            [hiccup.element :as html]
            [hickory.convert :as hc]
            [hickory.zip :as hz]
            [ultramar.core :as core]))

(def js-template "var spec=%s;var options={formatLocale:{\"decimal\":\",\",\"thousands\":\"\\u00a0\",\"grouping\":[3],\"currency\":[\"\",\"\\u00a0€\"]}};vegaEmbed('#%s',spec,options).then(function(result){}).catch(console.error);")

(defn embed-vega [hickory-data]
  (letfn [(viz-node? [zipper]
            (let [{:keys [tag attrs]} (zip/node zipper)]
              (and (= :div tag)
                   (= "vega-anchor" (:class attrs))
                   (not (contains? attrs :data-vega-wrapped)))))

          (load-spec [name]
            (-> (io/resource "specs")
                (fs/path (str name ".edn"))
                str
                slurp
                edn/read-string))

          (prep-spec [spec dataset]
            (-> spec
                (assoc-in [:data :url] (str "/dataset/" dataset ".json"))
                (json/write-json-str)))

          (wrap [node]
            {:type    :element,
             :attrs   {:class "vega-wrapper"},
             :tag     :div,
             :content [(assoc-in node [:attrs :data-vega-wrapped] true)]})

          (inject-vega-embed [zipper]
            (let [{:keys [id data-vega-spec data-vega-dataset]} (:attrs (zip/node zipper))
                  ;dataset (load-dataset data-vega-dataset)
                  spec (->
                         (load-spec data-vega-spec)
                         (prep-spec data-vega-dataset))
                  js-data (-> (format js-template spec id)
                              (html/javascript-tag))
                  hick-js-data (first (hc/hiccup-fragment-to-hickory [js-data]))]
              (-> zipper
                  ;(zip/edit wrap)
                  (zip/insert-right hick-js-data))))]
    (loop [curr (hz/hickory-zip hickory-data)]
      (cond (zip/end? curr) (zip/root curr)
            (viz-node? curr) (-> curr
                                 (inject-vega-embed)
                                 (zip/next)
                                 (recur))
            :else (recur (zip/next curr))))))


(defn numerate-paragraphs [hickory-data]
  (loop [curr (hz/hickory-zip hickory-data)
         pid 1]
    (cond (zip/end? curr) (zip/root curr)
          (-> curr zip/node :tag (= :p)) (-> curr
                                             (zip/edit #(update % :attrs merge {:id (str "p" pid)}))
                                             (zip/next)
                                             (recur (inc pid)))
          :else (recur (zip/next curr) pid))))

(def gather
  {:enter (fn [{:keys [site-meta books-dir markdown-parser drafts?] :as ctx}]
            (let [md-opts {:container-attribs {:class "book-content"}
                           :md-parser         markdown-parser
                           :drafts?           drafts?}
                  books (->> (core/gather-md-data books-dir md-opts)
                             (map (fn [[url doc]]
                                    (let [new-url (str "/book" url)
                                          doc-meta (-> (meta doc)
                                                       (assoc :url new-url)
                                                       (assoc :type :book)
                                                       (merge site-meta))
                                          doc (-> doc
                                                  (numerate-paragraphs)
                                                  (embed-vega)
                                                  (with-meta doc-meta))]
                                      [new-url doc])))
                             (into {}))]
              (-> ctx
                  (assoc :books books)
                  (update :page-map merge (update-vals books core/hickory-to-hiccup))
                  )))})

(defn book-entry [{:keys [title author date url draft] :as doc-meta}]
  (-> [:div.book-spine
       (html/link-to url [:h4 (cond-> title draft (str " [DRAFT]"))])
       [:div.book-author author]
       ;[:footer
       ; [:time {:datetime (format-date-attrib date)}
       ;  (format-date-archive date)]]
       ]
      (with-meta doc-meta)))

(defn library-page [posts]
  (->> posts
       (vals)
       (map meta)
       (sort-by :title)
       (mapv book-entry)
       (into [:div#book-shelf])))

(def generate
  {:enter (fn [{:keys [books site-meta] :as ctx}]
            (-> ctx
                (update :page-map merge
                        (let [url "/library.html"]
                          {url (-> (library-page books)
                                   (with-meta (merge site-meta
                                                     {:type        :library
                                                      :url         url
                                                      :title       "Biblioteca | Ultramar"
                                                      :description "Catálogo dos livros consultáveis"})))}))))})