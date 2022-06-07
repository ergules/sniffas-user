package app.vaazar.domain.upload.entity;

public enum UploadType {
    PROFILE_PIC("U%sPP-"),
    COMPANY_REGISTRY("UD%sCR-"),
    TAX_REGISTRY("UD%sTR-"),
    GM_IDENTITY("UD%sGMI-"),
    SH_IDENTITY("UD%sSHI-");

    UploadType(String template) {
        this.template = template;
    }

    private final String template;

    public String getTemplate() {
        return  template;
    }


}
