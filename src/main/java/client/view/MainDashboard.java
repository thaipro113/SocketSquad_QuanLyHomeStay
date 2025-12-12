package client.view;

import client.controller.ClientController;
import com.formdev.flatlaf.FlatLightLaf;
import common.Payload;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.TenantHistory;
import common.models.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class MainDashboard extends JFrame {
    private ClientController controller;
    private User currentUser;
    private JTabbedPane tabbedPane;

    // Room Components
    private JTable roomTable;
    private DefaultTableModel roomModel;
    private JButton btnCheckout;
    private JButton btnCheckin;
    private JLabel lblAvailableCount;
    private JLabel lblOccupiedCount;
    private JLabel lblReservedCount;
    private JLabel lblTotalCount;

    // Tenant Components
    private JTable tenantTable;
    private DefaultTableModel tenantModel;

    // Invoice Components
    private JTable invoiceTable;
    private DefaultTableModel invoiceModel;

    // History Components
    private JTable historyTable;
    private DefaultTableModel historyModel;

    // Màu sắc chủ đạo
    private final Color PRIMARY = new Color(46, 125, 50);
    private final Color PRIMARY_HOVER = new Color(76, 175, 80);
    private final Color LIGHT_BG = new Color(248, 250, 252);
    private final Color HEADER_COLOR = new Color(232, 247, 233);

    public MainDashboard(ClientController controller, User user) {
        this.controller = controller;
        this.currentUser = user;

        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize FlatLaf");
        }

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Homestay Pro - " + currentUser.getRole().toUpperCase());
        setSize(1200, 750);
        setMinimumSize(new Dimension(1000, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TableHeader.font", new Font("Segoe UI", Font.BOLD, 15));
        UIManager.put("TabbedPane.font", new Font("Segoe UI", Font.BOLD, 16));

        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(Color.WHITE);
        tabbedPane.setForeground(PRIMARY);
        tabbedPane.setTabPlacement(JTabbedPane.TOP);

        tabbedPane.addTab("   Quản Lý Phòng   ", new ImageIcon(), createRoomPanel());
        tabbedPane.addTab("   Khách Thuê   ", new ImageIcon(), createTenantPanel());
        tabbedPane.addTab("   Hóa Đơn & Chi Phí   ", new ImageIcon(), createInvoicePanel());
        tabbedPane.addTab("   Lịch Sử   ", new ImageIcon(), createHistoryPanel());

        add(tabbedPane);
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        // Panel thống kê
        JPanel statsPanel = createStatisticsPanel();
        panel.add(statsPanel, BorderLayout.NORTH);

        // Toolbar hiện đại
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

        JButton btnRefresh = createStyledButton("Làm mới", PRIMARY);
        JButton btnAdd = createStyledButton("Thêm Phòng", PRIMARY);
        JButton btnEdit = createStyledButton("Sửa Phòng", PRIMARY);
        JButton btnDelete = createStyledButton("Xóa Phòng", new Color(220, 53, 69));
        btnCheckout = createStyledButton("Trả Phòng", new Color(255, 152, 0));
        btnCheckin = createStyledButton("Nhận Phòng", new Color(33, 150, 243));

        btnRefresh.addActionListener(e -> loadRooms());
        btnAdd.addActionListener(e -> showAddRoomDialog());
        btnEdit.addActionListener(e -> showEditRoomDialog());
        btnDelete.addActionListener(e -> deleteRoom());
        btnCheckout.addActionListener(e -> checkoutRoom());
        btnCheckin.addActionListener(e -> checkinRoom());
        btnCheckout.setEnabled(false); // Mặc định disable
        btnCheckin.setEnabled(false); // Mặc định disable

        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnAdd);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnEdit);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnDelete);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnCheckout);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnCheckin);

        // Table
        String[] columns = { "ID", "Tên Phòng", "Trạng Thái", "Giá", "Hình Ảnh" };
        roomModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 4 ? ImageIcon.class : Object.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(roomModel);
        roomTable.setRowHeight(110);
        roomTable.setShowGrid(false);
        roomTable.setIntercellSpacing(new Dimension(0, 0));
        roomTable.getTableHeader().setBackground(PRIMARY);
        roomTable.getTableHeader().setForeground(Color.WHITE);
        roomTable.getTableHeader().setReorderingAllowed(false);
        roomTable.setSelectionBackground(HEADER_COLOR);
        roomTable.setSelectionForeground(Color.BLACK);
        roomTable.getColumnModel().getColumn(4).setCellRenderer(new ImageRenderer());

        // Thêm listener để enable/disable nút Trả phòng và Nhận phòng khi chọn phòng
        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = roomTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String status = (String) roomModel.getValueAt(selectedRow, 2);
                    btnCheckout.setEnabled("ĐANG THUÊ".equals(status));
                    btnCheckin.setEnabled("ĐÃ ĐẶT".equals(status));
                } else {
                    btnCheckout.setEnabled(false);
                    btnCheckin.setEnabled(false);
                }
            }
        });

        // Zebra striping
        roomTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_BG);
                }
                return c;
            }
        });

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.add(toolBar, BorderLayout.NORTH);
        contentPanel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        panel.add(contentPanel, BorderLayout.CENTER);

        loadRooms();
        return panel;
    }

    private JPanel createStatisticsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        // Card Phòng Trống
        Object[] availableResult = createStatCard("Phòng Trống", "0", new Color(76, 175, 80), new Color(232, 247, 233));
        lblAvailableCount = (JLabel) availableResult[1];
        statsPanel.add((JPanel) availableResult[0]);

        // Card Đang Sử Dụng
        Object[] occupiedResult = createStatCard("Đang Sử Dụng", "0", new Color(244, 67, 54), new Color(255, 235, 238));
        lblOccupiedCount = (JLabel) occupiedResult[1];
        statsPanel.add((JPanel) occupiedResult[0]);

        // Card Đặt Trước
        Object[] reservedResult = createStatCard("Đặt Trước", "0", new Color(255, 152, 0), new Color(255, 243, 224));
        lblReservedCount = (JLabel) reservedResult[1];
        statsPanel.add((JPanel) reservedResult[0]);

        // Card Tổng Số Phòng
        Object[] totalResult = createStatCard("Tổng Số Phòng", "0", PRIMARY, HEADER_COLOR);
        lblTotalCount = (JLabel) totalResult[1];
        statsPanel.add((JPanel) totalResult[0]);

        return statsPanel;
    }

    private Object[] createStatCard(String title, String value, Color titleColor, Color bgColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(bgColor);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), 100), 1),
                new EmptyBorder(15, 20, 15, 20)
        ));

        // Title
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue()));
        titleLabel.setHorizontalAlignment(SwingConstants.LEFT);
        card.add(titleLabel, BorderLayout.NORTH);

        // Value
        JPanel valuePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        valuePanel.setBackground(bgColor);
        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(titleColor);
        valuePanel.add(valueLabel);
        card.add(valuePanel, BorderLayout.CENTER);

        return new Object[] { card, valueLabel };
    }

    private JPanel createTenantPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

        JButton btnRefresh = createStyledButton("Làm mới", PRIMARY);
        JButton btnAdd = createStyledButton("Thêm Khách", PRIMARY);
        JButton btnEdit = createStyledButton("Sửa Khách", PRIMARY);
        JButton btnDelete = createStyledButton("Xóa Khách", new Color(220, 53, 69));

        btnRefresh.addActionListener(e -> loadTenants());
        btnAdd.addActionListener(e -> showAddTenantDialog());
        btnEdit.addActionListener(e -> showEditTenantDialog());
        btnDelete.addActionListener(e -> deleteTenant());

        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnAdd);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnEdit);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnDelete);

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng", "Ngày Bắt Đầu Thuê" };
        tenantModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        tenantTable = new JTable(tenantModel);
        styleTable(tenantTable);

        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(tenantTable), BorderLayout.CENTER);
        loadTenants();
        return panel;
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

        JButton btnRefresh = createStyledButton("Làm mới", PRIMARY);
        JButton btnCalc = createStyledButton("Tính Tiền Điện Nước", PRIMARY);
        JButton btnDelete = createStyledButton("Xóa Hóa Đơn", new Color(220, 53, 69));

        btnRefresh.addActionListener(e -> loadInvoices());
        btnCalc.addActionListener(e -> showCalculateCostDialog());
        btnDelete.addActionListener(e -> deleteInvoice());

        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnCalc);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnDelete);

        String[] columns = { "ID", "ID Phòng", "Tháng/Năm", "Tổng Tiền", "Trạng Thái" };
        invoiceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        invoiceTable = new JTable(invoiceModel);
        styleTable(invoiceTable);

        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(invoiceTable), BorderLayout.CENTER);
        loadInvoices();
        return panel;
    }

    private JPanel createHistoryPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBackground(Color.WHITE);
        toolBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(220, 220, 220)));

        JButton btnRefresh = createStyledButton("Làm mới", PRIMARY);

        btnRefresh.addActionListener(e -> loadHistory());

        toolBar.add(btnRefresh);

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng", "Ngày Bắt Đầu Thuê", "Ngày Trả Phòng" };
        historyModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        historyTable = new JTable(historyModel);
        styleTable(historyTable);

        panel.add(toolBar, BorderLayout.NORTH);
        panel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        loadHistory();
        return panel;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(50);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.getTableHeader().setBackground(PRIMARY);
        table.getTableHeader().setForeground(Color.WHITE);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionBackground(HEADER_COLOR);
        table.setSelectionForeground(Color.BLACK);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : LIGHT_BG);
                }
                return c;
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(140, 40));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(PRIMARY_HOVER);
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });
        return btn;
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
                        Image img = rawIcon.getImage().getScaledInstance(140, 100, Image.SCALE_SMOOTH);
                        imageIcon = new ImageIcon(img);
                    }
                }

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
        updateRoomStatistics(rooms);
    }

    private void updateRoomStatistics(List<Room> rooms) {
        if (lblAvailableCount == null || lblOccupiedCount == null ||
            lblReservedCount == null || lblTotalCount == null) {
            return; // Labels chưa được khởi tạo
        }

        if (rooms == null) {
            lblAvailableCount.setText("0");
            lblOccupiedCount.setText("0");
            lblReservedCount.setText("0");
            lblTotalCount.setText("0");
            return;
        }

        int available = 0;
        int occupied = 0;
        int reserved = 0;
        int total = rooms.size();

        for (Room r : rooms) {
            String status = r.getStatus();
            if ("AVAILABLE".equals(status)) {
                available++;
            } else if ("OCCUPIED".equals(status)) {
                occupied++;
            } else if ("RESERVED".equals(status)) {
                reserved++;
            }
        }

        lblAvailableCount.setText(String.valueOf(available));
        lblOccupiedCount.setText(String.valueOf(occupied));
        lblReservedCount.setText(String.valueOf(reserved));
        lblTotalCount.setText(String.valueOf(total));
    }

    private void loadTenants() {
        List<Tenant> tenants = controller.getTenants();
        tenantModel.setRowCount(0);
        if (tenants != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (Tenant t : tenants) {
                String checkinDateStr = "";
                if (t.getCheckinDate() != null) {
                    checkinDateStr = sdf.format(t.getCheckinDate());
                }
                tenantModel.addRow(new Object[] {
                        t.getId(),
                        t.getName(),
                        t.getIdCard(),
                        t.getPhone(),
                        t.getRoomId(),
                        checkinDateStr
                });
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

    private void loadHistory() {
        List<TenantHistory> history = controller.getTenantHistory();
        historyModel.setRowCount(0);
        if (history != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            for (TenantHistory t : history) {
                String checkinDateStr = "";
                String checkoutDateStr = "";

                if (t.getCheckinDate() != null) {
                    checkinDateStr = sdf.format(t.getCheckinDate());
                }
                if (t.getCheckoutDate() != null) {
                    checkoutDateStr = sdf.format(t.getCheckoutDate());
                }

                historyModel.addRow(new Object[] {
                        t.getId(),
                        t.getName(),
                        t.getIdCard(),
                        t.getPhone(),
                        t.getRoomId(),
                        checkinDateStr,
                        checkoutDateStr
                });
            }
        }
    }

    private void showAddRoomDialog() {
        JTextField nameField = new JTextField(20);
        JTextField priceField = new JTextField(20);
        JButton btnUpload = new JButton("Chọn Ảnh");
        JLabel lblImage = new JLabel("Chưa chọn ảnh");
        final String[] uploadedPath = { "" };

        btnUpload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File f = fc.getSelectedFile();
                lblImage.setText(f.getName());
                try {
                    byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                    String path = controller.uploadFile(f.getName(), bytes);
                    if (path != null) {
                        uploadedPath[0] = path;
                        JOptionPane.showMessageDialog(this, "Tải ảnh thành công!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object[] message = { "Tên Phòng:", nameField, "Giá:", priceField, "Hình Ảnh:", btnUpload, lblImage };
        int opt = JOptionPane.showConfirmDialog(this, message, "Thêm Phòng Mới", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room();
                room.setName(nameField.getText());
                room.setPrice(Double.parseDouble(priceField.getText()));
                room.setStatus("AVAILABLE");
                room.setImagePath(uploadedPath[0]);
                if (controller.addRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Thêm phòng thành công!");
                    loadRooms();
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi: " + ex.getMessage());
            }
        }
    }

    private void showEditRoomDialog() {
        int row = roomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng!");
            return;
        }

        int id = (int) roomModel.getValueAt(row, 0);
        String name = (String) roomModel.getValueAt(row, 1);
        String priceStr = ((String) roomModel.getValueAt(row, 3)).replaceAll("[^0-9]", "");
        double price = Double.parseDouble(priceStr);
        String status = (String) roomModel.getValueAt(row, 2);
        String dbStatus = "AVAILABLE";
        if ("ĐANG THUÊ".equals(status))
            dbStatus = "OCCUPIED";
        else if ("ĐÃ ĐẶT".equals(status))
            dbStatus = "RESERVED";

        JTextField nameField = new JTextField(name, 20);
        JTextField priceField = new JTextField(String.valueOf(price), 20);
        JButton btnUpload = new JButton("Chọn Ảnh Mới");
        JLabel lblImage = new JLabel("Giữ ảnh cũ");
        final String[] newPath = { null };

        btnUpload.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File f = fc.getSelectedFile();
                lblImage.setText(f.getName());
                try {
                    byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
                    String path = controller.uploadFile(f.getName(), bytes);
                    if (path != null)
                        newPath[0] = path;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object[] msg = { "Tên Phòng:", nameField, "Giá:", priceField, "Hình Ảnh:", btnUpload, lblImage };
        int opt = JOptionPane.showConfirmDialog(this, msg, "Sửa Phòng", JOptionPane.OK_CANCEL_OPTION);
        if (opt == JOptionPane.OK_OPTION) {
            Room room = new Room();
            room.setId(id);
            room.setName(nameField.getText());
            room.setPrice(Double.parseDouble(priceField.getText()));
            room.setStatus(dbStatus);

            List<Room> list = controller.getRooms();
            for (Room r : list)
                if (r.getId() == id) {
                    room.setImagePath(r.getImagePath());
                    break;
                }
            if (newPath[0] != null)
                room.setImagePath(newPath[0]);

            if (controller.updateRoom(room)) {
                JOptionPane.showMessageDialog(this, "Cập nhật thành công!");
                loadRooms();
            }
        }
    }

    private void deleteRoom() {
        int row = roomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Chọn phòng để xóa!");
            return;
        }
        int id = (int) roomModel.getValueAt(row, 0);
        if (JOptionPane.showConfirmDialog(this, "Xóa phòng này?", "Xác nhận",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (controller.deleteRoom(id)) {
                JOptionPane.showMessageDialog(this, "Xóa thành công!");
                loadRooms();
            }
        }
    }

    private void checkoutRoom() {
        int row = roomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng!");
            return;
        }

        String status = (String) roomModel.getValueAt(row, 2);
        if (!"ĐANG THUÊ".equals(status)) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể trả phòng đang được thuê!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int roomId = (int) roomModel.getValueAt(row, 0);
        String roomName = (String) roomModel.getValueAt(row, 1);

        //TẠO DIALOG NHẬP SỐ ĐIỆN/NƯỚC
        JTextField electricField = new JTextField("0");
        JTextField waterField = new JTextField("0");

        Object[] message = {
                "Phòng: " + roomName + " (ID: " + roomId + ")",
                " ",
                "Số điện (kWh):", electricField,
                "Số nước (m³):", waterField
        };

        int option = JOptionPane.showConfirmDialog(
                this,
                message,
                "Trả Phòng & Tạo Hóa Đơn",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (option == JOptionPane.OK_OPTION) {
            try {
                int electric = Integer.parseInt(electricField.getText().trim());
                int water = Integer.parseInt(waterField.getText().trim());

                if (electric < 0 || water < 0) {
                    JOptionPane.showMessageDialog(this, "Số điện và nước phải >= 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // ✅ GỌI API MỚI
                Payload response = controller.checkoutRoomWithInvoice(roomId, electric, water);

                if (response.getAction() == Payload.Action.SUCCESS) {
                    Invoice invoice = (Invoice) response.getData();

                    // ✅ HIỂN THỊ CHI TIẾT HÓA ĐƠN
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                    String invoiceDetails = String.format(
                            "✅ TRẢ PHÒNG THÀNH CÔNG!\n\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "📋 CHI TIẾT HÓA ĐƠN\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                                    "🏠 Phòng: %s (ID: %d)\n" +
                                    "📅 Check-in: %s\n" +
                                    "📅 Check-out: %s\n" +
                                    "⏱️  Số ngày thuê: %d ngày\n\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "💰 CHI PHÍ\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n" +
                                    "Tiền phòng: %,d × %,.0f = %,.0f VNĐ\n" +
                                    "Tiền điện: %d kWh × 3,500 = %,.0f VNĐ\n" +
                                    "Tiền nước: %d m³ × 20,000 = %,.0f VNĐ\n" +
                                    "Tiền internet: %,.0f VNĐ\n\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                                    "💵 TỔNG CỘNG: %,.0f VNĐ\n" +
                                    "━━━━━━━━━━━━━━━━━━━━━━━━━━━━",

                            roomName,
                            roomId,
                            sdf.format(invoice.getCheckinDate()),
                            sdf.format(invoice.getCheckoutDate()),
                            invoice.getRentalDays(),
                            invoice.getRentalDays(),
                            invoice.getRoomPrice(),
                            invoice.getRoomPrice() * invoice.getRentalDays(),
                            invoice.getElectricityUsage(),
                            invoice.getElectricityUsage() * 3500.0,
                            invoice.getWaterUsage(),
                            invoice.getWaterUsage() * 20000.0,
                            invoice.getInternetFee(),
                            invoice.getTotalAmount()
                    );

                    JOptionPane.showMessageDialog(
                            this,
                            invoiceDetails,
                            "Hóa Đơn Trả Phòng",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    loadRooms();
                    loadTenants();
                    loadInvoices();
                    loadHistory();

                } else {
                    JOptionPane.showMessageDialog(
                            this,
                            "Lỗi: " + response.getMessage(),
                            "Trả Phòng Thất Bại",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Vui lòng nhập số hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void checkinRoom() {
        int row = roomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn phòng!");
            return;
        }

        String status = (String) roomModel.getValueAt(row, 2);
        if (!"ĐÃ ĐẶT".equals(status)) {
            JOptionPane.showMessageDialog(this, "Chỉ có thể nhận phòng đang được đặt trước!", "Lỗi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) roomModel.getValueAt(row, 0);
        String roomName = (String) roomModel.getValueAt(row, 1);
        
        int confirm = JOptionPane.showConfirmDialog(this, 
                "Bạn có chắc muốn nhận phòng \"" + roomName + "\" (ID: " + id + ")?\n" +
                "Trạng thái phòng sẽ chuyển từ ĐÃ ĐẶT sang ĐANG THUÊ.", 
                "Xác nhận nhận phòng",
                JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.checkinRoom(id)) {
                JOptionPane.showMessageDialog(this, "Nhận phòng thành công! Phòng đã được cập nhật trạng thái.", 
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Nhận phòng thất bại. Vui lòng thử lại.", 
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showAddTenantDialog() {
        JTextField nameField = new JTextField(20);
        JTextField idCardField = new JTextField(20);
        JTextField phoneField = new JTextField(20);
        JTextField roomIdField = new JTextField(20);

        JRadioButton rbtnReserve = new JRadioButton("Đặt trước");
        JRadioButton rbtnCheckIn = new JRadioButton("Vào ở ngay");
        ButtonGroup group = new ButtonGroup();
        group.add(rbtnReserve);
        group.add(rbtnCheckIn);
        rbtnCheckIn.setSelected(true);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.add(new JLabel("Trạng thái:"));
        statusPanel.add(rbtnCheckIn);
        statusPanel.add(rbtnReserve);

        Object[] message = {
                "Họ Tên:", nameField,
                "CMND/CCCD:", idCardField,
                "Số Điện Thoại:", phoneField,
                "ID Phòng:", roomIdField,
                " ", statusPanel
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Thêm Khách Thuê Mới", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String idCard = idCardField.getText().trim();
            String phone = phoneField.getText().trim();
            String roomIdStr = roomIdField.getText().trim();

            // === KIỂM TRA RỖNG ===
            if (name.isEmpty() || idCard.isEmpty() || phone.isEmpty() || roomIdStr.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // === LẤY DANH SÁCH KHÁCH HIỆN TẠI ĐỂ KIỂM TRA TRÙNG ===
            List<Tenant> existingTenants = controller.getTenants();
            if (existingTenants != null) {
                for (Tenant t : existingTenants) {
                    if (t.getIdCard().equals(idCard)) {
                        JOptionPane.showMessageDialog(this,
                                "CMND/CCCD \"" + idCard + "\" đã được sử dụng bởi khách: " + t.getName(),
                                "Trùng CMND/CCCD", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (t.getPhone().equals(phone)) {
                        JOptionPane.showMessageDialog(this,
                                "Số điện thoại \"" + phone + "\" đã được sử dụng bởi khách: " + t.getName(),
                                "Trùng Số Điện Thoại", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }
            }

            // === KIỂM TRA PHÒNG ===
            int roomId;
            try {
                roomId = Integer.parseInt(roomIdStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "ID Phòng phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            List<Room> rooms = controller.getRooms();
            Room targetRoom = null;
            if (rooms != null) {
                for (Room r : rooms) {
                    if (r.getId() == roomId) {
                        targetRoom = r;
                        break;
                    }
                }
            }

            if (targetRoom == null) {
                JOptionPane.showMessageDialog(this, "Không tìm thấy phòng có ID: " + roomId, "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!"AVAILABLE".equals(targetRoom.getStatus())) {
                String statusVN = "TRỐNG";
                if ("OCCUPIED".equals(targetRoom.getStatus())) statusVN = "ĐANG THUÊ";
                if ("RESERVED".equals(targetRoom.getStatus())) statusVN = "ĐÃ ĐẶT";

                JOptionPane.showMessageDialog(this,
                        "Phòng này hiện đang " + statusVN + "!\nKhông thể thêm khách.",
                        "Phòng Không Trống", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // === TẠO KHÁCH MỚI ===
            Tenant tenant = new Tenant();
            tenant.setName(name);
            tenant.setIdCard(idCard);
            tenant.setPhone(phone);
            tenant.setRoomId(roomId);
            tenant.setContractPath("");
            tenant.setCheckinDate(new java.util.Date());

            if (controller.addTenant(tenant)) {
                // Cập nhật trạng thái phòng
                String newStatus = rbtnCheckIn.isSelected() ? "OCCUPIED" : "RESERVED";
                targetRoom.setStatus(newStatus);
                controller.updateRoom(targetRoom);

                JOptionPane.showMessageDialog(this,
                        "Thêm khách thành công!\nPhòng " + roomId + " đã chuyển sang trạng thái: " +
                                (rbtnCheckIn.isSelected() ? "ĐANG THUÊ" : "ĐÃ ĐẶT"),
                        "Thành công", JOptionPane.INFORMATION_MESSAGE);

                loadTenants();
                loadRooms();
            } else {
                JOptionPane.showMessageDialog(this, "Thêm khách thất bại! Vui lòng thử lại.", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
        int roomId = (int) tenantModel.getValueAt(selectedRow, 4);
        
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc muốn xóa khách này?", "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (controller.deleteTenant(tenantId)) {
                // Cập nhật trạng thái phòng về AVAILABLE
                List<Room> rooms = controller.getRooms();
                if (rooms != null) {
                    for (Room r : rooms) {
                        if (r.getId() == roomId) {
                            r.setStatus("AVAILABLE");
                            controller.updateRoom(r);
                            break;
                        }
                    }
                }
                
                JOptionPane.showMessageDialog(this, "Xóa khách thành công! Phòng " + roomId + " đã được cập nhật trạng thái.");
                loadTenants();
                loadRooms(); // Reload để cập nhật thống kê
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

    // Image Renderer đẹp hơn
    private static class ImageRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(JLabel.CENTER);
            label.setOpaque(true);
            label.setBackground(isSelected ? new Color(232, 247, 233) : Color.WHITE);

            if (value instanceof ImageIcon icon) {
                Image img = icon.getImage().getScaledInstance(140, 100, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(img));
            } else {
                label.setText("Không có ảnh");
                label.setForeground(Color.GRAY);
            }
            return label;
        }
    }
}