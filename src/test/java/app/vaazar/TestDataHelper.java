package app.vaazar;

import app.vaazar.config.ModelMapperConfig;
import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.CompanyType;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

public class TestDataHelper {

    private static final ModelMapper modelMapper = new ModelMapperConfig().getDefaultMapper();

    public static Company generateValidCompany() {
        Company company = new Company();
        company.setCompanyName("valid com");
        company.setTaxId("11111111111");
        company.setTradeRegistry("trade.pdf");
        company.setTaxRegistry("tax.pdf");
        company.setCompanyType(CompanyType.T01);
        company.setCompanyExecutiveFirstName("firstName");
        company.setCompanyExecutiveLastName("lastname");
        company.setCompanyExecutiveIdentity("id.pdf");

        Address address = new Address();
        address.setZipCode("VZC001");
        address.setTitle("valid com headquarters");
        company.setAddress(address);
        return company;
    }

    public static User getAdmin(TestEntityManager entityManager) {
        return entityManager.getEntityManager()
                .createQuery("select u from User u where u.role = :role", User.class)
                .setParameter("role", Role.ADMIN)
                .getSingleResult();
    }

    public static List<User> getPrivateSellers(TestEntityManager entityManager) {
        return entityManager.getEntityManager()
                .createQuery("select u from User u where u.role = :role", User.class)
                .setParameter("role", Role.SELLER)
                .getResultList();
    }

    public static List<User> getAllSellers(TestEntityManager entityManager) {
        return entityManager.getEntityManager()
                .createQuery("select u from User u where u.role in :roles", User.class)
                .setParameter("roles", List.of(Role.COMPANY, Role.SELLER))
                .getResultList();
    }

    public static void registerToSecurityContext(User user) {
        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(
                user, null,
                user.getAuthorities()
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    public static <T, D> T deepCopy(Class<D> withConverting, T obj) {
        D converted = modelMapper.map(obj, withConverting);
        return (T) modelMapper.map(converted, obj.getClass());
    }

}
