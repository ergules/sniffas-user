package app.vaazar.Endpoint;

import app.vaazar.Domain.Address.Boundary.AddressService;
import app.vaazar.Domain.Address.Entity.Address;
import app.vaazar.Domain.User.Boundary.UserService;
import app.vaazar.Domain.User.Entity.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;

@RestController
@RequestMapping("/users/{userId}")
@PreAuthorize("isAuthenticated()")
public class UsersApi {

    UserService userService;
    AddressService addressService;

    @PutMapping
    public User updateUser(@PathVariable Long userId,
                           @Valid @RequestBody User user,
                           Principal principal) {
        User loggedUser = (User) principal;
        if(!userId.equals(loggedUser.getId()) || user.getId()!= null && !userId.equals(user.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return userService.updateUserInfo(user);
    }

    @PostMapping("/addresses")
    public Address addAddressToUser(@PathVariable Long userId,
                                    @Valid @RequestBody Address address,
                                    UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if(!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        return addressService.addAddressToUser(address, userId);
    }

    @PutMapping("/addresses/{addressId}")
    public Address updateAddress(@PathVariable Long userId,
                                 @PathVariable Long addressId,
                                 @Valid @RequestBody Address address,
                                 UsernamePasswordAuthenticationToken contextUser) {
        User loggedUser = (User) contextUser.getPrincipal();
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        address.setId(addressId);
        return addressService.updateAddress(address, userId);
    }

    @DeleteMapping("/addresses/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long userId,
                                              @PathVariable Long addressId,
                                              @Valid @RequestBody Address address,
                                              Principal principal) {
        User loggedUser = (User) principal;
        if (!userId.equals(loggedUser.getId()))
            throw new AccessDeniedException("logged userId do not match with target");
        address.setId(addressId);

        addressService.deleteAddress(address, userId);
        return ResponseEntity.ok().build();
    }

    public UsersApi(UserService userService, AddressService addressService) {
        this.userService = userService;
        this.addressService = addressService;
    }
}
