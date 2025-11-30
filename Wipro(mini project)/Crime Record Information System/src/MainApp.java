// package com.example.crime;

import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class MainApp {
    private static final CrimeDAO dao = new CrimeDAO();
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.println("Crime Record Information System");
        while (true) {
            printMenu();
            String choice = sc.nextLine().trim();
            try {
                switch (choice) {
                    case "1": createCaseWithFir(); break;
                    case "2": addSuspect(); break;
                    case "3": addEvidence(); break;
                    case "4": addForensic(); break;
                    case "5": searchByCaseId(); break;
                    case "6": searchBySuspectName(); break;
                    case "7": dao.listAllCases(); break;
                    case "0": System.out.println("Bye"); System.exit(0);
                    default: System.out.println("Invalid choice");
                }
            } catch (SQLException e) {
                System.err.println("DB error: " + e.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void printMenu() {
        System.out.println("\nMenu:");
        System.out.println("1. Create case & add FIR");
        System.out.println("2. Add suspect to a case");
        System.out.println("3. Add evidence to a case");
        System.out.println("4. Add forensic record to a case");
        System.out.println("5. View case by Case ID");
        System.out.println("6. Search cases by suspect name");
        System.out.println("7. List all cases");
        System.out.println("0. Exit");
        System.out.print("Choose: ");
    }

    private static void createCaseWithFir() throws SQLException {
        System.out.print("Case title: "); String title = sc.nextLine();
        System.out.print("Case description: "); String desc = sc.nextLine();
        System.out.print("FIR number: "); String firNo = sc.nextLine();
        System.out.print("FIR date (yyyy-mm-dd): "); String firDateStr = sc.nextLine();
        System.out.print("Complainant: "); String complainant = sc.nextLine();
        System.out.print("FIR details: "); String firDetails = sc.nextLine();
        Date firDate = Date.valueOf(firDateStr);
        int caseId = dao.addCaseWithFir(title, desc, firNo, firDate, complainant, firDetails);
        System.out.println("Created case with ID: " + caseId);
    }

    private static void addSuspect() throws SQLException {
        System.out.print("Case ID: "); int caseId = Integer.parseInt(sc.nextLine());
        System.out.print("Suspect name: "); String name = sc.nextLine();
        System.out.print("Age (or blank): "); String ageStr = sc.nextLine();
        Integer age = ageStr.isBlank() ? null : Integer.parseInt(ageStr);
        System.out.print("Address: "); String addr = sc.nextLine();
        System.out.print("Status: "); String status = sc.nextLine();
        System.out.print("Notes: "); String notes = sc.nextLine();
        dao.addSuspect(caseId, name, age, addr, status, notes);
        System.out.println("Suspect added.");
    }

    private static void addEvidence() throws SQLException {
        System.out.print("Case ID: "); int caseId = Integer.parseInt(sc.nextLine());
        System.out.print("Description: "); String desc = sc.nextLine();
        System.out.print("Collected by: "); String by = sc.nextLine();
        System.out.print("Collected date (yyyy-mm-dd): "); String date = sc.nextLine();
        System.out.print("Notes: "); String notes = sc.nextLine();
        dao.addEvidence(caseId, desc, by, Date.valueOf(date), notes);
        System.out.println("Evidence added.");
    }

    private static void addForensic() throws SQLException {
        System.out.print("Case ID: "); int caseId = Integer.parseInt(sc.nextLine());
        System.out.print("Test type (DNA/Fingerprint/etc): "); String t = sc.nextLine();
        System.out.print("Result summary: "); String result = sc.nextLine();
        System.out.print("Analyst: "); String analyst = sc.nextLine();
        System.out.print("Analysis date (yyyy-mm-dd): "); String date = sc.nextLine();
        System.out.print("Storage location: "); String loc = sc.nextLine();
        System.out.print("Notes: "); String notes = sc.nextLine();
        dao.addForensic(caseId, t, result, analyst, Date.valueOf(date), loc, notes);
        System.out.println("Forensic record added.");
    }

    private static void searchByCaseId() throws SQLException {
        System.out.print("Enter case ID: ");
        int id = Integer.parseInt(sc.nextLine());
        dao.getCaseById(id);
    }

    private static void searchBySuspectName() throws SQLException {
        System.out.print("Enter suspect name (partial allowed): ");
        String q = sc.nextLine();
        List<Integer> ids = dao.searchCaseIdsBySuspectName(q);
        if (ids.isEmpty()) {
            System.out.println("No matching cases.");
        } else {
            System.out.println("Matching case IDs: " + ids);
            // optionally show details for each
            for (int id : ids) {
                System.out.println("\n*** Case ID: " + id);
                dao.getCaseById(id);
            }
        }
    }
}
