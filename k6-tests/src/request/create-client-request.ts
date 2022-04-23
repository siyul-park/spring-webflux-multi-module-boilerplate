type CreateClientRequest = {
  name: string;
  type: 'public' | 'confidential';
  origin: string;
};

export default CreateClientRequest;
