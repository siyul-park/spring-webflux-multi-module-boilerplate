import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { UserInfo } from './response';
import { dummyCreateUserRequest } from './dummy';

import client from './client';
import matrixType from './matrix-type';
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_users_id']);

export function setup() {
  const userGateway = new UserGateway({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret,
  });
  const createUserRequest = dummyCreateUserRequest();
  const user = userGateway.create(createUserRequest);

  return {
    ...user,
    ...createUserRequest,
  };
}

export default (user: UserInfo & { password: string }) => {
  const userGateway = new UserGateway({
    grantType: 'password',
    clientId: client.id,
    clientSecret: client.secret,
    username: user.name,
    password: user.password,
  });
  userGateway.read(user.id);
};
