package uk.gov.companieshouse.account.validator.service.file.transfer;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public final class FileDetails {
    private String id;

    @JsonProperty("av_timestamp")
    private String avTimestamp;

    @JsonProperty("av_status")
    private AvStatus avStatus;

    @JsonProperty("content_type")
    private String contentType;

    private long size;
    private String name;

    @JsonProperty("created_on")
    private String createdOn;

    private FileLinks links;

    public FileDetails() {
    }

    public FileDetails(String id, String avTimestamp, AvStatus avStatus, String contentType, long size, String name, String createdOn, FileLinks links) {
        this.id = id;
        this.avTimestamp = avTimestamp;
        this.avStatus = avStatus;
        this.contentType = contentType;
        this.size = size;
        this.name = name;
        this.createdOn = createdOn;
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public String getAvTimestamp() {
        return avTimestamp;
    }

    public AvStatus getAvStatus() {
        return avStatus;
    }

    public String getContentType() {
        return contentType;
    }

    public long getSize() {
        return size;
    }

    public String getName() {
        return name;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public FileLinks getLinks() {
        return links;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileDetails that = (FileDetails) o;
        return size == that.size && Objects.equals(id, that.id) && Objects.equals(avTimestamp, that.avTimestamp) && avStatus == that.avStatus && Objects.equals(contentType, that.contentType) && Objects.equals(name, that.name) && Objects.equals(createdOn, that.createdOn) && Objects.equals(links, that.links);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, avTimestamp, avStatus, contentType, size, name, createdOn, links);
    }

    @Override
    public String toString() {
        return "FileDetails{" +
                "id='" + id + '\'' +
                ", avTimestamp='" + avTimestamp + '\'' +
                ", avStatus=" + avStatus +
                ", contentType='" + contentType + '\'' +
                ", size=" + size +
                ", name='" + name + '\'' +
                ", createdOn='" + createdOn + '\'' +
                ", links=" + links +
                '}';
    }
}
