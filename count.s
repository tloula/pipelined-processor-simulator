-- R1 = i
Begin Assembly
ADDI R1, R0, 10
LABEL LOOP
NOP
NOP
NOP
NOP
BEQ R1, R0, END
NOP
NOP
NOP
NOP
ADDI R1, R1, -1
NOP
NOP
NOP
NOP
J LOOP
ADDI R2, R0, 2
ADDI R3, R0, 3
NOP
NOP
NOP
LABEL END
HALT
End Assembly
Begin Data 5000 100
End Data