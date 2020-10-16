package mips64;

public class IfIdStage {
  PipelineSimulator simulator;
  int instPC = -1;
  int opcode;

  public IfIdStage(PipelineSimulator sim) {
    simulator = sim;
  }

  public int getInstPC(){
    return instPC;
  }

  public void update() {
    if(this.opcode == Instruction.INST_HALT){
      return;
    }
    
    // Branching
    Instruction inst = new Instruction();
    if (simulator.getExMemStage().branchTaken()) {
      // Branch YES
      int branchAddr = simulator.getExMemStage().getAluIntData();
      simulator.getPCStage().setPC(branchAddr);
      this.instPC = branchAddr;
      inst = simulator.getMemory().getInstAtAddr(branchAddr);
    } else {
      // Branch NO
      this.instPC = simulator.getPCStage().getPC();
      inst = simulator.getMemory().getInstAtAddr(this.instPC);
    }
    this.opcode = inst.getOpcode();
  }
}
