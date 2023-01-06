package uk.gov.companieshouse.account.validator.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;
import uk.gov.companieshouse.account.validator.repository.AccountValidatedRepository;
import uk.gov.companieshouse.account.validator.service.AccountValidatedService;

/**
 * Implementation of {@link AccountValidatedService}
 */
@Service
public class AccountValidatedServiceImpl implements AccountValidatedService {

    @Autowired
    private AccountValidatedRepository accountValidatedRepository;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AccountValidated createAccount(AccountValidated accountValidated) {

        ensureIdsMatch(accountValidated);
        accountValidated.prune();

        return accountValidatedRepository.insert(accountValidated);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountValidated updateAccount(AccountValidated accountValidated) {

        ensureIdsMatch(accountValidated);
        accountValidated.prune();

        return accountValidatedRepository.save(accountValidated);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public AccountValidated getAccount(String accountId) {

        return accountValidatedRepository.findById(accountId).orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsWithAccountsId(String accountsId) {

        return accountValidatedRepository.existsWithAccountsId(accountsId);
    }

    /**
     * {@inheritDoc}
     * @return
     */
    @Override
    public int save(AccountValidated accountValidated) {

        ensureIdsMatch(accountValidated);
        accountValidated.prune();
        accountValidatedRepository.save(accountValidated);

        return 1;
    }

     /**
     * Ensure the {@link AccountValidated} and {@link ValidationResponse} ids match
     *
     * @param accountValidated
     */
    private void ensureIdsMatch(AccountValidated accountValidated) {


        if (!accountValidated.getId().equals(accountValidated.getData().getId())) {

            throw new IllegalArgumentException("AbridgedAccount and AbridgedAccountData ids differ!");
        }
    }

}
