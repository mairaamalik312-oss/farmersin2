package database;

import java.sql.Connection;

public class TESTconnection {

    public static void main(String[] args) {

        try {
            Connection connection = DBConnection.getConnection();

            if (connection != null) {
                System.out.println("✅ Database Connected Successfully!");
                connection.close();
            }

        } catch (Exception e) {
            System.out.println("❌ Database Connection Failed!");
            e.printStackTrace();
        }

    }
}