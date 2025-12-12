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
                tenants.add(new Tenant(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("id_card"),
                        rs.getString("phone"),
                        rs.getInt("room_id"),
                        rs.getString("contract_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tenants;
    }

    public boolean addTenant(Tenant tenant) {
        String query = "INSERT INTO Tenants (name, id_card, phone, room_id, contract_path) VALUES (?, ?, ?, ?, ?)";
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

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateTenant(Tenant tenant) {
        String query = "UPDATE Tenants SET name = ?, id_card = ?, phone = ?, room_id = ?, contract_path = ? WHERE id = ?";
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
            stmt.setInt(6, tenant.getId());

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
