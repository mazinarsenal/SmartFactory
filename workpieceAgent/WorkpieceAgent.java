package workpieceAgent;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.ContractNetInitiator;
import recipeManager.Process;
import recipeManager.Recipe;
import recipeManager.RecipeLoader;

public class WorkpieceAgent extends Agent {
	private int nResponders;
	private String itemType;
	private int serialN;

	private String recipesPath = "c:\\Users\\Mazin\\workspace\\SmartFactory\\recipes\\";
	private RecipeLoader recipeLoader;
	private Recipe recipe;
	private int stepN;
	private Process currentProcess;
	private RobotDestination currentProcessDestination;
	private HashMap<String, RobotDestination> itemsToMove;

	public WorkpieceAgent() {
		this.stepN = 0;
		this.itemsToMove = new HashMap<String, RobotDestination>();
	}

	protected void setup() {

		this.itemType = (String) this.getArguments()[0];
		this.serialN = (int) this.getArguments()[1];
		this.recipeLoader = new RecipeLoader(this.recipesPath);
		this.recipe = this.recipeLoader.load(itemType);
		// System.out.println("Recipe: " +
		// this.recipe.getProcesses().get(0).getInputMaterials());

		this.executeNextStep();
		// this.contractStorage("store Box");
		// this.contractStorage("fetch box");
		// this.contractTransport("move box");
		// this.delegate("fetch box 123");
		// this.delegate("AssembleBearingBox", "process");

	}

	private void executeNextStep() {

		if (this.stepN < this.recipe.getProcesses().size()) {
			this.currentProcess = this.recipe.getProcesses().get(this.stepN);
			// Use the process from the recipe
			this.contractProcess(this.currentProcess.getName());
			// this.contractStorage("store Box");
			// this.contractTransport("move Box");
			// this.contractStorage("store Bearing");
			// this.contractTransport("move Bearing");
			// this.contractStorage("store AssemblyTray");
			// this.contractTransport("move AssemblyTray");

			this.stepN += 1;
		} else {
			System.out.println(this.getAID() + " completed .. Terminating workpiece agent");
			this.doDelete();
		}
	}

	void contractProcess(String process) {
		DFAgentDescription[] serviceAgents = getServiceAgents(process);
		if ((serviceAgents != null) && (serviceAgents.length > 0)) {

			System.out.println(
					"Trying to delegate " + process + " to one out of " + serviceAgents.length + " responders.");

			// Fill the CFP message
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			for (DFAgentDescription agent : serviceAgents) {
				msg.addReceiver(agent.getName());
			}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 100000));
			msg.setContent(process + " " + this.serialN);

