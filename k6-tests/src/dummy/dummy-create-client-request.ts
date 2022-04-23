// @ts-ignore
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

import { CreateClientRequest } from '../request';

function dummyCreateClientRequest(): CreateClientRequest {
  return {
    name: randomString(10),
    type: 'confidential',
    origin: 'http://localhost:8080',
  };
}

export default dummyCreateClientRequest;
