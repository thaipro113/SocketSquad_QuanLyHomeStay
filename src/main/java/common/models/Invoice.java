package common.models;

import java.io.Serializable;
import java.util.Date;

public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private int roomId;
    private int month;
    private int year;
    private int electricityUsage;
    private int waterUsage;
    private double internetFee;
    private double totalAmount;
    private String status; // PAID, UNPAID
    private Date createdAt;
    private double roomPrice;
    private int rentalDays;
    private Date checkinDate;
    private Date checkoutDate;

    public Invoice() {
    }

    public Invoice(int id, int roomId, int month, int year, int electricityUsage, int waterUsage,
                   double internetFee, double totalAmount, String status, Date createdAt,
                   double roomPrice, int rentalDays, Date checkinDate, Date checkoutDate) {
        this.id = id;
        this.roomId = roomId;
        this.month = month;
        this.year = year;
        this.electricityUsage = electricityUsage;
        this.waterUsage = waterUsage;
        this.internetFee = internetFee;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
        this.roomPrice = roomPrice;
        this.rentalDays = rentalDays;
        this.checkinDate = checkinDate;
        this.checkoutDate = checkoutDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getElectricityUsage() {
        return electricityUsage;
    }

    public void setElectricityUsage(int electricityUsage) {
        this.electricityUsage = electricityUsage;
    }

    public int getWaterUsage() {
        return waterUsage;
    }

    public void setWaterUsage(int waterUsage) {
        this.waterUsage = waterUsage;
    }

    public double getInternetFee() {
        return internetFee;
    }

    public void setInternetFee(double internetFee) {
        this.internetFee = internetFee;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public double getRoomPrice() {
        return roomPrice;
    }

    public void setRoomPrice(double roomPrice) {
        this.roomPrice = roomPrice;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    public Date getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate) {
        this.checkinDate = checkinDate;
    }

    public Date getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(Date checkoutDate) {
        this.checkoutDate = checkoutDate;
    }
}
