import React, { useState, useEffect, useRef, useMemo } from 'react';
import { useForm } from 'react-hook-form';
import { zodResolver } from '@hookform/resolvers/zod';
import * as z from 'zod';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Mail, 
  Phone, 
  ShieldCheck, 
  ArrowLeft, 
  RefreshCw, 
  Key, 
  AlertCircle, 
  Sparkles, 
  Clock 
} from 'lucide-react';
import { Button, Input, useToast } from '../components/UI';
import { useAuth } from '../context/AuthContext';
import { UserRole } from '../services/api';

// ==========================================
// CUSTOM PROFESSIONAL SVG STROKE ICON COMPONENTS
// Rules: viewBox="0 0 24 24", 32px size, strokeWidth:1.8, strokeLinecap/Linejoin:round, fill:none
// ==========================================
export const CommunityMemberIcon: React.FC<{ className?: string; strokeColor?: string; size?: number }> = ({ className = '', strokeColor = 'currentColor', size = 32 }) => (
  <svg 
    viewBox="0 0 24 24" 
    width={size} 
    height={size} 
    fill="none" 
    stroke={strokeColor} 
    strokeWidth="1.8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    {/* House roof */}
    <path d="M3 10 L12 3 L21 10" />
    {/* House walls */}
    <path d="M5 10 V19 H19 V10" />
    {/* 3 family members inside: adult heads circles at (8,13.5) + (16,13.5), child head at (12,14.5) */}
    <circle cx="8" cy="13.5" r="1.5" />
    <circle cx="16" cy="13.5" r="1.5" />
    <circle cx="12" cy="14.5" r="1" />
    {/* Body arcs beneath each */}
    <path d="M5.5 19 C5.5 17.5, 6.5 16.5, 8 16.5 C9.5 16.5, 10.5 17.5, 10.5 19" />
    <path d="M13.5 19 C13.5 17.5, 14.5 16.5, 16 16.5 C17.5 16.5, 18.5 17.5, 18.5 19" />
    <path d="M10.5 19 C10.5 18, 11 17.5, 12 17.5 C13 17.5, 13.5 18, 13.5 19" />
    {/* Small filled heart above roof */}
    <path 
      d="M12 2.2 C11.8 1.8, 11.3 1.8, 11.1 2.0 C10.8 2.2, 10.8 2.7, 11.1 2.9 L12 3.8 L12.9 2.9 C13.2 2.7, 13.2 2.2, 12.9 2.0 C12.7 1.8, 12.2 1.8, 12 2.2 Z" 
      fill={strokeColor} 
      stroke="none" 
    />
  </svg>
);

export const VolunteerIcon: React.FC<{ className?: string; strokeColor?: string; size?: number }> = ({ className = '', strokeColor = 'currentColor', size = 32 }) => (
  <svg 
    viewBox="0 0 24 24" 
    width={size} 
    height={size} 
    fill="none" 
    stroke={strokeColor} 
    strokeWidth="1.8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    {/* Human figure with outstretched helping arm */}
    <circle cx="12" cy="5.5" r="1.8" />
    <path d="M12 7.3 V13 M9.5 19 L12 13 L14.5 19" />
    {/* Right arm raised upward */}
    <path d="M12 9 L16 4.5" />
    {/* Left hand holds medical aid box: rounded rect + medical cross */}
    <path d="M12 9 L7.5 11" />
    <rect x="2.5" y="10.5" width="5" height="4.5" rx="1" />
    <path d="M5 11.5 V14 M4 12.75 H6" strokeWidth="1.2" />
    {/* Small filled star above raised hand */}
    <path 
      d="M17 2 L17.2 2.6 L17.9 2.6 L17.4 3.0 L17.6 3.6 L17 3.2 L16.4 3.6 L16.6 3.0 L16.1 2.6 L16.8 2.6 Z" 
      fill={strokeColor} 
      stroke="none" 
    />
  </svg>
);

