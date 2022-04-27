import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { dummyCreateUserRequest, dummyUpdateUserRequest } from './dummy';

import client from './client';
import matrixType from './matrix-type';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['POST_token', 'POST_users', 'PATCH_users_id']);

const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  const user = userGateway.create(dummyCreateUserRequest());
  userGateway.update(user.id, dummyUpdateUserRequest());
};
