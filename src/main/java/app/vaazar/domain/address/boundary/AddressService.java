package app.vaazar.domain.address.boundary;

import app.vaazar.domain.address.entity.Address;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public interface AddressService {

    Address saveAddress(Address address);

    Address addAddressToUser(Address address, Long userId);

    Address updateAddress(Address entity, Long userId);

    void deleteAddress(Long entityId, Long userId);

    List<Address> findAllByUserId(Long userId);
}
