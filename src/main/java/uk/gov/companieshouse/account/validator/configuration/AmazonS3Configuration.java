package uk.gov.companieshouse.account.validator.configuration;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.environment.EnvironmentReader;

@Configuration
public class AmazonS3Configuration {

    @Autowired
    private EnvironmentReader environmentReader;

    @Bean
    public AmazonS3 getAmazonS3() {
        return AmazonS3ClientBuilder
            .standard()
            .withRegion(getRegion())
            .withClientConfiguration(getClientConfiguration())
            .build();
    }

    private Regions getRegion() {
        return Regions.fromName(getRegionNameForAmazonS3());
    }

    /**
     * Create ClientConfiguration for a public protocol. the proxy's host and port are set if the
     * environment variables has been configured.
     *
     * @return A {@link ClientConfiguration}
     */
    private ClientConfiguration getClientConfiguration() {

        ClientConfiguration clientConfiguration = new ClientConfiguration();

        String proxyHost = getProxyHost();
        Integer proxyPort = getProxyPort();

        if (proxyHost != null && !proxyHost.trim().isEmpty()) {
            clientConfiguration.setProxyHost(proxyHost);
        }

        if (proxyPort != null) {
            clientConfiguration.setProxyPort(proxyPort);
        }
        clientConfiguration.setProtocol(Protocol.HTTPS);

        return clientConfiguration;
    }

    private Integer getProxyPort() {
        return environmentReader.getOptionalInteger("HTTP_URL_CONNECTION_PROXY_PORT");
    }

    private String getProxyHost() {
        return environmentReader.getOptionalString("IMAGE_CLOUD_PROXY_HOST");
    }

    private String getRegionNameForAmazonS3() {
        return environmentReader.getMandatoryString("REGION_NAME_FOR_AMAZON_S3");
    }
}
