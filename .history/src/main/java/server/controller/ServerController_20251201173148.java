package server.controller;

import common.Payload;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.User;
import server.model.InvoiceDAO;
import server.model.RoomDAO;
import server.model.TenantDAO;
import server.model.UserDAO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ServerController {

    private UserDAO userDAO;
    private RoomDAO roomDAO;
    private TenantDAO tenantDAO;
    private InvoiceDAO invoiceDAO;

    public ServerController() {
        this.userDAO = new UserDAO();
        this.roomDAO = new RoomDAO();
        this.tenantDAO = new TenantDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public Payload handleRequest(Payload request) {
        try {
            switch (request.getAction()) {
                case LOGIN:
                    User userCredentials = (User) request.getData();
                    User user = userDAO.login(userCredentials.getUsername(), userCredentials.getPassword());
                    if (user != null) {
                        return new Payload(Payload.Action.SUCCESS, user, "Login successful");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Invalid username or password");
                    }

                case GET_ROOMS:
                    List<Room> rooms = roomDAO.getAllRooms();
                    return new Payload(Payload.Action.SUCCESS, rooms);

                case ADD_ROOM:
                    Room newRoom = (Room) request.getData();
                    if (roomDAO.addRoom(newRoom)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Room added successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to add room");
                    }

                case UPDATE_ROOM:
                    Room updateRoom = (Room) request.getData();
                    if (roomDAO.updateRoom(updateRoom)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Room updated successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to update room");
                    }

                case DELETE_ROOM:
                    int roomId = (int) request.getData();
                    if (roomDAO.deleteRoom(roomId)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Room deleted successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to delete room");
                    }

                case GET_TENANTS:
                    List<Tenant> tenants = tenantDAO.getAllTenants();
                    return new Payload(Payload.Action.SUCCESS, tenants);

                case ADD_TENANT:
                    Tenant newTenant = (Tenant) request.getData();
                    if (tenantDAO.addTenant(newTenant)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Tenant added successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to add tenant");
                    }

                case UPDATE_TENANT:
                    Tenant updateTenant = (Tenant) request.getData();
                    if (tenantDAO.updateTenant(updateTenant)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Tenant updated successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to update tenant");
                    }

                case DELETE_TENANT:
                    int tenantId = (int) request.getData();
                    if (tenantDAO.deleteTenant(tenantId)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Tenant deleted successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to delete tenant");
                    }

                case CALCULATE_COST:
                    Invoice costRequest = (Invoice) request.getData();
                    double elecPrice = invoiceDAO.getServicePrice("Electricity");
                    double waterPrice = invoiceDAO.getServicePrice("Water");
                    double internetPrice = invoiceDAO.getServicePrice("Internet");

                    double total = (costRequest.getElectricityUsage() * elecPrice) +
                            (costRequest.getWaterUsage() * waterPrice) +
                            internetPrice;

                    costRequest.setInternetFee(internetPrice);
                    costRequest.setTotalAmount(total);
                    costRequest.setStatus("UNPAID");

                    if (invoiceDAO.addInvoice(costRequest)) {
                        return new Payload(Payload.Action.SUCCESS, costRequest, "Invoice created successfully");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to create invoice");
                    }

                case GET_INVOICES:
                    List<Invoice> invoices = invoiceDAO.getAllInvoices();
                    return new Payload(Payload.Action.SUCCESS, invoices);

                case UPLOAD_FILE:
                    Object[] fileData = (Object[]) request.getData();
                    String fileName = (String) fileData[0];
                    byte[] fileBytes = (byte[]) fileData[1];

                    String uploadDir = "uploads";
                    File dir = new File(uploadDir);
                    if (!dir.exists())
                        dir.mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(uploadDir + File.separator + fileName)) {
                        fos.write(fileBytes);
                        return new Payload(Payload.Action.SUCCESS, uploadDir + "/" + fileName,
                                "File uploaded successfully");
                    } catch (IOException e) {
                        return new Payload(Payload.Action.FAILURE, null, "Failed to save file: " + e.getMessage());
                    }

                default:
                    return new Payload(Payload.Action.FAILURE, null, "Unknown action");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Payload(Payload.Action.FAILURE, null, "Server error: " + e.getMessage());
        }
    }
}
