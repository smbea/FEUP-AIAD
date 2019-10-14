import jade.core.Agent;
import jade.core.behaviours.*;

@SuppressWarnings("serial")
public class Plane extends Agent {
	protected void setup() {
		addBehaviour( // -------- Anonymous SimpleBehaviour

			new SimpleBehaviour(this) {
				int n = 0;

				public void action() {
					System.out.println("Hello World! Im a plane bitch " + myAgent.getLocalName());
					n++;
				}

				public boolean done() {
					return n >= 3;
				}
			});
	} // --- setup ---
}
