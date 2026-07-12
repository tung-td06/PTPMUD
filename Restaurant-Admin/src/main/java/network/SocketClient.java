package network; // Nhớ giữ nguyên dòng package này nếu nó trùng với cấu trúc thư mục của bạn

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class SocketClient {

    // Địa chỉ IP Wi-Fi của máy chạy Server và Cổng Port tương ứng
    public static final String SERVER_IP = "192.168.1.7";
    private static final int SERVER_PORT = 9999;

    private static SocketClient instance;
    private Socket socket;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private boolean isConnected = false;

    // Private constructor áp dụng mẫu Singleton để dùng chung 1 kết nối duy nhất
    private SocketClient() {
    }

    // Hàm lấy instance duy nhất của SocketClient
    public static synchronized SocketClient getInstance() {
        if (instance == null) {
            instance = new SocketClient();
        }
        return instance;
    }

    /**
     * Hàm thực hiện kết nối tới Server qua mạng Wi-Fi
     */
    public synchronized boolean connect() {
        if (isConnected) return true;

        try {
            System.out.println("Đang kết nối tới Server tại " + SERVER_IP + ":" + SERVER_PORT + "...");
            
            // Khởi tạo socket dựa trên IP Wifi của máy Server
            this.socket = new Socket(SERVER_IP, SERVER_PORT);
            
            // Khởi tạo luồng Object (Phải khởi tạo output trước input để tránh nghẽn luồng)
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.output.flush();
            this.input = new ObjectInputStream(socket.getInputStream());
            
            this.isConnected = true;
            System.out.println("Kết nối thành công tới Restaurant Server!");
            return true;
        } catch (IOException e) {
            System.err.println("Không thể kết nối Wi-Fi tới Server: " + e.getMessage());
            this.isConnected = false;
            return false;
        }
    }

    /**
     * Hàm gửi dữ liệu Request lên Server và đợi nhận về Object Response
     */
    public synchronized Response sendRequest(Request request) {
        // Tự động kết nối lại nếu bị rớt mạng hoặc chưa kết nối
        if (!isConnected || socket == null || socket.isClosed()) {
            if (!connect()) {
                return new Response(false, "Mất kết nối Wi-Fi. Không thể gửi yêu cầu!", null);
            }
        }

        try {
            // Gửi Object Request lên Server
            output.writeObject(request);
            output.flush();
            output.reset(); // Xóa bộ nhớ đệm Stream để tránh lỗi dữ liệu cũ ở các request sau

            // Chờ và nhận phản hồi tự động từ Server
            Object obj = input.readObject();
            if (obj instanceof Response response) {
                return response;
            } else {
                return new Response(false, "Kiểu dữ liệu phản hồi từ Server không hợp lệ!", null);
            }

        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Lỗi đường truyền thiết bị: " + e.getMessage());
            disconnect(); // Reset lại toàn bộ trạng thái kết nối khi gặp lỗi mạng
            return new Response(false, "Lỗi kết nối mạng: " + e.getMessage(), null);
        }
    }

    /**
     * Hàm đóng kết nối an toàn khi thoát chương trình
     */
    public synchronized void disconnect() {
        try {
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            this.isConnected = false;
            this.socket = null;
            this.output = null;
            this.input = null;
            System.out.println("Đã đóng kết nối SocketClient.");
        }
    }

    public boolean isConnected() {
        return isConnected;
    }
}