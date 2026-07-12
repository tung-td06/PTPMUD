package server;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import network.Request;
import network.Response;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Dispatcher dispatcher;

    public ClientHandler(Socket socket) {
        this.socket = socket;
        this.dispatcher = new Dispatcher();
    }

    @Override
    public void run() {
        System.out.println("Handler Started: " + socket.getInetAddress());

        try (socket;
             ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream input = new ObjectInputStream(socket.getInputStream())) {

            output.flush();

            while (true) {
                Object rawRequest = input.readObject();
                Response response;

                if (rawRequest instanceof Request request) {
                    response = dispatcher.handle(request);
                } else {
                    response = new Response(false, "Du lieu gui len khong phai Request", null);
                }

                output.writeObject(response);
                output.flush();
                output.reset();
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } catch (IOException e) {
            System.out.println("Client IO error: " + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("Khong doc duoc class tu client: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("Client handler error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
