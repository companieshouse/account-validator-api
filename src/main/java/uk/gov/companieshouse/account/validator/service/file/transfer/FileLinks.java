package uk.gov.companieshouse.account.validator.service.file.transfer;

import java.util.Objects;

public final class FileLinks {
    private String download;
    private String self;

    public FileLinks() {
    }

    FileLinks(String download, String self) {
        this.download = download;
        this.self = self;
    }

    public String getDownload() {
        return download;
    }

    public String getSelf() {
        return self;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FileLinks) obj;
        return Objects.equals(this.download, that.download) &&
                Objects.equals(this.self, that.self);
    }

    @Override
    public int hashCode() {
        return Objects.hash(download, self);
    }

    @Override
    public String toString() {
        return "FileLinks[" +
                "download=" + download + ", " +
                "self=" + self + ']';
    }
}
