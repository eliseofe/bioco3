package be.kuleuven.bioco3.selforg;

import java.util.Queue;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.core.SimulatorUser;
import rinde.sim.pdptw.central.Solver;
import rinde.sim.pdptw.central.Solvers;
import rinde.sim.pdptw.central.Solvers.SimulationSolver;
import rinde.sim.pdptw.central.Solvers.SolveArgs;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

import com.google.common.base.Optional;

public class SituatedCommunicatorSolver extends SituatedCommunicator implements SimulatorUser {

	private final Solver solver;
	private Optional<SimulationSolver> solverHandle;
	protected Optional<SimulatorAPI> simulator;

	public SituatedCommunicatorSolver(ObjectiveFunction objFunc, Solver s) {
		solver = s;
		solverHandle = Optional.absent();
		simulator = Optional.absent();
	}

	// not needed
	public void waitFor(DefaultParcel p) {
	}

	@Override
	public Optional<DefaultParcel> decideNextParcel() {

		Optional<DefaultParcel> result = Optional.absent();

		if (candidateParcels.size() > 0) {

			SolveArgs args = SolveArgs.create().noCurrentRoutes().useParcels(candidateParcels);
			final Queue<DefaultParcel> newRoute = solverHandle.get().solve(args).get(0);
			result = Optional.of(newRoute.peek());
			// System.out.println("  Result: " + result.get());
		}
		return result;
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

	protected void afterInit() {
		initSolver();
	}

	public void setSimulator(SimulatorAPI api) {
		simulator = Optional.of(api);
		initSolver();
	}

	private void initSolver() {
		if (simulator.isPresent() && rm.isPresent()
				&& !solverHandle.isPresent()) {
			solverHandle = Optional.of(Solvers.solverBuilder(solver)
					.with(rm.get()).with(pdpm.get()).with(simulator.get())
					.with(vehicle.get()).buildSingle());
		}
	}

}
