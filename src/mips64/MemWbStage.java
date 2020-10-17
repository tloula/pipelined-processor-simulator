package mips64;

public class MemWbStage {

    PipelineSimulator simulator;
    boolean halted = false;
    boolean shouldWriteback = false;
    boolean squashed = false;
    int instPC;
    int opcode;
    int aluIntData;
    int loadIntData;

    // Need to add forwarding variables??

    public MemWbStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean getSquashed() {
        return this.squashed;
    }

    public boolean isHalted() {
        return halted;
    }

    public int getAluIntData() {
        return this.aluIntData;
    }
    
    public int getLoadIntData() {
        return this.loadIntData;
    }

    public void update() {
        // Save results ----Need to save values to forwarding data variables????
        if (this.shouldWriteback) {
            Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);

            if (inst instanceof ITypeInst) {
                ITypeInst i = (ITypeInst)inst;
                if (i.getRT() == 0){
                    System.out.println("Can't sent R0");
                    // Throw exception
                    return;
                }
                if (inst.getOpcode() == Instruction.INST_LW) {
                    this.simulator.getIdExStage().setIntRegister(i.getRT(), loadIntData); // Change back to getRS()!!!
                }
                else {
                    this.simulator.getIdExStage().setIntRegister(i.getRT(), aluIntData); // Change back to getRS()!!!
                }
            }

            else if (inst instanceof RTypeInst) {
                // Isn't LW an I type instruction? Do we need to do this check??? I think we can just run this.simulator.get....
                RTypeInst r = (RTypeInst)inst;
                if (inst.getOpcode() == Instruction.INST_LW) {          // Kill me
                    this.simulator.getIdExStage().setIntRegister(r.getRD(), loadIntData); // Change back to getRS()!!!
                }
                else {
                    this.simulator.getIdExStage().setIntRegister(r.getRD(), aluIntData); // Change back to getRS()!!!
                }
            }
        }

        this.instPC = simulator.getExMemStage().getInstPC();
        if(this.instPC == -1) return;
        Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
        this.opcode = inst.getOpcode();
        this.squashed = simulator.getExMemStage().getSquashed();

        this.aluIntData = simulator.getExMemStage().getAluIntData();
        if (inst.getOpcode() == Instruction.INST_LW && this.aluIntData % 4 == 0 && !squashed) {
            this.loadIntData = simulator.getMemory().getIntDataAtAddr(aluIntData);
        }
        else if (inst.getOpcode() == Instruction.INST_SW && !squashed){
            // aluData is the address in memory
            this.simulator.getMemory().setIntDataAtAddr(aluIntData, simulator.getExMemStage().getStoreIntData());
        }
        else{
            this.loadIntData = 0;
        }

        shouldWriteback = simulator.getExMemStage().getShouldWriteBack();
        if(this.opcode == Instruction.INST_HALT && !this.squashed){
            halted = true;
        }
    }
}
