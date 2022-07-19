package app.vaazar.domain.company.boundary;

import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;


public interface CompanyService {

    Company saveCompany(Company entity);

    Company updateCompany(Company entity, Long userId);

    ShareHolder createShareHolder(ShareHolder entity, Long userId);

    ShareHolder updateShareHolder(ShareHolder entity, Long userId);

    void deleteShareHolder(Long entityId, Long userId);

}
