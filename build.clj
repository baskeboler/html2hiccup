(ns build
  (:refer-clojure :exclude [test])
  (:require [org.corfield.build :as bb]))



(def lib 'net.clojars.html2hiccup/html2hiccup)
(def version
  (System/getenv "GITVERSION_SEMVER"))
(def main 'html2hiccup.html2hiccup)

(defn test "Run the tests." [opts]
  (bb/run-tests opts))

(defn ci "Run the CI pipeline of tests (and build the uberjar)." [opts]
  (-> opts
      (assoc :lib lib :version version :main main)
      (bb/run-tests)
      (bb/clean)
      (bb/uber)))
