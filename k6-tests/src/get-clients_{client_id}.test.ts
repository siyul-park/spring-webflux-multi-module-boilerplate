import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import { ClientInfo } from './response';
import { dummyCreateClientRequest } from './dummy';

import client from './client';
import matrixType from './matrix-type';
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_clients_id']);

const clientGateway = new ClientGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export function setup() {
  return clientGateway.create(dummyCreateClientRequest());
}

export default (client: ClientInfo) => {
  clientGateway.read(client.id);
};
