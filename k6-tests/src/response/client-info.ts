type ClientInfo = {
  id: string;
  name: string;
  type: 'public' | 'confidential';
  origin: string;
  createdAt: number;
  updatedAt?: number;
};

export default ClientInfo;
