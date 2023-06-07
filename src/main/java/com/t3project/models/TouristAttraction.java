package com.t3project.models;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TouristAttraction {
    private int id;
    private String name;
    private String description;
    private String location;
    private String period;
    private String type;
    private double latitude;
    private double longitude;
    private double distanceFromPrevious; // New field for distance from the previous attraction
    private int visitDuration; // New field for the duration of the visit

    public TouristAttraction(int id, String name, String description, String location, String period, String type, double latitude, double longitude, int visitDuration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.location = location;
        this.period = period;
        this.type = type;
        this.latitude = latitude;
        this.longitude = longitude;
        this.visitDuration = visitDuration;
    }

    public static List<TouristAttraction> parseResultSet(ResultSet resultSet) throws SQLException {
        List<TouristAttraction> attractions = new ArrayList<>();

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

        return attractions;
    }

    // Getters and Setters

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getDistanceFromPrevious() {
        return distanceFromPrevious;
    }

    public void setDistanceFromPrevious(double distanceFromPrevious) {
        this.distanceFromPrevious = distanceFromPrevious;

    }

    public int getVisitDuration() {
        return visitDuration;
    }

    public void setVisitDuration(int visitDuration) {
        this.visitDuration = visitDuration;
    }
}
