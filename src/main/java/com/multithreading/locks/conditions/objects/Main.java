package com.multithreading.locks.conditions.objects;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringJoiner;

public class Main {
	private static final String INPUT_FILE = "./out/matrices";
	private static final String OUTPUT_FILE = "./out/matrices_results";
	private static final int N = 10;

	public static void main(String[] args) throws IOException {
		ThreadSafeQueue queue = new ThreadSafeQueue();
		File inputFile = new File(INPUT_FILE);
		File outputFile = new File(OUTPUT_FILE);

		MatricesReaderProducer matricesProducer = new MatricesReaderProducer(new FileReader(inputFile), queue);
		MatricesMultiplierConsumer matricesConsumer = new MatricesMultiplierConsumer(queue, new FileWriter(outputFile));

		matricesConsumer.start();
		matricesProducer.start();
	}

	private static class MatricesMultiplierConsumer extends Thread {
		private ThreadSafeQueue queue;
		private FileWriter fileWriter;

		public MatricesMultiplierConsumer(ThreadSafeQueue queue,
										  FileWriter fileWriter) {
			this.queue = queue;
			this.fileWriter = fileWriter;
		}

		@Override
		public void run() {
			while (true) {
				MatricesPair matricesPair = queue.remove();
				if (matricesPair == null) {
					System.out.println("No more matrices to read from the queue. Consumer thread is terminating");
					break;
				}

				float[][] result = multiplyMatrices(matricesPair.matrix1, matricesPair.matrix2);

				try {
					saveMatrixToFile(fileWriter, result);
				} catch (IOException var1) {
					var1.printStackTrace();
				}
			}
		}

		private static void saveMatrixToFile(FileWriter fileWriter, float[][] matrix) throws IOException {
			for (int r = 0; r < N; r++) {
				StringJoiner stringJoiner = new StringJoiner(",");
				for (int c = 0; c < N; c++) {
					stringJoiner.add(String.format("%.2f", matrix[r][c]));
				}
				fileWriter.write(stringJoiner.toString());
				fileWriter.write("\n");
			}
			fileWriter.write("\n");
		}

		private float[][] multiplyMatrices(float[][] m1, float[][] m2) {
			float[][] result = new float[N][N];
			for (int r = 0; r < N; r++) {
				for (int c = 0; c < N; c++) {
					for (int k = 0; k < N; k++) {
						result[r][c] += m1[r][k] * m2[k][c];
					}
				}
			}
			return result;
		}
	}

	private static class MatricesReaderProducer extends Thread {
		private Scanner scanner;
		private ThreadSafeQueue queue;

		public MatricesReaderProducer(FileReader reader, ThreadSafeQueue threadSafeQueue) {
			this.scanner = new Scanner(reader);
			this.queue = threadSafeQueue;
		}

		@Override
		public void run() {
			while (true) {
				float[][] matrix1 = readMatrix();
				float[][] matrix2 = readMatrix();

				if (matrix1 == null || matrix2 == null) {
					queue.terminate();
					System.out.println("No more matrices to read from the file. Producer thread is terminating");
					return;
				}

				MatricesPair matricesPair = new MatricesPair();
				matricesPair.matrix1 = matrix1;
				matricesPair.matrix2 = matrix2;

				queue.add(matricesPair);
			}
		}

		private float[][] readMatrix() {
			float[][] matrix = new float[N][N];
			for (int r = 0; r < N; r++) {
				if (!scanner.hasNext()) {
					return null;
				}
				String[] line = scanner.nextLine().split(",");
				for (int c = 0; c < N; c++) {
					matrix[r][c] = Float.valueOf(line[c]);
				}
			}
			scanner.nextLine();
			return matrix;
		}
	}

	private static class ThreadSafeQueue {
		private Queue<MatricesPair> queue = new LinkedList<>();
		private boolean isEmpty = true;
		private boolean isTerminate = false;
		private static final int CAPACITY = 5;

		public synchronized void add(MatricesPair matricesPair) {
			while (queue.size() == CAPACITY) {
				try {
					wait();
				} catch (InterruptedException ignored) {
				}
			}
			queue.add(matricesPair);
			isEmpty = false;
			notify();
		}

		public synchronized MatricesPair remove() {
			MatricesPair matricesPair;
			while (isEmpty && !isTerminate) {
				try {
					wait();
				} catch (InterruptedException e) {
				}
			}

			if (queue.size() == 1) {
				isEmpty = true;
			}

			if (queue.size() == 0 && isTerminate) {
				return null;
			}

			System.out.println("Queue size is " + queue.size());
			matricesPair = queue.remove();
			if (queue.size() == CAPACITY - 1) {
				notifyAll();
			}
			return matricesPair;
		}

		public synchronized void terminate() {
			isTerminate = true;
			notifyAll();
		}
	}

	private static class MatricesPair {
		private float[][] matrix1;
		private float[][] matrix2;
	}
}
