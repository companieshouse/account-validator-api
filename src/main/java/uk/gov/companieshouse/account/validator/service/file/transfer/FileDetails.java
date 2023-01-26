package uk.gov.companieshouse.account.validator.service.file.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;

public record FileDetails(
        String id,
        @JsonProperty("av_timestamp") String avTimestamp,
        @JsonProperty("av_status") AvStatus avStatus,
        @JsonProperty("content_type") String contentType,
        long size,
        String name,
        @JsonProperty("created_on") String createdOn,
        FileLinks links) {
}
