(define nop 0)
(define jmp 1)
(define jmz 2)
(define jlt 3)
(define jgt 4)
(define ldl 5)
(define lda 6)
(define ldi 7)
(define sta 8)
(define sti 9)
(define add 10)
(define sub 11)
(define mul 12)
(define div 13)
(define abs 14)
(define _sin 15)
(define atn 16)
(define dot 17)
(define crs 18)
(define sqr 19)
(define len 20)
(define dup 21)
(define drp 22)
(define cmp 23)
(define shf 24)
(define bld 25)
(define ret 26)
(define dbg 27)
(define nrm 28)
(define add.x 29)
(define add.y 30)
(define add.z 31)
(define end-check 999)

(define reg-pco 100)
(define reg-spd 101)
(define reg-pos 102)
(define reg-vel 103)
(define reg-col 104)
(define reg-nit 105)
(define reg-scc 106)
(define reg-sdr 107)
(define reg-and 108)
(define reg-ins 109)
(define reg-mdl 120)
(define reg-mdl-end 199)
(define reg-stp 200)
(define reg-stk 201)
(define reg-ndt 256)

(define (jelly-prog ins-per-frame l)
  (let ((p (foldl
            (lambda (i r)
              (let ((cur (car r))
                    (l (cadr r)))
                (if (eqv? (length cur) 3)
                    (list (list i) (append l (list (list->vector cur))))
                    (list (append cur (list i)) l))))
            (list '() '()) l)))
    (cond
     ((eq? (car (car p)) end-check)
      (pdata-set! "p" reg-ins (vector ins-per-frame 0 0))
      (define addr 0)
      (for-each
       (lambda (v)
         (pdata-set! "p" addr v)
         (set! addr (+ addr 1)))
       (cadr p)))
     (else (display "end check wrong ") (display p) (newline)))))

(clear)
(clear-colour (vector 0 0.2 0.5))
;(define p (build-cube))
(define jelly (build-jellyfish))

(define explode
  (list
   jmp 4 0           ;; goto code
   reg-mdl 0 0       ;; 1 address of current vertex
   reg-mdl 0 0       ;; 2 address of accum vertex (loop)
   0 0 0             ;; 3 influence

   ;; accum-loop:
   ldi  2  0         ;; 4 load current vertex
   ldi  1  0         ;; 5 load accum vertex
   sub  0  0         ;; 6 get the difference
   lda  3  0         ;; 7 load the accumulation
   add  0  0         ;; 8 accumulate
   nrm  0  0         ;; normalise

   sta  3  0         ;; 9 store accumulation

   ;; inc address
   add.x 2 1         ;; 10 load address add

   ;; loop section
   lda  2  0         ;; 15 load accum address
   ldl reg-mdl-end 0 ;; 14 load end address
   jlt  2  0         ;; 16 exit if greater than model end (relative address)
   jmp  4  0         ;; 17 accum-loop
   ;; end accum loop

   ;; normalise
   lda  3  0           ;; 18 load accum

   ;; make small
   ldl 0.05 0
   mul 0 0

   ldl 1 0   ;; subtract
   lda 1 0   ;; load current vertex address
   sub 0 0   ;; one
   sta 80 0 ;; store far away
   ldi 1 0   ;; load current
   ldi 80 0 ;; load previous
   sub 0 0   ;; get the difference
   ldl 0.5 0
   mul 0 0   ;; make small

   add 0 0   ;; add to accum result

   ;; do the animation
   ldi 1 0           ;; 24 load current vertex
   add 0 0           ;; 25 add the accum
   sti 1 0           ;; 26 write to model

   ;; reset the outer loop
   ldl 0 0           ;; 27
   sta 3 0           ;; 28 reset accum
   ldl reg-mdl 0     ;; 29
   sta 2 0           ;; 30 reset accum address

   add.x 1 1         ;; inc vertex address

   lda 1  0         ;; 36 load vertex address
   ldl reg-mdl-end 0 ;; 35 load end address
   jlt 2  0          ;; 37 if greater than model (relative address)
   jmp 4  0           ;; 38

   ;; reset current vertex
   ldl reg-mdl 0
   sta 1 0

   drp 0 0
   drp 0 0

   jmp 4 0
   end-check))

(define twirl
  (list
   jmp 3 0
   0 0 0   ;; time (increases by 1 each loop)
   2 2 -3  ;; shuffle data for converting (x y z) -> (z z x)
   ;; code follows to build a vertex by rotation around an angle based on the index
   lda 1 0       ;; load current time from address 0
   ldl 200.3 0   ;; load angle 135.3 (in degrees)
   mul 0 0       ;; multiply time by angle
   _sin 0 0       ;; makes (sin(angle) cos(angle) 0)
   ;; make a spiral by scaling up with time
   lda 1 0       ;; load time again
   ldl 0.05 0    ;; load 0.05
   mul 0 0       ;; multiply to get time*0.05
   mul 0 0       ;; mul rotation vector by time*0.05
   ;; move backward in z so we get some depth
   lda 1 0       ;; load the time again
   ldl 0.03 0    ;; load 0.03
   mul 0 0       ;; multiply the time by 0.01
   lda 2 0       ;; load the shuffle vec from address 1
   shf 0 0       ;; shuffle the x to z position
   add 0 0       ;; add (0 0 x) to set z on current position
   sti 1 reg-mdl ;; write position to model memory registers
   ;; increment the index by 1
   add.x 1 1
   jmp 3 0       ;; goto 2
   end-check   ))

(with-primitive
 jelly

 (pdata-index-map!
  (lambda (i p)
    (if (and (>= i reg-mdl) (< i reg-mdl-end))
        (srndvec)
        (vector 0 0 0)))
  "p")

 (jelly-prog 20000 explode)

 (pdata-map!
  (lambda (c)
    (rndvec)) ;(vector 0.7 0.7 0.7 0.4))
  "c")
 (hint-unlit)
 (hint-wire)
 (line-width 3))


(with-primitive
 (build-jellyfish)

 (pdata-index-map!
  (lambda (i p)
    (if (and (>= i reg-mdl) (< i reg-mdl-end))
        (srndvec)
        (vector 0 0 0)))
  "p")

 (jelly-prog 200 twirl)

 (pdata-map!
  (lambda (c)
    (rndvec)) ;(vector 0.7 0.7 0.7 0.4))
  "c")
 (hint-unlit)
 (hint-wire)
 (line-width 3))



(with-primitive
 jelly
  (translate (vector -0.3 1.4 0))
 (rotate (vector -40 -20 2))
 )

                                        ;(with-state
; (rotate (vector 45 45 0))
; (build-cube))

;(every-frame
; (with-primitive jelly (rotate (vector 2.2 2 1))))
