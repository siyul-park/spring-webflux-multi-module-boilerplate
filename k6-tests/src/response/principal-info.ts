import ScopeTokenInfo from './scope-token-info';

type PrincipalInfo = {
  id: string;
  type: string;
  scope: ScopeTokenInfo[];
};

export default PrincipalInfo;
