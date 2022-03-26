package com.multithreading.threads.initial;

public class Main {
	public static void main(String[] args) {
		Thread thread = new Thread(() -> {
			String threadName = Thread.currentThread().getName();
			System.out.println("We are now in thread [" + threadName + "]");
			System.out.println("Current priority of thread [" + threadName + "] is " + Thread.currentThread().getPriority());
			throw new RuntimeException("Something critical happened");
		});
		thread.setName("New worker thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setUncaughtExceptionHandler(
				(t, e) -> System.out.println("A critical error happened in thread [" + t.getName() + "]\n" +
						"Error: " + e.getMessage())
		);

		System.out.println("We are in thread [" + Thread.currentThread().getName() + "] before starting a new thread");
		thread.start();
		System.out.println("We are in thread [" + Thread.currentThread().getName() + "] after starting a new thread");
	}
}
