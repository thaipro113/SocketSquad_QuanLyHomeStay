package server.model;

import common.models.Tenant;
import common.models.TenantHistory;
import server.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//update
public class TenantHistoryDAO {

    public boolean addToHistory(Tenant tenant) {
        String query = "INSERT INTO TenantHistory (name, id_card, phone, room_id, contract_path, check_in_date, checkout_date) VALUES (?, ?, ?, ?, ?, ?, GETDATE())";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, tenant.getName());
            stmt.setString(2, tenant.getIdCard());
            stmt.setString(3, tenant.getPhone());
            if (tenant.getRoomId() > 0) {
                stmt.setInt(4, tenant.getRoomId());
            } else {
                stmt.setNull(4, Types.INTEGER);
            }
            stmt.setString(5, tenant.getContractPath());
            stmt.setTimestamp(6,
                    tenant.getCheckInDate() != null ? new java.sql.Timestamp(tenant.getCheckInDate().getTime()) : null);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<TenantHistory> getAllHistory() {
        List<TenantHistory> history = new ArrayList<>();
        String query = "SELECT * FROM TenantHistory ORDER BY checkout_date DESC";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                history.add(new TenantHistory(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("id_card"),
                        rs.getString("phone"),
                        rs.getInt("room_id"),
                        rs.getString("contract_path"),
                        rs.getTimestamp("check_in_date"),
                        rs.getTimestamp("checkout_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}
