package com.retailpos.pricing;

import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class TimeFactorService {

    public enum TimeWindow {
        MORNING, AFTERNOON, EVENING, NIGHT
    }

    public double getTimeFactorMultiplier(LocalTime time) {
        if (time == null) {
            time = LocalTime.now();
        }
        int hour = time.getHour();
        if (hour >= 6 && hour < 11) {
            return 1.0; // Morning
        } else if (hour >= 11 && hour < 16) {
            return 1.1; // Afternoon peak
        } else if (hour >= 16 && hour < 21) {
            return 1.2; // Evening high demand
        } else {
            return 1.0; // Night / Late hours
        }
    }

    public TimeWindow getTimeWindow(LocalTime time) {
        if (time == null) {
            time = LocalTime.now();
        }
        int hour = time.getHour();
        if (hour >= 6 && hour < 11) {
            return TimeWindow.MORNING;
        } else if (hour >= 11 && hour < 16) {
            return TimeWindow.AFTERNOON;
        } else if (hour >= 16 && hour < 21) {
            return TimeWindow.EVENING;
        } else {
            return TimeWindow.NIGHT;
        }
    }

    public double getTimeFactorScore(LocalTime time) {
        double multiplier = getTimeFactorMultiplier(time);
        // Normalize 1.0 -> 50, 1.1 -> 75, 1.2 -> 100
        return (multiplier - 1.0) * 250.0 + 50.0;
    }
}
