package uk.gov.companieshouse.account.validator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;

public interface RequestStatusRepository extends MongoRepository<RequestStatus, String> {

}