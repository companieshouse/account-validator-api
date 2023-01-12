package uk.gov.companieshouse.account.validator.repository;

import uk.gov.companieshouse.account.validator.model.AccountValidated;
import uk.gov.companieshouse.account.validator.model.ValidationResponse;

/**
 * A repository interface for {@link AccountValidated} following Spring conventions
 */
public interface AccountValidatedRepositoryCustom {

    /**
     * Determine whether any {@link AccountValidated}s exist with the given {@link ValidationResponse} id
     * @param validationResponsedId
     *
     * @return True or false
     */
    boolean existsWithAccountsId(String validationResponsedId);

}
