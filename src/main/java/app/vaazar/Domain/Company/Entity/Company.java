package app.vaazar.Domain.Company.Entity;

import app.vaazar.Domain.Address.Entity.Address;
import app.vaazar.Domain.BaseEntity.BaseEntity;
import app.vaazar.Domain.BaseEntity.IdSerializer;
import app.vaazar.Domain.User.Entity.User;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "COMPANIES")
public class Company extends BaseEntity {

    private String companyName;
    private String website;
    private String tradeRegistry;   // file
    private String taxRegistry;     // file
    private String IBAN;
    @Enumerated(EnumType.STRING)
    private CompanyType companyType;

    private String companyExecutiveFirstName;
    private String companyExecutiveLastName;
    private String companyExecutiveIdentity; // file

    private String phoneNumber;
    @OneToOne
    @Cascade(CascadeType.ALL)
    private Address address;

    @OneToMany(mappedBy = "company")
    @Cascade(CascadeType.ALL)
    private Set<ShareHolder> shareHolders;

    @OneToOne
    @JoinColumn(name = "USER_ID")
    @JsonSerialize(using = IdSerializer.class)
    private User user;

    public boolean checkForRequiredFields() {
        return ObjectUtils.allNotNull(companyName, tradeRegistry, taxRegistry, address, companyType,
                companyExecutiveFirstName, companyExecutiveLastName, companyExecutiveIdentity);
    }

    public void updateFields(Company other) {
        this.companyName = other.companyName;
        this.website = other.website;
        this.tradeRegistry = other.tradeRegistry;
        this.taxRegistry = other.taxRegistry;
        this.IBAN = other.IBAN;
        this.companyExecutiveFirstName = other.companyExecutiveFirstName;
        this.companyExecutiveLastName = other.companyExecutiveLastName;
        this.companyExecutiveIdentity = other.companyExecutiveIdentity;
        this.address.updateWithEntity(other.address);
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getTradeRegistry() {
        return tradeRegistry;
    }

    public void setTradeRegistry(String tradeRegistry) {
        this.tradeRegistry = tradeRegistry;
    }

    public String getTaxRegistry() {
        return taxRegistry;
    }

    public void setTaxRegistry(String taxRegistry) {
        this.taxRegistry = taxRegistry;
    }

    public String getIBAN() {
        return IBAN;
    }

    public void setIBAN(String IBAN) {
        this.IBAN = IBAN;
    }

    public String getCompanyExecutiveFirstName() {
        return companyExecutiveFirstName;
    }

    public void setCompanyExecutiveFirstName(String companyExecutiveFirstName) {
        this.companyExecutiveFirstName = companyExecutiveFirstName;
    }

    public String getCompanyExecutiveLastName() {
        return companyExecutiveLastName;
    }

    public void setCompanyExecutiveLastName(String companyExecutiveLastName) {
        this.companyExecutiveLastName = companyExecutiveLastName;
    }

    public String getCompanyExecutiveIdentity() {
        return companyExecutiveIdentity;
    }

    public void setCompanyExecutiveIdentity(String companyExecutiveIdentity) {
        this.companyExecutiveIdentity = companyExecutiveIdentity;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Set<ShareHolder> getShareHolders() {
        return shareHolders;
    }

    public void setShareHolders(Set<ShareHolder> shareHolders) {
        this.shareHolders = shareHolders;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }
}
