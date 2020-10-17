package mips64;

public class IfIdStage {
  PipelineSimulator simulator;
  boolean shouldWriteback = false;
  boolean squashed = false;
  int instPC = -1;
  int opcode;

  public IfIdStage(PipelineSimulator sim) {
    simulator = sim;
  }

  public boolean getShouldWriteBack() {
    return this.shouldWriteback;
  }

  public boolean getSquashed() {
    return this.squashed;
  }

  public void squash() {
    this.squashed = true;
  }

  public int getInstPC(){
    return instPC;
  }

  public void update() {
    if(this.opcode == Instruction.INST_HALT && !this.squashed){
      return;
    }

    this.instPC = simulator.getPCStage().getPC();
    Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
    this.opcode = inst.getOpcode();

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
      opcode == Instruction.INST_SW ||
      opcode == Instruction.INST_HALT ||
      opcode == Instruction.INST_NOP ||
      this.squashed
    ) {
      this.shouldWriteback = false;
    }
    else {
      this.shouldWriteback = true;
    }
    this.squashed = simulator.getPCStage().getSquashed();
  }
}
