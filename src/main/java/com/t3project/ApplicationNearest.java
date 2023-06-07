package com.t3project;

import com.t3project.algorithms.TourGeneration;
import com.t3project.database.DatabaseConnector;
import com.t3project.models.TouristAttraction;
import com.t3project.utils.DistanceCalculator;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class ApplicationNearest {

    public static void main(String[] args) {
        // Connect to the database
        DatabaseConnector connector = new DatabaseConnector();
        Connection connection = connector.getConnection();
        if (connection != null) {
            System.out.println("Connected to the database.");

            // Retrieve tourist attractions from the database
            List<TouristAttraction> attractions = getTouristAttractionsFromDatabase(connection);

            // Generate a tour plan
            String location = "paris";
            String period = "modern";
            double distanceThreshold = 1.3; // Adjust the distance threshold as needed
            int timeConstraint = 28; // Adjust the time constraint as needed

            List<TouristAttraction> tourPlan = generateTourPlan(attractions, location, period, distanceThreshold, timeConstraint);

            if (!tourPlan.isEmpty()) {
                displayTourPlan(tourPlan, timeConstraint,distanceThreshold);
            } else {
                System.out.println("No tour options available.");
            }
        } else {
            System.out.println("Failed to connect to the database.");
        }
    }

    private static List<TouristAttraction> generateTourPlan(List<TouristAttraction> attractions, String location, String period, double distanceThreshold, int timeConstraint) {
        // Filter attractions based on location and period
        List<TouristAttraction> filteredAttractions = filterAttractions(attractions, location, period);

        // Calculate distances between attractions
        double[][] distanceMatrix = calculateDistanceMatrix(filteredAttractions);

        // Generate the tour plan using an approximation algorithm (Nearest Neighbor)
        List<TouristAttraction> tourPlan = TSPNearestNeighbor(distanceMatrix, distanceThreshold, timeConstraint, filteredAttractions);

        return tourPlan;
    }

    private static List<TouristAttraction> filterAttractions(List<TouristAttraction> attractions, String location, String period) {
        List<TouristAttraction> filteredAttractions = new ArrayList<>();
        for (TouristAttraction attraction : attractions) {
            if (attraction.getLocation().equalsIgnoreCase(location) && attraction.getPeriod().equalsIgnoreCase(period)) {
                filteredAttractions.add(attraction);
            }
        }
        return filteredAttractions;
    }

    private static double[][] calculateDistanceMatrix(List<TouristAttraction> attractions) {
        int numAttractions = attractions.size();
        double[][] distanceMatrix = new double[numAttractions][numAttractions];

        for (int i = 0; i < numAttractions; i++) {
            for (int j = i + 1; j < numAttractions; j++) {
                TouristAttraction attraction1 = attractions.get(i);
                TouristAttraction attraction2 = attractions.get(j);
                double distance = DistanceCalculator.calculateDistance(
                        attraction1.getLatitude(), attraction1.getLongitude(),
                        attraction2.getLatitude(), attraction2.getLongitude()
                );
                distanceMatrix[i][j] = distance;
                distanceMatrix[j][i] = distance; // Symmetric distance matrix
            }
        }

        return distanceMatrix;
    }

    private static List<TouristAttraction> TSPNearestNeighbor(double[][] distanceMatrix, double distanceThreshold, int timeConstraint, List<TouristAttraction> attractions) {
        int numAttractions = distanceMatrix.length;

        // Create a set to track visited attractions
        Set<Integer> visited = new HashSet<>();

        // Start from the first attraction
        int currentAttraction = 0;
        visited.add(currentAttraction);

        // Initialize variables
        double totalDistance = 0;
        int totalTime = 0;

        // Create a list to store the tour plan
        List<TouristAttraction> tourPlan = new ArrayList<>();
        tourPlan.add(attractions.get(currentAttraction));

        while (visited.size() < numAttractions && totalTime <= timeConstraint) {
            int nearestAttraction = -1;
            double nearestDistance = Double.MAX_VALUE;

            // Find the nearest unvisited attraction
            for (int i = 0; i < numAttractions; i++) {
                if (!visited.contains(i) && distanceMatrix[currentAttraction][i] <= distanceThreshold && distanceMatrix[currentAttraction][i] < nearestDistance) {
                    nearestAttraction = i;
                    nearestDistance = distanceMatrix[currentAttraction][i];
                }
            }

            if (nearestAttraction == -1) {
                // No unvisited attractions within the distance threshold, break the loop
                break;
            }

            // Add the nearest attraction to the tour plan
            tourPlan.add(attractions.get(nearestAttraction));

            // Update variables
            totalDistance += nearestDistance;
            totalTime += attractions.get(nearestAttraction).getVisitDuration();

            // Move to the nearest attraction
            currentAttraction = nearestAttraction;
            visited.add(currentAttraction);
        }

        return tourPlan;
    }

//

    private static void displayTourPlan(List<TouristAttraction> tourPlan, int timeConstraint, double distanceThreshold) {
        int totalDays = (int) Math.ceil((double) timeConstraint / 8);

        int attractionIndex = 0;
        double totalDuration = 0.0;
        List<TouristAttraction> dailyAttractions = new ArrayList<>();

        for (int day = 0; day < totalDays; day++) {
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
                            System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                        } else {
                            System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)"
                                    + " - Distance from previous attraction: " + String.format("%.2f", distance) + " km");
                        }

                        dailyAttractions.add(attraction);
                        totalDuration += attraction.getVisitDuration();

                        if (distance > distanceThreshold) {
                            tourPlan.remove(attractionIndex + 1);
                        }
                    } else if (attractionIndex >= tourPlan.size() - 1) {
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




    //    private static void displayTourPlan(List<TouristAttraction> tourPlan, int timeConstraint, double distanceThreshold) {
//    int totalDays = (int) Math.ceil((double) timeConstraint / 8);
//
//    int attractionIndex = 0;
//    double totalDuration = 0.0;
//    List<TouristAttraction> dailyAttractions = new ArrayList<>();
//
//    for (int day = 0; day < totalDays; day++) {
//        System.out.println("Day " + (day + 1) + ":");
//
//        while (attractionIndex < tourPlan.size()) {
//            TouristAttraction attraction = tourPlan.get(attractionIndex);
//
//            if (totalDuration + attraction.getVisitDuration() <= 8.0) {
//                if (attractionIndex < tourPlan.size() - 1) {
//                    TouristAttraction nextAttraction = tourPlan.get(attractionIndex + 1);
//                    double distance = DistanceCalculator.calculateDistance(
//                            attraction.getLatitude(), attraction.getLongitude(),
//                            nextAttraction.getLatitude(), nextAttraction.getLongitude()
//                    );
//
//                    if (distance <= distanceThreshold) {
//                        System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
//                        dailyAttractions.add(attraction);
//                        totalDuration += attraction.getVisitDuration();
////                        if (totalDuration + attraction.getVisitDuration() <= 8.0) {
////                            System.out.println("Distance from next attraction: " + String.format("%.2f", distance) + " km");
////                            }else if(totalDuration + attraction.getVisitDuration()>8.0){
////                                attractionIndex--;
////                        }
//                    } else if(distance>distanceThreshold){
//                        break; // Distance threshold exceeded, move to next day
//                    }
//                } else if(attractionIndex >= tourPlan.size() - 1){
//                    System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
//                    dailyAttractions.add(attraction);
//                    totalDuration += attraction.getVisitDuration();
//                }
//            } else {
//                break; // Maximum duration per day exceeded, move to next day
//            }
//
//            attractionIndex++;
//        }
//
//        System.out.println();
//        totalDuration = 0.0;
//        dailyAttractions.clear();
//    }
//}
    private static List<TouristAttraction> getTouristAttractionsFromDatabase(Connection connection) {
        List<TouristAttraction> attractions = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM tourist_attractions");
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String location = resultSet.getString("location");
                String period = resultSet.getString("period");
                double latitude = resultSet.getDouble("latitude");
                double longitude = resultSet.getDouble("longitude");
                String description = resultSet.getString("description");
                int visitDuration = resultSet.getInt("visitDuration");
                String type = resultSet.getString("type");

                attractions.add(new TouristAttraction(id, name, description, location, period, type, latitude, longitude, visitDuration));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attractions;
    }
}
