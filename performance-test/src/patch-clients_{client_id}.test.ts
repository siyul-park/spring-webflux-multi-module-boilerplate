import { Options } from 'k6/options';

import { ClientGateway } from './gateway';
import { ClientInfo } from "./response";
import { dummyCreateClientRequest, dummyUpdateClientRequest } from "./dummy";

import client from './client';

export let options: Options = {
  vus: 50,
  duration: '10s'
};

const clientGateway = new ClientGateway(client);

export function setup() {
  return clientGateway.create(dummyCreateClientRequest());
}

export default (client: ClientInfo) => {
  clientGateway.update(client.id, dummyUpdateClientRequest());
};
