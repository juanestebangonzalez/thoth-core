export interface LoginRequest {
  username: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  password: string;
  email: string;
  role: string;
}

export interface ChangePasswordRequest {
  username: string;
  currentPassword: string;
  newPassword: string;
}

export interface AuthResponse {
  userId: string;
  token: string;
  username: string;
  role: string;
  message: string;
  passwordChangeRequired: boolean;
}