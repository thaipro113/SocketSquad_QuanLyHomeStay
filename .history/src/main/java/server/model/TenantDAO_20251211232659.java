package server.model;

import common.models.Tenant;
import server.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TenantDAO {

    public List<Tenant> getAllTenants() {
        List<Tenant> tenants = new ArrayList<>();
        String query = "SELECT * FROM Tenants";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                java.sql.Timestamp checkinTs = rs.getTimestamp("checkin_date");
                java.util.Date checkinDate = (checkinTs != null) ? new java.util.Date(checkinTs.getTime()) : null;

                tenants.add(new Tenant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("id_card"),
                        rs.getString("phone"),
                        rs.getInt("room_id"),
                        rs.getString("contract_path"),
                        checkinDate));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenants;
    }

    public boolean addTenant(Tenant tenant) {
        String query = "INSERT INTO Tenants (name, id_card, phone, room_id, contract_path, checkin_date) VALUES (?, ?, ?, ?, ?, ?)";
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

            if (tenant.getCheckinDate() != null) {
                stmt.setTimestamp(6, new java.sql.Timestamp(tenant.getCheckinDate().getTime()));
            } else {
                stmt.setTimestamp(6, new java.sql.Timestamp(System.currentTimeMillis())); // Default to now if null
            }

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTenant(Tenant tenant) {
        String query = "UPDATE Tenants SET name = ?, id_card = ?, phone = ?, room_id = ?, contract_path = ?, checkin_date = ? WHERE id = ?";
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

            if (tenant.getCheckinDate() != null) {
                stmt.setTimestamp(6, new java.sql.Timestamp(tenant.getCheckinDate().getTime()));
            } else {
                stmt.setNull(6, Types.TIMESTAMP);
            }

            stmt.setInt(7, tenant.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteTenant(int id) {
        String query = "DELETE FROM Tenants WHERE id = ?";
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
