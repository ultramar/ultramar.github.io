(ns ultramar.date
  (:require [clojure.string :as str]
            [tick.core :as t]))


(defn format-date-attrib [d]
  (-> (t/formatter "yyyy-MM-dd")
      (t/format d)))

(defn format-date [d]
  (let [year (-> (t/formatter "yyyy")
                 (t/format d))
        month (-> (t/formatter "LLLL")
                  (t/format d)
                  (str/capitalize))
        day (-> (t/formatter "d")
                (t/format d))]
    (format "%s de %s de %s" day month year)))

(defn format-date-archive [d]
  (let [month (-> (t/formatter "LLLL")
                  (t/format d)
                  (str/capitalize))
        day (-> (t/formatter "d")
                (t/format d))]
    (format "%s de %s" day month)))


(defn html-date [date]
  [:time {:datetime (format-date-attrib date)}
   (format-date date)])

(defn date-preposition [date]
  (if (= 1 (t/day-of-month date))
    "a " "aos "))

(defn format-header-date-line [date update?]
  [:span
   (str (when update? "revisto ")
        (date-preposition date))
   (html-date date)])

(defn format-footer-date-line [{:keys [published updated]}]
  [:span
   "publicado "
   (date-preposition published)
   (html-date published)
   (when updated
     [:span
      [:span.date-revision " e revisto "]
      (date-preposition updated)
      (html-date updated)])])