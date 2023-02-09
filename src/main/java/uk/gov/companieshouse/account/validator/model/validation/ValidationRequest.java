package uk.gov.companieshouse.account.validator.model.validation;


import java.util.Objects;

public final class ValidationRequest {
    private final String fileName;
    private final String id;
    private final String customerId;

    ValidationRequest(String fileName, String id, String customerId) {
        this.fileName = fileName;
        this.id = id;
        this.customerId = customerId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ValidationRequest) obj;
        return Objects.equals(this.fileName, that.fileName) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.customerId, that.customerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, id, customerId);
    }

    @Override
    public String toString() {
        return "ValidationRequest[" +
                "fileName=" + fileName + ", " +
                "id=" + id + ", " +
                "customerId=" + customerId + ']';
    }
}
