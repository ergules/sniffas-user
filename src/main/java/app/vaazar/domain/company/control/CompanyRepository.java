package app.vaazar.domain.company.control;

import app.vaazar.domain.company.entity.Company;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface CompanyRepository extends CrudRepository<Company, Long> {

    Optional<Company> findByUserId(Long userId);
}
