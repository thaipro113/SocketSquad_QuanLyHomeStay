package client.view;

import client.controller.ClientController;
import common.models.Invoice;
import common.models.Room;
import common.models.Tenant;
import common.models.User;

import javax.swing.*;
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
        setTitle("Homestay Management - " + currentUser.getRole());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Rooms", createRoomPanel());
        tabbedPane.addTab("Tenants", createTenantPanel());
        tabbedPane.addTab("Invoices", createInvoicePanel());

        add(tabbedPane);
    }

    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Toolbar
        JToolBar toolBar = new JToolBar();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add Room");

        btnRefresh.addActionListener(e -> loadRooms());
        btnAdd.addActionListener(e -> showAddRoomDialog());

        toolBar.add(btnRefresh);
        toolBar.add(btnAdd);
        panel.add(toolBar, BorderLayout.NORTH);

        // Table
        String[] columns = { "ID", "Name", "Status", "Price", "Image" };
        roomModel = new DefaultTableModel(columns, 0);
        roomTable = new JTable(roomModel);
        panel.add(new JScrollPane(roomTable), BorderLayout.CENTER);

        loadRooms();
        return panel;
    }

    private JPanel createTenantPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnAdd = new JButton("Add Tenant");

        btnRefresh.addActionListener(e -> loadTenants());
        btnAdd.addActionListener(e -> showAddTenantDialog());

        toolBar.add(btnRefresh);
        toolBar.add(btnAdd);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "Name", "ID Card", "Phone", "Room ID" };
        tenantModel = new DefaultTableModel(columns, 0);
        tenantTable = new JTable(tenantModel);
        panel.add(new JScrollPane(tenantTable), BorderLayout.CENTER);

        loadTenants();
        return panel;
    }

    private JPanel createInvoicePanel() {
        JPanel panel = new JPanel(new BorderLayout());

        JToolBar toolBar = new JToolBar();
        JButton btnRefresh = new JButton("Refresh");
        JButton btnCalc = new JButton("Calculate Cost");

        btnRefresh.addActionListener(e -> loadInvoices());
        btnCalc.addActionListener(e -> showCalculateCostDialog());

        toolBar.add(btnRefresh);
        toolBar.add(btnCalc);
        panel.add(toolBar, BorderLayout.NORTH);

        String[] columns = { "ID", "Room ID", "Month/Year", "Total Amount", "Status" };
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
                roomModel
                        .addRow(new Object[] { r.getId(), r.getName(), r.getStatus(), r.getPrice(), r.getImagePath() });
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
                invoiceModel.addRow(new Object[] { i.getId(), i.getRoomId(), i.getMonth() + "/" + i.getYear(),
                        i.getTotalAmount(), i.getStatus() });
            }
        }
    }

    private void showAddRoomDialog() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JComboBox<String> statusBox = new JComboBox<>(new String[] { "AVAILABLE", "OCCUPIED", "RESERVED" });
        JButton btnUpload = new JButton("Select Image");
        JLabel lblImage = new JLabel("No image selected");
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
                        JOptionPane.showMessageDialog(this, "Upload Successful!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Upload Failed.");
                    }
                } catch (java.io.IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Object[] message = {
                "Name:", nameField,
                "Price:", priceField,
                "Status:", statusBox,
                "Image:", btnUpload,
                lblImage
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Room", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Room room = new Room();
                room.setName(nameField.getText());
                room.setPrice(Double.parseDouble(priceField.getText()));
                room.setStatus((String) statusBox.getSelectedItem());
                room.setImagePath(uploadedPath[0]);

                if (controller.addRoom(room)) {
                    JOptionPane.showMessageDialog(this, "Room added!");
                    loadRooms();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add room.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price format.");
            }
        }
    }

    private void showAddTenantDialog() {
        JTextField nameField = new JTextField();
        JTextField idCardField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField roomIdField = new JTextField();

        Object[] message = {
                "Name:", nameField,
                "ID Card:", idCardField,
                "Phone:", phoneField,
                "Room ID:", roomIdField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Add Tenant", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Tenant tenant = new Tenant();
                tenant.setName(nameField.getText());
                tenant.setIdCard(idCardField.getText());
                tenant.setPhone(phoneField.getText());
                tenant.setRoomId(Integer.parseInt(roomIdField.getText()));
                tenant.setContractPath(""); // Placeholder

                if (controller.addTenant(tenant)) {
                    JOptionPane.showMessageDialog(this, "Tenant added!");
                    loadTenants();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to add tenant.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid Room ID.");
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
                "Room ID:", roomIdField,
                "Month:", monthField,
                "Year:", yearField,
                "Electricity (kWh):", elecField,
                "Water (m3):", waterField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Calculate Cost", JOptionPane.OK_CANCEL_OPTION);
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
                    JOptionPane.showMessageDialog(this, "Invoice Created! Total: " + result.getTotalAmount());
                    loadInvoices();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to calculate cost.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid number format.");
            }
        }
    }
}
