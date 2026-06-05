import React, { createContext, useContext, useState, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { X, CheckCircle, AlertTriangle, Info, Bell, Phone, Mail } from 'lucide-react';

// ==========================================
// 0. LOGO COMPONENT (CommuniLink Brand Mark)
// ==========================================
interface LogoProps {
  className?: string;
  size?: number;
}

export const Logo: React.FC<LogoProps> = ({ className = '', size = 36 }) => {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 32 32"
      fill="none"
      xmlns="http://www.w3.org/2000/svg"
      className={`${className} transition-all duration-300 hover:rotate-6 shrink-0`}
    >
      <defs>
        {/* Refined emerald green to teal gradient for leaves */}
        <linearGradient id="leafGrad" x1="0%" y1="100%" x2="100%" y2="0%">
          <stop offset="0%" stopColor="#0F766E" /> {/* Teal */}
          <stop offset="50%" stopColor="#0D9488" /> {/* Teal-medium */}
          <stop offset="100%" stopColor="#10B981" /> {/* Emerald */}
        </linearGradient>
        
        {/* Teal-blue to royal blue gradient for the human figure head */}
        <linearGradient id="headGrad" x1="0%" y1="0%" x2="100%" y2="100%">
          <stop offset="0%" stopColor="#06B6D4" /> {/* Teal-blue */}
          <stop offset="100%" stopColor="#1D4ED8" /> {/* Blue */}
        </linearGradient>
      </defs>

      {/* Symmetrical green leaves forming open caring hands or a blooming flower */}
      {/* Left Leaf / Hand */}
      <path 
        d="M16 26C11.5 22 6.5 15 8 9.5C9 6 12 7 14 10.5C15.2 12.6 16 16.5 16 26Z" 
        fill="url(#leafGrad)" 
      />
      
      {/* Right Leaf / Hand */}
      <path 
        d="M16 26C20.5 22 25.5 15 24 9.5C23 6 20 7 18 10.5C16.8 12.6 16 16.5 16 26Z" 
        fill="url(#leafGrad)" 
      />
      
      {/* Subtle white sparkle/star line accents inside each leaf to suggest growth and care */}
      {/* Left Star Accent */}
      <path 
        d="M10.5 13.5L12.5 15.5M12.5 13.5L10.5 15.5" 
        stroke="#FFFFFF" 
        strokeWidth="1.2" 
        strokeLinecap="round" 
        opacity="0.95" 
      />
      
      {/* Right Star Accent */}
      <path 
        d="M19.5 13.5L21.5 15.5M21.5 13.5L19.5 15.5" 
        stroke="#FFFFFF" 
        strokeWidth="1.2" 
        strokeLinecap="round" 
        opacity="0.95" 
      />
      
      {/* Circular teal-blue element above the leaves forming a human figure symbol */}
      <circle 
        cx="16" 
        cy="5.5" 
        r="3" 
        fill="url(#headGrad)" 
      />
    </svg>
  );
};

// ==========================================
// 1. BUTTON COMPONENT
// ==========================================
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'success' | 'danger' | 'outline' | 'ghost';
  size?: 'sm' | 'md' | 'lg';
  isLoading?: boolean;
  icon?: React.ReactNode;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  isLoading = false,
  icon,
  className = '',
  disabled,
  ...props
}) => {
  const baseStyles = 'inline-flex items-center justify-center font-medium rounded-lg transition-all duration-200 focus:outline-none focus:ring-2 focus:ring-offset-2 disabled:opacity-50 disabled:pointer-events-none cursor-pointer hover:-translate-y-0.5 active:scale-98';
  
  const variants = {
    primary: 'bg-blue-700 hover:bg-blue-800 text-white focus:ring-blue-500',
    secondary: 'bg-teal-700 hover:bg-teal-800 text-white focus:ring-teal-500',
    success: 'bg-green-600 hover:bg-green-700 text-white focus:ring-green-500',
    danger: 'bg-red-600 hover:bg-red-700 text-white focus:ring-red-500',
    outline: 'border border-slate-300 bg-white hover:bg-slate-50 text-slate-700 focus:ring-slate-500',
    ghost: 'hover:bg-slate-100 text-slate-600 focus:ring-slate-500',
  };

  const sizes = {
    sm: 'px-3 py-1.5 text-xs gap-1.5',
    md: 'px-5 py-2.5 text-sm gap-2',
    lg: 'px-6 py-3 text-base gap-2.5',
  };

  return (
    <button
      className={`${baseStyles} ${variants[variant]} ${sizes[size]} ${className}`}
      disabled={disabled || isLoading}
      {...props}
    >
      {isLoading ? (
        <svg className="animate-spin -ml-1 mr-2 h-4 w-4 text-current" fill="none" viewBox="0 0 24 24">
          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
        </svg>
      ) : icon ? (
        <span className="flex-shrink-0">{icon}</span>
      ) : null}
      {children}
    </button>
  );
};

