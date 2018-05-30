(ns lightcode.core
  (:require
   ["vscode" :as vscode]
   [datascript.core :as d]
   [lightcode.cmd :as cmd]
   [lightcode.out :as out]))


(defn ClojureLanguageConfiguration []
  (this-as this
           (set! (.-wordPattern this) #"[^\[\]\(\)\{\};\s\"\\]+")
           (set! (.-indentationRules this) #js {:increaseIndentPattern #"[\[\(\{]"
                                                :decreaseIndentPattern nil})))


(defn- register-command [sys cmd]
  (-> (.-commands vscode)
      (.registerCommand (-> cmd meta :cmd) #(cmd sys))))


(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))


(defn- cmd-setup [^js context sys cmd]
  (->> (register-command sys cmd)
       (register-disposable context)))


(defn activate [^js context]
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (ClojureLanguageConfiguration.)))

  (let [conn (d/create-conn {})

        ^js out (-> (.-window vscode)
                    (.createOutputChannel "Light Code"))

        sys {:conn conn
             :out  out}]

    (cmd-setup context sys #'cmd/switch-on)
    (cmd-setup context sys #'cmd/switch-off)

    (out/append-line out "Light Code is active.")))


(defn deactivate []
  nil)