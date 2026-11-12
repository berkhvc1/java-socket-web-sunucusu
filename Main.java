import java.io.IOException;

/** Uygulamayı oluşturan nesneleri birbirine bağlayan başlangıç sınıfı. */
public class Main {
    public static void main(String[] args) {
        // Teslimden önce bu yer tutucuyu kendi öğrenci numaranızla değiştirin.
        String studentNumber = args.length > 0 ? args[0] : "OGRENCI_NUMARANIZ";
        StudentProfile profile = new StudentProfile(
                "Bekir Berk Kahveci", studentNumber,
                "Kırklareli Üniversitesi Yazılım Mühendisliği Bölümü öğrencisiyim. "
                + "Java, Python, HTML ve CSS ile kendimi geliştiriyorum. "
                + "Yapay zekâ ve veri bilimi alanlarında çalışmayı hedefliyorum.");

        PageRenderer renderer = new BiographyPageRenderer(profile);
        // Aynı arayüzün alternatif uygulaması:
        // PageRenderer renderer = new SimplePageRenderer(profile);
        ClientHandler handler = new ClientHandler(renderer);
        WebServer server = new WebServer(1989, handler);
        try {
            server.start();
        } catch (IOException e) {
            System.err.println("Sunucu başlatılamadı veya durdu: " + e.getMessage());
            System.err.println("1989 portunun başka bir program tarafından kullanılmadığını kontrol edin.");
            System.exit(1);
        }
    }
}
