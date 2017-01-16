package storeAgent;

import storeAgent.StoreModel;
import storeAgent.StoreModel.StorageSlot;

public class Test {

	public static void main(String[] args) {

		// Test model class
		StoreModel storeM = new StoreModel();
		// Test findFreeSlot
		// StorageSlot slot = storeM.findFreeSlot();
		// System.out.println(slot.x);
		// System.out.println(slot.y);
		// Test store
		storeM.storeItem(storeM.new Item("12", "box"));
		storeM.storeItem(storeM.new Item("13", "ball"));
		System.out.println(storeM);
		System.out.println(storeM.hasItem("box"));
		storeM.fetchItem("box");
		System.out.println(storeM.hasItem("box"));
		System.out.println(storeM.hasItem("ball"));

	}

}
