(ns html2hiccup.html2hiccup
  (:require [clojure.string :as string]
            [hickory.core :as h]
            [seesaw.core :as s :refer [action alert config config! frame pack!
                                       show! to-frame vertical-panel]])
  (:import [org.fife.ui.rsyntaxtextarea RSyntaxTextArea]
           [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))

(s/native!)

(defn read-stdin []
  (slurp *in*))


(def open-action (action
                  :handler (fn [e] (alert "I should open a new something."))
                  :name "Open ..."
                  :key  "menu O"
                  :tip  "Open a new something something."))
(def exit-action (action
                  :handler (fn [e] (.dispose (to-frame e)))
                  :name "Exit"
                  :tip  "Close this window"))

(def my-panel (vertical-panel
               :items ["Label" 
                       (action :handler (fn [e] (alert "Hello!")) :name "Button")
                       open-action
                       exit-action
                       "Another label"]))

(def f (frame :title "html 2 hiccup"))

(defn display [content]
  (config! f :content content)
  content)

(def area (s/text :multi-line? true 
                  :font "MONOSPACED-PLAIN-14"
                  :text "This is a text area
                         it 
                         is 
                         multilined"))

(def available-syntaxes
  {:html    RSyntaxTextArea/SYNTAX_STYLE_HTML
   :clojure RSyntaxTextArea/SYNTAX_STYLE_CLOJURE})

(defn create-syntax-area [syntax]
  (let [area (doto (RSyntaxTextArea. 20 60)
               (.setSyntaxEditingStyle (get available-syntaxes syntax))
               (.setCodeFoldingEnabled true)
               (.setAutoIndentEnabled true)
               (.setLineWrap true))]
    area))


(def html-area (create-syntax-area :html))

(def clojure-area (create-syntax-area :clojure))
 
(defn code-pane [title area]
  (s/vertical-panel 
   :items [(s/la)title (RTextScrollPane. area)]))

(def html-clojure-split-area 
  (s/top-bottom-split (code-pane "html" html-area)
                      (code-pane "hiccup" clojure-area)
                      :divider-location 1/3))

(defn code-handler [e]
  (let [t  (-> e .getSource (config :text))
        converted (-> t
                      h/parse-fragment
                      first
                      h/as-hiccup
                      str
                      (string/replace #"\"\\n+ *\"" ""))]
    (println "selection: " t)
    (println "converted: " converted)
    (-> clojure-area (.setText converted))))

(s/listen html-area
          :selection
          code-handler)

(defn  show-frame [] 
  (-> f pack! show!))

(defn gui []
  (display html-clojure-split-area)
  (show-frame))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (gui)
  #_(let [input (read-stdin)
        output (h/parse  input)] 
    (->> output
         h/as-hiccup
         (drop-while #(not= :html (first %)))
         first
         pp/pprint)))
