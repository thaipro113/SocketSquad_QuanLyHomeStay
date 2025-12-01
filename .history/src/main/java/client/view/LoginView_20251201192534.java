package client.view;

import client.controller.ClientController;
import common.models.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class LoginView extends JFrame {
    private ClientController controller;
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;

    public LoginView(ClientController controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setTitle("Quản Lý Homestay - Đăng Nhập");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JLabel lblHeader = new JLabel("ĐĂNG NHẬP HỆ THỐNG", SwingConstants.CENTER);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblHeader.setForeground(new Color(50, 50, 150));
        lblHeader.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(lblHeader, BorderLayout.NORTH);

        // Form Panel
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Tên đăng nhập:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        txtUsername = new JTextField("admin");
        panel.add(txtUsername, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        panel.add(new JLabel("Mật khẩu:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        txtPassword = new JPasswordField("admin123");
        panel.add(txtPassword, gbc);

        add(panel, BorderLayout.CENTER);

        // Button Panel
        JPanel btnPanel = new JPanel();
        btnPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 20, 0));
        btnLogin = new JButton("Đăng Nhập");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(50, 150, 50));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.addActionListener(this::onLogin);

        btnPanel.add(btnLogin);
        add(btnPanel, BorderLayout.SOUTH);
    }

    private void onLogin(ActionEvent e) {
        String username = txtUsername.getText();
        String password = new String(txtPassword.getPassword());

        try {
            controller.connect();
            User user = controller.login(username, password);
            if (user != null) {
                JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
                new MainDashboard(controller, user).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Sai thông tin đăng nhập", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Lỗi kết nối: " + ex.getMessage(), "Lỗi", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
}
