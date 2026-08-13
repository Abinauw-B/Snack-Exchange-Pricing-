package com.retailpos.pricing;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingSimulationService {

    @Data
    public static class SimulationRequest {
        private String flavourName = "Mango Juice";
        private Integer initialVolumeMl = 20000;
        private BigDecimal initialPrice = new BigDecimal("20.00");
        private BigDecimal minPrice = new BigDecimal("18.00");
        private BigDecimal maxPrice = new BigDecimal("25.00");
        private Integer totalSimulatedPurchases = 40;
        private Integer cupsPerInterval = 4;
        private Integer intervalMinutes = 5;
        private String startTimeStr = "12:00"; // HH:mm format
        private Double weightVelocity = 0.40;
        private Double weightStockPressure = 0.40;
        private Double weightTimeFactor = 0.20;
    }

    @Data
    @Builder
    public static class SimulationStep {
        private int stepIndex;
        private String timeStr;
        private int remainingVolumeMl;
        private int estimatedRemainingCups;
        private int cupsSoldThisStep;
        private int cumulativeCupsSold;
        private double velocityScore;
        private double stockPressurePct;
        private double timeFactorMultiplier;
        private double demandScore;
        private BigDecimal price;
        private String priceMovement; // "+₹1", "-₹1", "UNCHANGED"
        private String explanation;
    }

    @Data
    @Builder
    public static class SimulationResponse {
        private String flavourName;
        private int initialVolumeMl;
        private int finalVolumeMl;
        private BigDecimal initialPrice;
        private BigDecimal finalPrice;
        private int totalCupsSold;
        private List<SimulationStep> steps;
    }

    public SimulationResponse runSimulation(SimulationRequest request) {
        int volume = (request.getInitialVolumeMl() != null) ? request.getInitialVolumeMl() : 20000;
        BigDecimal currentPrice = (request.getInitialPrice() != null) ? request.getInitialPrice() : new BigDecimal("20.00");
        BigDecimal minPrice = (request.getMinPrice() != null) ? request.getMinPrice() : new BigDecimal("18.00");
        BigDecimal maxPrice = (request.getMaxPrice() != null) ? request.getMaxPrice() : new BigDecimal("25.00");

        int cupsPerStep = (request.getCupsPerInterval() != null) ? request.getCupsPerInterval() : 4;
        int intervalMins = (request.getIntervalMinutes() != null) ? request.getIntervalMinutes() : 5;
        int totalPurchases = (request.getTotalSimulatedPurchases() != null) ? request.getTotalSimulatedPurchases() : 40;
        int maxSteps = Math.min(30, (int) Math.ceil((double) totalPurchases / cupsPerStep));

        LocalTime currentTime = LocalTime.parse(request.getStartTimeStr() != null ? request.getStartTimeStr() : "12:00");
        int cumulativeCups = 0;
        List<SimulationStep> steps = new ArrayList<>();

        double wVel = request.getWeightVelocity() != null ? request.getWeightVelocity() : 0.40;
        double wStock = request.getWeightStockPressure() != null ? request.getWeightStockPressure() : 0.40;
        double wTime = request.getWeightTimeFactor() != null ? request.getWeightTimeFactor() : 0.20;

        for (int i = 1; i <= maxSteps && volume > 0; i++) {
            int cupsToDeduct = Math.min(cupsPerStep, volume / 250);
            int mlDeducted = cupsToDeduct * 250;
            volume -= mlDeducted;
            cumulativeCups += cupsToDeduct;

            double velocityScore = Math.min(100.0, (cupsToDeduct / 5.0) * 100.0);
            double stockPressurePct = 100.0 - (((double) volume / request.getInitialVolumeMl()) * 100.0);
            stockPressurePct = Math.max(0.0, Math.min(100.0, stockPressurePct));

            int hour = currentTime.getHour();
            double timeMult = (hour >= 16 && hour < 21) ? 1.2 : (hour >= 11 && hour < 16) ? 1.1 : 1.0;
            double timeScore = (timeMult - 1.0) * 250.0 + 50.0;

            double demandScore = (wVel * velocityScore) + (wStock * stockPressurePct) + (wTime * timeScore);
            demandScore = Math.max(0.0, Math.min(100.0, demandScore));

            BigDecimal oldPrice = currentPrice;
            String movement = "UNCHANGED";
            String explanation;

            // Every 2 steps (~10 mins cooldown simulation) evaluate price step
            if (i % 2 == 0) {
                if (demandScore >= 65.0 && currentPrice.compareTo(maxPrice) < 0) {
                    currentPrice = currentPrice.add(BigDecimal.ONE);
                    movement = "+₹1";
                    explanation = String.format("Step %d: Demand Score %.1f triggers +₹1 price increase to ₹%s.", i, demandScore, currentPrice);
                } else if (demandScore <= 35.0 && currentPrice.compareTo(minPrice) > 0) {
                    currentPrice = currentPrice.subtract(BigDecimal.ONE);
                    movement = "-₹1";
                    explanation = String.format("Step %d: Low demand score %.1f triggers -₹1 price drop to ₹%s.", i, demandScore, currentPrice);
                } else {
                    explanation = String.format("Step %d: Demand Score %.1f. Price remains stable at ₹%s.", i, demandScore, currentPrice);
                }
            } else {
                explanation = String.format("Step %d: Cooldown window active. Price held at ₹%s.", i, currentPrice);
            }

            steps.add(SimulationStep.builder()
                    .stepIndex(i)
                    .timeStr(currentTime.toString())
                    .remainingVolumeMl(volume)
                    .estimatedRemainingCups(volume / 250)
                    .cupsSoldThisStep(cupsToDeduct)
                    .cumulativeCupsSold(cumulativeCups)
                    .velocityScore(velocityScore)
                    .stockPressurePct(stockPressurePct)
                    .timeFactorMultiplier(timeMult)
                    .demandScore(demandScore)
                    .price(currentPrice)
                    .priceMovement(movement)
                    .explanation(explanation)
                    .build());

            currentTime = currentTime.plusMinutes(intervalMins);
        }

        return SimulationResponse.builder()
                .flavourName(request.getFlavourName())
                .initialVolumeMl(request.getInitialVolumeMl())
                .finalVolumeMl(volume)
                .initialPrice(request.getInitialPrice())
                .finalPrice(currentPrice)
                .totalCupsSold(cumulativeCups)
                .steps(steps)
                .build();
    }
}