export const CoordinatorIcon: React.FC<{ className?: string; strokeColor?: string; size?: number }> = ({ className = '', strokeColor = 'currentColor', size = 32 }) => (
  <svg 
    viewBox="0 0 24 24" 
    width={size} 
    height={size} 
    fill="none" 
    stroke={strokeColor} 
    strokeWidth="1.8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    {/* Clipboard with top clip */}
    <rect x="5" y="4" width="14" height="15" rx="2" />
    <path d="M9 4 V3 A1 1 0 0 1 10 2 H14 A1 1 0 0 1 15 3 V4" />
    {/* 2 checked checklist rows */}
    <path d="M7.5 8 L8.5 9 L10.5 7" />
    <path d="M13 8 H16" />
    <path d="M7.5 12 L8.5 13 L10.5 11" />
    <path d="M13 12 H16" />
    {/* 1 pending row with empty circle */}
    <circle cx="8.5" cy="16" r="1.2" />
    <path d="M13 16 H16" />
    {/* Bottom connected network nodes */}
    <circle cx="6" cy="21.5" r="1.2" />
    <circle cx="12" cy="21.5" r="1.2" />
    <circle cx="18" cy="21.5" r="1.2" />
    <path d="M7.2 21.5 H10.8 M13.2 21.5 H16.8" />
  </svg>
);

export const NgoAdminIcon: React.FC<{ className?: string; strokeColor?: string; size?: number }> = ({ className = '', strokeColor = 'currentColor', size = 32 }) => (
  <svg 
    viewBox="0 0 24 24" 
    width={size} 
    height={size} 
    fill="none" 
    stroke={strokeColor} 
    strokeWidth="1.8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    {/* Shield outline */}
    <path d="M12 2 C12 2, 21 3, 21 11 C21 16.5, 16.5 20.5, 12 22 C7.5 20.5, 3 16.5, 3 11 C3 3, 12 2, 12 2 Z" />
    {/* Org hierarchy inside */}
    <circle cx="12" cy="7.5" r="1.2" />
    <circle cx="8" cy="13" r="1.2" />
    <circle cx="16" cy="13" r="1.2" />
    <path d="M11 8.5 L9 12 M13 8.5 L15 12" />
    {/* Small lock at shield bottom */}
    <rect x="10.5" y="17.5" width="3" height="2.2" rx="0.5" />
    <path d="M11 17.5 V16.5 A1 1 0 0 1 13 16.5 V17.5" />
  </svg>
);

export const SystemAdminIcon: React.FC<{ className?: string; strokeColor?: string; size?: number }> = ({ className = '', strokeColor = 'currentColor', size = 32 }) => (
  <svg 
    viewBox="0 0 24 24" 
    width={size} 
    height={size} 
    fill="none" 
    stroke={strokeColor} 
    strokeWidth="1.8" 
    strokeLinecap="round" 
    strokeLinejoin="round" 
    className={className}
  >
    {/* 3 stacked server rack units */}
    <rect x="3" y="4" width="18" height="4" rx="1" />
    <rect x="3" y="10" width="18" height="4" rx="1" />
    <rect x="3" y="16" width="18" height="4" rx="1" />
    {/* Status dots: green/green/yellow */}
    <circle cx="6" cy="6" r="0.8" fill="#10B981" stroke="none" />
    <circle cx="6" cy="12" r="0.8" fill="#10B981" stroke="none" />
    <circle cx="6" cy="18" r="0.8" fill="#FBBF24" stroke="none" />
    {/* Mini waveform activity lines */}
    <path d="M9 6 H12 L13 5 L14 7 L15 6 H19" strokeWidth="1.2" />
    <path d="M9 12 H12 L13 11 L14 13 L15 12 H19" strokeWidth="1.2" />
    <path d="M9 18 H12 L13 17 L14 19 L15 18 H19" strokeWidth="1.2" />
    {/* Top-right verified badge with checkmark */}
    <circle cx="20" cy="4" r="2.5" fill="#7C3AED" stroke="#FFFFFF" strokeWidth="0.8" />
    <path d="M19 4 L19.6 4.6 L20.6 3.4" stroke="#FFFFFF" strokeWidth="0.6" fill="none" />
  </svg>
);

// ==========================================
// ZOD VALIDATION SCHEMA
// ==========================================
const authSchema = z.object({
  contact: z.string().trim().min(1, "Email or phone number is required").refine((val) => {
    const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(val);
    const isPhone = /^\+?[1-9]\d{6,14}$/.test(val.replace(/[\s()-]/g, ''));
    return isEmail || isPhone;
  }, {
    message: "Please enter a valid email address or phone number (e.g. +15551234567)"
  })
});

type AuthFormValues = z.infer<typeof authSchema>;

interface AuthPageProps {
  onBackToLanding: () => void;
  onAuthSuccess: () => void;
}

