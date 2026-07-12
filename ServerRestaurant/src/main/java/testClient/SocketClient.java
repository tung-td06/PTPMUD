package testClient;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import network.Request;
import network.Response;

public class SocketClient {

    private static final String HOST = "localhost";
    private static final int PORT = 9999;

    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;

    public void connect() throws Exception {

        socket = new Socket(HOST, PORT);

        output = new ObjectOutputStream(socket.getOutputStream());
        output.flush();

        input = new ObjectInputStream(socket.getInputStream());

        System.out.println("Connected to Server");
    }

    public Response send(Request request) throws Exception {

        output.writeObject(request);
        output.flush();
        output.reset();

        return (Response) input.readObject();
    }

    public void disconnect() {

        try {

            if (input != null)
                input.close();

            if (output != null)
                output.close();

            if (socket != null)
                socket.close();

            System.out.println("Disconnected");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}