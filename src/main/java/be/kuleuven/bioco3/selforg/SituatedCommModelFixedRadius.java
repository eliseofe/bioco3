package be.kuleuven.bioco3.selforg;

import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

public class SituatedCommModelFixedRadius extends SituatedCommModel {

	private double sensingRadius;
	
	public SituatedCommModelFixedRadius(long seed, double sensingRadius) {
		super();
		this.sensingRadius = sensingRadius;
	}

	
	/**
	 * @return A {@link SupplierRng} that supplies {@link SituatedCommModel}
	 *         instances.
	 */
	public static SupplierRng<SituatedCommModelFixedRadius> supplier(final double parcelRadius) {
		return new DefaultSupplierRng<SituatedCommModelFixedRadius>() {
			public SituatedCommModelFixedRadius get(long seed) {
				return new SituatedCommModelFixedRadius(seed, parcelRadius);
			}
			
			@Override
	    public String toString() {
	      return super.toString() + "-" + parcelRadius;
	    }
		};
	}

	@Override
	protected void fillCandidateParcelList(SituatedCommunicator su) {
		// Give to su all parcels within unclaimedParcels that are within
		// parcelRadius
		for (final DefaultParcel dp : unassignedParcels) {
			// Source location
			Point parcelSource = dp.getPickupLocation();
			// Get a Point which represent the communicator position. Use road model
			// got from init?
			Point communicatorPosition = su.getPosition();
			// Compute distance
			double distance = Point.distance(parcelSource, communicatorPosition);

			// If distance is less than range, add dp to candidate parcels in range
			if (distance < sensingRadius) {
				su.addCandidateParcel(dp);
			} else {

				// Get the parcel destination
				// Point parcelDest = dp.getDestination();
				// // Compute distance
				// distance = Point.distance(parcelDest, communicatorPosition);
				//
				// // If distance is less than range, add dp to candidate parcels in range
				// if (distance < sensingRadius) {
				// su.addCandidateParcel(dp);
				// }
			}
		}
		
	}

}
