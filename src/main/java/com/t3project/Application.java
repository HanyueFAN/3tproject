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

public class Application {

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
            double distanceThreshold = 1; // Adjust the distance threshold as needed
            int timeConstraint = 28; // Adjust the time constraint as needed
//            List<TouristAttraction> tourPlan = TSPAlgorithm(distanceMatrix, timeConstraint, filteredAttractions);
//
//            return tourPlan;
            List<TouristAttraction> tourPlan = generateTourPlan(attractions, location, period, distanceThreshold, timeConstraint);


            if (!tourPlan.isEmpty()) {
//                displayTourPlan(tourPlan, timeConstraint,distanceThreshold);
                displayTourPlan(tourPlan, timeConstraint);

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

        // Generate the optimal tour plan using TSP algorithm
        List<TouristAttraction> tourPlan = TSPAlgorithm(distanceMatrix, distanceThreshold, timeConstraint, filteredAttractions);

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
            for (int j = 0; j < numAttractions; j++) {
                TouristAttraction attraction1 = attractions.get(i);
                TouristAttraction attraction2 = attractions.get(j);
                double distance = DistanceCalculator.calculateDistance(
                        attraction1.getLatitude(), attraction1.getLongitude(),
                        attraction2.getLatitude(), attraction2.getLongitude()
                );
                distanceMatrix[i][j] = distance;
            }
        }

        return distanceMatrix;
    }

    private static List<TouristAttraction> TSPAlgorithm(double[][] distanceMatrix, double distanceThreshold, int timeConstraint, List<TouristAttraction> attractions) {
        int numAttractions = distanceMatrix.length;

        // Create a dynamic programming table
        int[][] dp = new int[1 << numAttractions][numAttractions];

        // Initialize the table with maximum values
        for (int[] row : dp) {
            Arrays.fill(row, Integer.MAX_VALUE / 2);
        }

        // Initialize the base case
        dp[1][0] = 0;

        // Perform the dynamic programming computation
        for (int mask = 1; mask < (1 << numAttractions); mask++) {
            for (int last = 0; last < numAttractions; last++) {
                if ((mask & (1 << last)) != 0) {
                    for (int curr = 0; curr < numAttractions; curr++) {
                        if ((mask & (1 << curr)) != 0 && curr != last) {
                            int prevMask = mask ^ (1 << last);
                            dp[mask][curr] = Math.min(dp[mask][curr], dp[prevMask][last] + (int) distanceMatrix[last][curr]);
                        }
                    }
                }
            }
        }

        // Find the optimal tour by backtracking from the last attraction
        int lastAttraction = 0; // Starting point
        int mask = (1 << numAttractions) - 1;
        int minTourCost = Integer.MAX_VALUE;

        for (int curr = 0; curr < numAttractions; curr++) {
            if (curr != lastAttraction) {
                int tourCost = dp[mask][curr] + (int) distanceMatrix[curr][lastAttraction];
                if (tourCost < minTourCost) {
                    minTourCost = tourCost;
                    lastAttraction = curr;
                }
            }
        }

        // Reconstruct the optimal tour
        List<Integer> tourPath = new ArrayList<>();
        tourPath.add(lastAttraction);

        while (mask != 0) {
            int prevAttraction = -1;

            for (int prev = 0; prev < numAttractions; prev++) {
                if (prev != lastAttraction && (mask & (1 << prev)) != 0) {
                    if (prevAttraction == -1 || dp[mask][lastAttraction] == dp[mask ^ (1 << lastAttraction)][prev] + (int) distanceMatrix[prev][lastAttraction]) {
                        prevAttraction = prev;
                    }
                }
            }

            tourPath.add(prevAttraction);
            mask ^= (1 << lastAttraction);
            lastAttraction = prevAttraction;
        }

        // Reverse the tour path to start from the beginning
        Collections.reverse(tourPath);

//        // Construct the tour plan based on the tour path
        List<TouristAttraction> tourPlan = new ArrayList<>();

        for (int attractionIndex : tourPath) {
            if (attractionIndex >= 0 && attractionIndex < attractions.size()) {
                tourPlan.add(attractions.get(attractionIndex));
            }
        }

        //check distance
        Iterator<TouristAttraction> iterator = tourPlan.iterator();
        TouristAttraction prevAttraction = null;

        while (iterator.hasNext()) {
            TouristAttraction attraction = iterator.next();

            if (prevAttraction != null) {
                double distance = DistanceCalculator.calculateDistance(
                        prevAttraction.getLatitude(), prevAttraction.getLongitude(),
                        attraction.getLatitude(), attraction.getLongitude()
                );

                if (distance > distanceThreshold) {
                    iterator.remove();
                }
            }

            prevAttraction = attraction;
        }

        return tourPlan;
    }


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

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return attractions;
    }

    private static void displayTourPlan(List<TouristAttraction> tourPlan, int timeConstraint) {
        int totalDays = (int) Math.ceil((double) timeConstraint / 8);

        int attractionIndex = 0;

        for (int day = 0; day < totalDays; day++) {
            double totalDuration = 0.0;

            System.out.println("Day " + (day + 1) + ":");

            while (totalDuration < 8.0 && attractionIndex < tourPlan.size()) {
                TouristAttraction attraction = tourPlan.get(attractionIndex);

                if (totalDuration + attraction.getVisitDuration() <= 8.0) {
                    System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
                    if (attractionIndex < tourPlan.size() - 1) {
                        TouristAttraction nextAttraction = tourPlan.get(attractionIndex + 1);
                        double distance = DistanceCalculator.calculateDistance(
                                attraction.getLatitude(), attraction.getLongitude(),
                                nextAttraction.getLatitude(), nextAttraction.getLongitude()
                        );

                        System.out.println("Distance from previous attraction: " + String.format("%.2f", distance) + " km");

                    }
                    totalDuration += attraction.getVisitDuration();
                } else {
                    break;
                }

                attractionIndex++;
            }

            System.out.println();
        }
    }
}

