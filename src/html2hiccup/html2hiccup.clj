(ns html2hiccup.html2hiccup
  (:require [clojure.pprint :as pprint]
            [clojure.string :as string] 
            [seesaw.core :as s :refer [config config! frame menu menu-item
                                       menubar pack! separator show!
                                       vertical-panel]]
            [taipei-404.html :refer [html->hiccup minify-hiccup]])

  (:import [org.fife.ui.rsyntaxtextarea RSyntaxTextArea]
           [org.fife.ui.rtextarea RTextScrollPane])
  (:gen-class))

(declare clear-html clear-result f)


(s/native!)


(defn- create-app-frame []
  (frame :title "html 2 hiccup"
              :menubar
              (menubar :items
                       [(menu :text "File"
                              :items [(menu-item
                                       :text "Open")
                                      (separator)
                                      (menu-item
                                       :text "Exit"
                                       :listen [:action (fn [_] 
                                                          (s/invoke-now
                                                           (s/dispose! @f)))])])
                        (menu :text "Edit"
                              :items [(menu-item
                                       :text "Clear HTML"
                                       :listen [:action  (fn [_]
                                                           (s/invoke-now
                                                            (clear-html)))])
                                      (menu-item
                                       :text "Clear Result"
                                       :listen [:action  (fn [_]
                                                           (s/invoke-now
                                                            (clear-result)))])])])))



(def f (atom (create-app-frame)))

(defn display [content]
  (config! @f

           :content content
           :on-close :exit)
  content)


(def available-syntaxes
  {:html    RSyntaxTextArea/SYNTAX_STYLE_HTML
   :clojure RSyntaxTextArea/SYNTAX_STYLE_CLOJURE})

(defn syntax-area [syntax]
  (s/make-widget
   (doto (RSyntaxTextArea. 20 60)
     (.setSyntaxEditingStyle (get available-syntaxes syntax))
     (.setCodeFoldingEnabled true)
     (.setAutoIndentEnabled true)
     (.setLineWrap true))))


(def html-area (syntax-area :html))

(def clojure-area (syntax-area :clojure))

(defn code-pane [title area]
  (vertical-panel
   :items [title
           #_(s/label
              :text title
              :h-text-position :left
              :font "ARIAL-BOLD-24"
              :border (s/line-border  :color :red))

           (RTextScrollPane. area)]))


(def html-clojure-split-area
  (s/top-bottom-split (code-pane "html" html-area)
                      (code-pane "hiccup" clojure-area)
                      :divider-location 1/3))

(defn code-handler [e]
  (let [t  (-> e .getSource (config :text) string/trim)
        converted (-> t
                      html->hiccup
                      first
                      minify-hiccup
                      (clojure.pprint/write :stream nil))
        converted (if (= converted "nil")
                    ""
                    converted)]
    (println "selection: " t)
    (println "converted: " converted)
    (-> clojure-area 
        (.setText (or  converted "")))))

(defn clear-result []
  (-> clojure-area (.setText "")))

(defn clear-html []
  (-> html-area
      (.setText "")))


(s/listen html-area
          :selection
          code-handler)

(defn  show-frame []
  (-> @f pack! show! s/move-to-front!))

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

(defn reset-frame! []
  (s/config! @f :on-close :hide)
  (s/dispose! @f)
  (reset! f (create-app-frame))
  (gui))

(comment
  (swap! f #(s/config! % :on-close :hide))
  (gui)

  (reset-frame!)

  (seesaw.core/dispose! @f))

