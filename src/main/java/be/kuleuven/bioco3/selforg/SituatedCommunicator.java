package be.kuleuven.bioco3.selforg;

import static com.google.common.collect.Sets.newLinkedHashSet;

import java.util.Collection;
import java.util.Set;

import rinde.logistics.pdptw.mas.comm.Communicator;
import rinde.sim.core.graph.Point;
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
  private DefaultVehicle vehicle;
  private final EventDispatcher eventDispatcher;
  private Optional<DefaultParcel> assignedParcel;
  protected final Set<DefaultParcel> candidateParcels;

  public SituatedCommunicator() {
    eventDispatcher = new EventDispatcher(CommunicatorEventType.values());
    scModel = Optional.absent();
    assignedParcel = Optional.absent();
    candidateParcels = newLinkedHashSet();
  }

  public void init(RoadModel rm, PDPModel pm, DefaultVehicle v) {
    this.rm = rm;
    this.vehicle = v;
  }

  public boolean addCandidateParcel(DefaultParcel candidate) {
    return candidateParcels.add(candidate);
  }

  public void clearCandidateParcels() {
    candidateParcels.clear();
  }

  public Optional<DefaultParcel> getAssignedParcel() {
    return assignedParcel;
  }

  protected void setAssignedParcel(Optional<DefaultParcel> assignedParcel) {
    this.assignedParcel = assignedParcel;
    eventDispatcher.dispatchEvent(new Event(CommunicatorEventType.CHANGE, this));
  }

  public void setNoAssignedParcel() {
    this.assignedParcel = Optional.absent();
  }

  public void init(SituatedCommModel model) {
    scModel = Optional.of(model);
  }

  /**
   * Lay a claim on the specified {@link DefaultParcel}.
   * 
   * @param p
   *          The parcel to claim.
   */
  public void claim(DefaultParcel p) {
    // forward call to model
    scModel.get().claim(this, p);
  }

  // Overwrite this to indicate the simulator which one is the next parcel to get delivered.
  public Collection<DefaultParcel> getParcels() {
    return assignedParcel.asSet();
  }

  public void addUpdateListener(Listener l) {
    eventDispatcher.addListener(l, CommunicatorEventType.CHANGE);
  }

  public Point getPosition() {
    return rm.getPosition(vehicle);
  }

  public abstract Optional<DefaultParcel> decideNextParcel();

}
