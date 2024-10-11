# account-validator-api
A backend service allowing users to test XBRL validation on their accounts.
This application is written using the [Spring Boot](http://projects.spring.io/spring-boot/) Java framework.

## Requirements
In order to run the API locally you'll need the following installed on your machine:

- [Java 21](https://corretto.aws/downloads/latest/amazon-corretto-21-x64-linux-jdk.tar.gz)
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

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


Application specific attributes | Value                                | Description
:---------|:-----------------------------------------------------------------------------|:-----------
**ECS Cluster**        |filing-maintain                                      | ECS cluster (stack) the service belongs to
**Load balancer**      |{env}-chs-internalapi                                            | The load balancer that sits in front of the service
**Concourse pipeline**     |[Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/account-validator-api) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/account-validator-api)                                  | Concourse pipeline link in shared services


### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
