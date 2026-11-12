import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Çalışan sunucuyu test eder. JUnit veya başka bir bağımlılık gerektirmez. */
public class ServerIntegrationTest {
    private static int passed = 0;
    private static boolean offline;
    private static final ClientHandler handler = new ClientHandler(new BiographyPageRenderer(
            new StudentProfile("Bekir Berk Kahveci", "OGRENCI_NUMARANIZ", "Kırklareli Üniversitesi")));

    public static void main(String[] args) throws Exception {
        offline = args.length > 0 && args[0].equals("--offline");
        System.out.println(offline ? "MOD: Bellek içi test; gerçek TCP bağlantısı kullanılmaz."
                : "MOD: 127.0.0.1:1989 üzerinde gerçek TCP testi.");
        byte[] home = exchange("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        String html = body(home);
        check(status(home, 200), "Ana sayfa 200");
        check(html.contains("<h1>Bekir Berk Kahveci</h1>"), "Ad soyad h1");
        check(html.contains("<h2>Öğrenci No: "), "Öğrenci numarası h2");
        check(html.contains("Kırklareli Üniversitesi"), "UTF-8 Türkçe karakterler");
        check(header(home, "Content-Length").equals(Integer.toString(bodyBytes(home).length)),
                "Content-Length UTF-8 bayt uzunluğu");
        check(header(home, "Content-Type").contains("charset=UTF-8"), "UTF-8 içerik türü");
        check(status(exchange("GET /index.html HTTP/1.1\r\nHost: localhost\r\n\r\n"), 200),
                "index.html yolu");
        check(status(exchange("GET /?test=1 HTTP/1.1\r\nHost: localhost\r\n\r\n"), 200),
                "Sorgu parametreli ana sayfa");
        byte[] head = exchange("HEAD / HTTP/1.1\r\nHost: localhost\r\n\r\n");
        check(status(head, 200) && bodyBytes(head).length == 0
                && header(head, "Content-Length").equals(header(home, "Content-Length")), "HEAD gövdesiz yanıt");
        check(status(exchange("GET /yok HTTP/1.1\r\nHost: localhost\r\n\r\n"), 404), "Olmayan yol 404");
        byte[] post = exchange("POST / HTTP/1.1\r\nHost: localhost\r\nContent-Length: 0\r\n\r\n");
        check(status(post, 405) && header(post, "Allow").equals("GET, HEAD"), "POST 405 ve Allow");
        check(status(exchange("BOZUK\r\n\r\n"), 400), "Bozuk istek 400");
        check(status(exchange("GET / HTTP/1.1\r\n\r\n"), 400), "Host eksik 400");
        check(status(exchange("GET / HTTP/1.1\r\nHost: a\r\nHost: b\r\n\r\n"), 400), "Çift Host 400");
        check(status(exchange("GET / HTTP/1.0\r\n\r\n"), 200), "HTTP/1.0 temel istek");
        check(status(exchange("GET / HTTP/1.1\r\nHost: localhost\r\nX-Test: "
                + "a".repeat(16400) + "\r\n\r\n"), 431), "Büyük başlık 431");
        check(status(exchange("GET / HTTP/1.1\r\nHost: localhost\r\n"), 408), "Tamamlanmayan başlık 408");
        if (offline) {
            exchange("");
        } else {
            try (Socket unused = new Socket("127.0.0.1", 1989)) { /* Veri göndermeden kapat. */ }
        }
        check(status(exchange("GET / HTTP/1.1\r\nHost: localhost\r\n\r\n"), 200),
                "Boş bağlantı ve hatalardan sonra sunucu çalışıyor");
        StudentProfile profile = new StudentProfile("Ad <&>", "12345", "Türkçe biyografi");
        for (PageRenderer renderer : new PageRenderer[] {
                new BiographyPageRenderer(profile), new SimplePageRenderer(profile) }) {
            String page = renderer.render();
            check(page.startsWith("<!doctype html>") && page.contains("<h1>Ad &lt;&amp;&gt;</h1>")
                    && page.contains("<h2>Öğrenci No: 12345</h2>") && page.contains("Türkçe biyografi"),
                    renderer.getClass().getSimpleName() + " sözleşme ve HTML kaçış kontrolü");
        }
        System.out.println("SONUÇ: " + passed + "/" + passed + " kontrol başarılı.");
    }

    private static byte[] exchange(String request) throws Exception {
        if (offline) {
            try (MemorySocket socket = new MemorySocket(request)) {
                handler.handle(socket);
                return socket.output.toByteArray();
            }
        }
        try (Socket socket = new Socket("127.0.0.1", 1989)) {
            socket.setSoTimeout(7000);
            socket.getOutputStream().write(request.getBytes(StandardCharsets.US_ASCII));
            socket.getOutputStream().flush();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            socket.getInputStream().transferTo(output);
            return output.toByteArray();
        }
    }

    /** Ağ izinleri olmayan ortamlarda aynı işleyiciyi gerçek bellek akışlarıyla sınar. */
    private static class MemorySocket extends Socket {
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        private final InputStream input;

        MemorySocket(String request) {
            ByteArrayInputStream bytes = new ByteArrayInputStream(request.getBytes(StandardCharsets.US_ASCII));
            input = new InputStream() {
                @Override public int read() throws IOException {
                    int value = bytes.read();
                    if (value == -1 && !request.isEmpty() && !request.endsWith("\r\n\r\n"))
                        throw new SocketTimeoutException("Testte üretilen hareketsizlik");
                    return value;
                }
            };
        }
        @Override public InputStream getInputStream() { return input; }
        @Override public OutputStream getOutputStream() { return output; }
        @Override public void setSoTimeout(int timeout) { /* Gerçek ağ yok. */ }
    }

    private static String text(byte[] response) {
        return new String(response, StandardCharsets.UTF_8);
    }

    private static boolean status(byte[] response, int code) {
        return text(response).startsWith("HTTP/1.1 " + code + " ");
    }

    private static String header(byte[] response, String name) {
        for (String line : text(response).split("\r\n\r\n", 2)[0].split("\r\n")) {
            if (line.startsWith(name + ": ")) return line.substring(name.length() + 2);
        }
        return "";
    }

    private static byte[] bodyBytes(byte[] response) {
        for (int i = 0; i + 3 < response.length; i++) {
            if (response[i] == 13 && response[i+1] == 10 && response[i+2] == 13 && response[i+3] == 10)
                return Arrays.copyOfRange(response, i + 4, response.length);
        }
        throw new AssertionError("HTTP başlık sonu bulunamadı");
    }

    private static String body(byte[] response) {
        return new String(bodyBytes(response), StandardCharsets.UTF_8);
    }

    private static void check(boolean ok, String name) {
        if (!ok) throw new AssertionError("BAŞARISIZ: " + name);
        passed++;
        System.out.println("OK " + passed + ": " + name);
    }
}
