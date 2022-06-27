import { Options } from 'k6/options';

import { AuthGateway, GatewayAuthorization } from './gateway';

import client from './client';
import matrixType from './matrix-type';
export { default as handleSummary } from './handle-summary';

export const options: Options = {
  vus: 200,
  duration: '10s',
};

matrixType(options, ['GET_self']);

const authGateway = new AuthGateway();

const gatewayAuthorization = new GatewayAuthorization({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  authGateway.read(gatewayAuthorization.getAuthorization());
};
