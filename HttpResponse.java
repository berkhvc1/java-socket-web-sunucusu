import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/** HTTP başlıklarını ve UTF-8 gövdesini çıkış akışına yazar. */
public final class HttpResponse {
    private HttpResponse() { }

    public static void send(OutputStream output, int status, String reason,
                            String html, boolean headOnly) throws IOException {
        byte[] body = html.getBytes(StandardCharsets.UTF_8);
        String headers = "HTTP/1.1 " + status + " " + reason + "\r\n"
                + "Content-Type: text/html; charset=UTF-8\r\n"
                + "Content-Length: " + body.length + "\r\n"
                + "Connection: close\r\n"
                + (status == 405 ? "Allow: GET, HEAD\r\n" : "")
                + "\r\n";
        output.write(headers.getBytes(StandardCharsets.US_ASCII));
        if (!headOnly) {
            output.write(body);
        }
        output.flush();
    }
}
