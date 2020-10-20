package mips64;

public class ExMemStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    boolean squashed = false;
    boolean stalled = false;
    int instPC = -1;
    int opcode;
    int aluIntData;
    int storeIntData;
    int willForward; // Specifically from MEM/WB because of a dependency with load word
    Boolean branchTaken = false;
    int branchCount = 0;
    int stallCount = 0;

    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
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

    public boolean shouldWriteBack() {
        return this.shouldWriteback;
    }

    public boolean branchTaken() {
        return this.branchTaken;
    }

    public void setWillForward(int reg) {
        this.willForward = reg;
    }

    public int getWhatForward() {
        return this.willForward;
    }

    public boolean getWillForward() {
        return this.willForward == 0 ? false : true;
    }

    public void stall() {
        stallCount++;
        System.out.println("Stalled: " + stallCount);
        this.stalled = true;
    }

    public void unstall(){
        this.stalled = false;
    }

    public int getAluIntData(){
        return this.aluIntData;
    }

    public int getStoreIntData(){
        return this.storeIntData;
    }

    public int getInstPC(){
        return this.instPC;
    }

    public void update() {

        // Handle Stall and Halt
        if (this.stalled) {
            this.squashed = true;
            this.shouldWriteback = false;
            return;
        }
        if(this.opcode == Instruction.INST_HALT && !this.squashed) {
            return;
        }

        // Set data
        this.instPC = simulator.getIdExStage().getInstPC();
        Instruction inst = new Instruction();
        if(this.instPC == -1){
            inst.setOpcode(Instruction.INST_NOP);
        }
        else {
            inst = simulator.getMemory().getInstAtAddr(this.instPC);
        }
        this.opcode = inst.getOpcode();
        this.shouldWriteback = simulator.getIdExStage().getShouldWriteBack();
        this.squashed = simulator.getIdExStage().getSquashed();
        int leftOperand = -1;
        int rightOperand = -1;

        // Save Register data to local variables
        int regAData = simulator.getIdExStage().getRegAData();
        int regBData = simulator.getIdExStage().getRegBData();
        int immediate = simulator.getIdExStage().getImmediate();

        // Set ALU operands
        if(opcode == Instruction.INST_SW){
            storeIntData = regBData;
        }
        else{
            storeIntData = 0;
        }

        // IType Instructions
        if (inst instanceof ITypeInst) {
            leftOperand = regAData;
            rightOperand = immediate;

            // Branch Instructions
            if( opcode == Instruction.INST_BEQ ||
                opcode == Instruction.INST_BNE || 
                opcode == Instruction.INST_BGEZ || 
                opcode == Instruction.INST_BGTZ || 
                opcode == Instruction.INST_BLEZ || 
                opcode == Instruction.INST_BLTZ ) {
                leftOperand = instPC;

                // decide if branch is taken
                switch(opcode){
                    case Instruction.INST_BEQ:
                        if (regAData == regBData) branchTaken = true;
                        else branchTaken = false;
                        break;
                    case Instruction.INST_BNE:
                        if (regAData != regBData) branchTaken = true;
                        else branchTaken = false;
                        break;
                    case Instruction.INST_BGEZ:
                        if (regAData >= 0) branchTaken = true;
                        else branchTaken = false;
                        break;
                    case Instruction.INST_BGTZ:
                        if (regAData > 0) branchTaken = true;
                        else branchTaken = false;
                        break;
                    case Instruction.INST_BLEZ:
                        if (regAData <= 0) branchTaken = true;
                        else branchTaken = false;
                        break;
                    case Instruction.INST_BLTZ:
                        if (regAData < 0) branchTaken = true;
                        else branchTaken = false;
                        break;
                    default:
                        branchTaken = false;
                        System.out.println("How did you get here?" + Instruction.getNameFromOpcode(opcode));
                        break;
                }
            }
            else {
                branchTaken = false;
            }
        }

        // IType JUMP Instructions
        if ( opcode == Instruction.INST_JR || 
             opcode == Instruction.INST_JALR){

            leftOperand = regAData;
            rightOperand = 0;
            branchTaken = true;
        }
        // JType Instructions
        else if (inst instanceof JTypeInst){
            leftOperand = simulator.getIdExStage().getImmediate();
            rightOperand = instPC;
            if( opcode == Instruction.INST_J ||
                opcode == Instruction.INST_JAL){
                branchTaken = true;
            }
            else{
                branchTaken = false;
            }
        }
        // RType Instructions
        else if (inst instanceof RTypeInst) {
            leftOperand = simulator.getIdExStage().getRegAData();
            rightOperand = simulator.getIdExStage().getRegBData();
        }

        // Handle forwarding for when stalls happen
        if(getWillForward() && simulator.getMemWbStage().getForwardedShouldWriteBack()){
            if(willForward == 1){
                leftOperand = simulator.getMemWbStage().getForwardedLoadData();
            }
            else if (willForward == 2){
                rightOperand = simulator.getMemWbStage().getForwardedLoadData();
            }
            else {
                leftOperand = rightOperand = simulator.getMemWbStage().getForwardedLoadData();                
            }
            willForward = 0;
        }

        // Do ALU operation
        switch(this.opcode) {
            case Instruction.INST_ADD:
            case Instruction.INST_ADDI:
            case Instruction.INST_SW:
            case Instruction.INST_LW:
            case Instruction.INST_BEQ:
            case Instruction.INST_BNE:
            case Instruction.INST_BGEZ: 
            case Instruction.INST_BGTZ:
            case Instruction.INST_BLEZ: 
            case Instruction.INST_BLTZ:
            case Instruction.INST_JR: 
            case Instruction.INST_JALR:
            case Instruction.INST_JAL:
            case Instruction.INST_J:
                aluIntData = leftOperand + rightOperand;
                break;
            case Instruction.INST_SUB:
                aluIntData = leftOperand - rightOperand;
                break;
            case Instruction.INST_MUL:
                aluIntData = leftOperand * rightOperand;
                break;
            case Instruction.INST_DIV:
                if(rightOperand == 0){
                    throw new MIPSException("Divide By Zero");
                }
                aluIntData = leftOperand / rightOperand;
                break;
            case Instruction.INST_AND:
            case Instruction.INST_ANDI:
                aluIntData = leftOperand & rightOperand;
                break;
            case Instruction.INST_OR:
            case Instruction.INST_ORI:
                aluIntData = leftOperand | rightOperand;
                break;
            case Instruction.INST_XOR:
            case Instruction.INST_XORI:
                aluIntData = leftOperand ^ rightOperand;
                break;
            case Instruction.INST_SLL:
                // TODO: Fix arithmetic shift????? 
                aluIntData = leftOperand << rightOperand;
                break;
            case Instruction.INST_SRL:
                aluIntData = leftOperand >> rightOperand;
                break;
            case Instruction.INST_SRA:
                aluIntData = leftOperand >> rightOperand;
                break;
            default:
                System.out.println("Invalid ARU inst: " + Instruction.getNameFromOpcode(opcode));
                aluIntData = 0;
        }

        // Tell the other stages that the branch was taken
        // Squash previous 2 instructions since they are from the fallthrough path
        if(branchTaken && !this.squashed){
            branchCount++;
            System.out.println("Branches taken: " + branchCount);
            simulator.getPCStage().squash();
            simulator.getIfIdStage().squash();
            if (this.opcode == Instruction.INST_JAL || this.opcode == Instruction.INST_JALR)
                simulator.getIdExStage().setIntRegister(31, this.instPC);
        }
    }
}
