package uk.gov.companieshouse.account.validator.model.content;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

import java.util.Objects;

public class AccountsDetails {
    
    private PackageTypeApi packageType;
    private String companyNumber;

    public AccountsDetails() {
    }

    public AccountsDetails(PackageTypeApi packageType, String companyNumber) {
        this.companyNumber = companyNumber;
        this.packageType = packageType;
    }

    public PackageTypeApi getPackageType() {
        return packageType;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccountsDetails that = (AccountsDetails) o;
        return packageType == that.packageType && Objects.equals(companyNumber, that.companyNumber);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(packageType);
        result = 31 * result + Objects.hashCode(companyNumber);
        return result;
    }

    @Override
    public String toString() {
        return "AccountsDetails{" +
                "packageType=" + packageType +
                ", companyNumber='" + companyNumber + '\'' +
                '}';
    }
}
