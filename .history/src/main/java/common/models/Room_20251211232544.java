package common.models;

import java.io.Serializable;

public class Room implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String status; // AVAILABLE, OCCUPIED, RESERVED
    private double price;
    private String imagePath;
    private int electricityIndex;
    private int waterIndex;

    public Room() {
    }

    public Room(int id, String name, String status, double price, String imagePath, int electricityIndex,
            int waterIndex) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.price = price;
        this.imagePath = imagePath;
        this.electricityIndex = electricityIndex;
        this.waterIndex = waterIndex;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}
