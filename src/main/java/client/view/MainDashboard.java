package client.view;

import client.controller.ClientController;
import com.formdev.flatlaf.FlatLightLaf;
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
import java.util.List;

public class MainDashboard extends JFrame {
    private ClientController controller;
    private User currentUser;
    private JTabbedPane tabbedPane;

    // Room Components
    private JTable roomTable;
    private DefaultTableModel roomModel;
    private JButton btnCheckout;
    private List<Invoice> displayedInvoices;
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

        // Áp dụng giao diện FlatLaf hiện đại
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

        // Font toàn cục
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
        tabbedPane.addTab("   Thống Kê   ", new ImageIcon(), createStatisticsTab());
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
                BorderFactory.createLineBorder(
                        new Color(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), 100), 1),
                new EmptyBorder(15, 20, 15, 20)));

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

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng" };
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
        JButton btnDelete = createStyledButton("Xóa Hóa Đơn", new Color(220, 53, 69));

        btnRefresh.addActionListener(e -> loadInvoices());
        btnDelete.addActionListener(e -> deleteInvoice());

        toolBar.add(btnRefresh);
        toolBar.addSeparator(new Dimension(10, 0));
        toolBar.add(btnDelete);

        String[] columns = { "ID", "ID Phòng", "Tháng/Năm", "Tổng Tiền", "Trạng Thái", "Chi tiết" };
        invoiceModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        invoiceTable = new JTable(invoiceModel);
        styleTable(invoiceTable);

        // Add MouseListener for "Xem chi tiết" (Column 5)
        invoiceTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = invoiceTable.rowAtPoint(evt.getPoint());
                int col = invoiceTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col == 5) { // Column index 5 is "Chi tiết"
                    if (displayedInvoices != null && row < displayedInvoices.size()) {
                        // Double check ID to match
                        int id = (int) invoiceTable.getValueAt(row, 0);
                        Invoice selected = null;
                        for (Invoice inv : displayedInvoices) {
                            if (inv.getId() == id) {
                                selected = inv;
                                break;
                            }
                        }

                        if (selected != null) {
                            // Fetch prices to estimate costs
                            double elecPrice = 3500.0;
                            double waterPrice = 15000.0;
                            try {
                                java.util.Map<String, Double> prices = controller.getServices();
                                if (prices != null) {
                                    elecPrice = prices.getOrDefault("Electricity", 3500.0);
                                    waterPrice = prices.getOrDefault("Water", 15000.0);
                                }
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }

                            double elecCost = selected.getElectricityUsage() * elecPrice;
                            double waterCost = selected.getWaterUsage() * waterPrice;
                            // Room Cost = Total - (Elec + Water + Internet)
                            double roomCost = selected.getTotalAmount() - elecCost - waterCost
                                    - selected.getInternetFee();

                            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
                            String startDateStr = selected.getStartDate() != null ? sdf.format(selected.getStartDate())
                                    : "N/A";
                            String endDateStr = selected.getEndDate() != null ? sdf.format(selected.getEndDate())
                                    : "N/A";

                            String msg = String.format(
                                    "Chi Tiết Hóa Đơn #%d\n--------------------\n" +
                                            "Phòng ID: %d\n" +
                                            "Tháng/Năm: %d/%d\n" +
                                            "Ngày Vào: %s\n" +
                                            "Ngày Ra: %s\n\n" +
                                            "Tiền Phòng: %,.0f VNĐ\n" +
                                            "Điện (%d kWh): %,.0f VNĐ\n" +
                                            "Nước (%d m3): %,.0f VNĐ\n" +
                                            "Internet: %,.0f VNĐ\n\n" +
                                            "Tổng Tiền: %,.0f VNĐ",
                                    selected.getId(), selected.getRoomId(), selected.getMonth(), selected.getYear(),
                                    startDateStr, endDateStr,
                                    roomCost,
                                    selected.getElectricityUsage(), elecCost,
                                    selected.getWaterUsage(), waterCost,
                                    selected.getInternetFee(),
                                    selected.getTotalAmount());
                            JOptionPane.showMessageDialog(MainDashboard.this, msg, "Chi Tiết Hóa Đơn",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });

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

        String[] columns = { "ID", "Họ Tên", "CMND/CCCD", "SĐT", "ID Phòng", "Ngày Nhận Phòng", "Ngày Trả Phòng" };
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

    private JPanel createStatisticsTab() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 20, 20));
        statsGrid.setBackground(Color.WHITE);

        // Placeholder labels - will be updated by loadStatistics
        // Reuse createStatCard but maybe bigger or different colors

        // 1. Doanh Thu
        Object[] rev = createStatCard("Tổng Doanh Thu", "0 VNĐ", PRIMARY, HEADER_COLOR);
        JLabel lblRev = (JLabel) rev[1];
        statsGrid.add((JPanel) rev[0]);

        // 2. Khách Hàng
        Object[] tenant = createStatCard("Tổng Khách Đang Thuê", "0", new Color(33, 150, 243),
                new Color(227, 242, 253));
        JLabel lblTenant = (JLabel) tenant[1];
        statsGrid.add((JPanel) tenant[0]);

        // 3. Khách đã sử dụng (Dựa trên hóa đơn)
        Object[] served = createStatCard("Khách Đã Trả Phòng", "0", new Color(255, 152, 0), new Color(255, 243, 224));
        JLabel lblServed = (JLabel) served[1];
        statsGrid.add((JPanel) served[0]);

        // 4. Tỉ lệ lấp đầy
        Object[] occ = createStatCard("Tỉ Lệ Lấp Đầy", "0%", new Color(233, 30, 99), new Color(252, 228, 236));
        JLabel lblOcc = (JLabel) occ[1];
        statsGrid.add((JPanel) occ[0]);

        panel.add(statsGrid, BorderLayout.CENTER);

        JButton btnRefresh = createStyledButton("Cập Nhật", PRIMARY);
        btnRefresh.addActionListener(e -> {
            java.util.Map<String, Object> stats = controller.getStatistics();
            if (stats != null) {
                double revenue = (double) stats.get("totalRevenue");
                int tenants = (int) stats.get("totalTenants");
                int servedCount = (int) stats.get("totalServed");
                int rooms = (int) stats.get("totalRooms");
                int occupied = (int) stats.get("occupiedRooms");

                lblRev.setText(String.format("%,.0f VNĐ", revenue));
                lblTenant.setText(String.valueOf(tenants));
                lblServed.setText(String.valueOf(servedCount));

                double rate = rooms > 0 ? ((double) occupied / rooms) * 100 : 0;
                lblOcc.setText(String.format("%.1f%%", rate));
            }
        });

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topPanel.setBackground(Color.WHITE);
        topPanel.add(btnRefresh);
        panel.add(topPanel, BorderLayout.NORTH);

        // Initial load
        SwingUtilities.invokeLater(() -> btnRefresh.doClick());

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
                        String.format("%,.0f VNĐ/Ngày", r.getPrice()),
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
            for (Tenant t : tenants) {
                tenantModel.addRow(new Object[] { t.getId(), t.getName(), t.getIdCard(), t.getPhone(), t.getRoomId() });
            }
        }
    }

    private void loadInvoices() {
        displayedInvoices = controller.getInvoices();
        invoiceModel.setRowCount(0);
        if (displayedInvoices != null) {
            for (Invoice i : displayedInvoices) {
                String displayStatus = i.getStatus();
                if ("PAID".equals(displayStatus)) {
                    displayStatus = "ĐÃ THANH TOÁN";
                } else if ("UNPAID".equals(displayStatus)) {
                    displayStatus = "CHƯA THANH TOÁN";
                }

                invoiceModel.addRow(new Object[] {
                        i.getId(),
                        i.getRoomId(),
                        i.getMonth() + "/" + i.getYear(),
                        String.format("%,.0f VNĐ", i.getTotalAmount()),
                        displayStatus,
                        "Xem chi tiết"
                });
            }
        }
    }

    private void loadHistory() {
        List<TenantHistory> history = controller.getTenantHistory();
        historyModel.setRowCount(0);
        if (history != null) {
            for (TenantHistory t : history) {
                String checkoutDateStr = "";
                String checkInDateStr = "";
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

                if (t.getCheckoutDate() != null) {
                    checkoutDateStr = sdf.format(t.getCheckoutDate());
                }
                if (t.getCheckInDate() != null) {
                    checkInDateStr = sdf.format(t.getCheckInDate());
                } else {
                    checkInDateStr = "N/A";
                }

                historyModel.addRow(new Object[] {
                        t.getId(),
                        t.getName(),
                        t.getIdCard(),
                        t.getPhone(),
                        t.getRoomId(),
                        checkInDateStr,
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
            JOptionPane.showMessageDialog(this, "Chỉ có thể trả phòng đang được thuê!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int id = (int) roomModel.getValueAt(row, 0);
        String roomName = (String) roomModel.getValueAt(row, 1);

        // Fetch current room details to get old meters
        // In a real app we might want to fetch fresh data from server,
        // but here we rely on the list we have or fetch specific room if needed.
        // Simplified: Fetch all rooms again or rely on what we have?
        // To be safe and get meters, let's fetch fresh list or assume we have to find
        // it in current list.
        // Detailed room info might not be in the table model (only partial columns).
        // Let's find the room object from controller to get meters.
        List<Room> rooms = controller.getRooms();
        Room currentRoom = null;
        if (rooms != null) {
            for (Room r : rooms) {
                if (r.getId() == id) {
                    currentRoom = r;
                    break;
                }
            }
        }

        if (currentRoom == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy thông tin phòng!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int oldElec = currentRoom.getElectricityMeter();
        int oldWater = currentRoom.getWaterMeter();

        // Dialog Components
        JTextField txtNewElec = new JTextField(10);
        JTextField txtNewWater = new JTextField(10);
        JLabel lblOldElec = new JLabel(String.valueOf(oldElec));
        JLabel lblOldWater = new JLabel(String.valueOf(oldWater));

        // Fetch Service Prices
        java.util.Map<String, Double> prices = controller.getServices();
        double elecPrice = prices != null ? prices.getOrDefault("Electricity", 3500.0) : 3500.0;
        double waterPrice = prices != null ? prices.getOrDefault("Water", 15000.0) : 15000.0;

        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));
        panel.add(new JLabel("Phòng:"));
        panel.add(new JLabel(roomName));

        panel.add(new JLabel("Số điện cũ:"));
        panel.add(lblOldElec);

        panel.add(new JLabel("Số điện mới (" + String.format("%,.0f", elecPrice) + " đ/kWh):"));
        panel.add(txtNewElec);

        panel.add(new JLabel("Số nước cũ:"));
        panel.add(lblOldWater);

        panel.add(new JLabel("Số nước mới (" + String.format("%,.0f", waterPrice) + " đ/m3):"));
        panel.add(txtNewWater);

        // --- ADDED: Show Dates and Estimated Room Cost ---
        // Find tenant to get CheckIn Date
        List<Tenant> tenants = controller.getTenants();
        Tenant currentTenant = null;
        if (tenants != null) {
            for (Tenant t : tenants) {
                if (t.getRoomId() == id) {
                    currentTenant = t;
                    break;
                }
            }
        }

        if (currentTenant != null) {
            java.util.Date checkIn = currentTenant.getCheckInDate();
            if (checkIn == null)
                checkIn = new java.util.Date(); // Fallback
            java.util.Date now = new java.util.Date();

            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            panel.add(new JLabel("Ngày vào:"));
            panel.add(new JLabel(sdf.format(checkIn)));

            panel.add(new JLabel("Ngày ra:"));
            panel.add(new JLabel(sdf.format(now)));

            // Estimated Cost Calculation (Same logic'ish as server)
            long diffInMillies = Math.abs(now.getTime() - checkIn.getTime());
            long diffHours = java.util.concurrent.TimeUnit.HOURS.convert(diffInMillies,
                    java.util.concurrent.TimeUnit.MILLISECONDS);
            long diffDays = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies,
                    java.util.concurrent.TimeUnit.MILLISECONDS);

            double estimatedRoomCost = 0;
            if (diffHours < 24) {
                if (diffHours < 1)
                    diffHours = 1;
                estimatedRoomCost = (currentRoom.getPrice() / 24.0) * diffHours;
            } else {
                if (diffDays == 0)
                    diffDays = 1;
                long extraHours = diffHours % 24;
                estimatedRoomCost = (diffDays * currentRoom.getPrice())
                        + (extraHours * (currentRoom.getPrice() / 24.0));
            }

            panel.add(new JLabel("Tiền phòng ("
                    + (diffHours < 24 ? diffHours + "h" : diffDays + "d " + (diffHours % 24) + "h") + "):"));
            panel.add(new JLabel(String.format("%,.0f VNĐ", estimatedRoomCost)));
        } else {
            panel.add(new JLabel("Thông báo:"));
            panel.add(new JLabel("Không tìm thấy thông tin khách thuê!"));
        }
        // -------------------------------------------------

        int result = JOptionPane.showConfirmDialog(this, panel, "Trả Phòng & Tính Tiền",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String newElecStr = txtNewElec.getText().trim();
                String newWaterStr = txtNewWater.getText().trim();

                if (newElecStr.isEmpty() || newWaterStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ chỉ số điện nước!", "Lỗi",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int newElecVal = Integer.parseInt(newElecStr);
                int newWaterVal = Integer.parseInt(newWaterStr);

                if (newElecVal < oldElec) {
                    JOptionPane.showMessageDialog(this, "Số điện mới không được nhỏ hơn số cũ (" + oldElec + ")!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (newWaterVal < oldWater) {
                    JOptionPane.showMessageDialog(this, "Số nước mới không được nhỏ hơn số cũ (" + oldWater + ")!",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Confirm with estimated calculation?
                // The server does the final calculation, but we can show immediate feedback or
                // just send it.
                // The prompt implies "hiển thị tiền phòng... lúc ấn trả phòng".
                // Since this is the confirmation step, let's send to server.
                // The server response message contains the details.

                common.Payload response = controller.checkoutRoom(id, newElecVal, newWaterVal);
                if (response.getAction() == common.Payload.Action.SUCCESS) {
                    JOptionPane.showMessageDialog(this, response.getMessage(), "Hóa Đơn",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadRooms();
                    loadTenants();
                    loadHistory();
                    loadInvoices();
                } else {
                    JOptionPane.showMessageDialog(this, "Lỗi: " + response.getMessage(), "Thất bại",
                            JOptionPane.ERROR_MESSAGE);
                }

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Chỉ số phải là số nguyên!", "Lỗi", JOptionPane.ERROR_MESSAGE);
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
            JOptionPane.showMessageDialog(this, "Chỉ có thể nhận phòng đang được đặt trước!", "Lỗi",
                    JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Vui lòng điền đầy đủ thông tin!", "Lỗi",
                        JOptionPane.WARNING_MESSAGE);
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
                JOptionPane.showMessageDialog(this, "Không tìm thấy phòng có ID: " + roomId, "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!"AVAILABLE".equals(targetRoom.getStatus())) {
                String statusVN = "TRỐNG";
                if ("OCCUPIED".equals(targetRoom.getStatus()))
                    statusVN = "ĐANG THUÊ";
                if ("RESERVED".equals(targetRoom.getStatus()))
                    statusVN = "ĐÃ ĐẶT";

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
                JOptionPane.showMessageDialog(this, "Thêm khách thất bại! Vui lòng thử lại.", "Lỗi",
                        JOptionPane.ERROR_MESSAGE);
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
        int roomId = (int) tenantModel.getValueAt(selectedRow, 4); // Lấy roomId từ cột thứ 5 (index 4)

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

                JOptionPane.showMessageDialog(this,
                        "Xóa khách thành công! Phòng " + roomId + " đã được cập nhật trạng thái.");
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