import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { UserInfo } from "./response";
import { dummyCreateUserRequest } from "./dummy";
import client from './client';

export let options: Options = {
  vus: 200,
  duration: '10s',
  thresholds: {
    'http_req_duration{type:POST_token}': ['max>=0'],
    'http_req_duration{type:POST_users}': ['max>=0'],
    'http_req_duration{type:GET_users_id}': ['max>=0'],
  },
};

export function setup() {
  const userGateway = new UserGateway({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
  const createUserRequest = dummyCreateUserRequest();
  const user = userGateway.create(createUserRequest);

  return {
    ...user,
    ...createUserRequest
  };
}

export default (user: UserInfo & { password: string }) => {
  const userGateway = new UserGateway({
    grantType: 'password',
    clientId: client.id,
    clientSecret: client.secret,
    username: user.name,
    password: user.password
  });
  userGateway.read(user.id);
};
