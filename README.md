# Java Socket ile Basit Web Sunucusu

Java'nın standart kütüphanesiyle geliştirilen bu uygulama, **1989** numaralı TCP portunda HTTP isteklerini karşılar. Tarayıcıda öğrencinin adı soyadı `h1`, öğrenci numarası `h2` ve kısa biyografisi biçimlendirilmiş HTML olarak gösterilir.

**Hazırlayan:** Bekir Berk Kahveci  
**Bölüm:** Kırklareli Üniversitesi, Yazılım Mühendisliği  
**Öğrenci numarası:** `OGRENCI_NUMARANIZ`

## Özellikler

- Yalnızca Java SE: `ServerSocket`, `Socket` ve standart giriş/çıkış sınıfları.
- Spring, Tomcat, Servlet, hazır HTTP sunucusu veya üçüncü taraf bağımlılık kullanılmaz.
- UTF-8 destekli, renk ve yazı tipi içeren, mobil ekranlara uyumlu biyografi sayfası.
- `/` ve `/index.html` için `GET` ve `HEAD` desteği.
- Temel hatalar için `400`, `404`, `405`, `408` ve `431` yanıtları.
- SOLID ilkelerini örnekleyen sorumluluk ayrımı ve `PageRenderer` arayüzü.
- Haricî test kütüphanesi gerektirmeyen 20 kontrol içeren test sınıfı.

## Gereksinimler

- **JDK 17 veya üzeri** (yalnızca JRE yeterli değildir).
- Herhangi bir modern web tarayıcısı.
- Boş bir 1989 TCP portu.

Maven, Gradle veya internetten paket indirmek gerekmez. Kaynak dosyaları UTF-8 olarak kaydedilmiştir.

## Öğrenci bilgilerini düzenleme

`src/Main.java` içindeki `OGRENCI_NUMARANIZ` metnini gerçek öğrenci numarasıyla değiştirin. Aynı bilgiyi bu README'ye ve `docs/Rapor.md` dosyasına da yazın. PDF'deki öğrenci numarası alanı elle doldurulabilir. Biyografi metni `Main.java` içinde bulunur.

Öğrenci numarası geçici olarak komut satırı argümanıyla da verilebilir:

```bash
java -cp out Main 123456789
```

Buradaki `123456789` yalnızca örnektir. Teslim edilen kaynakta yer tutucu bırakmayın.

## Derleme ve çalıştırma

Terminali projenin `README.md` ve `src` klasörünü içeren ana dizininde açın:

```bash
javac -encoding UTF-8 --release 17 -d out src/*.java
java -cp out Main
```

Windows PowerShell, Komut İstemi veya macOS/Linux terminalinde aynı komutlar kullanılabilir. `javac` tanınmıyorsa JDK kurulumu ve PATH ayarları kontrol edilmelidir.

Sunucu çalışırken tarayıcıdan açın:

**http://127.0.0.1:1989/**

`http://localhost:1989/` adresi de localhost IPv4 loopback adresine çözümlendiğinde kullanılabilir. Bağlantı sorunu varsa açıkça `127.0.0.1` adresini kullanın. Terminal açık kalmalıdır; sunucuyu durdurmak için **Ctrl+C** kullanın.

### IntelliJ IDEA ile çalıştırma

1. Proje klasörünü **Open** ile açın ve proje JDK'sını 17 veya üzeri seçin.
2. Gerekirse `src` klasörüne sağ tıklayıp **Mark Directory as → Sources Root** seçin.
3. `Main.java` dosyasındaki öğrenci numarasını düzenleyin.
4. `Main.main()` metodunu çalıştırın ve tarayıcıda yukarıdaki adresi açın.

## Proje dosyaları

| Dosya | Sorumluluk |
|---|---|
| `src/Main.java` | Profil, görünüm, istek işleyici ve sunucu nesnelerini oluşturur. |
| `src/StudentProfile.java` | Ad, numara ve biyografi verilerini saklar. |
| `src/PageRenderer.java` | HTML üretme sözleşmesini tanımlar. |
| `src/BiographyPageRenderer.java` | Varsayılan HTML ve CSS görünümünü üretir. |
| `src/SimplePageRenderer.java` | Aynı sözleşmeyi sağlayan alternatif sade görünümü üretir. |
| `src/Html.java` | Metin içindeki HTML özel karakterlerini dönüştürür. |
| `src/ClientHandler.java` | İstek başlıklarını okur, yöntemi ve yolu kontrol eder. |
| `src/HttpResponse.java` | HTTP başlıklarını ve UTF-8 gövdesini yazar. |
| `src/WebServer.java` | 1989 portunda bağlantı bekler ve kabul eder. |
| `tests/ServerIntegrationTest.java` | Çalışan sunucuyu ve görünüm sözleşmesini test eder. |
| `docs/Rapor.pdf` | A4 boyutunda, beş sayfalık yazılı rapor. |
| `docs/Rapor.md` | Raporun düzenlenebilir metni. |
| `docs/Test_Sonuclari.txt` | Hazırlama sırasında elde edilen gerçek test çıktısı. |
| `TESLIM_KONTROL.md` | Teslim öncesi tamamlanacak işlemler. |

## Çalışma mantığı

`Main`, bir `PageRenderer` nesnesini `ClientHandler` yapıcısına verir. `WebServer`, `ServerSocket` ile 1989 portunu dinler. Tarayıcı bağlandığında `accept()` bir `Socket` döndürür. İşleyici başlıkların sonundaki boş satıra kadar okur; istek uygunsa görünümün `render()` metodunu çağırır. `HttpResponse`, durum satırını, başlıkları ve HTML baytlarını gönderir. Yanıttan sonra bağlantı kapatılır ve sunucu yeni bağlantıyı bekler.

