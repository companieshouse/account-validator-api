package uk.gov.companieshouse.account.validator.model.validation;


import java.util.Objects;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

public final class ValidationRequest {
    private String fileName;
    private String id;
    private String customerId;
    private PackageTypeApi packageType;

    public ValidationRequest() {
    }

    ValidationRequest(String fileName, String id, String customerId, PackageTypeApi packageType) {
        this.fileName = fileName;
        this.id = id;
        this.customerId = customerId;
        this.packageType = packageType;
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

    public PackageTypeApi getPackageType() {
        return packageType;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ValidationRequest) obj;
        return Objects.equals(this.fileName, that.fileName) &&
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.customerId, that.customerId) &&
                Objects.equals(this.packageType, that.packageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, id, customerId, packageType);
    }

    @Override
    public String toString() {
        return "ValidationRequest[" +
                "fileName=" + fileName + ", " +
                "id=" + id + ", " +
                "customerId=" + customerId + ", " +
                "packageType=" + packageType +  ']';
    }
}