// ==========================================
// 2. INPUT COMPONENT
// ==========================================
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
  helperText?: string;
  icon?: React.ReactNode;
}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ label, error, helperText, icon, className = '', id, ...props }, ref) => {
    const inputId = id || `input-${Math.random().toString(36).substr(2, 9)}`;

    return (
      <div className="w-full flex flex-col gap-1.5">
        {label && (
          <label htmlFor={inputId} className="text-xs font-semibold text-slate-700 tracking-wide uppercase">
            {label}
          </label>
        )}
        <div className="relative">
          {icon && (
            <div className="absolute left-3.5 top-1/2 -translate-y-1/2 text-slate-400">
              {icon}
            </div>
          )}
          <input
            id={inputId}
            ref={ref}
            className={`w-full bg-white border ${
              error ? 'border-red-500 focus:ring-red-500 focus:border-red-500' : 'border-slate-200 focus:ring-blue-500 focus:border-blue-500'
            } rounded-lg ${icon ? 'pl-10' : 'px-4'} py-2.5 text-slate-800 placeholder-slate-400 text-sm focus:outline-none focus:ring-2 focus:ring-offset-0 transition-all ${className}`}
            aria-invalid={error ? 'true' : 'false'}
            aria-describedby={error ? `${inputId}-error` : helperText ? `${inputId}-helper` : undefined}
            {...props}
          />
        </div>
        {error && (
          <p id={`${inputId}-error`} role="alert" className="text-xs font-medium text-red-600 flex items-center gap-1 mt-0.5">
            <AlertTriangle size={12} /> {error}
          </p>
        )}
        {!error && helperText && (
          <p id={`${inputId}-helper`} className="text-xs text-slate-500">
            {helperText}
          </p>
        )}
      </div>
    );
  }
);

Input.displayName = 'Input';

// ==========================================
// 3. CARD COMPONENT
// ==========================================
interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  hoverLift?: boolean;
}

export const Card: React.FC<CardProps> = ({ children, hoverLift = false, className = '', ...props }) => {
  return (
    <div
      className={`bg-white rounded-xl border border-slate-100 shadow-xs p-6 ${
        hoverLift ? 'hover:shadow-md hover:-translate-y-1 transition-all duration-300' : ''
      } ${className}`}
      {...props}
    >
      {children}
    </div>
  );
};

// ==========================================
// 4. MODAL COMPONENT
// ==========================================
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title: string;
  children: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
  // Handle escape key to close modal
  useEffect(() => {
    const handleEscape = (e: KeyboardEvent) => {
      if (e.key === 'Escape') onClose();
    };
    if (isOpen) {
      document.body.style.overflow = 'hidden';
      window.addEventListener('keydown', handleEscape);
    }
    return () => {
      document.body.style.overflow = 'unset';
      window.removeEventListener('keydown', handleEscape);
    };
  }, [isOpen, onClose]);

  return (
    <AnimatePresence>
      {isOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          {/* Backdrop */}
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={onClose}
            className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs"
            aria-hidden="true"
          />

          {/* Modal Content */}
          <motion.div
            initial={{ scale: 0.95, opacity: 0, y: 15 }}
            animate={{ scale: 1, opacity: 1, y: 0 }}
            exit={{ scale: 0.95, opacity: 0, y: 15 }}
            transition={{ type: 'spring', duration: 0.4 }}
            role="dialog"
            aria-modal="true"
            aria-labelledby="modal-title"
            className="relative w-full max-w-lg bg-white rounded-xl shadow-xl border border-slate-100 overflow-hidden z-10 flex flex-col max-h-[90vh]"
          >
            {/* Header */}
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-100 bg-slate-50/50">
              <h3 id="modal-title" className="text-base font-bold text-slate-800">
                {title}
              </h3>
              <button
                onClick={onClose}
                className="text-slate-400 hover:text-slate-600 hover:bg-slate-100 p-1.5 rounded-lg transition-colors cursor-pointer"
                aria-label="Close modal"
              >
                <X size={18} />
              </button>
            </div>

            {/* Body */}
            <div className="p-6 overflow-y-auto flex-1">
              {children}
            </div>
          </motion.div>
        </div>
      )}
    </AnimatePresence>
  );
};

