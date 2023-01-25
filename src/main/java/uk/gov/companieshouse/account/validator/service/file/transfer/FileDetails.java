package uk.gov.companieshouse.account.validator.service.file.transfer;

public record FileDetails(
        String id,
        String avTimestamp,
        AvStatus avStatus,
        String contentType,
        long size,
        String name,
        String createdOn,
        FileLinks links) {
}
