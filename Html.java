/** Metni HTML içine güvenli biçimde yerleştirmek için özel karakterleri dönüştürür. */
public final class Html {
    private Html() { }

    public static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}