// ==========================================
// 5. TOAST NOTIFICATION SYSTEM
// ==========================================
export interface Toast {
  id: string;
  type: 'success' | 'error' | 'info' | 'otp';
  message: string;
  title?: string;
  otpData?: {
    type: 'email' | 'phone';
    value: string;
    otp: string;
    resends: number;
  };
}

interface ToastContextType {
  toasts: Toast[];
  showToast: (type: Toast['type'], message: string, title?: string, otpData?: Toast['otpData']) => void;
  removeToast: (id: string) => void;
}

const ToastContext = createContext<ToastContextType | undefined>(undefined);

export const ToastProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [toasts, setToasts] = useState<Toast[]>([]);

  const showToast = (type: Toast['type'], message: string, title?: string, otpData?: Toast['otpData']) => {
    const id = Math.random().toString(36).substr(2, 9);
    setToasts((prev) => [...prev, { id, type, message, title, otpData }]);

    // Auto-dismiss standard toasts after 5 seconds, leave OTP notifications slightly longer (12s)
    const duration = type === 'otp' ? 15000 : 5000;
    setTimeout(() => {
      removeToast(id);
    }, duration);
  };

  const removeToast = (id: string) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  };

  // Listen to mock telecom OTP events emitted by our apiService
  useEffect(() => {
    const handleMockOTP = (e: Event) => {
      const customEvent = e as CustomEvent<NonNullable<Toast['otpData']>>;
      const { type, value, otp, resends } = customEvent.detail;
      
      const channelLabel = type === 'email' ? 'Email Address' : 'Phone Number';
      const channelIcon = type === 'email' ? '📧' : '💬';
      
      showToast(
        'otp',
        `Simulated secure verification code sent to ${value}. Code is valid for 5 minutes.`,
        `Incoming Mock ${type === 'email' ? 'Email' : 'SMS'} Gateway`,
        { type, value, otp, resends }
      );
    };

    window.addEventListener('sevaai_mock_otp_delivered', handleMockOTP);
    return () => {
      window.removeEventListener('sevaai_mock_otp_delivered', handleMockOTP);
    };
  }, []);

  return (
    <ToastContext.Provider value={{ toasts, showToast, removeToast }}>
      {children}
      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </ToastContext.Provider>
  );
};

export const useToast = () => {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast must be used within a ToastProvider');
  return context;
};

