(ns ultramar.core
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [hiccup.element :as html]
            [hiccup.page :as hiccup]
            [hickory.convert :as hc]
            [hickory.core :as hick]
            [hickory.select :as hs]
            [stasis.core :as stasis]
            [ultramar.markdown :as mdu])
  (:import (java.text Normalizer Normalizer$Form)))

(defn hickory-to-hiccup [data]
  (-> (hc/hickory-to-hiccup data)
      (with-meta (meta data))))

(defn first-paragraph [post-data]
  (->> post-data
       (hs/select (hs/child (hs/tag :p)))
       (first)
       (hc/hickory-to-hiccup)
       (walk/walk (fn [a]
                    (cond
                      (string? a) a
                      (vector? a) (filter string? a)))
                  identity)
       (flatten)
       (apply str)))

(defn md->hickory
  ([text]
   (md->hickory {:md-parser (mdu/make-flexmark)} text))
  ([{:keys [container-attribs
            md-parser]
     :as   _opts}
    text]
   (let [{:keys [html metadata]} (mdu/to-html md-parser text)]
     (-> (str "<div>" html "</div>")
         (hick/parse-fragment)
         (first)
         (hick/as-hickory)
         (update :attrs merge container-attribs)
         (with-meta metadata)))))

(defn key-to-html [s]
  (-> s
      (str/replace #"_" "-")
      (str/replace #".md" ".html")))

(defn update-doc-url [doc-map]
  (->> doc-map
       (map (fn [[url post]]
              [url (vary-meta post assoc :url url)]))
       (into {})))

(defn remove-drafts [data {:keys [drafts?]}]
  (if-not drafts?
    (->> data
         (remove (comp :draft meta val))
         (into {}))
    data))

(defn gather-md-data [src opts]
  (-> src
      (stasis/slurp-directory #".*\.md$")
      (update-keys key-to-html)
      (update-vals (partial md->hickory opts))
      (remove-drafts opts)
      (update-doc-url)))


(defn base-html [{:keys          [title
                                  description
                                  content
                                  canonical-url]
                  site-title     :site/title
                  site-subtitle  :site/subtitle
                  og-title       :meta.og/title
                  og-type        :meta.og/type
                  og-url         :meta.og/url
                  og-image       :meta.og/image
                  og-description :meta.og/description}]
  (hiccup/html5
    {:lang "pt"}
    [:head
     [:title title]
     [:meta {:name    "description"
             :content description}]
     [:meta {:name    "viewport"
             :content "width=device-width, initial-scale=1"}]
     [:meta {:charset "utf-8"}]

     [:meta {:property "og:title"
             :content  og-title}]
     [:meta {:property "og:type"
             :content  og-type}]
     [:meta {:property "og:description"
             :content  og-description}]
     [:meta {:property "og:url"
             :content  og-url}]
     [:meta {:property "og:image"
             :content  og-image}]
     [:meta {:property "og:locale"
             :content  "pt_PT"}]

     [:link {:rel "canonical" :href canonical-url}]

     [:link {:rel "stylesheet" :href "/css/main.css"}]
     [:link {:rel "stylesheet" :href "/css/normalize.css"}]

     [:link {:rel "preload" :as "font" :href "/fonts/futura-md-bt-medium-subset.woff2"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-It-subset.woff"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-It-subset.woff2"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-Regular-subset.woff"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-Regular-subset.woff2"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-Semibold-subset.woff"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-Semibold-subset.woff2"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-SemiboldIt-subset.woff"}]
     [:link {:rel "preload" :as "font" :href "/fonts/SourceSerif4Subhead-SemiboldIt-subset.woff2"}]

     [:link {:rel "preload" :as "image" :href "/images/cristo_simples.svg"}]

     [:script {:src "https://cdn.jsdelivr.net/npm/vega@5.22.1"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-lite@5.6.0"}]
     [:script {:src "https://cdn.jsdelivr.net/npm/vega-embed@6.21.0"}]]

    [:body

     [:div#content-area

      [:header
       [:div#title (str/upper-case site-title)]
       [:div#subtitle (str/upper-case site-subtitle)]
       [:img#title-image {:src   "/images/cristo_simples.svg"
                          :width "200"
                          :alt   "Cristo"}]
       [:nav
        [:ul
         [:li [:a {:href "/"} "Rol"]]
         [:li [:a {:href "/archives.html"} "Arquivo"]]
         [:li [:a {:href "/library.html"} "Biblioteca"]]]]]

      [:main
       content]

      [:footer
       ;(html/link-to "/feeds/all.rss.xml" "atom")
       ;"/"
       [:span
        (html/link-to "/feeds/all.rss.xml" "rss")
        " - 2022 - ultramaris@yahoo.com"]]]
     ]))


(defmulti format-page (comp :type meta))

(defmethod format-page :default [page]
  (let [{:keys            [title
                           description
                           url
                           type]
         site-title       :site/title
         site-description :site/description
         site-url         :site/url}
        (meta page)
        canonical (str site-url url)]
    (base-html
      {:site/title          site-title
       :site/subtitle       site-description

       :title               (str title "| Ultramar")
       :description         (or description site-description)
       :canonical-url       canonical
       :meta.og/title       title
       :meta.og/type        type
       :meta.og/url         canonical
       :meta.og/image       "images/ultramar_og.jpg"
       :meta.og/description (or description site-description)
       :content             page})))

(defmethod format-page :article [page]
  (let [{:keys            [title
                           url
                           type
                           blurb]
         site-title       :site/title
         site-description :site/description
         site-url         :site/url}
        (meta page)
        canonical (str site-url url)]
    (base-html
      {:site/title          site-title
       :site/subtitle       site-description

       :title               (str title "| Ultramar")
       :description         blurb
       :canonical-url       canonical
       :meta.og/title       title
       :meta.og/type        type
       :meta.og/url         canonical
       :meta.og/image       "images/ultramar_og.jpg"
       :meta.og/description blurb
       :content             page})))


(defn- trim-to [string-to-trim trim-value]
  (apply str (take trim-value string-to-trim)))

(defn- normalize [string-to-normalize]
  (let [normalized (Normalizer/normalize string-to-normalize Normalizer$Form/NFD)
        ascii (-> normalized
                  (str/replace #"[\.]+" "")
                  (str/replace #"[\P{ASCII}]+" ""))]
    (str/lower-case ascii)))

(defn slugify
  "Returns a slugified string. Takes two optional parameters:
  delimiter (str): string that interleaves valid words,
  trim-value (int): max url value."
  ([string-to-slugify] (slugify string-to-slugify "-"))
  ([string-to-slugify delimiter] (slugify string-to-slugify delimiter 250))
  ([string-to-slugify delimiter trim-value]
   (let [normalized (normalize string-to-slugify)
         split-s (str/split (str/triml normalized) #"[\p{Space}\p{P}]+")
         combined (str/join delimiter split-s)]
     (trim-to combined trim-value))))
