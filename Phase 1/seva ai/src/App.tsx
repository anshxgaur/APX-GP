import React from 'react';
import { BrowserRouter, Routes, Route, Navigate, useNavigate } from 'react-router-dom';
import { ToastProvider, Loader } from './components/UI';
import { AuthProvider, useAuth } from './context/AuthContext';
import { PlatformProvider } from './context/PlatformContext';
import { LandingPage } from './pages/LandingPage';
import { AuthPage } from './pages/AuthPage';
import { DashboardPage } from './pages/DashboardPage';

// ==========================================
// PROTECTED ROUTE GUARD
// ==========================================
const ProtectedRoute: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="min-h-screen bg-slate-50 flex flex-col items-center justify-center gap-3">
        <Loader size="lg" />
        <span className="text-xs font-bold text-slate-500 tracking-wider uppercase animate-pulse">
          Securing session pipeline...
        </span>
      </div>
    );
  }

  if (!isAuthenticated) {
    return <Navigate to="/auth" replace />;
  }

  return <>{children}</>;
};

// ==========================================
// NAVIGATION COORDINATOR
// ==========================================
const AppContent: React.FC = () => {
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  return (
    <Routes>
      {/* 1. Landing Page */}
      <Route 
        path="/" 
        element={
          <LandingPage 
            onNavigateToAuth={(mode) => {
              navigate('/auth');
            }} 
          />
        } 
      />
      
      {/* 2. OTP Authentication Gateway */}
      <Route 
        path="/auth" 
        element={
          isAuthenticated ? (
            <Navigate to="/dashboard" replace />
          ) : (
            <AuthPage 
              onBackToLanding={() => navigate('/')} 
              onAuthSuccess={() => navigate('/dashboard')} 
            />
          )
        } 
      />
      
      {/* 3. Protected Enterprise Dashboard */}
      <Route 
        path="/dashboard" 
        element={
          <ProtectedRoute>
            <DashboardPage />
          </ProtectedRoute>
        } 
      />
      
      {/* 4. Fallback / Wildcard Redirect */}
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

// ==========================================
// MAIN APP ROOT
// ==========================================
export default function App() {
  return (
    <ToastProvider>
      <AuthProvider>
        <PlatformProvider>
          <BrowserRouter>
            <AppContent />
          </BrowserRouter>
        </PlatformProvider>
      </AuthProvider>
    </ToastProvider>
  );
}
