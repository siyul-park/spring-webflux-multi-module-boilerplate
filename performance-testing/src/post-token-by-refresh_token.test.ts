import { Options } from 'k6/options';

import { AuthGateway } from './gateway';
import client from './client';

export let options: Options = {
  vus: 50,
  duration: '10s'
};

const authGateway = new AuthGateway();

export function setup() {
  return authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
}

export default ({ refreshToken = '' }) => {
  authGateway.createToken({
    grantType: 'refresh_token',
    clientId: client.id,
    clientSecret: client.secret,
    refreshToken: refreshToken,
  });
};
