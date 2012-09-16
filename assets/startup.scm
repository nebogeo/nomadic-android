(define cube 
  (with-state
   (colour (vector 1 0.5 0.8))
   (build-cube)))
  
(every-frame 
 (with-primitive 
  cube
  (rotate (vector 1.2 1 2))))