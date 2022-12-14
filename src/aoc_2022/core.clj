(ns aoc-2022.core
  (:require [aoc-2022.utils :refer [input]]
            [clojure.string :as str]))

(defn day1 [actual? part]
  (let [input (input 1 {:lines? false :actual? actual?})
        elves (->> (str/split input #"\n\n")
                   (map str/split-lines)
                   (map #(map read-string %))
                   (map (partial apply +)))]
    (case part
      1 (apply max elves)
      2 (apply + (take 3 (sort > elves))))))

(defn day2 [actual? part]
  (let [input (input 2 {:lines? true :actual? actual?})
        match-result1 (fn [a b]
                        (let [b ({\X \A
                                  \Y \B
                                  \Z \C} b)
                              ab (str a b)]
                          (if (= a b)
                            3
                            (case ab
                              "AB" 6
                              "AC" 0
                              "BC" 6
                              "BA" 0
                              "CB" 0
                              "CA" 6
                              0))))
        rps (fn [a res]
              (case res
                \X (char (+ 65 (mod (int a) 3)))
                \Y a
                \Z (char (+ 65 (mod (+ 2 (int a)) 3)))))
        val {\X 1
             \Y 2
             \Z 3
             \A 1
             \B 2
             \C 3}
        res-1 (loop [input input
                     acc 0]
                (if (> (count input) 0)
                  (recur (rest input)
                         (+ acc (val (last (first input))) (match-result1 (ffirst input) (last (first input)))))
                  acc))
        res-2 (loop [input input
                     acc 0]
                (if (empty? input)
                  acc
                  (let [mine (rps (ffirst input) (last (first input)))]
                    (recur (rest input)
                           (+ acc (val mine)
                              (case (last (first input))
                                \X 0
                                \Y 3
                                \Z 6))))))]
    (case part
      1 res-1
      2 res-2)))

(defn day3 [actual? part]
  (let [char-to-prio (fn [char]
                       (let [i (int char)]
                         (if (>= (int \Z) i)
                           (- i 38)
                           (- i 96))))
        input (input 3 {:actual? actual?})]
    (case part
      1 (reduce #(+ %1 (let [h1 (apply str (take (/ (count %2) 2) %2))
                             h2 (apply str (drop (/ (count %2) 2) %2))]
                         (char-to-prio (first (for [x h1
                                                    :when (str/includes? h2 (str x))]
                                                x))))) 0 input)
      2 (loop [input input
               acc 0]
          (if (empty? input)
            acc
            (recur
             (drop 3 input)
             (+ acc (first (let [elves (take 3 input)]
                             (for [x (first elves)
                                   :when (and
                                          (str/includes? (nth elves 1) (str x))
                                          (str/includes? (nth elves 2) (str x)))]
                               (char-to-prio x)))))))))))

