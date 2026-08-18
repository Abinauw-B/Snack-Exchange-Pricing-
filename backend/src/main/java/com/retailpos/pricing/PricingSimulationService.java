package com.retailpos.pricing;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class PricingSimulationService {

    public static class SimulationRequest {
        private String flavourName = "Mango Juice";
        private Integer initialVolumeMl = 20000;
        private BigDecimal initialPrice = new BigDecimal("20.00");
        private BigDecimal minPrice = new BigDecimal("18.00");
        private BigDecimal maxPrice = new BigDecimal("25.00");
        private Integer totalSimulatedPurchases = 40;
        private Integer cupsPerInterval = 4;
        private Integer intervalMinutes = 5;
        private String startTimeStr = "12:00";
        private Double weightVelocity = 0.40;
        private Double weightStockPressure = 0.40;
        private Double weightTimeFactor = 0.20;

        public SimulationRequest() {}

        public String getFlavourName() { return flavourName; }
        public void setFlavourName(String flavourName) { this.flavourName = flavourName; }
        public Integer getInitialVolumeMl() { return initialVolumeMl; }
        public void setInitialVolumeMl(Integer initialVolumeMl) { this.initialVolumeMl = initialVolumeMl; }
        public BigDecimal getInitialPrice() { return initialPrice; }
        public void setInitialPrice(BigDecimal initialPrice) { this.initialPrice = initialPrice; }
        public BigDecimal getMinPrice() { return minPrice; }
        public void setMinPrice(BigDecimal minPrice) { this.minPrice = minPrice; }
        public BigDecimal getMaxPrice() { return maxPrice; }
        public void setMaxPrice(BigDecimal maxPrice) { this.maxPrice = maxPrice; }
        public Integer getTotalSimulatedPurchases() { return totalSimulatedPurchases; }
        public void setTotalSimulatedPurchases(Integer totalSimulatedPurchases) { this.totalSimulatedPurchases = totalSimulatedPurchases; }
        public Integer getCupsPerInterval() { return cupsPerInterval; }
        public void setCupsPerInterval(Integer cupsPerInterval) { this.cupsPerInterval = cupsPerInterval; }
        public Integer getIntervalMinutes() { return intervalMinutes; }
        public void setIntervalMinutes(Integer intervalMinutes) { this.intervalMinutes = intervalMinutes; }
        public String getStartTimeStr() { return startTimeStr; }
        public void setStartTimeStr(String startTimeStr) { this.startTimeStr = startTimeStr; }
        public Double getWeightVelocity() { return weightVelocity; }
        public void setWeightVelocity(Double weightVelocity) { this.weightVelocity = weightVelocity; }
        public Double getWeightStockPressure() { return weightStockPressure; }
        public void setWeightStockPressure(Double weightStockPressure) { this.weightStockPressure = weightStockPressure; }
        public Double getWeightTimeFactor() { return weightTimeFactor; }
        public void setWeightTimeFactor(Double weightTimeFactor) { this.weightTimeFactor = weightTimeFactor; }
    }

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
        private String priceMovement;
        private String explanation;

        public SimulationStep() {}
        public SimulationStep(int stepIndex, String timeStr, int remainingVolumeMl, int estimatedRemainingCups, int cupsSoldThisStep, int cumulativeCupsSold, double velocityScore, double stockPressurePct, double timeFactorMultiplier, double demandScore, BigDecimal price, String priceMovement, String explanation) {
            this.stepIndex = stepIndex;
            this.timeStr = timeStr;
            this.remainingVolumeMl = remainingVolumeMl;
            this.estimatedRemainingCups = estimatedRemainingCups;
            this.cupsSoldThisStep = cupsSoldThisStep;
            this.cumulativeCupsSold = cumulativeCupsSold;
            this.velocityScore = velocityScore;
            this.stockPressurePct = stockPressurePct;
            this.timeFactorMultiplier = timeFactorMultiplier;
            this.demandScore = demandScore;
            this.price = price;
            this.priceMovement = priceMovement;
            this.explanation = explanation;
        }

        public int getStepIndex() { return stepIndex; }
        public void setStepIndex(int stepIndex) { this.stepIndex = stepIndex; }
        public String getTimeStr() { return timeStr; }
        public void setTimeStr(String timeStr) { this.timeStr = timeStr; }
        public int getRemainingVolumeMl() { return remainingVolumeMl; }
        public void setRemainingVolumeMl(int remainingVolumeMl) { this.remainingVolumeMl = remainingVolumeMl; }
        public int getEstimatedRemainingCups() { return estimatedRemainingCups; }
        public void setEstimatedRemainingCups(int estimatedRemainingCups) { this.estimatedRemainingCups = estimatedRemainingCups; }
        public int getCupsSoldThisStep() { return cupsSoldThisStep; }
        public void setCupsSoldThisStep(int cupsSoldThisStep) { this.cupsSoldThisStep = cupsSoldThisStep; }
        public int getCumulativeCupsSold() { return cumulativeCupsSold; }
        public void setCumulativeCupsSold(int cumulativeCupsSold) { this.cumulativeCupsSold = cumulativeCupsSold; }
        public double getVelocityScore() { return velocityScore; }
        public void setVelocityScore(double velocityScore) { this.velocityScore = velocityScore; }
        public double getStockPressurePct() { return stockPressurePct; }
        public void setStockPressurePct(double stockPressurePct) { this.stockPressurePct = stockPressurePct; }
        public double getTimeFactorMultiplier() { return timeFactorMultiplier; }
        public void setTimeFactorMultiplier(double timeFactorMultiplier) { this.timeFactorMultiplier = timeFactorMultiplier; }
        public double getDemandScore() { return demandScore; }
        public void setDemandScore(double demandScore) { this.demandScore = demandScore; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public String getPriceMovement() { return priceMovement; }
        public void setPriceMovement(String priceMovement) { this.priceMovement = priceMovement; }
        public String getExplanation() { return explanation; }
        public void setExplanation(String explanation) { this.explanation = explanation; }

        public static SimulationStepBuilder builder() { return new SimulationStepBuilder(); }
        public static class SimulationStepBuilder {
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
            private String priceMovement;
            private String explanation;

            public SimulationStepBuilder stepIndex(int stepIndex) { this.stepIndex = stepIndex; return this; }
            public SimulationStepBuilder timeStr(String timeStr) { this.timeStr = timeStr; return this; }
            public SimulationStepBuilder remainingVolumeMl(int remainingVolumeMl) { this.remainingVolumeMl = remainingVolumeMl; return this; }
            public SimulationStepBuilder estimatedRemainingCups(int estimatedRemainingCups) { this.estimatedRemainingCups = estimatedRemainingCups; return this; }
            public SimulationStepBuilder cupsSoldThisStep(int cupsSoldThisStep) { this.cupsSoldThisStep = cupsSoldThisStep; return this; }
            public SimulationStepBuilder cumulativeCupsSold(int cumulativeCupsSold) { this.cumulativeCupsSold = cumulativeCupsSold; return this; }
            public SimulationStepBuilder velocityScore(double velocityScore) { this.velocityScore = velocityScore; return this; }
            public SimulationStepBuilder stockPressurePct(double stockPressurePct) { this.stockPressurePct = stockPressurePct; return this; }
            public SimulationStepBuilder timeFactorMultiplier(double timeFactorMultiplier) { this.timeFactorMultiplier = timeFactorMultiplier; return this; }
            public SimulationStepBuilder demandScore(double demandScore) { this.demandScore = demandScore; return this; }
            public SimulationStepBuilder price(BigDecimal price) { this.price = price; return this; }
            public SimulationStepBuilder priceMovement(String priceMovement) { this.priceMovement = priceMovement; return this; }
            public SimulationStepBuilder explanation(String explanation) { this.explanation = explanation; return this; }
            public SimulationStep build() { return new SimulationStep(stepIndex, timeStr, remainingVolumeMl, estimatedRemainingCups, cupsSoldThisStep, cumulativeCupsSold, velocityScore, stockPressurePct, timeFactorMultiplier, demandScore, price, priceMovement, explanation); }
        }
    }

    public static class SimulationResponse {
        private String flavourName;
        private int initialVolumeMl;
        private int finalVolumeMl;
        private BigDecimal initialPrice;
        private BigDecimal finalPrice;
        private int totalCupsSold;
        private List<SimulationStep> steps;

        public SimulationResponse() {}
        public SimulationResponse(String flavourName, int initialVolumeMl, int finalVolumeMl, BigDecimal initialPrice, BigDecimal finalPrice, int totalCupsSold, List<SimulationStep> steps) {
            this.flavourName = flavourName;
            this.initialVolumeMl = initialVolumeMl;
            this.finalVolumeMl = finalVolumeMl;
            this.initialPrice = initialPrice;
            this.finalPrice = finalPrice;
            this.totalCupsSold = totalCupsSold;
            this.steps = steps;
        }

        public String getFlavourName() { return flavourName; }
        public void setFlavourName(String flavourName) { this.flavourName = flavourName; }
        public int getInitialVolumeMl() { return initialVolumeMl; }
        public void setInitialVolumeMl(int initialVolumeMl) { this.initialVolumeMl = initialVolumeMl; }
        public int getFinalVolumeMl() { return finalVolumeMl; }
        public void setFinalVolumeMl(int finalVolumeMl) { this.finalVolumeMl = finalVolumeMl; }
        public BigDecimal getInitialPrice() { return initialPrice; }
        public void setInitialPrice(BigDecimal initialPrice) { this.initialPrice = initialPrice; }
        public BigDecimal getFinalPrice() { return finalPrice; }
        public void setFinalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; }
        public int getTotalCupsSold() { return totalCupsSold; }
        public void setTotalCupsSold(int totalCupsSold) { this.totalCupsSold = totalCupsSold; }
        public List<SimulationStep> getSteps() { return steps; }
        public void setSteps(List<SimulationStep> steps) { this.steps = steps; }

        public static SimulationResponseBuilder builder() { return new SimulationResponseBuilder(); }
        public static class SimulationResponseBuilder {
            private String flavourName;
            private int initialVolumeMl;
            private int finalVolumeMl;
            private BigDecimal initialPrice;
            private BigDecimal finalPrice;
            private int totalCupsSold;
            private List<SimulationStep> steps;

            public SimulationResponseBuilder flavourName(String flavourName) { this.flavourName = flavourName; return this; }
            public SimulationResponseBuilder initialVolumeMl(int initialVolumeMl) { this.initialVolumeMl = initialVolumeMl; return this; }
            public SimulationResponseBuilder finalVolumeMl(int finalVolumeMl) { this.finalVolumeMl = finalVolumeMl; return this; }
            public SimulationResponseBuilder initialPrice(BigDecimal initialPrice) { this.initialPrice = initialPrice; return this; }
            public SimulationResponseBuilder finalPrice(BigDecimal finalPrice) { this.finalPrice = finalPrice; return this; }
            public SimulationResponseBuilder totalCupsSold(int totalCupsSold) { this.totalCupsSold = totalCupsSold; return this; }
            public SimulationResponseBuilder steps(List<SimulationStep> steps) { this.steps = steps; return this; }
            public SimulationResponse build() { return new SimulationResponse(flavourName, initialVolumeMl, finalVolumeMl, initialPrice, finalPrice, totalCupsSold, steps); }
        }
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
