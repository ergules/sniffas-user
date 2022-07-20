package app.vaazar.domain.upload.entity;

public enum UploadType {
    PROFILE_PIC("U%sPP-", false),
    COMPANY_REGISTRY("UD%sCR-", true),
    TAX_REGISTRY("UD%sTR-", true),
    GM_IDENTITY("UD%sGMI-", true),
    SH_IDENTITY("UD%sSHI-", true),
    PRODUCT_VIDEO("PV%S-", false),
    PRODUCT_IMAGE("PI%S-", false),
    DISPUTE_IMAGE("DI%S-", false),
    LIVE_EVENT_IMAGE("LI%S-", false),
    SLIDER_IMG("SI-", false);

    UploadType(String template, boolean doc) {
        this.template = template;
        this.isDocument = doc;
    }

    private final String template;
    private final boolean isDocument;

    public String getTemplate() {
        return template;
    }

    public boolean isDocument() {
        return isDocument;
    }
}
