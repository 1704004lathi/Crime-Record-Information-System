// package com.example.crime;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CrimeDAO {

    // Add a basic case and associated FIR (two statements, transactional)
    public int addCaseWithFir(String title, String desc, String firNumber, Date firDate, String complainant, String firDetails) throws SQLException {
        String insertCase = "INSERT INTO cases(case_title, case_description) VALUES (?, ?)";
        String insertFir = "INSERT INTO fir(case_id, fir_number, fir_date, complainant, details) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement psCase = conn.prepareStatement(insertCase, Statement.RETURN_GENERATED_KEYS)) {
                psCase.setString(1, title);
                psCase.setString(2, desc);
                psCase.executeUpdate();
                ResultSet rs = psCase.getGeneratedKeys();
                if (rs.next()) {
                    int caseId = rs.getInt(1);
                    try (PreparedStatement psFir = conn.prepareStatement(insertFir)) {
                        psFir.setInt(1, caseId);
                        psFir.setString(2, firNumber);
                        psFir.setDate(3, firDate);
                        psFir.setString(4, complainant);
                        psFir.setString(5, firDetails);
                        psFir.executeUpdate();
                    }
                    conn.commit();
                    return caseId;
                } else {
                    conn.rollback();
                    throw new SQLException("Failed to create case");
                }
            } catch (SQLException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void addSuspect(int caseId, String name, Integer age, String address, String status, String notes) throws SQLException {
        String sql = "INSERT INTO suspects(case_id, name, age, address, status, notes) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ps.setString(2, name);
            if (age == null) ps.setNull(3, Types.INTEGER); else ps.setInt(3, age);
            ps.setString(4, address);
            ps.setString(5, status);
            ps.setString(6, notes);
            ps.executeUpdate();
        }
    }

    public void addEvidence(int caseId, String description, String collectedBy, Date collectedDate, String notes) throws SQLException {
        String sql = "INSERT INTO evidence(case_id, description, collected_by, collected_date, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ps.setString(2, description);
            ps.setString(3, collectedBy);
            ps.setDate(4, collectedDate);
            ps.setString(5, notes);
            ps.executeUpdate();
        }
    }

    public void addForensic(int caseId, String testType, String result, String analyst, Date analysisDate, String storageLocation, String notes) throws SQLException {
        String sql = "INSERT INTO forensic(case_id, test_type, result, analyst, analysis_date, storage_location, notes) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, caseId);
            ps.setString(2, testType);
            ps.setString(3, result);
            ps.setString(4, analyst);
            ps.setDate(5, analysisDate);
            ps.setString(6, storageLocation);
            ps.setString(7, notes);
            ps.executeUpdate();
        }
    }

    // Retrieve case + related FIR, suspects, evidence, forensic summary by case id
    public void getCaseById(int caseId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            // Case
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM cases WHERE case_id = ?")) {
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                if (!rs.next()) {
                    System.out.println("No case found with id " + caseId);
                    return;
                }
                System.out.println("=== CASE ===");
                System.out.println("ID: " + rs.getInt("case_id"));
                System.out.println("Title: " + rs.getString("case_title"));
                System.out.println("Desc: " + rs.getString("case_description"));
                System.out.println("Created: " + rs.getTimestamp("created_at"));
            }

            // FIR
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM fir WHERE case_id = ?")) {
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                System.out.println("\n--- FIR(s) ---");
                while (rs.next()) {
                    System.out.println("FIR#: " + rs.getString("fir_number") + " Date: " + rs.getDate("fir_date"));
                    System.out.println("Complainant: " + rs.getString("complainant"));
                    System.out.println("Details: " + rs.getString("details"));
                }
            }

            // Suspects
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM suspects WHERE case_id = ?")) {
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                System.out.println("\n--- Suspects ---");
                while (rs.next()) {
                    System.out.println("Name: " + rs.getString("name") + ", Age: " + rs.getInt("age") + ", Status: " + rs.getString("status"));
                    System.out.println("Notes: " + rs.getString("notes"));
                }
            }

            // Evidence
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM evidence WHERE case_id = ?")) {
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                System.out.println("\n--- Evidence ---");
                while (rs.next()) {
                    System.out.println("Evidence ID: " + rs.getInt("evidence_id") + " Desc: " + rs.getString("description"));
                    System.out.println("Collected by: " + rs.getString("collected_by") + " on " + rs.getDate("collected_date"));
                    System.out.println("Notes: " + rs.getString("notes"));
                }
            }

            // Forensic
            try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM forensic WHERE case_id = ?")) {
                ps.setInt(1, caseId);
                ResultSet rs = ps.executeQuery();
                System.out.println("\n--- Forensic ---");
                while (rs.next()) {
                    System.out.println("Type: " + rs.getString("test_type") + ", Analyst: " + rs.getString("analyst") + ", Date: " + rs.getDate("analysis_date"));
                    System.out.println("Result: " + rs.getString("result"));
                    System.out.println("Store: " + rs.getString("storage_location"));
                    System.out.println("Notes: " + rs.getString("notes"));
                }
            }
        }
    }

    // Search: find cases by suspect name (partial match)
    public List<Integer> searchCaseIdsBySuspectName(String suspectNamePattern) throws SQLException {
        List<Integer> caseIds = new ArrayList<>();
        String sql = "SELECT DISTINCT case_id FROM suspects WHERE name LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + suspectNamePattern + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                caseIds.add(rs.getInt("case_id"));
            }
        }
        return caseIds;
    }

    // List all cases (brief)
    public void listAllCases() throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT case_id, case_title, created_at FROM cases ORDER BY created_at DESC");
             ResultSet rs = ps.executeQuery()) {
            System.out.println("=== ALL CASES ===");
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("case_id") + " | Title: " + rs.getString("case_title") + " | Created: " + rs.getTimestamp("created_at"));
            }
        }
    }
}
