import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Bir bağlantıdaki temel HTTP isteğini değerlendirir. */
public class ClientHandler {
    private static final int MAX_HEADER_BYTES = 16384;
    private final PageRenderer renderer;

    // Somut HTML sınıfı yerine arayüz alınır: constructor injection (DIP).
    public ClientHandler(PageRenderer renderer) {
        this.renderer = Objects.requireNonNull(renderer);
    }

    public void handle(Socket client) throws IOException {
        client.setSoTimeout(3000);
        String headers;
        try {
            headers = readHeaders(client.getInputStream());
        } catch (SocketTimeoutException e) {
            reply(client, 408, "Request Timeout", false);
            return;
        } catch (IllegalArgumentException e) {
            reply(client, 431, "Request Header Fields Too Large", false);
            return;
        }
        // Tarayıcının veri göndermeden kapattığı ön bağlantılar atlanır.
        if (headers == null) return;
        String[] lines = headers.split("\r\n");
        String[] request = lines[0].split(" ", -1);
        if (request.length != 3 || !request[0].matches("[A-Z]+")
                || !request[1].startsWith("/")
                || !(request[2].equals("HTTP/1.1") || request[2].equals("HTTP/1.0"))) {
            reply(client, 400, "Bad Request", false);
            return;
        }
        boolean head = request[0].equals("HEAD");
        int hostCount = 0;
        for (int i = 1; i < lines.length; i++) {
            int colon = lines[i].indexOf(':');
            if (colon <= 0) { reply(client, 400, "Bad Request", head); return; }
            if (lines[i].substring(0, colon).equalsIgnoreCase("Host")) {
                hostCount++;
                if (lines[i].substring(colon + 1).isBlank()) {
                    reply(client, 400, "Bad Request", head); return;
                }
            }
        }
        if (hostCount > 1 || (request[2].equals("HTTP/1.1") && hostCount != 1)) {
            reply(client, 400, "Bad Request", head); return;
        }
        if (!request[0].equals("GET") && !head) {
            reply(client, 405, "Method Not Allowed", false);
            return;
        }
        String path = request[1].split("\\?", 2)[0];
        if (!path.equals("/") && !path.equals("/index.html")) {
            reply(client, 404, "Not Found", head);
            return;
        }
        HttpResponse.send(client.getOutputStream(), 200, "OK", renderer.render(), head);
    }

    private String readHeaders(InputStream input) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int previous = 0;
        int current;
        while ((current = input.read()) != -1) {
            buffer.write(current);
            if (buffer.size() > MAX_HEADER_BYTES) throw new IllegalArgumentException();
            previous = (previous << 8) | current;
            if (previous == 0x0D0A0D0A) {
                return buffer.toString(StandardCharsets.ISO_8859_1);
            }
        }
        return buffer.size() == 0 ? null : "INVALID";
    }

    private void reply(Socket client, int status, String reason, boolean head) throws IOException {
        String body = "<!doctype html><html lang=\"en\"><meta charset=\"UTF-8\">"
                + "<title>" + status + "</title><h1>" + status + " " + reason + "</h1></html>";
        HttpResponse.send(client.getOutputStream(), status, reason, body, head);
    }
}
