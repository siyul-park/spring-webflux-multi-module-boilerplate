import ScopeTokenInfo from './scope-token-info';

type ClientInfo = {
  id: string;
  name: string;
  type: 'public' | 'confidential';
  origins: string[];
  scope?: ScopeTokenInfo[],
  createdAt: number;
  updatedAt?: number;
};

export default ClientInfo;
