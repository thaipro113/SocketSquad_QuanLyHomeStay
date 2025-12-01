package client.view;

import client.controller.ClientController;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.User;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class MainDashboard extends JFrame {
    private ClientController controller;
    private User currentUser;
    private JTabbedPane tabbedPane;

    // Room Components
    private JTable roomTable;
    private DefaultTableModel roomModel;

    // Tenant Components
    private JTable tenantTable;
    private DefaultTableModel tenantModel;

    // Invoice Components
    private JTable invoiceTable;
    private DefaultTableModel invoiceModel;

    public MainDashboard(ClientController controller, User user) {
        this.controller = controller;
        this.currentUser = user;
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản Lý Homestay - " + currentUser.getRole());
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Set Font
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 14));

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 16));

        tabbedPane.addTab("Quản Lý Phòng", createRoomPanel());
        tabbedPane.addTab("Khách Thuê", createTenantPanel());
        tabbedPane.addTab("Hóa Đơn & Chi Phí", createInvoicePanel());

        add(tabbedPane);
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnAdd = new JButton("Thêm Phòng");
        JButton btnEdit = new JButton("Sửa Phòng");
        JButton btnDelete = new JButton("Xóa Phòng");

        btnRefresh.addActionListener(e -> loadRooms());
        btnAdd.addActionListener(e -> showAddRoomDialog());
        btnEdit.addActionListener(e -> showEditRoomDialog());
        btnDelete.addActionListener(e -> deleteRoom());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Tên Phòng", "Trạng Thái", "Giá", "Hình Ảnh" };
        roomModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                if (column == 4)
                    return ImageIcon.class;
                return Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        roomTable = new JTable(roomModel);
        roomTable.setRowHeight(100);
        roomTable.getColumnModel().getColumn(4).setCellRenderer(new ImageRenderer());

        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        loadRooms();
        return panel;
    }

    private JPanel createTenantPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnAdd = new JButton("Thêm Khách");
        JButton btnEdit = new JButton("Sửa Khách");
        JButton btnDelete = new JButton("Xóa Khách");

        btnRefresh.addActionListener(e -> loadTenants());
        btnAdd.addActionListener(e -> showAddTenantDialog());
        btnEdit.addActionListener(e -> showEditTenantDialog());
        btnDelete.addActionListener(e -> deleteTenant());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnAdd);
        toolBar.add(btnEdit);
        toolBar.add(btnDelete);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng" };
        tenantModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tenantTable = new JTable(tenantModel);
        panel.add(new JScrollPane(tenantTable), BorderLayout.CENTER);

        loadTenants();
        return panel;
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        JButton btnRefresh = new JButton("Làm mới");
        JButton btnCalc = new JButton("Tính Tiền Điện Nước");
        JButton btnDelete = new JButton("Xóa Hóa Đơn");

        btnRefresh.addActionListener(e -> loadInvoices());
        btnCalc.addActionListener(e -> showCalculateCostDialog());
        btnDelete.addActionListener(e -> deleteInvoice());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnCalc);
        toolBar.add(btnDelete);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "ID Phòng", "Tháng/Năm", "Tổng Tiền", "Trạng Thái" };
        invoiceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        invoiceTable = new JTable(invoiceModel);
        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);

        loadInvoices();
        return panel;
    }

    private void loadRooms() {
        List<Room> rooms = controller.getRooms();
        roomModel.setRowCount(0);
        if (rooms != null) {
            for (Room r : rooms) {
                ImageIcon imageIcon = null;
                if (r.getImagePath() != null && !r.getImagePath().isEmpty()) {
                    byte[] imgBytes = controller.getImage(r.getImagePath());
                    if (imgBytes != null) {
                        ImageIcon rawIcon = new ImageIcon(imgBytes);
                        Image img = rawIcon.getImage().getScaledInstance(120, 90, Image.SCALE_SMOOTH);
                        imageIcon = new ImageIcon(img);
                    }
                }

                // Map DB status to Vietnamese display
                String displayStatus = "TRỐNG";
                if ("OCCUPIED".equals(r.getStatus()))
                    displayStatus = "ĐANG THUÊ";
                else if ("RESERVED".equals(r.getStatus()))
                    displayStatus = "ĐÃ ĐẶT";

                roomModel.addRow(new Object[] {
                        r.getId(),
                        r.getName(),
                        displayStatus,
                        String.format("%,.0f VNĐ", r.getPrice()),
                        imageIcon
                });
            }
        }
    }

    private void loadTenants() {
        List<Tenant> tenants = controller.getTenants();
        tenantModel.setRowCount(0);
        if (tenants != null) {
            for (Tenant t : tenants) {
                tenantModel.addRow(new Object[] { t.getId(), t.getName(), t.getIdCard(), t.getPhone(), t.getRoomId() });
            }
        }
    }

    private void loadInvoices() {
        List<Invoice> invoices = controller.getInvoices();
        invoiceModel.setRowCount(0);
        if (invoices != null) {
            for (Invoice i : invoices) {
                invoiceModel.addRow(new Object[] {
                        i.getId(),
                        i.getRoomId(),
                        i.getMonth() + "/" + i.getYear(),
                        String.format("%,.0f VNĐ", i.getTotalAmount()),
                        i.getStatus()
                });
            }
        }
    }

    private void showAddRoomDialog() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JButton btnUpload = new JButton("Chọn Ảnh");
        JLabel lblImage = new JLabel("Chưa chọn ảnh");
        final String[] uploadedPath = { "" };

        btnUpload.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                lblImage.setText(file.getName());
                try {
                    byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    String serverPath = controller.uploadFile(file.getName(), fileBytes);
                    if (serverPath != null) {
                        uploadedPath[0] = serverPath;
                        JOptionPane.showMessageDialog(this, "Tải ảnh thành công!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Tải ảnh thất bại.");
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object[] message = {
                "Tên Phòng:", nameField,
                "Giá:", priceField,
                "Hình Ảnh:", btnUpload,
                lblImage
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Phòng Mới", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room();
                room.setName(nameField.getText());
                room.setPrice(Double.parseDouble(priceField.getText()));
                room.setStatus("AVAILABLE"); // Default status
                room.setImagePath(uploadedPath[0]);

                if (controller.addRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Thêm phòng thành công!");
                    loadRooms();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm phòng thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Định dạng giá không hợp lệ.");
            }
        }
    }

    private void showEditRoomDialog() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để sửa.");
            return;
        }

        int roomId = (int) roomModel.getValueAt(selectedRow, 0);
        String currentName = (String) roomModel.getValueAt(selectedRow, 1);
        String currentPriceStr = (String) roomModel.getValueAt(selectedRow, 3);
        double currentPrice = Double.parseDouble(currentPriceStr.replace(" VNĐ", "").replace(",", ""));

        // Note: We don't edit status here typically, but we can if needed.
        // For now, let's allow editing Name, Price, and Image. Status is managed via
        // Tenants.

        JTextField nameField = new JTextField(currentName);
        JTextField priceField = new JTextField(String.valueOf(currentPrice));
        JButton btnUpload = new JButton("Chọn Ảnh Mới");
        JLabel lblImage = new JLabel("Giữ ảnh cũ");
        final String[] uploadedPath = { null }; // Null means keep old image

        btnUpload.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File file = fileChooser.getSelectedFile();
                lblImage.setText(file.getName());
                try {
                    byte[] fileBytes = java.nio.file.Files.readAllBytes(file.toPath());
                    String serverPath = controller.uploadFile(file.getName(), fileBytes);
                    if (serverPath != null) {
                        uploadedPath[0] = serverPath;
                        JOptionPane.showMessageDialog(this, "Tải ảnh thành công!");
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object[] message = {
                "Tên Phòng:", nameField,
                "Giá:", priceField,
                "Hình Ảnh:", btnUpload,
                lblImage
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Sửa Phòng", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room();
                room.setId(roomId);
                room.setName(nameField.getText());
                room.setPrice(Double.parseDouble(priceField.getText()));
                // Keep old status, we need to fetch it or store it in table model hidden column
                // For simplicity, let's assume we don't change status here or we fetch it.
                // Actually, updateRoom in DAO usually updates all fields.
                // We should probably get the full room object first.
                // But since we don't have getRoom(id), let's just use what we have.
                // Wait, if we send null status, it might break.
                // Let's assume the user doesn't change status here.
                // We need to pass the current status back.
                // Let's grab the display status and convert back.
                String displayStatus = (String) roomModel.getValueAt(selectedRow, 2);
                String dbStatus = "AVAILABLE";
                if ("ĐANG THUÊ".equals(displayStatus))
                    dbStatus = "OCCUPIED";
                else if ("ĐÃ ĐẶT".equals(displayStatus))
                    dbStatus = "RESERVED";
                room.setStatus(dbStatus);

                if (uploadedPath[0] != null) {
                    room.setImagePath(uploadedPath[0]);
                } else {
                    // We need to keep the old path.
                    // Since we don't have it in the table (only ImageIcon), we might lose it.
                    // Ideally, we should store the Room object in the table model or fetch it.
                    // For now, let's try to not update image path if it's null in DAO?
                    // Or we can't without changing DAO.
                    // Let's assume we can't easily keep the old image without fetching.
                    // Workaround: Store Room object in a hidden column or list.
                    // Let's just warn the user or assume they re-upload if they want to change.
                    // Actually, let's try to find the room in the list we loaded.
                    // We need to store the list of rooms.
                    List<Room> rooms = controller.getRooms();
                    for (Room r : rooms) {
                        if (r.getId() == roomId) {
                            room.setImagePath(r.getImagePath());
                            break;
                        }
                    }
                    if (uploadedPath[0] != null)
                        room.setImagePath(uploadedPath[0]);
                }

                if (controller.updateRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật phòng thành công!");
                    loadRooms();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật phòng thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Định dạng giá không hợp lệ.");
            }
        }
    }

    private void deleteRoom() {
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng để xóa.");
            return;
        }

        int roomId = (int) roomModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa phòng này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteRoom(roomId)) {
                JOptionPane.showMessageDialog(this, "Xóa phòng thành công!");
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa phòng thất bại.");
            }
        }
    }

    private void showAddTenantDialog() {
        JTextField nameField = new JTextField();
        JTextField idCardField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField roomIdField = new JTextField();

        JRadioButton rbtnReserve = new JRadioButton("Đặt trước");
        JRadioButton rbtnCheckIn = new JRadioButton("Vào ở ngay");
        ButtonGroup group = new ButtonGroup();
        group.add(rbtnReserve);
        group.add(rbtnCheckIn);
        rbtnCheckIn.setSelected(true); // Default

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(rbtnReserve);
        statusPanel.add(rbtnCheckIn);

        Object[] message = {
                "Họ Tên:", nameField,
                "CMND/CCCD:", idCardField,
                "Số Điện Thoại:", phoneField,
                "ID Phòng:", roomIdField,
                "Trạng Thái:", statusPanel
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Khách Thuê", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Tenant tenant = new Tenant();
                tenant.setName(nameField.getText());
                tenant.setIdCard(idCardField.getText());
                tenant.setPhone(phoneField.getText());
                int roomId = Integer.parseInt(roomIdField.getText());
                tenant.setRoomId(roomId);
                tenant.setContractPath("");

                if (controller.addTenant(tenant)) {
                    // Update Room Status based on selection
                    String newStatus = rbtnCheckIn.isSelected() ? "OCCUPIED" : "RESERVED";

                    // We need to update the room status.
                    // We need to fetch the room first to preserve other fields?
                    // Or just create a dummy room with ID and Status if DAO supports partial
                    // update?
                    // DAO updateRoom updates all fields. So we must fetch first.
                    List<Room> rooms = controller.getRooms();
                    Room targetRoom = null;
                    for (Room r : rooms) {
                        if (r.getId() == roomId) {
                            targetRoom = r;
                            break;
                        }
                    }

                    if (targetRoom != null) {
                        targetRoom.setStatus(newStatus);
                        controller.updateRoom(targetRoom);
                    }

                    JOptionPane.showMessageDialog(this, "Thêm khách thành công!");
                    loadTenants();
                    loadRooms(); // Refresh room list to show new status
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm khách thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID Phòng không hợp lệ.");
            }
        }
    }

    private void showEditTenantDialog() {
        int selectedRow = tenantTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách để sửa.");
            return;
        }

        int tenantId = (int) tenantModel.getValueAt(selectedRow, 0);
        String currentName = (String) tenantModel.getValueAt(selectedRow, 1);
        String currentIdCard = (String) tenantModel.getValueAt(selectedRow, 2);
        String currentPhone = (String) tenantModel.getValueAt(selectedRow, 3);
        int currentRoomId = (int) tenantModel.getValueAt(selectedRow, 4);

        JTextField nameField = new JTextField(currentName);
        JTextField idCardField = new JTextField(currentIdCard);
        JTextField phoneField = new JTextField(currentPhone);
        JTextField roomIdField = new JTextField(String.valueOf(currentRoomId));

        Object[] message = {
                "Họ Tên:", nameField,
                "CMND/CCCD:", idCardField,
                "Số Điện Thoại:", phoneField,
                "ID Phòng:", roomIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Sửa Khách Thuê", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Tenant tenant = new Tenant();
                tenant.setId(tenantId);
                tenant.setName(nameField.getText());
                tenant.setIdCard(idCardField.getText());
                tenant.setPhone(phoneField.getText());
                tenant.setRoomId(Integer.parseInt(roomIdField.getText()));
                tenant.setContractPath("");

                if (controller.updateTenant(tenant)) {
                    JOptionPane.showMessageDialog(this, "Cập nhật khách thành công!");
                    loadTenants();
                } else {
                    JOptionPane.showMessageDialog(this, "Cập nhật khách thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID Phòng không hợp lệ.");
            }
        }
    }

    private void deleteTenant() {
        int selectedRow = tenantTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn khách để xóa.");
            return;
        }

        int tenantId = (int) tenantModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteTenant(tenantId)) {
                JOptionPane.showMessageDialog(this, "Xóa khách thành công!");
                loadTenants();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa khách thất bại.");
            }
        }
    }

    private void deleteInvoice() {
        int selectedRow = invoiceTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn hóa đơn để xóa.");
            return;
        }

        int invoiceId = (int) invoiceModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa hóa đơn này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteInvoice(invoiceId)) {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thành công!");
                loadInvoices();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa hóa đơn thất bại.");
            }
        }
    }

    private void showCalculateCostDialog() {
        JTextField roomIdField = new JTextField();
        JTextField monthField = new JTextField();
        JTextField yearField = new JTextField();
        JTextField elecField = new JTextField();
        JTextField waterField = new JTextField();

        Object[] message = {
                "ID Phòng:", roomIdField,
                "Tháng:", monthField,
                "Năm:", yearField,
                "Số Điện (kWh):", elecField,
                "Số Nước (m3):", waterField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Tính Tiền Điện Nước", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Invoice invoice = new Invoice();
                invoice.setRoomId(Integer.parseInt(roomIdField.getText()));
                invoice.setMonth(Integer.parseInt(monthField.getText()));
                invoice.setYear(Integer.parseInt(yearField.getText()));
                invoice.setElectricityUsage(Integer.parseInt(elecField.getText()));
                invoice.setWaterUsage(Integer.parseInt(waterField.getText()));

                Invoice result = controller.calculateCost(invoice);
                if (result != null) {
                    JOptionPane.showMessageDialog(this,
                            "Đã tạo hóa đơn! Tổng tiền: " + String.format("%,.0f VNĐ", result.getTotalAmount()));
                    loadInvoices();
                } else {
                    JOptionPane.showMessageDialog(this, "Tính tiền thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Định dạng số không hợp lệ.");
            }
        }
    }

    // Custom Renderer for Images
    private static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = new JLabel();
            if (value instanceof ImageIcon) {
                label.setIcon((ImageIcon) value);
                label.setHorizontalAlignment(JLabel.CENTER);
            } else {
                label.setText("Không có ảnh");
                label.setHorizontalAlignment(JLabel.CENTER);
            }
            return label;
        }
    }
}
