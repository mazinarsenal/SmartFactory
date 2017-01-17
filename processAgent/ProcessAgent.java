package processAgent;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
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
import partyDemo.HostAgent;

public class ProcessAgent extends Agent {

	private ProcessModel processModel;
	private String[] operations;
	private int[] location;

	public ProcessAgent() {
		this.processModel = new ProcessModel();
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
				service.setType("Process");
				dfd.addServices(service);
			}

			DFService.register(this, dfd);
		} catch (Exception e) {
			System.out.println("Saw exception in ProcessAgent: " + e);
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
		// we assume the action string is in the form "operation
		// workpieceSerialNo"
		String operation = action.split(" ")[0];
		String serialN = action.split(" ")[1];
		for (String op : this.operations) {
			if (op.equals(operation)) {
				return true;
			}
		}
		return false;

	}

	public String startProcess(String action) {
		this.processModel.setBusy();
		String[] missingMaterials = action.split(" ");
		for (String material : missingMaterials) {
			this.processModel.addMissingMaterial(material);
		}
		this.receiveMaterials();
		this.processModel.startProcess();
		this.doWait(5000);
		this.processModel.closeProcess();

		return "Success";
	}

	private void receiveMaterials() {
		System.out.println("Waiting for needed input materials " + this.processModel.missingMaterials);

		// listen if a receive message arrives
		this.doWait();
		ACLMessage msg = this.receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));

		if (msg.getContent() != null) {

			if (msg.getContent().startsWith("receive")) {
				// receive msg should be in the form "receive itemName"
				String receivedItem = msg.getContent().split(" ")[1];
				System.out.println("Received: " + receivedItem);
				this.processModel.reciveMissingMaterial(receivedItem);
				if (this.processModel.noMissingMaterials()) {
					System.out.println("All missing materials are received .. Process starting");

				}
			}
		} else {

			this.doWait();
		}

		if (this.processModel.noMissingMaterials() == false) {
			this.receiveMaterials();
		}

	}

	public String propose(String action) {
		// proposla should be in the form of "queue size locationx locationy"
		return "0 " + String.valueOf(this.location[0]) + " " + String.valueOf(this.location[1]);
	}

	//////////////////////////////////////////////////////////
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
			// We expect to receive the missing materials
			String action = accept.getContent();
			String actionResult = ProcessAgent.this.startProcess(action);
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
