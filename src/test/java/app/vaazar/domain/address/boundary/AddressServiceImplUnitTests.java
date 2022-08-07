package app.vaazar.domain.address.boundary;

import app.vaazar.domain.address.boundary.impl.AddressServiceImpl;
import app.vaazar.domain.address.control.AddressRepository;
import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(SpringExtension.class)
public class AddressServiceImplUnitTests {

    @Mock
    AddressRepository addressRepo;
    @Mock
    UserRepository userRepo;
    @InjectMocks
    AddressServiceImpl service;

    @Captor
    ArgumentCaptor<Address> addressCaptor;


    @Test
    public void saveAddress_withUser() {
        Address address = new Address();
        User user = new User();
        user.setId(10L);

        address.setUser(user);
        address.setCity("_city");

        Mockito.when(userRepo.findById(any())).thenReturn(Optional.of(user));
        service.saveAddress(address);

        Mockito.verify(addressRepo).save(any());
    }

    @Test
    public void saveAddress_whenAddressHasNoRelation_thenThrowIllegalArgumentException() {
        Address address = new Address();
        assertThrows(IllegalArgumentException.class, () -> service.saveAddress(address));
    }

    @Test
    public void addAddressToUser() {
        Address address = new Address();

        User user = new User();
        user.setId(10L);

        address.setUser(user);
        address.setCity("_city");

        Mockito.when(userRepo.findById(any())).thenReturn(Optional.of(user));
        service.addAddressToUser(address, 10L);

        Mockito.verify(addressRepo).save(any());
    }

    @Test
    public void updateAddress_happy() {
        Address address = new Address();

        User user = new User();
        user.setId(10L);

        address.setUser(user);
        address.setCity("_city");

        Mockito.when(addressRepo.findById(any())).thenReturn(Optional.of(address));

        service.updateAddress(address, 10L);
        Mockito.verify(addressRepo).save(any());
    }

    @Test
    public void updateAddress_whenUserIsWrong_thenThrowAccessDeniedException() {
        Address address = new Address();
        User user = new User();
        user.setId(10L);
        address.setUser(user);

        Mockito.when(addressRepo.findById(any())).thenReturn(Optional.of(address));
        assertThrows(AccessDeniedException.class,
                () -> service.updateAddress(address, 11L));
    }

    @Test
    public void deleteAddress_happy() {
        Address address = new Address();
        User user = new User();

        user.setId(10L);
        address.setUser(user);

        Mockito.when(addressRepo.findById(any())).thenReturn(Optional.of(address));

        service.deleteAddress(0L, 10L);
        Mockito.verify(addressRepo).save(addressCaptor.capture());

        assertTrue(addressCaptor.getValue().getDeleted());
    }

    @Test
    public void deleteAddress_whenUserIsWrong_thenThrowAccessDeniedException() {
        Address address = new Address();
        User user = new User();
        user.setId(10L);
        address.setUser(user);

        Mockito.when(addressRepo.findById(any())).thenReturn(Optional.of(address));
        assertThrows(AccessDeniedException.class,
                () -> service.deleteAddress(0L, 11L));
    }

    @Test
    public void findAddressByUserId() {

        Mockito.when(addressRepo.findAddressByUserId(any()))
                .thenReturn(Collections.emptyList());
        service.findAllByUserId(-1L);

        Mockito.verify(addressRepo).findAddressByUserId(-1L);
    }

}
