package orderHandlerAgent;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;
import jade.wrapper.PlatformController;
import recipeManager.Recipe;
import recipeManager.RecipeLoader;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

public class OrderHandlerAgent extends Agent {

	private PlatformController container;
	private int serialN;

	public OrderHandlerAgent() {

		this.serialN = 0;
	}

	protected void setup() {
		try {
			System.out.println(getLocalName() + " setting up");

			// create the agent description of itself and register it in DF
			DFAgentDescription dfd = new DFAgentDescription();
			dfd.setName(getAID());
			ServiceDescription service = new ServiceDescription();
			service.setName("OrderHandler");
			service.setType("OrderHandler");
			dfd.addServices(service);
			DFService.register(this, dfd);
		} catch (Exception e) {
			System.out.println("Saw exception in StoreAgent: " + e);
			e.printStackTrace();
		}

		try {
			this.setupHTTPServer();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.container = getContainerController();
		this.createOrder("Order assembledBearingBox 2");

	}

	private void setupHTTPServer() throws Exception {

		HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
		server.createContext("/order", new MyHandler());
		server.setExecutor(null); // creates a default executor
		server.start();

	}

	private void createOrder(String order) {
		this.serialN += 1;
		String[] orderStrings = order.split(" ");
		String itemName = orderStrings[1];
		Object[] args = { itemName, this.serialN };
		System.out.println("Processing: " + order);

		// int qty = Integer.valueOf(orderStrings[2]);

		// Recipe recipe = this.recipeLoader.load(itemName);
		// To Do : Create workpiece agent from recipe
		String localName = "Workpiece_" + this.serialN;
		try {
			AgentController workpiece = this.container.createNewAgent(localName, "workpieceAgent.WorkpieceAgent", args);
			workpiece.start();
		} catch (Exception e) {
			System.err.println("Exception while adding workpiece agent  " + this.serialN + " " + e);
			e.printStackTrace();
		}

	}

	class MyHandler implements HttpHandler {
		public void handle(HttpExchange t) throws IOException {
			String response;
			// Order request format: Order itemName Qty
			String request = getStringFromInputStream(t.getRequestBody());
			String[] requestStrings = request.split(" ");
			if ((requestStrings.length == 3) && (requestStrings[0].equals("Order"))) {
				response = "Order received: " + request;
				System.out.println("Request received from");
				System.out.println(t.getRemoteAddress());
				System.out.println("Request method:");
				System.out.println(t.getRequestMethod());
				System.out.println("Request body:");
				System.out.println(request);
				// Order is valid then create the order
				OrderHandlerAgent.this.createOrder(request);
			} else {
				response = "Invalid request ! Valid format: Order itemName Qty";
			}

			t.sendResponseHeaders(200, response.length());
			OutputStream os = t.getResponseBody();
			os.write(response.getBytes());
			os.close();

		}

		// convert InputStream to String
		private String getStringFromInputStream(InputStream is) {

			BufferedReader br = null;
			StringBuilder sb = new StringBuilder();

			String line;
			try {

				br = new BufferedReader(new InputStreamReader(is));
				while ((line = br.readLine()) != null) {
					sb.append(line);
				}

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			return sb.toString();

		}

	}
}
