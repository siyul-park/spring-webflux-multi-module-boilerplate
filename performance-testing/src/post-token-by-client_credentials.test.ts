import { Options } from 'k6/options';

import { AuthGateway } from './gateway';
import client from './client';

export let options: Options = {
  vus: 50,
  duration: '10s'
};

const authGateway = new AuthGateway();

export default () => {
  authGateway.createToken({
    grantType: 'client_credentials',
    clientId: client.id,
    clientSecret: client.secret
  });
};
