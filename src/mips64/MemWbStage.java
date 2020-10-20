package mips64;
public class MemWbStage {

    PipelineSimulator simulator;
    boolean halted = false;
    boolean shouldWriteback = false;
    boolean squashed = false;
    boolean forwardedShouldWriteBack;
    int instPC;
    int opcode;
    int aluIntData;
    int loadIntData;
    int forwardedLoadData;

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
    public boolean getForwardedShouldWriteBack() {
        return this.forwardedShouldWriteBack;
    }
    public int getAluIntData() {
        return this.aluIntData;
    }
    public int getLoadIntData() {
        return this.loadIntData;
    }
    public int getForwardedLoadData() {
        return this.forwardedLoadData;
    }
    
    public void update() {
        // Do Write Back
        if (this.shouldWriteback) {
            Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);

            if (inst instanceof ITypeInst) {
                ITypeInst i = (ITypeInst)inst;
                if (i.getRT() == 0){
                    System.out.println("Can't set R0");
                    // Throw exception
                    return;
                }
                if (inst.getOpcode() == Instruction.INST_LW) {
                    this.simulator.getIdExStage().setIntRegister(i.getRT(), loadIntData);
                    this.forwardedLoadData = this.loadIntData;
                }
                else {
                    this.simulator.getIdExStage().setIntRegister(i.getRT(), aluIntData);
                }
            }

            else if (inst instanceof RTypeInst) {
                RTypeInst r = (RTypeInst)inst;
                this.simulator.getIdExStage().setIntRegister(r.getRD(), aluIntData);
            }
        }

        // Set Data
        this.forwardedShouldWriteBack = this.shouldWriteback;
        this.instPC = simulator.getExMemStage().getInstPC();
        if(this.instPC == -1) return;
        Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
        this.opcode = inst.getOpcode();
        this.squashed = simulator.getExMemStage().getSquashed();
        this.aluIntData = simulator.getExMemStage().getAluIntData();
        this.shouldWriteback = simulator.getExMemStage().getShouldWriteBack();

        // Get or Set data in memory
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

        // Handle Halt
        if(this.opcode == Instruction.INST_HALT && !this.squashed){
            halted = true;
        }
    }
}
