import http from 'k6/http';
import { check } from 'k6';

import { CreateClientRequest } from '../request';
import { ClientInfo } from '../response';
import { camelToSnake, snakeToCamel } from '../util';

import GatewayAuthorization from './gateway-authorization';

const url = __ENV.URL;

class ClientGateway {
    private readonly gatewayAuthorization: GatewayAuthorization;

    constructor(client: { id: string, secret: string }) {
        this.gatewayAuthorization = new GatewayAuthorization(client);
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
            }
        );

        check(response, {
            'POST /clients is status 201': (r) => r.status === 201,
        });

        return snakeToCamel(JSON.parse(response.body as string) as Record<string, unknown>) as ClientInfo;
    }
}

export default ClientGateway;