const ToastContainer: React.FC<{ toasts: Toast[]; removeToast: (id: string) => void }> = ({ toasts, removeToast }) => {
  return (
    <div className="fixed bottom-5 right-5 z-50 flex flex-col gap-3 w-full max-w-sm sm:max-w-md pointer-events-none px-4 sm:px-0">
      <AnimatePresence>
        {toasts.map((toast) => (
          <motion.div
            key={toast.id}
            layout
            initial={{ opacity: 0, y: 30, scale: 0.9 }}
            animate={{ opacity: 1, y: 0, scale: 1 }}
            exit={{ opacity: 0, scale: 0.85, transition: { duration: 0.2 } }}
            className={`pointer-events-auto w-full rounded-xl border shadow-lg overflow-hidden flex flex-col ${
              toast.type === 'success' ? 'bg-white border-green-100 text-slate-800' :
              toast.type === 'error' ? 'bg-white border-red-100 text-slate-800' :
              toast.type === 'otp' ? 'bg-slate-900 border-teal-500/30 text-white shadow-teal-500/10' :
              'bg-white border-blue-100 text-slate-800'
            }`}
          >
            {/* Standard Toast Body */}
            {toast.type !== 'otp' ? (
              <div className="p-4 flex gap-3">
                <div className="mt-0.5 flex-shrink-0">
                  {toast.type === 'success' && <CheckCircle className="text-green-500" size={20} />}
                  {toast.type === 'error' && <AlertTriangle className="text-red-500" size={20} />}
                  {toast.type === 'info' && <Info className="text-blue-500" size={20} />}
                </div>
                <div className="flex-1">
                  {toast.title && <h4 className="text-sm font-bold text-slate-800">{toast.title}</h4>}
                  <p className="text-xs text-slate-600 mt-0.5 leading-relaxed">{toast.message}</p>
                </div>
                <button
                  onClick={() => removeToast(toast.id)}
                  className="text-slate-400 hover:text-slate-600 p-0.5 self-start cursor-pointer"
                >
                  <X size={16} />
                </button>
              </div>
            ) : (
              /* High Fidelity Mock Telecom Gateway Toast */
              <div className="p-4 flex flex-col gap-3.5">
                <div className="flex justify-between items-center pb-2 border-b border-slate-800">
                  <div className="flex items-center gap-2">
                    <div className="p-1.5 rounded-md bg-teal-500/10 text-teal-400">
                      {toast.otpData?.type === 'email' ? <Mail size={16} /> : <Phone size={16} />}
                    </div>
                    <span className="text-xs font-bold tracking-wider text-slate-400 uppercase">
                      {toast.title}
                    </span>
                  </div>
                  <button
                    onClick={() => removeToast(toast.id)}
                    className="text-slate-500 hover:text-slate-300 p-0.5 cursor-pointer"
                  >
                    <X size={16} />
                  </button>
                </div>

                <div className="flex flex-col gap-2">
                  <p className="text-xs text-slate-300 leading-relaxed">
                    Incoming secure code dispatched to <strong className="text-teal-400">{toast.otpData?.value}</strong>:
                  </p>
                  
                  {/* Digital OTP Card */}
                  <div className="bg-slate-950/80 rounded-lg p-3 border border-slate-800/80 flex items-center justify-between mt-1">
                    <div className="flex flex-col">
                      <span className="text-[9px] font-bold tracking-widest text-slate-500 uppercase">Verification Code</span>
                      <span className="text-2xl font-mono font-black tracking-widest text-teal-400">
                        {toast.otpData?.otp}
                      </span>
                    </div>
                    <button
                      onClick={() => {
                        navigator.clipboard.writeText(toast.otpData?.otp || '');
                        // Visual feedback can be shown, but simple alert or copy works
                        alert("OTP copied to clipboard!");
                      }}
                      className="px-2.5 py-1 text-[10px] bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold rounded-md transition-colors cursor-pointer"
                    >
                      Copy Code
                    </button>
                  </div>
                </div>

                <div className="flex items-center justify-between text-[10px] text-slate-400 pt-1">
                  <span className="flex items-center gap-1">
                    <span className="h-1.5 w-1.5 rounded-full bg-green-500 animate-pulse" />
                    Mock Service Active
                  </span>
                  <span>Resend attempt: {toast.otpData?.resends}/3</span>
                </div>
              </div>
            )}
          </motion.div>
        ))}
      </AnimatePresence>
    </div>
  );
};

// ==========================================
// 6. LOADER & SKELETON
// ==========================================
export const Loader: React.FC<{ size?: 'sm' | 'md' | 'lg'; color?: string }> = ({ size = 'md', color = 'text-blue-700' }) => {
  const sizes = {
    sm: 'h-5 w-5 border-2',
    md: 'h-8 w-8 border-3',
    lg: 'h-12 w-12 border-4',
  };

  return (
    <div className="flex items-center justify-center">
      <div className={`animate-spin rounded-full border-t-transparent border-current ${color} ${sizes[size]}`} />
    </div>
  );
};

export const Skeleton: React.FC<{ className?: string }> = ({ className = '' }) => {
  return (
    <div className={`animate-pulse bg-slate-200 rounded-md ${className}`} />
  );
};
