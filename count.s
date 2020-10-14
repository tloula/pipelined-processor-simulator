-- R1 = i
Begin Assembly
ADDI R1, R0, 10
Label LOOP
ADDI R1, R1, -1
NOP
NOP
NOP
NOP
BEQ R1, R0, LOOP
NOP
NOP
NOP
NOP
HALT
End Assembly
Begin Data 5000 100
End Data