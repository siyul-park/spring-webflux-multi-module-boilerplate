import ScopeTokenInfo from './scope-token-info';

type UserInfo = {
  id: string;
  name: string;
  email: string;
  scope?: ScopeTokenInfo[],
  createdAt: number;
  updatedAt?: number;
};

export default UserInfo;
