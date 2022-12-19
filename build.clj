(ns build
  (:refer-clojure :exclude [compile])
  (:require [org.corfield.build :as bb]
            [clojure.tools.build.api :as b]))

(defn compile-java [_]
      (println "Compiling Java sources...")
      (b/javac {:src-dirs   ["src"]
                :class-dir  (bb/default-class-dir)
                :basis      (bb/default-basis)
                :javac-opts ["-target" "17" "-source" "17"]}))