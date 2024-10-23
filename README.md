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

## Benchmark

```shell
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/delete-clients_{client_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ POST /clients is status 201
     ✓ DELETE /clients/{client-id} is status 204
     checks.........................: 100.00% ✓ 2836       ✗ 0    
     data_received..................: 3.5 MB  330 kB/s
     data_sent......................: 676 kB  65 kB/s
     http_req_blocked...............: avg=2.06ms   min=2.48µs   med=4.48µs   max=52.84ms p(90)=7.96µs   p(95)=8.67ms  
     http_req_connecting............: avg=2.04ms   min=0s       med=0s       max=52.73ms p(90)=0s       p(95)=8.64ms  
     http_req_duration..............: avg=721.59ms min=19.33ms  med=563.75ms max=3.15s   p(90)=1.32s    p(95)=2.07s   
       { expected_response:true }...: avg=721.59ms min=19.33ms  med=563.75ms max=3.15s   p(90)=1.32s    p(95)=2.07s   
       { type:DELETE_clients_id }...: avg=432.5ms  min=25.36ms  med=418.38ms max=1.47s   p(90)=644.24ms p(95)=694.92ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 2836 
       { type:DELETE_clients_id }...: 0.00%   ✓ 0          ✗ 1318 
     http_req_receiving.............: avg=77.58µs  min=16.09µs  med=42.7µs   max=14.99ms p(90)=71.46µs  p(95)=85.83µs 
       { type:DELETE_clients_id }...: avg=73.6µs   min=16.09µs  med=34.15µs  max=14.99ms p(90)=55.29µs  p(95)=62.05µs 
     http_req_sending...............: avg=89µs     min=8µs      med=16.69µs  max=30.66ms p(90)=38.61µs  p(95)=358.83µs
       { type:DELETE_clients_id }...: avg=57.07µs  min=8µs      med=13.85µs  max=13.83ms p(90)=25.92µs  p(95)=37.27µs 
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s      p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=721.42ms min=19.23ms  med=563.51ms max=3.15s   p(90)=1.32s    p(95)=2.07s   
       { type:DELETE_clients_id }...: avg=432.37ms min=25.28ms  med=418.33ms max=1.47s   p(90)=644.18ms p(95)=694.85ms
     http_reqs......................: 2836    270.685185/s
       { type:DELETE_clients_id }...: 1318    125.797981/s
     iteration_duration.............: avg=1.56s    min=474.07ms med=1.13s    max=5.17s   p(90)=3.73s    p(95)=4.68s   
     iterations.....................: 1318    125.797981/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/delete-users_{user_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ POST /users is status 201
     ✓ DELETE /users/{user-id} is status 204
     checks.........................: 100.00% ✓ 5252       ✗ 0    
     data_received..................: 7.1 MB  685 kB/s
     data_sent......................: 1.2 MB  118 kB/s
     http_req_blocked...............: avg=319.92µs min=1.71µs   med=4.07µs   max=120.52ms p(90)=6.75µs   p(95)=11.52µs 
     http_req_connecting............: avg=143.98µs min=0s       med=0s       max=31.47ms  p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=382.53ms min=30.22ms  med=366.93ms max=1.03s    p(90)=575.67ms p(95)=656ms   
       { expected_response:true }...: avg=382.53ms min=30.22ms  med=366.93ms max=1.03s    p(90)=575.67ms p(95)=656ms   
       { type:DELETE_users_id }.....: avg=283.79ms min=39.73ms  med=281.28ms max=763.1ms  p(90)=380.93ms p(95)=408.79ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 5252 
       { type:DELETE_users_id }.....: 0.00%   ✓ 0          ✗ 2526 
     http_req_receiving.............: avg=3.41ms   min=14.79µs  med=39.37µs  max=233.34ms p(90)=323.23µs p(95)=12.62ms 
       { type:DELETE_users_id }.....: avg=2.43ms   min=14.79µs  med=30.89µs  max=233.34ms p(90)=295.38µs p(95)=10.84ms 
     http_req_sending...............: avg=564.23µs min=7.24µs   med=14.64µs  max=299.57ms p(90)=98.75µs  p(95)=163.11µs
       { type:DELETE_users_id }.....: avg=875.76µs min=7.24µs   med=12.94µs  max=299.57ms p(90)=71.1µs   p(95)=156.95µs
     http_req_tls_handshaking.......: avg=0s       min=0s       med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=378.54ms min=30.15ms  med=361.8ms  max=1.03s    p(90)=567.52ms p(95)=653.62ms
       { type:DELETE_users_id }.....: avg=280.48ms min=39.69ms  med=279.97ms max=571.88ms p(90)=378.04ms p(95)=400.7ms 
     http_reqs......................: 5252    504.577693/s
       { type:DELETE_users_id }.....: 2526    242.681503/s
     iteration_duration.............: avg=810.07ms min=192.79ms med=765.38ms max=1.98s    p(90)=1.04s    p(95)=1.29s   
     iterations.....................: 2526    242.681503/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200

         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/get-clients_{client_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ GET /clients/{client-id} is status 200
     █ setup
       ✓ POST /token client_credentials is status 201
       ✓ POST /clients is status 201
     checks.........................: 100.00% ✓ 7504       ✗ 0    
     data_received..................: 16 MB   1.6 MB/s
     data_sent......................: 1.4 MB  137 kB/s
     http_req_blocked...............: avg=1.05ms   min=1.59µs  med=3.87µs   max=392.95ms p(90)=5.52µs   p(95)=9.44µs  
     http_req_connecting............: avg=982.2µs  min=0s      med=0s       max=392.88ms p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=159.95ms min=1.69ms  med=91.09ms  max=1.67s    p(90)=393.37ms p(95)=665.76ms
       { expected_response:true }...: avg=159.95ms min=1.69ms  med=91.09ms  max=1.67s    p(90)=393.37ms p(95)=665.76ms
       { type:GET_clients_id }......: avg=145.13ms min=1.69ms  med=88.24ms  max=1.67s    p(90)=329.2ms  p(95)=537.82ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 7504 
       { type:GET_clients_id }......: 0.00%   ✓ 0          ✗ 7302 
     http_req_receiving.............: avg=40.19ms  min=14.95µs med=35.64µs  max=1.41s    p(90)=139.62ms p(95)=369.2ms 
       { type:GET_clients_id }......: avg=40.72ms  min=14.95µs med=35.47µs  max=1.41s    p(90)=144.39ms p(95)=370.78ms
     http_req_sending...............: avg=4.41ms   min=5.98µs  med=11.43µs  max=950.48ms p(90)=123.8µs  p(95)=1.46ms  
       { type:GET_clients_id }......: avg=3.78ms   min=5.98µs  med=11.38µs  max=950.48ms p(90)=50.88µs  p(95)=154.57µs
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=115.33ms min=1.64ms  med=84.27ms  max=957.75ms p(90)=227.52ms p(95)=278.97ms
       { type:GET_clients_id }......: avg=100.62ms min=1.64ms  med=82.08ms  max=548.12ms p(90)=212.96ms p(95)=252ms   
     http_reqs......................: 7504    741.396193/s
       { type:GET_clients_id }......: 7302    721.438567/s
     iteration_duration.............: avg=267.52ms min=2.7ms   med=146.83ms max=1.97s    p(90)=682.37ms p(95)=902.39ms
     iterations.....................: 7302    721.438567/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200
     
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/get-self.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ GET /self is status 200
     checks.........................: 100.00% ✓ 5147       ✗ 0    
     data_received..................: 27 MB   2.6 MB/s
     data_sent......................: 806 kB  79 kB/s
     http_req_blocked...............: avg=1.01ms   min=1.55µs  med=3.88µs   max=125.12ms p(90)=6.46µs   p(95)=11.32µs 
     http_req_connecting............: avg=931.66µs min=0s      med=0s       max=125.09ms p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=205.35ms min=1.21ms  med=53.69ms  max=2.29s    p(90)=685.72ms p(95)=891.19ms
       { expected_response:true }...: avg=205.35ms min=1.21ms  med=53.69ms  max=2.29s    p(90)=685.72ms p(95)=891.19ms
       { type:GET_self }............: avg=185.08ms min=1.21ms  med=50.95ms  max=2.15s    p(90)=641.78ms p(95)=823.53ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 5147 
       { type:GET_self }............: 0.00%   ✓ 0          ✗ 4947 
     http_req_receiving.............: avg=127.71ms min=17.68µs med=53.75µs  max=2.07s    p(90)=572.53ms p(95)=699.69ms
       { type:GET_self }............: avg=123.92ms min=17.68µs med=51.68µs  max=2.07s    p(90)=561.87ms p(95)=699.56ms
     http_req_sending...............: avg=5.06ms   min=4.66µs  med=11.43µs  max=1.2s     p(90)=136.58µs p(95)=1.37ms  
       { type:GET_self }............: avg=4.88ms   min=4.66µs  med=11.33µs  max=1.2s     p(90)=112.19µs p(95)=154.04µs
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=72.57ms  min=1.14ms  med=42.96ms  max=759.48ms p(90)=141.41ms p(95)=196.5ms 
       { type:GET_self }............: avg=56.27ms  min=1.14ms  med=40.51ms  max=345.7ms  p(90)=127.16ms p(95)=150.08ms
     http_reqs......................: 5147    503.8883/s
       { type:GET_self }............: 4947    484.308417/s
     iteration_duration.............: avg=395.66ms min=3.95ms  med=115.68ms max=3.97s    p(90)=1.14s    p(95)=1.42s   
     iterations.....................: 4947    484.308417/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200
     
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/get-users_{user_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token password is status 201
     ✓ GET /users/{user-id} is status 200
     █ setup
       ✓ POST /token client_credentials is status 201
       ✓ POST /users is status 201
     checks.........................: 100.00% ✓ 7528       ✗ 0    
     data_received..................: 11 MB   1.1 MB/s
     data_sent......................: 1.8 MB  175 kB/s
     http_req_blocked...............: avg=547.09µs min=1.62µs  med=3.94µs   max=120.16ms p(90)=6.19µs   p(95)=10.93µs 
     http_req_connecting............: avg=468.89µs min=0s      med=0s       max=82.62ms  p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=255.53ms min=1.58ms  med=208.12ms max=1.05s    p(90)=521.72ms p(95)=562.94ms
       { expected_response:true }...: avg=255.53ms min=1.58ms  med=208.12ms max=1.05s    p(90)=521.72ms p(95)=562.94ms
       { type:GET_users_id }........: avg=68.23ms  min=1.58ms  med=48.59ms  max=557.41ms p(90)=150.64ms p(95)=195.86ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 7528 
       { type:GET_users_id }........: 0.00%   ✓ 0          ✗ 3763 
     http_req_receiving.............: avg=7.5ms    min=19.9µs  med=37.96µs  max=560.1ms  p(90)=1.92ms   p(95)=44.82ms 
       { type:GET_users_id }........: avg=2.13ms   min=19.9µs  med=36.66µs  max=368.19ms p(90)=309.13µs p(95)=1.56ms  
     http_req_sending...............: avg=1.12ms   min=6.88µs  med=13.24µs  max=367.54ms p(90)=129.55µs p(95)=365.37µs
       { type:GET_users_id }........: avg=875.45µs min=6.88µs  med=11.17µs  max=194.27ms p(90)=119.71µs p(95)=183.99µs
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=246.9ms  min=1.38ms  med=198.73ms max=1.05s    p(90)=509.98ms p(95)=553.37ms
       { type:GET_users_id }........: avg=65.22ms  min=1.38ms  med=47.07ms  max=441.59ms p(90)=146.02ms p(95)=184.34ms
     http_reqs......................: 7528    728.989927/s
       { type:GET_users_id }........: 3763    364.398126/s
     iteration_duration.............: avg=536.96ms min=16.54ms med=532.33ms max=1.22s    p(90)=687.65ms p(95)=781.95ms
     iterations.....................: 3763    364.398126/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200
     
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/patch-clients_{client_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ POST /clients is status 201
     ✓ PATCH /clients/{client-id} is status 200
     checks.........................: 100.00% ✓ 5840       ✗ 0    
     data_received..................: 12 MB   1.2 MB/s
     data_sent......................: 1.6 MB  152 kB/s
     http_req_blocked...............: avg=1ms      min=1.89µs  med=4.03µs   max=132.91ms p(90)=7µs      p(95)=11.86µs 
     http_req_connecting............: avg=920.51µs min=0s      med=0s       max=92.01ms  p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=326.44ms min=5.12ms  med=328.33ms max=983.13ms p(90)=486.8ms  p(95)=549.6ms 
       { expected_response:true }...: avg=326.44ms min=5.12ms  med=328.33ms max=983.13ms p(90)=486.8ms  p(95)=549.6ms 
       { type:PATCH_clients_id }....: avg=307.16ms min=5.12ms  med=311.07ms max=970.39ms p(90)=470.58ms p(95)=515.41ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 5840 
       { type:PATCH_clients_id }....: 0.00%   ✓ 0          ✗ 2820 
     http_req_receiving.............: avg=19.72ms  min=19.88µs med=39.66µs  max=591.75ms p(90)=71.9ms   p(95)=147.3ms 
       { type:PATCH_clients_id }....: avg=18.85ms  min=19.94µs med=38.91µs  max=591.75ms p(90)=67.11ms  p(95)=147.54ms
     http_req_sending...............: avg=1.36ms   min=7.94µs  med=14.47µs  max=449.61ms p(90)=153.81µs p(95)=1.61ms  
       { type:PATCH_clients_id }....: avg=1.09ms   min=8.7µs   med=14.44µs  max=434.78ms p(90)=123.5µs  p(95)=175.74µs
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=305.36ms min=5.08ms  med=313.46ms max=815.77ms p(90)=442.68ms p(95)=488.47ms
       { type:PATCH_clients_id }....: avg=287.22ms min=5.08ms  med=299.57ms max=654.68ms p(90)=423.1ms  p(95)=464.33ms
     http_reqs......................: 5840    559.160664/s
       { type:PATCH_clients_id }....: 2820    270.005663/s
     iteration_duration.............: avg=723.73ms min=19.92ms med=705.07ms max=1.84s    p(90)=1.01s    p(95)=1.13s   
     iterations.....................: 2820    270.005663/s
     vus............................: 198     min=198      max=200
     vus_max........................: 200     min=200      max=200
     
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/patch-users_{user_id}.test.js
        output: -
     scenarios: (100.00%) 1 scenario, 200 max VUs, 40s max duration (incl. graceful stop):
              * default: 200 looping VUs for 10s (gracefulStop: 30s)
     ✓ POST /token client_credentials is status 201
     ✓ POST /users is status 201
     ✓ PATCH /users/{user-id} is status 200
     checks.........................: 100.00% ✓ 5894       ✗ 0    
     data_received..................: 14 MB   1.4 MB/s
     data_sent......................: 1.6 MB  153 kB/s
     http_req_blocked...............: avg=2.14ms   min=1.68µs  med=4.1µs    max=131.31ms p(90)=6.11µs   p(95)=11.67µs 
     http_req_connecting............: avg=2.04ms   min=0s      med=0s       max=101.94ms p(90)=0s       p(95)=0s      
     http_req_duration..............: avg=278.29ms min=2.71ms  med=239.44ms max=1.82s    p(90)=584.11ms p(95)=768.83ms
       { expected_response:true }...: avg=278.29ms min=2.71ms  med=239.44ms max=1.82s    p(90)=584.11ms p(95)=768.83ms
       { type:PATCH_users_id }......: avg=250.9ms  min=2.71ms  med=208.94ms max=1.82s    p(90)=475.38ms p(95)=740.45ms
     http_req_failed................: 0.00%   ✓ 0          ✗ 5894 
       { type:PATCH_users_id }......: 0.00%   ✓ 0          ✗ 2847 
     http_req_receiving.............: avg=60.41ms  min=17µs    med=41.06µs  max=1.02s    p(90)=190.93ms p(95)=515.69ms
       { type:PATCH_users_id }......: avg=57.23ms  min=17µs    med=41.22µs  max=1.02s    p(90)=182.95ms p(95)=495.39ms
     http_req_sending...............: avg=3.35ms   min=6.95µs  med=14.43µs  max=824.71ms p(90)=158.31µs p(95)=2.95ms  
       { type:PATCH_users_id }......: avg=2.51ms   min=8.06µs  med=14.53µs  max=824.71ms p(90)=139.69µs p(95)=225.47µs
     http_req_tls_handshaking.......: avg=0s       min=0s      med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...............: avg=214.52ms min=2.67ms  med=214.65ms max=1.77s    p(90)=363.57ms p(95)=420.44ms
       { type:PATCH_users_id }......: avg=191.15ms min=2.67ms  med=187.26ms max=1.77s    p(90)=340.2ms  p(95)=366.25ms
     http_reqs......................: 5894    570.626807/s
       { type:PATCH_users_id }......: 2847    275.631917/s
     iteration_duration.............: avg=716.07ms min=20.07ms med=654.11ms max=2.41s    p(90)=1.33s    p(95)=1.51s   
     iterations.....................: 2847    275.631917/s
     vus............................: 200     min=200      max=200
     vus_max........................: 200     min=200      max=200
     
         /\      Grafana   /‾‾/  
    /\  /  \     |\  __   /  /   
   /  \/    \    | |/ /  /   ‾‾\ 
  /          \   |   (  |  (‾)  |
 / __________ \  |_|\_\  \_____/ 
     execution: local
        script: ./k6-tests/dist/post-token.test.js
        output: -
     scenarios: (100.00%) 3 scenarios, 200 max VUs, 30s max duration (incl. graceful stop):
              * clientCredentials: 200 looping VUs for 10s (exec: clientCredentials)
              * password: 200 looping VUs for 10s (exec: password, startTime: 10s)
              * refreshToken: 200 looping VUs for 10s (exec: refreshToken, startTime: 20s)
     ✓ POST /token client_credentials is status 201
     ✓ POST /token password is status 201
     ✓ POST /token refresh_token is status 201
     █ setup
       ✓ POST /token client_credentials is status 201
       ✓ POST /users is status 201
     checks.............................: 100.00% ✓ 22476      ✗ 0    
     data_received......................: 12 MB   400 kB/s
     data_sent..........................: 6.8 MB  227 kB/s
     http_req_blocked...................: avg=648.43µs min=1.4µs    med=3.47µs   max=128.36ms p(90)=4.67µs   p(95)=5.87µs  
     http_req_connecting................: avg=626.68µs min=0s       med=0s       max=82.09ms  p(90)=0s       p(95)=0s      
     http_req_duration..................: avg=261.26ms min=3.44ms   med=252.38ms max=896.15ms p(90)=342.91ms p(95)=369.47ms
       { expected_response:true }.......: avg=261.26ms min=3.44ms   med=252.38ms max=896.15ms p(90)=342.91ms p(95)=369.47ms
       { scenario:clientCredentials }...: avg=255.78ms min=51.26ms  med=253.74ms max=354.41ms p(90)=303.98ms p(95)=318.56ms
       { scenario:password }............: avg=216.91ms min=121.3ms  med=217.83ms max=418.98ms p(90)=261.81ms p(95)=275.2ms 
       { scenario:refreshToken }........: avg=338.76ms min=5.27ms   med=330.96ms max=896.15ms p(90)=387.2ms  p(95)=404.12ms
     http_req_failed....................: 0.00%   ✓ 0          ✗ 22476
       { scenario:clientCredentials }...: 0.00%   ✓ 0          ✗ 7662 
       { scenario:password }............: 0.00%   ✓ 0          ✗ 9069 
       { scenario:refreshToken }........: 0.00%   ✓ 0          ✗ 5742 
     http_req_receiving.................: avg=89.73µs  min=18.46µs  med=37.58µs  max=63.11ms  p(90)=53.49µs  p(95)=97.65µs 
       { scenario:clientCredentials }...: avg=78.71µs  min=18.46µs  med=37.64µs  max=17.32ms  p(90)=52.61µs  p(95)=92.34µs 
       { scenario:password }............: avg=100.58µs min=19.53µs  med=36.11µs  max=63.11ms  p(90)=54.8µs   p(95)=190.46µs
       { scenario:refreshToken }........: avg=87.33µs  min=19.83µs  med=39.21µs  max=27.78ms  p(90)=53.08µs  p(95)=64.52µs 
     http_req_sending...................: avg=147.79µs min=6.61µs   med=12.85µs  max=94.98ms  p(90)=22.38µs  p(95)=114.45µs
       { scenario:clientCredentials }...: avg=259.8µs  min=7.19µs   med=12.68µs  max=32.68ms  p(90)=23.09µs  p(95)=120.28µs
       { scenario:password }............: avg=83.63µs  min=6.61µs   med=12.84µs  max=94.98ms  p(90)=22.24µs  p(95)=123.27µs
       { scenario:refreshToken }........: avg=99.72µs  min=7.54µs   med=13.05µs  max=57.62ms  p(90)=21.53µs  p(95)=49µs    
     http_req_tls_handshaking...........: avg=0s       min=0s       med=0s       max=0s       p(90)=0s       p(95)=0s      
     http_req_waiting...................: avg=261.02ms min=3.37ms   med=252.19ms max=895.67ms p(90)=342.78ms p(95)=369.38ms
       { scenario:clientCredentials }...: avg=255.44ms min=35.97ms  med=253.53ms max=354.37ms p(90)=303.9ms  p(95)=318.48ms
       { scenario:password }............: avg=216.73ms min=121.26ms med=217.62ms max=417.71ms p(90)=261.68ms p(95)=274.87ms
       { scenario:refreshToken }........: avg=338.57ms min=5.22ms   med=330.8ms  max=895.67ms p(90)=387.14ms p(95)=404.07ms
     http_reqs..........................: 22476   748.140336/s
       { scenario:clientCredentials }...: 7662    255.038764/s
       { scenario:password }............: 9069    301.872429/s
       { scenario:refreshToken }........: 5742    191.129285/s
     iteration_duration.................: avg=262.49ms min=5.55ms   med=253.5ms  max=920.39ms p(90)=343.77ms p(95)=370.9ms 
     iterations.........................: 22473   748.040478/s
     vus................................: 200     min=200      max=200
     vus_max............................: 200     min=200      max=200
```