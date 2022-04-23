import http from 'k6/http';
import { check } from 'k6';

import { CreateClientRequest, CreateTokenRequest, UpdateClientRequest } from '../request';
import { ClientInfo } from '../response';
import { camelToSnake, snakeToCamel } from '../util';

import GatewayAuthorization from './gateway-authorization';
import log from './log';

const url = __ENV.URL;

class ClientGateway {
  private readonly gatewayAuthorization: GatewayAuthorization;

  constructor(request: CreateTokenRequest) {
    this.gatewayAuthorization = new GatewayAuthorization(request);
  }

  create(request: CreateClientRequest): ClientInfo {
    const response = http.post(
      `${url}/clients`,
      JSON.stringify(camelToSnake(request)),
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
          'Content-Type': 'application/json',
        },
        tags: { type: 'POST_clients' },
      },
    );

    log(response);
    check(response, {
      'POST /clients is status 201': (r) => r.status === 201,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as ClientInfo;
  }

  readAll(): ClientInfo[] {
    const response = http.get(
      `${url}/clients`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_clients' },
      },
    );

    log(response);
    check(response, {
      'GET /clients is status 200': (r) => r.status === 200,
    });

    return JSON.parse(response.body as string).map((it: Record<string, unknown>) => snakeToCamel(it) as ClientInfo);
  }

  readSelf(): ClientInfo {
    const response = http.get(
      `${url}/clients/self`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_clients_self' },
      },
    );

    log(response);
    check(response, {
      'GET /clients/self is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as ClientInfo;
  }

  read(clientId: string): ClientInfo {
    const response = http.get(
      `${url}/clients/${clientId}`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_clients_id' },
      },
    );

    log(response);
    check(response, {
      'GET /clients/{client-id} is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as ClientInfo;
  }

  update(clientId: string, request: UpdateClientRequest): ClientInfo {
    const response = http.patch(
      `${url}/clients/${clientId}`,
      JSON.stringify(camelToSnake(request)),
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
          'Content-Type': 'application/json',
        },
        tags: { type: 'PATCH_clients_id' },
      },
    );

    log(response);
    check(response, {
      'PATCH /clients/{client-id} is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as ClientInfo;
  }

  delete(clientId: string): void {
    const response = http.del(
      `${url}/clients/${clientId}`,
      null,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'DELETE_clients_id' },
      },
    );

    log(response);
    check(response, {
      'DELETE /clients/{client-id} is status 204': (r) => r.status === 204,
    });
  }
}

export default ClientGateway;
