import { Options } from 'k6/options';

import { AuthGateway, UserGateway } from './gateway';
import { TokenInfo, UserInfo } from "./response";
import { dummyCreateUserRequest } from './dummy';

import matrixScenarios from './matrix-scenarios';
import client from './client';

export let options: Options = {
  scenarios: {
    client_credentials: {
      vus: 200,
      duration: '10s',
      gracefulStop: '0s',
      exec: 'client_credentials',
      executor: 'constant-vus',
    },
    password: {
      vus: 200,
      duration: '10s',
      gracefulStop: '0s',
      exec: 'password',
      executor: 'constant-vus',
      startTime: '10s',
    },
    refresh_token: {
      vus: 200,
      duration: '10s',
      gracefulStop: '0s',
      exec: 'refresh_token',
      executor: 'constant-vus',
      startTime: '20s',
    },
  }
};

matrixScenarios(options);

const authGateway = new AuthGateway();
const userGateway = new UserGateway(client);

export function setup() {
  const token = authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
  const createUserRequest = dummyCreateUserRequest();
  const user = userGateway.create(createUserRequest);

  return {
    token,
    user: {
      ...user,
      ...createUserRequest
    },
  }
}

export function client_credentials() {
  authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
}

export function password({ user }: { user: UserInfo & { password: string } }) {
  authGateway.createToken({
    grantType: 'password',
    clientId: client.id,
    clientSecret: client.secret,
    username: user.name,
    password: user.password
  });
}

export function refresh_token({ token }: { token: TokenInfo }) {
  const { refreshToken = '' } = token;
  authGateway.createToken({
    grantType: 'refresh_token',
    clientId: client.id,
    clientSecret: client.secret,
    refreshToken: refreshToken,
  });
}

