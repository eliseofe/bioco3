package be.kuleuven.bioco3.selforg;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Set;

import rinde.logistics.pdptw.mas.comm.AbstractCommModel;
import rinde.sim.core.graph.Point;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

import com.google.common.base.Optional;

public class SituatedCommModel extends AbstractCommModel<SituatedCommunicator> implements Listener {

  private double sensingRadius;
  private final Set<DefaultParcel> unassignedParcels;

  public SituatedCommModel(long seed, double sensingRadius) {
    this.sensingRadius = sensingRadius;
    unassignedParcels = newLinkedHashSet();
  }

  public boolean register(SituatedCommunicator element) {
    super.register(element);
    element.init(this);
    return true;
  }

  @Override
  protected void receiveParcel(DefaultParcel p, long time) {
    checkState(!communicators.isEmpty(), "there are no agents..");
    unassignedParcels.add(p);
    performDecisionMaking();

  }

  private void performDecisionMaking() {
    for (final SituatedCommunicator su : communicators) {
      // Release all assigned parcels in order to reallocate them. Put them in unclaimedParcels
      if (su.getAssignedParcel().isPresent()) {
	unassignedParcels.add(su.getAssignedParcel().get());
	su.setNoAssignedParcel();
      }
      su.clearCandidateParcels();
    }

    for (final SituatedCommunicator su : communicators) {
      // Give to su all parcels within unclaimedParcels that are within parcelRadius
      for (final DefaultParcel dp : unassignedParcels) {
	// Source location
	Point parcelSource = dp.dto.pickupLocation;
	// Get a Point which represent the communicator position. Use road model got from init?
	Point communicatorPosition = su.getPosition();
	// Compute distance
	double distance = Point.distance(parcelSource,
	    communicatorPosition);

	// If distance is less than range, add dp to candidate parcels
	// in range
	if (distance < sensingRadius) {
	  su.addCandidateParcel(dp);
	} else {

	  // Get the parcel destination
	  // Point parcelDest = dp.getDestination();
	  // // Compute distance
	  // distance = Point.distance(parcelDest,
	  // communicatorPosition);
	  //
	  // // If distance is less than range, add dp to candidate parcels in range
	  // if (distance < sensingRadius) {
	  // su.addCandidateParcel(dp);
	  // }
	}
      }

      // TODO: add a step here that does something if the list of candidate parcels is empty.
      // Ask Rinde what to do, it's ok a vehicle is idle right?

      // Ask su to decide the one parcel to deliver next based on the list it received
      Optional<DefaultParcel> nextParcel = su.decideNextParcel();
      // TODO: what to do if none is selected?
      // checkState(nextParcel.isPresent(),
      // "Error! No parcel was selected among the candidates in range!");
      // Remove the above parcel from unclaimedParcels
      if (nextParcel.isPresent()) {
	unassignedParcels.remove(nextParcel.get());
      }

    }

    // TODO: if there are still unclaimed parcels, do something (what?)

  }

  /**
   * @return A {@link SupplierRng} that supplies {@link SituatedCommModel} instances.
   */
  public static SupplierRng<SituatedCommModel> supplier(
      final double parcelRadius) {
    return new DefaultSupplierRng<SituatedCommModel>() {
      public SituatedCommModel get(long seed) {
	return new SituatedCommModel(seed, parcelRadius);
      }
    };
  }

  /**
   * Lays a claim on the specified {@link DefaultParcel}. This means that this parcel is no longer available to other {@link SituatedCommunicator}s.
   * 
   * @param claimer
   *          The user that claims the parcel.
   * @param p
   *          The parcel that is claimed.
   */
  public void claim(SituatedCommunicator claimer, DefaultParcel p) {
    checkArgument(!unassignedParcels.contains(p), "Parcel %s must be unclaimed.", p);
    claimer.setNoAssignedParcel();
  }

  @Override
  public void registerModelProvider(ModelProvider mp) {
    super.registerModelProvider(mp);
    // mp.getModel(PDPModel.class).getEventAPI().addListener(this, PDPModelEventType.END_DELIVERY, PDPModelEventType.END_PICKUP);
    mp.getModel(PDPModel.class).getEventAPI().addListener(this, PDPModelEventType.END_DELIVERY);
  }

  public void handleEvent(Event e) {
    if (e.getEventType() == PDPModelEventType.END_DELIVERY) {
      performDecisionMaking();
    }

  }

}
