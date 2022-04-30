import http from 'k6/http';
import { check } from 'k6';

import { CreateTokenRequest, CreateUserRequest, UpdateClientRequest } from '../request';
import { UserInfo } from '../response';
import { camelToSnake, snakeToCamel } from '../util';

import GatewayAuthorization from './gateway-authorization';
import log from './log';

const url = __ENV.URL;

class UserGateway {
  private readonly gatewayAuthorization: GatewayAuthorization;

  constructor(request: CreateTokenRequest) {
    this.gatewayAuthorization = new GatewayAuthorization(request);
  }

  create(request: CreateUserRequest): UserInfo {
    const response = http.post(
      `${url}/users`,
      JSON.stringify(camelToSnake(request)),
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
          'Content-Type': 'application/json',
        },
        tags: { type: 'POST_users' },
      },
    );

    log(response);
    check(response, {
      'POST /users is status 201': (r) => r.status === 201,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as UserInfo;
  }

  readAll(): UserInfo[] {
    const response = http.get(
      `${url}/users`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_users' },
      },
    );

    log(response);
    check(response, {
      'GET /users is status 200': (r) => r.status === 200,
    });

    return JSON.parse(response.body as string).map((it: Record<string, unknown>) => snakeToCamel(it) as UserInfo);
  }

  readSelf(): UserInfo {
    const response = http.get(
      `${url}/users/self`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_users_self' },
      },
    );

    log(response);
    check(response, {
      'GET /users/self is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as UserInfo;
  }

  read(userId: string): UserInfo {
    const response = http.get(
      `${url}/users/${userId}`,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'GET_users_id' },
      },
    );

    log(response);
    check(response, {
      'GET /users/{user-id} is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as UserInfo;
  }

  update(userId: string, request: UpdateClientRequest): UserInfo {
    const response = http.patch(
      `${url}/users/${userId}`,
      JSON.stringify(camelToSnake(request)),
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
          'Content-Type': 'application/json',
        },
        tags: { type: 'PATCH_users_id' },
      },
    );

    log(response);
    check(response, {
      'PATCH /users/{user-id} is status 200': (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as UserInfo;
  }

  delete(userId: string): void {
    const response = http.del(
      `${url}/users/${userId}`,
      null,
      {
        headers: {
          'Authorization': this.gatewayAuthorization.getAuthorization(),
        },
        tags: { type: 'DELETE_users_id' },
      },
    );

    log(response);
    check(response, {
      'DELETE /users/{user-id} is status 204': (r) => r.status === 204,
    });
  }
}

export default UserGateway;
