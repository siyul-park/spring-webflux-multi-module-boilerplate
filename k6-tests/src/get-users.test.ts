import { Options } from 'k6/options';

import { UserGateway } from './gateway';

import client from './client';
import matrixType from './matrix-type';
import { dummyCreateUserRequest } from "./dummy";
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_users']);

const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export function setup() {
  for (let i = 0; i < 100; i++) {
    userGateway.create(dummyCreateUserRequest());
  }
  return;
}

export default () => {
  userGateway.readAll();
};
