import React, { useState, useMemo, useEffect } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  LayoutDashboard,
  ShieldAlert, 
  Users, 
  Truck, 
  ClipboardList, 
  BarChart3, 
  Bell, 
  Settings,
  Activity, 
  LogOut, 
  User, 
  CheckCircle, 
  Clock, 
  Send, 
  Filter, 
  Search, 
  Sliders, 
  Sparkles, 
  Wrench, 
  MapPin,
  RefreshCw,
  Eye,
  Heart,
  FileText,
  Shield,
  Check,
  Download,
  Database,
  Lock,
  Terminal,
  Server,
  AlertTriangle,
  ChevronRight,
  Menu,
  X,
  PlusCircle,
  TrendingUp,
  Radio,
  Zap,
  Flame,
  Droplet
} from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { usePlatform, EmergencyCase, Volunteer } from '../context/PlatformContext';
import { Button, Card, useToast, Logo } from '../components/UI';

// Mock Security Audit Trail logs for System Admin
interface AuditLog {
  id: string;
  timestamp: string;
  actor: string;
  action: string;
  status: 'SUCCESS' | 'WARNING' | 'CRITICAL';
  ipAddress: string;
}

const INITIAL_AUDIT_LOGS: AuditLog[] = [
  { id: 'aud_101', timestamp: new Date(Date.now() - 5 * 60000).toISOString(), actor: 'admin@sevaai-ngo.org', action: 'Modified System-wide Configuration: Bypass OTP (Disabled)', status: 'SUCCESS', ipAddress: '192.168.1.45' },
  { id: 'aud_102', timestamp: new Date(Date.now() - 12 * 60000).toISOString(), actor: 'SYSTEM', action: 'Automated AI Triage: Case #case_001 tagged Critical', status: 'SUCCESS', ipAddress: 'localhost' },
  { id: 'aud_103', timestamp: new Date(Date.now() - 18 * 60000).toISOString(), actor: 'director@sevaai-ngo.org', action: 'Downloaded Regional Impact Report (Q1 Disaster Response)', status: 'SUCCESS', ipAddress: '192.168.1.12' },
  { id: 'aud_104', timestamp: new Date(Date.now() - 35 * 60000).toISOString(), actor: 'unauthenticated-ip', action: 'Failed Sign-In Attempt: Max OTP resends reached for +1 (555) 019-2834', status: 'WARNING', ipAddress: '102.15.88.23' },
  { id: 'aud_105', timestamp: new Date(Date.now() - 52 * 60000).toISOString(), actor: 'admin@sevaai-ngo.org', action: 'Updated security firewall configuration keys', status: 'SUCCESS', ipAddress: '192.168.1.45' }
];

// Mock Live Activity Stream for the Dashboard
interface ActivityFeedItem {
  id: string;
  type: 'DISPATCH' | 'RESOLVED' | 'ALERT' | 'SYSTEM';
  title: string;
  desc: string;
  time: string;
  priority: 'Critical' | 'High' | 'Medium' | 'Low';
}

const INITIAL_ACTIVITIES: ActivityFeedItem[] = [
  { id: 'act_1', type: 'RESOLVED', title: 'Water Main Response Completed', desc: 'Water contamination response completed in Southside Ward.', time: '2 mins ago', priority: 'High' },
  { id: 'act_2', type: 'DISPATCH', title: 'Volunteer Dispatched', desc: 'Dr. Evelyn Martinez assigned to critical insulin delivery.', time: '14 mins ago', priority: 'High' },
  { id: 'act_3', type: 'ALERT', title: 'Critical Incident Reported', desc: 'Family of 4 trapped on roof due to rapid flooding.', time: '45 mins ago', priority: 'Critical' },
  { id: 'act_4', type: 'SYSTEM', title: 'AI Prioritization Engine Active', desc: 'Automatically triaged and mapped 12 incoming requests.', time: '1 hr ago', priority: 'Medium' }
];

// ==========================================
// PREMIUM NGO DASHBOARD CSS KEYFRAMES
// ==========================================
const PremiumDashboardStyles: React.FC = () => (
  <style>{`
    @keyframes pulse-icon {
      0%, 100% { transform: scale(1); }
      50% { transform: scale(1.15); }
    }
    @keyframes pulse-text-red {
      0%, 100% { color: #DC2626; text-shadow: 0 0 2px rgba(220, 38, 38, 0.2); }
      50% { color: #EF4444; text-shadow: 0 0 8px rgba(239, 68, 68, 0.4); }
    }
    @keyframes pulse-dot {
      0%, 100% { transform: scale(1); opacity: 1; }
      50% { transform: scale(1.25); opacity: 0.8; }
    }
    @keyframes sonar-pulse {
      0% { transform: scale(1); opacity: 1; }
      100% { transform: scale(3.5); opacity: 0; }
    }
    @keyframes border-glow-pulse {
      0%, 100% { box-shadow: 0 0 0 0 rgba(59, 130, 246, 0.1); border-color: #E2E8F0; }
      50% { box-shadow: 0 0 8px 3px rgba(59, 130, 246, 0.35); border-color: #3B82F6; }
    }
    @keyframes draw-top-border {
      0% { width: 0; }
      100% { width: 100%; }
    }
    .animate-pulse-icon {
      animation: pulse-icon 1.5s ease-in-out infinite;
    }
    .animate-pulse-text-red {
      animation: pulse-text-red 1.5s ease-in-out infinite;
    }
    .animate-pulse-dot {
      animation: pulse-dot 2s ease-in-out infinite;
      transform-origin: center;
    }
    .sonar-ring-1 {
      animation: sonar-pulse 2.4s cubic-bezier(0.16, 1, 0.3, 1) infinite;
    }
    .sonar-ring-2 {
      animation: sonar-pulse 2.4s cubic-bezier(0.16, 1, 0.3, 1) infinite;
      animation-delay: 0.8s;
    }
    .sonar-ring-3 {
      animation: sonar-pulse 2.4s cubic-bezier(0.16, 1, 0.3, 1) infinite;
      animation-delay: 1.6s;
    }
    .animate-border-glow {
      animation: border-glow-pulse 2.5s ease-in-out infinite;
    }
    .will-change-transform {
      will-change: transform;
    }
  `}</style>
);

// ==========================================
// REQUEST-ANIMATION-FRAME COUNT-UP COMPONENT
// ==========================================
const AnimatedCounter: React.FC<{
  endValue: number;
  duration: number; // in seconds
  formatter?: (val: number) => string;
}> = ({ endValue, duration, formatter }) => {
  const [count, setCount] = useState(0);

  useEffect(() => {
    let startTimestamp: number | null = null;
    const step = (timestamp: number) => {
      if (!startTimestamp) startTimestamp = timestamp;
      const progress = Math.min((timestamp - startTimestamp) / (duration * 1000), 1);
      // easeOutCubic: t => 1 - Math.pow(1 - t, 3)
      const easeProgress = 1 - Math.pow(1 - progress, 3);
      setCount(Math.floor(easeProgress * endValue));
      if (progress < 1) {
        window.requestAnimationFrame(step);
      } else {
        setCount(endValue);
      }
    };
    window.requestAnimationFrame(step);
  }, [endValue, duration]);

  return <span className="will-change-transform font-mono font-black">{formatter ? formatter(count) : count.toLocaleString()}</span>;
};

