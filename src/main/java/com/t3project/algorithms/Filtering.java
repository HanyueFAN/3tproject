package com.t3project.algorithms;

import com.t3project.models.TouristAttraction;

import java.util.List;
import java.util.stream.Collectors;

public class Filtering {

    public static List<TouristAttraction> filterByLocation(List<TouristAttraction> attractions, String location) {
        if (location == null || location.isEmpty()) {
            return attractions;
        }
        return attractions.stream()
                .filter(attraction -> attraction.getLocation().equalsIgnoreCase(location))
                .collect(Collectors.toList());
    }

    public static List<TouristAttraction> filterByPeriod(List<TouristAttraction> attractions, String period) {
        if (period == null || period.isEmpty()) {
            return attractions;
        }
        return attractions.stream()
                .filter(attraction -> attraction.getPeriod().equalsIgnoreCase(period))
                .collect(Collectors.toList());
    }

    public static List<TouristAttraction> filterByType(List<TouristAttraction> attractions, String type) {
        if (type == null || type.isEmpty()) {
            return attractions;
        }
        return attractions.stream()
                .filter(attraction -> attraction.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Add additional filtering methods as needed

}
