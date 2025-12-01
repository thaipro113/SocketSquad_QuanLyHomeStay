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
}
