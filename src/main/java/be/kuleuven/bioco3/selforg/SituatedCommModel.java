package be.kuleuven.bioco3.selforg;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomGenerator;

import com.google.common.base.Optional;

import rinde.logistics.pdptw.mas.comm.AbstractCommModel;
import rinde.logistics.pdptw.mas.comm.AuctionCommModel;
import rinde.sim.core.model.ModelProvider;
import rinde.sim.core.model.pdp.PDPModel;
import rinde.sim.core.model.pdp.PDPModelEvent;
import rinde.sim.core.model.pdp.PDPModel.PDPModelEventType;
import rinde.sim.core.model.pdp.Vehicle;
import rinde.sim.core.model.road.RoadModel;
import rinde.sim.core.model.road.RoadModels;
import rinde.sim.event.Event;
import rinde.sim.event.Listener;
import rinde.sim.pdptw.common.DefaultParcel;
import rinde.sim.util.SupplierRng;
import rinde.sim.util.SupplierRng.DefaultSupplierRng;

public class SituatedCommModel extends AbstractCommModel<SituatedCommunicator> {
	
	private final RandomGenerator rng;
	private double parcelRadius;

	public SituatedCommModel(long seed, double parcelRadius) {
		rng = new MersenneTwister(seed);
		this.parcelRadius = parcelRadius;
	}

	@Override
	protected void receiveParcel(DefaultParcel p, long time) {
		// TODO Auto-generated method stub
		
	}
	
	public void registerModelProvider(ModelProvider mp) {
	    super.registerModelProvider(mp);
	    RoadModel rm = mp.getModel(RoadModel.class);
	    //RoadModels.findObjectsWithinRadius(rm.getPosition(Vehicle), rm, parceRadius); // km
	    
	}
	
	 /**
	   * @return A {@link SupplierRng} that supplies {@link SituatedCommModel}
	   *         instances.
	   */
	  public static SupplierRng<SituatedCommModel> supplier(final double parcelRadius) {
	    return new DefaultSupplierRng<SituatedCommModel>() {	      
	      public SituatedCommModel get(long seed) {
	        return new SituatedCommModel(seed,parcelRadius);
	      }
	    };
	  }

}
