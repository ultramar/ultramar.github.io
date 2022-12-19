(ns ultramar.index
  (:require [hiccup.element :as html]
            [ultramar.date :as date]))


(defn post-entry [post-data]
  (let [{title     :title
         published :published
         updated   :updated
         blurb     :blurb
         draft     :draft
         url       :url} (meta post-data)]
    (-> [:article
         [:header
          (html/link-to url [:h1 (cond-> title draft (str " [DRAFT]"))])]
         [:div.excerpt
          [:p blurb]]
         [:footer.entry-footer
          (html/link-to (str url "#titulo") [:i.read-prompt "Ler"])
          [:span.publish-info
           [:span
            (str (if updated "revisto " "publicado ")
                 (date/date-preposition (or updated published)))
            (date/html-date (or updated published))]]]])))


(defn pagination [{:keys [articles next-page previous-page]}]
  [:div.blog-index
   (into [:div.articles] articles)
   [:div.pagination
    [:a#previous-page {:href previous-page :class (when-not previous-page "invisible")} "← Recentes"]
    [:a#next-page {:href next-page :class (when-not next-page "invisible")} "Antigos →"]]])

(defn paginate [articles]
  (let [article-sets (partition 5 articles)
        total-pages (count article-sets)
        last-page (dec total-pages)
        page-url #(str "/index" (when-not (zero? %) %) ".html")
        site-meta (meta articles)]
    (->> article-sets
         (map-indexed
           (fn [page articles]
             [(page-url page)
              (cond
                (= 1 total-pages)
                [:div.blog-index
                 (into [:div.articles] articles)]

                (zero? page)
                (pagination {:articles  articles
                             :next-page (-> page inc page-url)})
                (= last-page page)
                (pagination {:articles      articles
                             :previous-page (-> page dec page-url)})
                :else
                (pagination {:articles      articles
                             :next-page     (-> page inc page-url)
                             :previous-page (-> page dec page-url)}))]))
         (map (fn [[url page]]
                [url (with-meta page (merge site-meta
                                            {:type  :index
                                             :url   url
                                             :title "Rol | Ultramar"}))]))
         (into {}))))

(defn index-pages [posts]
  (paginate
    (with-meta
      (->> posts
           (vals)
           (sort-by (comp :published meta))
           (reverse)
           (map post-entry))
      (meta posts))))

(def generate
  {:enter (fn [{:keys [posts site-meta] :as ctx}]
            (-> ctx
                (update :page-map merge (-> posts
                                            (with-meta site-meta)
                                            (index-pages)))))})