// Visual Role Selection Options
interface RoleOption {
  id: UserRole;
  title: string;
  icon: (strokeColor: string, size?: number) => React.ReactNode;
  description: string;
  demoContact: string;
  color: string; // Exact hex code
  activeBorderClass: string;
  activeBgClass: string;
  activeIconTileBgClass: string;
  activeTextClass: string;
  hoverBorderClass: string;
  hoverBgClass: string;
}

export const AuthPage: React.FC<AuthPageProps> = ({ onBackToLanding, onAuthSuccess }) => {
  const { requestOTP, verifyOTP, authError, setAuthError } = useAuth();
  const { showToast } = useToast();

  const [step, setStep] = useState<1 | 2>(1);
  const [selectedRole, setSelectedRole] = useState<UserRole>('citizen');
  const [contactValue, setContactValue] = useState<string>('');
  const [contactType, setContactType] = useState<'email' | 'phone'>('email');
  const [isRequesting, setIsRequesting] = useState<boolean>(false);
  const [isVerifying, setIsVerifying] = useState<boolean>(false);

  // OTP Input States
  const [otp, setOtp] = useState<string[]>(Array(6).fill(''));
  const inputRefs = useRef<(HTMLInputElement | null)[]>([]);

  // Timer States
  const [timeLeft, setTimeLeft] = useState<number>(300); // 5 minutes
  const [canResend, setCanResend] = useState<boolean>(false);
  const [resendCooldown, setResendCooldown] = useState<number>(30); // 30s rate limit
  const [resendCount, setResendCount] = useState<number>(0);

  // React Hook Form
  const { register, handleSubmit, formState: { errors }, setValue } = useForm<AuthFormValues>({
    resolver: zodResolver(authSchema),
    defaultValues: { contact: 'member@sevaai-ngo.org' }
  });

  const roleOptions: RoleOption[] = [
    {
      id: 'citizen',
      title: 'Community Member',
      icon: (color, size) => <CommunityMemberIcon strokeColor={color} size={size} />,
      description: 'Submits emergency needs, requests resources, and tracks family/neighborhood safety status.',
      demoContact: 'member@sevaai-ngo.org',
      color: '#1D4ED8', // Blue 700
      activeBorderClass: 'border-[#1D4ED8]',
      activeBgClass: 'bg-[#1D4ED8]/[0.04]',
      activeIconTileBgClass: 'bg-[#1D4ED8]/[0.14]',
      activeTextClass: 'text-[#1D4ED8] font-bold',
      hoverBorderClass: 'hover:border-[#1D4ED8]/50',
      hoverBgClass: 'hover:bg-[#1D4ED8]/[0.03]'
    },
    {
      id: 'volunteer',
      title: 'Volunteer',
      icon: (color, size) => <VolunteerIcon strokeColor={color} size={size} />,
      description: 'Accepts assigned crisis tasks, coordinates field logistics, and submits completion feedback.',
      demoContact: '+1 (555) 909-0808',
      color: '#0F766E', // Teal 700
      activeBorderClass: 'border-[#0F766E]',
      activeBgClass: 'bg-[#0F766E]/[0.04]',
      activeIconTileBgClass: 'bg-[#0F766E]/[0.14]',
      activeTextClass: 'text-[#0F766E] font-bold',
      hoverBorderClass: 'hover:border-[#0F766E]/50',
      hoverBgClass: 'hover:bg-[#0F766E]/[0.03]'
    },
    {
      id: 'coordinator',
      title: 'Coordinator',
      icon: (color, size) => <CoordinatorIcon strokeColor={color} size={size} />,
      description: 'Reviews incoming reports, approves matches, edits triage priorities, and dispatches responders.',
      demoContact: 'coordinator@sevaai-ngo.org',
      color: '#0F766E', // Teal 700
      activeBorderClass: 'border-[#0F766E]',
      activeBgClass: 'bg-[#0F766E]/[0.04]',
      activeIconTileBgClass: 'bg-[#0F766E]/[0.14]',
      activeTextClass: 'text-[#0F766E] font-bold',
      hoverBorderClass: 'hover:border-[#0F766E]/50',
      hoverBgClass: 'hover:bg-[#0F766E]/[0.03]'
    },
    {
      id: 'ngo_admin',
      title: 'NGO Admin',
      icon: (color, size) => <NgoAdminIcon strokeColor={color} size={size} />,
      description: 'Tracks overall needs, volunteers, critical supply inventories, and generates global impact reports.',
      demoContact: 'director@sevaai-ngo.org',
      color: '#DC2626', // Red 600
      activeBorderClass: 'border-[#DC2626]',
      activeBgClass: 'bg-[#DC2626]/[0.04]',
      activeIconTileBgClass: 'bg-[#DC2626]/[0.14]',
      activeTextClass: 'text-[#DC2626] font-bold',
      hoverBorderClass: 'hover:border-[#DC2626]/50',
      hoverBgClass: 'hover:bg-[#DC2626]/[0.03]'
    },
    {
      id: 'system_admin',
      title: 'System Admin',
      icon: (color, size) => <SystemAdminIcon strokeColor={color} size={size} />,
      description: 'Manages user roles, security settings, API configurations, and reviews real-time compliance audit trails.',
      demoContact: 'admin@sevaai-ngo.org',
      color: '#7C3AED', // Purple 600
      activeBorderClass: 'border-[#7C3AED]',
      activeBgClass: 'bg-[#7C3AED]/[0.04]',
      activeIconTileBgClass: 'bg-[#7C3AED]/[0.14]',
      activeTextClass: 'text-[#7C3AED] font-bold',
      hoverBorderClass: 'hover:border-[#7C3AED]/50',
      hoverBgClass: 'hover:bg-[#7C3AED]/[0.03]'
    }
  ];

  // Auto pre-fill contact box when a role is selected
  const handleRoleSelect = (role: RoleOption) => {
    setSelectedRole(role.id);
    setValue('contact', role.demoContact);
    setAuthError(null);
  };

  // Countdown timer for 5-minute OTP expiry
  useEffect(() => {
    if (step !== 2) return;
    
    if (timeLeft <= 0) {
      showToast('error', "Your verification code has expired. Please request a new one.", "Code Expired");
      return;
    }

    const interval = setInterval(() => {
      setTimeLeft((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [step, timeLeft]);

  // Cooldown timer for 30s Resend Rate Limit
  useEffect(() => {
    if (step !== 2 || canResend) return;

    if (resendCooldown <= 0) {
      setCanResend(true);
      return;
    }

    const interval = setInterval(() => {
      setResendCooldown((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(interval);
  }, [step, resendCooldown, canResend]);

  // Focus first OTP box on step change
  useEffect(() => {
    if (step === 2) {
      setTimeout(() => {
        inputRefs.current[0]?.focus();
      }, 150);
    }
  }, [step]);

  // Format time (e.g. 300s -> 05:00)
  const formatTime = (seconds: number) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  /**
   * Step 1: Submit email/phone to request OTP
   */
  const handleRequestOTP = async (data: AuthFormValues) => {
    setIsRequesting(true);
    setAuthError(null);
    const contact = data.contact.trim();
    const isEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(contact);
    const detectedType = isEmail ? 'email' : 'phone';

    try {
      const response = await requestOTP(detectedType, contact);
      if (response.success) {
        setContactValue(contact);
        setContactType(detectedType);
        setTimeLeft(response.expiresIn);
        setResendCooldown(30);
        setCanResend(false);
        setOtp(Array(6).fill('')); // Reset OTP inputs
        setStep(2);
      }
    } catch (err) {
      // Handled in AuthContext / toast
    } finally {
      setIsRequesting(false);
    }
  };

  /**
   * Resend OTP Handler
   */
  const handleResendOTP = async () => {
    if (!canResend) return;
    if (resendCount >= 3) {
      showToast('error', "Max resend limit reached. Please restart the process.", "Resend Locked");
      return;
    }

    setIsRequesting(true);
    setAuthError(null);
    try {
      const response = await requestOTP(contactType, contactValue);
      if (response.success) {
        setTimeLeft(response.expiresIn);
        setResendCooldown(30);
        setCanResend(false);
        setResendCount(prev => prev + 1);
        setOtp(Array(6).fill(''));
        inputRefs.current[0]?.focus();
      }
    } catch (err) {
      // Handled in context
    } finally {
      setIsRequesting(false);
    }
  };

  /**
   * Handle OTP text change
   */
  const handleOtpChange = (index: number, value: string) => {
    const cleanValue = value.replace(/[^0-9]/g, '');
    if (!cleanValue) {
      const newOtp = [...otp];
      newOtp[index] = '';
      setOtp(newOtp);
      return;
    }

    const digit = cleanValue[cleanValue.length - 1];
    const newOtp = [...otp];
    newOtp[index] = digit;
    setOtp(newOtp);

    if (index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  /**
   * Handle backspace and cursor navigation
   */
  const handleOtpKeyDown = (index: number, e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Backspace') {
      if (otp[index] === '') {
        if (index > 0) {
          const newOtp = [...otp];
          newOtp[index - 1] = '';
          setOtp(newOtp);
          inputRefs.current[index - 1]?.focus();
        }
      } else {
        const newOtp = [...otp];
        newOtp[index] = '';
        setOtp(newOtp);
      }
    } else if (e.key === 'ArrowLeft' && index > 0) {
      inputRefs.current[index - 1]?.focus();
    } else if (e.key === 'ArrowRight' && index < 5) {
      inputRefs.current[index + 1]?.focus();
    }
  };

  /**
   * Handle OTP Paste Support
   */
  const handleOtpPaste = (e: React.ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text').trim();
    
    if (/^\d{6}$/.test(pastedData)) {
      const digits = pastedData.split('');
      setOtp(digits);
      inputRefs.current[5]?.focus();
      
      setTimeout(() => {
        handleVerifyOTP(digits.join(''));
      }, 200);
    } else {
      showToast('error', "Pasted content must be exactly 6 digits.", "Paste Failed");
    }
  };

  /**
   * Step 2: Trigger verify OTP API
   */
  const handleVerifyOTP = async (codeToVerify?: string) => {
    const finalCode = codeToVerify || otp.join('');
    if (finalCode.length < 6) {
      showToast('error', "Please enter all 6 digits of the verification code.", "Incomplete Code");
      return;
    }

    setIsVerifying(true);
    setAuthError(null);
    try {
      const success = await verifyOTP(contactValue, finalCode, selectedRole);
      if (success) {
        onAuthSuccess();
      }
    } catch (err: any) {
      if (err.message?.includes('locked') || err.message?.includes('expired')) {
        setOtp(Array(6).fill(''));
        inputRefs.current[0]?.focus();
      }
    } finally {
      setIsVerifying(false);
    }
  };

  // Check if all OTP boxes are filled to auto-submit
  useEffect(() => {
    if (otp.every(digit => digit !== '') && step === 2) {
      handleVerifyOTP();
    }
  }, [otp]);

  // Find currently active role details for dynamic privileges banner
  const activeRoleDetails = useMemo(() => {
    return roleOptions.find(r => r.id === selectedRole) || roleOptions[0];
  }, [selectedRole, roleOptions]);

  return (
    <div className="min-h-screen bg-slate-50 flex flex-col justify-center items-center py-12 px-6 relative font-sans">
      
      {/* Background Decorative Gradients */}
      <div className="absolute top-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-blue-200/20 blur-3xl pointer-events-none" />
      <div className="absolute bottom-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-teal-200/20 blur-3xl pointer-events-none" />

      {/* Brand Header */}
      <div className="flex items-center gap-2.5 mb-6">
        <div className="h-9 w-9 rounded-lg bg-gradient-to-tr from-blue-700 to-teal-600 flex items-center justify-center shadow-md">
          <ShieldCheck className="text-white h-5 w-5" />
        </div>
        <span className="text-xl font-black tracking-tight bg-gradient-to-r from-blue-800 to-teal-700 bg-clip-text text-transparent">
          SevaAi Secure Gate
        </span>
      </div>

      <div className="w-full max-w-3xl">
        
        {/* Back Button */}
        <button
          onClick={step === 2 ? () => setStep(1) : onBackToLanding}
          className="inline-flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-blue-700 mb-4 transition-colors cursor-pointer"
        >
          <ArrowLeft size={14} />
          {step === 2 ? "Back to Login Method" : "Back to Landing Page"}
        </button>

        {/* Auth Wrapper Card */}
        <div className="bg-white rounded-2xl border border-slate-100 shadow-xl p-8 relative overflow-hidden text-left">
          
          {/* Top Secure Indicator Line */}
          <div className="absolute top-0 left-0 right-0 h-1.5 bg-gradient-to-r from-blue-700 via-teal-600 to-green-500" />

          <AnimatePresence mode="wait">
            {step === 1 ? (
              /* ==========================================
                  STEP 1: ROLE SELECTOR & CONTACT INPUT
                  ========================================== */
              <motion.div
                key="step1"
                initial={{ opacity: 0, x: -15 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: 15 }}
                transition={{ duration: 0.25 }}
                className="flex flex-col gap-6"
              >
                <div className="flex flex-col gap-1.5">
                  <h2 className="text-xl font-black text-slate-900">Secure Sign In</h2>
                  <p className="text-xs text-slate-500 leading-relaxed">
                    Select your organizational role. We will send a secure 6-digit verification code (OTP) to authenticate your access with the correct privilege levels.
                  </p>
                </div>

                {/* VISUALLY DISTINGUISHED ROLE SELECTOR */}
                <div className="flex flex-col gap-3">
                  <label className="text-xs font-bold text-slate-700 tracking-wide uppercase">
                    Select Your Role
                  </label>
                  <div className="grid grid-cols-1 sm:grid-cols-5 gap-3">
                    {roleOptions.map((role) => {
                      const isSelected = selectedRole === role.id;
                      return (
                        <button
                          key={role.id}
                          type="button"
                          onClick={() => handleRoleSelect(role)}
                          className={`p-3.5 rounded-xl border text-left flex flex-col gap-2 transition-all duration-200 cursor-pointer relative hover:-translate-y-0.5 ${
                            isSelected 
                              ? `${role.activeBorderClass} ${role.activeBgClass} ring-2 ring-blue-500/10 shadow-sm` 
                              : `border-slate-200 bg-white ${role.hoverBorderClass} ${role.hoverBgClass}`
                          }`}
                        >
                          <div className={`w-[52px] h-[52px] rounded-xl flex items-center justify-center self-start transition-colors ${
                            isSelected ? role.activeIconTileBgClass : 'bg-slate-50'
                          }`}>
                            {role.icon(isSelected ? role.color : '#6B7280', 32)}
                          </div>
                          <div>
                            <span className={`text-xs block leading-tight transition-colors ${
                              isSelected ? role.activeTextClass : 'text-slate-700 font-semibold'
                            }`}>
                              {role.title}
                            </span>
                          </div>
                        </button>
                      );
                    })}
                  </div>

                  {/* Concise Role Description Alert - PRIVILEGES BANNER */}
                  <div className="p-4 rounded-xl bg-slate-50 border border-slate-100 text-xs text-slate-600 leading-relaxed flex items-start gap-3 mt-1.5 relative overflow-hidden">
                    <div className="absolute left-0 top-0 bottom-0 w-1" style={{ backgroundColor: activeRoleDetails.color }} />
                    <div className="flex-shrink-0 mt-0.5 transition-all duration-300">
                      {activeRoleDetails.icon(activeRoleDetails.color, 20)}
                    </div>
                    <div className="transition-all duration-300">
                      <strong className="font-bold block" style={{ color: activeRoleDetails.color }}>
                        {activeRoleDetails.title} Privileges:
                      </strong>
                      <span className="text-slate-500 mt-0.5 block">
                        {activeRoleDetails.description}
                      </span>
                    </div>
                  </div>
                </div>

                <form onSubmit={handleSubmit(handleRequestOTP)} className="flex flex-col gap-5">
                  <Input
                    label="Email or Phone Number"
                    placeholder="e.g. name@ngo.org or +15551234567"
                    icon={contactType === 'email' ? <Mail size={18} className="text-slate-400" /> : <Phone size={18} className="text-slate-400" />}
                    error={errors.contact?.message}
                    {...register('contact')}
                  />

                  {authError && (
                    <div className="p-3 rounded-lg bg-red-50 border border-red-100 text-xs text-red-700 font-medium flex items-start gap-2">
                      <AlertCircle size={16} className="mt-0.5 flex-shrink-0" />
                      <span>{authError}</span>
                    </div>
                  )}

                  <Button
                    type="submit"
                    variant="primary"
                    isLoading={isRequesting}
                    className="w-full"
                    icon={<Key size={16} />}
                  >
                    Request OTP Verification Code
                  </Button>
                </form>
              </motion.div>
            ) : (
              /* ==========================================
                  STEP 2: OTP VERIFICATION SCREEN
                  ========================================== */
              <motion.div
                key="step2"
                initial={{ opacity: 0, x: 15 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -15 }}
                transition={{ duration: 0.25 }}
                className="flex flex-col gap-6"
              >
                <div className="flex flex-col gap-1.5">
                  <h2 className="text-xl font-black text-slate-900">Enter Security Code</h2>
                  <p className="text-xs text-slate-500 leading-relaxed">
                    We sent a 6-digit OTP to <strong className="text-blue-700 font-bold">{contactValue}</strong> for the <strong className="text-teal-700 font-bold">{roleOptions.find(r => r.id === selectedRole)?.title}</strong> role. Enter it below to complete authentication.
                  </p>
                </div>

                <div className="flex flex-col gap-5">
                  {/* 6 OTP Input Boxes */}
                  <div className="flex justify-between items-center gap-2" onPaste={handleOtpPaste}>
                    {otp.map((digit, index) => (
                      <input
                        key={index}
                        ref={(el) => { inputRefs.current[index] = el; }}
                        type="text"
                        inputMode="numeric"
                        maxLength={1}
                        value={digit}
                        onChange={(e) => handleOtpChange(index, e.target.value)}
                        onKeyDown={(e) => handleOtpKeyDown(index, e)}
                        className="w-12 h-14 bg-slate-50 hover:bg-slate-100/50 focus:bg-white border-2 border-slate-200 focus:border-blue-600 rounded-xl text-center text-xl font-bold font-mono text-slate-800 focus:outline-none focus:ring-2 focus:ring-blue-500/20 transition-all"
                        autoFocus={index === 0}
                      />
                    ))}
                  </div>

                  {/* Timer & Resend Layout */}
                  <div className="flex items-center justify-between text-xs font-semibold">
                    <span className="text-slate-500 flex items-center gap-1.5">
                      <Clock size={14} className="text-slate-400" />
                      Code expires in: <span className={`font-mono font-bold ${timeLeft < 60 ? 'text-red-600 animate-pulse' : 'text-slate-700'}`}>{formatTime(timeLeft)}</span>
                    </span>

                    {canResend ? (
                      <button
                        onClick={handleResendOTP}
                        disabled={isRequesting}
                        className="text-blue-700 hover:text-blue-800 hover:underline flex items-center gap-1 cursor-pointer font-bold disabled:opacity-50"
                      >
                        <RefreshCw size={12} className={isRequesting ? 'animate-spin' : ''} />
                        Resend Code
                      </button>
                    ) : (
                      <span className="text-slate-400">
                        Resend in {resendCooldown}s
                      </span>
                    )}
                  </div>

                  {authError && (
                    <div className="p-3 rounded-lg bg-red-50 border border-red-100 text-xs text-red-700 font-medium flex items-start gap-2">
                      <AlertCircle size={16} className="mt-0.5 flex-shrink-0" />
                      <span>{authError}</span>
                    </div>
                  )}

                  <div className="flex flex-col gap-3">
                    <Button
                      onClick={() => handleVerifyOTP()}
                      variant="success"
                      isLoading={isVerifying}
                      className="w-full"
                      disabled={otp.some(digit => digit === '')}
                    >
                      Verify & Sign In
                    </Button>
                    <button
                      onClick={() => setStep(1)}
                      className="text-xs text-slate-500 hover:text-slate-800 font-bold py-1 transition-colors cursor-pointer text-center"
                    >
                      Use a different contact method
                    </button>
                  </div>
                </div>

                {/* Security Best Practices Notice */}
                <div className="p-3 rounded-xl bg-slate-50 border border-slate-100 flex gap-2.5 items-start text-[10px] text-slate-500 leading-relaxed">
                  <ShieldCheck size={18} className="text-teal-600 mt-0.5 flex-shrink-0" />
                  <div>
                    <strong className="text-slate-700 font-bold">Secure Session Protection:</strong>
                    <p className="mt-0.5">Codes are cryptographically hashed and automatically deleted after verification. Session credentials expire after 24 hours.</p>
                  </div>
                </div>
              </motion.div>
            )}
          </AnimatePresence>

        </div>

        {/* Bottom Compliance Badges */}
        <div className="flex justify-center items-center gap-6 mt-6 text-[10px] text-slate-400 font-bold tracking-wide uppercase">
          <span className="flex items-center gap-1">
            <ShieldCheck size={12} className="text-green-500" /> AES-256 SSL
          </span>
          <span>•</span>
          <span>HIPAA/GDPR Ready</span>
          <span>•</span>
          <span>ISO 27001</span>
        </div>

      </div>
    </div>
  );
};
