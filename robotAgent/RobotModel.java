package robotAgent;

import storeAgent.StoreModel.Item;

public class RobotModel {
	private float x;
	private float y;
	private float speed = 1;

	enum Status {
		BUSY, FAILURE, READY
	};

	private Status status;
	private Item load;

	public RobotModel() {
		this.status = Status.READY;
	}

	void goToDest(final float xg, final float yg, final RobotModel robot) {
		// Simulate straight line motion
		new Thread(new Runnable() {
			public void run() {
				System.out.println("Goal received (x,y) = " + String.valueOf(xg) + "," + String.valueOf(yg));
				int timeStep = 100;
				float dx = xg - robot.x;
				float dy = yg - robot.y;
				float distance = (float) Math.sqrt(dx * dx + dy * dy);
				float vxh = dx / distance;
				float vyh = dy / distance;
				while (Math.abs(xg - robot.x) > Math.abs(vxh)) {

					robot.x += vxh;
					robot.y += vyh;
					try {
						Thread.sleep(timeStep);

					} catch (InterruptedException ie) {
					}
					System.out
							.println("New location (x,y) = " + String.valueOf(robot.x) + "," + String.valueOf(robot.y));

				}

			}
		}).start();
	}

	public void pickup(Item load) {
		if (this.load == null) {
			this.load = load;
		}
	}

}
