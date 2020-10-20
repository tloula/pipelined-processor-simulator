package mips64;

public class IfIdStage {

  PipelineSimulator simulator;
  boolean shouldWriteback = false;
  boolean squashed = false;
  boolean stalled = false;
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

  public boolean getStalled() {
    return this.stalled;
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

  public void update() {

    // Handle stall
    this.stalled = simulator.getIdExStage().getStalled();
    if (this.stalled) {
      simulator.getIdExStage().unstall();
      return;
    }
  
    // Handle Halt (Copies G's Simulator, ish)
    if(this.opcode == Instruction.INST_HALT && !this.squashed){
      return;
    }

    // Set data
    this.instPC = simulator.getPCStage().getPC();
    this.opcode = simulator.getMemory().getInstAtAddr(this.instPC).getOpcode();;
    this.squashed = simulator.getPCStage().getSquashed();

    // Decide if this instruction should write back (things like branches shouldn't)
    if (this.opcode == Instruction.INST_BEQ ||
        this.opcode == Instruction.INST_BGEZ ||
        this.opcode == Instruction.INST_BGTZ ||
        this.opcode == Instruction.INST_BLEZ ||
        this.opcode == Instruction.INST_BLTZ ||
        this.opcode == Instruction.INST_BNE ||
        this.opcode == Instruction.INST_J ||
        this.opcode == Instruction.INST_JR ||
        this.opcode == Instruction.INST_JAL ||
        this.opcode == Instruction.INST_JALR || 
        this.opcode == Instruction.INST_SW ||
        this.opcode == Instruction.INST_HALT ||
        this.opcode == Instruction.INST_NOP ||
        this.squashed
    ) {
      this.shouldWriteback = false;
    }
    else {
      this.shouldWriteback = true;
    }
  }
}
