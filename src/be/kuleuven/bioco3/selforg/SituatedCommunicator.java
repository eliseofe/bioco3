package be.kuleuven.bioco3.selforg;

import java.util.Collection;

import rinde.logistics.pdptw.mas.comm.Communicator;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.event.Event;
import rinde.sim.event.EventDispatcher;
import rinde.sim.event.Listener;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.pdptw.common.DefaultVehicle;

import com.google.common.base.Optional;

public abstract class SituatedCommunicator implements Communicator {
	
	private Optional<SituatedCommModel> scModel;
	private RoadModel rm;
  private final EventDispatcher eventDispatcher;
  private Optional<DefaultParcel> assignedParcel;
  
  public SituatedCommunicator(){
  	eventDispatcher = new EventDispatcher(CommunicatorEventType.values());
  	scModel = Optional.absent();
  	assignedParcel = Optional.absent();
  }
  
  public void init(RoadModel rm, PDPModel pm, DefaultVehicle v) {
		this.rm = rm;
	}
  
  public Optional<DefaultParcel> getAssignedParcel() {
		return assignedParcel;
	}

	public void setAssignedParcel(DefaultParcel assignedParcel) {
		this.assignedParcel = Optional.of(assignedParcel);
	}
  
  public void init(SituatedCommModel model){
  	scModel = Optional.of(model);
  }
  
  /**
   * Lay a claim on the specified {@link DefaultParcel}.
   * @param p The parcel to claim.
   */
  public void claim(DefaultParcel p) {
    // forward call to model
    scModel.get().claim(this, p);
  }
  
	public Collection<DefaultParcel> getParcels() {
		return scModel.get().getUnclaimedParcels();
	}
  
  /**
   * Notifies this situated communicator of a change in the environment.
   */
  public void update() {
    eventDispatcher
        .dispatchEvent(new Event(CommunicatorEventType.CHANGE, this));
  }
	
  public void addUpdateListener(Listener l) {
    eventDispatcher.addListener(l, CommunicatorEventType.CHANGE);
  }

}
