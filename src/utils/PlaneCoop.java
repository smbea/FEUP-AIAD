package utils;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class PlaneCoop extends PlanePersonality
{
	int minAcceptBid=15;

	
	public PlaneCoop() {
		this.route = new LinkedList<>(){{add("DDR");add("DDR");add("DDR");add("DDR");}};
		super.distanceLeft = this.route.size();
		this.actualPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
		this.finalPos = new HashMap<String, Integer>(){{put("x", 4);put("y", 4);}};
	}

} 