//    private static void displayTourPlan(List<TouristAttraction> tourPlan, int timeConstraint, double distanceThreshold) {
//        int totalDays = (int) Math.ceil((double) timeConstraint / 8);
//
//        int attractionIndex = 0;
//        double totalDuration = 0.0;
//        List<TouristAttraction> dailyAttractions = new ArrayList<>();
//
//        for (int day = 0; day < totalDays; day++) {
//            System.out.println("Day " + (day + 1) + ":");
//
//            while (attractionIndex < tourPlan.size()) {
//                TouristAttraction attraction = tourPlan.get(attractionIndex);
//
//                if (totalDuration + attraction.getVisitDuration() <= 8.0) {
//                    if (attractionIndex < tourPlan.size() - 1) {
//                        TouristAttraction nextAttraction = tourPlan.get(attractionIndex + 1);
//                        double distance = DistanceCalculator.calculateDistance(
//                                attraction.getLatitude(), attraction.getLongitude(),
//                                nextAttraction.getLatitude(), nextAttraction.getLongitude()
//                        );
//
//                        if (distance <= distanceThreshold) {
//                            System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
//                            System.out.println("Distance from previous attraction: " + String.format("%.2f", distance) + " km");
//                            dailyAttractions.add(attraction);
//                            totalDuration += attraction.getVisitDuration();
//                        } else {
//                            break; // Distance threshold exceeded, move to next day
//                        }
//                    } else {
//                        System.out.println(attraction.getName() + " (" + attraction.getVisitDuration() + " hours)");
//                        dailyAttractions.add(attraction);
//                        totalDuration += attraction.getVisitDuration();
//                    }
//                } else {
//                    break; // Maximum duration per day exceeded, move to next day
//                }
//
//                attractionIndex++;
//            }
//
//            System.out.println();
//            totalDuration = 0.0;
//            dailyAttractions.clear();
//        }
//    }
//}

