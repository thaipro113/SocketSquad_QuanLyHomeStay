package client.controller;

import client.network.SocketClient;
import common.Payload;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.TenantHistory;
import common.models.User;

import java.io.IOException;
import java.util.List;

public class ClientController {
    private SocketClient socketClient;

    public ClientController() {
        this.socketClient = new SocketClient("localhost", 12345);
    }

    public void connect() throws IOException {
        socketClient.connect();
    }

    public User login(String username, String password) {
        try {
            User user = new User(0, username, password, null);
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.LOGIN, user));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (User) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> getRooms() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_ROOMS, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (List<Room>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addRoom(Room room) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.ADD_ROOM, room));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.UPDATE_ROOM, room));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int roomId) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.DELETE_ROOM, roomId));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Tenant> getTenants() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_TENANTS, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (List<Tenant>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addTenant(Tenant tenant) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.ADD_TENANT, tenant));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTenant(Tenant tenant) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.UPDATE_TENANT, tenant));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTenant(int tenantId) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.DELETE_TENANT, tenantId));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Invoice calculateCost(Invoice invoiceData) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.CALCULATE_COST, invoiceData));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (Invoice) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Invoice> getInvoices() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_INVOICES, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (List<Invoice>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deleteInvoice(int invoiceId) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.DELETE_INVOICE, invoiceId));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String uploadFile(String fileName, byte[] fileBytes) {
        try {
            Object[] fileData = new Object[] { fileName, fileBytes };
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.UPLOAD_FILE, fileData));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (String) response.getData(); // Returns the server path
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public byte[] getImage(String imagePath) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_IMAGE, imagePath));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (byte[]) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkoutRoom(int roomId) {
        // Old method kept for compatibility, usage should be updated
        return checkoutRoom(roomId, 0, 0).getAction() == Payload.Action.SUCCESS;
    }

    public Payload checkoutRoom(int roomId, int electricityMeter, int waterMeter) {
        try {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("roomId", roomId);
            data.put("electricityMeter", electricityMeter);
            data.put("waterMeter", waterMeter);
            return socketClient.sendRequest(new Payload(Payload.Action.CHECKOUT_ROOM, data));
        } catch (Exception e) {
            e.printStackTrace();
            return new Payload(Payload.Action.FAILURE, null, "Lỗi kết nối");
        }
    }

    public java.util.Map<String, Double> getServices() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_SERVICES, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (java.util.Map<String, Double>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean checkinRoom(int roomId) {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.CHECKIN_ROOM, roomId));
            return response.getAction() == Payload.Action.SUCCESS;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TenantHistory> getTenantHistory() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_TENANT_HISTORY, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (List<TenantHistory>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public java.util.Map<String, Object> getStatistics() {
        try {
            Payload response = socketClient.sendRequest(new Payload(Payload.Action.GET_STATISTICS, null));
            if (response.getAction() == Payload.Action.SUCCESS) {
                return (java.util.Map<String, Object>) response.getData();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
