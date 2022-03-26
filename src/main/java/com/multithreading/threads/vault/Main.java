package com.multithreading.threads.vault;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
	public static final int MAX_PASSWORD = 9999;

	public static void main(String[] args) {
		Random random = new Random();

		Vault vault = new Vault(random.nextInt(MAX_PASSWORD));

		List<Thread> threads = new ArrayList<>();
		threads.add(new AscendingHackerThread(vault));
		threads.add(new DescendingHackerThread(vault));
		threads.add(new PoliceThread());

		for (Thread thread : threads) {
			thread.start();
		}
	}

	private static class Vault {
		private final int password;

		public Vault(int password) {
			this.password = password;
		}

		public boolean isPasswordCorrect(int guess) {
			try {
				Thread.sleep(5L);
			} catch (InterruptedException var1) {
				Thread.currentThread().interrupt();
			}
			return guess == this.password;
		}
	}

	private static abstract class HackerThread extends Thread {
		protected final Vault vault;

		public HackerThread(Vault vault) {
			this.vault = vault;
			this.setName(this.getClass().getSimpleName());
			this.setPriority(Thread.MAX_PRIORITY);
		}

		@Override
		public void start() {
			System.out.println("Starting thread [" + this.getName() + "]");
			super.start();
		}
	}

	private static class AscendingHackerThread extends HackerThread {
		public AscendingHackerThread(Vault vault) {
			super(vault);
		}

		@Override
		public void run() {
			for (int i = 0; i < MAX_PASSWORD; i++) {
				if (vault.isPasswordCorrect(i)) {
					System.out.println("Thread [" + this.getName() + "] guessed the password: " + i);
					System.exit(0);
				}
			}
		}
	}

	private static class DescendingHackerThread extends HackerThread {
		public DescendingHackerThread(Vault vault) {
			super(vault);
		}

		@Override
		public void run() {
			for (int i = MAX_PASSWORD; i >= 0; i--) {
				if (vault.isPasswordCorrect(i)) {
					System.out.println("Thread [" + this.getName() + "] guessed the password: " + i);
					System.exit(0);
				}
			}
		}
	}

	private static class PoliceThread extends Thread {
		@Override
		public void run() {
			for (int i = 10; i >= 0; i--) {
				try {
					Thread.sleep(1000L);
				} catch (InterruptedException e) {
					this.interrupt();
				}
				System.out.println(i);
			}
			System.out.println("Game over for hackers");
			System.exit(0);
		}
	}
}