export const DashboardPage: React.FC = () => {
  const { user, logout } = useAuth();
  const { 
    cases, 
    volunteers, 
    resources, 
    stats, 
    submitNeed, 
    assignVolunteer, 
    updateCaseStatus, 
    registerVolunteer 
  } = usePlatform();

  const { showToast } = useToast();

  // Responsive Sidebar Menu State (Mobile)
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);

  // Active Tab State
  const [activeTab, setActiveTab] = useState<string>('dashboard');

  // Interactive Filter States
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState<string>('All');
  const [priorityFilter, setPriorityFilter] = useState<string>('All');
  const [statusFilter, setStatusFilter] = useState<string>('All');

  // GIS Map States
  const [selectedCaseId, setSelectedCaseId] = useState<string | null>('case_001');
  const [showHeatmap, setShowHeatmap] = useState(false);
  const [hoveredCase, setHoveredCase] = useState<EmergencyCase | null>(null);

  // Live Activity Stream State
  const [activityFeed, setActivityFeed] = useState<ActivityFeedItem[]>(INITIAL_ACTIVITIES);

  // Real-time Tickers / Metrics State
  const [liveActiveCases, setLiveActiveCases] = useState(1284);
  const [liveActiveVolunteers, setLiveActiveVolunteers] = useState(342);
  const [liveUrgentReports, setLiveUrgentReports] = useState(15);

  // Premium Dashboard Animation Trigger States
  const [isChartVisible, setIsChartVisible] = useState(false);
  const [hoveredSegment, setHoveredSegment] = useState<'water' | 'medical' | 'food' | 'shelter' | null>(null);

  // Submit Need Form States
  const [newTitle, setNewTitle] = useState('');
  const [newCategory, setNewCategory] = useState<'Rescue' | 'Medical' | 'Food' | 'Water' | 'Shelter' | 'Power'>('Rescue');
  const [newLocation, setNewLocation] = useState('');
  const [newContact, setNewContact] = useState('');
  const [newDescription, setNewDescription] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [triageStep, setTriageStep] = useState<string>('');

  // Register Volunteer Form States
  const [isRegModalOpen, setIsRegModalOpen] = useState(false);
  const [regName, setRegName] = useState('');
  const [regSpecialty, setRegSpecialty] = useState<'Rescue' | 'Medical' | 'Food' | 'Water' | 'Shelter' | 'Power' | 'General'>('General');
  const [regPhone, setRegPhone] = useState('');
  const [regLocation, setRegLocation] = useState('');

  // Case Resolution Notes State
  const [resolutionNotes, setResolutionNotes] = useState('');
  const [resolvingCaseId, setResolvingCaseId] = useState<string | null>(null);

  // Standby Toggle for Volunteers
  const [isStandbyActive, setIsStandbyActive] = useState(true);

  // System Admin States
  const [auditLogs, setAuditLogs] = useState<AuditLog[]>(INITIAL_AUDIT_LOGS);
  const [otpBypass, setOtpBypass] = useState(false);
  const [maintenanceMode, setMaintenanceMode] = useState(false);
  const [systemLatency, setSystemLatency] = useState(14); // ms

  // Active chart interactive states
  const [hoveredBarIndex, setHoveredBarIndex] = useState<number | null>(null);
  const [hoveredLineIndex, setHoveredLineIndex] = useState<number | null>(null);
  const [selectedDonutCategory, setSelectedDonutCategory] = useState<string>('Water');

  // ==========================================
  // REAL-TIME INCIDENT SIMULATION LOOP
  // ==========================================
  useEffect(() => {
    const interval = setInterval(() => {
      // Create a simulated incoming emergency report
      const simulationTemplates = [
        { title: "Power Outage at Temporary Shelter", desc: "Local shelter in Westside Sector lost backup grid power.", cat: "Power", loc: "Westside Gym" },
        { title: "Isolated Senior Needs Rations", desc: "Elderly citizen isolated by rising waters needs emergency food packs.", cat: "Food", loc: "Riverside Sector B" },
        { title: "Emergency Medical Transport Required", desc: "Citizen with breathing difficulty needs standby EMT rescue.", cat: "Medical", loc: "Central Plaza" },
        { title: "Clean Water Request Block 3", desc: "Water main fracture contamination reported. Block 3 needs water liters.", cat: "Water", loc: "Northside District" }
      ];

      const template = simulationTemplates[Math.floor(Math.random() * simulationTemplates.length)];
      const id = `act_sim_${Math.floor(Math.random() * 900) + 100}`;
      
      // Update dynamic states
      setLiveActiveCases(prev => prev + 1);
      setLiveUrgentReports(prev => prev + 1);

      // Add to activity stream
      const newItem: ActivityFeedItem = {
        id,
        type: 'ALERT',
        title: `Simulated: ${template.title}`,
        desc: template.desc,
        time: 'Just now',
        priority: 'High'
      };
      setActivityFeed(prev => [newItem, ...prev.slice(0, 5)]);

      // Display custom AI Triage Toast
      showToast(
        'otp', 
        `AI Triage Engine intercepted a new incident in ${template.loc}. Categorized as High Priority based on risk words.`,
        `🚨 AI Triage Alert: ${template.title}`
      );

      // Add to audit trail for system admin
      const auditLog: AuditLog = {
        id: `aud_sim_${Math.floor(Math.random() * 900) + 100}`,
        timestamp: new Date().toISOString(),
        actor: 'AI_COORDINATOR_BOT',
        action: `Ingested & Triaged simulated incident: "${template.title}" in ${template.loc}`,
        status: 'SUCCESS',
        ipAddress: '127.0.0.1'
      };
      setAuditLogs(prev => [auditLog, ...prev.slice(0, 10)]);

    }, 18000); // Trigger every 18 seconds to keep dashboard highly active but not overwhelming

    return () => clearInterval(interval);
  }, []);

  // Filtered Cases for Grid View
  const filteredCases = useMemo(() => {
    return cases.filter((c) => {
      const matchesSearch = 
        c.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        c.location.toLowerCase().includes(searchQuery.toLowerCase());
      
      const matchesCategory = categoryFilter === 'All' || c.category === categoryFilter;
      const matchesPriority = priorityFilter === 'All' || c.priority === priorityFilter;
      const matchesStatus = statusFilter === 'All' || c.status === statusFilter;

      return matchesSearch && matchesCategory && matchesPriority && matchesStatus;
    });
  }, [cases, searchQuery, categoryFilter, priorityFilter, statusFilter]);

  // Selected Case Object
  const selectedCase = useMemo(() => {
    return cases.find((c) => c.id === selectedCaseId) || null;
  }, [cases, selectedCaseId]);

  // Cases submitted by the Community Member (citizen)
  const mySubmittedCases = useMemo(() => {
    return cases.filter(c => c.contactInfo.includes('member') || c.id.startsWith('my_case_') || c.id === 'case_001');
  }, [cases]);

  // Active missions assigned to the Volunteer
  const myAssignedTasks = useMemo(() => {
    return cases.filter(c => c.status !== 'Resolved' && (c.assignedVolunteerId === 'vol_001' || c.assignedVolunteerId === 'vol_003'));
  }, [cases]);

  // Completed missions by the Volunteer
  const myCompletedTasks = useMemo(() => {
    return cases.filter(c => c.status === 'Resolved' && (c.assignedVolunteerId === 'vol_001' || c.assignedVolunteerId === 'vol_003'));
  }, [cases]);

  /**
   * Submit Incident Need with AI Triaging Simulator
   */
  const handleNeedSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle || !newLocation || !newContact || !newDescription) {
      showToast('error', "Please fill in all required fields.", "Form Incomplete");
      return;
    }

    setIsSubmitting(true);
    setTriageStep('Ingesting report and sanitizing inputs...');

    setTimeout(() => {
      setTriageStep('Analyzing NLP risk factors & safety-threat levels...');
    }, 500);

    setTimeout(() => {
      setTriageStep('Evaluating geographical proximity and local standby personnel...');
    }, 1000);

    try {
      const newCase = await submitNeed({
        title: newTitle,
        category: newCategory,
        location: newLocation,
        contactInfo: newContact,
        description: newDescription,
      });
      
      // Clear fields
      setNewTitle('');
      setNewLocation('');
      setNewContact('');
      setNewDescription('');
      
      // Navigate to Community Needs tab
      setActiveTab('needs');
      setSelectedCaseId(newCase.id);
    } catch (err) {
      // Handled
    } finally {
      setIsSubmitting(false);
      setTriageStep('');
    }
  };

  /**
   * Register Standby Volunteer
   */
  const handleVolunteerReg = (e: React.FormEvent) => {
    e.preventDefault();
    if (!regName || !regPhone || !regLocation) return;
    registerVolunteer({
      name: regName,
      specialty: regSpecialty,
      phone: regPhone,
      location: regLocation
    });
    setIsRegModalOpen(false);
    setRegName('');
    setRegPhone('');
    setRegLocation('');
  };

  /**
   * Resolve Case Action
   */
  const handleResolveCase = (caseId: string) => {
    if (!resolutionNotes.trim()) {
      showToast('error', "Please provide feedback/resolution notes before closing the task.", "Notes Required");
      return;
    }
    updateCaseStatus(caseId, 'Resolved', resolutionNotes);
    setResolvingCaseId(null);
    setResolutionNotes('');
    if (selectedCaseId === caseId) {
      setSelectedCaseId(null);
    }
    
    // Add audit log
    const newLog: AuditLog = {
      id: `aud_${Math.floor(Math.random() * 900) + 100}`,
      timestamp: new Date().toISOString(),
      actor: user?.emailOrPhone || 'SYSTEM',
      action: `Resolved Incident #${caseId} with feedback: "${resolutionNotes}"`,
      status: 'SUCCESS',
      ipAddress: '192.168.1.18'
    };
    setAuditLogs(prev => [newLog, ...prev]);
  };

  const handleDownloadImpactReport = () => {
    showToast('success', "Compiling regional disaster response dataset and volunteer analytics...", "Compiling Report");
    setTimeout(() => {
      showToast('success', "Impact Report PDF has been compiled and downloaded successfully.", "Download Complete");
    }, 1500);
  };

  const priorityColors = {
    Critical: 'bg-red-50 text-red-700 border-red-200',
    High: 'bg-orange-50 text-orange-700 border-orange-200',
    Medium: 'bg-blue-50 text-blue-700 border-blue-200',
    Low: 'bg-slate-50 text-slate-700 border-slate-200'
  };

  const categoryIcons = {
    Rescue: '🚁',
    Medical: '🏥',
    Food: '📦',
    Water: '💧',
    Shelter: '🎪',
    Power: '⚡'
  };

  // Sidebar navigation options
  const navItems = [
    { id: 'dashboard', label: 'Dashboard', icon: <LayoutDashboard size={18} /> },
    { id: 'needs', label: 'Community Needs', icon: <ShieldAlert size={18} />, badge: cases.filter(c => c.status === 'Pending').length },
    { id: 'volunteers', label: 'Volunteers', icon: <Users size={18} /> },
    { id: 'resources', label: 'Resources', icon: <Truck size={18} /> },
    { id: 'tasks', label: 'Tasks Assigned', icon: <ClipboardList size={18} />, badge: myAssignedTasks.length },
    { id: 'analytics', label: 'Analytics', icon: <BarChart3 size={18} /> },
    { id: 'notifications', label: 'Notifications', icon: <Bell size={18} />, badge: 3 },
    { id: 'settings', label: 'Settings', icon: <Settings size={18} /> }
  ];

  return (
    <div className="min-h-screen bg-slate-50 flex text-slate-800 font-sans overflow-hidden">
      
      {/* ==========================================
          LEFT SIDE NAVIGATION MENU (Sidebar)
          ========================================== */}
      {/* Mobile Sidebar Overlay */}
      <AnimatePresence>
        {isSidebarOpen && (
          <motion.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            onClick={() => setIsSidebarOpen(false)}
            className="fixed inset-0 z-40 bg-slate-900/40 backdrop-blur-xs lg:hidden"
          />
        )}
      </AnimatePresence>

      {/* Sidebar Container */}
      <aside className={`fixed inset-y-0 left-0 z-40 w-64 bg-slate-900 text-slate-300 border-r border-slate-800 flex flex-col justify-between transition-transform duration-300 transform lg:translate-x-0 lg:static lg:h-screen ${
        isSidebarOpen ? 'translate-x-0' : '-translate-x-full'
      }`}>
        
        {/* Sidebar Header with exact visual logo */}
        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2.5">
            {/* Logo Squircle with Gradient and Activity pulse */}
            <Logo size={36} />
            <div>
              <span className="text-lg font-black tracking-tight bg-gradient-to-r from-blue-400 to-teal-400 bg-clip-text text-transparent">
                SevaAi
              </span>
              <span className="block text-[8px] font-bold text-slate-400 tracking-wider uppercase">
                NGO Portal
              </span>
            </div>
          </div>
          <button 
            onClick={() => setIsSidebarOpen(false)}
            className="text-slate-400 hover:text-white lg:hidden cursor-pointer"
          >
            <X size={18} />
          </button>
        </div>

        {/* Sidebar Navigation Options */}
        <nav className="flex-1 px-4 py-6 space-y-1.5 overflow-y-auto text-left">
          {navItems.map((item) => {
            const isActive = activeTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => {
                  setActiveTab(item.id);
                  setIsSidebarOpen(false);
                }}
                className={`w-full px-4 py-2.5 rounded-xl text-xs font-bold flex items-center justify-between transition-all cursor-pointer ${
                  isActive 
                    ? 'bg-blue-600 text-white shadow-md shadow-blue-600/10 scale-102' 
                    : 'text-slate-400 hover:text-white hover:bg-slate-800/50'
                }`}
              >
                <div className="flex items-center gap-3">
                  {item.icon}
                  <span>{item.label}</span>
                </div>
                {item.badge !== undefined && item.badge > 0 && (
                  <span className={`px-1.5 py-0.5 rounded-full text-[9px] font-black ${
                    item.id === 'needs' || item.id === 'tasks'
                      ? 'bg-red-500 text-white animate-pulse'
                      : 'bg-slate-800 text-slate-400'
                  }`}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        {/* Sidebar Footer (User info & Logout) */}
        <div className="p-4 border-t border-slate-800 flex flex-col gap-3">
          <div className="flex items-center gap-3 p-2 rounded-xl bg-slate-800/30 border border-slate-800/50">
            <div className="h-8 w-8 rounded-full bg-gradient-to-tr from-blue-500 to-teal-400 text-slate-900 font-black flex items-center justify-center text-xs">
              {user?.name ? user.name.split(' ').map(n => n[0]).join('') : 'U'}
            </div>
            <div className="flex-1 min-w-0 text-left">
              <span className="text-xs font-bold text-white block truncate">{user?.name}</span>
              <span className="text-[9px] font-bold text-emerald-400 tracking-widest uppercase block truncate">
                {user?.role === 'citizen' && 'Community Member'}
                {user?.role === 'volunteer' && 'Field Volunteer'}
                {user?.role === 'coordinator' && 'Coordinator'}
                {user?.role === 'ngo_admin' && 'NGO Admin'}
                {user?.role === 'system_admin' && 'System Admin'}
              </span>
            </div>
          </div>
          <button
            onClick={logout}
            className="w-full px-4 py-2 rounded-xl text-xs font-bold border border-slate-800 hover:border-slate-700 hover:bg-slate-800 text-slate-400 hover:text-red-400 flex items-center justify-center gap-2 transition-all cursor-pointer"
          >
            <LogOut size={14} />
            <span>Secure Logout</span>
          </button>
        </div>

      </aside>

      {/* ==========================================
          RIGHT SIDE MAIN CONTENT AREA
          ========================================== */}
      <div className="flex-1 flex flex-col h-screen overflow-hidden">
        
        {/* Top Header Bar */}
        <header className="bg-white border-b border-slate-200 h-16 px-6 flex items-center justify-between flex-shrink-0">
          <div className="flex items-center gap-3">
            <button
              onClick={() => setIsSidebarOpen(true)}
              className="p-2 -ml-2 text-slate-500 hover:text-slate-800 lg:hidden cursor-pointer"
              aria-label="Open navigation menu"
            >
              <Menu size={20} />
            </button>
            <h2 className="text-sm font-black text-slate-800 uppercase tracking-wider flex items-center gap-2">
              {navItems.find(item => item.id === activeTab)?.label}
              <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            </h2>
          </div>

          <div className="flex items-center gap-4">
            
            {/* Quick Role Switcher for Testing */}
            <div className="hidden sm:flex items-center gap-2 bg-slate-100 p-1 rounded-lg">
              <span className="text-[9px] font-bold text-slate-400 uppercase px-2">Role Switch:</span>
              <select
                value={user?.role}
                onChange={(e) => {
                  if (user) {
                    const updatedUser = { ...user, role: e.target.value as any };
                    sessionStorage.setItem('sevaai_session', JSON.stringify(updatedUser));
                    window.location.reload();
                  }
                }}
                className="bg-white border-none rounded text-[10px] font-bold text-slate-700 p-1 focus:outline-none cursor-pointer"
              >
                <option value="citizen">Community Member</option>
                <option value="volunteer">Volunteer</option>
                <option value="coordinator">Coordinator</option>
                <option value="ngo_admin">NGO Admin</option>
                <option value="system_admin">System Admin</option>
              </select>
            </div>

            <div className="h-4 w-px bg-slate-200 hidden sm:block" />

            <span className="text-[10px] text-slate-400 font-bold flex items-center gap-1.5 bg-slate-50 border border-slate-100 px-3 py-1.5 rounded-lg">
              <span className="h-1.5 w-1.5 rounded-full bg-emerald-500 animate-pulse" />
              Secure AES-256 Connection
            </span>
          </div>
        </header>

        {/* Content Viewport */}
        <div className="flex-1 overflow-y-auto p-6 space-y-6">
          
          {/* ==========================================
              TOP KEY METRIC CARDS (Exact Matches)
              ========================================== */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
            
            {/* Metric 1: Active Cases */}
            <Card className="bg-white border border-slate-100 flex items-center justify-between p-5 hover:shadow-md transition-shadow relative overflow-hidden group">
              <div className="absolute top-0 left-0 h-full w-1 bg-blue-600 group-hover:w-1.5 transition-all" />
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center">
                  <Activity size={24} />
                </div>
                <div className="text-left">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Active Cases</span>
                  <span className="text-2xl font-black text-slate-900 font-mono">
                    {liveActiveCases.toLocaleString()}
                  </span>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xs font-bold text-emerald-600 block">+12%</span>
                <span className="text-[9px] text-slate-400 block">vs last week</span>
              </div>
            </Card>

            {/* Metric 2: Active Volunteers */}
            <Card className="bg-white border border-slate-100 flex items-center justify-between p-5 hover:shadow-md transition-shadow relative overflow-hidden group">
              <div className="absolute top-0 left-0 h-full w-1 bg-emerald-500 group-hover:w-1.5 transition-all" />
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-emerald-50 text-emerald-700 flex items-center justify-center">
                  <Users size={24} />
                </div>
                <div className="text-left">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Active Volunteers</span>
                  <span className="text-2xl font-black text-slate-900 font-mono">
                    {liveActiveVolunteers.toLocaleString()}
                  </span>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xs font-bold text-emerald-600 block">+8%</span>
                <span className="text-[9px] text-slate-400 block">standby roster</span>
              </div>
            </Card>

            {/* Metric 3: Total Resources */}
            <Card className="bg-white border border-slate-100 flex items-center justify-between p-5 hover:shadow-md transition-shadow relative overflow-hidden group">
              <div className="absolute top-0 left-0 h-full w-1 bg-indigo-500 group-hover:w-1.5 transition-all" />
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-indigo-50 text-indigo-700 flex items-center justify-center">
                  <Truck size={24} />
                </div>
                <div className="text-left">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Total Resources</span>
                  <span className="text-2xl font-black text-slate-900 font-mono">8.2k</span>
                </div>
              </div>
              <div className="text-right">
                <span className="text-xs font-bold text-indigo-600 block">98%</span>
                <span className="text-[9px] text-slate-400 block">allocated</span>
              </div>
            </Card>

            {/* Metric 4: Urgent Reports */}
            <Card className="bg-red-50/50 border border-red-100 flex items-center justify-between p-5 hover:shadow-md transition-shadow relative overflow-hidden group">
              <div className="absolute top-0 right-0 h-1.5 w-full bg-red-500" />
              <div className="flex items-center gap-4">
                <div className="h-12 w-12 rounded-xl bg-red-100 text-red-700 flex items-center justify-center relative">
                  <AlertTriangle size={24} className="animate-bounce" />
                  <span className="absolute -top-1 -right-1 h-3 w-3 rounded-full bg-red-500 animate-ping" />
                </div>
                <div className="text-left">
                  <span className="text-[10px] font-bold text-red-600 uppercase tracking-wider block flex items-center gap-1">
                    Urgent Reports
                  </span>
                  <span className="text-2xl font-black text-red-700 font-mono">
                    {liveUrgentReports}
                  </span>
                </div>
              </div>
              <div className="text-right">
                <span className="px-2 py-0.5 rounded-full bg-red-100 text-red-700 text-[9px] font-black uppercase tracking-wide block animate-pulse">
                  Critical
                </span>
                <span className="text-[9px] text-slate-400 block mt-1">Immediate SLA</span>
              </div>
            </Card>

          </div>

          {/* ==========================================
              TAB VIEW 1: MAIN DASHBOARD OVERVIEW (With Premium Animations)
              ========================================== */}
          {activeTab === 'dashboard' && (
            <div className="space-y-6 text-left">
              
              {/* Graphical Representations Grid */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                
                {/* Graph 1: Response Trend (SVG Area Chart with Scanline + Sweeping Animation) */}
                <Card className="bg-white border border-slate-100 lg:col-span-8 p-6 relative overflow-hidden">
                  
                  {/* Decorative Scanline effect */}
                  <div className="absolute top-0 left-0 w-2 h-full bg-gradient-to-r from-blue-500/10 to-transparent pointer-events-none animate-[sweep_4s_ease-in-out_infinite]" />

                  <div className="flex flex-col sm:flex-row sm:items-center justify-between border-b border-slate-100 pb-4 mb-4 gap-2">
                    <div>
                      <h3 className="text-sm font-black text-slate-900 flex items-center gap-2">
                        <BarChart3 size={18} className="text-blue-700" />
                        Disaster Response & Triage Trends (Last 7 Days)
                      </h3>
                      <p className="text-[11px] text-slate-400 mt-0.5">Visual representation of daily incoming reports vs. successfully resolved cases.</p>
                    </div>
                    <div className="flex items-center gap-3 text-[10px] font-bold">
                      <span className="flex items-center gap-1.5 text-slate-500">
                        <span className="h-2.5 w-2.5 rounded-full bg-blue-600" />
                        Incoming Reports
                      </span>
                      <span className="flex items-center gap-1.5 text-slate-500">
                        <span className="h-2.5 w-2.5 rounded-full bg-emerald-500 animate-pulse" />
                        Resolved Cases
                      </span>
                    </div>
                  </div>

                  {/* SVG Area Chart with custom CSS transitions */}
                  <div className="relative pt-6">
                    <svg viewBox="0 0 500 200" className="w-full h-48 select-none">
                      <defs>
                        <linearGradient id="blueGrad-dash" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#1D4ED8" stopOpacity="0.2" />
                          <stop offset="100%" stopColor="#1D4ED8" stopOpacity="0" />
                        </linearGradient>
                        <linearGradient id="emeraldGrad-dash" x1="0" y1="0" x2="0" y2="1">
                          <stop offset="0%" stopColor="#10B981" stopOpacity="0.2" />
                          <stop offset="100%" stopColor="#10B981" stopOpacity="0" />
                        </linearGradient>
                      </defs>

                      {/* Glowing Grid Lines */}
                      <line x1="40" y1="20" x2="480" y2="20" stroke="#F8FAFC" strokeWidth="1" />
                      <line x1="40" y1="60" x2="480" y2="60" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="100" x2="480" y2="100" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="140" x2="480" y2="140" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="170" x2="480" y2="170" stroke="#E2E8F0" strokeWidth="1.5" />

                      {/* Y-Axis Labels */}
                      <text x="15" y="24" className="text-[9px] font-bold fill-slate-400">150</text>
                      <text x="15" y="64" className="text-[9px] font-bold fill-slate-400">100</text>
                      <text x="15" y="104" className="text-[9px] font-bold fill-slate-400">50</text>
                      <text x="15" y="144" className="text-[9px] font-bold fill-slate-400">10</text>
                      <text x="15" y="174" className="text-[9px] font-bold fill-slate-400">0</text>

                      {/* Area Chart Path: Incoming Reports */}
                      <path 
                        d="M 40,170 Q 113,110 186,130 T 332,60 T 480,30 L 480,170 Z" 
                        fill="url(#blueGrad-dash)" 
                      />
                      <path 
                        d="M 40,170 Q 113,110 186,130 T 332,60 T 480,30" 
                        fill="none" 
                        stroke="#1D4ED8" 
                        strokeWidth="3" 
                        strokeLinecap="round"
                        className="stroke-[3] drop-shadow-[0_2px_4px_rgba(29,78,216,0.3)]"
                      />

                      {/* Area Chart Path: Resolved Cases */}
                      <path 
                        d="M 40,170 Q 113,140 186,150 T 332,90 T 480,50 L 480,170 Z" 
                        fill="url(#emeraldGrad-dash)" 
                      />
                      <path 
                        d="M 40,170 Q 113,140 186,150 T 332,90 T 480,50" 
                        fill="none" 
                        stroke="#10B981" 
                        strokeWidth="3" 
                        strokeLinecap="round"
                        className="stroke-[3] drop-shadow-[0_2px_4px_rgba(16,185,129,0.3)]"
                      />

                      {/* Interactive Data Points */}
                      {[
                        { x: 40, incoming: 170, resolved: 170, label: 'Mon', incVal: 12, resVal: 8 },
                        { x: 113, incoming: 110, resolved: 140, label: 'Tue', incVal: 84, resVal: 52 },
                        { x: 186, incoming: 130, resolved: 150, label: 'Wed', incVal: 62, resVal: 41 },
                        { x: 259, incoming: 80, resolved: 110, label: 'Thu', incVal: 112, resVal: 88 },
                        { x: 332, incoming: 60, resolved: 90, label: 'Fri', incVal: 134, resVal: 110 },
                        { x: 405, incoming: 45, resolved: 70, label: 'Sat', incVal: 148, resVal: 125 },
                        { x: 480, incoming: 30, resolved: 50, label: 'Sun', incVal: 165, resVal: 142 }
                      ].map((pt, idx) => (
                        <g key={idx} className="cursor-pointer group/node">
                          <circle 
                            cx={pt.x} 
                            cy={pt.incoming} 
                            r={hoveredLineIndex === idx ? "7" : "4.5"} 
                            fill="#1D4ED8" 
                            stroke="#FFFFFF" 
                            strokeWidth="2.5" 
                            onMouseEnter={() => setHoveredLineIndex(idx)}
                            onMouseLeave={() => setHoveredLineIndex(null)}
                            className="transition-all duration-150"
                          />
                          <circle 
                            cx={pt.x} 
                            cy={pt.resolved} 
                            r={hoveredLineIndex === idx ? "7" : "4.5"} 
                            fill="#10B981" 
                            stroke="#FFFFFF" 
                            strokeWidth="2.5" 
                            onMouseEnter={() => setHoveredLineIndex(idx)}
                            onMouseLeave={() => setHoveredLineIndex(null)}
                            className="transition-all duration-150"
                          />
                          <text x={pt.x - 10} y="190" className="text-[9px] font-bold fill-slate-400 group-hover/node:fill-slate-800 transition-colors">{pt.label}</text>
                        </g>
                      ))}
                    </svg>

                    {/* Interactive Tooltip Overlay */}
                    <AnimatePresence>
                      {hoveredLineIndex !== null && (
                        <motion.div 
                          initial={{ opacity: 0, y: 10, scale: 0.95 }}
                          animate={{ opacity: 1, y: 0, scale: 1 }}
                          exit={{ opacity: 0, y: 10, scale: 0.95 }}
                          className="absolute top-2 left-1/2 -translate-x-1/2 bg-slate-950 text-white p-3 rounded-xl text-xs shadow-xl flex flex-col gap-1.5 pointer-events-none border border-slate-800"
                        >
                          <strong className="font-bold border-b border-slate-800 pb-1 mb-0.5 text-slate-300 flex items-center gap-1">
                            <Clock size={12} />
                            Day Performance Metrics
                          </strong>
                          <span className="flex items-center gap-1.5">
                            <span className="h-2 w-2 rounded-full bg-blue-500" />
                            Incoming Cases: <strong className="text-blue-400">142</strong>
                          </span>
                          <span className="flex items-center gap-1.5">
                            <span className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
                            Resolved Cases: <strong className="text-emerald-400">129</strong>
                          </span>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                </Card>

                {/* Graph 2: Resource Allocation (SVG Donut Chart with Category Interactivity) */}
                <Card className="bg-white border border-slate-100 lg:col-span-4 p-6 flex flex-col justify-between relative overflow-hidden">
                  <div>
                    <h3 className="text-sm font-black text-slate-900 border-b border-slate-100 pb-4 mb-4 flex items-center gap-2">
                      <Database size={18} className="text-blue-700" />
                      Resource Allocation Breakdown
                    </h3>
                    <p className="text-[11px] text-slate-400 leading-relaxed">Click a category segment below to focus metrics and track allocation.</p>
                  </div>

                  {/* SVG Donut Chart */}
                  <div className="flex justify-center items-center py-4 relative">
                    <svg viewBox="0 0 200 200" className="w-36 h-36 select-none transform -rotate-90">
                      {/* Donut Segments */}
                      {/* Segment 1: Water (40%) - Blue */}
                      <circle 
                        cx="100" 
                        cy="100" 
                        r="70" 
                        fill="none" 
                        stroke="#1D4ED8" 
                        strokeWidth={selectedDonutCategory === 'Water' ? "28" : "22"} 
                        strokeDasharray="440" 
                        strokeDashoffset="176" 
                        onClick={() => setSelectedDonutCategory('Water')}
                        className="transition-all duration-200 hover:stroke-blue-800 cursor-pointer"
                      />
                      {/* Segment 2: Medical (25%) - Teal */}
                      <circle 
                        cx="100" 
                        cy="100" 
                        r="70" 
                        fill="none" 
                        stroke="#0F766E" 
                        strokeWidth={selectedDonutCategory === 'Medical' ? "28" : "22"} 
                        strokeDasharray="440" 
                        strokeDashoffset="286" 
                        style={{ transformOrigin: 'center', transform: 'rotate(144deg)' }}
                        onClick={() => setSelectedDonutCategory('Medical')}
                        className="transition-all duration-200 hover:stroke-teal-800 cursor-pointer"
                      />
                      {/* Segment 3: Food (20%) - Orange */}
                      <circle 
                        cx="100" 
                        cy="100" 
                        r="70" 
                        fill="none" 
                        stroke="#F97316" 
                        strokeWidth={selectedDonutCategory === 'Food' ? "28" : "22"} 
                        strokeDasharray="440" 
                        strokeDashoffset="352" 
                        style={{ transformOrigin: 'center', transform: 'rotate(234deg)' }}
                        onClick={() => setSelectedDonutCategory('Food')}
                        className="transition-all duration-200 hover:stroke-orange-600 cursor-pointer"
                      />
                      {/* Segment 4: Shelter & Rescue (15%) - Purple */}
                      <circle 
                        cx="100" 
                        cy="100" 
                        r="70" 
                        fill="none" 
                        stroke="#8B5CF6" 
                        strokeWidth={selectedDonutCategory === 'Shelter' ? "28" : "22"} 
                        strokeDasharray="440" 
                        strokeDashoffset="374" 
                        style={{ transformOrigin: 'center', transform: 'rotate(306deg)' }}
                        onClick={() => setSelectedDonutCategory('Shelter')}
                        className="transition-all duration-200 hover:stroke-purple-700 cursor-pointer"
                      />
                    </svg>

                    {/* Center Text displaying Focused Category */}
                    <div className="absolute flex flex-col items-center justify-center pointer-events-none">
                      <span className="text-xl font-black text-slate-900">
                        {selectedDonutCategory === 'Water' && '40%'}
                        {selectedDonutCategory === 'Medical' && '25%'}
                        {selectedDonutCategory === 'Food' && '20%'}
                        {selectedDonutCategory === 'Shelter' && '15%'}
                      </span>
                      <span className="text-[8px] font-bold text-slate-400 uppercase tracking-widest">
                        {selectedDonutCategory}
                      </span>
                    </div>
                  </div>

                  {/* Donut Legend */}
                  <div className="grid grid-cols-2 gap-2 text-[10px] font-bold mt-2">
                    <button 
                      onClick={() => setSelectedDonutCategory('Water')}
                      className={`flex items-center gap-1.5 p-1.5 rounded-lg text-left cursor-pointer transition-all ${
                        selectedDonutCategory === 'Water' ? 'bg-blue-50 text-blue-800' : 'text-slate-500 hover:bg-slate-50'
                      }`}
                    >
                      <span className="h-2 w-2 rounded-full bg-blue-600" /> Water (40%)
                    </button>
                    <button 
                      onClick={() => setSelectedDonutCategory('Medical')}
                      className={`flex items-center gap-1.5 p-1.5 rounded-lg text-left cursor-pointer transition-all ${
                        selectedDonutCategory === 'Medical' ? 'bg-teal-50 text-teal-800' : 'text-slate-500 hover:bg-slate-50'
                      }`}
                    >
                      <span className="h-2 w-2 rounded-full bg-teal-700" /> Medical (25%)
                    </button>
                    <button 
                      onClick={() => setSelectedDonutCategory('Food')}
                      className={`flex items-center gap-1.5 p-1.5 rounded-lg text-left cursor-pointer transition-all ${
                        selectedDonutCategory === 'Food' ? 'bg-orange-50 text-orange-800' : 'text-slate-500 hover:bg-slate-50'
                      }`}
                    >
                      <span className="h-2 w-2 rounded-full bg-orange-500" /> Food (20%)
                    </button>
                    <button 
                      onClick={() => setSelectedDonutCategory('Shelter')}
                      className={`flex items-center gap-1.5 p-1.5 rounded-lg text-left cursor-pointer transition-all ${
                        selectedDonutCategory === 'Shelter' ? 'bg-purple-50 text-purple-800' : 'text-slate-500 hover:bg-slate-50'
                      }`}
                    >
                      <span className="h-2 w-2 rounded-full bg-purple-500" /> Shelter (15%)
                    </button>
                  </div>
                </Card>

              </div>

              {/* Grid: Recent Activity & Live Incident GIS Mapping */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6">
                
                {/* Left Side: Live Urgent Reports Map Pins (Interactive GIS Map) */}
                <div className="lg:col-span-8 flex flex-col gap-4">
                  <h3 className="text-sm font-black text-slate-900 flex items-center gap-2">
                    <MapPin size={18} className="text-red-500 animate-bounce" />
                    Urgent Incident GIS Mapping Grid & Dispatch Overlay
                  </h3>

                  <Card className="bg-slate-950 rounded-2xl border border-slate-900 shadow-inner p-4 relative h-72 flex items-center justify-center overflow-hidden">
                    
                    {/* SVG Map Background */}
                    <svg viewBox="0 0 1000 700" className="w-full h-full select-none absolute inset-0 opacity-30">
                      <defs>
                        <pattern id="grid-dash" width="40" height="40" patternUnits="userSpaceOnUse">
                          <path d="M 40 0 L 0 0 0 40" fill="none" stroke="#ffffff" strokeWidth="1" strokeOpacity="0.04" />
                        </pattern>
                      </defs>
                      <rect width="100%" height="100%" fill="url(#grid-dash)" />
                      <line x1="0" y1="350" x2="1000" y2="350" stroke="#1E293B" strokeWidth="4" />
                      <line x1="500" y1="0" x2="500" y2="700" stroke="#1E293B" strokeWidth="4" />
                      <path d="M 150 0 C 300 200, 250 400, 500 500 C 700 580, 800 600, 1000 700 L 1000 700 L 950 700 C 750 600, 650 580, 450 500 C 200 400, 250 200, 100 0 Z" fill="#0B132B" />
                    </svg>

                    {/* Dispatch Overlay: Draw animated line from nearest volunteer to selected case */}
                    {selectedCase && (
                      <svg viewBox="0 0 1000 700" className="w-full h-full absolute inset-0 pointer-events-none">
                        <line 
                          x1={selectedCase.coordinates.x * 10} 
                          y1={selectedCase.coordinates.y * 7} 
                          x2="500" 
                          y2="350" 
                          stroke="#10B981" 
                          strokeWidth="3" 
                          strokeDasharray="8,8" 
                          className="animate-[dash_4s_linear_infinite]"
                        />
                        <circle cx="500" cy="350" r="8" fill="#10B981" stroke="#FFFFFF" strokeWidth="2" />
                        <text x="515" y="354" className="text-[11px] font-bold fill-emerald-400">Standby Base</text>
                      </svg>
                    )}

                    {/* Glowing Map Hotspots */}
                    {cases.map((c) => {
                      const isSelected = selectedCaseId === c.id;
                      const isCritical = c.priority === 'Critical';
                      const color = isCritical ? '#EF4444' : c.priority === 'High' ? '#F97316' : '#3B82F6';
                      
                      return (
                        <div 
                          key={c.id} 
                          className="absolute cursor-pointer flex flex-col items-center group z-10"
                          style={{ left: `${c.coordinates.x}%`, top: `${c.coordinates.y}%`, transform: 'translate(-50%, -50%)' }}
                          onClick={() => {
                            setSelectedCaseId(c.id);
                            showToast('info', `Focused dispatch control on ${c.title}.`, "GIS Focus");
                          }}
                        >
                          {isCritical && (
                            <span className="h-5 w-5 rounded-full bg-red-600/30 animate-ping absolute" />
                          )}
                          <span 
                            className={`h-3.5 w-3.5 rounded-full border-2 border-white relative z-10 transition-transform duration-200 hover:scale-125`} 
                            style={{ backgroundColor: color }}
                          />
                          
                          {/* Floating interactive tooltip */}
                          <div className="absolute bottom-5 bg-slate-900 border border-slate-800 text-white text-[10px] font-bold px-2.5 py-1.5 rounded-lg shadow-lg opacity-0 group-hover:opacity-100 transition-opacity whitespace-nowrap pointer-events-none flex items-center gap-1">
                            <span>{categoryIcons[c.category]}</span>
                            <span>{c.title}</span>
                          </div>
                        </div>
                      );
                    })}

                    <div className="absolute bottom-4 right-4 bg-slate-950/80 border border-slate-800 rounded-lg p-2.5 text-[10px] text-slate-400 text-left">
                      <span className="font-bold text-white block">GIS Live Route Active</span>
                      <span>Dashed line shows dispatched route from Standby Base.</span>
                    </div>
                  </Card>
                </div>

                {/* Right Side: Recent Activity Feed */}
                <Card className="bg-white border border-slate-100 lg:col-span-4 p-6 flex flex-col justify-between relative overflow-hidden">
                  <div>
                    <h3 className="text-sm font-black text-slate-900 border-b border-slate-100 pb-4 mb-4 flex items-center gap-2">
                      <Activity size={18} className="text-blue-700" />
                      Live Activity Ticker
                    </h3>

                    <div className="space-y-4 text-xs">
                      <AnimatePresence mode="popLayout">
                        {activityFeed.map((item) => (
                          <motion.div 
                            key={item.id}
                            initial={{ opacity: 0, x: -20 }}
                            animate={{ opacity: 1, x: 0 }}
                            exit={{ opacity: 0, x: 20 }}
                            className="flex gap-3 items-start border-b border-slate-50 pb-3 last:border-0"
                          >
                            <div className={`h-2.5 w-2.5 rounded-full mt-1 flex-shrink-0 ${
                              item.priority === 'Critical' ? 'bg-red-500 animate-pulse' :
                              item.type === 'RESOLVED' ? 'bg-emerald-500' : 'bg-blue-500'
                            }`} />
                            <div className="flex-1">
                              <strong className="font-bold text-slate-800 block">{item.title}</strong>
                              <p className="text-slate-500 mt-0.5 leading-relaxed">{item.desc}</p>
                              <span className="text-[9px] text-slate-400 block mt-1">{item.time}</span>
                            </div>
                          </motion.div>
                        ))}
                      </AnimatePresence>
                    </div>
                  </div>

                  <Button
                    variant="outline"
                    size="sm"
                    className="w-full mt-4"
                    onClick={() => setActiveTab('notifications')}
                    icon={<Bell size={12} />}
                  >
                    View All Platform Alerts
                  </Button>
                </Card>

              </div>

            </div>
          )}

          {/* ==========================================
              TAB VIEW 2: COMMUNITY NEEDS (Incident Queue)
              ========================================== */}
          {activeTab === 'needs' && (
            <div className="space-y-6 text-left">
              
              {/* Search, Filter & AI Prioritization Panel */}
              <div className="bg-white rounded-xl border border-slate-100 p-4 flex flex-wrap gap-4 items-center justify-between">
                <div className="relative flex-1 min-w-[240px]">
                  <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
                  <input
                    type="text"
                    placeholder="Search by title, report description, or district..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full bg-slate-50 border border-slate-200 rounded-lg pl-10 pr-4 py-2 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div className="flex flex-wrap gap-3">
                  <div className="flex items-center gap-1.5">
                    <span className="text-[10px] font-bold text-slate-400 uppercase">Category:</span>
                    <select
                      value={categoryFilter}
                      onChange={(e) => setCategoryFilter(e.target.value)}
                      className="bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs focus:outline-none cursor-pointer"
                    >
                      <option value="All">All Categories</option>
                      <option value="Rescue">Rescue</option>
                      <option value="Medical">Medical</option>
                      <option value="Food">Food</option>
                      <option value="Water">Water</option>
                      <option value="Shelter">Shelter</option>
                      <option value="Power">Power</option>
                    </select>
                  </div>

                  <div className="flex items-center gap-1.5">
                    <span className="text-[10px] font-bold text-slate-400 uppercase">Priority:</span>
                    <select
                      value={priorityFilter}
                      onChange={(e) => setPriorityFilter(e.target.value)}
                      className="bg-slate-50 border border-slate-200 rounded-lg px-2.5 py-1.5 text-xs focus:outline-none cursor-pointer"
                    >
                      <option value="All">All Priorities</option>
                      <option value="Critical">Critical</option>
                      <option value="High">High</option>
                      <option value="Medium">Medium</option>
                      <option value="Low">Low</option>
                    </select>
                  </div>
                </div>
              </div>

              {/* Split layout: GIS Map on left, detail drawer on right */}
              <div className="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
                
                {/* Incident Queue List */}
                <div className="lg:col-span-8 space-y-4">
                  {filteredCases.length > 0 ? (
                    filteredCases.map((c) => {
                      const isSelected = selectedCaseId === c.id;
                      return (
                        <Card 
                          key={c.id} 
                          onClick={() => setSelectedCaseId(c.id)}
                          className={`cursor-pointer transition-all border-l-4 text-left hover:shadow-md ${
                            isSelected ? 'border-l-blue-600 bg-blue-50/10 ring-1 ring-blue-100' : 'border-l-slate-300'
                          }`}
                        >
                          <div className="flex justify-between items-start gap-4">
                            <div className="flex-1">
                              <div className="flex items-center gap-2">
                                <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest">{c.id}</span>
                                <span className={`px-2 py-0.5 rounded-sm text-[9px] font-extrabold border ${priorityColors[c.priority]}`}>
                                  {c.priority}
                                </span>
                              </div>
                              <h4 className="text-sm font-black text-slate-900 mt-1">{c.title}</h4>
                              <p className="text-xs text-slate-500 mt-1 line-clamp-2">{c.description}</p>
                              <div className="text-[10px] text-slate-400 flex flex-wrap gap-x-4 gap-y-1 mt-2">
                                <span>📍 Location: {c.location}</span>
                                <span>🕒 Reported: {new Date(c.dateSubmitted).toLocaleTimeString()}</span>
                              </div>
                            </div>
                            <ChevronRight size={16} className="text-slate-400 self-center" />
                          </div>
                        </Card>
                      );
                    })
                  ) : (
                    <div className="bg-white rounded-xl border border-slate-100 p-12 text-center text-slate-400">
                      <ShieldAlert size={40} className="text-slate-300 mx-auto mb-3" />
                      <h4 className="text-xs font-bold text-slate-700">No Incident Reports Match Filters</h4>
                      <p className="text-[11px] text-slate-400 mt-1">Try clearing some query filters or search keywords.</p>
                    </div>
                  )}
                </div>

                {/* GIS Dispatch panel details */}
                <div className="lg:col-span-4">
                  <Card className="bg-white border border-slate-100 p-6 flex flex-col justify-between h-full">
                    <h3 className="text-sm font-black text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                      <Sliders size={18} className="text-blue-700" />
                      GIS Incident Dispatch Detail
                    </h3>

                    {selectedCase ? (
                      <div className="space-y-4">
                        <div className="flex justify-between items-start">
                          <div>
                            <span className="text-[9px] font-bold text-slate-400 block">Incident ID: {selectedCase.id}</span>
                            <h4 className="text-sm font-black text-slate-900 mt-0.5">{selectedCase.title}</h4>
                          </div>
                          <span className={`px-2 py-0.5 rounded-sm text-[9px] font-extrabold border ${priorityColors[selectedCase.priority]}`}>
                            {selectedCase.priority}
                          </span>
                        </div>

                        <div className="grid grid-cols-2 gap-2 text-xs">
                          <div className="p-2.5 rounded-lg bg-slate-50 border border-slate-100">
                            <span className="text-[9px] font-bold text-slate-400 block uppercase">Category</span>
                            <span className="font-bold text-slate-800 mt-0.5 flex items-center gap-1">
                              {categoryIcons[selectedCase.category]} {selectedCase.category}
                            </span>
                          </div>
                          <div className="p-2.5 rounded-lg bg-slate-50 border border-slate-100">
                            <span className="text-[9px] font-bold text-slate-400 block uppercase">Operational Status</span>
                            <span className="font-bold text-slate-800 mt-0.5 block">{selectedCase.status}</span>
                          </div>
                        </div>

                        <div className="text-xs space-y-1 text-slate-600">
                          <p>📍 <strong>Location:</strong> {selectedCase.location}</p>
                          <p>📞 <strong>Contact:</strong> {selectedCase.contactInfo}</p>
                        </div>

                        <div className="p-3 bg-slate-50 border border-slate-100 rounded-lg text-xs leading-relaxed text-slate-600">
                          <strong>Incident Report:</strong> {selectedCase.description}
                        </div>

                        <div className="border-t border-slate-100 pt-4">
                          {selectedCase.status === 'Resolved' ? (
                            <div className="p-3 bg-emerald-50 border border-emerald-100 rounded-xl text-xs text-emerald-800">
                              <strong className="font-bold block">✓ Incident Resolved Successfully</strong>
                              <p className="mt-0.5">{selectedCase.resolutionNotes}</p>
                            </div>
                          ) : (
                            <div className="flex flex-col gap-2">
                              {selectedCase.assignedVolunteerId ? (
                                <div className="p-3 bg-blue-50 border border-blue-100 rounded-xl text-xs text-blue-800">
                                  <strong>Assigned Responder:</strong> Standby personnel active in field.
                                </div>
                              ) : (
                                <div className="flex flex-col gap-2">
                                  <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">Match Standby Responder</span>
                                  <div className="flex gap-2">
                                    <select 
                                      className="flex-1 bg-slate-50 border border-slate-200 rounded-lg p-2 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                                      id="needs-vol-select"
                                      defaultValue=""
                                    >
                                      <option value="" disabled>Select responder...</option>
                                      {volunteers.map(v => (
                                        <option key={v.id} value={v.id}>{v.name} ({v.specialty})</option>
                                      ))}
                                    </select>
                                    <Button
                                      variant="secondary"
                                      size="sm"
                                      onClick={() => {
                                        const select = document.getElementById('needs-vol-select') as HTMLSelectElement;
                                        if (select && select.value) {
                                          assignVolunteer(selectedCase.id, select.value);
                                        } else {
                                          showToast('error', "Please select a responder first.", "Match Error");
                                        }
                                      }}
                                    >
                                      Assign
                                    </Button>
                                  </div>
                                </div>
                              )}
                            </div>
                          )}
                        </div>
                      </div>
                    ) : (
                      <div className="flex-1 flex flex-col items-center justify-center text-center p-6 text-slate-400 gap-3">
                        <div className="h-12 w-12 rounded-full bg-slate-50 flex items-center justify-center text-slate-300">
                          <MapPin size={24} />
                        </div>
                        <div>
                          <h4 className="text-xs font-bold text-slate-700">No Incident Selected</h4>
                          <p className="text-[11px] text-slate-400 mt-1 max-w-[200px] leading-relaxed">
                            Select any incident card in the queue to load dispatch control options.
                          </p>
                        </div>
                      </div>
                    )}
                  </Card>
                </div>

              </div>

            </div>
          )}

          {/* ==========================================
              TAB VIEW 3: VOLUNTEERS STANDBY ROSTER
              ========================================== */}
          {activeTab === 'volunteers' && (
            <div className="space-y-6 text-left">
              
              <div className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-100 flex-shrink-0">
                <div>
                  <h3 className="text-sm font-black text-slate-900">Standby Emergency Responders</h3>
                  <p className="text-xs text-slate-500 mt-0.5">Personnel currently registered in the regional coordination database.</p>
                </div>
                <Button
                  variant="success"
                  size="sm"
                  onClick={() => setIsRegModalOpen(true)}
                  icon={<PlusCircle size={14} />}
                >
                  Register Standby Responder
                </Button>
              </div>

              {/* Standby roster grid */}
              <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                {volunteers.map((v) => (
                  <Card key={v.id} className="flex flex-col justify-between">
                    <div className="flex flex-col gap-3">
                      <div className="flex justify-between items-start gap-2">
                        <div>
                          <span className="text-[9px] font-bold text-slate-400 uppercase tracking-widest">{v.id}</span>
                          <h4 className="text-xs font-black text-slate-900 mt-0.5">{v.name}</h4>
                        </div>
                        <span className={`px-2 py-0.5 rounded-full text-[9px] font-bold ${
                          v.status === 'Available' ? 'bg-green-50 text-green-700 border border-green-100' :
                          v.status === 'Active' ? 'bg-orange-50 text-orange-700 border border-orange-100' :
                          'bg-slate-100 text-slate-600'
                        }`}>
                          {v.status}
                        </span>
                      </div>
                      <span className="px-2 py-0.5 rounded-lg bg-blue-50 text-blue-800 text-[10px] font-bold self-start">
                        🔧 {v.specialty} Specialist
                      </span>
                      <div className="text-[11px] text-slate-500 flex flex-col gap-1 border-t border-slate-100 pt-3 mt-1">
                        <span>📍 Base District: {v.location}</span>
                        <span>📞 Phone: {v.phone}</span>
                        <span>🏆 Completed Tasks: {v.casesCompleted}</span>
                      </div>
                    </div>
                  </Card>
                ))}
              </div>

              {/* Register Volunteer Modal */}
              {isRegModalOpen && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
                  <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs" onClick={() => setIsRegModalOpen(false)} />
                  <motion.div 
                    initial={{ opacity: 0, scale: 0.95, y: 20 }}
                    animate={{ opacity: 1, scale: 1, y: 0 }}
                    className="bg-white rounded-xl shadow-xl border border-slate-100 p-6 max-w-md w-full relative z-10"
                  >
                    <h3 className="text-sm font-black text-slate-900 mb-1">Add Standby Personnel</h3>
                    <p className="text-[11px] text-slate-400 mb-4">
                      Register a new standby responder into the emergency dispatch network database.
                    </p>
                    <form onSubmit={handleVolunteerReg} className="flex flex-col gap-4">
                      <div>
                        <label className="text-xs font-bold text-slate-700 uppercase block mb-1">Full Name</label>
                        <input 
                          type="text" 
                          required 
                          value={regName} 
                          onChange={(e) => setRegName(e.target.value)}
                          placeholder="e.g. Mark Robinson"
                          className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs font-bold text-slate-700 uppercase block mb-1">Specialty Discipline</label>
                        <select 
                          value={regSpecialty} 
                          onChange={(e: any) => setRegSpecialty(e.target.value)}
                          className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs focus:outline-none"
                        >
                          <option value="General">General Logistics & Support</option>
                          <option value="Medical">Medical Care (Doctor/Nurse/EMT)</option>
                          <option value="Rescue">Search & Rescue Operations</option>
                          <option value="Water">Water Logistics & Plumbing</option>
                          <option value="Shelter">Shelter Assembly & Care</option>
                          <option value="Power">Electrical & Power Systems</option>
                        </select>
                      </div>
                      <div>
                        <label className="text-xs font-bold text-slate-700 uppercase block mb-1">Phone Number</label>
                        <input 
                          type="tel" 
                          required 
                          value={regPhone} 
                          onChange={(e) => setRegPhone(e.target.value)}
                          placeholder="e.g. +1 (555) 909-0808"
                          className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div>
                        <label className="text-xs font-bold text-slate-700 uppercase block mb-1">Base District</label>
                        <input 
                          type="text" 
                          required 
                          value={regLocation} 
                          onChange={(e) => setRegLocation(e.target.value)}
                          placeholder="e.g. Riverside Sector"
                          className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-xs focus:outline-none focus:ring-2 focus:ring-blue-500"
                        />
                      </div>
                      <div className="flex gap-3 justify-end mt-2">
                        <button 
                          type="button" 
                          onClick={() => setIsRegModalOpen(false)}
                          className="px-4 py-2 text-xs font-bold text-slate-500 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
                        >
                          Cancel
                        </button>
                        <Button type="submit" variant="success" size="sm">
                          Register Responder
                        </Button>
                      </div>
                    </form>
                  </motion.div>
                </div>
              )}

            </div>
          )}

          {/* ==========================================
              TAB VIEW 4: RESOURCES INVENTORY
              ========================================== */}
          {activeTab === 'resources' && (
            <div className="space-y-6 text-left">
              
              <div className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-100 flex-shrink-0">
                <div>
                  <h3 className="text-sm font-black text-slate-900">Regional Supply Inventories</h3>
                  <p className="text-xs text-slate-500 mt-0.5">Manage critical humanitarian logistics, cots, boats, and medical kits.</p>
                </div>
                <Button
                  variant="primary"
                  size="sm"
                  onClick={() => {
                    showToast('success', "Dispatched replenishment orders to Geneva HQ warehouse.", "Warehouse Synced");
                  }}
                  icon={<RefreshCw size={12} />}
                >
                  Sync Global Warehouse
                </Button>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {resources.map((r, idx) => {
                  const percentage = Math.round((r.available / r.total) * 100);
                  return (
                    <Card key={idx} className="bg-white border border-slate-100 flex flex-col justify-between">
                      <div>
                        <div className="flex justify-between items-center">
                          <strong className="font-black text-slate-900 text-xs">{r.category}</strong>
                          <span className={`px-2 py-0.5 rounded-sm text-[9px] font-extrabold ${
                            percentage < 30 ? 'bg-red-100 text-red-700' : 'bg-emerald-100 text-emerald-700'
                          }`}>
                            {percentage < 30 ? 'LOW STOCK' : 'ADEQUATE'}
                          </span>
                        </div>
                        <div className="text-2xl font-black text-slate-800 mt-3">
                          {r.available} <span className="text-xs font-normal text-slate-400">/ {r.total} {r.unit}</span>
                        </div>
                      </div>

                      <div className="mt-4">
                        <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden">
                          <div 
                            className={`h-full rounded-full transition-all duration-500 ${percentage < 30 ? 'bg-red-500' : 'bg-emerald-500'}`}
                            style={{ width: `${percentage}%` }}
                          />
                        </div>
                      </div>
                    </Card>
                  );
                })}
              </div>

            </div>
          )}

          {/* ==========================================
              TAB VIEW 5: TASKS ASSIGNED
              ========================================== */}
          {activeTab === 'tasks' && (
            <div className="space-y-6 text-left">
              
              <div className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-100 flex-shrink-0">
                <div>
                  <h3 className="text-sm font-black text-slate-900">Assigned Field Missions</h3>
                  <p className="text-xs text-slate-500 mt-0.5">Tasks matched to standby personnel for regional crisis response.</p>
                </div>
              </div>

              {myAssignedTasks.length > 0 ? (
                <div className="space-y-4">
                  {myAssignedTasks.map((task) => (
                    <Card key={task.id} className="border-l-4 border-l-emerald-500 bg-white">
                      <div className="flex flex-col md:flex-row justify-between gap-6 items-start md:items-center">
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <span className="text-[10px] font-bold text-slate-400 uppercase">Task ID: {task.id}</span>
                            <span className={`px-2 py-0.5 rounded-sm text-[9px] font-extrabold border ${priorityColors[task.priority]}`}>
                              {task.priority} Priority
                            </span>
                          </div>
                          <h4 className="text-sm font-black text-slate-900 mt-1.5">{task.title}</h4>
                          <p className="text-xs text-slate-500 mt-1">{task.description}</p>
                          <div className="text-[10px] text-slate-400 flex flex-wrap gap-x-4 gap-y-1 mt-2">
                            <span>📍 Location: {task.location}</span>
                            <span>📞 Contact: {task.contactInfo}</span>
                          </div>
                        </div>

                        {/* Completion Feedback Drawer */}
                        <div className="w-full md:w-[320px] bg-slate-50 p-4 rounded-xl border border-slate-100">
                          {resolvingCaseId === task.id ? (
                            <div className="flex flex-col gap-3">
                              <textarea
                                required
                                value={resolutionNotes}
                                onChange={(e) => setResolutionNotes(e.target.value)}
                                placeholder="Describe response action, resources deployed, and completion details..."
                                className="w-full text-xs p-2.5 bg-white border border-slate-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                                rows={3}
                              />
                              <div className="flex gap-2 justify-end">
                                <button
                                  onClick={() => setResolvingCaseId(null)}
                                  className="px-3 py-1.5 text-xs font-bold text-slate-500 hover:bg-slate-100 rounded-lg cursor-pointer"
                                >
                                  Cancel
                                </button>
                                <Button
                                  variant="success"
                                  size="sm"
                                  onClick={() => handleResolveCase(task.id)}
                                >
                                  Submit Feedback
                                </Button>
                              </div>
                            </div>
                          ) : (
                            <div className="flex flex-col gap-2">
                              <span className="text-[10px] font-bold text-slate-500 block">
                                Once this task is completed, submit completion feedback to release resources and log impact.
                              </span>
                              <Button
                                variant="success"
                                size="sm"
                                className="w-full"
                                onClick={() => setResolvingCaseId(task.id)}
                                icon={<CheckCircle size={14} />}
                              >
                                Mark Completed
                              </Button>
                            </div>
                          )}
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              ) : (
                <div className="bg-white rounded-xl border border-slate-100 p-12 text-center text-slate-400">
                  <CheckCircle size={40} className="text-emerald-500/20 mx-auto mb-3" />
                  <h4 className="text-xs font-bold text-slate-700">All Assigned Tasks Completed</h4>
                  <p className="text-[11px] text-slate-400 mt-1">Standby for new emergency dispatches from the coordination team.</p>
                </div>
              )}

            </div>
          )}

          {/* ==========================================
              TAB VIEW 6: ANALYTICS REPORTS
              ========================================== */}
          {activeTab === 'analytics' && (
            <div className="space-y-6 text-left">
              
              <div className="flex justify-between items-center bg-white p-4 rounded-xl border border-slate-100 flex-shrink-0">
                <div>
                  <h3 className="text-sm font-black text-slate-900">NGO Impact & Analytical Reports</h3>
                  <p className="text-xs text-slate-500 mt-0.5">Download visual summaries of regional disaster relief metrics, volunteer performance, and resource distribution logs.</p>
                </div>
                <Button
                  variant="success"
                  size="sm"
                  onClick={handleDownloadImpactReport}
                  icon={<Download size={14} />}
                >
                  Compile Impact Report
                </Button>
              </div>

              {/* Graphical representations grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                
                {/* Visual Representation 1: Multi-Bar Chart with Springy Scales */}
                <Card className="bg-white border border-slate-100 p-6 relative overflow-hidden">
                  <h4 className="text-xs font-black text-slate-900 border-b border-slate-100 pb-3 mb-4">
                    Volunteer Performance by District (Active vs. Standby)
                  </h4>
                  <div className="relative pt-6">
                    <svg viewBox="0 0 500 200" className="w-full h-48 select-none">
                      <line x1="40" y1="20" x2="480" y2="20" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="70" x2="480" y2="70" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="120" x2="480" y2="120" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="170" x2="480" y2="170" stroke="#E2E8F0" strokeWidth="1.5" />

                      <text x="15" y="24" className="text-[9px] font-bold fill-slate-400">100</text>
                      <text x="15" y="74" className="text-[9px] font-bold fill-slate-400">50</text>
                      <text x="15" y="124" className="text-[9px] font-bold fill-slate-400">10</text>
                      <text x="15" y="174" className="text-[9px] font-bold fill-slate-400">0</text>

                      {/* Bars with dynamic interactive scales */}
                      {[
                        { x: 60, active: 110, standby: 50, label: 'Riverside', actVal: 60, stbVal: 120 },
                        { x: 160, active: 80, standby: 90, label: 'Northside', actVal: 90, stbVal: 80 },
                        { x: 260, active: 140, standby: 60, label: 'Westside', actVal: 30, stbVal: 110 },
                        { x: 360, active: 90, standby: 100, label: 'Central', actVal: 80, stbVal: 70 },
                        { x: 460, active: 120, standby: 40, label: 'Southside', actVal: 50, stbVal: 130 }
                      ].map((item, idx) => (
                        <g key={idx}>
                          {/* Active Bar */}
                          <rect 
                            x={item.x} 
                            y={item.active} 
                            width="14" 
                            height={170 - item.active} 
                            fill="#1D4ED8" 
                            rx="2.5"
                            onMouseEnter={() => setHoveredBarIndex(idx)}
                            onMouseLeave={() => setHoveredBarIndex(null)}
                            className="transition-all duration-200 cursor-pointer hover:fill-blue-800"
                          />
                          {/* Standby Bar */}
                          <rect 
                            x={item.x + 18} 
                            y={item.standby} 
                            width="14" 
                            height={170 - item.standby} 
                            fill="#10B981" 
                            rx="2.5"
                            onMouseEnter={() => setHoveredBarIndex(idx)}
                            onMouseLeave={() => setHoveredBarIndex(null)}
                            className="transition-all duration-200 cursor-pointer hover:fill-emerald-600"
                          />
                          <text x={item.x - 5} y="190" className="text-[9px] font-bold fill-slate-400">{item.label}</text>
                        </g>
                      ))}
                    </svg>

                    {/* Bar Tooltip */}
                    <AnimatePresence>
                      {hoveredBarIndex !== null && (
                        <motion.div 
                          initial={{ opacity: 0, scale: 0.9 }}
                          animate={{ opacity: 1, scale: 1 }}
                          exit={{ opacity: 0, scale: 0.9 }}
                          className="absolute top-2 left-1/2 -translate-x-1/2 bg-slate-900 text-white p-2.5 rounded-lg text-[10px] shadow-xl pointer-events-none border border-slate-800"
                        >
                          Active: <strong className="text-blue-400">84</strong> | Standby: <strong className="text-emerald-400">92</strong>
                        </motion.div>
                      )}
                    </AnimatePresence>
                  </div>
                </Card>

                {/* Visual Representation 2: Multi-Series Line Chart with animated SLA marker */}
                <Card className="bg-white border border-slate-100 p-6 relative overflow-hidden">
                  <h4 className="text-xs font-black text-slate-900 border-b border-slate-100 pb-3 mb-4">
                    Incident Dispatch Velocity (Response Time over Last 6 Months)
                  </h4>
                  <div className="relative pt-6">
                    <svg viewBox="0 0 500 200" className="w-full h-48 select-none">
                      <line x1="40" y1="20" x2="480" y2="20" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="70" x2="480" y2="70" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="120" x2="480" y2="120" stroke="#F1F5F9" strokeWidth="1" />
                      <line x1="40" y1="170" x2="480" y2="170" stroke="#E2E8F0" strokeWidth="1.5" />

                      <text x="15" y="24" className="text-[9px] font-bold fill-slate-400">60m</text>
                      <text x="15" y="74" className="text-[9px] font-bold fill-slate-400">30m</text>
                      <text x="15" y="124" className="text-[9px] font-bold fill-slate-400">10m</text>
                      <text x="15" y="174" className="text-[9px] font-bold fill-slate-400">0m</text>

                      {/* Multi Series Line Path (Target SLA vs Actual Response) */}
                      <path d="M 40,70 L 128,70 L 216,70 L 304,70 L 392,70 L 480,70" fill="none" stroke="#EF4444" strokeWidth="2" strokeDasharray="5,5" />
                      <path d="M 40,140 Q 128,110 216,90 T 304,50 T 392,40 T 480,30" fill="none" stroke="#1D4ED8" strokeWidth="3" className="stroke-[3]" />

                      {[
                        { x: 40, label: 'Nov' },
                        { x: 128, label: 'Dec' },
                        { x: 216, label: 'Jan' },
                        { x: 304, label: 'Feb' },
                        { x: 392, label: 'Mar' },
                        { x: 480, label: 'Apr' }
                      ].map((item, idx) => (
                        <g key={idx}>
                          <circle cx={item.x} cy={70} r="3" fill="#EF4444" />
                          <circle cx={item.x} cy={140 - idx * 20} r="4.5" fill="#1D4ED8" stroke="#FFFFFF" strokeWidth="2" />
                          <text x={item.x - 10} y="190" className="text-[9px] font-bold fill-slate-400">{item.label}</text>
                        </g>
                      ))}
                    </svg>
                  </div>
                </Card>

              </div>

            </div>
          )}

          {/* ==========================================
              TAB VIEW 7: NOTIFICATIONS
              ========================================== */}
          {activeTab === 'notifications' && (
            <div className="space-y-6 text-left">
              
              <div className="bg-white rounded-xl border border-slate-100 p-6">
                <h3 className="text-sm font-black text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                  <Bell size={18} className="text-blue-700" />
                  System Notifications & Broadcast Alerts
                </h3>

                <div className="divide-y divide-slate-100">
                  
                  <div className="py-4 flex gap-4 items-start">
                    <div className="h-2 w-2 rounded-full bg-red-500 mt-1.5 animate-ping flex-shrink-0" />
                    <div>
                      <strong className="text-red-700 text-xs font-black">CRITICAL: Severe Weather Warning Broadcasted</strong>
                      <p className="text-xs text-slate-500 mt-0.5">Standby responders in Westside Sector have been alerted to flash flood risks.</p>
                      <span className="text-[10px] text-slate-400 block mt-1">10 minutes ago</span>
                    </div>
                  </div>

                  <div className="py-4 flex gap-4 items-start">
                    <div className="h-2 w-2 rounded-full bg-blue-500 mt-1.5 flex-shrink-0" />
                    <div>
                      <strong className="text-slate-800 text-xs font-black">AI Prioritization Engine Complete</strong>
                      <p className="text-xs text-slate-500 mt-0.5">Automatically triaged case #case_004 as High Priority (Water Contamination Response).</p>
                      <span className="text-[10px] text-slate-400 block mt-1">1 hour ago</span>
                    </div>
                  </div>

                  <div className="py-4 flex gap-4 items-start">
                    <div className="h-2 w-2 rounded-full bg-green-500 mt-1.5 flex-shrink-0" />
                    <div>
                      <strong className="text-slate-800 text-xs font-black">Global Supply Chain Replenishment</strong>
                      <p className="text-xs text-slate-500 mt-0.5">Red Cross Warehouse in Geneva successfully processed cots and rations restock requests.</p>
                      <span className="text-[10px] text-slate-400 block mt-1">4 hours ago</span>
                    </div>
                  </div>

                </div>
              </div>

            </div>
          )}

          {/* ==========================================
              TAB VIEW 8: SETTINGS
              ========================================== */}
          {activeTab === 'settings' && (
            <div className="space-y-6 text-left">
              
              <Card className="bg-white">
                <h3 className="text-sm font-black text-slate-900 border-b border-slate-100 pb-3 mb-6 flex items-center gap-2">
                  <Settings size={18} className="text-blue-700" />
                  System Security & Configuration Panel
                </h3>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                  
                  {/* Security Configurations */}
                  <div className="flex flex-col gap-4">
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Security Credentials</span>
                    
                    <div className="p-4 rounded-xl border border-slate-100 flex justify-between items-center text-xs">
                      <div>
                        <strong className="font-bold text-slate-800 block">OTP Bypass Mode (Testing only)</strong>
                        <span className="text-slate-400">Allows instant login bypassing the simulated SMS/Email gateway.</span>
                      </div>
                      <input 
                        type="checkbox" 
                        checked={otpBypass}
                        onChange={() => {
                          setOtpBypass(!otpBypass);
                          showToast('info', `OTP Bypass Mode has been ${!otpBypass ? 'ENABLED' : 'DISABLED'}.`, "Security Config Updated");
                        }}
                        className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500 cursor-pointer"
                      />
                    </div>

                    <div className="p-4 rounded-xl border border-slate-100 flex justify-between items-center text-xs">
                      <div>
                        <strong className="font-bold text-slate-800 block">System Maintenance Mode</strong>
                        <span className="text-slate-400">Sets the platform to read-only mode for all roles except System Admin.</span>
                      </div>
                      <input 
                        type="checkbox" 
                        checked={maintenanceMode}
                        onChange={() => {
                          setMaintenanceMode(!maintenanceMode);
                          showToast('info', `System Maintenance Mode has been ${!maintenanceMode ? 'ENABLED' : 'DISABLED'}.`, "System Maintenance");
                        }}
                        className="h-4 w-4 rounded text-blue-600 focus:ring-blue-500 cursor-pointer"
                      />
                    </div>
                  </div>

                  {/* System Parameters */}
                  <div className="flex flex-col gap-4">
                    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Database & Services Status</span>
                    
                    <div className="p-4 rounded-xl border border-slate-100 flex justify-between items-center text-xs">
                      <div>
                        <strong className="font-bold text-slate-800 block">Simulated Telecom Latency</strong>
                        <span className="text-slate-400">Adjust the delay of the mock SMS/Email dispatcher.</span>
                      </div>
                      <select
                        value={systemLatency}
                        onChange={(e) => {
                          setSystemLatency(Number(e.target.value));
                          showToast('success', "Simulated gateway latency adjusted.", "Latency Synced");
                        }}
                        className="bg-slate-50 border border-slate-200 rounded-lg p-1.5 focus:outline-none cursor-pointer"
                      >
                        <option value={14}>Low (14ms)</option>
                        <option value={150}>Standard (150ms)</option>
                        <option value={1200}>Simulated Lag (1200ms)</option>
                      </select>
                    </div>

                    <div className="p-4 rounded-xl border border-slate-100 flex justify-between items-center text-xs">
                      <div>
                        <strong className="font-bold text-slate-800 block">AES-256 Key Rotation</strong>
                        <span className="text-slate-400">Force rotate simulated session encryption keys.</span>
                      </div>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          showToast('success', "AES-256 encryption keys rotated successfully. Session logs flushed.", "Keys Rotated");
                        }}
                      >
                        Rotate Keys
                      </Button>
                    </div>
                  </div>

                </div>
              </Card>

            </div>
          )}

        </div>

      </div>

    </div>
  );
};
