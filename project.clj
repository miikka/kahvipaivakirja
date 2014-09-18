(defproject kahvipaivakirja "0.1.0-SNAPSHOT"
  :description "Coffee tasting diary"
  :url "https://github.com/miikka/kahvipaivakirja"
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [com.cemerick/friend "0.2.1"]
                 [compojure "1.1.8"]
                 [crypto-password "0.1.3"]
                 [endophile "0.1.2"]
                 [hiccup "1.0.5"]
                 [org.postgresql/postgresql "9.3-1102-jdbc4"]
                 [ring "1.3.1"]
                 [ring-server "0.3.1"]
                 [yesql "0.4.0"]]
  :plugins [[lein-ring "0.8.11"]
            [cider/cider-nrepl "0.8.0-SNAPSHOT"]]
  :ring {:handler kahvipaivakirja.core/app})
