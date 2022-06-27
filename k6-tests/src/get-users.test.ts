import { Options } from 'k6/options';

import { UserGateway } from './gateway';

import client from './client';
import matrixType from './matrix-type';
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

export default () => {
  userGateway.readAll();
};
