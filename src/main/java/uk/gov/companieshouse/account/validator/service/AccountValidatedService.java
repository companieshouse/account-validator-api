package uk.gov.companieshouse.account.validator.service;

import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.patch.service.PatchService;

/**
 * A service for {@link AccountValidated}s
 */
public interface AccountValidatedService {

    /**
     * Create the given {@link AccountValidated}
     *
     * @param accountValidated
     * @return An {@link AccountValidated}
     */
    AccountValidated createAccount(AccountValidated accountValidated);

    /**
     * Get the given {@link AccountValidated}
     *
     * @param accountId
     * @return An {@link AccountValidated} or null
     */
    AccountValidated getAccount(String accountId);

    /**
     * Determine whether any {@link AccountValidated}s exist with the given {@link AccountValidated} id
     *
     * @param accountsId
     * @return True or false
     */
    boolean existsWithAccountsId(String accountsId);

    /**
     * Update the given {@link AccountValidated}
     * 
     * @param abridgedAccount
     * @return An {@link AccountValidated} or null
     */
    AccountValidated updateAccount(AccountValidated abridgedAccount);

    int save(AccountValidated accountValidated);
}
