package com.multithreading.threads.isinterrupted;

import java.math.BigInteger;

public class Main {
	public static void main(String[] args) {
		Thread thread = new Thread(new LongComputationTask(new BigInteger("2000000"), new BigInteger("100000000")));

		thread.start();
		thread.interrupt();
	}

	private static class LongComputationTask implements Runnable {
		private BigInteger base;
		private BigInteger power;

		public LongComputationTask(BigInteger base, BigInteger power) {
			this.base = base;
			this.power = power;
		}

		@Override
		public void run() {
			System.out.println(this.base + "^" + this.power + " = " + pow(this.base, this.power));
		}

		private BigInteger pow(BigInteger base, BigInteger power) {
			BigInteger result = BigInteger.ONE;

			for (BigInteger i = BigInteger.ZERO; i.compareTo(power) != 0; i = i.add(BigInteger.ONE)) {
				if (Thread.currentThread().isInterrupted()) {
					System.out.println("Prematurely interrupted calculation");
					return BigInteger.ZERO;
				}
				result = result.multiply(base);
			}

			return result;
		}
	}
}
