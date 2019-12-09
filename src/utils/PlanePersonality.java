package utils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class PlanePersonality {

    /**
     * Fuel Left (liters)
     */
    int fuelLeft;
    /**
     * Plane average speed of 100 km/h (kilometers per hour)
     */
    int speed;
    /**
     * Plane average total fuel loss of 10 L/km (liters per kilometer)
     */
    int fuelLoss = 10;
    /**
     * Predicted Flight Time Left (minutes)
     */
    int timeLeft = (int)((double)(fuelLeft/fuelLoss)/speed*60);

    /**
     * Money available to spend
     */
    int moneyAvailable;

    /**
     * Maximum delay allowed is 2 times the current estimated flight time
     */
    int maxDelay = 2*timeLeft;

    /**
     * Current position coordinates
     */
    Pair<Integer, Integer> actualPos;

    /**
     * Destination position coordinates
     */
    Pair<Integer, Integer> finalPos;
    /**
     * Distance left (km)
     */
    int distanceLeft = 0;
    int bid=10;

    public PlanePersonality() { }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public int getMoneyAvailable() {
        return moneyAvailable;
    }

    public void setMoneyAvailable(int moneyAvailable) {
        this.moneyAvailable = moneyAvailable;
    }

    public int getMaxDelay() {
        return maxDelay;
    }

    public void setMaxDelay(int maxDelay) {
        this.maxDelay = maxDelay;
    }

    public int getFuelLeft() {
        return fuelLeft;
    }

    public void setFuelLeft(int fuelLeft) {
        this.fuelLeft = fuelLeft;
    }

    public int getFuelLoss() {
        return fuelLoss;
    }

    public void setFuelLoss(int fuelLoss) {
        this.fuelLoss = fuelLoss;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    public void setTimeLeft(int timeLeft) {
        this.timeLeft = timeLeft;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }

    public int getDistanceLeft() {
        return distanceLeft;
    }

    public void setDistanceLeft(int distanceLeft) {
        this.distanceLeft = distanceLeft;
    }

    public Pair<Integer, Integer> getActualPos() {
        return actualPos;
    }

    public void setActualPos(Pair<Integer, Integer> actualPos) {
        this.actualPos = actualPos;
    }

    public Pair<Integer, Integer> getFinalPos() {
        return finalPos;
    }

    public void setFinalPos(Pair<Integer, Integer> finalPos) {
        this.finalPos = finalPos;
    }
}