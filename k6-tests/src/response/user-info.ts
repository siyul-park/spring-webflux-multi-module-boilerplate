type UserInfo = {
  id: string;
  name: string;
  contact?: { email: string };
  createdAt: number;
  updatedAt?: number;
};

export default UserInfo;
