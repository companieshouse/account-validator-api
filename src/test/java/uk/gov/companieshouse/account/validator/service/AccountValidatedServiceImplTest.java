package uk.gov.companieshouse.account.validator.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.repository.AccountValidatedRepository;
import uk.gov.companieshouse.account.validator.service.impl.AccountValidatedServiceImpl;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit test for {@link AccountValidatedServiceImpl}
 */
@ExtendWith(MockitoExtension.class)
public class AccountValidatedServiceImplTest {


    public static final String ACCOUNT_VALIDATED_ID = "account-validate";


    /**
     * The object under test
     */
    @InjectMocks
    private AccountValidatedServiceImpl service;

    @Mock
    private AccountValidatedRepository accountValidatedRepository;

    @Mock
    private AccountValidated accountValidated;

    @Mock
    private ValidationResponse accountValidatedData;
    

    @Test
    void shouldNotFindAccountValidated() {

        given(accountValidatedRepository.findById(ACCOUNT_VALIDATED_ID)).willReturn(ofNullable(null));
        
        AccountValidated result = service.getAccount(ACCOUNT_VALIDATED_ID);

        assertNull(result);
        verify(accountValidatedRepository, times(1)).findById(ACCOUNT_VALIDATED_ID);
    }

    @Test
    void shouldFindAccountValidated() {

        given(accountValidatedRepository.findById(ACCOUNT_VALIDATED_ID)).willReturn(of(accountValidated));

        AccountValidated result = service.getAccount(ACCOUNT_VALIDATED_ID);

        assertEquals(accountValidated, result);
        verify(accountValidatedRepository, times(1)).findById(ACCOUNT_VALIDATED_ID);
    }

    @Test
    void shouldCreateAccountValidated() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("1");
        given(accountValidatedRepository.insert(accountValidated)).willReturn(accountValidated);

        AccountValidated result = service.createAccount(accountValidated);

        assertNotNull(result);
        assertEquals(accountValidated, result);
        verify(accountValidated, times(1)).getId();
        verify(accountValidatedData, times(1)).getId();
        verify(accountValidatedRepository, times(1)).insert(accountValidated);
    }

    @Test
    void shouldThrowExceptionOnCreateAccountValidatedDifferingIds() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("2");

        assertThrows(IllegalArgumentException.class, () -> service.createAccount(accountValidated));

        verify(accountValidated, times(1)).getId();
        verify(accountValidatedData, times(1)).getId();
    }

    @Test
    void shouldUpdateAccountValidated() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("1");
        given(accountValidatedRepository.save(accountValidated)).willReturn(accountValidated);

        AccountValidated result = service.updateAccount(accountValidated);

        assertNotNull(result);
        assertEquals(accountValidated, result);
        verify(accountValidated, times(1)).getId();
        verify(accountValidatedData, times(1)).getId();
        verify(accountValidatedRepository, times(1)).save(accountValidated);
    }

    @Test
    void shouldThrowExceptionOnUpdateAccountValidatedDifferingIds() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("2");

        assertThrows(IllegalArgumentException.class, () -> service.updateAccount(accountValidated));

        verify(accountValidated, times(1)).getId();
        verify(accountValidatedData, times(1)).getId();
    }

    @Test
    void shouldSaveAccountValidated() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("1");
        when(accountValidatedRepository.save(accountValidated)).thenReturn(accountValidated);

        int result = service.save(accountValidated);

        assertEquals(1, result);
        verify(accountValidatedRepository, times(1)).save(accountValidated);
    }

    @Test
    void shouldThrowExceptionOnSaveAccountValidatedDifferingIds() {

        when(accountValidated.getId()).thenReturn("1");
        when(accountValidated.getData()).thenReturn(accountValidatedData);

        when(accountValidatedData.getId()).thenReturn("2");

        assertThrows(IllegalArgumentException.class, () -> service.save(accountValidated));

        verify(accountValidated, times(1)).getId();
        verify(accountValidatedData, times(1)).getId();
    }

}
