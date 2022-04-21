import { sleep, check } from 'k6';
import { Options } from 'k6/options';
import http from 'k6/http';

import a from "./aa"

export let options: Options = {
  vus: 50,
  duration: '10s'
};

export default () => {
  console.debug(a());
  const res = http.get('https://test-api.k6.io');
  check(res, {
    'status is 200': () => res.status === 200,
  });
  sleep(1);
};
