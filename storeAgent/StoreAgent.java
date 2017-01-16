package storeAgent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

@SuppressWarnings("serial")
public class StoreAgent extends Agent {
	StoreModel model;

	final static String[] OPS = { "store", "fetch" };
	final static Set<String> OPERATIONS = new HashSet<String>(Arrays.asList(OPS));

	// Constructors
	public StoreAgent() {
		this.model = new StoreModel();
	}

	// Agent setup
	protected void setup() {
		try {
			System.out.println(getLocalName() + " setting up");

			// create the agent description of itself
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			for (String operation : this.OPERATIONS) {
				ServiceDescription service = new ServiceDescription();
				service.setName(operation);
				service.setType(operation);
				dfd.addServices(service);
			}

			DFService.register(this, dfd);
		} catch (Exception e) {
			System.out.println("Saw exception in StoreAgent: " + e);
			e.printStackTrace();
		}
		System.out.println("Agent " + getLocalName() + " waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetResponder(this, template) {
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
		});
	}

	private boolean evaluateAction(String action) {
		String operation = action.split(" ")[0];
		String type = action.split(" ")[1];
		System.out.println("Evaluating operation: " + operation);
		System.out.println(StoreAgent.OPERATIONS.contains(operation));
		if (StoreAgent.OPERATIONS.contains(operation)) {
			if (operation.equals("store")) {
				System.out.println(this.model.getFreeSpace());
				return this.model.getFreeSpace() > 0;
			}
			if (operation.equals("fetch")) {
				return model.hasItem(type);
			}
		}
		return false;

	}

	private String propose(String action) throws RefuseException {
		String operation = action.split(" ")[0];
		String type = action.split(" ")[1];
		if (StoreAgent.OPERATIONS.contains(operation)) {
			if (operation.equals("store")) {
				return String.valueOf(this.model.getFreeSpace());
			}
			if (operation.equals("fetch")) {
				return String.valueOf(this.model.countType(type));
			}
		}
		System.out.println("Agent " + getLocalName() + ": Refuse to propose " + type);
		throw new RefuseException("Failed to propose");

	}

	private String performAction(String action) {
		String operation = action.split(" ")[0];
		String type = action.split(" ")[1];
		if (operation.equals("store")) {
			String serialN = action.split(" ")[2];
			this.model.storeItem(this.model.new Item(type, serialN));
			return action;
		}
		if (operation.equals("fetch")) {
			Item load = this.model.fetchItem(type);
			return load.serialNum + " " + load.type;
		}
		return null;
	}

}