`Content-Length`, Java metninin karakter sayısı yerine UTF-8 olarak kodlanmış **bayt sayısından** hesaplanır. Bu nedenle `ı`, `ş` ve `ğ` gibi karakterler yanıt uzunluğunu bozmaz.

## SOLID ilkeleri

| İlke | Projedeki karşılığı |
|---|---|
| **S — Tek sorumluluk** | Profil verisi, bağlantı kabulü, HTTP işleme ve sayfa üretimi ayrı sınıflardadır. |
| **O — Açık kapalı** | Yeni bir görünüm `PageRenderer` uygulanarak eklenebilir; `ClientHandler` ve `WebServer` değişmez. `Main` içindeki seçim değişir. |
| **L — Liskov yerine geçme** | İki görünüm de null olmayan tam HTML döndürür. Aynı istemci kodunda birbirlerinin yerine kullanılabilirler. |
| **I — Arayüz ayrımı** | `PageRenderer` yalnızca `render()` metodunu içerir; ağ veya dosya işlemleri dayatmaz. |
| **D — Bağımlılıkların tersine çevrilmesi** | `ClientHandler`, somut görünüm sınıfına değil `PageRenderer` arayüzüne bağımlıdır. Nesne dışarıdan yapıcıyla verilir. |

Bu küçük projede ilkeler özellikle sayfa üretimi ile istek işleme arasındaki sınırda uygulanmıştır. Her sınıf için gereksiz bir arayüz oluşturulmamıştır.

## Testleri çalıştırma

Önce sunucuyu bir terminalde çalıştırın. İkinci terminali aynı proje klasöründe açın:

```bash
javac -encoding UTF-8 --release 17 -cp out -d out tests/ServerIntegrationTest.java
java -cp out ServerIntegrationTest
```

Başarılı çalışmada sonuç `SONUÇ: 20/20 kontrol başarılı.` olur. Eksik başlık testi nedeniyle yaklaşık üç saniyelik bekleme normaldir. İlk başarısız kontrolde test `AssertionError` ile durur ve sıfırdan farklı çıkış kodu üretir.

Ağ bağlantısı izni olmayan ortamlarda, sunucuyu başlatmadan aynı HTTP işleyicisi bellek akışlarıyla sınanabilir:

```bash
java -cp out ServerIntegrationTest --offline
```

Hazırlama ortamında kaynak kod JDK 26 ile Java 17 hedeflenerek derlendi ve sunucunun 1989 portunda başladığı görüldü. Test istemcisinin yerel TCP bağlantısı ortam izniyle engellendiği için kaydedilen test çıktısı **bellek içi moddan** alınmıştır. Bu mod gerçek ağ bağlantısını, tarayıcı görünümünü veya işletim sistemi zaman aşımını doğrulamaz; zaman aşımı hatasını test akışı üretir. Gerçek bağlantı testi teslim öncesinde yukarıdaki normal komutla çalıştırılmalıdır.

## Kapsam ve sınırlamalar

- Sunucu **127.0.0.1** adresine bağlanır. Aynı bilgisayardaki farklı tarayıcılar erişebilir; başka cihazlar doğrudan erişemez.
- `localhost`, bağlantıyı açan cihazın kendisidir. GitHub'a kod yüklemek, Java sunucusunu internette çalıştırmaz. GitHub Pages bu Java sürecini çalıştıramaz.
- İstekler sırayla işlenir. HTTPS, eş zamanlı iş parçacıkları, oturum, dosya sunma ve kalıcı HTTP bağlantısı yoktur.
- Her bağlantıda tek istek değerlendirilir ve yanıt sonunda bağlantı kapatılır. İstek gövdeleri işlenmez; HTTP'nin tüm özellikleri uygulanmaz.
- Başlıklar 16 KiB ile sınırlıdır. Soket okumasında üç saniyelik hareketsizlik zaman aşımı vardır; bu, bütün isteğin toplam süresi için bir üst sınır değildir.
- Eğitim amaçlıdır; üretim sunucusu olarak tasarlanmamıştır.

## GitHub ve yazılı teslim

Repo **Public** olmalıdır. Projenin `src`, `tests`, `docs` klasörlerini, README'yi ve `.gitignore` dosyasını yükleyin; yalnızca ZIP yüklemek kaynak kodları incelemeyi zorlaştırır. `out` klasörünü veya `.class` dosyalarını yüklemeyin. Repo bağlantısını oturum açılmamış bir tarayıcıda kontrol edin ve teslim tarihinde paylaşın.

Yazılı teslim için `docs/Rapor.pdf` dosyasının **beş sayfasını da A4 olarak** yazdırın. Öğrenci numarası ve gerçek GitHub repo bağlantısı alanlarını doldurun. Proje dosyalarının ayrıca derste alınacağı ve geç teslimin sağlık raporu dışında kabul edilmediği ödev yönergesinde belirtilmiştir.

## Kaynaklar

- [Oracle — ServerSocket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/ServerSocket.html)
- [Oracle — Socket](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/net/Socket.html)
- [IETF — RFC 9112 HTTP/1.1](https://www.rfc-editor.org/rfc/rfc9112.html)
- [Robert C. Martin — Design Principles and Design Patterns](https://objectmentor.com/resources/articles/Principles_and_Patterns.pdf)
- [GitHub — Depoya dosya ekleme](https://docs.github.com/en/repositories/working-with-files/managing-files/adding-a-file-to-a-repository)
