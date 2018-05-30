(ns lightcode.core
  (:require
   ["vscode" :as vscode]
   [datascript.core :as d]
   [lightcode.cmd :as cmd]
   [lightcode.out :as out]))


(def *sys
  (atom {}))


(defn- register-command [*sys cmd]
  (vscode/commands.registerCommand (-> cmd meta :cmd) #(cmd *sys)))


(defn- register-disposable [^js context ^js disposable]
  (-> (.-subscriptions context)
      (.push disposable)))


(defn- reg-cmd [^js context *sys cmd]
  (->> (register-command *sys cmd)
       (register-disposable context)))


(defn activate [^js context]
  (let [clojure-language-configuration {:wordPattern #"[^\[\]\(\)\{\};\s\"\\]+"
                                        :indentationRules {:increaseIndentPattern #"[\[\(\{]"
                                                           :decreaseIndentPattern nil}}]

    (vscode/languages.setLanguageConfiguration "clojure"  (clj->js clojure-language-configuration)))


  (reg-cmd context *sys #'cmd/switch-on)
  (reg-cmd context *sys #'cmd/switch-off)

  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)