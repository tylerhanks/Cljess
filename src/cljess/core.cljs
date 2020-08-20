(ns cljess.core
  (:require [reagent.core :as r]
            [reagent.dom :as rDOM]))

(def piece-image
  {:wp "https://upload.wikimedia.org/wikipedia/commons/4/45/Chess_plt45.svg"
   :bp "https://upload.wikimedia.org/wikipedia/commons/c/c7/Chess_pdt45.svg"
   :wn "https://upload.wikimedia.org/wikipedia/commons/7/70/Chess_nlt45.svg"
   :bn "https://upload.wikimedia.org/wikipedia/commons/e/ef/Chess_ndt45.svg"
   :wb "https://upload.wikimedia.org/wikipedia/commons/b/b1/Chess_blt45.svg"
   :bb "https://upload.wikimedia.org/wikipedia/commons/9/98/Chess_bdt45.svg"
   :wr "https://upload.wikimedia.org/wikipedia/commons/7/72/Chess_rlt45.svg"
   :br "https://upload.wikimedia.org/wikipedia/commons/f/ff/Chess_rdt45.svg"
   :wq "https://upload.wikimedia.org/wikipedia/commons/1/15/Chess_qlt45.svg"
   :bq "https://upload.wikimedia.org/wikipedia/commons/4/47/Chess_qdt45.svg"
   :wk "https://upload.wikimedia.org/wikipedia/commons/4/42/Chess_klt45.svg"
   :bk "https://upload.wikimedia.org/wikipedia/commons/f/f0/Chess_kdt45.svg"})

(def piece-color
  {:wp 1 :bp 0
   :wn 1 :bn 0
   :wb 1 :bb 0
   :wr 1 :br 0
   :wq 1 :bq 0
   :wk 1 :bk 0})

;;map from board position (e.g. e4) to coordinate in game-state array (e.g. [3 4])
(def board-to-coord
  {:a8 [0 0] :b8 [0 1] :c8 [0 2] :d8 [0 3] :e8 [0 4] :f8 [0 5] :g8 [0 6] :h8 [0 7]
   :a7 [1 0] :b7 [1 1] :c7 [1 2] :d7 [1 3] :e7 [1 4] :f7 [1 5] :g7 [1 6] :h7 [1 7]
   :a6 [2 0] :b6 [2 1] :c6 [2 2] :d6 [2 3] :e6 [2 4] :f6 [2 5] :g6 [2 6] :h6 [2 7]
   :a5 [3 0] :b5 [3 1] :c5 [3 2] :d5 [3 3] :e5 [3 4] :f5 [3 5] :g5 [3 6] :h5 [3 7]
   :a4 [4 0] :b4 [4 1] :c4 [4 2] :d4 [4 3] :e4 [4 4] :f4 [4 5] :g4 [4 6] :h4 [4 7]
   :a3 [5 0] :b3 [5 1] :c3 [5 2] :d3 [5 3] :e3 [5 4] :f3 [5 5] :g3 [5 6] :h3 [5 7]
   :a2 [6 0] :b2 [6 1] :c2 [6 2] :d2 [6 3] :e2 [6 4] :f2 [6 5] :g2 [6 6] :h2 [6 7]
   :a1 [7 0] :b1 [7 1] :c1 [7 2] :d1 [7 3] :e1 [7 4] :f1 [7 5] :g1 [7 6] :h1 [7 7]})

(def starting-position
  [[:br :bn :bb :bq :bk :bb :bn :br]
   [:bp :bp :bp :bp :bp :bp :bp :bp]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [0 0 0 0 0 0 0 0]
   [:wp :wp :wp :wp :wp :wp :wp :wp]
   [:wr :wn :wb :wq :wk :wb :wn :wr]])

;;(defmulti legal-moves (fn [pos board] (get-piece pos)))

;;(defmethod legal-moves [:bk :wk] [pos board])

#_(defn gen-legal-rook-moves [pos color board]
  (let [[x y] pos]
    (let [legal-moves #{}]
      (let [not-blocked? true]
        (for [i (range x 8) :let [piece (get-piece-coord [i y])] :while not-blocked?]
          (if (zero? piece) (conj legal-moves [i y]) ))))))

(defonce game-state (r/atom nil))

(defn new-game [] (reset! game-state starting-position))

(defonce square-selection (r/atom nil))

(defn get-piece [pos] (cond (vector? pos) (get-in @game-state pos) (keyword? pos) (let [p (board-to-coord pos)] (get-in @game-state p))))

(defn move-piece [from to]
  (let [piece (get-piece from)]
    (swap! game-state (fn [state pos piece] (assoc-in state pos piece)) (cond (keyword? from) (board-to-coord from) (vector? from) from) 0)
    (swap! game-state (fn [state pos piece] (assoc-in state pos piece)) (cond (keyword? to) (board-to-coord to) (vector? to) to) piece)))

(defmulti legal-moves (fn [pos board] (get-piece pos)))
(defmethod legal-moves [:br :wr] [pos board]
  (let [[x y] pos moves #{} color (piece-color (get-piece pos)) blocked? (atom false)]
    (for [i (range x 8) :let [piece (get-piece [i y])] :while (not @blocked?)]
      (if (zero? piece) (conj moves [i y])
          (if (= color (piece-color piece)) (reset! blocked? true) ((reset! blocked? true) (conj moves [i y])))))))

(defmethod legal-moves [:bk :wk] [pos board]
  (let [piece (get-piece pos) moves #{} [x y] pos]))


(defn square [piece coord color]
  [:button {:class (str color "-square")
            :on-click #((if (and (nil? @square-selection) (zero? (get-piece coord))) nil
                            (if (nil? @square-selection) (reset! square-selection coord)
                                ((move-piece @square-selection coord) (reset! square-selection nil)))))}
   [:img {:src piece}]])

(defn board []
  (let [v ["dark" "light"]] (into [:div] (for [i (range 8)] [:div {:class "board-row"} (map-indexed (fn [j el] (square (piece-image el) [i j] (v (mod (+ i j) 2)))) (nth @game-state i))]))))

(defn app []
  [:div
   [:h1 "Cljess!"]
   [board]
   [:button {:on-click #(new-game)} "New Game"]])

(defn init! []
  (new-game)
  (r/render [app] (.getElementById js/document "app")))

(init!)
