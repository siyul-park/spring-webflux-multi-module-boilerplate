import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { dummyCreateUserRequest } from './dummy';

import client from './client';

export const options: Options = {
  vus: 200,
  duration: '10s',
  thresholds: {
    'http_req_duration{type:POST_users}': ['max>=0'],
    'http_req_duration{type:DELETE_users_id}': ['max>=0'],
  },
};

const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  const user = userGateway.create(dummyCreateUserRequest());
  userGateway.delete(user.id);
};
