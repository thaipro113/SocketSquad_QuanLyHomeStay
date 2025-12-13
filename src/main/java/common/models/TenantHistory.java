package common.models;

import java.io.Serializable;
import java.sql.Timestamp;

public class TenantHistory implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String idCard;
    private String phone;
    private int roomId;
    private String contractPath;
    private Timestamp checkInDate;
    private Timestamp checkoutDate;

    public TenantHistory() {
    }

    public TenantHistory(int id, String name, String idCard, String phone, int roomId, String contractPath,
            Timestamp checkInDate, Timestamp checkoutDate) {
        this.id = id;
        this.name = name;
        this.idCard = idCard;
        this.phone = phone;
        this.roomId = roomId;
        this.contractPath = contractPath;
        this.checkInDate = checkInDate;
        this.checkoutDate = checkoutDate;
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

    public Timestamp getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Timestamp checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Timestamp getCheckoutDate() {
        return checkoutDate;
    }

    public void setCheckoutDate(Timestamp checkoutDate) {
        this.checkoutDate = checkoutDate;
    }
}
