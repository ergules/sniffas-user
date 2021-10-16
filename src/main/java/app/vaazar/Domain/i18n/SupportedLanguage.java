package app.vaazar.Domain.i18n;

public enum SupportedLanguage {
    DE,
    EN;

    public static SupportedLanguage getDefault() {
        return DE;
    }
}
