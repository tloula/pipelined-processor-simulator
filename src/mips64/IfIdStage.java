package mips64;

public class IfIdStage {
  PipelineSimulator simulator;
  int instPC;
  int opcode;

  public IfIdStage(PipelineSimulator sim) {
    simulator = sim;
  }

  public int getInstPC(){
    return instPC;
  }

  public void update() {
    this.instPC = simulator.getPCStage().getPC();
    Instruction inst = simulator.getMemory().getInstAtAddr(this.instPC);
    this.opcode = inst.getOpcode();
  }
}
