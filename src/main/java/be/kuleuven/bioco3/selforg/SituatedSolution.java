package be.kuleuven.bioco3.selforg;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import rinde.logistics.pdptw.mas.TruckConfiguration;
import rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import rinde.logistics.pdptw.mas.comm.RandomBidder;
import rinde.logistics.pdptw.mas.comm.SolverBidder;
import rinde.logistics.pdptw.mas.route.RandomRoutePlanner;
import rinde.logistics.pdptw.mas.route.SolverRoutePlanner;
import rinde.logistics.pdptw.solver.MultiVehicleHeuristicSolver;
import rinde.sim.pdptw.central.Solver;
import rinde.sim.pdptw.common.DynamicPDPTWScenario.ProblemClass;
import rinde.sim.pdptw.experiment.Experiment;
import rinde.sim.pdptw.experiment.Experiment.ExperimentResults;
import rinde.sim.pdptw.experiment.Experiment.SimulationResult;
import rinde.sim.pdptw.experiment.MASConfiguration;
import rinde.sim.pdptw.gendreau06.Gendreau06ObjectiveFunction;
import rinde.sim.pdptw.gendreau06.Gendreau06Scenarios;
import rinde.sim.pdptw.gendreau06.GendreauProblemClass;
import rinde.sim.util.SupplierRng;

import com.google.common.base.Charsets;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.google.common.collect.Table.Cell;
import com.google.common.io.Files;

public class SituatedSolution {

	private static final String SCENARIO_FILE = "files/scenarios/gendreau06/req_rapide_1_240_24";
	private static final String SCENARIOS_PATH = "files/scenarios/gendreau06/";
	private static final SupplierRng<Solver> SOLVER_SUPPLIER = MultiVehicleHeuristicSolver.supplier(200, 50000);
	private static final SupplierRng<Solver> SOLVER_SUPPLIER_INTERNAL = MultiVehicleHeuristicSolver.supplier(20, 10000);

	public static void main(String[] args) {
		Gendreau06ObjectiveFunction objFunc = new Gendreau06ObjectiveFunction();
		final Gendreau06Scenarios onlineScenarios = new Gendreau06Scenarios(
				SCENARIOS_PATH, true, GendreauProblemClass.values());
		ExperimentResults results = Experiment
				.build(objFunc)
				// .addScenario(Gendreau06Parser.parse(SCENARIO_FILE))
				.addScenarioProvider(onlineScenarios)
				// .showGui()
				.repeat(5)
				.withThreads(20)
				.withRandomSeed(1)
				.addConfiguration(
						new TruckConfiguration(RandomRoutePlanner.supplier(),
								RandomBidder.supplier(),
								ImmutableList.of(AuctionCommModel.supplier())))
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplier(SOLVER_SUPPLIER),
								RandomBidder.supplier(),
								ImmutableList.of(AuctionCommModel.supplier())))
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplier(SOLVER_SUPPLIER),
								SolverBidder.supplier(objFunc, SOLVER_SUPPLIER_INTERNAL),
								ImmutableList.of(AuctionCommModel.supplier())))
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplierWithoutCurrentRoutes(SOLVER_SUPPLIER),
								SituatedCommunicatorRandom.supplier(),
								ImmutableList.of(SituatedCommModelFixedRadius.supplier(3)))) // Sensing
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplierWithoutCurrentRoutes(SOLVER_SUPPLIER),
								SituatedCommunicatorRandom.supplier(),
								ImmutableList.of(SituatedCommModelClosestN.supplier(10)))) // Number of closest parcels
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplierWithoutCurrentRoutes(SOLVER_SUPPLIER),
								SituatedCommunicatorSolver.supplier(objFunc, SOLVER_SUPPLIER_INTERNAL),
								ImmutableList.of(SituatedCommModelClosestN.supplier(10)))) // Number of closest parcels
				.addConfiguration(
						new TruckConfiguration(SolverRoutePlanner.supplierWithoutCurrentRoutes(SOLVER_SUPPLIER),
								SituatedCommunicatorSolver.supplier(objFunc, SOLVER_SUPPLIER_INTERNAL),
								ImmutableList.of(SituatedCommModelFixedRadius.supplier(3)))) // Radius range
				// .addConfiguration(
				// Central.solverConfiguration(MultiVehicleHeuristicSolver.supplier(500, 10000), "-Offline"))
				.perform();
		writeResults(results);
	}

	// TODO: for vehicle num, write here a version of public ImmutableList<DynamicPDPTWScenario> provide() in Gendreau06Scenarios and use the list with addScenarios
	// TODO: for delivery decision making, you can use a >1 sized (k) list of assigned parcels instead of one (the solver will figure things out automagically)
	// TODO: try co change the parameters of the solver, you should lower them in principle
	// TODO: Rinde is solving the solver (conversion) problem
	// TODO: run runs with different number of vehicles and different k values for the above algorithm

	static void writeResults(ExperimentResults results) {

		final Table<MASConfiguration, ProblemClass, StringBuilder> table = HashBasedTable.create();

		checkArgument(results.objectiveFunction instanceof Gendreau06ObjectiveFunction);
		final Gendreau06ObjectiveFunction obj = (Gendreau06ObjectiveFunction) results.objectiveFunction;

		for (final SimulationResult r : results.results) {
			final MASConfiguration config = r.masConfiguration;
			final ProblemClass pc = r.scenario.getProblemClass();

			if (!table.contains(config, pc)) {
				table.put(config, pc, new StringBuilder("seed,instance,duration,frequency,cost,tardiness,travelTime,overTime,computationTime\n"));
			}
			final StringBuilder sb = table.get(config, pc);

			final GendreauProblemClass gpc = (GendreauProblemClass) pc;
			/* seed */
			sb.append(r.seed).append(",")
					/* instance */
					.append(r.scenario.getProblemInstanceId()).append(",")
					/* duration */
					.append(gpc.duration).append(",")
					/* frequency */
					.append(gpc.frequency).append(",")
					/* cost */
					.append(obj.computeCost(r.stats)).append(',')
					/* tardiness */
					.append(obj.tardiness(r.stats)).append(',')
					/* travelTime */
					.append(obj.travelTime(r.stats)).append(',')
					/* overTime */
					.append(obj.overTime(r.stats)).append(',')
					/* computation time */
					.append(r.stats.computationTime).append("\n");
		}

		final Set<Cell<MASConfiguration, ProblemClass, StringBuilder>> set = table.cellSet();
		for (final Cell<MASConfiguration, ProblemClass, StringBuilder> cell : set) {
			try {
				final File dir = new File("files/results/gendreau" + cell.getColumnKey().getId());
				if (!dir.exists() || !dir.isDirectory()) {
					Files.createParentDirs(dir);
					dir.mkdir();
				}
				final File file = new File(dir.getPath() + "/" + cell.getRowKey().toString() + "_" + results.masterSeed + cell.getColumnKey().getId() + ".txt");
				if (file.exists()) {
					file.delete();
				}

				Files.write(cell.getValue().toString(), file, Charsets.UTF_8);
			} catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
