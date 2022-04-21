type CreateTokenFromClientCredentials = {
    grantType: 'client_credentials';
    clientId: string;
    clientSecret: string;
}

type CreateTokenFromRefreshToken = {
    grantType: 'refresh_token';
    clientId: string;
    clientSecret: string;
    refreshToken: string;
}

type CreateTokenRequest = CreateTokenFromClientCredentials | CreateTokenFromRefreshToken;

export default CreateTokenRequest;
