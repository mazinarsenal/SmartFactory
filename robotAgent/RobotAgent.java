package robotAgent;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class RobotAgent extends Agent {
	RobotModel robotModel;
	private String[] operations;
	private int[] location;

	public RobotAgent() {
		this.robotModel = new RobotModel();
	}

	public void setup() {

		this.unpackArgs();
		try {
			System.out.println(getLocalName() + " setting up");

			// create the agent description of itself
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			for (String operation : this.operations) {
				ServiceDescription service = new ServiceDescription();
				service.setName(operation);
				service.setType("Transport");
				dfd.addServices(service);
			}

			DFService.register(this, dfd);
		} catch (Exception e) {
			System.out.println("Saw exception in RobotAgent: " + e);
			e.printStackTrace();
		}

	}

	private void unpackArgs() {
		// expected args : {String[] operations, int[] location}
		this.operations = (String[]) this.getArguments()[0];
		this.location = (int[]) this.getArguments()[1];

	}

}
