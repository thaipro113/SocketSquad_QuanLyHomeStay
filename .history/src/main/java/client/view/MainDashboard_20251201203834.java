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

        btnRefresh.addActionListener(e -> loadRooms());
        btnAdd.addActionListener(e -> showAddRoomDialog());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnAdd);
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

        btnRefresh.addActionListener(e -> loadTenants());
        btnAdd.addActionListener(e -> showAddTenantDialog());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnAdd);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng" };
        tenantModel = new DefaultTableModel(columns, 0);
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

        btnRefresh.addActionListener(e -> loadInvoices());
        btnCalc.addActionListener(e -> showCalculateCostDialog());

        toolBar.add(btnRefresh);
        toolBar.addSeparator();
        toolBar.add(btnCalc);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "ID Phòng", "Tháng/Năm", "Tổng Tiền", "Trạng Thái" };
        invoiceModel = new DefaultTableModel(columns, 0);
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

                roomModel.addRow(new Object[] {
                        r.getId(),
                        r.getName(),
                        r.getStatus(),
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
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "AVAILABLE", "OCCUPIED", "RESERVED" });
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
                "Trạng Thái:", statusBox,
                "Hình Ảnh:", btnUpload,
                lblImage
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Phòng Mới", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room();
                room.setName(nameField.getText());
                room.setPrice(Double.parseDouble(priceField.getText()));
                room.setStatus((String) statusBox.getSelectedItem());
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

    private void showAddTenantDialog() {
        JTextField nameField = new JTextField();
        JTextField idCardField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField roomIdField = new JTextField();

        Object[] message = {
                "Họ Tên:", nameField,
                "CMND/CCCD:", idCardField,
                "Số Điện Thoại:", phoneField,
                "ID Phòng:", roomIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Thêm Khách Thuê", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Tenant tenant = new Tenant();
                tenant.setName(nameField.getText());
                tenant.setIdCard(idCardField.getText());
                tenant.setPhone(phoneField.getText());
                tenant.setRoomId(Integer.parseInt(roomIdField.getText()));
                tenant.setContractPath(""); // Placeholder

                if (controller.addTenant(tenant)) {
                    JOptionPane.showMessageDialog(this, "Thêm khách thành công!");
                    loadTenants();
                } else {
                    JOptionPane.showMessageDialog(this, "Thêm khách thất bại.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID Phòng không hợp lệ.");
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
