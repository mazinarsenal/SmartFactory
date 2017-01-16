package robotAgent;

public class RobotModel {
	float x;
	float y;
	float speed = 1;

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

}
