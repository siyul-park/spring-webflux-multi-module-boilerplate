import http, { StructuredRequestBody } from 'k6/http';
import { check } from 'k6';

import { CreateTokenRequest } from '../request';
import { PrincipalInfo, TokenInfo } from '../response';
import { camelToSnake, snakeToCamel } from '../util';
import log from './log';

const url = __ENV.URL;

class AuthGateway {
  createToken(request: CreateTokenRequest): TokenInfo | null {
    const response = http.post(
      `${url}/token`,
      camelToSnake(request) as StructuredRequestBody,
      {
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        tags: { type: 'POST_token' },
      },
    );

    log(response);
    check(response, {
      [`POST /token ${request.grantType} is status 201`]: (r) => r.status === 201,
    });

    if (response.status !== 201) {
      return null;
    }
    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as TokenInfo;
  }

  read(authorization: string): PrincipalInfo {
    const response = http.get(
      `${url}/self`,
      {
        headers: {
          'Authorization': authorization,
        },
        tags: { type: 'GET_self' },
      },
    );

    log(response);
    check(response, {
      ['GET /self is status 200']: (r) => r.status === 200,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as PrincipalInfo;
  }
}

export default AuthGateway;
