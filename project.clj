(defproject kahvipaivakirja "0.1.0-SNAPSHOT"
  :description "Coffee tasting diary"
  :url "https://github.com/miikka/kahvipaivakirja"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [clj-time "0.8.0"]
                 [com.cemerick/friend "0.2.1" :exclusions [ring/ring-core]]
                 [compojure "1.1.8"]
                 [crypto-password "0.1.3"]
                 [endophile "0.1.2"]
                 [formative "0.8.8" :exclusions [clj-time]]
                 [hiccup "1.0.5"]
                 [org.clojure/tools.namespace "0.2.7"]
                 [org.postgresql/postgresql "9.3-1102-jdbc4"]
                 [ring "1.3.1"]
                 [ring-server "0.3.1"]
                 [yesql "0.4.0"]]
  :plugins [[lein-ring "0.8.11" :exclusions [org.clojure/clojure]]
            [cider/cider-nrepl "0.8.0-SNAPSHOT"]]
  :ring {:handler kahvipaivakirja.core/app})
