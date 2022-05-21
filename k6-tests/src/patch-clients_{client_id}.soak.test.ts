import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import { ClientInfo } from './response';
import { dummyCreateClientRequest, dummyUpdateClientRequest } from './dummy';

import client from './client';
import matrixType from './matrix-type';

export const options: Options = {
  stages: [
    { duration: '2m', target: 6000 },
    { duration: '3h56m', target: 6000 },
    { duration: '2m', target: 0 },
  ],
};

matrixType(options, ['PATCH_clients_id']);

const clientGateway = new ClientGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export function setup() {
  return clientGateway.create(dummyCreateClientRequest());
}

export default (client: ClientInfo) => {
  clientGateway.update(client.id, dummyUpdateClientRequest());
};
