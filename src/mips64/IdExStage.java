package mips64;

public class IdExStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode;
    int regAData;
    int regBData;
    int immediate;

    int[] regs = new int[32];   // THE HECK G, WHY ARE THESE HERE!!!

    public IdExStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean getShouldWriteBack() {
        return this.shouldWriteback;
    }

    public int getInstPC(){
        return instPC;
    }

    public int getIntRegister(int regNum) {
        return this.regs[regNum];
    }

    public int getRegAData() {
        return this.regAData;
    }

    public int getRegBData() {
        return this.regBData;
    }

    public int getImmediate() {
        return this.immediate;
    }

    void setIntRegister(int regNum, int value) {
        this.regs[regNum] = value;
    }

    public static int signExtend(int val) {
        return val << 16 >> 16;
    }

    public void update() {
        if(this.opcode == Instruction.INST_HALT){
            return;
        }
        this.instPC = simulator.getIfIdStage().getInstPC();
        Instruction inst = new Instruction(); 

        if(this.instPC == -1){
            inst.setOpcode(Instruction.INST_NOP);
        }
        else {
            inst = simulator.getMemory().getInstAtAddr(this.instPC);
        }
        this.opcode = inst.getOpcode();

        if (inst instanceof ITypeInst) {
            ITypeInst i = (ITypeInst)inst;
            this.immediate = i.getImmed();
            this.regAData = this.getIntRegister(i.getRS()); // rs is not the destination like it should be, rt is
        }
        else if (inst instanceof JTypeInst) {
            JTypeInst j = (JTypeInst)inst;
            this.immediate = j.getOffset();
            this.regAData = 0;
            this.regBData = 0;
        }
        else if (inst instanceof RTypeInst) {
            RTypeInst r = (RTypeInst)inst;
            this.immediate = 0;
            this.regAData = this.getIntRegister(r.getRS()); 
            this.regBData = this.getIntRegister(r.getRT()); // AAAAAHHHHHHHHHHHHH
        }
        if (opcode == Instruction.INST_BEQ ||
            opcode == Instruction.INST_BGEZ ||
            opcode == Instruction.INST_BGTZ ||
            opcode == Instruction.INST_BLEZ ||
            opcode == Instruction.INST_BLTZ ||
            opcode == Instruction.INST_BNE ||
            opcode == Instruction.INST_J ||
            opcode == Instruction.INST_JR ||
            opcode == Instruction.INST_JAL ||
            opcode == Instruction.INST_JALR || 
            opcode == Instruction.INST_HALT ||
            opcode == Instruction.INST_SW ||
            opcode == Instruction.INST_NOP
        ) {
            shouldWriteback = false;
        }
        else {
            shouldWriteback = true;
        }
    }
}
