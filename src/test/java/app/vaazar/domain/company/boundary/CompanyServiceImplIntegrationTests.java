package app.vaazar.domain.company.boundary;

import app.vaazar.domain.company.boundary.impl.CompanyServiceImpl;
import app.vaazar.domain.company.control.CompanyRepository;
import app.vaazar.domain.company.control.ShareHolderRepository;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import static app.vaazar.TestDataHelper.generateValidCompany;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
public class CompanyServiceImplIntegrationTests {

    private final TestEntityManager entityManager;
    private final CompanyServiceImpl service;

    @Test
    public void saveCompany() {
        Company company = generateValidCompany();
        assertThrows(DataIntegrityViolationException.class, () -> service.saveCompany(company));

        User user1 = entityManager.find(User.class, 1L);
        company.setUser(user1);
        Company persisted = service.saveCompany(company);
        assertNotNull(persisted.getId());
        assertNotNull(persisted.getAddress().getId());
        assertEquals(user1, persisted.getUser());
    }

    @Test
    public void updateCompany() {
        Company stored = entityManager.find(Company.class, 1L);
        entityManager.clear();

        Company company = generateValidCompany();
        company.setId(stored.getId());
        Company merged = service.updateCompany(company, stored.getUser().getId());

        assertNotNull(merged.getId());
        assertNotNull(merged.getAddress().getId());
        assertEquals(stored.getUser().getId(), merged.getUser().getId());
        assertEquals(company.getAddress().getTitle(), merged.getAddress().getTitle());
    }

    @Test
    public void createShareHolder() {
        Company company = entityManager.find(Company.class, 1L);
        entityManager.clear();

        ShareHolder shareholder = new ShareHolder();
        shareholder.setFirstName("share");
        shareholder.setLastName("holder");
        shareholder.setIdentity("sh.png");

        ShareHolder persisted = service.createShareHolder(shareholder, company.getUser().getId());
        assertNotNull(persisted.getId());
        assertEquals(company, persisted.getCompany());
        assertEquals(shareholder.getLastName(), persisted.getLastName());
    }

    @Test
    public void updateShareHolder() {
        Company company = entityManager.find(Company.class, 1L);

        ShareHolder shareholder = new ShareHolder();
        shareholder.setFirstName("share");
        shareholder.setLastName("holder");
        shareholder.setIdentity("sh.png");
        shareholder.setCompany(company);
        entityManager.persistAndFlush(shareholder);
        entityManager.clear();

        ShareHolder updated = new ShareHolder();
        updated.updateFields(shareholder);
        updated.setId(shareholder.getId());
        updated.setLastName("newHolder");
        updated.setIdentity("newIdentity.png");

        ShareHolder merged = service.updateShareHolder(updated, company.getUser().getId());
        assertEquals(shareholder.getId(), merged.getId());
        assertEquals(company, merged.getCompany());
        assertEquals(updated.getLastName(), merged.getLastName());
    }

    @Test
    public void deleteShareHolder() {
        Company company = entityManager.find(Company.class, 1L);

        ShareHolder shareholder = new ShareHolder();
        shareholder.setFirstName("share");
        shareholder.setLastName("holder");
        shareholder.setIdentity("sh.png");
        shareholder.setCompany(company);
        entityManager.persistAndFlush(shareholder);
        entityManager.clear();

        assertNotNull(entityManager.find(ShareHolder.class, shareholder.getId()));

        service.deleteShareHolder(shareholder.getId(), null);
        assertNull(entityManager.find(ShareHolder.class, shareholder.getId()));
    }


    @Autowired
    public CompanyServiceImplIntegrationTests(TestEntityManager entityManager,
                                              CompanyRepository companyRepo,
                                              ShareHolderRepository shareholderRepo) {
        Logger logger = LoggerFactory.getLogger(CompanyServiceImpl.class);
        this.entityManager = entityManager;
        this.service = new CompanyServiceImpl(logger, companyRepo, shareholderRepo);
    }
}
