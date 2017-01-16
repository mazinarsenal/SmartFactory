package workpieceAgent;

import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
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

public class WorkpieceAgent extends Agent {
	private int nResponders;

	public WorkpieceAgent() {
		this.nResponders = 1;
	}

	protected void setup() {

		delegate("store box 1234");
	}

	void delegate(String task) {
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
			msg.setContent(task);

			addBehaviour(new ContractNetDelegate(this, msg));
		} else {
			System.out.println("No responder specified.");
		}

	}

	DFAgentDescription[] getServiceAgents(String serviceName) {
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription service = new ServiceDescription();
		service.setName(serviceName);
		DFAgentDescription[] services = null;
		System.out.println("Searching for " + serviceName + " agents");
		try {
			services = DFService.search(this, dfd);
		} catch (FIPAException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		for (DFAgentDescription s : services) {
			System.out.println(s.getName());
		}
		return services;
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

		protected void handleAllResponses(Vector responses, Vector acceptances) {
			if (responses.size() < nResponders) {
				// Some responder didn't reply within the specified
				// timeout
				System.out.println("Timeout expired: missing " + (nResponders - responses.size()) + " responses");
			}
			// Evaluate proposals.
			int bestProposal = -1;
			AID bestProposer = null;
			ACLMessage accept = null;
			Enumeration e = responses.elements();
			while (e.hasMoreElements()) {
				ACLMessage msg = (ACLMessage) e.nextElement();
				if (msg.getPerformative() == ACLMessage.PROPOSE) {
					ACLMessage reply = msg.createReply();
					reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
					acceptances.addElement(reply);
					int proposal = Integer.parseInt(msg.getContent());
					if (proposal > bestProposal) {
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
			}
		}

		protected void handleInform(ACLMessage inform) {
			System.out
					.println("Agent " + inform.getSender().getName() + " successfully performed the requested action");
		}

	}
}
