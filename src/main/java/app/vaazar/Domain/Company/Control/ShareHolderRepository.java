package app.vaazar.Domain.Company.Control;

import app.vaazar.Domain.Company.Entity.ShareHolder;
import org.springframework.data.repository.CrudRepository;

public interface ShareHolderRepository extends CrudRepository<ShareHolder, Long> {
}
