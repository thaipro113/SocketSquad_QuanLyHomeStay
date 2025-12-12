package server.model;

import common.models.Invoice;
import server.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InvoiceDAO {

    public List<Invoice> getAllInvoices() {
        List<Invoice> invoices = new ArrayList<>();
        String query = "SELECT * FROM Invoices";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                invoices.add(new Invoice(
                        rs.getInt("id"),
                        rs.getInt("room_id"),
                        rs.getInt("month"),
                        rs.getInt("year"),
                        rs.getInt("electricity_usage"),
                        rs.getInt("water_usage"),
                        rs.getDouble("internet_fee"),
                        rs.getDouble("total_amount"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return invoices;
    }

    public boolean addInvoice(Invoice invoice) {
        String query = "INSERT INTO Invoices (room_id, month, year, electricity_usage, water_usage, internet_fee, total_amount, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, invoice.getRoomId());
            stmt.setInt(2, invoice.getMonth());
            stmt.setInt(3, invoice.getYear());
            stmt.setInt(4, invoice.getElectricityUsage());
            stmt.setInt(5, invoice.getWaterUsage());
            stmt.setDouble(6, invoice.getInternetFee());
            stmt.setDouble(7, invoice.getTotalAmount());
            stmt.setString(8, invoice.getStatus());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Helper to get unit prices
    public double getServicePrice(String serviceName) {
        String query = "SELECT unit_price FROM Services WHERE name = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, serviceName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("unit_price");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public java.util.Map<String, Double> getAllServices() {
        java.util.Map<String, Double> services = new java.util.HashMap<>();
        String query = "SELECT name, unit_price FROM Services";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                services.put(rs.getString("name"), rs.getDouble("unit_price"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return services;
    }

    public boolean deleteInvoice(int id) {
        String query = "DELETE FROM Invoices WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
