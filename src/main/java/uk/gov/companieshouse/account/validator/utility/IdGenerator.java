package uk.gov.companieshouse.account.validator.utility;

/**
 * Generates an id given a string
 */
public interface IdGenerator {


    /**
     * Generate an id
     *
     * @return A {@link String}
     */
    String generate(String key);

    /**
     * Generate a random id
     * @return A {@link String}
     */
    String generateRandom();
}

