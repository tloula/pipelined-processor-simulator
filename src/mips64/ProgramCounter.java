package mips64;

public class ProgramCounter {

  PipelineSimulator simulator;
  boolean squashed = false;
  boolean stalled = false;
  int pc;

  public ProgramCounter(PipelineSimulator sim) {
    pc = 0;
    simulator = sim;
  }

  public boolean getSquashed() {
    return this.squashed;
  }

  public void squash() {
    this.squashed = true;
  }

  public int getPC () {
    return pc;
  }

  public void setPC (int newPC) {
    pc = newPC;
  }

  public void incrPC () {
    pc += 4;
  }

  public void update() {

    // Handle stalls
    this.stalled = simulator.getIfIdStage().getStalled();
    if (this.stalled) {
      simulator.getIfIdStage().unstall();
      this.stalled = false;
      return;
    }

    // Handle Branches
    this.squashed = false;
    if (simulator.getExMemStage().branchTaken() && !simulator.getExMemStage().getSquashed()) {
        this.pc = simulator.getExMemStage().getAluIntData();
    }

    incrPC();
  }
}