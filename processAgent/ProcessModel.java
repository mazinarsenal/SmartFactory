package processAgent;

import java.util.ArrayList;

public class ProcessModel {

	enum Status {
		READY, BUSY, FAULT, WAITING
	}

	Status status = Status.READY;
	ArrayList<String> missingMaterials;

	public ProcessModel() {
		this.missingMaterials = new ArrayList<String>();
	}

	// Code to start the process
	void startProcess() {
		this.status = Status.BUSY;
		System.out.println("Process started");
		// Code to start the process

		this.monitorProcess(this);
	}

	private void monitorProcess(final ProcessModel process) {
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(5000);
					process.closeProcess();
				} catch (InterruptedException ie) {
				}
			}
		}).start();

	}

	private void closeProcess() {
		// code to be executed when the process is finished
		this.status = Status.READY;
		System.out.println("Process finished");

	}

	void addMissingMaterial(String material) {
		this.missingMaterials.add(material);
	}

	void setBusy() {
		this.status = Status.BUSY;
	}

	boolean isReady() {
		return this.status == Status.READY;
	}

}
