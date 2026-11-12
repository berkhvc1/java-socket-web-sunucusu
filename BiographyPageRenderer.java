import java.util.Objects;

/** Yalnızca biyografi sayfasının HTML ve CSS içeriğinden sorumludur. */
public class BiographyPageRenderer implements PageRenderer {
    private final StudentProfile profile;

    public BiographyPageRenderer(StudentProfile profile) {
        this.profile = Objects.requireNonNull(profile);
    }

    @Override
    public String render() {
        return """
                <!doctype html>
                <html lang="tr">
                <head>
                  <meta charset="UTF-8">
                  <meta name="viewport" content="width=device-width, initial-scale=1">
                  <title>Bekir Berk Kahveci | Biyografi</title>
                  <style>
                    * { box-sizing: border-box; }
                    body { margin: 0; padding: 48px 20px; background: #eef2f6;
                           color: #253447; font-family: Arial, sans-serif; }
                    main { max-width: 760px; margin: 40px auto; padding: 40px;
                           background: white; border-top: 6px solid #246b75;
                           border-radius: 12px; box-shadow: 0 8px 30px #20304012; }
                    .label { color: #246b75; font-size: 13px; letter-spacing: 2px; }
                    h1 { font-family: Georgia, serif; font-size: 38px; margin: 18px 0; }
                    h2 { color: #246b75; font-size: 22px; overflow-wrap: anywhere; }
                    h3 { margin-top: 30px; font-size: 19px; }
                    p { font-size: 17px; line-height: 1.8; }
                    .note { margin-top: 32px; color: #586574; font-size: 14px; }
                    @media (max-width: 520px) {
                      body { padding: 20px 12px; } main { margin: 10px auto; padding: 24px; }
                      h1 { font-size: 30px; } h2 { font-size: 19px; }
                    }
                  </style>
                </head>
                <body>
                  <main>
                    <div class="label">KİŞİSEL BİYOGRAFİ</div>
                    <h1>%s</h1>
                    <h2>Öğrenci No: %s</h2>
                    <h3>Hakkımda</h3>
                    <p>%s</p>
                    <p class="note">Kırklareli Üniversitesi · Yazılım Mühendisliği</p>
                  </main>
                </body>
                </html>
                """.formatted(Html.escape(profile.getFullName()),
                Html.escape(profile.getStudentNumber()), Html.escape(profile.getBiography()));
    }
}
