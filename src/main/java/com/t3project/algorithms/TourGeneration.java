package com.t3project.algorithms;

import com.t3project.models.TouristAttraction;
import com.t3project.utils.DistanceCalculator;

import java.util.*;

public class TourGeneration {

    public static List<TouristAttraction> generateTourPlan(List<TouristAttraction> attractions, String location, String period, double distanceThreshold, int timeConstraint) {
        // Filter attractions based on location and period
        List<TouristAttraction> filteredAttractions = filterAttractions(attractions, location, period);

        // Calculate distances between attractions
        double[][] distanceMatrix = calculateDistanceMatrix(filteredAttractions);

        // Generate the optimal tour plan using TSP algorithm
        List<TouristAttraction> tourPlan = TSPAlgorithm(distanceMatrix, timeConstraint,filteredAttractions);
        List<TouristAttraction> tourPlan1 = TSPNearestNeighbor(distanceMatrix, distanceThreshold,timeConstraint,filteredAttractions);
        Collections.shuffle(tourPlan1);

        return tourPlan1;
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


    private static List<TouristAttraction> TSPAlgorithm(double[][] distanceMatrix, int timeConstraint, List<TouristAttraction> attractions) {
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

        return tourPlan;
    }

    private static List<TouristAttraction> TSPNearestNeighbor(double[][] distanceMatrix, double distanceThreshold, int timeConstraint, List<TouristAttraction> attractions) {
        int numAttractions = distanceMatrix.length;

        // Create a set to track visited attractions
        Set<Integer> visited = new HashSet<>();

        // Start from the first attraction
        int currentAttraction = 0;
        visited.add(currentAttraction);
//        Random random = new Random();
//        int currentAttraction = random.nextInt(numAttractions);
//        visited.add(currentAttraction);

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

    public static TouristAttraction selectNextAttraction(List<TouristAttraction> attractions, List<TouristAttraction> selectedAttractions, double distanceThreshold, double remainingTime) {
        TouristAttraction selectedAttraction = null;
        double minDistance = Double.MAX_VALUE;

        for (TouristAttraction attraction : attractions) {
            if (attraction.getVisitDuration() <= remainingTime) {
                double distance = calculateDistanceToLastAttraction(attraction, selectedAttractions);
                if (distance <= distanceThreshold && distance < minDistance) {
                    minDistance = distance;
                    selectedAttraction = attraction;
                }
            }
        }

        return selectedAttraction;
    }

    private static double calculateDistanceToLastAttraction(TouristAttraction attraction, List<TouristAttraction> selectedAttractions) {
        if (selectedAttractions.isEmpty()) {
            return 0.0;
        }

        TouristAttraction lastAttraction = selectedAttractions.get(selectedAttractions.size() - 1);
        return DistanceCalculator.calculateDistance(lastAttraction.getLatitude(), lastAttraction.getLongitude(),
                attraction.getLatitude(), attraction.getLongitude());
    }
}
