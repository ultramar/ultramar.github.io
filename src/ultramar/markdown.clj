(ns ultramar.markdown
  (:require [clojure.set :as set]
            [clojure.string :as str]
            [tick.core :as t])
  (:import (com.vladsch.flexmark.ext.abbreviation AbbreviationExtension)
           (com.vladsch.flexmark.ext.anchorlink AnchorLinkExtension)
           (com.vladsch.flexmark.ext.footnotes FootnoteExtension)
           (com.vladsch.flexmark.ext.resizable.image ResizableImageExtension)
           (com.vladsch.flexmark.ext.toc TocExtension)
           (com.vladsch.flexmark.ext.typographic TypographicExtension)
           (com.vladsch.flexmark.ext.yaml.front.matter AbstractYamlFrontMatterVisitor YamlFrontMatterExtension)
           (com.vladsch.flexmark.html HtmlRenderer)
           (com.vladsch.flexmark.parser Parser)
           (com.vladsch.flexmark.profile.pegdown PegdownOptionsAdapter)
           (com.vladsch.flexmark.util.data MutableDataSet)
           (com.vladsch.flexmark.util.misc Extension)
           (com.vladsch.flexmark.ext.aside AsideExtension)
           (com.vladsch.flexmark.ext.tables TablesExtension)
           (com.vladsch.flexmark.ext.attributes AttributesExtension)))

(defn parse-date [v]
  (when v
    (t/parse-date-time v (t/formatter "yyyy-MM-dd HH:mm"))))


(defn make-flexmark []
  (let [extensions (->> [(YamlFrontMatterExtension/create)
                         (FootnoteExtension/create)
                         (TocExtension/create)
                         (TypographicExtension/create)
                         (AbbreviationExtension/create)
                         (AsideExtension/create)
                         (AttributesExtension/create)
                         (TablesExtension/create)
                         ;(ResizableImageExtension/create)
                         ]
                        (into-array Extension))
        options (doto (MutableDataSet. (PegdownOptionsAdapter/flexmarkOptions (int 65535) extensions))
                  (.set HtmlRenderer/SOFT_BREAK " ")
                  (.set AnchorLinkExtension/ANCHORLINKS_WRAP_TEXT false)
                  (.set TablesExtension/CLASS_NAME "md-table")
                  )]
    {:parser   (-> (Parser/builder options) (.build))
     :renderer (-> (HtmlRenderer/builder options) (.build))}))

(defn split-tags [tags]
  (when tags
    (->> (str/split tags #",")
         (mapv str/trim))))

(defn to-html
  ([s]
   (to-html (make-flexmark) s))
  ([{:keys [parser renderer]} s]
   (let [document (.parse parser s)
         visitor (AbstractYamlFrontMatterVisitor.)]
     (.visit visitor document)
     {:html     (.render renderer document)
      :metadata (-> (into {} (.getData visitor))
                    (set/rename-keys {"title"           :title
                                      "author"          :author
                                      "published"       :published
                                      "updated"         :updated
                                      "tags"            :tags
                                      "draft"           :draft

                                      "opcit-author"    :opcit/author
                                      "opcit-title"     :opcit/title
                                      "opcit-volume"    :opcit/volume
                                      "opcit-publisher" :opcit/publisher
                                      "opcit-year"      :opcit/year
                                      "opcit-loc"       :opcit/loc
                                      "opcit-link"      :opcit/link
                                      "opcit-worldcat"  :opcit/worldcat
                                      "opcit-goodreads" :opcit/goodreads
                                      })
                    (update-vals first)
                    (update :published parse-date)
                    (update :updated parse-date)
                    (update :tags split-tags))})))