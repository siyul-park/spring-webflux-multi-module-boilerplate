import { Options } from 'k6/options';

import { ClientGateway } from './gateway';

import client from './client';
import matrixType from './matrix-type';
import { dummyCreateClientRequest } from "./dummy";
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_clients']);

const clientGateway = new ClientGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export function setup() {
  for (let i = 0; i < 100; i++) {
    clientGateway.create(dummyCreateClientRequest());
  }
  return;
}

export default () => {
  clientGateway.readAll();
};
