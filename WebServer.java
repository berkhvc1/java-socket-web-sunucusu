import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/** TCP bağlantılarını kabul eder; HTML içeriğini bilmez. */
public class WebServer {
    private final int port;
    private final ClientHandler handler;

    public WebServer(int port, ClientHandler handler) {
        this.port = port;
        this.handler = Objects.requireNonNull(handler);
    }

    public void start() throws IOException {
        // Yerel gösterim için yalnızca bu bilgisayardaki IPv4 loopback adresi.
        try (ServerSocket server = new ServerSocket(port, 50,
                InetAddress.getByName("127.0.0.1"))) {
            System.out.println("Sunucu hazır: http://127.0.0.1:" + port);
            System.out.println("Durdurmak için Ctrl+C kullanın.");
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket client = server.accept()) {
                    handler.handle(client);
                } catch (IOException e) {
                    if (server.isClosed()) throw e;
                    System.err.println("Bağlantı işlenemedi: " + e.getMessage());
                }
            }
        }
    }
}
