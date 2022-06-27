import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { dummyCreateUserRequest, dummyUpdateUserRequest } from './dummy';

import client from './client';
import matrixType from './matrix-type';
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  stages: [
    { duration: '2m', target: 6000 },
    { duration: '3h56m', target: 6000 },
    { duration: '2m', target: 0 },
  ],
};

matrixType(options, ['PATCH_users_id']);

const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  const user = userGateway.create(dummyCreateUserRequest());
  userGateway.update(user.id, dummyUpdateUserRequest());
};
