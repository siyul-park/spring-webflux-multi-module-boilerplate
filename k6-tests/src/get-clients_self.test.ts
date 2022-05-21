import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import client from './client';
import matrixType from './matrix-type';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_clients_self']);

const clientGateway = new ClientGateway({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  clientGateway.readSelf();
};
