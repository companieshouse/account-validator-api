package uk.gov.companieshouse.account.validator.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.account.validator.model.AccountValidated;

import static org.springframework.data.mongodb.core.query.Criteria.where;

/**
 * Custom Implementation for methods not implemented in {@link MongoRepository}
 */
public class AccountValidatedRepositoryImpl implements AccountValidatedRepositoryCustom {

    private static final String ACCOUNTS_ID = "accounts_id";

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean existsWithAccountsId(String accountsId) {
        return mongoTemplate.exists(new Query().addCriteria(where(ACCOUNTS_ID).is(accountsId)), AccountValidated.class);
    }

}
