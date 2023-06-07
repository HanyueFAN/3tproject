package com.t3project.models;

import com.t3project.models.TouristAttraction;

import java.util.List;

public class Tour {
    private List<TouristAttraction> attractions;
    private double totalDistance;

    public Tour(List<TouristAttraction> attractions, double totalDistance) {
        this.attractions = attractions;
        this.totalDistance = totalDistance;
    }

    public List<TouristAttraction> getAttractions() {
        return attractions;
    }

    public double getTotalDistance() {
        return totalDistance;
    }
}
