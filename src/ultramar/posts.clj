(ns ultramar.posts
  (:require [clojure.string :as str]
            [clojure.zip :as zip]
            [hickory.convert :as hc]
            [hickory.zip :as hz]
            [ultramar.core :as core]
            [ultramar.date :as date]))

(defn post [post-data]
  (let [{title     :title
         published :published
         updated   :updated
         url       :url
         :as       post-meta} (meta post-data)]
    (-> [:article.post
         [:header
          [:h1 title]
          (date/format-header-date-line (or updated published) (some? updated))]
         [:section
          post-data]
         [:footer
          (date/format-footer-date-line post-meta)]]
        (with-meta post-meta))))

(defn format-opcit [{:opcit/keys [author
                                  title
                                  volume
                                  publisher
                                  year
                                  loc
                                  worldcat
                                  goodreads
                                  link]}]
  [:section.opcit
   [:h5 "OPUS CITATUM"]
   (->> [(when title
           [:span.opcit-title title])
         (when volume
           (str ", " volume))]
        (remove nil?)
        (into [:span]))
   [:span author]
   [:span (->> [publisher year]
               (remove nil?)
               (str/join ", "))]
   [:span (->> [loc]
               (str/join ", "))]
   (->> [(when worldcat
           [:a {:href worldcat} "WorldCat"])
         (when goodreads
           [:a {:href goodreads} "Goodreads"])
         (when link
           [:a {:href link} "Link"])
         ]
        (remove nil?)
        (into [:div.opcit-ext]))])

(defn inject-opcit [hickory-data opcit]
  (letfn [(cit-node? [zipper]
            (let [{:keys [attrs]} (zip/node zipper)]
              (= "cit" (:id attrs))))
          (inject [zipper]
            (let [hick-opcit (first (hc/hiccup-fragment-to-hickory [opcit]))]
              (-> zipper
                  (zip/insert-left hick-opcit))))
          (wrap-cit [zipper]
            (let [siblings (zip/rights zipper)
                  new-node (zip/make-node (zip/next zipper)
                                          {:type  :element
                                           :tag   :section
                                           :attrs {:id "cit" :class "book-content"}}
                                          siblings)]
              (loop [current (zip/rightmost zipper)]
                (cond
                  (= "cit" (get-in (zip/node current) [:attrs :id]))
                  (zip/replace current new-node)
                  :else
                  (recur (zip/remove current))))))]
    (loop [curr (hz/hickory-zip hickory-data)]
      (cond (zip/end? curr) (zip/root curr)
            (cit-node? curr) (-> curr
                                 (inject)
                                 (wrap-cit)
                                 (zip/root))
            :else (recur (zip/next curr))))))


(def url-replacements {"/pires-de-lima-sobre-o-brasil-da-2-grande-guerra.html"
                       "/pires-de-lima-sobre-o-brasil-da-2a-grande-guerra.html"})

(def gather
  {:enter (fn [{:keys [site-meta posts-dir markdown-parser drafts?] :as ctx}]
            (let [md-opts {:container-attribs {:class "article-content"}
                           :md-parser         markdown-parser
                           :drafts?           drafts?}
                  posts (->> (core/gather-md-data posts-dir md-opts)
                             (map (fn [[url doc]]
                                    (let [opcit (format-opcit (meta doc))]
                                      [url (inject-opcit doc opcit)])))
                             (map (fn [[_ doc]]
                                    (let [{:keys [title]} (meta doc)
                                          new-url (str "/" (core/slugify title) ".html")
                                          new-url (get url-replacements new-url new-url)
                                          doc (-> doc
                                                  (vary-meta assoc :url new-url)
                                                  (vary-meta assoc :type :article)
                                                  (vary-meta assoc :blurb (core/first-paragraph doc))
                                                  (vary-meta merge site-meta))]
                                      [new-url doc])))
                             (into {}))]
              (-> ctx
                  (assoc :posts posts)
                  (update :page-map merge (update-vals posts (comp post core/hickory-to-hiccup))))))})