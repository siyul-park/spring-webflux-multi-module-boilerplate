import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import { dummyCreateClientRequest } from "./dummy";

import client from './client';

export let options: Options = {
  vus: 200,
  duration: '10s',
  thresholds: {
    'http_req_duration{type:POST_clients}': ['max>=0'],
    'http_req_duration{type:DELETE_clients_id}': ['max>=0'],
  },
};

const clientGateway = new ClientGateway(client);

export default () => {
  const client = clientGateway.create(dummyCreateClientRequest());
  clientGateway.delete(client.id);
};
