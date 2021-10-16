package app.vaazar.Domain.Company.Entity;

import app.vaazar.Domain.BaseEntity.BaseEntity;
import app.vaazar.Domain.BaseEntity.IdSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Entity
@Table(name = "COMPANY_SHARE_HOLDERS")
public class ShareHolder extends BaseEntity {

    private String firstName;
    private String lastName;
    private String identity;

    @ManyToOne
    @JoinColumn(name = "COMPANY_ID")
    @JsonSerialize(using = IdSerializer.class)
    private Company company;

    public void updateFields(ShareHolder other) {
        this.firstName = other.firstName;
        this.lastName = other.lastName;
        this.identity = other.identity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ShareHolder that = (ShareHolder) o;
        return Objects.equals(getId(), that.getId()) &&
                Objects.equals(getFirstName(), that.getFirstName()) &&
                Objects.equals(getLastName(), that.getLastName()) &&
                Objects.equals(getIdentity(), that.getIdentity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getFirstName(), getLastName(), getIdentity());
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
}
