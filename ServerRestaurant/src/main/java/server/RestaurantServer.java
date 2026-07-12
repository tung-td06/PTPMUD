package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class RestaurantServer {

    private static final int PORT = 9999;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

            System.out.println("================================");
            System.out.println(" Restaurant Server Started");
            System.out.println(" Listening on port: " + PORT);
            System.out.println("================================");

            while (true) {

                Socket socket = serverSocket.accept();

                System.out.println(
                        "[CONNECTED] "
                        + socket.getInetAddress()
                        + ":" + socket.getPort());

                new Thread(new ClientHandler(socket)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
