package main.java.be.kuleuven.bioco3.selforg;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Set;

import rinde.logistics.pdptw.mas.comm.AbstractCommModel;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.pdptw.common.DefaultParcel;

import com.google.common.base.Optional;

public abstract class SituatedCommModel extends AbstractCommModel<SituatedCommunicator> implements Listener {

	protected Set<DefaultParcel> unassignedParcels;
	
	public SituatedCommModel(){
		unassignedParcels = newLinkedHashSet();
	}

	public boolean register(SituatedCommunicator element) {
		super.register(element);
		element.init(this);
		return true;
	}

	@Override
	protected void receiveParcel(DefaultParcel p, long time) {
		//System.out.println("New parcel: "+p);
		checkState(!communicators.isEmpty(), "there are no agents..");
		unassignedParcels.add(p);
		checkArgument(unassignedParcels.contains(p), "Parcel %s should be added.", p);

		performDecisionMaking();

	}

	protected void releaseUncommittedParcels() {
		for (final SituatedCommunicator su : communicators) {
			// Release all assigned parcels in order to reallocate them. Put them in unclaimedParcels
			if (su.getAssignedParcel().isPresent()) {
				unassignedParcels.add(su.getAssignedParcel().get());
				//System.out.println("Adding back parcel: "+su.getAssignedParcel().get());
				checkArgument(unassignedParcels.contains(su.getAssignedParcel().get()), "Parcel %s should be added.", su.getAssignedParcel().get());
				su.forgetPreviousAssignment();
				checkState(!su.getAssignedParcel().isPresent(), "There should be no assigned parcel");
			}
			su.clearCandidateParcels();
			checkState(su.getCandidateParcels().isEmpty(), "There should be no candidate parcels");
		}
	}

	protected void commitToParcel(SituatedCommunicator su) {
		// Ask su to decide the one parcel to deliver next based on the list it received
		Optional<DefaultParcel> nextParcel = su.decideNextParcel();
		if (!su.getCandidateParcels().isEmpty()) {
			checkState(nextParcel.isPresent(), "Since the candidate list is not empty one parcel should now be assigned.");
		}
		// TODO: what to do if none is selected?
		// checkState(nextParcel.isPresent(),
		// "Error! No parcel was selected among the candidates in range!");
		// Remove the above parcel from unclaimedParcels
		if (nextParcel.isPresent() && unassignedParcels.contains(nextParcel.get())) {
			su.setAssignedParcel(nextParcel);
			//checkArgument(unassignedParcels.contains(nextParcel.get()), "Parcel %s should still be in the list.", nextParcel.get());
			unassignedParcels.remove(nextParcel.get());
			checkArgument(!unassignedParcels.contains(nextParcel.get()), "Parcel %s should be removed.", nextParcel.get());
			//System.out.println("Removed parcel " + nextParcel.get() + " assigned to " +su);
		}
		
	}

	protected abstract void performDecisionMaking();

	/**
	 * Lays a claim on the specified {@link DefaultParcel}. This means that this
	 * parcel is no longer available to other {@link SituatedCommunicator}s.
	 * 
	 * @param claimer
	 *          The user that claims the parcel.
	 * @param p
	 *          The parcel that is claimed.
	 */
	public void claim(SituatedCommunicator claimer, DefaultParcel p) {

		//System.out.println("Parcel "+p + " claimed by vehicle " + claimer);

		checkState(claimer.getAssignedParcel().isPresent(), "There should be an assigned parcel");
		checkArgument(claimer.getAssignedParcel().get().equals(p), "Claim is done on a non assigned parcel %s", p);

		checkArgument(!unassignedParcels.contains(p), "Parcel %s must be unclaimed.", p);
		claimer.setNoAssignedParcel();
	}

	@Override
	public void registerModelProvider(ModelProvider mp) {
		super.registerModelProvider(mp);
		// mp.getModel(PDPModel.class).getEventAPI().addListener(this,
		// PDPModelEventType.END_DELIVERY, PDPModelEventType.END_PICKUP);
		mp.getModel(PDPModel.class).getEventAPI().addListener(this, PDPModelEventType.END_DELIVERY);
	}

	public void handleEvent(Event e) {
		if (e.getEventType() == PDPModelEventType.END_DELIVERY) {
			performDecisionMaking();
		}

	}

}
