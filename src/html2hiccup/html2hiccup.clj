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

(def f (frame :title "html 2 hiccup"))

(defn display [content]
  (config! f :content content)
  content)


(def available-syntaxes
  {:html    RSyntaxTextArea/SYNTAX_STYLE_HTML
   :clojure RSyntaxTextArea/SYNTAX_STYLE_CLOJURE})

(defn create-syntax-area [syntax]
  (doto (RSyntaxTextArea. 20 60)
    (.setSyntaxEditingStyle (get available-syntaxes syntax))
    (.setCodeFoldingEnabled true)
    (.setAutoIndentEnabled true)
    (.setLineWrap true)))

(def html-area (create-syntax-area :html))

(def clojure-area (create-syntax-area :clojure))
 
(defn code-pane [title area]
  (s/vertical-panel 
   :items [title (RTextScrollPane. area)]))

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
