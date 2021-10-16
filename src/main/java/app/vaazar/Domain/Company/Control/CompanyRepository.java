package app.vaazar.Domain.Company.Control;

import app.vaazar.Domain.Company.Entity.Company;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByUserId(Long userId);
}
