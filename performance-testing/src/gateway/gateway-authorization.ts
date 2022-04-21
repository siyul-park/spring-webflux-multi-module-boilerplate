import AuthGateway from './auth-gateway';
import { TokenInfo } from '../response';

class GatewayAuthorization {
    private readonly authGateway = new AuthGateway();

    private tokens: TokenInfo | null = null;
    private createdAt: Date | null = null

    constructor(
        private readonly client: { id: string, secret: string }
    ) {
    }

    getAuthorization(): string {
        if (this.isExpired()) {
            this.tokens = this.authGateway.createToken({
                grantType: 'client_credentials',
                clientId: this.client.id,
                clientSecret: this.client.secret
            });
            this.createdAt = new Date();
        }

        return `${this.tokens?.tokenType} ${this.tokens?.accessToken}`
    }

    private isExpired(): boolean {
        if (this.tokens == null || this.createdAt == null) {
            return true;
        }

        return (this.createdAt.getUTCSeconds() + this.tokens.expiresIn - 60) <= new Date().getUTCSeconds()
    }
}

export default GatewayAuthorization;
