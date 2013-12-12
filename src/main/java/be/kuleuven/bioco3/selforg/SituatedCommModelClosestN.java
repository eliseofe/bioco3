package be.kuleuven.bioco3.selforg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

public class SituatedCommModelClosestN extends SituatedCommModel {

	private int nClosestParcels;

	public SituatedCommModelClosestN(double seed, int n) {
		super();
		nClosestParcels = n;
	}

	/**
	 * @return A {@link SupplierRng} that supplies {@link SituatedCommModel} instances.
	 */
	public static SupplierRng<SituatedCommModelClosestN> supplier(final int nClosestParcels) {
		return new DefaultSupplierRng<SituatedCommModelClosestN>() {
			public SituatedCommModelClosestN get(long seed) {
				return new SituatedCommModelClosestN(seed, nClosestParcels);
			}

			@Override
			public String toString() {
				return super.toString() + "-" + nClosestParcels;
			}
		};
	}

	@Override
	protected void fillCandidateParcelList(SituatedCommunicator su) {
		final Point referencePos = su.getPosition();
		List<DefaultParcel> listParcels = new ArrayList<DefaultParcel>(unassignedParcels);

		Collections.sort(listParcels, new Comparator<DefaultParcel>() {
			public int compare(DefaultParcel d1, DefaultParcel d2) {
				Point d1Pos = d1.getPickupLocation();
				Point d2Pos = d2.getPickupLocation();
				double distanceD1 = Point.distance(referencePos, d1Pos);
				double distanceD2 = Point.distance(referencePos, d2Pos);
				return (int) Math.signum(distanceD1 - distanceD2);
			}
		});

		int numOfParcels = listParcels.size() < nClosestParcels ? listParcels.size() : nClosestParcels;
		for (int i = 0; i < numOfParcels; i++) {
			su.addCandidateParcel(listParcels.get(i));
		}
		
	}

}
