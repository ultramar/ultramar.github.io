(ns ultramar.feed
  (:require [clj-rss.core :as rss]
            [clojure.set :as set])
  (:import (java.time ZoneId)))


(def generate-rss
  {:enter (fn [{:keys [posts site-meta] :as ctx}]


            (let [{:site/keys [title description blurb url]} site-meta
                  feed (->> posts
                            (vals)
                            (sort-by (comp :published meta))
                            (reverse)
                            (map (fn [post]
                                   (let [post-meta (meta post)]
                                     (-> post-meta
                                         (update :published (fn [date]
                                                              (-> date
                                                                  (.atZone (ZoneId/of "Europe/Zurich"))
                                                                  (.toInstant))))
                                         (set/rename-keys {:published :pubDate})
                                         (assoc :description blurb)
                                         (select-keys [:title
                                                       :link
                                                       :description
                                                       :lastBuildDate
                                                       :pubDate])))))
                            (rss/channel-xml {:title       title
                                              :description description
                                              :link        url
                                              :feed-url    (str url "/feeds/all.rss.xml")}))]

              (-> ctx
                  (assoc :rss {"/feeds/all.rss.xml" feed}))))})
