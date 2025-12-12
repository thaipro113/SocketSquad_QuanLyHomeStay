package server.controller;

import common.Payload;
import common.models.CheckoutRequest;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.TenantHistory;
import common.models.User;
import server.model.InvoiceDAO;
import server.model.RoomDAO;
import server.model.TenantDAO;
import server.model.TenantHistoryDAO;
import server.model.UserDAO;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ServerController {

    private UserDAO userDAO;
    private RoomDAO roomDAO;
    private TenantDAO tenantDAO;
    private TenantHistoryDAO tenantHistoryDAO;
    private InvoiceDAO invoiceDAO;

    public ServerController() {
        this.userDAO = new UserDAO();
        this.roomDAO = new RoomDAO();
        this.tenantDAO = new TenantDAO();
        this.tenantHistoryDAO = new TenantHistoryDAO();
        this.invoiceDAO = new InvoiceDAO();
    }

    public Payload handleRequest(Payload request) {
        try {
            switch (request.getAction()) {
                case LOGIN:
                    User userCredentials = (User) request.getData();
                    User user = userDAO.login(userCredentials.getUsername(), userCredentials.getPassword());
                    if (user != null) {
                        return new Payload(Payload.Action.SUCCESS, user, "Đăng nhập thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Sai tên đăng nhập hoặc mật khẩu");
                    }

                case GET_ROOMS:
                    List<Room> rooms = roomDAO.getAllRooms();
                    return new Payload(Payload.Action.SUCCESS, rooms);

                case ADD_ROOM:
                    Room newRoom = (Room) request.getData();
                    if (roomDAO.addRoom(newRoom)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Thêm phòng thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Thêm phòng thất bại");
                    }

                case UPDATE_ROOM:
                    Room updateRoom = (Room) request.getData();
                    if (roomDAO.updateRoom(updateRoom)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Cập nhật phòng thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Cập nhật phòng thất bại");
                    }

                case DELETE_ROOM:
                    int roomId = (int) request.getData();
                    if (roomDAO.deleteRoom(roomId)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Xóa phòng thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Xóa phòng thất bại");
                    }

                case GET_TENANTS:
                    List<Tenant> tenants = tenantDAO.getAllTenants();
                    return new Payload(Payload.Action.SUCCESS, tenants);

                case ADD_TENANT:
                    Tenant newTenant = (Tenant) request.getData();
                    if (tenantDAO.addTenant(newTenant)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Thêm khách thuê thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Thêm khách thuê thất bại");
                    }

                case UPDATE_TENANT:
                    Tenant updateTenant = (Tenant) request.getData();
                    if (tenantDAO.updateTenant(updateTenant)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Cập nhật khách thuê thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Cập nhật khách thuê thất bại");
                    }

                case DELETE_TENANT:
                    int tenantId = (int) request.getData();
                    if (tenantDAO.deleteTenant(tenantId)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Xóa khách thuê thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Xóa khách thuê thất bại");
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
                        return new Payload(Payload.Action.SUCCESS, costRequest, "Tạo hóa đơn thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Tạo hóa đơn thất bại");
                    }

                case GET_INVOICES:
                    List<Invoice> invoices = invoiceDAO.getAllInvoices();
                    return new Payload(Payload.Action.SUCCESS, invoices);

                case DELETE_INVOICE:
                    int invoiceId = (int) request.getData();
                    if (invoiceDAO.deleteInvoice(invoiceId)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Xóa hóa đơn thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Xóa hóa đơn thất bại");
                    }

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
                                "Tải file lên thành công");
                    } catch (IOException e) {
                        return new Payload(Payload.Action.FAILURE, null, "Lỗi khi lưu file: " + e.getMessage());
                    }

                case GET_IMAGE:
                    String imagePath = (String) request.getData();
                    try {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            byte[] imgBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                            return new Payload(Payload.Action.SUCCESS, imgBytes);
                        } else {
                            return new Payload(Payload.Action.FAILURE, null, "File không tồn tại");
                        }
                    } catch (IOException e) {
                        return new Payload(Payload.Action.FAILURE, null, "Lỗi đọc file: " + e.getMessage());
                    }

                    // ✅ CASE MỚI: Trả phòng + Tạo hóa đơn
                case CHECKOUT_WITH_INVOICE:
                    CheckoutRequest checkoutReq = (CheckoutRequest) request.getData();
                    return handleCheckoutWithInvoice(checkoutReq);

                case CHECKOUT_ROOM:
                    int checkoutRoomId = (int) request.getData();
                    // Lấy thông tin phòng hiện tại
                    List<Room> allRooms = roomDAO.getAllRooms();
                    Room roomToCheckout = null;
                    for (Room r : allRooms) {
                        if (r.getId() == checkoutRoomId) {
                            roomToCheckout = r;
                            break;
                        }
                    }

                    if (roomToCheckout == null) {
                        return new Payload(Payload.Action.FAILURE, null, "Không tìm thấy phòng");
                    }

                    // Kiểm tra phòng có đang được thuê không
                    if (!"OCCUPIED".equals(roomToCheckout.getStatus())) {
                        return new Payload(Payload.Action.FAILURE, null, "Phòng này không đang được thuê");
                    }

                    // Tìm khách thuê phòng này
                    List<Tenant> allTenants = tenantDAO.getAllTenants();
                    Tenant tenantToCheckout = null;
                    for (Tenant t : allTenants) {
                        if (t.getRoomId() == checkoutRoomId) {
                            tenantToCheckout = t;
                            break;
                        }
                    }

                    // Lưu khách vào lịch sử trước khi xóa
                    if (tenantToCheckout != null) {
                        if (!tenantHistoryDAO.addToHistory(tenantToCheckout)) {
                            return new Payload(Payload.Action.FAILURE, null, "Lỗi khi lưu vào lịch sử");
                        }

                        // Xóa khách khỏi bảng Tenants
                        if (!tenantDAO.deleteTenant(tenantToCheckout.getId())) {
                            return new Payload(Payload.Action.FAILURE, null, "Lỗi khi xóa khách");
                        }
                    }

                    // Cập nhật trạng thái phòng thành AVAILABLE
                    roomToCheckout.setStatus("AVAILABLE");
                    if (roomDAO.updateRoom(roomToCheckout)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Trả phòng thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Cập nhật trạng thái phòng thất bại");
                    }

                case CHECKIN_ROOM:
                    int checkinRoomId = (int) request.getData();
                    // Lấy thông tin phòng hiện tại
                    List<Room> allRoomsForCheckin = roomDAO.getAllRooms();
                    Room roomToCheckin = null;
                    for (Room r : allRoomsForCheckin) {
                        if (r.getId() == checkinRoomId) {
                            roomToCheckin = r;
                            break;
                        }
                    }

                    if (roomToCheckin == null) {
                        return new Payload(Payload.Action.FAILURE, null, "Không tìm thấy phòng");
                    }

                    // Kiểm tra phòng có đang được đặt trước không
                    if (!"RESERVED".equals(roomToCheckin.getStatus())) {
                        return new Payload(Payload.Action.FAILURE, null, "Phòng này không đang được đặt trước");
                    }

                    // Cập nhật trạng thái phòng thành OCCUPIED
                    roomToCheckin.setStatus("OCCUPIED");
                    if (roomDAO.updateRoom(roomToCheckin)) {
                        return new Payload(Payload.Action.SUCCESS, null, "Nhận phòng thành công");
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Cập nhật trạng thái phòng thất bại");
                    }

                case GET_TENANT_HISTORY:
                    List<TenantHistory> history = tenantHistoryDAO.getAllHistory();
                    return new Payload(Payload.Action.SUCCESS, history);

                default:
                    return new Payload(Payload.Action.FAILURE, null, "Hành động không xác định");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new Payload(Payload.Action.FAILURE, null, "Lỗi Server: " + e.getMessage());
        }
    }

    // Xử lý trả phòng + tạo hóa đơn
    private Payload handleCheckoutWithInvoice(CheckoutRequest request) {
        try {
            int roomId = request.getRoomId();

            // 1. Lấy thông tin phòng
            Room room = null;
            List<Room> rooms = roomDAO.getAllRooms();
            for (Room r : rooms) {
                if (r.getId() == roomId) {
                    room = r;
                    break;
                }
            }

            if (room == null) {
                return new Payload(Payload.Action.FAILURE, null, "Không tìm thấy phòng");
            }

            if (!"OCCUPIED".equals(room.getStatus())) {
                return new Payload(Payload.Action.FAILURE, null, "Phòng không đang được thuê");
            }

            // 2. Lấy thông tin khách thuê
            Tenant tenant = tenantDAO.getTenantByRoomId(roomId);
            if (tenant == null) {
                return new Payload(Payload.Action.FAILURE, null, "Không tìm thấy khách thuê");
            }

            // 3. Tính số ngày thuê
            Date checkinDate = tenant.getCheckinDate();
            Date checkoutDate = new Date();

            long diffInMillis = checkoutDate.getTime() - checkinDate.getTime();
            int rentalDays = (int) TimeUnit.MILLISECONDS.toDays(diffInMillis);
            if (rentalDays < 1) rentalDays = 1; // Tối thiểu 1 ngày

            // 4. Lấy giá dịch vụ
            double elecPrice = invoiceDAO.getServicePrice("Electricity");
            double waterPrice = invoiceDAO.getServicePrice("Water");
            double internetPrice = invoiceDAO.getServicePrice("Internet");

            // 5. Tính tổng tiền
            double roomCost = room.getPrice() * rentalDays;
            double elecCost = request.getElectricityUsage() * elecPrice;
            double waterCost = request.getWaterUsage() * waterPrice;
            double totalAmount = roomCost + elecCost + waterCost + internetPrice;

            // 6. Tạo hóa đơn
            Invoice invoice = new Invoice();
            invoice.setRoomId(roomId);

            Calendar cal = Calendar.getInstance();
            invoice.setMonth(cal.get(Calendar.MONTH) + 1);
            invoice.setYear(cal.get(Calendar.YEAR));

            invoice.setElectricityUsage(request.getElectricityUsage());
            invoice.setWaterUsage(request.getWaterUsage());
            invoice.setInternetFee(internetPrice);
            invoice.setTotalAmount(totalAmount);
            invoice.setStatus("UNPAID");

            invoice.setRoomPrice(room.getPrice());
            invoice.setRentalDays(rentalDays);
            invoice.setCheckinDate(checkinDate);
            invoice.setCheckoutDate(checkoutDate);

            // 7. Lưu hóa đơn vào database
            if (!invoiceDAO.addInvoice(invoice)) {
                return new Payload(Payload.Action.FAILURE, null, "Lỗi khi tạo hóa đơn");
            }

            // 8. Lưu khách vào lịch sử
            if (!tenantHistoryDAO.addToHistory(tenant)) {
                return new Payload(Payload.Action.FAILURE, null, "Lỗi khi lưu lịch sử");
            }

            // 9. Xóa khách khỏi bảng Tenants
            if (!tenantDAO.deleteTenant(tenant.getId())) {
                return new Payload(Payload.Action.FAILURE, null, "Lỗi khi xóa khách");
            }

            // 10. Cập nhật trạng thái phòng thành AVAILABLE
            room.setStatus("AVAILABLE");
            if (!roomDAO.updateRoom(room)) {
                return new Payload(Payload.Action.FAILURE, null, "Lỗi khi cập nhật phòng");
            }

            // 11. Trả về thông tin hóa đơn
            return new Payload(Payload.Action.SUCCESS, invoice,
                    "Trả phòng thành công!\n" +
                            "Số ngày thuê: " + rentalDays + " ngày\n" +
                            "Tiền phòng: " + String.format("%,.0f VNĐ", roomCost) + "\n" +
                            "Tiền điện: " + String.format("%,.0f VNĐ", elecCost) + "\n" +
                            "Tiền nước: " + String.format("%,.0f VNĐ", waterCost) + "\n" +
                            "Tiền internet: " + String.format("%,.0f VNĐ", internetPrice) + "\n" +
                            "TỔNG CỘNG: " + String.format("%,.0f VNĐ", totalAmount));

        } catch (Exception e) {
            e.printStackTrace();
            return new Payload(Payload.Action.FAILURE, null, "Lỗi: " + e.getMessage());
        }
    }
}