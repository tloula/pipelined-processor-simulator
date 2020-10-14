package mips64;

public class IdExStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    int instPC;
    int opcode;
    int regAData;
    int regBData;
    int immediate;

    int[] regs = new int[32];   // THE HECK G, WHY ARE THESE HERE!!!

    public IdExStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public int getInstPC(){
        return instPC;
    }

    int getIntRegister(int regNum) {
        return this.regs[regNum];
    }

    void setIntRegister(int regNum, int value) {
        this.regs[regNum] = value;
    }

    public static int signExtend(int val) {
        return val << 16 >> 16;
    }

    public void update() {
        this.instPC = simulator.getIfIdStage().getInstPC();
        Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
        this.opcode = inst.getOpcode();

        if (inst instanceof ITypeInst) {
            ITypeInst i = (ITypeInst)inst;
            this.immediate = i.getImmed();
            this.regAData = i.getRS();
        }
        if (inst instanceof JTypeInst) {
            JTypeInst j = (JTypeInst)inst;
            this.immediate = j.getOffset();
            this.regAData = -1;
            this.regBData = -1;
        }
        if (inst instanceof RTypeInst) {
            RTypeInst r = (RTypeInst)inst;
            this.immediate = -1;
            this.regAData = r.getRT();
            this.regBData = r.getRD();
        }
    }
}
