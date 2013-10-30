package be.kuleuven.bioco3.selforg;

import rinde.logistics.pdptw.mas.TruckConfiguration;
import rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import rinde.logistics.pdptw.mas.comm.SolverBidder;
import rinde.logistics.pdptw.mas.route.SolverRoutePlanner;
import rinde.logistics.pdptw.solver.MultiVehicleHeuristicSolver;
import rinde.sim.pdptw.central.Solver;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Parser;
import rinde.sim.util.SupplierRng;

import com.google.common.collect.ImmutableList;

public class Example {
	private static final String SCENARIO_FILE = "files/scenarios/gendreau06/req_rapide_1_240_24";
	private static final SupplierRng<Solver> SOLVER_SUPPLIER = MultiVehicleHeuristicSolver
			.supplier(50, 100);

	public static void main(String[] args) {
		Gendreau06ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		Experiment
				.build(objFunc)
				.showGui()
				.addScenario(Gendreau06Parser.parse(SCENARIO_FILE))
				.repeat(1)
				.withThreads(1)
				.withRandomSeed(1)
				.addConfiguration(
						new TruckConfiguration(
								SolverRoutePlanner.supplier(SOLVER_SUPPLIER),
								SolverBidder.supplier(objFunc, SOLVER_SUPPLIER),
								ImmutableList.of(AuctionCommModel.supplier())))
				.perform();
	}
}
