package storeAgent;

import java.util.HashMap;
import java.util.LinkedList;

public class StoreModel {
	// Instance variables
	private int storeWidth = 10;
	private int storeHeight = 10;
	private Item[][] storeArea = new Item[storeWidth][storeHeight];
	// Hashmap to store location of items. The key is item serial number and the
	// value is thestorage slot where the item is stored
	private HashMap<String, StorageSlot> storeSerialIndex = new HashMap<String, StorageSlot>();
	private HashMap<String, LinkedList<StorageSlot>> storeTypeIndex = new HashMap<String, LinkedList<StorageSlot>>();
	private int freeSpace;
	private int reservedSpace;
	private HashMap<String, Item> loadingSpace;

	public StoreModel() {
		this.freeSpace = this.storeWidth * this.storeHeight;
		this.loadingSpace = new HashMap<String, Item>();
		this.reservedSpace = 0;
	}

	// Getters
	// Returns a deep copy of the store
	public Item[][] getStoreArea() {
		Item[][] copy = new Item[this.storeWidth][this.storeHeight];
		for (int i = 0; i < this.storeHeight; i++) {
			for (int j = 0; j < this.storeWidth; i++) {
				copy[i][j] = new Item(this.storeArea[i][j]);
			}
		}
		return copy;

	}

	public int getFreeSpace() {
		return this.freeSpace - this.reservedSpace;
	}

	// Methods
	private StorageSlot findFreeSlot() {
		for (int i = 0; i < this.storeHeight; i++) {
			for (int j = 0; j < this.storeWidth; i++) {
				if (this.storeArea[i][j] == null) {
					return new StorageSlot(i, j);
				}
			}
		}
		return null;
	}

	public void storeItem(Item item) {
		// Find the next free storage slot
		StorageSlot slot = this.findFreeSlot();
		this.storeArea[slot.x][slot.y] = item;
		this.storeSerialIndex.put(item.serialNum, slot);
		LinkedList<StorageSlot> itemsList = this.storeTypeIndex.get(item.type);
		if (itemsList == null) {
			itemsList = new LinkedList<StorageSlot>();
			itemsList.add(slot);
			this.storeTypeIndex.put(item.type, itemsList);

		} else {
			itemsList.add(slot);
		}

		this.freeSpace = this.freeSpace - 1;

	}

	Item fetchItem(String itemType) {
		Item item = this.loadingSpace.get(itemType);
		this.loadingSpace.remove(itemType);
		return item;

	}

	void prepareForLoading(String itemType) {
		LinkedList<StorageSlot> list = this.storeTypeIndex.get(itemType);
		// If this item type is in store
		Item item = null;
		if (list != null) {
			// get its storage slot
			StorageSlot slot = this.storeTypeIndex.get(itemType).removeFirst();
			item = this.storeArea[slot.x][slot.y];
			// Remove its serial number for serial index
			this.storeSerialIndex.remove(item.serialNum);
			// Mark its slot empty
			this.storeArea[slot.x][slot.y] = null;
			// If we dont have this type anymore
			if (this.storeTypeIndex.get(itemType).size() == 0) {
				// REmove it from the type index
				this.storeTypeIndex.remove(itemType);
			}
		}
		this.freeSpace = this.freeSpace + 1;
		this.reservedSpace -= 1;
		this.loadingSpace.put(itemType, item);
	}

	void reserveStorage() {
		this.reservedSpace += 1;
	}

	boolean hasItem(String itemType) {
		return this.storeTypeIndex.containsKey(itemType);
	}

	int countType(String type) {
		if (this.storeTypeIndex.containsKey(type)) {
			return this.storeTypeIndex.get(type).size();
		}
		return 0;
	}

	////////////////////////////////////////////////////////
	// Internal classes
	public class StorageSlot {
		int x;
		int y;

		public StorageSlot(int x, int y) {
			this.x = x;
			this.y = y;
		}
	}

	static final public class Item {
		public String serialNum;
		public String type;

		public Item(String serialNum, String type) {
			this.serialNum = serialNum;
			this.type = type;
		}

		public Item(Item master) {
			this.serialNum = master.serialNum;
			this.type = master.type;
		}

	}
}
