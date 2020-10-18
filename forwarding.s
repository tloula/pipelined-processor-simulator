Begin Assembly
ADDI R1, R1, 1
ADDI R2, R2, 2
JAL Spot 
ADDI R3, R3, 3
ADDI R4, R4, 4
J aftJR
Label Spot
ADDI R5, R5, 5
ADDI R6, R6, 6
JR R31
LABEL aftJR
ADDI R7, R7, 7
ADDI R8, R8, 8
HALT
End Assembly
Begin Data 4000 4
25
End Data