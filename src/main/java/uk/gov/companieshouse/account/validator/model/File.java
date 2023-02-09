package uk.gov.companieshouse.account.validator.model;

import java.util.Objects;

public final class File {
    private final String id;
    private final String name;
    private final byte[] data;

    public File(String id, String name, byte[] data) {
        this.id = id;
        this.name = name;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (File) obj;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, data);
    }

    @Override
    public String toString() {
        return "File[" +
                "id=" + id + ", " +
                "name=" + name + ", " +
                "data=" + data + ']';
    }
}
