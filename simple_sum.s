Begin Assembly
ADDI R10, R0, 36
LW R9, 4000(R0)
ADDI R8, R0, 2000
ADDI R1, R0, 10
JAL AP
ADDI R2, R0, 9
ADDI R3, R0, 8
ADDI R4, R0, 7
LABEL AP
ADDI R5, R0, 6
ADDI R1, R1, -1
ADDI R6, R0, 5
SUB R7, R2, R3
JR R31
HALT
SW R1, 2000(R8)
HALT
End Assembly
Begin Data 4000 64
25
0
0
0
0
0
0
0
0
0
0
0
0
0
0
0
End Data