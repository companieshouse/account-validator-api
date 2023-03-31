package uk.gov.companieshouse.account.validator.service.maintenance;
/**
 * An interface exposing the required methods to maintain company accounts
 */
public interface AccountMaintenanceStrategy {

    /**
     * Delete files.
     */
    public void deleteFiles();
}
