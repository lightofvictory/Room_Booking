package com.user_menu;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Scanner;

public class Reverve_menu {
    private static final String url = "jdbc:mysql://localhost:3306/hotel_db";
    private static final String username = "root";
    private static final String password = "123456789";

    public static void main(String[] args) throws ClassNotFoundException,SQLException{
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }

        try (Connection connect = DriverManager.getConnection(url, username, password)) {
            while (true) {
                System.out.println();
                System.out.println("HOTEL MANAGEMENT SYSTEM");
                Scanner scanner=new Scanner(System.in);
                System.out.println("1. Reserve a Room ");
                System.out.println("2. View Reserved Room");
                System.out.println("3. Get Room Number");
                System.out.println("4. Update Reserve Detail");
                System.out.println("5. Delete Reserved Room");
                System.out.println("0. Exit");
                System.out.print("Choose an option: ");
                int option = scanner.nextInt();

                switch (option) {
                    case 1:
                        reserveRoom(connect, scanner);
                        break;
                    case 2:
                        viewReservedRoom(connect);
                        break;
                    case 3:
                        getRoomNumber(connect, scanner);
                        break;
                    case 4:
                        updateReservation(connect, scanner);
                        break;
                    case 5:
                        deleteReservation(connect, scanner);
                        break;
                    case 0:
                        exit();
                        scanner.close();
                        return;
                    default:
                        System.out.println("INVALID CHOICE! Try again...");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static void reserveRoom(Connection connect, Scanner scanner) {
        try {


            System.out.print("Enter your name: ");
            String name = scanner.next();
            scanner.nextLine(); // Consume the newline
            System.out.print("Enter the room number: ");
            int room = scanner.nextInt();
            System.out.print("Enter the contact number: ");
            String contactNo = scanner.next();

            String sql = "INSERT INTO roombooking (guest_name, room_no, contact_no)"+"VALUES ('"+name+"','"+room+"','"+contactNo+"')";

            try (Statement statement = connect.createStatement()) {

                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation successful!");
                } else {
                    System.out.println("Reservation failed!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewReservedRoom(Connection connection) throws SQLException {
        String sql = "SELECT reservation_id, guest_name, room_no, contact_no, rev_date FROM roombooking";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            System.out.println("Current Reservations:");
            System.out.println("+----------------+----------------+-------------+----------------------+----------------------+");
            System.out.println("| Reservation ID | Guest          | Room Number | Contact Number       | Reservation Date     |");
            System.out.println("+----------------+----------------+-------------+----------------------+----------------------+");

            while (resultSet.next()) {
                int reservationID = resultSet.getInt("reservation_id");
                String guestName = resultSet.getString("guest_name");
                int roomNumber = resultSet.getInt("room_no");
                String contactNumber = resultSet.getString("contact_no");
                String reservationDate = resultSet.getTimestamp("rev_date").toString();

                // Format and display the reservation data in a table-like format
                System.out.printf("| %-14d | %-15s | %-13d | %-20s | %-19s |\n",
                        reservationID, guestName, roomNumber, contactNumber, reservationDate);
            }
            System.out.println("+----------------+----------------+-------------+----------------------+----------------------+");
        }
    }

    private static void getRoomNumber(Connection connection, Scanner scanner) {
        try {
            System.out.print("Enter Reservation ID: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline
            System.out.print("Enter guest name: ");
            String guestName = scanner.nextLine();

            String sql = "SELECT room_no FROM roombooking WHERE reservation_id = "+reservationId+"AND guest_name ="+guestName;

            try (Statement statement = connection.prepareStatement(sql)) {


                try (ResultSet resultSet = statement.executeQuery(sql)) {
                    if (resultSet.next()) {
                        int roomNumber = resultSet.getInt("room_no");
                        System.out.println("Room number for Reservation ID " + reservationId + " and Guest " + guestName + " is: " + roomNumber);
                    } else {
                        System.out.println("Reservation not found for the given ID and guest name.");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateReservation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter Reservation ID to update: ");
            int reservationId = scanner.nextInt();
            scanner.nextLine(); // Consume the newline

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            System.out.println("Enter the new guest name: ");
            String newGuestName = scanner.nextLine();
            System.out.println("Enter the new room number: ");
            int newRoomNumber = scanner.nextInt();
            System.out.println("Enter the new contact number: ");
            String newContactNumber = scanner.next();

            String sql = "UPDATE roombooking SET guest_name = "+newGuestName+" room_no ="+newRoomNumber +" contact_no = "+newContactNumber+"WHERE reservation_id = "+reservationId;

            try (Statement statement = connection.prepareStatement(sql)) {


                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation updated successfully!");
                } else {
                    System.out.println("Reservation update failed!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void deleteReservation(Connection connection, Scanner scanner) {
        try {
            System.out.println("Enter reservation ID to delete: ");
            int reservationId = scanner.nextInt();

            if (!reservationExists(connection, reservationId)) {
                System.out.println("Reservation not found for the given ID.");
                return;
            }

            String sql = "DELETE FROM roombooking WHERE reservation_id = "+reservationId;

            try (Statement statement = connection.prepareStatement(sql)) {

                int affectedRows = statement.executeUpdate(sql);

                if (affectedRows > 0) {
                    System.out.println("Reservation deleted successfully!");
                } else {
                    System.out.println("Reservation deletion failed.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean reservationExists(Connection connection, int reservationId) {
        String sql = "SELECT reservation_id FROM roombooking WHERE reservation_id = "+reservationId;
        try (Statement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery(sql)) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void exit() throws InterruptedException {
        System.out.println("Exiting system");
        int i = 5;
        while (i != 0) {
            System.out.print(".");
            Thread.sleep(450);
            i--;
        }
        System.out.println();
        System.out.println("Thank you for using Hotel Reservation System!");
    }
}
