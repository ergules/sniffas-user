package app.vaazar.domain.address.Boundary;

import app.vaazar.domain.address.Control.AddressRepository;
import app.vaazar.domain.address.Entity.Address;
import app.vaazar.domain.user.control.UserRepository;
import app.vaazar.domain.user.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AddressService {

    AddressRepository repository;
    UserRepository userRepo;

    public Address saveAddress(Address address) {
        address.setUser(userRepo.findById(address.getUser().getId()).orElseThrow());
        return repository.save(address);
    }

    public Address addAddressToUser(Address address, Long userId) {
        User user = userRepo.findById(userId).orElseThrow();
        address.setUser(user);
        return repository.save(address);
    }

    public Address updateAddress(Address entity, Long userId) {
        Address address = repository.findById(entity.getId()).orElseThrow();
        if (userId != null && !userId.equals(address.getUser().getId()))
            throw new AccessDeniedException("address does not belong to user");
        address.updateWithEntity(entity);
        return repository.save(address);
    }

    public void deleteAddress(Long entityId, Long userId) {
        Address address = repository.findById(entityId).orElseThrow();
        if (userId != null && !userId.equals(address.getUser().getId()))
            throw new AccessDeniedException("address does not belong to user");
        address.setUser(null);
        address.setDeleted(true);
        repository.save(address);
    }

    public List<Address> findAllByUserId(Long userId) {
        return repository.findAddressByUserId(userId);
    }

    public AddressService(AddressRepository repository, UserRepository userRepository) {
        this.repository = repository;
        userRepo = userRepository;
    }
}
