package common.models;

import java.io.Serializable;

public class CheckoutPayload implements Serializable {
    private static final long serialVersionUID = 1L;

    private int roomId;
    private int newElectricityIndex;
    private int waterUsage; // Or newWaterIndex if we track index, user asked for usage calc but input new
                            // index usually
    private double otherCosts;

    // For calculation display
    private double totalAmount;

    public CheckoutPayload() {
    }

    public CheckoutPayload(int roomId, int newElectricityIndex, int waterUsage) {
        this.roomId = roomId;
        this.newElectricityIndex = newElectricityIndex;
        this.waterUsage = waterUsage;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getNewElectricityIndex() {
        return newElectricityIndex;
    }

    public void setNewElectricityIndex(int newElectricityIndex) {
        this.newElectricityIndex = newElectricityIndex;
    }

    public int getWaterUsage() {
        return waterUsage;
    }

    public void setWaterUsage(int waterUsage) {
        this.waterUsage = waterUsage;
    }

    public double getOtherCosts() {
        return otherCosts;
    }

    public void setOtherCosts(double otherCosts) {
        this.otherCosts = otherCosts;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
