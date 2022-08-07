package app.vaazar.domain.address.boundary;

import app.vaazar.domain.address.boundary.impl.AddressServiceImpl;
import app.vaazar.domain.address.control.AddressRepository;
import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class AddressServiceImplIntegrationTests {

    private final TestEntityManager entityManager;
    private final AddressServiceImpl service;


    @Test
    public void saveAddress() {
        String title = "save test title";
        User user = new User();
        user.setId(1L);

        Address address = new Address();
        address.setUser(user);
        address.setTitle(title);
        Address persisted = service.saveAddress(address);

        assertEquals(user.getId(), persisted.getUser().getId());
        assertEquals(title, persisted.getTitle());
    }

    @Test
    public void addAddressToUser() {
        String title = "adding new";
        Address address = new Address();
        address.setTitle(title);
        Address persisted = service.addAddressToUser(address, 2L);

        assertEquals(2, persisted.getUser().getId());
        assertEquals(title, persisted.getTitle());
    }

    @Test
    public void updateAddress() {
        String title = "update address";
        String zipcode = "new zip";
        Address address = new Address();
        address.setTitle(title);
        address.setZipCode(zipcode);
        address.setId(1L);
        Address merged = service.updateAddress(address, 1L);

        assertEquals(1L, merged.getId());
        assertEquals(1L, merged.getUser().getId());
        assertEquals(zipcode, merged.getZipCode());
        assertEquals(title, merged.getTitle());
    }

    @Test
    public void deleteAddress() {

        service.deleteAddress(2L, 2L);
        Address address = entityManager.find(Address.class, 2L);
        assertNull(address.getUser());
        assertTrue(address.getDeleted());
    }

    @Test
    public void findAllByUserId() {
        List<Address> _1 = service.findAllByUserId(1L);
        List<Address> _2 = service.findAllByUserId(2L);
        assertEquals(1, _1.size());
        assertEquals(1, _2.size());
        assertEquals(1L, _1.get(0).getUser().getId());
        assertEquals(2L, _2.get(0).getUser().getId());
    }


    @Autowired
    public AddressServiceImplIntegrationTests(TestEntityManager entityManager,
                                              AddressRepository addressRepository,
                                              UserRepository userRepo) {
        this.entityManager = entityManager;
        this.service = new AddressServiceImpl(addressRepository, userRepo);
    }
}
