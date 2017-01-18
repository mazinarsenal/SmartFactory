package launcherAgent;

import java.util.ArrayList;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import storeAgent.StoreModel.Item;

public class LauncherAgent extends Agent {
	private PlatformController container;

	protected void setup() {
		try {
			System.out.println(getLocalName() + " setting up");

			// create the agent description of itself and register it in DF
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			DFService.register(this, dfd);
			this.container = getContainerController();
		} catch (Exception e) {
			System.out.println("Saw exception in LauncherAgent: " + e);
			e.printStackTrace();
		}

		this.startAllAgents();
	}

	private void startAllAgents() {
		// start the orderHandlerAgent
		try {
			AgentController orderHandler = this.container.createNewAgent("OrderHandler",
					"orderHandlerAgent.OrderHandlerAgent", null);
			orderHandler.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch OrderHAndler Agent");
			e.printStackTrace();
		}
		// start the assemblyProcessAgent
		try {
			int[] location = { 50, 50 };
			String[] operations = { "assembleBearingBox" };
			Object[] args = { operations, location };
			AgentController assemblyProcessAgent = this.container.createNewAgent("AssemblyProcessAgent",
					"processAgent.ProcessAgent", args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch AssemblyProcess Agent");
			e.printStackTrace();
		}

		// start the StoreAgentA
		try {
			int[] location = { 75, 25 };
			ArrayList<Item> initialItems = new ArrayList<Item>();
			initialItems.add(new Item("i1", "Box"));
			initialItems.add(new Item("i2", "Box"));
			initialItems.add(new Item("i3", "Bearing"));
			initialItems.add(new Item("i4", "Bearing"));
			Object[] args = { location, initialItems };
			AgentController assemblyProcessAgent = this.container.createNewAgent("StoreAgentA", "storeAgent.StoreAgent",
					args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch StoreAgentA");
			e.printStackTrace();
		}
		// start the StoreAgentB
		try {
			int[] location = { 75, 75 };
			ArrayList<Item> initialItems = new ArrayList<Item>();
			initialItems.add(new Item("i5", "AssemblyTray"));
			initialItems.add(new Item("i6", "AssemblyTray"));
			Object[] args = { location, initialItems };
			AgentController assemblyProcessAgent = this.container.createNewAgent("StoreAgentB", "storeAgent.StoreAgent",
					args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch StoreAgentB");
			e.printStackTrace();
		}

		// start the StoreAgentC
		try {
			int[] location = { 25, 75 };
			ArrayList<Item> initialItems = new ArrayList<Item>();
			Object[] args = { location, initialItems };
			AgentController assemblyProcessAgent = this.container.createNewAgent("StoreAgentC", "storeAgent.StoreAgent",
					args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch StoreAgentC");
			e.printStackTrace();
		}

		// start the RobotAgentA
		try {
			int[] location = { 12, 25 };
			String[] operations = { "move" };
			Object[] args = { operations, location };
			AgentController assemblyProcessAgent = this.container.createNewAgent("RobotAgentA", "robotAgent.RobotAgent",
					args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch RobotAgentA");
			e.printStackTrace();
		}

		// start the RobotAgentB
		try {
			int[] location = { 37, 25 };
			String[] operations = { "move" };
			Object[] args = { operations, location };
			AgentController assemblyProcessAgent = this.container.createNewAgent("RobotAgentB", "robotAgent.RobotAgent",
					args);
			assemblyProcessAgent.start();
		} catch (ControllerException e) {
			System.err.println("Failed to launch RobotAgentB");
			e.printStackTrace();
		}
	}

}
