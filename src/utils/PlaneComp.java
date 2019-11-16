package utils;
import java.util.HashMap;
import java.util.Queue;
import java.util.LinkedList;

@SuppressWarnings("serial")
public class PlaneComp extends PlanePersonality
{

	/**
	 * Current Distance Left (km)
	 */
	String goal="money";								//money, time, fuel, etc


	public PlaneComp() {
		this.route = new LinkedList<>(){{add("DUL");add("DUL");add("DUL");}};
		this.actualPos = new HashMap<String, Integer>(){{put("x", 3);put("y", 3);}};
		this.finalPos = new HashMap<String, Integer>(){{put("x", 0);put("y", 0);}};
	}

} 