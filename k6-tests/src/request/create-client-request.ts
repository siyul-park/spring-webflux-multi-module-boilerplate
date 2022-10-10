type CreateClientRequest = {
  name: string;
  type: 'public' | 'confidential';
  origins: string[];
};

export default CreateClientRequest;
