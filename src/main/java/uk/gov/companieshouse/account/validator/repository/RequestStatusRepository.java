package uk.gov.companieshouse.account.validator.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import uk.gov.companieshouse.account.validator.model.validation.RequestStatus;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface RequestStatusRepository extends MongoRepository<RequestStatus, String> {

    List<RequestStatus> findByStatusInAndUpdatedDateTimeLessThan(Collection<String> status, LocalDate updated);

    List<RequestStatus> findByCreatedDateTimeIsNull();

}