package server.model;

import common.models.Room;
import server.database.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM Rooms";
        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                rooms.add(new Room(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("status"),
                        rs.getDouble("price"),
                        rs.getString("image_path")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    public boolean addRoom(Room room) {
        String query = "INSERT INTO Rooms (name, status, price, image_path) VALUES (?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, room.getName());
            stmt.setString(2, room.getStatus());
            stmt.setDouble(3, room.getPrice());
            stmt.setString(4, room.getImagePath());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateRoom(Room room) {
        String query = "UPDATE Rooms SET name = ?, status = ?, price = ?, image_path = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, room.getName());
            stmt.setString(2, room.getStatus());
            stmt.setDouble(3, room.getPrice());
            stmt.setString(4, room.getImagePath());
            stmt.setInt(5, room.getId());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteRoom(int id) {
        String query = "DELETE FROM Rooms WHERE id = ?";
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
