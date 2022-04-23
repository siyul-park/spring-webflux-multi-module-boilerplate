// @ts-ignore
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

import { CreateUserRequest } from '../request';

function dummyCreateUserRequest(): CreateUserRequest {
  return {
    name: randomString(10),
    email: `${randomString(10)}@test.com`,
    password: randomString(10),
  };
}

export default dummyCreateUserRequest;
