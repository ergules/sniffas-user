package app.vaazar.Domain.Address.Control;

import app.vaazar.Domain.Address.Entity.Address;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface AddressRepository extends CrudRepository<Address, Long> {
    List<Address> findAddressByUserId(Long userId);
}
