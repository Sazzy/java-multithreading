package com.multithreading.locks.reentrant.readwrite;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Main {
	public static final int HIGHER_PRICE = 1000;

	public static void main(String[] args) throws InterruptedException {
		InventoryDatabase inventoryDatabase = new InventoryDatabase();

		Random random = new Random();
		for (int i = 0; i < 100000; i++) {
			inventoryDatabase.addItem(random.nextInt(HIGHER_PRICE));
		}

		Thread writer = new Thread(() -> {
			while (true) {
				inventoryDatabase.addItem(random.nextInt(HIGHER_PRICE));
				inventoryDatabase.removeItem(random.nextInt(HIGHER_PRICE));

				try {
					Thread.sleep(10);
				} catch (InterruptedException ignored) {
				}
			}
		});

		writer.setDaemon(true);
		writer.start();

		int numberOfReadThreads = 7;
		List<Thread> readers = new ArrayList<>();

		for (int readerIndex = 0; readerIndex < numberOfReadThreads; readerIndex++) {
			Thread reader = new Thread(() -> {
				for (int i = 0; i < 100000; i++) {
					int upperBoundPrice = random.nextInt(HIGHER_PRICE);
					int lowerBoundPrice = upperBoundPrice > 0 ? random.nextInt(upperBoundPrice) : 0;

					inventoryDatabase.getNumberOfItemsInPriceRange(lowerBoundPrice, upperBoundPrice);
				}
			});

			reader.setDaemon(true);
			readers.add(reader);
		}

		long startReadingTime = System.currentTimeMillis();

		for (Thread reader : readers) {
			reader.start();
		}

		for (Thread reader : readers) {
			reader.join();
		}

		long endReadingTime = System.currentTimeMillis();

		System.out.printf("Reading took %d ms%n", endReadingTime - startReadingTime);
	}

	public static class InventoryDatabase {
		private TreeMap<Integer, Integer> priceToCountMap = new TreeMap<>();
		private ReentrantLock lock = new ReentrantLock();
		private ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
		private Lock readLock = readWriteLock.readLock();
		private Lock writeLock = readWriteLock.writeLock();

		public int getNumberOfItemsInPriceRange(int lowerBound, int upperBound) {
			readLock.lock();
			try {
				Integer fromKey = priceToCountMap.ceilingKey(lowerBound);

				Integer toKey = priceToCountMap.floorKey(upperBound);

				if (fromKey == null || toKey == null) {
					return 0;
				}

				NavigableMap<Integer, Integer> rangeOfPrices = priceToCountMap.subMap(fromKey, true, toKey, true);
				int sum = 0;
				for (int numberOfItemsForPrice : rangeOfPrices.values()) {
					sum += numberOfItemsForPrice;
				}

				return sum;
			} finally {
				readLock.unlock();
			}
		}

		public void addItem(int price) {
			writeLock.lock();
			try {
				Integer numberOfItemsForPrice = priceToCountMap.get(price);
				if (numberOfItemsForPrice == null) {
					priceToCountMap.put(price, 1);
				} else {
					priceToCountMap.put(price, numberOfItemsForPrice + 1);
				}
			} finally {
				writeLock.unlock();
			}
		}

		public void removeItem(int price) {
			writeLock.lock();
			try {
				Integer numberOfItemsForPrice = priceToCountMap.get(price);
				if (numberOfItemsForPrice == null || numberOfItemsForPrice == 1) {
					priceToCountMap.remove(price);
				} else {
					priceToCountMap.put(price, numberOfItemsForPrice - 1);
				}
			} finally {
				writeLock.unlock();
			}
		}
	}
}
