package uk.gov.companieshouse.account.validator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.account.validator.model.AccountValidated;

/**
 * Repository interface for {@link AccountValidated}s required by Spring
 */
public interface AccountValidatedRepository extends MongoRepository<AccountValidated, String>,
                                                   AccountValidatedRepositoryCustom {}
