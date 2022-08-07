package app.vaazar.domain.address.entity;

import app.vaazar.domain.baseEntity.BaseEntity;
import app.vaazar.domain.baseEntity.IdSerializer;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.user.entity.User;
import app.vaazar.security.HashUtil;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.*;

@Entity
@Table(name = "ADDRESSES")
public class Address extends BaseEntity {

    private String title;
    private String country;
    private String province;
    private String city;
    private String town;
    private String zipCode;
    private String streetName;
    private String streetNo;
    private String addressString;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @JsonSerialize(using = IdSerializer.class)
    private User user;

    @OneToOne(mappedBy = "address")
    @JsonSerialize(using = IdSerializer.class)
    private Company company;

    public void updateWithEntity(Address other) {
        this.title = other.title;
        this.country = other.country;
        this.province = other.province;
        this.city = other.city;
        this.town = other.town;
        this.zipCode = other.zipCode;
        this.addressString = other.addressString;
        this.streetName = other.streetName;
        this.streetNo = other.streetNo;
    }

    @JsonProperty("phrase")
    public String getIdPhrase() {
        return HashUtil.expressHash("address#" + getId().toString());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getAddressString() {
        return addressString;
    }

    public void setAddressString(String addressString) {
        this.addressString = addressString;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getStreetNo() {
        return streetNo;
    }

    public void setStreetNo(String streetNo) {
        this.streetNo = streetNo;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        Address address = (Address) o;

        if (getTitle() != null ? !getTitle().equals(address.getTitle()) : address.getTitle() != null) return false;
        if (getCountry() != null ? !getCountry().equals(address.getCountry()) : address.getCountry() != null)
            return false;
        if (getProvince() != null ? !getProvince().equals(address.getProvince()) : address.getProvince() != null)
            return false;
        if (getCity() != null ? !getCity().equals(address.getCity()) : address.getCity() != null) return false;
        if (getTown() != null ? !getTown().equals(address.getTown()) : address.getTown() != null) return false;
        if (getZipCode() != null ? !getZipCode().equals(address.getZipCode()) : address.getZipCode() != null)
            return false;
        if (getStreetName() != null ? !getStreetName().equals(address.getStreetName()) : address.getStreetName() != null)
            return false;
        if (getStreetNo() != null ? !getStreetNo().equals(address.getStreetNo()) : address.getStreetNo() != null)
            return false;
        return getAddressString() != null ? getAddressString().equals(address.getAddressString()) : address.getAddressString() == null;
    }

    @Override
    public int hashCode() {
        int result = getTitle() != null ? getTitle().hashCode() : 0;
        result = 31 * result + (getCountry() != null ? getCountry().hashCode() : 0);
        result = 31 * result + (getProvince() != null ? getProvince().hashCode() : 0);
        result = 31 * result + (getCity() != null ? getCity().hashCode() : 0);
        result = 31 * result + (getTown() != null ? getTown().hashCode() : 0);
        result = 31 * result + (getZipCode() != null ? getZipCode().hashCode() : 0);
        result = 31 * result + (getStreetName() != null ? getStreetName().hashCode() : 0);
        result = 31 * result + (getStreetNo() != null ? getStreetNo().hashCode() : 0);
        result = 31 * result + (getAddressString() != null ? getAddressString().hashCode() : 0);
        return result;
    }
}
