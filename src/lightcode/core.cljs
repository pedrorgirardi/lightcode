(ns lightcode.core
  (:require 
   ["vscode" :as vscode]
   [datascript.core :as d]
   [lightcode.cmd :as cmd]))


(defn ClojureLanguageConfiguration []
  (this-as this
           (set! (.-wordPattern this) #"[^\[\]\(\)\{\};\s\"\\]+")
           (set! (.-indentationRules this) #js {:increaseIndentPattern #"[\[\(\{]"
                                                :decreaseIndentPattern nil})))


(defn- register-command [ctx cmd]
  (-> (.-commands vscode)
      (.registerCommand (-> cmd meta :cmd) #(cmd ctx))))
                 
                 
(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))

                 
(defn- cmd-setup [^js context ctx cmd]
  (->> (register-command ctx cmd)
       (register-disposable context)))
                 

(defn activate [^js context]
  (-> (.-languages vscode)
      (.setLanguageConfiguration "clojure" (ClojureLanguageConfiguration.)))
  
  (let [conn (d/create-conn {})
        ctx  {:conn conn}]
   (cmd-setup context ctx #'cmd/welcome))
  
  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)