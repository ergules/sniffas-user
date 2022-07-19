package app.vaazar.domain.company.boundary.impl;

import app.vaazar.domain.company.boundary.CompanyService;
import app.vaazar.domain.company.control.CompanyRepository;
import app.vaazar.domain.company.control.ShareHolderRepository;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CompanyServiceImpl implements CompanyService {
    Logger log;
    CompanyRepository companyRepo;
    ShareHolderRepository shareHolderRepo;

    public Company saveCompany(Company entity) {
        return companyRepo.save(entity);
    }

    public Company updateCompany(Company entity, Long userId) {
        Company company = companyRepo.findById(entity.getId()).orElseThrow();
        if (userId != null && !userId.equals(company.getUser().getId()))
            throw new AccessDeniedException("entity does not belong to user");
        company.updateFields(entity);
        return companyRepo.save(company);
    }

    public ShareHolder createShareHolder(ShareHolder entity, Long userId) {
        Company company = companyRepo.findByUserId(userId).orElseThrow();
        entity.setCompany(company);
        return shareHolderRepo.save(entity);
    }

    public ShareHolder updateShareHolder(ShareHolder entity, Long userId) {
        ShareHolder shareHolder = shareHolderRepo.findById(entity.getId()).orElseThrow();
        if (userId != null && !shareHolder.getCompany().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("entity does not belong to user");
        }
        shareHolder.updateFields(entity);
        return shareHolderRepo.save(shareHolder);
    }

    public void deleteShareHolder(Long entityId, Long userId) {
        ShareHolder shareHolder = shareHolderRepo.findById(entityId).orElseThrow();
        if (userId != null && !shareHolder.getCompany().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("entity does not belong to user");
        }
        shareHolderRepo.delete(shareHolder);
    }


    public CompanyServiceImpl(Logger log, CompanyRepository companyRepo, ShareHolderRepository shareHolderRepo) {
        this.log = log;
        this.companyRepo = companyRepo;
        this.shareHolderRepo = shareHolderRepo;
    }
}
