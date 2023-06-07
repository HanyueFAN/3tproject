package com.t3project.gui;

import com.t3project.algorithms.Filtering;
import com.t3project.algorithms.TourGeneration;
import com.t3project.database.DatabaseConnector;
import com.t3project.models.TouristAttraction;
import com.t3project.utils.DistanceCalculator;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.NumberStringConverter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.t3project.algorithms.TourGeneration.selectNextAttraction;

public class TourGenerationGUI extends Application {

    private List<TouristAttraction> attractions;

    private Label distanceResultLabel;
    private Label tourResultLabel;
    private ListView<String> dailyAttractionsListView;
    private static final Logger LOGGER = Logger.getLogger(TourGenerationGUI.class.getName());

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tour Generation");

        // Connect to the database
        DatabaseConnector connector = new DatabaseConnector();
        Connection connection = connector.getConnection();

        if (connection != null) {
            LOGGER.log(Level.INFO, "Connected to the database.");

            // Retrieve tourist attractions from the database
            attractions = getTouristAttractionsFromDatabase(connection);

            // Create UI components
            Label locationLabel = new Label("Location:");
            ComboBox<String> locationComboBox = createLocationComboBox();

            Label periodLabel = new Label("Period:");
            ComboBox<String> periodComboBox = createPeriodComboBox();

            Label distanceLabel = new Label("Distance (km):");
            Slider distanceSlider = createDistanceSlider();

            Label timeLabel = new Label("Hours:");
            Slider timeSlider = createTimeSlider();

            Label distanceValueLabel = new Label();
            distanceValueLabel.getStyleClass().add("value-label");
            distanceValueLabel.textProperty().bindBidirectional(distanceSlider.valueProperty(), new NumberStringConverter());

            Label timeValueLabel = new Label();
            timeValueLabel.getStyleClass().add("value-label");
            timeValueLabel.textProperty().bindBidirectional(timeSlider.valueProperty(), new NumberStringConverter());


//            Label timeInputLabel = new Label("Enter Number of Days:");
//            timeInput = new TextField();
//            timeInput.getStyleClass().add("text-field");

            // Create additional labels for displaying the results
            distanceResultLabel = new Label();
            distanceResultLabel.getStyleClass().add("result-label");

            tourResultLabel = new Label();
            tourResultLabel.getStyleClass().add("result-label");

            dailyAttractionsListView = new ListView<>();
            dailyAttractionsListView.getStyleClass().add("list-view");

            Button generateButton = new Button("Generate Tour");
            generateButton.setOnAction(event -> {
                String selectedLocation = locationComboBox.getValue();
                String selectedPeriod = periodComboBox.getValue();
                double selectedDistance = distanceSlider.getValue();
                int selectedTimeConstraint = (int)timeSlider.getValue();
                List<TouristAttraction> tourPlan = generateTourPlan(attractions, selectedLocation, selectedPeriod, selectedDistance, selectedTimeConstraint);
                displayTourPlan(tourPlan, selectedTimeConstraint,selectedDistance);
//                displayTourPlan(tourPlan, selectedTimeConstraint);
            });


            VBox root = new VBox(10);
            root.setPadding(new Insets(10));
            root.getStyleClass().add("root");
            root.getChildren().addAll(locationLabel, locationComboBox, periodLabel, periodComboBox,
                    distanceLabel, distanceSlider,distanceValueLabel,
                    timeLabel,timeSlider, timeValueLabel,
                    generateButton,
                    distanceResultLabel, tourResultLabel, dailyAttractionsListView);

            Scene scene = new Scene(root, 500, 600);
            String cssFile = getClass().getResource("/styles.css").toExternalForm();
            scene.getStylesheets().add(cssFile);
            primaryStage.setScene(scene);
            primaryStage.show();
        } else {
            LOGGER.log(Level.SEVERE, "Failed to connect to the database.");
        }
    }

    private ComboBox<String> createLocationComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("combo-box");
        comboBox.getItems().addAll( "Paris", "Normandy","France"); // Modify with your actual locations
        comboBox.setValue("All");
        return comboBox;
    }

    private ComboBox<String> createPeriodComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.getStyleClass().add("combo-box");
        comboBox.getItems().addAll( "Modern", "Renaissance", "Medieval"); // Modify with your actual periods
        comboBox.setValue("All");
        return comboBox;
    }

    private Slider createDistanceSlider() {
        Slider slider = new Slider(0, 10, 1); // Modify the min, max, and default values as needed
        slider.getStyleClass().add("slider");
        slider.setMajorTickUnit(1);
        slider.setMinorTickCount(1);
//        slider.setShowTickLabels(true);
//        slider.setShowTickMarks(true);
        return slider;
    }


    private Slider createTimeSlider() {
        Slider slider = new Slider(0, 48, 8); // Modify the min, max, and default values as needed
        slider.getStyleClass().add("slider");
        slider.setMajorTickUnit(10);
        slider.setMinorTickCount(1);
//        slider.setShowTickLabels(true);
//        slider.setShowTickMarks(true);
        slider.setBlockIncrement(1); // Set the increment to 1
        slider.setSnapToTicks(true); // Snap to integer values
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            slider.setValue(newValue.intValue()); // Set the value as an integer
        });
        return slider;
    }

    private List<TouristAttraction> getTouristAttractionsFromDatabase(Connection connection) {
        List<TouristAttraction> attractions = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tourist_attractions");

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String description = resultSet.getString("description");
                String location = resultSet.getString("location");
                String period = resultSet.getString("period");
                String type = resultSet.getString("type");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                int visitDuration = resultSet.getInt("visitDuration");

                TouristAttraction attraction = new TouristAttraction(id, name, description, location, period, type, latitude, longitude, visitDuration);

                attractions.add(attraction);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error while retrieving tourist attractions from the database.", e);
        }

        return attractions;
    }

    private List<TouristAttraction> generateTourPlan(List<TouristAttraction> attractions, String location, String period, double distanceThreshold, int timeConstraint) {
        List<TouristAttraction> tourPlan = TourGeneration.generateTourPlan(attractions, location, period, distanceThreshold, timeConstraint);
        return tourPlan;
    }


    private  void displayTourPlan(List<TouristAttraction> tourPlan, int timeConstraint, double distanceThreshold) {
        int totalDays = (int) Math.ceil((double) timeConstraint / 8);
        dailyAttractionsListView.getItems().clear();

        int attractionIndex = 0;
        double totalDuration = 0.0;
        List<TouristAttraction> dailyAttractions = new ArrayList<>();

        for (int day = 0; day < totalDays; day++) {
            dailyAttractionsListView.getItems().add("Day " + (day + 1) + ":");
            System.out.println("Day " + (day + 1) + ":");

            while (attractionIndex < tourPlan.size()) {
                TouristAttraction attraction = tourPlan.get(attractionIndex);

                if (totalDuration + attraction.getVisitDuration() <= 8.0) {
                    if (attractionIndex < tourPlan.size() - 1) {
                        TouristAttraction nextAttraction = tourPlan.get(attractionIndex + 1);
                        double distance = DistanceCalculator.calculateDistance(
                                attraction.getLatitude(), attraction.getLongitude(),
                                nextAttraction.getLatitude(), nextAttraction.getLongitude()
                        );

                        if (attractionIndex == 0) {
                            dailyAttractionsListView.getItems().add(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                            System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                        } else {
                            dailyAttractionsListView.getItems().add(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)"
                                    + " - Distance from previous attraction: " + String.format("%.2f", distance) + " km");
                            System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)"
                                    + " - Distance from previous attraction: " + String.format("%.2f", distance) + " km");
                        }

                        dailyAttractions.add(attraction);
                        totalDuration += attraction.getVisitDuration();

                        if (distance > distanceThreshold) {
                            tourPlan.remove(attractionIndex + 1);
                        }
                    } else if (attractionIndex >= tourPlan.size() - 1) {
                        dailyAttractionsListView.getItems().add(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                        System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                        dailyAttractions.add(attraction);
                        totalDuration += attraction.getVisitDuration();
                    }
                } else {
                    break; // Maximum duration per day exceeded, move to next day
                }

                attractionIndex++;
            }

            System.out.println();
            totalDuration = 0.0;
            dailyAttractions.clear();
        }
    }
}