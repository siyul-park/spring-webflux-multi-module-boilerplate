import { Options } from 'k6/options';
import { group } from "k6";

import { AuthGateway } from './gateway';
import client from './client';

export let options: Options = {
  vus: 50,
  duration: '10s'
};

const authGateway = new AuthGateway();

export default () => {
  group("grantType: 'client_credentials'", () => {
    authGateway.createToken({
      grantType: 'client_credentials',
      clientId: client.id,
      clientSecret: client.secret
    });
  });
};
