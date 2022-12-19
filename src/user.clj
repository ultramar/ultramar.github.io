(ns user
  "Userspace functions you can run by default in your local REPL."
  (:require [nextjournal.clerk :as clerk]
            [babashka.fs :as fs]))


; or let Clerk watch the given `:paths` for changes
(clerk/serve! {:watch-paths    ["src/ultramar"]
               :show-filter-fn (fn [path]
                                 (or (= "data.clj" (fs/file-name path))
                                     (fs/starts-with? path "src/ultramar/notebooks")))})


