package uk.gov.companieshouse.account.validator.model.content;

import uk.gov.companieshouse.api.model.felixvalidator.PackageTypeApi;

public class FileAccountContent {
    
    private PackageTypeApi packageType;

    public FileAccountContent() {
    }

    public FileAccountContent(PackageTypeApi packageType) {
        this.packageType = packageType;
    }

    public PackageTypeApi getPackageType() {
        return packageType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((packageType == null) ? 0 : packageType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FileAccountContent other = (FileAccountContent) obj;
        if (packageType != other.packageType)
            return false;
        return true;
    }

    

}
