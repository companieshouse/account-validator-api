# account-validator-api
A backend service allowing users to test XBRL validation on their accounts.
This application is written using the [Spring Boot](http://projects.spring.io/spring-boot/) Java framework.

## Requirements
In order to run the API locally you'll need the following installed on your machine:

- [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)
- [MongoDB](https://www.mongodb.com)

## Getting Started
1. Run `make`
2. Run `./start.sh`

## Environment Variables
The supported environmental variables have been categorised by use case and are as follows.

### Code Analysis Variables
Name                   | Description                                                                                                                               | Mandatory | Default | Example
---------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- | --------- | ------- | ------------------
CODE_ANALYSIS_HOST_URL | The host URL of the code analysis server. See [here](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters)                        | ✓         |         | http://HOST:PORT
CODE_ANALYSIS_LOGIN    | The analysis server account to use when analysing or publishing. See [here](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters) | ✓         |         | login
CODE_ANALYSIS_PASSWORD | The analysis server account password. See [here](https://docs.sonarqube.org/display/SONAR/Analysis+Parameters)                            | ✓         |         | password

### Deployment Variables

Name                                   | Description                                                                          | Mandatory | Default | Example
-------------------------------------- |--------------------------------------------------------------------------------------| --------- | ------- | ----------------------------------------
IXBRL_VALIDATOR_BASE64_URI             | URL to IXBRL validator for Base64 encoded files                                      | ✓         |         | http://HOST/validateBase64
BUCKET_NAME                            | S3 BUCKET NAME                                                                       | ✓         |         | accounts-validator
DOCUMENT_BUCKET_NAME                   | Bucket name on s3                                                                    | ✓         |         | example-bucket
DOCUMENT_RENDER_SERVICE_HOST           | [Document render service](https://github.com/companieshouse/document-render-service) | ✓         |         | http://HOST:PORT
MONGODB_HOST                           | Mongo database host                                                                  | ✓         |         | HOST:PORT
MONGODB_PORT                           | Mongo database port                                                                  | ✓         |         | 1234
MONGODB_TRANSACTIONS_DATABASE          | MongoDB Transactions Database name.                                                  | ✓         |         | accounts_validator
MONGODB_URL                            | Mongo database URL.                                                                  | ✓         |         | mongodb://HOST:PORT/DATABASE
MONGO_CONNECTION_POOL_MIN_SIZE         | Mongo Database connection pool size (Min)                                            | ✗         | 0       | 1
MONGO_CONNECTION_MAX_IDLE_TIME         | Mongo Database connection idle time, 0 for no ideal time                             | ✗         | 0       | 0
MONGO_CONNECTION_MAX_LIFE_TIME         | Mongo Database connection life time, 0 for infinite life time.                       | ✗         | 0       | 0


### MondogDB End point for tests
#### Pre-requisite MongoDB up and running 
#### Database accounts_validator pre created

```
  POST
  http://localhost:18624/mock_validate
  body:
  {
    "customer_id" : "1234567890",
    "file_name" : "account.zip",
    "s3_key" : "697e40ba-cc6a-4b40-967c-6b4cdde8af23"
  }
```
