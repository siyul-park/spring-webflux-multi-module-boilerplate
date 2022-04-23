type ScopeTokenInfo = {
  id: string,
  name: string,
  description?: string,
  system: boolean,
  children?: ScopeTokenInfo[],
  createdAt: number;
  updatedAt?: number;
};

export default ScopeTokenInfo;