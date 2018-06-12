(defproject lightcode.server "0.1.0-SNAPSHOT"
  :description "Light Code Server"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/main" "src/dev"]

  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [yada "1.2.13"]
                 [integrant "0.7.0-alpha2"]]

  :profiles {:dev {:dependencies [[cider/cider-nrepl "0.17.0"]
                                  [integrant/repl "0.3.1"]
                                  [io.aviso/pretty "0.1.34"]
                                  [com.bhauman/rebel-readline "0.1.1"]]}})
