package uk.gov.companieshouse.account.validator;

import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class MongoDbConnectionPoolProperties {

    private static final String MONGO_CONNECTION_POOL_MIN_SIZE_KEY = "MONGO_CONNECTION_POOL_MIN_SIZE";
    private static final String MONGO_CONNECTION_MAX_IDLE_KEY = "MONGO_CONNECTION_MAX_IDLE_TIME";
    private static final String MONGO_CONNECTION_MAX_LIFE_KEY = "MONGO_CONNECTION_MAX_LIFE_TIME";
    private static final String MONGODB_URL = "MONGODB_URL";

    private static final String DEFAULT_URL = "mongodb://mongo:27017";

    private int minSize;

    private int maxConnectionIdleTimeMS;

    private int maxConnectionLifeTimeMS;

    private String connectionString;

    /**
     * Constructs the config using environment variables for
     * Mongo Connection Pool settings. Sets default values in case
     * the environment variables are not supplied.
     */
    public MongoDbConnectionPoolProperties() {
        this.minSize = System.getenv(MONGO_CONNECTION_POOL_MIN_SIZE_KEY) != null ?
                Integer.parseInt(System.getenv(MONGO_CONNECTION_POOL_MIN_SIZE_KEY)) : 1;
        this.maxConnectionIdleTimeMS = System.getenv(MONGO_CONNECTION_MAX_IDLE_KEY) != null ?
                Integer.parseInt(System.getenv(MONGO_CONNECTION_MAX_IDLE_KEY)) : 0;
        this.maxConnectionLifeTimeMS = System.getenv(MONGO_CONNECTION_MAX_LIFE_KEY) != null ?
                Integer.parseInt(System.getenv(MONGO_CONNECTION_MAX_LIFE_KEY)) : 0;

        this.connectionString =
                Optional.ofNullable(System.getenv(MONGODB_URL))
                        .orElse(DEFAULT_URL);
    }

    public int getMinSize() {
        return minSize;
    }

    public int getMaxConnectionIdleTimeMS() {
        return maxConnectionIdleTimeMS;
    }

    public int getMaxConnectionLifeTimeMS() {
        return maxConnectionLifeTimeMS;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
