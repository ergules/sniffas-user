package app.vaazar.Domain.Upload;

public enum UploadType {
    PROFILE_PIC("U%sPP-"),
    COMPANY_REGISTRY("UD%sCR-"),
    TAX_REGISTRY("UD%sTR-"),
    GM_IDENTITY("UD%sGMI-"),
    SH_IDENTITY("UD%sSHI-%s-");

    UploadType(String template) {
        this.template = template;
    }

    private final String template;

    private String getTemplate() {
        return  template;
    }


}
