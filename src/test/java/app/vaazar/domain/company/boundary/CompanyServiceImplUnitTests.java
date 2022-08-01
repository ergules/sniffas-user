package app.vaazar.domain.company.boundary;

import app.vaazar.domain.address.entity.Address;
import app.vaazar.domain.company.boundary.impl.CompanyServiceImpl;
import app.vaazar.domain.company.control.CompanyRepository;
import app.vaazar.domain.company.control.ShareHolderRepository;
import app.vaazar.domain.company.entity.Company;
import app.vaazar.domain.company.entity.ShareHolder;
import app.vaazar.domain.user.entity.Role;
import app.vaazar.domain.user.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class CompanyServiceImplUnitTests {
    @Mock
    Logger log;
    @Mock
    CompanyRepository companyRepo;
    @Mock
    ShareHolderRepository shareHolderRepo;

    @InjectMocks
    CompanyServiceImpl service;

    @Test
    public void saveCompany() {
        Company company = new Company();
        company.setTaxId("_taxId");
        when(companyRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertEquals(service.saveCompany(company).getTaxId(), "_taxId");
        verify(companyRepo).save(any());
    }

    @Test
    public void updateCompany_happy() {
        Long userId = 10L;
        Long companyId = 5L;
        Company company = new Company();
        company.setId(companyId);
        company.setAddress(new Address());
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);

        when(companyRepo.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Company saved = service.updateCompany(company, userId);

        assertEquals(saved, company);
    }

    @Test
    public void updateCompany_whenIllegalAccess_thenThrowAccessDeniedException() {
        long userId = 10L;
        Long companyId = 5L;
        Company company = new Company();
        company.setId(companyId);
        company.setAddress(new Address());
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);

        when(companyRepo.findById(companyId)).thenReturn(Optional.of(company));
        when(companyRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(AccessDeniedException.class, () ->
                service.updateCompany(company, userId + 1));
    }

    @Test
    public void createShareHolder() {
        long userId = 10;
        long shareHolderId = 3;
        long companyId = 5;

        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setId(shareHolderId);
        Company company = new Company();
        company.setId(companyId);

        when(companyRepo.findByUserId(userId)).thenReturn(Optional.of(company));
        when(shareHolderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ShareHolder saved = service.createShareHolder(shareHolder, userId);
        assertEquals(saved.getId(), shareHolderId);
        assertEquals(saved.getCompany(), company);
    }

    @Test
    public void updateShareHolder_happy() {
        long userId = 10;
        long shareHolderId = 3;
        long companyId = 5;

        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setId(shareHolderId);
        Company company = new Company();
        company.setId(companyId);
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);
        shareHolder.setCompany(company);

        when(shareHolderRepo.findById(shareHolderId)).thenReturn(Optional.of(shareHolder));
        when(shareHolderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        ShareHolder saved = service.updateShareHolder(shareHolder, userId);
        assertEquals(saved.getId(), shareHolderId);
    }

    @Test
    public void updateShareHolder_whenIllegalAccess_thenThrowAccessDeniedException() {
        long userId = 10;
        long shareHolderId = 3;
        long companyId = 5;

        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setId(shareHolderId);
        Company company = new Company();
        company.setId(companyId);
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);
        shareHolder.setCompany(company);

        when(shareHolderRepo.findById(shareHolderId)).thenReturn(Optional.of(shareHolder));
        when(shareHolderRepo.save(any())).thenAnswer(i -> i.getArguments()[0]);

        assertThrows(AccessDeniedException.class, () ->
                service.updateShareHolder(shareHolder, userId + 1));
    }

    @Test
    public void deleteShareHolder_happy() {
        long userId = 10;
        long shareHolderId = 3;
        long companyId = 5;


        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setId(shareHolderId);
        Company company = new Company();
        company.setId(companyId);
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);
        shareHolder.setCompany(company);

        when(shareHolderRepo.findById(shareHolderId)).thenReturn(Optional.of(shareHolder));

        service.deleteShareHolder(shareHolderId, userId);
        verify(shareHolderRepo).delete(shareHolder);
    }

    @Test
    public void deleteShareHolder_whenIllegalAccess_thenThrowAccessDeniedException() {
        long userId = 10;
        long shareHolderId = 3;
        long companyId = 5;

        ShareHolder shareHolder = new ShareHolder();
        shareHolder.setId(shareHolderId);
        Company company = new Company();
        company.setId(companyId);
        User user = new User(userId, null, null, Role.COMPANY);
        company.setUser(user);
        shareHolder.setCompany(company);

        when(shareHolderRepo.findById(shareHolderId)).thenReturn(Optional.of(shareHolder));

        assertThrows(AccessDeniedException.class, () ->
                service.deleteShareHolder(shareHolderId, userId + 1));
    }

}
