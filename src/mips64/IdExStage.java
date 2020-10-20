package mips64;

public class IdExStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    boolean squashed = false;
    boolean stalled = false;
    int instPC = -1;
    int opcode;
    int regAData;
    int regBData;
    int immediate;

    // Forwarding
    Instruction inst0;
    Instruction inst1;
    Instruction inst2;

    int[] regs = new int[32];   // THE HECK G, WHY ARE THESE HERE!!!

    public IdExStage(PipelineSimulator sim) {
        simulator = sim;
        inst0 = new Instruction();
        inst1 = new Instruction();
        inst2 = new Instruction();
    }

    public boolean getShouldWriteBack() {
        return this.shouldWriteback;
    }

    public boolean getSquashed() {
        return this.squashed;
    }

    public boolean getStalled() {
        return this.stalled;
    }

    public void setIntRegister(int regNum, int value) {
        this.regs[regNum] = value;
    }

    public void unstall(){
        this.stalled = false;
    }

    public void squash() {
        this.squashed = true;
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

    public static int signExtend(int val) {
        return val << 16 >> 16;
    }

    public void update() {

        this.stalled = simulator.getExMemStage().getStalled();
        if (this.stalled) {
            inst2 = inst1;
            simulator.getExMemStage().unstall();
            return;
        }

        if(this.opcode == Instruction.INST_HALT && !this.squashed){
            return;
        }
        this.instPC = simulator.getIfIdStage().getInstPC();

        inst2 = inst1;
        inst1 = inst0;

        if(this.instPC == -1){
            inst0.setOpcode(Instruction.INST_NOP);
        }
        else {
            inst0 = simulator.getMemory().getInstAtAddr(this.instPC);
        }
        this.opcode = inst0.getOpcode();

        // Get operands from instruction normally, without forwarding
        if (inst0 instanceof ITypeInst) {
            ITypeInst i = (ITypeInst)inst0;
            this.immediate = i.getImmed();
            this.regAData = this.getIntRegister(i.getRS());
            this.regBData = 0;

            // Handle branches
            if ( opcode == Instruction.INST_BEQ ||
                 opcode == Instruction.INST_BNE ||
                 opcode == Instruction.INST_BGEZ ||
                 opcode == Instruction.INST_BGTZ ||
                 opcode == Instruction.INST_BLEZ ||
                 opcode == Instruction.INST_BLTZ ||
                 opcode == Instruction.INST_JR ||
                 opcode == Instruction.INST_JALR ||
                 opcode == Instruction.INST_SW
                ){
                    this.regBData = this.getIntRegister(i.getRT());
            }
        }
        else if (inst0 instanceof JTypeInst) {
            JTypeInst j = (JTypeInst)inst0;
            this.immediate = j.getOffset();
            this.regAData = 0;
            this.regBData = 0;
        }
        else if (inst0 instanceof RTypeInst) {
            RTypeInst r = (RTypeInst)inst0;
            this.immediate = 0;
            this.regAData = this.getIntRegister(r.getRS()); 
            this.regBData = this.getIntRegister(r.getRT()); // AAAAAHHHHHHHHHHHHH
        
            if( opcode == Instruction.INST_SLL ||
                opcode == Instruction.INST_SRA ||
                opcode == Instruction.INST_SRL ){
        
                regBData = ((RTypeInst)inst0).getShamt();
            }
        }

        this.shouldWriteback = simulator.getIfIdStage().getShouldWriteBack();
        this.squashed = simulator.getIfIdStage().getSquashed();

        if(this.squashed) this.shouldWriteback = false;

        // Handle forwarding by overwriting previous values if they are incorrect

        // Get ops and dest
        int inst0Ops1 = -1; 
        int inst0Ops2 = -1;
        int inst1Dest = -1;
        int inst2Dest = -1;

        if (inst1 instanceof ITypeInst) 
            inst1Dest = ((ITypeInst)inst1).getRT();
        else if (inst1 instanceof RTypeInst)
            inst1Dest = ((RTypeInst)inst1).getRD();
        else inst1Dest = -1;

        if (inst2 instanceof ITypeInst)
            inst2Dest = ((ITypeInst)inst2).getRT();
        else if (inst2 instanceof RTypeInst)
            inst2Dest = ((RTypeInst)inst2).getRD();
        else inst2Dest = -1;

        if (inst0 instanceof ITypeInst){
            inst0Ops1 = ((ITypeInst)inst0).getRS();
            inst0Ops2 = -2;
        }
        else if (inst0 instanceof RTypeInst){
            inst0Ops1 = ((RTypeInst)inst0).getRS();
            inst0Ops2 = ((RTypeInst)inst0).getRT();
        }
        else return;

        // Compare ops and dest
        // if match and not squashed, forward
        Boolean exMemSquashed = simulator.getExMemStage().getSquashed();
        Boolean memWbSquashed = simulator.getMemWbStage().getSquashed();

        Boolean inst1IsBranch = false;
        if (inst1.getOpcode() == Instruction.INST_BEQ ||
        inst1.getOpcode() == Instruction.INST_BNE ||
        inst1.getOpcode() == Instruction.INST_BGEZ ||
        inst1.getOpcode() == Instruction.INST_BGTZ ||
        inst1.getOpcode() == Instruction.INST_BLEZ ||
        inst1.getOpcode() == Instruction.INST_BLTZ ) {
            inst1IsBranch = true;
        }
        Boolean inst2IsBranch = false;
        if (inst2.getOpcode() == Instruction.INST_BEQ ||
            inst2.getOpcode() == Instruction.INST_BNE ||
            inst2.getOpcode() == Instruction.INST_BGEZ ||
            inst2.getOpcode() == Instruction.INST_BGTZ ||
            inst2.getOpcode() == Instruction.INST_BLEZ ||
            inst2.getOpcode() == Instruction.INST_BLTZ ) {
                inst2IsBranch = true;
        }

        if (inst1Dest == inst0Ops1 && !exMemSquashed && !inst1IsBranch) {
            if (inst1.getOpcode() == Instruction.INST_LW){
                // Stall and tell Ex/Mem to forward
                simulator.getExMemStage().stall();
                simulator.getExMemStage().setWillForward(1);
            }
            else {
                this.regAData = simulator.getExMemStage().getAluIntData();
            }
        }
        if (inst1Dest == inst0Ops2 && !exMemSquashed && !inst1IsBranch) {
            if (inst1.getOpcode() == Instruction.INST_LW){
                // Stall and tell Ex/Mem to forward
                simulator.getExMemStage().stall();
                if(simulator.getExMemStage().getWillForward()){
                    simulator.getExMemStage().setWillForward(3);
                }
                else{
                    simulator.getExMemStage().setWillForward(2);
                }
            }
            else {
                this.regBData = simulator.getExMemStage().getAluIntData();
            }
        }
        if (inst2Dest == inst0Ops1 && !memWbSquashed && !inst2IsBranch && (inst2Dest != inst1Dest || (inst2Dest == inst1Dest && inst1IsBranch))) {
            if (inst2.getOpcode() == Instruction.INST_LW) {
                this.regAData = simulator.getMemWbStage().getLoadIntData();
            }
            else if (inst2.getOpcode() != Instruction.INST_SW) {
                this.regAData = simulator.getMemWbStage().getAluIntData();
            }
        }
        if (inst2Dest == inst0Ops2 && !memWbSquashed && !inst2IsBranch && (inst2Dest != inst1Dest || (inst2Dest == inst1Dest && inst1IsBranch))) {
            if (inst2.getOpcode() == Instruction.INST_LW){
                this.regBData = simulator.getMemWbStage().getLoadIntData();
            }
            else if (inst2.getOpcode() != Instruction.INST_SW) {
                this.regBData = simulator.getMemWbStage().getAluIntData();
            }
        }

        if(inst0.getOpcode() == Instruction.INST_SW && inst1.getOpcode() == Instruction.INST_LW){
            // Stall...frick, 
            // TODO: Fix or delete
        }
        if(inst0.getOpcode() == Instruction.INST_SW && inst2.getOpcode() == Instruction.INST_LW){
            this.regBData = simulator.getMemWbStage().getLoadIntData();
        }
    }
}
