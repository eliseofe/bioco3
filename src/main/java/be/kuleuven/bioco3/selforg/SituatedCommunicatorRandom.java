package main.java.be.kuleuven.bioco3.selforg;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

import com.google.common.base.Optional;

public class SituatedCommunicatorRandom extends SituatedCommunicator {

	private final RandomGenerator rng;

	public SituatedCommunicatorRandom(long seed) {
		rng = new MersenneTwister(seed);
	}

	public Optional<DefaultParcel> decideNextParcel() {
		Optional<DefaultParcel> result = Optional.absent();
		if (!candidateParcels.isEmpty()) {
			int indexToGet = rng.nextInt(candidateParcels.size());
			int i = 0;
			for (DefaultParcel dp : candidateParcels) {
				if (i == indexToGet) {
					result = Optional.of(dp);
				}
				i++;
			}
		}
		return result;
	}

	// not needed
	public void waitFor(DefaultParcel p) {
	}

	public static SupplierRng<SituatedCommunicatorRandom> supplier() {
		return new DefaultSupplierRng<SituatedCommunicatorRandom>() {
			public SituatedCommunicatorRandom get(long seed) {
				return new SituatedCommunicatorRandom(seed);
			}
		};
	}

}
