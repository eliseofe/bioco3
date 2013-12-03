package main.java.be.kuleuven.bioco3.selforg;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.pdptw.central.Solver;
import rinde.sim.pdptw.central.Solvers.SimulationSolver;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

import com.google.common.base.Optional;

public class SituatedCommunicatorSolver extends SituatedCommunicator {

  private final ObjectiveFunction objectiveFunction;
  private final Solver solver;
  private Optional<SimulationSolver> solverHandle;
  protected Optional<SimulatorAPI> simulator;
	
	 public SituatedCommunicatorSolver(ObjectiveFunction objFunc, Solver s) {
	    objectiveFunction = objFunc;
	    solver = s;
	    solverHandle = Optional.absent();
	    simulator = Optional.absent();
	  }
	
	// not needed
	public void waitFor(DefaultParcel p) {
	}

	@Override
	public Optional<DefaultParcel> decideNextParcel() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public static SupplierRng<SituatedCommunicatorSolver> supplier(
      final ObjectiveFunction objFunc,
      final SupplierRng<? extends Solver> solverSupplier) {
    return new DefaultSupplierRng<SituatedCommunicatorSolver>() {
      public SituatedCommunicatorSolver get(long seed) {
        return new SituatedCommunicatorSolver(objFunc, solverSupplier.get(seed));
      }

      @Override
      public String toString() {
        return super.toString() + "-" + solverSupplier.toString();
      }
    };
  }

}
