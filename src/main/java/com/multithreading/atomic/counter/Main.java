package com.multithreading.atomic.counter;

import java.util.concurrent.atomic.AtomicInteger;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		InventoryCounter inventoryCounter = new InventoryCounter();

		IncrementingThread incrementingThread = new IncrementingThread(inventoryCounter);
		DecrementingThread decrementingThread = new DecrementingThread(inventoryCounter);

		incrementingThread.start();
		decrementingThread.start();

		incrementingThread.join();
		decrementingThread.join();

		System.out.println(inventoryCounter.getItems());
	}

	private static class InventoryCounter {
		private AtomicInteger items = new AtomicInteger(0);

		public void increment() {
			items.incrementAndGet();
		}

		public void decrement() {
			items.decrementAndGet();
		}

		public int getItems() {
			return items.get();
		}
	}

	private static class DecrementingThread extends Thread {
		private InventoryCounter inventoryCounter;

		public DecrementingThread(InventoryCounter inventoryCounter) {
			this.inventoryCounter = inventoryCounter;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10000; i++) {
				inventoryCounter.decrement();
			}
		}
	}

	private static class IncrementingThread extends Thread {
		private InventoryCounter inventoryCounter;

		public IncrementingThread(InventoryCounter inventoryCounter) {
			this.inventoryCounter = inventoryCounter;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10000; i++) {
				inventoryCounter.increment();
			}
		}
	}
}
