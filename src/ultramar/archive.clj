(ns ultramar.archive
  (:require [hiccup.element :as html]
            [tick.core :as t]
            [ultramar.date :as date]))

(defn archive-entry [{:keys [title
                             published
                             url
                             draft]
                      :as   post-meta}]
  (-> [:article
       [:header
        (html/link-to url [:h4 (cond-> title draft (str " [DRAFT]"))])]
       [:footer
        [:time {:datetime (date/format-date-attrib published)}
         (date/format-date-archive published)]]]
      (with-meta post-meta)))

(defn archive-page [posts]
  (->> posts
       (vals)
       (map meta)
       (sort-by :published)
       (reverse)
       (mapv archive-entry)
       (group-by (comp #(vector :h2 %) str t/year :published meta))
       (sort-by key)
       (reverse)
       (mapcat (fn [[year-header entries]]
                 (into [year-header] entries)))
       (into [:div#blog-archives])))

(def generate
  {:enter (fn [{:keys [posts site-meta] :as ctx}]
            (-> ctx
                (update :page-map merge
                        (let [url "/archives.html"]
                          {url (-> (archive-page posts)
                                   (with-meta (merge site-meta
                                                     {:type  :archive
                                                      :url   url
                                                      :title "Arquivo | Ultramar"
                                                      :blurb "Entradas por ano"})))}))))})