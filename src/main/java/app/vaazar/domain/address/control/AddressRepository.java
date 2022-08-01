package app.vaazar.domain.address.control;

import app.vaazar.domain.address.entity.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Long> {
    List<Address> findAddressByUserId(Long userId);
}
