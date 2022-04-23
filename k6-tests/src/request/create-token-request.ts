type CreateTokenFromClientCredentials = {
  grantType: 'client_credentials';
  clientId: string;
  clientSecret: string;
};

type CreateTokenFromRefreshToken = {
  grantType: 'refresh_token';
  clientId: string;
  clientSecret: string;
  refreshToken: string;
};

type CreateTokenFromPassword = {
  grantType: 'password';
  clientId: string;
  clientSecret: string;
  username: string;
  password: string;
};

type CreateTokenRequest = CreateTokenFromClientCredentials | CreateTokenFromRefreshToken | CreateTokenFromPassword;

export default CreateTokenRequest;
