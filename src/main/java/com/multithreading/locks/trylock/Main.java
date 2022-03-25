package com.multithreading.locks.trylock;

import javafx.animation.AnimationTimer;
import javafx.animation.FillTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Main extends Application {
	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		primaryStage.setTitle("Cryptocurrency Prices");

		GridPane grid = createGrid();
		Map<String, Label> cryptoLabels = createCryptoPricesLabels();

		addLabelsToGrid(cryptoLabels, grid);

		double width = 300;
		double height = 250;

		StackPane root = new StackPane();

		Rectangle background = createBackgroundRectangleWithAnimation(width, height);

		root.getChildren().add(background);
		root.getChildren().add(grid);

		primaryStage.setScene(new Scene(root, width, height));

		PricesContainer pricesContainer = new PricesContainer();

		PriceUpdater priceUpdater = new PriceUpdater(pricesContainer);

		AnimationTimer animationTimer = new AnimationTimer() {
			@Override
			public void handle(long l) {
				if (pricesContainer.getLock().tryLock()) {
					try {
						Label bitcoinLabel = cryptoLabels.get("BTC");
						bitcoinLabel.setText(String.valueOf(pricesContainer.getBitcoinPrice()));

						Label etherLabel = cryptoLabels.get("ETH");
						etherLabel.setText(String.valueOf(pricesContainer.getEtherPrice()));

						Label litecoinLabel = cryptoLabels.get("LTC");
						litecoinLabel.setText(String.valueOf(pricesContainer.getLitecoinPrice()));

						Label bitcoinCashLabel = cryptoLabels.get("BCH");
						bitcoinCashLabel.setText(String.valueOf(pricesContainer.getBitcoinCashPrice()));

						Label rippleLabel = cryptoLabels.get("XRP");
						rippleLabel.setText(String.valueOf(pricesContainer.getRipplePrice()));
					} finally {
						pricesContainer.getLock().unlock();
					}
				}
			}
		};

		animationTimer.start();

		priceUpdater.start();

		primaryStage.show();
	}

	private Map<String, Label> createCryptoPricesLabels() {
		Label bitcoinPrice = new Label("0");
		bitcoinPrice.setId("BTC");

		Label etherPrice = new Label("0");
		etherPrice.setId("ETH");

		Label litecoinPrice = new Label("0");
		litecoinPrice.setId("LTC");

		Label bitcoinCashPrice = new Label("0");
		bitcoinCashPrice.setId("BCH");

		Label ripplePrice = new Label("0");
		ripplePrice.setId("XRP");

		Map<String, Label> labels = new HashMap<>();
		labels.put("BTC", bitcoinPrice);
		labels.put("ETH", etherPrice);
		labels.put("LTC", litecoinPrice);
		labels.put("BCH", bitcoinCashPrice);
		labels.put("XRP", ripplePrice);

		return labels;
	}

	private GridPane createGrid() {
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setAlignment(Pos.CENTER);
		return grid;
	}

	private void addLabelsToGrid(Map<String, Label> labels, GridPane grid) {
		int row = 0;
		for (Map.Entry<String, Label> entry : labels.entrySet()) {
			String cryptoName = entry.getKey();
			Label nameLabel = new Label(cryptoName);
			nameLabel.setTextFill(Color.BLUE);
			nameLabel.setOnMousePressed(event -> nameLabel.setTextFill(Color.RED));
			nameLabel.setOnMouseReleased(event -> nameLabel.setTextFill(Color.BLUE));

			grid.add(nameLabel, 0, row);
			grid.add(entry.getValue(), 1, row);

			row++;
		}
	}

	private Rectangle createBackgroundRectangleWithAnimation(double width,
															 double height) {
		Rectangle background = new Rectangle(width, height);
		FillTransition fillTransition = new FillTransition(Duration.millis(1000), background, Color.LIGHTGREEN, Color.LIGHTBLUE);
		fillTransition.setCycleCount(Timeline.INDEFINITE);
		fillTransition.setAutoReverse(true);
		fillTransition.play();
		return background;
	}

	public static class PricesContainer {
		private Lock lock = new ReentrantLock();

		private double bitcoinPrice;
		private double etherPrice;
		private double litecoinPrice;
		private double bitcoinCashPrice;
		private double ripplePrice;

		public Lock getLock() {
			return lock;
		}

		public double getBitcoinPrice() {
			return bitcoinPrice;
		}

		public void setBitcoinPrice(double bitcoinPrice) {
			this.bitcoinPrice = bitcoinPrice;
		}

		public double getEtherPrice() {
			return etherPrice;
		}

		public void setEtherPrice(double etherPrice) {
			this.etherPrice = etherPrice;
		}

		public double getLitecoinPrice() {
			return litecoinPrice;
		}

		public void setLitecoinPrice(double litecoinPrice) {
			this.litecoinPrice = litecoinPrice;
		}

		public double getBitcoinCashPrice() {
			return bitcoinCashPrice;
		}

		public void setBitcoinCashPrice(double bitcoinCashPrice) {
			this.bitcoinCashPrice = bitcoinCashPrice;
		}

		public double getRipplePrice() {
			return ripplePrice;
		}

		public void setRipplePrice(double ripplePrice) {
			this.ripplePrice = ripplePrice;
		}
	}

	public static class PriceUpdater extends Thread {
		private PricesContainer pricesContainer;
		private Random random = new Random();

		public PriceUpdater(PricesContainer pricesContainer) {
			this.pricesContainer = pricesContainer;
		}

		@Override
		public void run() {
			while (true) {
				pricesContainer.getLock().lock();

				try {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException ignored) {
					}
					pricesContainer.setBitcoinPrice(random.nextInt(20000));
					pricesContainer.setEtherPrice(random.nextInt(2000));
					pricesContainer.setLitecoinPrice(random.nextInt(500));
					pricesContainer.setBitcoinCashPrice(random.nextInt(5000));
					pricesContainer.setRipplePrice(random.nextDouble());
				} finally {
					pricesContainer.getLock().unlock();
				}

				try {
					Thread.sleep(2000);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
