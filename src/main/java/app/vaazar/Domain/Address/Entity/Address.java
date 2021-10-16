package app.vaazar.Domain.Address.Entity;

import app.vaazar.Domain.BaseEntity.BaseEntity;
import app.vaazar.Domain.BaseEntity.IdSerializer;
import app.vaazar.Domain.User.Entity.User;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "ADDRESSES")
public class Address extends BaseEntity {

    private String title;
    private String country;
    private String province;
    private String city;
    private String town;
    private String zipCode;
    private String addressString;

    @ManyToOne
    @JoinColumn(name = "USER_ID")
    @JsonSerialize(using = IdSerializer.class)
    private User user;

    public void updateWithEntity(Address other) {
        this.title = other.title;
        this.country = other.country;
        this.province = other.province;
        this.city = other.city;
        this.town = other.town;
        this.zipCode = other.zipCode;
        this.addressString = other.addressString;
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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
