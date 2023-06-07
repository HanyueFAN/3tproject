package com.t3project.algorithms;

import com.t3project.models.Tour;
import com.t3project.models.TouristAttraction;
import com.t3project.utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Optimization {

    public static Tour optimizeTour(Tour tour) {
        List<TouristAttraction> attractions = tour.getAttractions();
        List<TouristAttraction> optimizedAttractions = new ArrayList<>(attractions);

        Collections.shuffle(optimizedAttractions);

        double optimizedDistance = calculateTourDistance(optimizedAttractions);

        return new Tour(optimizedAttractions, optimizedDistance);
    }

    private static double calculateTourDistance(List<TouristAttraction> attractions) {
        double totalDistance = 0.0;
        for (int i = 0; i < attractions.size() - 1; i++) {
            TouristAttraction currentAttraction = attractions.get(i);
            TouristAttraction nextAttraction = attractions.get(i + 1);
            double distance = DistanceCalculator.calculateDistance(
                    currentAttraction.getLatitude(), currentAttraction.getLongitude(),
                    nextAttraction.getLatitude(), nextAttraction.getLongitude()
            );
            totalDistance += distance;
        }
        return totalDistance;
    }
}
