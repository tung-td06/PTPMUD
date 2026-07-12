package testClient;

import java.util.List;
import network.Action;
import network.Module;
import network.Request;
import network.Response;

public class TestClient {

    public static void main(String[] args) {

        try {

            SocketClient client = new SocketClient();

            client.connect();

            Request request = new Request(
                    Module.ACCOUNT,
                    Action.LOGIN,
                    List.of("nv002", "123456")
            );

            Response response = client.send(request);

            System.out.println("=========== RESPONSE ===========");
            System.out.println("Success : " + response.isSuccess());
            System.out.println("Message : " + response.getMessage());
            System.out.println("Data    : " + response.getData());
            System.out.println("================================");

            client.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}