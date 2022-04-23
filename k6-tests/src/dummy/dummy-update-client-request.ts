// @ts-ignore
import { randomString } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

import { UpdateClientRequest } from '../request';

function dummyUpdateClientRequest(): UpdateClientRequest {
  return {
    name: randomString(10),
  };
}

export default dummyUpdateClientRequest;
