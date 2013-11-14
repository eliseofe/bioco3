package be.kuleuven.bioco3.selforg;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

public class SituatedCommunitorRandom extends SituatedCommunicator {

	private final RandomGenerator rng;
	
	public SituatedCommunitorRandom(long seed) {
		rng = new MersenneTwister(seed);
	}

	// not needed
	public void waitFor(DefaultParcel p) {}
	
	public static SupplierRng<SituatedCommunitorRandom> supplier() {
		return new DefaultSupplierRng<SituatedCommunitorRandom>() {
			public SituatedCommunitorRandom get(long seed) {
				return new SituatedCommunitorRandom(seed);
			}
		};
  }

}
