import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import { dummyCreateClientRequest } from './dummy';

import client from './client';

export let options: Options = {
  vus: 200,
  duration: '10s'
};

const clientGateway = new ClientGateway(client);

export default () => {
  clientGateway.create(dummyCreateClientRequest());
};
