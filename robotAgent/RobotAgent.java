package robotAgent;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetResponder;
import storeAgent.StoreModel.Item;

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
		System.out.println("Agent " + getLocalName() + " waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetBidder(this, template));

	}

	private void unpackArgs() {
		// expected args : {String[] operations, int[] location}
		this.operations = (String[]) this.getArguments()[0];
		this.location = (int[]) this.getArguments()[1];

	}

	public boolean evaluateAction(String action) {
		if (action.startsWith("move")) {
			if (this.robotModel.isReady()) {
				return true;
			}
		}
		return false;
	}

	public String performAction(String action) {
		this.robotModel.setBusy();
		String[] actionStrings = action.split(" ");
		String itemType = actionStrings[1];
		String serialN = actionStrings[2];
		int[] from = { Integer.parseInt(actionStrings[3]), Integer.parseInt(actionStrings[4]) };
		int[] to = { Integer.parseInt(actionStrings[5]), Integer.parseInt(actionStrings[6]) };
		String sourceAgentName = actionStrings[7];
		String distAgentName = actionStrings[8];
		Item item = new Item(serialN, itemType);
		// execute the action

		this.robotModel.goToDest(from[0], from[1], this.robotModel);
		this.robotModel.pickup(item);
		this.notifyAgent("pickup", item, sourceAgentName);
		this.robotModel.goToDest(to[0], to[1], this.robotModel);
		this.robotModel.drop();
		this.notifyAgent("receive", item, distAgentName);

		this.robotModel.setReady();

		return "success";

	}

	private void notifyAgent(String operation, Item item, String receiver) {
		ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
		msg.setContent(operation + " " + item.type + " " + item.serialNum);
		AID receiverAID = new AID(receiver);
		msg.addReceiver(receiverAID);
		send(msg);
		System.out.println(this.getAID().getName() + ":Sending notification - " + msg.getContent());

	}

	final static double findDistance(int[] loc1, int[] loc2) {
		int dx = loc1[0] - loc2[1];
		int dy = loc1[0] - loc2[1];
		return Math.sqrt(dx * dx + dy * dy);
	}

	public String propose(String action) {
		String[] actionStrings = action.split(" ");
		int[] from = { Integer.parseInt(actionStrings[3]), Integer.parseInt(actionStrings[4]) };
		// Proposal cost is distance from robot to from location

		int proposal = (int) this.findDistance(from, this.robotModel.getLocation());

		return String.valueOf(proposal);
	}

	/////////////////////////////////////////////////////////
	// Behaviours
	/////////////////////////////////////////////////////////
	class ContractNetBidder extends ContractNetResponder {
		public ContractNetBidder(Agent a, MessageTemplate mt) {
			super(a, mt);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
			System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName()
					+ ". Action is " + cfp.getContent());
			String action = cfp.getContent();
			if (evaluateAction(action)) {
				// We provide a proposal
				String proposal = propose(action);
				System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);
				ACLMessage propose = cfp.createReply();
				propose.setPerformative(ACLMessage.PROPOSE);
				propose.setContent(String.valueOf(proposal));
				return propose;
			} else {
				// We refuse to provide a proposal
				System.out.println("Agent " + getLocalName() + ": Refuse");
				throw new RefuseException("evaluation-failed");
			}
		}

		@Override
		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
				throws FailureException {
			System.out.println("Agent " + getLocalName() + ": Proposal accepted");
			String action = cfp.getContent();
			String actionResult = performAction(action);
			if (actionResult != null) {
				System.out.println("Agent " + getLocalName() + ": Action: " + action + " successfully performed");
				ACLMessage inform = accept.createReply();
				inform.setPerformative(ACLMessage.INFORM);
				inform.setContent(actionResult);
				return inform;
			} else {
				System.out.println("Agent " + getLocalName() + ": Action execution failed");
				throw new FailureException("unexpected-error");
			}
		}

		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			System.out.println("Agent " + getLocalName() + ": Proposal rejected");
		}
	}

}
