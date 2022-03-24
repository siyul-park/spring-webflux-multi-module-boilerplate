# Spring Webflux Multi-Module Boilerplate
Boilerplate of general service implemented using spring webflux and r2dbc  
<img width="1452" alt="스크린샷 2022-03-24 오후 8 54 48" src="https://user-images.githubusercontent.com/21099176/159911071-9e46e52b-f708-4443-9dba-731cc1372beb.png">

## Bussiness Function
- User/Client token issuance
- Scope(permission) management
- User/Client Scope management

## Foundation Function
- In-memory cache that supports transactions
- Persistence management of domain objects like JPA
- Writing/Reading Converter auto configuration
- Async Event Emitter
- RHS based filtering and sort parser
- Easy conversion between dtos
- Update patch that distinguishes between undefined and null
- Easy addition of new types of authentication principal
