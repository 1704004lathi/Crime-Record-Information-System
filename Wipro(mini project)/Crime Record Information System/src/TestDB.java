public class TestDB {
    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("JDBC Driver Loaded Successfully!");
        } catch (Exception e) {
            System.out.println("Driver NOT Loaded: " + e);
        }
    }
}