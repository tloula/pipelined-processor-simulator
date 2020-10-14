package mips64;

public class ExMemStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    int instPC;
    int opcode;
    int aluIntData;
    int storeIntData;

    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public int getAluIntData(){
        return aluIntData;
    }

    public boolean shouldWriteBack() {
        return this.shouldWriteback;
    }

    public int getInstPC(){
        return instPC;
    }

    public void update() {
        this.instPC = simulator.getIdExStage().getInstPC();
        Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
        this.opcode = inst.getOpcode();
    }
}
