import { Options } from 'k6/options';

import { AuthGateway, UserGateway } from './gateway';
import { TokenInfo, UserInfo } from './response';
import { dummyCreateUserRequest } from './dummy';

import matrixScenarios from './matrix-scenarios';
import client from './client';
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  scenarios: {
    clientCredentials: {
      vus: 200,
      duration: '10s',
      gracefulStop: '0s',
      exec: 'clientCredentials',
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
    refreshToken: {
      vus: 200,
      duration: '10s',
      gracefulStop: '0s',
      exec: 'refreshToken',
      executor: 'constant-vus',
      startTime: '20s',
    },
  },
};

matrixScenarios(options);

const authGateway = new AuthGateway();
const userGateway = new UserGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export function setup() {
  const token = authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret,
  });
  const createUserRequest = dummyCreateUserRequest();
  const user = userGateway.create(createUserRequest);

  return {
    token,
    user: {
      ...user,
      ...createUserRequest,
    },
  };
}

export function clientCredentials() {
  authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret,
  });
}

export function password({ user }: { user: UserInfo & { password: string } }) {
  authGateway.createToken({
    grantType: 'password',
    clientId: client.id,
    clientSecret: client.secret,
    username: user.name,
    password: user.password,
  });
}

export function refreshToken({ token }: { token: TokenInfo }) {
  const { refreshToken = '' } = token;
  authGateway.createToken({
    grantType: 'refresh_token',
    clientId: client.id,
    clientSecret: client.secret,
    refreshToken: refreshToken,
  });
}

