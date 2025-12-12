package server.utils;

import server.database.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class MigrationUtil {

    public static void main(String[] args) {
        migrate();
    }

    public static void migrate() {
        System.out.println("Starting Database Migration...");

        try (Connection conn = DBConnection.getConnection();
                Statement stmt = conn.createStatement()) {

            // 1. Add electricity_meter and water_meter to Rooms
            try {
                String sql = "ALTER TABLE Rooms ADD electricity_meter INT DEFAULT 0;";
                stmt.executeUpdate(sql);
                System.out.println("Added electricity_meter column to Rooms.");
            } catch (SQLException e) {
                if (e.getMessage().contains("Column names in each table must be unique")) {
                    System.out.println("electricity_meter column already exists in Rooms.");
                } else {
                    e.printStackTrace();
                }
            }

            try {
                String sql = "ALTER TABLE Rooms ADD water_meter INT DEFAULT 0;";
                stmt.executeUpdate(sql);
                System.out.println("Added water_meter column to Rooms.");
            } catch (SQLException e) {
                if (e.getMessage().contains("Column names in each table must be unique")) {
                    System.out.println("water_meter column already exists in Rooms.");
                } else {
                    e.printStackTrace();
                }
            }

            // 2. Add check_in_date to Tenants
            try {
                String sql = "ALTER TABLE Tenants ADD check_in_date DATETIME DEFAULT GETDATE();";
                stmt.executeUpdate(sql);
                System.out.println("Added check_in_date column to Tenants.");
            } catch (SQLException e) {
                if (e.getMessage().contains("Column names in each table must be unique")) {
                    System.out.println("check_in_date column already exists in Tenants.");
                } else {
                    e.printStackTrace();
                }
            }

            System.out.println("Migration Completed.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
