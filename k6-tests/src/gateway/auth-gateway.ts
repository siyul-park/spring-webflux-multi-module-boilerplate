import http, { StructuredRequestBody } from 'k6/http';
import { check } from 'k6';

import { CreateTokenRequest } from '../request';
import { TokenInfo } from '../response';
import { camelToSnake, snakeToCamel } from '../util';
import log from './log';

const url = __ENV.URL;

class AuthGateway {
  createToken(request: CreateTokenRequest): TokenInfo {
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
    log(response);
    check(response, {
      [`POST /token ${request.grantType} is status 201`]: (r) => r.status === 201,
    });

    return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as TokenInfo;
  }
}

export default AuthGateway;
