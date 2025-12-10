package server.controller;

import common.Payload;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.User;
import server.exception.ValidationException;
import server.exception.BusinessException;
import server.model.InvoiceDAO;
import server.model.RoomDAO;
import server.model.TenantDAO;
import server.model.UserDAO;
import server.service.ValidationService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerController {

    private static final Logger LOGGER = Logger.getLogger(ServerController.class.getName());

    private UserDAO userDAO;
    private RoomDAO roomDAO;
    private TenantDAO tenantDAO;
    private InvoiceDAO invoiceDAO;
    private ValidationService validationService;

    public ServerController() {
        this.userDAO = new UserDAO();
        this.roomDAO = new RoomDAO();
        this.tenantDAO = new TenantDAO();
        this.invoiceDAO = new InvoiceDAO();
        this.validationService = new ValidationService();
    }

    public Payload handleRequest(Payload request) {
        try {
            LOGGER.info("Processing request: " + request.getAction());

            switch (request.getAction()) {
                case LOGIN:
                    return handleLogin(request);

                case GET_ROOMS:
                    return handleGetRooms();

                case ADD_ROOM:
                    return handleAddRoom(request);

                case UPDATE_ROOM:
                    return handleUpdateRoom(request);

                case DELETE_ROOM:
                    return handleDeleteRoom(request);

                case GET_TENANTS:
                    return handleGetTenants();

                case ADD_TENANT:
                    return handleAddTenant(request);

                case UPDATE_TENANT:
                    return handleUpdateTenant(request);

                case DELETE_TENANT:
                    return handleDeleteTenant(request);

                case CALCULATE_COST:
                    return handleCalculateCost(request);

                case GET_INVOICES:
                    return handleGetInvoices();

                case DELETE_INVOICE:
                    return handleDeleteInvoice(request);

                case UPLOAD_FILE:
                    return handleUploadFile(request);

                case GET_IMAGE:
                    return handleGetImage(request);

                default:
                    LOGGER.warning("Unknown action: " + request.getAction());
                    return new Payload(Payload.Action.FAILURE, null, "Hành động không xác định");
            }
        } catch (ValidationException e) {
            LOGGER.log(Level.WARNING, "Validation error", e);
            return new Payload(Payload.Action.FAILURE, null, "Lỗi xác thực: " + e.getMessage());
        } catch (BusinessException e) {
            LOGGER.log(Level.WARNING, "Business logic error", e);
            return new Payload(Payload.Action.FAILURE, null, "Lỗi nghiệp vụ: " + e.getMessage());
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unexpected error", e);
            return new Payload(Payload.Action.FAILURE, null, "Lỗi hệ thống: " + e.getMessage());
        }
    }

    // ==================== LOGIN ====================
    private Payload handleLogin(Payload request) throws ValidationException {
        User userCredentials = (User) request.getData();
        validationService.validateUser(userCredentials);

        User user = userDAO.login(userCredentials.getUsername(), userCredentials.getPassword());
        if (user != null) {
            LOGGER.info("User logged in: " + user.getUsername());
            return new Payload(Payload.Action.SUCCESS, user, "Đăng nhập thành công");
        } else {
            LOGGER.warning("Login failed for user: " + userCredentials.getUsername());
            return new Payload(Payload.Action.FAILURE, null, "Sai tên đăng nhập hoặc mật khẩu");
        }
    }

    // ==================== ROOM OPERATIONS ====================
    private Payload handleGetRooms() {
        List<Room> rooms = roomDAO.getAllRooms();
        LOGGER.info("Retrieved " + rooms.size() + " rooms");
        return new Payload(Payload.Action.SUCCESS, rooms);
    }

    private Payload handleAddRoom(Payload request) throws ValidationException, BusinessException {
        Room newRoom = (Room) request.getData();
        validationService.validateRoom(newRoom);

        // Business rule: Check if room name already exists
        List<Room> existingRooms = roomDAO.getAllRooms();
        for (Room room : existingRooms) {
            if (room.getName().equalsIgnoreCase(newRoom.getName())) {
                throw new BusinessException(
                        "Tên phòng đã tồn tại: " + newRoom.getName(),
                        BusinessException.ErrorCode.DUPLICATE_ENTRY
                );
            }
        }

        if (roomDAO.addRoom(newRoom)) {
            LOGGER.info("Room added: " + newRoom.getName());
            return new Payload(Payload.Action.SUCCESS, null, "Thêm phòng thành công");
        } else {
            throw new BusinessException(
                    "Không thể thêm phòng",
                    BusinessException.ErrorCode.DATABASE_ERROR
            );
        }
    }

    private Payload handleUpdateRoom(Payload request) throws ValidationException, BusinessException {
        Room updateRoom = (Room) request.getData();
        validationService.validateRoom(updateRoom);
        validationService.validateId(updateRoom.getId(), "Phòng");

        if (roomDAO.updateRoom(updateRoom)) {
            LOGGER.info("Room updated: " + updateRoom.getId());
            return new Payload(Payload.Action.SUCCESS, null, "Cập nhật phòng thành công");
        } else {
            throw new BusinessException(
                    "Không thể cập nhật phòng",
                    BusinessException.ErrorCode.ROOM_NOT_FOUND
            );
        }
    }

    private Payload handleDeleteRoom(Payload request) throws ValidationException, BusinessException {
        int roomId = (int) request.getData();
        validationService.validateId(roomId, "Phòng");

        // Business rule: Check if room has active tenants
        List<Tenant> tenants = tenantDAO.getAllTenants();
        for (Tenant tenant : tenants) {
            if (tenant.getRoomId() == roomId) {
                throw new BusinessException(
                        "Không thể xóa phòng còn khách thuê",
                        BusinessException.ErrorCode.ROOM_OCCUPIED
                );
            }
        }

        if (roomDAO.deleteRoom(roomId)) {
            LOGGER.info("Room deleted: " + roomId);
            return new Payload(Payload.Action.SUCCESS, null, "Xóa phòng thành công");
        } else {
            throw new BusinessException(
                    "Không thể xóa phòng",
                    BusinessException.ErrorCode.ROOM_NOT_FOUND
            );
        }
    }

    // ==================== TENANT OPERATIONS ====================
    private Payload handleGetTenants() {
        List<Tenant> tenants = tenantDAO.getAllTenants();
        LOGGER.info("Retrieved " + tenants.size() + " tenants");
        return new Payload(Payload.Action.SUCCESS, tenants);
    }

    private Payload handleAddTenant(Payload request) throws ValidationException, BusinessException {
        Tenant newTenant = (Tenant) request.getData();
        validationService.validateTenant(newTenant);

        // Business rule: Check if ID card already exists
        List<Tenant> existingTenants = tenantDAO.getAllTenants();
        for (Tenant tenant : existingTenants) {
            if (tenant.getIdCard().equals(newTenant.getIdCard())) {
                throw new BusinessException(
                        "CMND/CCCD đã tồn tại: " + newTenant.getIdCard(),
                        BusinessException.ErrorCode.DUPLICATE_ENTRY
                );
            }
        }

        // Business rule: Check if room is available
        if (newTenant.getRoomId() > 0) {
            List<Room> rooms = roomDAO.getAllRooms();
            boolean roomFound = false;
            for (Room room : rooms) {
                if (room.getId() == newTenant.getRoomId()) {
                    roomFound = true;
                    if (!"AVAILABLE".equalsIgnoreCase(room.getStatus())) {
                        throw new BusinessException(
                                "Phòng không khả dụng",
                                BusinessException.ErrorCode.ROOM_OCCUPIED
                        );
                    }
                    break;
                }
            }
            if (!roomFound) {
                throw new BusinessException(
                        "Phòng không tồn tại",
                        BusinessException.ErrorCode.ROOM_NOT_FOUND
                );
            }
        }

        if (tenantDAO.addTenant(newTenant)) {
            LOGGER.info("Tenant added: " + newTenant.getName());
            return new Payload(Payload.Action.SUCCESS, null, "Thêm khách thuê thành công");
        } else {
            throw new BusinessException(
                    "Không thể thêm khách thuê",
                    BusinessException.ErrorCode.DATABASE_ERROR
            );
        }
    }

    private Payload handleUpdateTenant(Payload request) throws ValidationException, BusinessException {
        Tenant updateTenant = (Tenant) request.getData();
        validationService.validateTenant(updateTenant);
        validationService.validateId(updateTenant.getId(), "Khách thuê");

        if (tenantDAO.updateTenant(updateTenant)) {
            LOGGER.info("Tenant updated: " + updateTenant.getId());
            return new Payload(Payload.Action.SUCCESS, null, "Cập nhật khách thuê thành công");
        } else {
            throw new BusinessException(
                    "Không thể cập nhật khách thuê",
                    BusinessException.ErrorCode.TENANT_NOT_FOUND
            );
        }
    }

    private Payload handleDeleteTenant(Payload request) throws ValidationException, BusinessException {
        int tenantId = (int) request.getData();
        validationService.validateId(tenantId, "Khách thuê");

        if (tenantDAO.deleteTenant(tenantId)) {
            LOGGER.info("Tenant deleted: " + tenantId);
            return new Payload(Payload.Action.SUCCESS, null, "Xóa khách thuê thành công");
        } else {
            throw new BusinessException(
                    "Không thể xóa khách thuê",
                    BusinessException.ErrorCode.TENANT_NOT_FOUND
            );
        }
    }

    // ==================== INVOICE OPERATIONS ====================
    private Payload handleGetInvoices() {
        List<Invoice> invoices = invoiceDAO.getAllInvoices();
        LOGGER.info("Retrieved " + invoices.size() + " invoices");
        return new Payload(Payload.Action.SUCCESS, invoices);
    }

    private Payload handleCalculateCost(Payload request) throws ValidationException, BusinessException {
        Invoice costRequest = (Invoice) request.getData();
        validationService.validateInvoice(costRequest);

        // Business rule: Check if invoice already exists for this room/month/year
        List<Invoice> existingInvoices = invoiceDAO.getAllInvoices();
        for (Invoice inv : existingInvoices) {
            if (inv.getRoomId() == costRequest.getRoomId()
                    && inv.getMonth() == costRequest.getMonth()
                    && inv.getYear() == costRequest.getYear()) {
                throw new BusinessException(
                        "Hóa đơn cho phòng này trong tháng " + costRequest.getMonth() + "/" + costRequest.getYear() + " đã tồn tại",
                        BusinessException.ErrorCode.INVOICE_EXISTS
                );
            }
        }

        // Get service prices
        double elecPrice = invoiceDAO.getServicePrice("Electricity");
        double waterPrice = invoiceDAO.getServicePrice("Water");
        double internetPrice = invoiceDAO.getServicePrice("Internet");

        // Validate service prices
        validationService.validateServicePrice(elecPrice);
        validationService.validateServicePrice(waterPrice);
        validationService.validateServicePrice(internetPrice);

        // Calculate total
        double total = (costRequest.getElectricityUsage() * elecPrice) +
                (costRequest.getWaterUsage() * waterPrice) +
                internetPrice;

        costRequest.setInternetFee(internetPrice);
        costRequest.setTotalAmount(total);
        costRequest.setStatus("UNPAID");

        if (invoiceDAO.addInvoice(costRequest)) {
            LOGGER.info(String.format("Invoice created for room %d: %.2f VND",
                    costRequest.getRoomId(), total));
            return new Payload(Payload.Action.SUCCESS, costRequest, "Tạo hóa đơn thành công");
        } else {
            throw new BusinessException(
                    "Không thể tạo hóa đơn",
                    BusinessException.ErrorCode.DATABASE_ERROR
            );
        }
    }

    private Payload handleDeleteInvoice(Payload request) throws ValidationException, BusinessException {
        int invoiceId = (int) request.getData();
        validationService.validateId(invoiceId, "Hóa đơn");

        if (invoiceDAO.deleteInvoice(invoiceId)) {
            LOGGER.info("Invoice deleted: " + invoiceId);
            return new Payload(Payload.Action.SUCCESS, null, "Xóa hóa đơn thành công");
        } else {
            throw new BusinessException(
                    "Không thể xóa hóa đơn",
                    BusinessException.ErrorCode.DATABASE_ERROR
            );
        }
    }

    // ==================== FILE OPERATIONS ====================
    private Payload handleUploadFile(Payload request) throws ValidationException {
        Object[] fileData = (Object[]) request.getData();
        String fileName = (String) fileData[0];
        byte[] fileBytes = (byte[]) fileData[1];

        validationService.validateFileUpload(fileName, fileBytes);

        String uploadDir = "uploads";
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        try (FileOutputStream fos = new FileOutputStream(uploadDir + File.separator + fileName)) {
            fos.write(fileBytes);
            String filePath = uploadDir + "/" + fileName;
            LOGGER.info("File uploaded: " + filePath);
            return new Payload(Payload.Action.SUCCESS, filePath, "Tải file lên thành công");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "File upload error", e);
            return new Payload(Payload.Action.FAILURE, null, "Lỗi khi lưu file: " + e.getMessage());
        }
    }

    private Payload handleGetImage(Payload request) throws BusinessException {
        String imagePath = (String) request.getData();

        try {
            File imgFile = new File(imagePath);
            if (!imgFile.exists()) {
                throw new BusinessException(
                        "File không tồn tại: " + imagePath,
                        BusinessException.ErrorCode.FILE_NOT_FOUND
                );
            }

            byte[] imgBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
            LOGGER.info("Image retrieved: " + imagePath);
            return new Payload(Payload.Action.SUCCESS, imgBytes);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Image read error", e);
            throw new BusinessException(
                    "Lỗi đọc file: " + e.getMessage(),
                    BusinessException.ErrorCode.FILE_NOT_FOUND
            );
        }
    }
}