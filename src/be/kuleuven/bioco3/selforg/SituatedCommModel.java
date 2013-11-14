package be.kuleuven.bioco3.selforg;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newLinkedHashSet;
import static java.util.Collections.unmodifiableSet;

import java.util.Set;

import rinde.logistics.pdptw.mas.comm.AbstractCommModel;
import rinde.logistics.pdptw.mas.comm.BlackboardUser;
import rinde.sim.core.graph.Point;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

public class SituatedCommModel extends AbstractCommModel<SituatedCommunicator> {

	private double parcelRadius;
	private final Set<DefaultParcel> unclaimedParcels;

	public SituatedCommModel(long seed, double parcelRadius) {
		this.parcelRadius = parcelRadius;
		unclaimedParcels = newLinkedHashSet();
	}

	public boolean register(SituatedCommunicator element){
		super.register(element);
    element.init(this);
    return true;
	}
	
	@Override
	protected void receiveParcel(DefaultParcel p, long time) {
		checkState(!communicators.isEmpty(), "there are no agents..");
		unclaimedParcels.add(p);
		for (final SituatedCommunicator su : communicators) {
			// Release all assigned parcels in order to reallocate them. Put them in unclaimedParcels
			if(su.getAssignedParcel().isPresent()){
				unclaimedParcels.add(su.getAssignedParcel().get());
			}
		}
		
		for (final SituatedCommunicator su : communicators) {
			// TODO: give to su all parcels within unclaimedParcels that are within parcelRadius
			for(final DefaultParcel dp : unclaimedParcels){
				Point parcelDest = dp.getDestination();
				
				// TODO: get a Point which represent the communicator position. Use road model got from init?
				// TODO: compute distance
				// TODO: if distance is less than range, add dp to a new collection inside the situatedCommunicator
				// that represents candidate parcels in range to chose from
				
				// TODO: and the source?
				
			}
			// TODO: ask su to decide the one parcel to deliver next based on the list it received
			// TODO: remove the above parcel from unclaimedParcels
		}
		
		// TODO: if there are still unclaimed parcels, do something (what?)
		
		for (final SituatedCommunicator su : communicators) {
			// TODO: this does what exactly?
			// notify all users of the new parcel
			su.update();
		}
	}

	
//	public void registerModelProvider(ModelProvider mp) {
//		super.registerModelProvider(mp);
//		RoadModel rm = mp.getModel(RoadModel.class);
//		rm.
//		// RoadModels.findObjectsWithinRadius(rm.getPosition(Vehicle), rm,
//		// parceRadius); // km
//
//	}

	/**
	 * @return A {@link SupplierRng} that supplies {@link SituatedCommModel}
	 *         instances.
	 */
	public static SupplierRng<SituatedCommModel> supplier(final double parcelRadius) {
		return new DefaultSupplierRng<SituatedCommModel>() {
			public SituatedCommModel get(long seed) {
				return new SituatedCommModel(seed, parcelRadius);
			}
		};
	}

	public Set<DefaultParcel> getUnclaimedParcels() {
		return unmodifiableSet(unclaimedParcels);
	}

	/**
	 * Lays a claim on the specified {@link DefaultParcel}. This means that this
	 * parcel is no longer available to other {@link BlackboardUser}s.
	 * 
	 * @param claimer
	 *          The user that claims the parcel.
	 * @param p
	 *          The parcel that is claimed.
	 */
	public void claim(SituatedCommunicator claimer, DefaultParcel p) {
		checkArgument(unclaimedParcels.contains(p), "Parcel %s must be unclaimed.", p);
		unclaimedParcels.remove(p);
		for (final SituatedCommunicator su : communicators) {
			if (su != claimer) {
				su.update();
			}
		}
	}

}
