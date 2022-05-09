import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import client from './client';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  userGateway.readAll();
};