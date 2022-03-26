package com.multithreading.threads.join;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	public static void main(String[] args) throws InterruptedException {
		List<Long> inputNumbers = Arrays.asList(1000000L, 3435L, 35435L, 2324L, 4656L, 23L, 2435L, 5566L);

		List<FactorialThread> threads = new ArrayList<>();
		for (long inputNumber : inputNumbers) {
			threads.add(new FactorialThread(inputNumber));
		}

		for (Thread thread : threads) {
			thread.start();
		}

		for (Thread thread : threads) {
			thread.join(2000L);
		}

		for (int i = 0; i < inputNumbers.size(); i++) {
			FactorialThread factorialThread = threads.get(i);
			if (factorialThread.isFinished()) {
				System.out.println("Factorial of " + inputNumbers.get(i) + " is " + factorialThread.getResult());
			} else {
				System.out.println("Calculation for number " + inputNumbers.get(i) + " is still in progress");
			}
		}
	}

	public static class FactorialThread extends Thread {
		private Long inputNumber;
		private BigInteger result;
		private boolean isFinished;

		public FactorialThread(Long inputNumber) {
			this.inputNumber = inputNumber;
		}

		@Override
		public void run() {
			this.result = factorial(inputNumber);
			this.isFinished = true;
		}

		public BigInteger factorial(long n) {
			BigInteger tempResult = BigInteger.ONE;

			for (long i = n; i > 0; i--) {
				tempResult = tempResult.multiply(new BigInteger(Long.toString(i)));
			}
			return tempResult;
		}

		public boolean isFinished() {
			return isFinished;
		}

		public BigInteger getResult() {
			return result;
		}
	}
}
