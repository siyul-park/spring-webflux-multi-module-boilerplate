[![build](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/build.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/build.yml)
[![check](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/check.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/check.yml)
[![k6-test](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/k6-test.yml/badge.svg)](https://github.com/siyual-park/spring-webflux-multi-module-boilerplate/actions/workflows/k6-test.yml)
[![codecov](https://codecov.io/gh/siyual-park/spring-webflux-multi-module-boilerplate/branch/master/graph/badge.svg?token=ICZfrp7K5c)](https://codecov.io/gh/siyual-park/spring-webflux-multi-module-boilerplate)

# Spring Webflux Multi-Module Boilerplate
Boilerplate of general service implemented using spring webflux and r2dbc  
<img width="1452" alt="스크린샷 2022-03-24 오후 8 54 48" src="https://user-images.githubusercontent.com/21099176/159911071-9e46e52b-f708-4443-9dba-731cc1372beb.png">

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
