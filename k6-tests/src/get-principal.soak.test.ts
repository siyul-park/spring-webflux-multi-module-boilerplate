import { Options } from 'k6/options';

import { AuthGateway, GatewayAuthorization } from './gateway';
import client from './client';

export const options: Options = {
  stages: [
    { duration: '2m', target: 2000 },
    { duration: '3h56m', target: 2000 },
    { duration: '2m', target: 0 },
  ],
  thresholds: {
    'http_req_duration{type:POST_token}': ['max>=0'],
    'http_req_duration{type:GET_principal}': ['max>=0'],
  },
};

const authGateway = new AuthGateway();

const gatewayAuthorization = new GatewayAuthorization({
  grantType: 'client_credentials',
  clientId: client.id,
  clientSecret: client.secret,
});

export default () => {
  authGateway.read(gatewayAuthorization.getAuthorization());
};
