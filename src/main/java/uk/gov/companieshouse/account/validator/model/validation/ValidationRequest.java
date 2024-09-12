package uk.gov.companieshouse.account.validator.model.validation;


import java.util.Objects;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

public final class ValidationRequest {
    private String fileName;
    private String id;
    private String customerId;
    private PackageTypeApi packageType;
        private String companyNumber;

    public ValidationRequest() {
    }

    ValidationRequest(String fileName, String id, String customerId, PackageTypeApi packageType, String companyNumber) {
        this.fileName = fileName;
        this.id = id;
        this.customerId = customerId;
        this.packageType = packageType;
        this.companyNumber = companyNumber;
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

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidationRequest that = (ValidationRequest) o;
        return Objects.equals(fileName, that.fileName) && Objects.equals(id, that.id) && Objects.equals(customerId, that.customerId) && packageType == that.packageType && Objects.equals(companyNumber, that.companyNumber);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(fileName);
        result = 31 * result + Objects.hashCode(id);
        result = 31 * result + Objects.hashCode(customerId);
        result = 31 * result + Objects.hashCode(packageType);
        result = 31 * result + Objects.hashCode(companyNumber);
        return result;
    }

    @Override
    public String toString() {
        return "ValidationRequest{" +
                "fileName='" + fileName + '\'' +
                ", id='" + id + '\'' +
                ", customerId='" + customerId + '\'' +
                ", packageType=" + packageType +
                ", companyNumber='" + companyNumber + '\'' +
                '}';
    }
}
