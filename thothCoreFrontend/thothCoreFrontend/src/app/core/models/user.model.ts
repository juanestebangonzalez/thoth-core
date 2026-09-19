export interface User {
  id: string;
  username: string;
  email: string;
  role: string;
  enabled: boolean;
  createdAt: string;
}

export interface ResetPasswordResult {
  newPassword: string;
  message: string;
  success: boolean;
}

export interface UpdateResult {
  message: string;
  success: boolean;
}