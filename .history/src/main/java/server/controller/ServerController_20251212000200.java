package server.controller;

import common.Payload;
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
import java.util.List;

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

                case CHECKOUT_ROOM: {
                    // Data: Map<String, Object> or generic Map
                    // Expected keys: roomId, electricityMeter, waterMeter
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> checkoutData = (java.util.Map<String, Object>) request.getData();
                    int checkoutRoomId = (int) checkoutData.get("roomId");
                    int newElec = (int) checkoutData.get("electricityMeter");
                    int newWater = (int) checkoutData.get("waterMeter");

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

                    // TÍNH TOÁN CHI PHÍ
                    double totalAmount = 0;
                    int elecUsage = 0;
                    int waterUsage = 0;
                    double roomCost = 0;

                    // 1. Tiền Điện & Nước
                    // Nếu công tơ mới < cũ => có thể do reset công tơ hoặc nhập sai.
                    // Ở đây xử lý đơn giản: nếu nhỏ hơn thì coi như usage = new (reset) hoặc 0.
                    // Tốt nhất là trust user input usage = new - old.
                    int oldElec = roomToCheckout.getElectricityMeter();
                    int oldWater = roomToCheckout.getWaterMeter();

                    elecUsage = newElec - oldElec;
                    if (elecUsage < 0)
                        elecUsage = 0; // Tránh âm

                    waterUsage = newWater - oldWater;
                    if (waterUsage < 0)
                        waterUsage = 0;

                    double elecPrice = invoiceDAO.getServicePrice("Electricity");
                    double waterPrice = invoiceDAO.getServicePrice("Water");
                    double internetPrice = invoiceDAO.getServicePrice("Internet");

                    double electricityCost = elecUsage * elecPrice;
                    double waterCost = waterUsage * waterPrice;

                    // 2. Tiền Phòng
                    java.util.Date now = new java.util.Date();
                    java.util.Date checkIn = null;
                    if (tenantToCheckout != null) {
                        checkIn = tenantToCheckout.getCheckInDate();
                    }
                    if (checkIn == null)
                        checkIn = now; // Fallback

                    long diffInMillies = Math.abs(now.getTime() - checkIn.getTime());
                    long diffHours = java.util.concurrent.TimeUnit.HOURS.convert(diffInMillies,
                            java.util.concurrent.TimeUnit.MILLISECONDS);
                    long diffDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies,
                            java.util.concurrent.TimeUnit.MILLISECONDS);

                    if (diffHours < 24) {
                        // Tính theo giờ: Giá ngày / 24 * số giờ (tối thiểu 1h)
                        if (diffHours < 1)
                            diffHours = 1;
                        roomCost = (roomToCheckout.getPrice() / 24.0) * diffHours;
                    } else {
                        // Tính theo ngày: làm tròn lên nếu quá 12h? Hoặc tính chẵn ngày.
                        // Logic đơn giản: tính theo ngày thực tế (làm tròn lên)
                        if (diffDays == 0)
                            diffDays = 1; // Tối thiểu 1 ngày nếu code logic sai
                        // Nếu dư giờ > 0 thì +1 ngày? Hay tính lẻ?
                        // User yêu cầu "ngày, giờ tùy theo khách".
                        // Cách tính phổ biến: Ngày + Giờ lẻ.
                        // Ví dụ: 1 ngày 5 giờ. = 1 * Giá ngày + 5 * (Giá ngày/24).
                        long extraHours = diffHours % 24;
                        roomCost = (diffDays * roomToCheckout.getPrice())
                                + (extraHours * (roomToCheckout.getPrice() / 24.0));
                    }

                    totalAmount = roomCost + electricityCost + waterCost + internetPrice;

                    // TẠO HÓA ĐƠN
                    Invoice newInvoice = new Invoice();
                    newInvoice.setRoomId(checkoutRoomId);
                    java.time.LocalDate localDate = java.time.LocalDate.now();
                    newInvoice.setMonth(localDate.getMonthValue());
                    newInvoice.setYear(localDate.getYear());
                    newInvoice.setElectricityUsage(elecUsage);
                    newInvoice.setWaterUsage(waterUsage);
                    newInvoice.setInternetFee(internetPrice);
                    newInvoice.setTotalAmount(totalAmount);
                    newInvoice.setStatus("UNPAID");
                    newInvoice.setCreatedAt(now);

                    if (!invoiceDAO.addInvoice(newInvoice)) {
                        return new Payload(Payload.Action.FAILURE, null, "Lỗi khi tạo hóa đơn");
                    }

                    // LƯU LỊCH SỬ KHÁCH
                    if (tenantToCheckout != null) {
                        if (!tenantHistoryDAO.addToHistory(tenantToCheckout)) {
                            return new Payload(Payload.Action.FAILURE, null, "Lỗi khi lưu vào lịch sử");
                        }
                        // Xóa khách khỏi bảng Tenants
                        if (!tenantDAO.deleteTenant(tenantToCheckout.getId())) {
                            return new Payload(Payload.Action.FAILURE, null, "Lỗi khi xóa khách");
                        }
                    }

                    // CẬP NHẬT PHÒNG
                    roomToCheckout.setStatus("AVAILABLE");
                    roomToCheckout.setElectricityMeter(newElec);
                    roomToCheckout.setWaterMeter(newWater);

                    if (roomDAO.updateRoom(roomToCheckout)) {
                        String msg = String.format(
                                "Trả phòng thành công!\nTiền phòng: %,.0f\nĐiện: %,.0f\nNước: %,.0f\nInternet: %,.0f\nTổng cộng: %,.0f",
                                roomCost, electricityCost, waterCost, internetPrice, totalAmount);
                        return new Payload(Payload.Action.SUCCESS, null, msg);
                    } else {
                        return new Payload(Payload.Action.FAILURE, null, "Cập nhật trạng thái phòng thất bại");
                    }
                }

                case GET_SERVICES:
                    // Return map of service prices
                    java.util.Map<String, Double> prices = new java.util.HashMap<>();
                    prices.put("Electricity", invoiceDAO.getServicePrice("Electricity"));
                    prices.put("Water", invoiceDAO.getServicePrice("Water"));
                    prices.put("Internet", invoiceDAO.getServicePrice("Internet"));
                    return new Payload(Payload.Action.SUCCESS, prices);

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
}
