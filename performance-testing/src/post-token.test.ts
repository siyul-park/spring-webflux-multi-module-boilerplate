import { Options } from 'k6/options';

import { AuthGateway } from './gateway';
import client from './client';

export let options: Options = {
  scenarios: {
    client_credentials: {
      vus: 50,
      duration: '10s',
      exec: 'client_credentials',
      executor: 'constant-vus',
    },
    refresh_token: {
      vus: 50,
      duration: '10s',
      exec: 'refresh_token',
      executor: 'constant-vus',
    },
  }
};

const authGateway = new AuthGateway();

export function setup() {
  return authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
}

export function client_credentials() {
  authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
}

export function refresh_token({ refreshToken = '' }) {
  authGateway.createToken({
    grantType: 'refresh_token',
    clientId: client.id,
    clientSecret: client.secret,
    refreshToken: refreshToken,
  });
}
