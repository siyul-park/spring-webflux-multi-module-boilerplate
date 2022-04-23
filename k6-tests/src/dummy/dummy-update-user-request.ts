// @ts-ignore
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

import { UpdateUserRequest } from '../request';

function dummyUpdateUserRequest(): UpdateUserRequest {
  return {
    name: randomString(10),
  };
}

export default dummyUpdateUserRequest;
