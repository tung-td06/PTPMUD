package dashboard;

import model.Dashboard;
import network.Action;
import network.Module;
import network.Request;
import network.Response;
import network.SocketClient;

public class DashboardData {

    public Dashboard loadDashboard() {
        Request request = new Request(
                Module.DASHBOARD,
                Action.LOAD,
                null
        );

        Response response = SocketClient.getInstance().sendRequest(request);

        if (response != null && response.isSuccess()) {
            return (Dashboard) response.getData();
        }

        return null;
    }
}