			System.out.println("Started contracting for task of type process");
			addBehaviour(new ProcessDelegation(this, msg));

		} else {
			System.out.println("No responder specified.");
		}

	}

	void contractStorage(String task) {
		// task in the form of "store/fetch itemType"
		// Find the service requested
		String service = task.split(" ")[0];
		// Find all agents that can perform this service
		DFAgentDescription[] serviceAgents = getServiceAgents(service);

		if ((serviceAgents != null) && (serviceAgents.length > 0)) {

			System.out
					.println("Trying to delegate " + task + " to one out of " + serviceAgents.length + " responders.");

			// Fill the CFP message
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			for (DFAgentDescription agent : serviceAgents) {
				msg.addReceiver(agent.getName());
			}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
			msg.setContent(task + " " + this.serialN);

			System.out.println("Started contracting for task of type storage");
			addBehaviour(new StorageDelegation(this, msg));

		} else {
			System.out.println("No responder specified.");
		}

	}

	void contractTransport(String itemType, String operation) {
		// task format: "move itemType"
		// source info will come form this.currentProcessDestination
		// destination info will be looked up in this.itemsToMove
		// Find the service requested
		String service = "move";

		// Find all agents that can perform this service
		DFAgentDescription[] serviceAgents = getServiceAgents(service);

		if ((serviceAgents != null) && (serviceAgents.length > 0)) {

			System.out.println("Trying to delegate move to one out of " + serviceAgents.length + " responders.");

			// Fill the CFP message
			ACLMessage msg = new ACLMessage(ACLMessage.CFP);
			for (DFAgentDescription agent : serviceAgents) {
				msg.addReceiver(agent.getName());
			}
			msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
			// We want to receive a reply in 10 secs
			msg.setReplyByDate(new Date(System.currentTimeMillis() + 10000));

			int[] from = this.currentProcessDestination.location;
			int[] to = this.itemsToMove.get(itemType).location;
			String sourceAgentName = this.currentProcessDestination.agentAID.getName();
			String distAgentName = this.itemsToMove.get(itemType).agentAID.getName();
			// The opposite destination and source if the operation is fetch
			if (operation.equals("fetch")) {
				from = this.itemsToMove.get(itemType).location;
				to = this.currentProcessDestination.location;
				sourceAgentName = this.itemsToMove.get(itemType).agentAID.getName();
				distAgentName = this.currentProcessDestination.agentAID.getName();

			}
			// cfp format "move itemType serialN from.x from.y to.x to.y
			// operation"
			msg.setContent("move " + itemType + " " + this.serialN + " " + from[0] + " " + from[1] + " " + to[0] + " "
					+ to[1] + " " + sourceAgentName + " " + distAgentName + " " + operation);

			System.out.println("Started contracting for task of type transport: " + msg.getContent());
			addBehaviour(new TransportDelegation(this, msg));

		} else {
			System.out.println("No responder specified.");
		}

	}

	DFAgentDescription[] getServiceAgents(String serviceName) {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription service = new ServiceDescription();
		service.setName(serviceName);
		DFAgentDescription[] services = null;
		dfd.addServices(service);
		System.out.println("Searching for " + serviceName + " agents");
		try {
			services = DFService.search(this, dfd);
		} catch (FIPAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		/*
		 * for (DFAgentDescription s : services) {
		 * System.out.println(s.getName()); }
		 */
		return services;
	}

	final static double findDistance(int[] loc1, int[] loc2) {
		int dx = loc1[0] - loc2[1];
		int dy = loc1[0] - loc2[1];
		return Math.sqrt(dx * dx + dy * dy);
	}

	/////////////////////////////////////////
	// Behaviours
	/////////////////////////////////////////

	class ContractNetDelegate extends ContractNetInitiator {

		public ContractNetDelegate(Agent a, ACLMessage cfp) {
			super(a, cfp);
		}

		protected void handlePropose(ACLMessage propose, Vector v) {
			System.out.println("Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
		}

		protected void handleRefuse(ACLMessage refuse) {
			System.out.println("Agent " + refuse.getSender().getName() + " refused");
		}

		protected void handleFailure(ACLMessage failure) {
			if (failure.getSender().equals(myAgent.getAMS())) {
				// FAILURE notification from the JADE runtime: the
				// receiver
				// does not exist
				System.out.println("Responder does not exist");
			} else {
				System.out.println("Agent " + failure.getSender().getName() + " failed");
			}
			// Immediate failure --> we will not receive a response from
			// this agent
			nResponders--;
		}

		protected void handleInform(ACLMessage inform) {
			System.out
					.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		}

	}

	class ProcessDelegation extends ContractNetDelegate {

		public ProcessDelegation(Agent a, ACLMessage cfp) {
			super(a, cfp);
			// TODO Auto-generated constructor stub
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {
			if (responses.size() < nResponders) {
				// Some responder didn't reply within the specified
				// timeout
				System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
			}
			// Evaluate proposals.
			int bestProposal = 99999;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			int[] processLocation = { 0, 0 };
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();

				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					// We assume the proposal msg is in the form of
					// "NoOfQuedJobs Location.x Location.Y
					int proposal = Integer.parseInt(msg.getContent().split(" ")[0]);
					if (proposal < bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
						processLocation[0] = Integer.parseInt(msg.getContent().split(" ")[1]);
						processLocation[1] = Integer.parseInt(msg.getContent().split(" ")[2]);

					}
				}
			}
			// Accept the proposal of the best proposer
			if (accept != null) {
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
				// Pass the missing materials
				String missingMaterials = "";
				for (String material : WorkpieceAgent.this.currentProcess.getInputMaterials()) {
					missingMaterials += material + " ";
					WorkpieceAgent.this.contractStorage("fetch " + material);
				}
				accept.setContent(missingMaterials);

				WorkpieceAgent.this.currentProcessDestination = new RobotDestination(
						(AID) accept.getAllReceiver().next(), processLocation);

			}
		}

		protected void handleInform(ACLMessage inform) {
			super.handleInform(inform);
			WorkpieceAgent.this.executeNextStep();
		}
	}

	class StorageDelegation extends ContractNetDelegate {
		String itemType;
		String operation;

		public StorageDelegation(Agent a, ACLMessage cfp) {
			super(a, cfp);
			this.operation = cfp.getContent().split(" ")[0];
			this.itemType = cfp.getContent().split(" ")[1];
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {
			if (responses.size() < nResponders) {
				// Some responder didn't reply within the specified
				// timeout
				System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
			}
			// Evaluate proposals.
			double bestProposal = 99999;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			int[] processLocation = { 0, 0 };
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();

				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					// We assume the proposal msg is in the form of Location.x
					// Location.Y
					int[] storeLocation = { Integer.parseInt(msg.getContent().split(" ")[0]),
							Integer.parseInt(msg.getContent().split(" ")[1]) };
					double proposal = WorkpieceAgent.this.findDistance(storeLocation,
							WorkpieceAgent.this.currentProcessDestination.location);
					if (proposal < bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;
						processLocation = storeLocation;

					}
				}
			}
			// Accept the proposal of the best proposer
			if (accept != null) {
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

				WorkpieceAgent.this.itemsToMove.put(this.itemType,
						new RobotDestination((AID) accept.getAllReceiver().next(), processLocation));
				WorkpieceAgent.this.contractTransport(this.itemType, this.operation);

			} else {
				System.out.println("No bids received .. retrying again in 30 seconds");
				WorkpieceAgent.this.doWait(30000);
				WorkpieceAgent.this.contractStorage(this.operation + " " + this.itemType);
			}
		}

		protected void handleInform(ACLMessage inform) {
			System.out
					.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
			// WorkpieceAgent.this.contractTransport("move Box");
		}

	}

	class TransportDelegation extends ContractNetDelegate {
		String itemType;
		String operation;

		public TransportDelegation(Agent a, ACLMessage cfp) {
			super(a, cfp);
			this.operation = cfp.getContent().split(" ")[9];
			this.itemType = cfp.getContent().split(" ")[1];
		}

		protected void handleAllResponses(Vector responses, Vector acceptances) {
			if (responses.size() < nResponders) {
				// Some responder didn't reply within the specified
				// timeout
				System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
			}
			// Evaluate proposals.
			double bestProposal = 99999;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			int[] processLocation = { 0, 0 };
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();

				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					// We assume the proposal msg is in the form of distance

					int proposal = Integer.parseInt(msg.getContent().split(" ")[0]);
					if (proposal < bestProposal) {
						bestProposal = proposal;
						bestProposer = msg.getSender();
						accept = reply;

					}
				}
			}
			// Accept the proposal of the best proposer
			if (accept != null) {
				System.out.println("Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
				accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);

			} else {
				System.out.println("No bids received .. retrying again in 30 seconds");
				WorkpieceAgent.this.doWait(30000);
				WorkpieceAgent.this.contractTransport(this.itemType, this.operation);
			}
		}

	}

	class RobotDestination {
		AID agentAID;
		int[] location;

		public RobotDestination(AID agentAID, int[] location) {
			this.agentAID = agentAID;
			this.location = location;
		}
	}
}
