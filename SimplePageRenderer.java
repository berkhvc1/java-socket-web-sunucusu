import java.util.Objects;

/** LSP ve OCP için aynı sözleşmeyi sağlayan alternatif, sade HTML görünümü. */
public class SimplePageRenderer implements PageRenderer {
    private final StudentProfile profile;

    public SimplePageRenderer(StudentProfile profile) {
        this.profile = Objects.requireNonNull(profile);
    }

    @Override
    public String render() {
        return """
                <!doctype html>
                <html lang="tr">
                <head><meta charset="UTF-8"><title>Biyografi</title>
                <style>body {font-family: Arial, sans-serif; margin: 40px; color: #253447;}
                h1 {color: #246b75;} p {line-height: 1.8;}</style></head>
                <body><h1>%s</h1><h2>Öğrenci No: %s</h2><p>%s</p></body>
                </html>
                """.formatted(Html.escape(profile.getFullName()),
                Html.escape(profile.getStudentNumber()), Html.escape(profile.getBiography()));
    }
}
