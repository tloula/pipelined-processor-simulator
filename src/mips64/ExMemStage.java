package mips64;

public class ExMemStage {

    PipelineSimulator simulator;
    boolean shouldWriteback = false;
    int instPC = -1;
    int opcode;
    int aluIntData;
    int storeIntData;

    public ExMemStage(PipelineSimulator sim) {
        simulator = sim;
    }

    public boolean getShouldWriteBack() {
        return this.shouldWriteback;
    }

    public int getAluIntData(){
        return this.aluIntData;
    }

    public int getStoreIntData(){
        return this.storeIntData;
    }

    public boolean shouldWriteBack() {
        return this.shouldWriteback;
    }

    public int getInstPC(){
        return this.instPC;
    }

    public void update() {
        if(this.opcode == Instruction.INST_HALT){
            return;
        }
        this.instPC = simulator.getIdExStage().getInstPC();
        Instruction inst = new Instruction();

        if(this.instPC == -1){
            inst.setOpcode(Instruction.INST_NOP);
        }
        else {
            inst = simulator.getMemory().getInstAtAddr(this.instPC);
        }
        this.opcode = inst.getOpcode();

        int leftOperand = -1;
        int rightOperand = -1;
        
        // Set ALU operands
        if(opcode == Instruction.INST_SW){
            // SW R1, 1000(R2)
            leftOperand = simulator.getIdExStage().getRegAData();
            rightOperand = simulator.getIdExStage().getImmediate();
            storeIntData = simulator.getIdExStage().getIntRegister(((ITypeInst)inst).getRT()); // TODO: Fix???? 
        }
        else{
            if(opcode == Instruction.INST_LW){
                leftOperand = simulator.getIdExStage().getRegAData();
                rightOperand = simulator.getIdExStage().getImmediate();
            }
            storeIntData = 0;
        }
        if (inst instanceof ITypeInst) {
            leftOperand = simulator.getIdExStage().getImmediate();
            rightOperand = simulator.getIdExStage().getRegAData();
        }
        else if (inst instanceof JTypeInst){
            leftOperand = simulator.getIdExStage().getImmediate();
        }
        else if (inst instanceof RTypeInst) {
            leftOperand = simulator.getIdExStage().getRegAData();
            rightOperand = simulator.getIdExStage().getRegBData();
        }
        
        // Do ALU operation
        switch(this.opcode) {
            case Instruction.INST_ADD:
            case Instruction.INST_ADDI:
            case Instruction.INST_SW:
            case Instruction.INST_LW:
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
        shouldWriteback = simulator.getIdExStage().getShouldWriteBack();
    }
}
