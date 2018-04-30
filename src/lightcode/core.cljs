(ns lightcode.core
  (:require ["vscode" :refer (commands window)]))

(defn cmds [context]
  {"lightcode.welcome" #(.showInformationMessage window "Light Code")})


(defn activate [context]
  (doseq [[cmd f] (cmds context)]
    (.registerCommand commands cmd f))
  
  (js/console.log "Light Code is active."))


(defn deactivate []
  nil)