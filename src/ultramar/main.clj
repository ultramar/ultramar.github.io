(ns ultramar.main
  (:require [clojure.string :as str]
            [exoscale.interceptor :as interceptor]
            [optimus.assets :as assets]
            [optimus.export :as opt-export]
            [optimus.optimizations :as optimizations]
            [optimus.prime :as optimus]
            [optimus.strategies :refer [serve-live-assets]]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :as ring]
            [stasis.core :as stasis]
            [ultramar.core :as core]
            [ultramar.markdown :as mdu]
            [ultramar.posts :as posts]
            [ultramar.archive :as archive]
            [ultramar.index :as index]
            [ultramar.library :as library]
            [ultramar.datasets :as datasets]
            [ultramar.feed :as feed]))


(def live-app)

(defonce server (run-jetty #'live-app {:port 8080 :join? false}))

(defn serve-live [app]
  (alter-var-root #'live-app (constantly app)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-assets []
  (vec
    (concat
      (assets/load-assets "public" [#".+\.(css|js|png|jpg|svg|ico|woff|woff2)$"])
      (assets/load-assets "public" [#"CNAME"])
      (assets/load-assets "posts" [#".+\.(css|js|png|jpg|svg|woff|woff2)$"])
      ;(assets/load-assets "livros" [#".+\.(css|js|png|jpg|svg|woff)$"])
      )))

(def gather-assets
  {:enter (fn [ctx]
            (assoc ctx :assets (get-assets)))})

(def merge-pages
  {:enter (fn [{:keys [rss page-map] :as ctx}]
            (-> ctx
                (assoc :merged-page-sources
                       (stasis/merge-page-sources
                         {:pages (->> page-map
                                      (reduce-kv
                                        (fn [pages url content]
                                          (assoc pages url
                                                       (fn [arg]
                                                         (cond-> content
                                                                 (str/ends-with? url ".html")
                                                                 (core/format-page)))))
                                        {}))
                          :rss   rss}))))})


(defn prepare-assets [ctx]
  (interceptor/execute
    (merge {:base-dir        "resources"
            :posts-dir       "resources/posts"
            :books-dir       "resources/books"
            :datasets-dir    "resources/data"
            :site-meta       {:site/title       "Ultramar"
                              :site/description "Aspectos da política ultramarina portuguesa"
                              :site/url         "https://www.ultramar.blog"}
            :markdown-parser (mdu/make-flexmark)}
           ctx)
    [posts/gather
     archive/generate
     index/generate

     library/gather
     library/generate

     datasets/gather

     feed/generate-rss
     merge-pages

     gather-assets]))

(defn serve-site [{:keys [merged-page-sources assets]}]
  (-> merged-page-sources
      (stasis/serve-pages)
      (optimus/wrap (constantly assets) optimizations/all serve-live-assets)
      (ring/wrap-content-type)
      (serve-live)))

(defn export-site [{:keys [merged-page-sources assets]}]
  (let [assets (optimizations/all assets {})
        pages merged-page-sources
        target-dir "docs"]
    (stasis/empty-directory! target-dir)
    (opt-export/save-assets assets target-dir)
    (stasis/export-pages pages target-dir {:assets assets})))

(defn serve [_]
  (-> (prepare-assets {:drafts? true})
      (serve-site)))

(defn generate [_]
  (-> (prepare-assets {:drafts? false})
      (export-site)))


(comment
  (serve nil)
  )

(comment
  (generate nil)
  )