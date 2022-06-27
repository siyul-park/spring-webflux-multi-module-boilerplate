import { Options } from 'k6/options';

import { ClientGateway } from './gateway';

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

matrixType(options, ['GET_clients']);

const clientGateway = new ClientGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  clientGateway.readAll();
};
