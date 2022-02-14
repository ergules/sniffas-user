package app.vaazar.Domain.Company.Entity;

public enum CompanyType {

    T00("Einzelunternehmer"),
    T01("Gesellschaft bürgerlichen Rechts (GbR)"),
    T02("Offene Handelsgesellschaft (OHG)"),
    T03("Kommanditgesellschaft (KG)"),
    T04("Aktiengesellschaft (AG)"),
    T05("Gesellschaft mit beschränkter Haftung (GmbH)"),
    T06("Unternehmergesellschaft (haftungsbeschränkt) (UG (haftungsbeschränkt))"),
    T07("GmbH & Co. KG"),
    T08("Kommanditgesellschaft auf Aktien (KG aA)"),
    T09("Eingetragene Genossenschaft (eG)");

    private final String desc_ge;

    CompanyType(String desc_ge) {
        this.desc_ge = desc_ge;
    }

    public String getDesc_ge() {
        return desc_ge;
    }
}
