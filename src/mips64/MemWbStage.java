package mips64;

public class MemWbStage {

    PipelineSimulator simulator;
    boolean halted;
    boolean shouldWriteback = false;
    int instPC;
    int opcode;
    int aluIntData;
    int loadIntData;

    // Need to add forwarding variables??

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean isHalted() {
        return halted;
    }

    public void update() {
        // Save results ----Need to save values to forwarding data variables????
        if (this.shouldWriteback) {
            Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC - 4);

            if (inst instanceof ITypeInst) {
                ITypeInst i = (ITypeInst)inst;
                if (inst.getOpcode() == Instruction.INST_LW) {
                    this.simulator.getIdExStage().setIntRegister(i.rs, loadIntData);
                }
                else {
                    this.simulator.getIdExStage().setIntRegister(i.rs, aluIntData);
                }
            }

            if (inst instanceof RTypeInst) {
                ITypeInst r = (ITypeInst)inst;
                if (inst.getOpcode() == Instruction.INST_LW) {
                    this.simulator.getIdExStage().setIntRegister(r.rs, loadIntData);
                }
                else {
                    this.simulator.getIdExStage().setIntRegister(r.rs, aluIntData);
                }
            }
        }

        this.instPC = simulator.getExMemStage().getInstPC();
        Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
        this.opcode = inst.getOpcode();

        this.aluIntData = simulator.getExMemStage().getAluIntData();
        this.loadIntData = simulator.getMemory().getIntDataAtAddr(aluIntData);
    }
}
