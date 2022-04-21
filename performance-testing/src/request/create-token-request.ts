type CreateTokenFromClientCredentials = {
    grantType: 'client_credentials';
    clientId: string;
    clientSecret: string;
}

type CreateTokenRequest = CreateTokenFromClientCredentials;

export default CreateTokenRequest;
