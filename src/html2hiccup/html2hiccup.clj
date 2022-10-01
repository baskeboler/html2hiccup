(ns html2hiccup.html2hiccup
  (:require [seesaw.border :as sb]
            [seesaw.core :as s :refer [config config! frame menu menu-item
                                       menubar pack! separator show!
                                       vertical-panel]]
            [taipei-404.html :refer [html->hiccup minify-hiccup]]
            [clojure.pprint :as pprint])
  (:import [org.fife.ui.rsyntaxtextarea RSyntaxTextArea]
           [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))

(declare clear-html clear-result)

(s/native!)

(defn read-stdin []
  (slurp *in*))

(def f (frame :title "html 2 hiccup"
              :menubar (menubar :items [(menu :text "File"
                                              :items [(menu-item :text "Open")
                                                      (separator)
                                                      (menu-item :text "Exit")])
                                        (menu :text "Edit"
                                              :items [(menu-item :text "Clear HTML"
                                                                 :listen [:action #(clear-html)])
                                                      (menu-item :text "Clear Result"
                                                                 :listen [:action #(clear-result)])])])))

(defn display [content]
  (config! f 
           :content content
           :on-close :exit)
  content)


(def available-syntaxes
  {:html    RSyntaxTextArea/SYNTAX_STYLE_HTML
   :clojure RSyntaxTextArea/SYNTAX_STYLE_CLOJURE})

(defn syntax-area [syntax]
  (doto (RSyntaxTextArea. 20 60)
    (.setSyntaxEditingStyle (get available-syntaxes syntax))
    (.setCodeFoldingEnabled true)
    (.setAutoIndentEnabled true)
    (.setLineWrap true)))

(def html-area (syntax-area :html))

(def clojure-area (syntax-area :clojure))
 
(defn code-pane [title area]
  (s/vertical-panel 
   :items [(s/label :text title
                    :h-text-position :center
                    :font "ARIAL-BOLD-24"
                    :halign :left
                    :border (sb/line-border  :color :red))
           (RTextScrollPane. area)]))


(def html-clojure-split-area 
  (s/top-bottom-split (code-pane "html" html-area)
                      (code-pane "hiccup" clojure-area)
                      :divider-location 1/3))

(def line-break "
")

(defn code-handler [e]
  (let [t  (-> e .getSource (config :text) string/trim)
        converted (-> t
                      html->hiccup
                      first 
                      minify-hiccup
                      (clojure.pprint/write :stream nil)
                      ;; h/parse-fragment
                      ;; first
                      ;; h/as-hiccup
                      ;; str
                      ;; (string/replace #"\"\\n+ *\"" "")
                      ;; str
                      )]
    (println "selection: " t)
    (println "converted: " converted)
    (-> clojure-area (.setText converted))))

(defn clear-result [] 
  (-> clojure-area (.setText "")))

(defn clear-html []
  (-> html-area 
      (.setText "")))


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

(comment 
  (gui)
  
  
  (seesaw.core/dispose! f)
  )