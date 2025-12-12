package common.models;

import java.io.Serializable;
import java.util.Date;

public class Tenant implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String idCard;
    private String phone;
    private int roomId;
    private String contractPath;
    private Date checkinDate;

    public Tenant() {
    }

    public Tenant(int id, String name, String idCard, String phone, int roomId, String contractPath, Date checkinDate) {
        this.id = id;
        this.name = name;
        this.idCard = idCard;
        this.phone = phone;
        this.roomId = roomId;
        this.contractPath = contractPath;
        this.checkinDate = checkinDate;
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

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getContractPath() {
        return contractPath;
    }

    public void setContractPath(String contractPath) {
        this.contractPath = contractPath;
    }

    public Date getCheckinDate() {
        return checkinDate;
    }

    public void setCheckinDate(Date checkinDate) {
        this.checkinDate = checkinDate;
    }
}