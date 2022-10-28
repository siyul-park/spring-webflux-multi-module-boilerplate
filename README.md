[![build](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/build.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/build.yml)
[![check](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/check.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/check.yml)
[![k6-test](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/k6-test.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/k6-test.yml)
[![codecov](https://codecov.io/gh/siyual-park/spring-webflux-multi-module-boilerplate/branch/master/graph/badge.svg?token=ICZfrp7K5c)](https://codecov.io/gh/siyual-park/spring-webflux-multi-module-boilerplate)
[![CodeFactor](https://www.codefactor.io/repository/github/siyual-park/spring-webflux-multi-module-boilerplate/badge)](https://www.codefactor.io/repository/github/siyual-park/spring-webflux-multi-module-boilerplate)

# Spring Webflux Multi-Module Boilerplate
Boilerplate of general service implemented using spring webflux and r2dbc  

## Requirements
- [JDK 11](https://openjdk.org/projects/jdk/11/)
- [Redis](https://redis.io/)
- [PostgreSQL](https://www.postgresql.org/)
- [MongoDB](https://www.mongodb.com/)

## Bussiness Function
- User/client token issuance
- Scope(permission) management
- User/client Scope management
- Basic user/client CRUD
- Dynamic client based allowed origin header configuration

## Foundation Function
- In-memory cache that supports transactions
- Persistence management of domain objects like JPA
- Writing/reading converter auto configuration
- Easy configurate custom validation for basic validate annotion
- Support async event emitter
- RHS based filter and sort parser
- Easy conversion between dtos
- Update patch that distinguishes between undefined and null
- Easy addition of new types of authentication principal

## Configuration
###
### Environments
You can set environment variables using system environment variables.

| Name                    | Description                     | Example                          |
|-------------------------|---------------------------------|----------------------------------|
| PORT                    | Server port                     | 8080                             |
| R2DBC_URL               | Set r2dbc url                   | r2dbc:h2:mem://./tmp/            |
| R2DBC_USERNAME          | Set r2dbc usernmae              | username                         |
| R2DBC_PASSWORD          | Set r2dbc password              | password                         |
| MONGODB_URI             | Set mongodb url                 | mongodb://localhost:27017/test   |
| MONGODB_EMBEDDED_ENABLE | Use embedded mongodb            | true                             |
| REDIS_URI               | Set redis url                   | redis://localhost:6379           |
| REDIS_EMBEDDED_ENABLE   | Use embedded redis              | true                             |
| DATA_LOGGING            | Set data log level              | INFO                             |
| ACCESS_TOKEN_AGE        | Set access token age            | 3600s                            |
| REFRESH_TOKEN_AGE       | Set refresh token age           | 259200s                          |
| MIGRATION_CLEAR         | Undo all migration before start | true                             |
| CLIENT_ROOT_NAME        | Set root client name            | root                             |
| CLIENT_ROOT_ID          | Set root client id(ulid)        | 01G1G1DN4JVHEKN7BHQH0F62TJ       |
| CLIENT_ROOT_SECRET      | Set root client password        | d9keQxhgVDDF8JJLDIPZ8uq159ffOFYy |
| CLIENT_ROOT_ORIGIN      | Set root client origin          | https://localhost:8080           |

### Stand-alone
If you want to run alone, set it as follows

| Name                    | Value                 |
|-------------------------|-----------------------|
| R2DBC_URL               | r2dbc:h2:mem://./tmp/ |
| MONGODB_EMBEDDED_ENABLE | true                  |
| REDIS_EMBEDDED_ENABLE   | true                  |
