(defproject kahvipaivakirja "0.1.0-SNAPSHOT"
  :description "Coffee tasting diary"
  :url "https://github.com/miikka/kahvipaivakirja"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [compojure "1.1.8"]
                 [hiccup "1.0.5"]
                 [markdown-clj "0.9.47"]]
  :plugins [[lein-ring "0.8.11"]]
  :ring {:handler kahvipaivakirja.core/app})
