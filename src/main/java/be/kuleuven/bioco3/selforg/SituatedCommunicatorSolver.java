package be.kuleuven.bioco3.selforg;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Queue;
import java.util.Set;

import rinde.sim.core.SimulatorAPI;
import rinde.sim.core.SimulatorUser;
import rinde.sim.core.model.pdp.PDPModel.ParcelState;
import rinde.sim.core.model.road.MovingRoadUser;
import rinde.sim.pdptw.central.Solver;
import rinde.sim.pdptw.central.Solvers;
import rinde.sim.pdptw.central.Solvers.SimulationSolver;
import rinde.sim.pdptw.central.Solvers.SolveArgs;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.ObjectiveFunction;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;
import be.kuleuven.bioco3.selforg.SituatedCommunicator;

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

			DefaultParcel destParcel = rm.get().getDestinationToParcel((MovingRoadUser) vehicle.get());
			final Set<DefaultParcel> solverParcels = newLinkedHashSet(candidateParcels);

			if (destParcel != null) {
				if (!solverParcels.contains(destParcel) && pdpm.get().getParcelState(destParcel) != ParcelState.IN_CARGO) {
					solverParcels.add(destParcel); // TODO: Why Rinde says to do it? Ask him
				}
			}

			// System.out.println("Vehicle " + vehicle.get() + " Solving for "+solverParcels.size());
			// for(DefaultParcel p : solverParcels){
			// System.out.println("  "+p);
			// }
			SolveArgs args = SolveArgs.create().noCurrentRoutes().useParcels(solverParcels);
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
