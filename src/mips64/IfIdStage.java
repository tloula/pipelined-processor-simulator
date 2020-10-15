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
    this.instPC = simulator.getPCStage().getPC();
    Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
    this.opcode = inst.getOpcode();
  }
}
