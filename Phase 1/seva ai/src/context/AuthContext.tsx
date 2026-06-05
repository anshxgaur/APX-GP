import React, { createContext, useContext, useState, useEffect } from 'react';
import { apiService, UserSession, UserRole } from '../services/api';
import { useToast } from '../components/UI';

interface AuthContextType {
  user: UserSession | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  requestOTP: (type: 'email' | 'phone', value: string) => Promise<{ success: boolean; expiresIn: number }>;
  verifyOTP: (value: string, otp: string, selectedRole?: UserRole) => Promise<boolean>;
  logout: () => void;
  authError: string | null;
  setAuthError: (err: string | null) => void;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserSession | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [authError, setAuthError] = useState<string | null>(null);
  const { showToast } = useToast();

  // Check session on mount
  useEffect(() => {
    const activeSession = apiService.getSession();
    if (activeSession) {
      setUser(activeSession);
    }
    setIsLoading(false);
  }, []);

  const requestOTP = async (type: 'email' | 'phone', value: string) => {
    setAuthError(null);
    try {
      const response = await apiService.requestOtp(type, value);
      showToast('success', `Verification code requested successfully. Code expires in 5 minutes.`, 'Code Dispatched');
      return response;
    } catch (err: any) {
      const errorMessage = err.message || "An error occurred while requesting your OTP. Please try again.";
      setAuthError(errorMessage);
      showToast('error', errorMessage, 'Request Failed');
      throw err;
    }
  };

  const verifyOTP = async (value: string, otp: string, selectedRole?: UserRole) => {
    setAuthError(null);
    try {
      const response = await apiService.verifyOtp(value, otp, selectedRole);
      setUser(response.user);
      showToast('success', `Welcome back, ${response.user.name}! You have logged in securely as a ${response.user.role.replace('_', ' ').toUpperCase()}.`, 'Authentication Successful');
      return true;
    } catch (err: any) {
      const errorMessage = err.message || "Invalid or expired verification code. Please try again.";
      setAuthError(errorMessage);
      showToast('error', errorMessage, 'Verification Failed');
      throw err;
    }
  };

  const logout = () => {
    apiService.logout();
    setUser(null);
    showToast('info', "You have been securely logged out. Session deleted.", "Logged Out");
  };

  const value: AuthContextType = {
    user,
    isAuthenticated: !!user,
    isLoading,
    requestOTP,
    verifyOTP,
    logout,
    authError,
    setAuthError,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within an AuthProvider');
  return context;
};