(defn day4 [actual? part]
  (let [input (input 4
                     {:actual? actual?})
        pairs (->> input
                   (map (fn [pair]
                          (->> (str/split pair #",")
                               (map #(str/split % #"-"))
                               (map #(map read-string %))
                               (map (fn [[start end]]
                                      (range start (inc end))))
                               (sort-by (comp - count))))))]
    (case part
      1 (count (filter (fn [pair]
                         (= (second pair) (filter (set (first pair)) (second pair)))) pairs))
      2 (count (filter (fn [pair]
                         (seq (filter (set (first pair)) (second pair)))) pairs)))))

(defn day5 [actual? part]
  (let [input (input 5 {:actual? actual? :lines? false})
        [rows moves] (mapv str/split-lines (str/split input #"\n\n"))
        stacks (into [] (loop [stacks (repeat (Character/digit (last (str/trim (last rows))) 10) [])
                               rows rows]
                          (if (or (= 1 (count rows)) (empty? rows))
                            stacks
                            (recur
                             (map-indexed
                              (fn [i stack]
                                (let [char (nth (first rows) (+ 1 (* 4 i)))]
                                  (if (= \space char)
                                    stack
                                    (conj stack char))))
                              stacks)
                             (rest rows)))))
        move-stacks (fn [one-by-one?]
                      (apply str (map first (loop [stacks stacks
                                                   moves moves]
                                              (if (empty? moves)
                                                stacks
                                                (recur
                                                 (let [move (str/split (first moves) #" ")
                                                       amount (read-string (nth move 1))
                                                       from (read-string (nth move 3))
                                                       to (read-string (nth move 5))
                                                       moved (cond-> (take amount (nth stacks (dec from)))
                                                               one-by-one? reverse)]
                                                   (-> stacks
                                                       (update-in [(dec from)] (partial drop amount))
                                                       (update-in [(dec to)] #(concat moved %))))
                                                 (rest moves)))))))]
    (case part
      1 (move-stacks true)
      2 (move-stacks false))))

(defn day6 [input part]
  (let [mark-length (case part 1 4 2 14)]
    (loop [i mark-length]
      (if (or (= i (count input))
              (=  mark-length (count (set (subs input (- i mark-length) i)))))
        i
        (recur (inc i))))))

(defn day8 [actual? part]
  (let [input (map #(map read-string (str/split % #"")) (input 8 {:actual? actual?}))
        get-tree-count (fn [tree
                            trees]
                         (loop [acc 0
                                trees trees]
                           (cond
                             (nil? (first trees))
                             acc
                             (<= tree (first trees))
                             (inc acc)
                             :else
                             (recur (inc acc) (rest trees)))))]
    (case part
      1
      (loop [i 0
             input1 input
             acc 0]
        (if (empty? input1)
          acc
          (recur
           (inc i)
           (rest input1)
           (let [row (first input1)]
             (cond
               (or (= i 0)
                   (= 1 (count input1)))
               (+ acc (count row))
               :else
               (+ acc (loop [acc 0
                             j 0
                             row1 row]
                        (if (empty? row1)
                          acc
                          (recur
                           (let [tree (first row1)]
                             (cond (or (= j 0)
                                       (= 1 (count row1)))
                                   (inc acc)

                                   (or
                                ;up
                                    (> tree (apply max
                                                   (map #(nth (nth input %) j) (reverse (range 0 i)))))
                                ; down
                                    (> tree (apply max
                                                   (map #(nth (nth input %) j) (range (inc i) (count input)))))
                               ; right 
                                    (> tree (apply max
                                                   (map #(nth (nth input i) %) (range (inc j) (count row)))))
                               ; left
                                    (> tree
                                       (apply max
                                              (map #(nth (nth input i) %) (reverse (range 0 j))))))

                                   (inc acc)
                                   :else acc))

                           (inc j)
                           (rest row1))))))))))

      2 (loop [i 0
               j 0
               high-score 0]
          (let [last-i? (= i (dec (count input)))
                last-j? (= j (dec (count (first input))))
                tree (nth (nth input i) j)]
            (if (and last-i?
                     last-j?)
              high-score
              (recur
               (if last-j?
                 (inc i)
                 i)
               (if last-j?
                 0
                 (inc j))
               (max high-score
                    (*
                     ;up
                     (get-tree-count tree (map #(nth (nth input %) j) (reverse (range 0 i))))
                     ;down
                     (get-tree-count tree (map #(nth (nth input %) j) (range (inc i) (count input))))
                     ;left
                     (get-tree-count tree (map #(nth (nth input i) %) (range (inc j) (count (first input)))))
                     ;right
                     (get-tree-count tree (map #(nth (nth input i) %) (reverse (range 0 j)))))))))))))

(defn day10 [actual? part]
  (let [input (input 10 {:actual? actual?})
        result
        (loop [input1 input
               last-command "noop"
               acc []]
          (if (empty? input1)
            acc
            (recur
             (rest input1)
             (first input1)
             (let [[lc lv] (last acc)
                   [command _] (str/split (first input1) #" ")
                   [_ cv] (str/split last-command #" ")
                   cv (when cv (read-string cv))
                   cycles (if (= "noop" command)
                            1
                            2)]
               (apply conj acc
                      (map (fn [c]
                             [(+ (or lc 0) c) (+ (or lv 1) (or cv 0))]) (range 1 (inc cycles))))))))
        pixel (fn [i sprite]
                (if (#{(dec sprite) sprite (inc sprite)} i)
                  "#"
                  "."))]
    (case part
      1
      (apply + (map (fn [[a b]] (* a b)) (filter (fn [[c _]] (#{20 60 100 140 180 220} c)) result)))
      2
      (for [row (range 0 6)]
        (println (apply str (map (fn [i]
                                   (pixel i (second (nth result (+ (* 40 row) i))))) (range 0 40))))))))

(defn day11 [actual? part]
  (let [input (input 11 {:lines? false :actual? actual?})
        monkeys (str/split input #"\n\n")
        monkeys (mapv (fn [monkey]
                        (let [monkey (str/split-lines monkey)]
                          {:id (read-string (last (str/split (subs (first monkey) 0 (dec (count (first monkey)))) #" ")))
                           :inspected 0
                           :items (map #(bigint (read-string %))
                                       (-> (second monkey)
                                           (str/split #":")
                                           second
                                           (str/trim)
                                           (str/split #",")))
                           :op (let [op (str/trim (last (str/split (nth monkey 2) #"=")))
                                     [_num1 op num2] (str/split op #" ")]
                                 (fn [old]
                                   ((case op
                                      "*"
                                      *
                                      "+"
                                      +) old (if (= "old" num2) old (read-string num2)))))
                           :div (-> (nth monkey 3)
                                    (str/split #" ")
                                    last
                                    read-string)
                           :targets (map #(read-string (last (str/split % #" "))) (take-last 2 monkey))})) monkeys)
        cap (apply * (concat (map #(bigint (:div %)) monkeys)
                             (flatten (map :items monkeys))))
        do-turn (fn [mi monkeys]
                  (let [monkey (nth monkeys mi)
                        items (case part
                                1 (map (fn [n] (int (/ ((:op monkey) n) 3))) (:items monkey))
                                2 (map #(mod ((:op monkey) %) cap) (:items monkey)))
                        monkey (assoc monkey :items '())
                        monkey (update monkey :inspected (partial + (count items)))
                        items-moved (map (fn [item] [(if (= 0 (mod item (:div monkey)))
                                                       (first (:targets monkey))
                                                       (second (:targets monkey))) item]) items)]
                    (loop [items items-moved
                           monkeys (assoc-in monkeys [mi] monkey)]
                      (if (empty? items)
                        monkeys
                        (recur (rest items)
                               (update monkeys (ffirst items) (fn [monkey] (update monkey :items #(conj % (last (first items)))))))))))
        do-round (fn [monkeys] (loop [ids (range 0 (count monkeys))
                                      acc monkeys]
                                 (if (empty? ids)
                                   acc
                                   (recur
                                    (rest ids)
                                    (do-turn (first ids) acc)))))]
    (apply * (take 2 (sort > (map :inspected (nth (iterate do-round monkeys) (case part 1 20 2 10000))))))))
