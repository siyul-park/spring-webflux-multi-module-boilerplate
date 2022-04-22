import { Options } from 'k6/options';

import { UserGateway } from './gateway';
import { dummyUserClientRequest } from "./dummy";

import client from './client';

export let options: Options = {
  vus: 200,
  duration: '10s'
};

const userGateway = new UserGateway(client);

export default () => {
  userGateway.create(dummyUserClientRequest());
};
