type TokenInfo = {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
  refreshToken?: string;
};

export default TokenInfo;
