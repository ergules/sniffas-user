package app.vaazar.domain.address.Control;

import app.vaazar.domain.address.Entity.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Long> {
    List<Address> findAddressByUserId(Long userId);
}
