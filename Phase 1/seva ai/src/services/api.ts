// Smart Community Resource Platform - API Simulation Service
// Implements the secure OTP authentication flow and server-side state simulation.

export interface OTPState {
  otp: string;
  expiresAt: number;
  attempts: number;
  resends: number;
  lastRequested: number;
}

export type UserRole = 'citizen' | 'volunteer' | 'coordinator' | 'ngo_admin' | 'system_admin';

export interface UserSession {
  id: string;
  emailOrPhone: string;
  role: UserRole;
  name: string;
  token: string;
}

// In-memory fallback if sessionStorage is blocked
const memoryStorage: Record<string, string> = {};

const safeSessionStorage = {
  getItem: (key: string): string | null => {
    try {
      return sessionStorage.getItem(key);
    } catch {
      return memoryStorage[key] || null;
    }
  },
  setItem: (key: string, value: string): void => {
    try {
      sessionStorage.setItem(key, value);
    } catch {
      memoryStorage[key] = value;
    }
  },
  removeItem: (key: string): void => {
    try {
      sessionStorage.removeItem(key);
    } catch {
      delete memoryStorage[key];
    }
  }
};

const STORAGE_KEY_OTP = 'sevaai_mock_otps';
const STORAGE_KEY_SESSION = 'sevaai_session';

const getStoredOTPs = (): Record<string, OTPState> => {
  try {
    const data = safeSessionStorage.getItem(STORAGE_KEY_OTP);
    return data ? JSON.parse(data) : {};
  } catch {
    return {};
  }
};

const saveStoredOTPs = (otps: Record<string, OTPState>) => {
  safeSessionStorage.setItem(STORAGE_KEY_OTP, JSON.stringify(otps));
};

// Helper to simulate network latency
const delay = (ms: number) => new Promise(resolve => setTimeout(resolve, ms));

// Generate a random 6-digit OTP
const generate6DigitOTP = (): string => {
  return Math.floor(100000 + Math.random() * 900000).toString();
};

export const apiService = {
  /**
   * Request OTP
   * POST /api/auth/request-otp
   */
  requestOtp: async (type: 'email' | 'phone', value: string): Promise<{ success: boolean; expiresIn: number }> => {
    await delay(1000); // Simulate network lag

    const cleanValue = value.trim().toLowerCase();
    const otps = getStoredOTPs();
    const now = Date.now();
    const existing = otps[cleanValue];

    // 1. Rate Limiting: Minimum 30s wait between requests
    if (existing && now - existing.lastRequested < 30000) {
      const waitTime = Math.ceil((30000 - (now - existing.lastRequested)) / 1000);
      throw new Error(`Rate limit exceeded. Please wait ${waitTime} seconds before requesting a new OTP.`);
    }

    // 2. Max Resends: Max 3 resends per session (total 4 OTP requests)
    if (existing && existing.resends >= 3) {
      throw new Error("Max OTP resend limit reached (3/3). Please try again in 1 hour or contact support.");
    }

    // Generate new OTP
    const otpCode = generate6DigitOTP();
    const expiresIn = 300; // 5 minutes in seconds
    const expiresAt = now + expiresIn * 1000;

    otps[cleanValue] = {
      otp: otpCode, // In a real server, this would be salted and hashed
      expiresAt,
      attempts: 0,
      resends: existing ? existing.resends + 1 : 0,
      lastRequested: now,
    };

    saveStoredOTPs(otps);

    // Dispatch a custom event so the client UI can display an "Incoming SMS/Email" notification
    // containing the OTP. This allows seamless testing without a real gateway.
    const notificationEvent = new CustomEvent('sevaai_mock_otp_delivered', {
      detail: {
        type,
        value: cleanValue,
        otp: otpCode,
        resends: otps[cleanValue].resends,
      }
    });
    window.dispatchEvent(notificationEvent);

    console.log(`[MOCK SERVER] OTP for ${cleanValue} is: ${otpCode} (Expires in 5m)`);

    return {
      success: true,
      expiresIn,
    };
  },

  /**
   * Verify OTP
   * POST /api/auth/verify-otp
   */
  verifyOtp: async (
    value: string, 
    otp: string, 
    selectedRole: UserRole = 'citizen'
  ): Promise<{ success: boolean; token: string; user: UserSession }> => {
    await delay(1200); // Simulate network lag

    const cleanValue = value.trim().toLowerCase();
    const otps = getStoredOTPs();
    const now = Date.now();
    const record = otps[cleanValue];

    // 1. Check if OTP was ever requested
    if (!record) {
      throw new Error("No OTP request found for this contact. Please request an OTP first.");
    }

    // 2. Check Expiry (5 minutes)
    if (now > record.expiresAt) {
      throw new Error("OTP has expired (5 minute limit). Please request a new code.");
    }

    // 3. Check Max Attempts (Max 5 attempts)
    if (record.attempts >= 5) {
      throw new Error("Too many incorrect attempts. This OTP has been locked. Please request a new one.");
    }

    // 4. Validate OTP
    if (record.otp !== otp) {
      record.attempts += 1;
      saveStoredOTPs(otps);
      
      const remaining = 5 - record.attempts;
      if (remaining <= 0) {
        throw new Error("Too many incorrect attempts. This OTP has been locked. Please request a new one.");
      }
      throw new Error(`Invalid verification code. ${remaining} attempts remaining.`);
    }

    // Success - Clear OTP record and generate Session
    delete otps[cleanValue];
    saveStoredOTPs(otps);

    // Generate simulated JWT Token (Header.Payload.Signature mock)
    const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }));
    const payload = btoa(JSON.stringify({
      sub: cleanValue,
      role: selectedRole,
      iat: Math.floor(now / 1000),
      exp: Math.floor(now / 1000) + 86400 // 1 day
    }));
    const token = `${header}.${payload}.mock_signature_sha256`;

    // Create user profile based on role
    let name = "Community Member";
    if (selectedRole === 'volunteer') {
      name = "Dr. Evelyn Martinez";
    } else if (selectedRole === 'coordinator') {
      name = "Sarah Jenkins";
    } else if (selectedRole === 'ngo_admin') {
      name = "Director Marcus Vance";
    } else if (selectedRole === 'system_admin') {
      name = "Admin Root";
    } else {
      const emailName = cleanValue.split('@')[0].replace(/[^a-zA-Z]/g, ' ').replace(/\b\w/g, c => c.toUpperCase());
      name = emailName || "Community Member";
    }

    const user: UserSession = {
      id: `usr_${Math.random().toString(36).substr(2, 9)}`,
      emailOrPhone: cleanValue,
      role: selectedRole,
      name: name,
      token,
    };

    // Save session to sessionStorage
    safeSessionStorage.setItem(STORAGE_KEY_SESSION, JSON.stringify(user));

    return {
      success: true,
      token,
      user,
    };
  },

  /**
   * Get Active Session
   */
  getSession: (): UserSession | null => {
    try {
      const session = safeSessionStorage.getItem(STORAGE_KEY_SESSION);
      return session ? JSON.parse(session) : null;
    } catch {
      return null;
    }
  },

  /**
   * Terminate Session
   */
  logout: () => {
    safeSessionStorage.removeItem(STORAGE_KEY_SESSION);
  }
};
