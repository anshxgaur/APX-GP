import React, { useEffect, useState, useRef } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { 
  Shield, 
  Users, 
  Truck, 
  AlertTriangle, 
  Activity, 
  CheckCircle, 
  TrendingUp, 
  Map, 
  ArrowRight, 
  Heart, 
  Sparkles, 
  Clock, 
  MessageSquare,
  HelpCircle,
  Award,
  Globe
} from 'lucide-react';
import { Button, Card, Logo } from '../components/UI';
import { usePlatform } from '../context/PlatformContext';

// ==========================================
// SCROLL REVEAL COMPONENT
// ==========================================
const ScrollReveal: React.FC<{ children: React.ReactNode; delay?: number }> = ({ children, delay = 0 }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 30 }}
      whileInView={{ opacity: 1, y: 0 }}
      viewport={{ once: true, margin: '-100px' }}
      transition={{ duration: 0.6, delay }}
    >
      {children}
    </motion.div>
  );
};

// ==========================================
// PREMIUM 3D EARTH GLOBE SECTION
// ==========================================
const EarthGlobeSection: React.FC = () => {
  const [isVisible, setIsVisible] = useState(false);
  const sectionRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          setIsVisible(true);
          observer.unobserve(entry.target);
        }
      },
      { threshold: 0.1 }
    );

    if (sectionRef.current) {
      observer.observe(sectionRef.current);
    }

    return () => {
      observer.disconnect();
    };
  }, []);

  return (
    <section 
      ref={sectionRef}
      className="relative w-full overflow-hidden flex flex-col items-center justify-center py-16"
      style={{
        height: '520px',
        background: 'linear-gradient(180deg, #ffffff 0%, #EFF6FF 20%, #0B1120 100%)'
      }}
    >
      {/* 3 blurred floating gradient orbs behind globe for ambient depth */}
      <div className="absolute top-1/4 left-1/3 w-80 h-80 rounded-full bg-blue-500/10 blur-[100px] pointer-events-none" />
      <div className="absolute bottom-1/4 right-1/3 w-80 h-80 rounded-full bg-emerald-500/10 blur-[100px] pointer-events-none" />
      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-96 h-96 rounded-full bg-indigo-500/10 blur-[120px] pointer-events-none" />

      {/* Section Label (Above Globe) */}
      <div className="absolute top-8 z-20 flex flex-col items-center gap-2">
        <div className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-blue-500/10 border border-blue-400/20 backdrop-blur-md text-xs font-black text-blue-400 uppercase tracking-widest animate-pulse">
          🌍 Live Global Operations Network
        </div>
      </div>

      {/* Main Globe Area */}
      <div 
        className="relative flex items-center justify-center transition-all duration-1000 ease-out"
        style={{
          transform: isVisible ? 'scale(1)' : 'scale(0.6)',
          opacity: isVisible ? 1 : 0,
          width: '380px',
          height: '380px',
        }}
      >
        {/* GLOBE WRAPPER (380px diameter circle with clip-path) */}
        <div 
          className="relative w-[380px] h-[380px] rounded-full overflow-hidden shadow-2xl border border-blue-500/20"
          style={{
            background: 'radial-gradient(circle at 30% 30%, #1D4ED8 0%, #1E3A8A 50%, #0B132B 100%)',
            boxShadow: 'inset 20px 20px 50px rgba(255,255,255,0.25), inset -20px -20px 50px rgba(0,0,0,0.85), 0 0 40px rgba(59,130,246,0.4), 0 0 80px rgba(59,130,246,0.15)'
          }}
        >
          {/* Ocean depth overlay with mix-blend-mode: screen */}
          <div 
            className="absolute inset-0 pointer-events-none"
            style={{
              background: 'radial-gradient(circle at 40% 40%, rgba(255,255,255,0.15) 0%, rgba(0,0,0,0) 80%)',
              mixBlendMode: 'screen'
            }}
          />

          {/* ROTATING CONTINENTS LAYER */}
          <div className="absolute inset-0 pointer-events-none globeRotator">
            <svg width="800" height="380" viewBox="0 0 800 240" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-full w-[800px]">
              {/* Cycle 1 (0 to 400) */}
              {/* North America */}
              <path d="M 30,50 Q 50,40 80,45 T 120,60 T 140,90 T 110,120 T 70,100 T 40,70 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 32,52 Q 52,42 82,47 T 122,62 T 142,92 T 112,122 T 72,102 T 42,72 Z" fill="#34D399" fillOpacity="0.4" />
              
              {/* South America */}
              <path d="M 100,120 Q 120,130 115,160 T 95,200 T 80,140 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 102,122 Q 122,132 117,162 T 97,202 T 82,142 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Europe/Asia */}
              <path d="M 180,45 Q 220,35 280,40 T 350,55 T 390,90 T 340,110 T 260,90 T 190,70 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 182,47 Q 222,37 282,42 T 352,57 T 392,92 T 342,112 T 262,92 T 192,72 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Africa */}
              <path d="M 185,95 Q 220,105 230,130 T 205,175 T 170,135 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 187,97 Q 222,107 232,132 T 207,177 T 172,137 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Australia */}
              <path d="M 340,135 Q 370,145 360,165 T 320,155 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 342,137 Q 372,147 362,167 T 322,157 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Antarctica */}
              <path d="M 20,225 L 380,225 L 380,235 L 20,235 Z" fill="#10B981" fillOpacity="0.85" />

              {/* Cycle 2 (400 to 800) */}
              {/* North America */}
              <path d="M 430,50 Q 450,40 480,45 T 520,60 T 540,90 T 510,120 T 470,100 T 440,70 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 432,52 Q 452,42 482,47 T 522,62 T 542,92 T 512,122 T 472,102 T 442,72 Z" fill="#34D399" fillOpacity="0.4" />
              
              {/* South America */}
              <path d="M 500,120 Q 520,130 515,160 T 495,200 T 480,140 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 502,122 Q 522,132 517,162 T 497,202 T 482,142 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Europe/Asia */}
              <path d="M 580,45 Q 620,35 680,40 T 750,55 T 790,90 T 740,110 T 660,90 T 590,70 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 582,47 Q 622,37 682,42 T 752,57 T 792,92 T 742,112 T 662,92 T 592,72 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Africa */}
              <path d="M 585,95 Q 620,105 630,130 T 605,175 T 570,135 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 587,97 Q 622,107 632,132 T 607,177 T 572,137 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Australia */}
              <path d="M 740,135 Q 770,145 760,165 T 720,155 Z" fill="#10B981" fillOpacity="0.85" />
              <path d="M 742,137 Q 772,147 762,167 T 722,157 Z" fill="#34D399" fillOpacity="0.4" />

              {/* Antarctica */}
              <path d="M 420,225 L 780,225 L 780,235 L 420,235 Z" fill="#10B981" fillOpacity="0.85" />

              {/* CONNECTING DASHED SVG NETWORK LINES (Inside Rotator) */}
              <g strokeWidth="1.5" fill="none">
                {/* Cycle 1 Connections */}
                <path d="M 80,70 Q 135,45 190,50" stroke="#3B82F6" className="dashLine" />
                <path d="M 190,50 Q 200,75 210,100" stroke="#0D9488" className="dashLine" />
                <path d="M 210,100 Q 215,117 205,135" stroke="#F97316" className="dashLine" />
                <path d="M 80,70 Q 95,110 110,150" stroke="#F97316" className="dashLine" />
                <path d="M 210,100 Q 245,95 280,90" stroke="#0D9488" className="dashLine" />
                <path d="M 280,90 Q 305,82 330,75" stroke="#EF4444" className="dashLine" />
                <path d="M 330,75 Q 350,112 340,150" stroke="#10B981" className="dashLine" />

                {/* Cycle 2 Connections */}
                <path d="M 480,70 Q 535,45 590,50" stroke="#3B82F6" className="dashLine" />
                <path d="M 590,50 Q 600,75 610,100" stroke="#0D9488" className="dashLine" />
                <path d="M 610,100 Q 615,117 605,135" stroke="#F97316" className="dashLine" />
                <path d="M 480,70 Q 495,110 510,150" stroke="#F97316" className="dashLine" />
                <path d="M 610,100 Q 645,95 680,90" stroke="#0D9488" className="dashLine" />
                <path d="M 680,90 Q 705,82 730,75" stroke="#EF4444" className="dashLine" />
                <path d="M 730,75 Q 750,112 740,150" stroke="#10B981" className="dashLine" />
              </g>

              {/* GLOWING PULSE NODES (Inside Rotator for seamless movement) */}
              {/* Cycle 1 Nodes */}
              <g>
                <circle cx="80" cy="70" r="4.5" fill="#3B82F6" />
                <circle cx="80" cy="70" r="4.5" fill="none" stroke="#3B82F6" className="svgPulseCircle" />

                <circle cx="190" cy="50" r="4.5" fill="#0D9488" />
                <circle cx="190" cy="50" r="4.5" fill="none" stroke="#0D9488" className="svgPulseCircle" />

                <circle cx="210" cy="100" r="4.5" fill="#F97316" />
                <circle cx="210" cy="100" r="4.5" fill="none" stroke="#F97316" className="svgPulseCircle" />

                <circle cx="340" cy="150" r="4.5" fill="#10B981" />
                <circle cx="340" cy="150" r="4.5" fill="none" stroke="#10B981" className="svgPulseCircle" />

                <circle cx="330" cy="75" r="4.5" fill="#EF4444" />
                <circle cx="330" cy="75" r="4.5" fill="none" stroke="#EF4444" className="svgPulseCircle" />

                <circle cx="110" cy="150" r="4.5" fill="#F97316" />
                <circle cx="110" cy="150" r="4.5" fill="none" stroke="#F97316" className="svgPulseCircle" />

                <circle cx="280" cy="90" r="4.5" fill="#0D9488" />
                <circle cx="280" cy="90" r="4.5" fill="none" stroke="#0D9488" className="svgPulseCircle" />

                <circle cx="205" cy="135" r="4.5" fill="#3B82F6" />
                <circle cx="205" cy="135" r="4.5" fill="none" stroke="#3B82F6" className="svgPulseCircle" />
              </g>

              {/* Cycle 2 Nodes */}
              <g>
                <circle cx="480" cy="70" r="4.5" fill="#3B82F6" />
                <circle cx="480" cy="70" r="4.5" fill="none" stroke="#3B82F6" className="svgPulseCircle" />

                <circle cx="590" cy="50" r="4.5" fill="#0D9488" />
                <circle cx="590" cy="50" r="4.5" fill="none" stroke="#0D9488" className="svgPulseCircle" />

                <circle cx="610" cy="100" r="4.5" fill="#F97316" />
                <circle cx="610" cy="100" r="4.5" fill="none" stroke="#F97316" className="svgPulseCircle" />

                <circle cx="740" cy="150" r="4.5" fill="#10B981" />
                <circle cx="740" cy="150" r="4.5" fill="none" stroke="#10B981" className="svgPulseCircle" />

                <circle cx="730" cy="75" r="4.5" fill="#EF4444" />
                <circle cx="730" cy="75" r="4.5" fill="none" stroke="#EF4444" className="svgPulseCircle" />

                <circle cx="510" cy="150" r="4.5" fill="#F97316" />
                <circle cx="510" cy="150" r="4.5" fill="none" stroke="#F97316" className="svgPulseCircle" />

                <circle cx="680" cy="90" r="4.5" fill="#0D9488" />
                <circle cx="680" cy="90" r="4.5" fill="none" stroke="#0D9488" className="svgPulseCircle" />

                <circle cx="605" cy="135" r="4.5" fill="#3B82F6" />
                <circle cx="605" cy="135" r="4.5" fill="none" stroke="#3B82F6" className="svgPulseCircle" />
              </g>
            </svg>
          </div>

          {/* ROTATING CLOUDS LAYER (Blurred, semi-transparent) */}
          <div className="absolute inset-0 pointer-events-none cloudRotator opacity-35 filter blur-[1px]">
            <svg width="800" height="380" viewBox="0 0 800 240" fill="none" xmlns="http://www.w3.org/2000/svg" className="h-full w-[800px]">
              {/* Cycle 1 Clouds */}
              <path d="M 50,60 Q 80,40 120,70 T 200,60 T 260,90 T 150,110 Z" fill="#FFFFFF" />
              <path d="M 280,100 Q 310,80 340,110 T 390,130 Z" fill="#FFFFFF" />
              <path d="M 100,160 Q 140,150 180,180 T 120,200 Z" fill="#FFFFFF" />

              {/* Cycle 2 Clouds */}
              <path d="M 450,60 Q 480,40 520,70 T 600,60 T 660,90 T 550,110 Z" fill="#FFFFFF" />
              <path d="M 680,100 Q 710,80 740,110 T 790,130 Z" fill="#FFFFFF" />
              <path d="M 500,160 Q 540,150 580,180 T 520,200 Z" fill="#FFFFFF" />
            </svg>
          </div>

          {/* LATITUDE + LONGITUDE GRID LINES (Static Layer on top for 3D effect) */}
          <svg className="absolute inset-0 w-full h-full pointer-events-none opacity-20" viewBox="0 0 380 380">
            {/* Latitude Ellipses */}
            <ellipse cx="190" cy="190" rx="190" ry="150" fill="none" stroke="#FFFFFF" strokeWidth="1" />
            <ellipse cx="190" cy="190" rx="190" ry="90" fill="none" stroke="#FFFFFF" strokeWidth="1" />
            <ellipse cx="190" cy="190" rx="190" ry="30" fill="none" stroke="#FFFFFF" strokeWidth="1" />
            
            {/* Longitude Ellipses */}
            <ellipse cx="190" cy="190" rx="150" ry="190" fill="none" stroke="#FFFFFF" strokeWidth="1" />
            <ellipse cx="190" cy="190" rx="90" ry="190" fill="none" stroke="#FFFFFF" strokeWidth="1" />
            <ellipse cx="190" cy="190" rx="30" ry="190" fill="none" stroke="#FFFFFF" strokeWidth="1" />

            {/* Equator & Prime Meridian Grid Lines */}
            <line x1="0" y1="190" x2="380" y2="190" stroke="#FFFFFF" strokeWidth="1.5" />
            <line x1="190" y1="0" x2="190" y2="380" stroke="#FFFFFF" strokeWidth="1.5" />
          </svg>

          {/* FIXED SPECULAR HIGHLIGHT */}
          <div 
            className="absolute inset-0 pointer-events-none"
            style={{
              background: 'radial-gradient(circle at 30% 30%, rgba(255,255,255,0.45) 0%, rgba(255,255,255,0) 55%)'
            }}
          />

          {/* FIXED DARK SHADOW */}
          <div 
            className="absolute inset-0 pointer-events-none"
            style={{
              background: 'radial-gradient(circle at 75% 75%, rgba(0,0,0,0) 25%, rgba(0,0,0,0.85) 85%)'
            }}
          />
        </div>

        {/* ==========================================
            FLOATING NGO INFO CARDS (4 cards around globe)
            ========================================== */}
        {/* Card 1: AI Triage (Top-Left) */}
        <div 
          className="absolute -top-6 -left-28 sm:-left-36 z-20 animate-float-1 transition-all duration-1000 delay-100"
          style={{
            transform: isVisible ? 'translateY(0) scale(1)' : 'translateY(40px) scale(0.8)',
            opacity: isVisible ? 1 : 0
          }}
        >
          <div className="bg-white/70 backdrop-blur-md border border-white/40 rounded-xl p-3 shadow-lg flex items-center gap-3 max-w-[210px] text-left">
            <div className="h-2 w-2 rounded-full bg-red-500 animate-ping flex-shrink-0" />
            <div className="flex flex-col">
              <span className="text-[9px] font-black text-red-600 tracking-wider uppercase">AI Triage</span>
              <span className="text-[11px] font-black text-slate-800 leading-tight">Critical Priority Incident</span>
            </div>
          </div>
        </div>

        {/* Card 2: Volunteers (Top-Right) */}
        <div 
          className="absolute -top-2 -right-28 sm:-right-36 z-20 animate-float-2 transition-all duration-1000 delay-300"
          style={{
            transform: isVisible ? 'translateY(0) scale(1)' : 'translateY(40px) scale(0.8)',
            opacity: isVisible ? 1 : 0
          }}
        >
          <div className="bg-white/70 backdrop-blur-md border border-white/40 rounded-xl p-3 shadow-lg flex items-center gap-3 max-w-[210px] text-left">
            <div className="h-2 w-2 rounded-full bg-emerald-500 animate-pulse flex-shrink-0" />
            <div className="flex flex-col">
              <span className="text-[9px] font-black text-emerald-600 tracking-wider uppercase">Volunteers</span>
              <span className="text-[11px] font-black text-slate-800 leading-tight">12 Active Nearby</span>
            </div>
          </div>
        </div>

        {/* Card 3: Dispatch Queue (Bottom-Left) */}
        <div 
          className="absolute -bottom-4 -left-28 sm:-left-36 z-20 animate-float-3 transition-all duration-1000 delay-500"
          style={{
            transform: isVisible ? 'translateY(0) scale(1)' : 'translateY(40px) scale(0.8)',
            opacity: isVisible ? 1 : 0
          }}
        >
          <div className="bg-white/70 backdrop-blur-md border border-white/40 rounded-xl p-3 shadow-lg flex items-center gap-3 max-w-[220px] text-left">
            <div className="h-2 w-2 rounded-full bg-blue-500 animate-pulse flex-shrink-0" />
            <div className="flex flex-col">
              <span className="text-[9px] font-black text-blue-600 tracking-wider uppercase">Dispatch Queue</span>
              <span className="text-[11px] font-black text-slate-800 leading-tight">Water Truck Dispatched</span>
            </div>
          </div>
        </div>

        {/* Card 4: Resources (Bottom-Right) */}
        <div 
          className="absolute -bottom-6 -right-28 sm:-right-36 z-20 animate-float-4 transition-all duration-1000 delay-700"
          style={{
            transform: isVisible ? 'translateY(0) scale(1)' : 'translateY(40px) scale(0.8)',
            opacity: isVisible ? 1 : 0
          }}
        >
          <div className="bg-white/70 backdrop-blur-md border border-white/40 rounded-xl p-3 shadow-lg flex items-center gap-3 max-w-[210px] text-left">
            <div className="h-2 w-2 rounded-full bg-teal-500 animate-pulse flex-shrink-0" />
            <div className="flex flex-col">
              <span className="text-[9px] font-black text-teal-600 tracking-wider uppercase">Resources</span>
              <span className="text-[11px] font-black text-slate-800 leading-tight">367 Delivered Today</span>
            </div>
          </div>
        </div>

      </div>

      {/* Below Globe Label */}
      <div className="absolute bottom-6 z-20 text-center">
        <p className="text-xs font-bold text-slate-400 tracking-wide">
          Monitoring crisis response across <span className="text-blue-400 font-black">120+ cities</span> in real time
        </p>
      </div>

    </section>
  );
};

// ==========================================
// VIEWPORT COUNT-UP COMPONENT
// ==========================================
const CountUpStat: React.FC<{ value: number; suffix?: string; prefix?: string; duration?: number; blinkPlus?: boolean }> = ({ 
  value, 
  suffix = '', 
  prefix = '', 
  duration = 2,
  blinkPlus = false
}) => {
  const [count, setCount] = useState(0);
  const [hasAnimated, setHasAnimated] = useState(false);
  const [showBlink, setShowBlink] = useState(false);
  const ref = useRef<HTMLSpanElement>(null);

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        if (entries[0].isIntersecting && !hasAnimated) {
          setHasAnimated(true);
          const end = value;
          if (end <= 0) {
            setCount(0);
            return;
          }
          let start = 0;
          const totalMs = duration * 1000;
          const stepTime = Math.max(Math.floor(totalMs / end), 16); // cap at ~60fps
          
          const timer = setInterval(() => {
            const increment = Math.ceil(end / (totalMs / stepTime));
            start += isNaN(increment) ? 1 : increment;
            if (start >= end) {
              clearInterval(timer);
              setCount(end);
              if (blinkPlus) {
                setShowBlink(true);
              }
            } else {
              setCount(start);
            }
          }, stepTime);
        }
      },
      { threshold: 0.1 }
    );

    if (ref.current) {
      observer.observe(ref.current);
    }

    return () => observer.disconnect();
  }, [value, duration, hasAnimated, blinkPlus]);

  return (
    <span ref={ref} className="font-mono">
      {prefix}
      {count.toLocaleString()}
      {suffix}
      {blinkPlus && showBlink && (
        <span className="animate-pulse text-teal-400 ml-0.5">+</span>
      )}
    </span>
  );
};

interface LandingPageProps {
  onNavigateToAuth: (mode?: 'login' | 'volunteer') => void;
}

export const LandingPage: React.FC<LandingPageProps> = ({ onNavigateToAuth }) => {
  const { stats, registerVolunteer } = usePlatform();
  const [isVolModalOpen, setIsVolModalOpen] = useState(false);
  const [volName, setVolName] = useState('');
  const [volSpecialty, setVolSpecialty] = useState<'Rescue' | 'Medical' | 'Food' | 'Water' | 'Shelter' | 'Power' | 'General'>('General');
  const [volPhone, setVolPhone] = useState('');
  const [volLocation, setVolLocation] = useState('');
  
  // Navbar scroll blur effect state
  const [isScrolled, setIsScrolled] = useState(false);

  useEffect(() => {
    const handleScroll = () => {
      if (window.scrollY > 20) {
        setIsScrolled(true);
      } else {
        setIsScrolled(false);
      }
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleVolSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!volName || !volPhone || !volLocation) return;
    registerVolunteer({
      name: volName,
      specialty: volSpecialty,
      phone: volPhone,
      location: volLocation
    });
    setIsVolModalOpen(false);
    setVolName('');
    setVolPhone('');
    setVolLocation('');
  };

  // Headline words for stagger animation
  const headlineWords = "Connecting Communities Through".split(" ");

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 flex flex-col overflow-x-hidden font-sans selection:bg-teal-200 selection:text-teal-900">
      
      {/* ==========================================
          DYNAMIC KEYFRAME INJECTIONS (60FPS Animations)
          ========================================== */}
      <style>{`
        /* Slow slow logo rotation */
        @keyframes slow-logo-spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        .animate-logo-slow {
          animation: slow-logo-spin 8s linear infinite;
        }

        /* Ambient floating blurred background circles */
        @keyframes float-ambient-1 {
          0% { transform: translate(0px, 0px) scale(1); }
          50% { transform: translate(40px, -60px) scale(1.15); }
          100% { transform: translate(0px, 0px) scale(1); }
        }
        @keyframes float-ambient-2 {
          0% { transform: translate(0px, 0px) scale(1.1); }
          50% { transform: translate(-50px, 40px) scale(0.9); }
          100% { transform: translate(0px, 0px) scale(1.1); }
        }
        @keyframes float-ambient-3 {
          0% { transform: translate(0px, 0px) scale(1); }
          50% { transform: translate(30px, 30px) scale(1.1); }
          100% { transform: translate(0px, 0px) scale(1); }
        }
        .animate-ambient-1 { animation: float-ambient-1 12s ease-in-out infinite; }
        .animate-ambient-2 { animation: float-ambient-2 15s ease-in-out infinite; }
        .animate-ambient-3 { animation: float-ambient-3 18s ease-in-out infinite; }

        /* Shimmer animation on teal border */
        @keyframes border-shimmer {
          0% { border-color: rgba(13, 148, 136, 0.2); box-shadow: 0 0 0 0px rgba(13, 148, 136, 0); }
          50% { border-color: rgba(13, 148, 136, 0.6); box-shadow: 0 0 8px 1px rgba(13, 148, 136, 0.15); }
          100% { border-color: rgba(13, 148, 136, 0.2); box-shadow: 0 0 0 0px rgba(13, 148, 136, 0); }
        }
        .animate-shimmer-pill {
          animation: border-shimmer 3s ease-in-out infinite;
        }

        /* Animated blue to teal text loop */
        @keyframes gradient-text-loop {
          0% { background-position: 0% 50%; }
          50% { background-position: 100% 50%; }
          100% { background-position: 0% 50%; }
        }
        .animate-gradient-text {
          background-size: 200% auto;
          animation: gradient-text-loop 4s ease infinite;
        }

        /* Infinite rotating wireframe globe */
        @keyframes globe-spin {
          0% { transform: rotate(0deg); }
          100% { transform: rotate(360deg); }
        }
        .animate-globe-spin {
          animation: globe-spin 25s linear infinite;
        }

        /* Staggered node pulses */
        @keyframes node-pulse-1 {
          0%, 100% { transform: scale(1); opacity: 1; }
          50% { transform: scale(1.6); opacity: 0.4; }
        }
        @keyframes node-pulse-2 {
          0%, 100% { transform: scale(1.6); opacity: 0.4; }
          50% { transform: scale(1); opacity: 1; }
        }
        .node-p-1 { animation: node-pulse-1 2s ease-in-out infinite; transform-origin: center; }
        .node-p-2 { animation: node-pulse-2 2s ease-in-out infinite; transform-origin: center; }

        /* Dashed data flow lines */
        @keyframes dash-flow {
          to {
            stroke-dashoffset: -20;
          }
        }
        .animate-dash-flow {
          stroke-dasharray: 6, 4;
          animation: dash-flow 1.5s linear infinite;
        }

        /* Floating cards absolute coordinates */
        @keyframes card-float-y {
          0%, 100% { transform: translateY(-8px); }
          50% { transform: translateY(8px); }
        }
        .animate-card-float {
          animation: card-float-y 5s ease-in-out infinite;
        }
        .animate-card-float-delayed {
          animation: card-float-y 5s ease-in-out infinite;
          animation-delay: 2.5s;
        }

        /* Sonar pulse rings for critical alerts */
        @keyframes sonar-ring {
          0% { transform: scale(0.8); opacity: 0.8; }
          100% { transform: scale(2.2); opacity: 0; }
        }
        .animate-sonar {
          animation: sonar-ring 1.8s cubic-bezier(0.16, 1, 0.3, 1) infinite;
        }

        /* Left-to-right draw for stats underline */
        @keyframes draw-underline {
          0% { width: 0%; }
          100% { width: 100%; }
        }
        .reveal-underline {
          animation: draw-underline 1.2s cubic-bezier(0.16, 1, 0.3, 1) forwards;
        }
      `}</style>

      {/* ==========================================
          HEADER / NAVIGATION
          ========================================== */}
      <header className={`sticky top-0 z-40 transition-all duration-300 ${
        isScrolled 
          ? 'bg-white/95 backdrop-blur-lg shadow-sm border-b border-slate-100 py-3' 
          : 'bg-transparent py-5'
      }`}>
        <div className="max-w-7xl mx-auto px-6 flex items-center justify-between">
          
          {/* Logo Brand */}
          <div className="flex items-center gap-2.5 cursor-pointer" onClick={() => onNavigateToAuth('login')}>
            <div className="animate-logo-slow">
              <Logo size={36} />
            </div>
            <div>
              <span className="text-lg font-black tracking-tight bg-gradient-to-r from-blue-800 to-teal-700 bg-clip-text text-transparent">
                SevaAi
              </span>
              <span className="hidden sm:inline-block ml-1.5 px-1.5 py-0.5 rounded-sm bg-blue-50 text-[10px] font-bold text-blue-700 tracking-wider uppercase">
                NGO Portal
              </span>
            </div>
          </div>

          {/* Nav Links with Underline Animate Left-Right */}
          <nav className="hidden md:flex items-center gap-8 text-sm font-semibold text-slate-600">
            {['Features', 'How-It-Works', 'Impact', 'Testimonials'].map((link) => (
              <a 
                key={link}
                href={`#${link.toLowerCase()}`} 
                className="relative py-1 hover:text-teal-600 transition-colors group"
              >
                {link.replace('-', ' ')}
                <span className="absolute bottom-0 left-0 w-0 h-0.5 bg-teal-500 transition-all duration-300 group-hover:w-full" />
              </a>
            ))}
          </nav>

          {/* CTA Actions */}
          <div className="flex items-center gap-3">
            <button
              onClick={() => onNavigateToAuth('login')}
              className="text-sm font-bold text-slate-600 hover:text-teal-600 px-3 py-2 cursor-pointer transition-colors"
            >
              Sign In
            </button>
            <div className="relative group p-[1px] rounded-lg bg-gradient-to-r from-blue-600 to-teal-500 hover:from-blue-700 hover:to-teal-600 transition-all duration-300">
              <Button
                variant="primary"
                size="sm"
                onClick={() => onNavigateToAuth('login')}
                icon={<ArrowRight size={14} />}
                className="bg-slate-900 text-white hover:bg-transparent transition-all border-none"
              >
                Access Dashboard
              </Button>
            </div>
          </div>

        </div>
      </header>

      {/* ==========================================
          HERO SECTION
          ========================================== */}
      <section className="relative pt-10 pb-20 md:py-28 bg-white overflow-hidden">
        
        {/* Ambient Floating Blurred Circles */}
        <div className="absolute top-[10%] left-[5%] w-56 h-56 rounded-full bg-blue-200/30 blur-3xl pointer-events-none animate-ambient-1" />
        <div className="absolute bottom-[15%] right-[5%] w-60 h-60 rounded-full bg-teal-200/20 blur-3xl pointer-events-none animate-ambient-2" />
        <div className="absolute top-[40%] left-[45%] w-48 h-48 rounded-full bg-emerald-100/30 blur-3xl pointer-events-none animate-ambient-3" />

        <div className="max-w-7xl mx-auto px-6 grid grid-cols-1 lg:grid-cols-12 gap-12 items-center">
          
          {/* Hero Left Content */}
          <div className="lg:col-span-6 flex flex-col gap-6 text-left relative z-10">
            
            {/* Top Pill Shimmer */}
            <motion.div
              initial={{ opacity: 0, scale: 0.95 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.6 }}
              className="inline-flex items-center gap-2 px-3.5 py-1.5 rounded-full bg-teal-50/50 border border-teal-100/50 self-start text-xs font-bold text-teal-800 animate-shimmer-pill"
            >
              <Sparkles size={14} className="text-teal-600 animate-pulse" />
              <span>AI-Powered Crisis Coordination Platform</span>
            </motion.div>

            {/* Headline Words Stagger Upward */}
            <h1 className="text-4xl sm:text-5xl lg:text-6xl font-black text-slate-900 leading-[1.1] tracking-tight">
              <span className="block overflow-hidden py-1">
                {headlineWords.map((word, idx) => (
                  <motion.span
                    key={idx}
                    initial={{ y: "100%" }}
                    animate={{ y: 0 }}
                    transition={{ duration: 0.6, delay: idx * 0.08, ease: [0.16, 1, 0.3, 1] }}
                    className="inline-block mr-3"
                  >
                    {word}
                  </motion.span>
                ))}
              </span>
              <span className="block overflow-hidden py-1">
                <motion.span
                  initial={{ y: "100%" }}
                  animate={{ y: 0 }}
                  transition={{ duration: 0.8, delay: 0.4, ease: [0.16, 1, 0.3, 1] }}
                  className="bg-gradient-to-r from-blue-700 via-teal-600 to-blue-600 bg-clip-text text-transparent animate-gradient-text"
                >
                  Intelligent Resource
                </motion.span>
              </span>
              <span className="block overflow-hidden py-1">
                <motion.span
                  initial={{ y: "100%" }}
                  animate={{ y: 0 }}
                  transition={{ duration: 0.8, delay: 0.5, ease: [0.16, 1, 0.3, 1] }}
                >
                  Coordination
                </motion.span>
              </span>
            </h1>

            {/* Subtitle Fade In */}
            <motion.p
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ duration: 1, delay: 0.8 }}
              className="text-base sm:text-lg text-slate-600 max-w-2xl leading-relaxed"
            >
              SevaAi is an enterprise-grade crisis response system. We leverage intelligent NLP triage algorithms to prioritize emergency needs, coordinate local volunteers, and track critical resources in real-time.
            </motion.p>

            {/* CTA Buttons with Spring Animations */}
            <motion.div
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ type: "spring", stiffness: 100, damping: 15, delay: 1 }}
              className="flex flex-col sm:flex-row gap-4 mt-2"
            >
              <Button
                variant="primary"
                size="lg"
                onClick={() => onNavigateToAuth('login')}
                icon={<ArrowRight size={18} />}
                className="hover:scale-103 active:scale-97 transition-transform"
              >
                Get Started
              </Button>
              <Button
                variant="outline"
                size="lg"
                onClick={() => setIsVolModalOpen(true)}
                icon={<Heart size={18} className="text-red-500 fill-red-500" />}
                className="hover:scale-103 active:scale-97 transition-transform"
              >
                Join as Volunteer
              </Button>
            </motion.div>

            {/* Trust Badges */}
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 1.2 }}
              className="grid grid-cols-3 gap-4 border-t border-slate-100 pt-8 mt-4 text-slate-500"
            >
              <div className="flex flex-col">
                <span className="text-xl font-black text-slate-900">99.9%</span>
                <span className="text-xs">Uptime SLA</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xl font-black text-slate-900">&lt; 30s</span>
                <span className="text-xs">AI Priority Triage</span>
              </div>
              <div className="flex flex-col">
                <span className="text-xl font-black text-slate-900">100%</span>
                <span className="text-xs">Secure OTP Auth</span>
              </div>
            </motion.div>
          </div>

          {/* Hero Right Visual: Animated SVG Network Globe & Floating Cards */}
          <div className="lg:col-span-6 relative flex justify-center items-center h-[520px]">
            
            {/* Animated Wireframe Globe Container */}
            <div className="relative w-[480px] h-[480px] flex items-center justify-center">
              
              {/* Globe SVG */}
              <svg 
                width="480" 
                height="480" 
                viewBox="0 0 480 480" 
                fill="none" 
                xmlns="http://www.w3.org/2000/svg"
                className="animate-globe-spin"
              >
                {/* Thin Blue Grid Lines (Globe Latitude/Longitude Lines) */}
                <circle cx="240" cy="240" r="200" stroke="rgba(29,78,216,0.15)" strokeWidth="1" />
                <circle cx="240" cy="240" r="150" stroke="rgba(29,78,216,0.12)" strokeWidth="1" />
                <circle cx="240" cy="240" r="100" stroke="rgba(29,78,216,0.1)" strokeWidth="1" />
                
                {/* Longitudinal Ellipses */}
                <ellipse cx="240" cy="240" rx="200" ry="80" stroke="rgba(29,78,216,0.12)" strokeWidth="1" />
                <ellipse cx="240" cy="240" rx="80" ry="200" stroke="rgba(29,78,216,0.12)" strokeWidth="1" />
                <ellipse cx="240" cy="240" rx="200" ry="140" stroke="rgba(29,78,216,0.08)" strokeWidth="1" />
                <ellipse cx="240" cy="240" rx="140" ry="200" stroke="rgba(29,78,216,0.08)" strokeWidth="1" />

                {/* Animated Dashed Connection Lines (Data Flow) */}
                <path d="M 120 120 L 360 360" stroke="rgba(13,148,136,0.4)" strokeWidth="1.5" className="animate-dash-flow" />
                <path d="M 360 120 L 120 360" stroke="rgba(29,78,216,0.4)" strokeWidth="1.5" className="animate-dash-flow" />
                <path d="M 240 40 L 240 440" stroke="rgba(16,185,129,0.3)" strokeWidth="1.5" className="animate-dash-flow" />
                <path d="M 40 240 L 440 240" stroke="rgba(249,115,22,0.3)" strokeWidth="1.5" className="animate-dash-flow" />

                {/* 12 Glowing Nodes (Staggered Pulsing Circles) */}
                {/* Node 1: Blue */}
                <g transform="translate(120, 120)">
                  <circle cx="0" cy="0" r="10" fill="#1D4ED8" fillOpacity="0.8" className="node-p-1" />
                  <circle cx="0" cy="0" r="4" fill="#FFFFFF" />
                </g>
                {/* Node 2: Teal */}
                <g transform="translate(360, 120)">
                  <circle cx="0" cy="0" r="10" fill="#0D9488" fillOpacity="0.8" className="node-p-2" />
                  <circle cx="0" cy="0" r="4" fill="#FFFFFF" />
                </g>
                {/* Node 3: Green */}
                <g transform="translate(120, 360)">
                  <circle cx="0" cy="0" r="10" fill="#10B981" fillOpacity="0.8" className="node-p-2" />
                  <circle cx="0" cy="0" r="4" fill="#FFFFFF" />
                </g>
                {/* Node 4: Red */}
                <g transform="translate(360, 360)">
                  <circle cx="0" cy="0" r="10" fill="#EF4444" fillOpacity="0.8" className="node-p-1" />
                  <circle cx="0" cy="0" r="4" fill="#FFFFFF" />
                </g>
                {/* Node 5: Orange */}
                <g transform="translate(240, 40)">
                  <circle cx="0" cy="0" r="8" fill="#F97316" fillOpacity="0.8" className="node-p-1" />
                </g>
                {/* Node 6: Blue */}
                <g transform="translate(240, 440)">
                  <circle cx="0" cy="0" r="8" fill="#1D4ED8" fillOpacity="0.8" className="node-p-2" />
                </g>
                {/* Node 7: Green */}
                <g transform="translate(40, 240)">
                  <circle cx="0" cy="0" r="8" fill="#10B981" fillOpacity="0.8" className="node-p-2" />
                </g>
                {/* Node 8: Teal */}
                <g transform="translate(440, 240)">
                  <circle cx="0" cy="0" r="8" fill="#0D9488" fillOpacity="0.8" className="node-p-1" />
                </g>
                {/* Node 9: Red */}
                <g transform="translate(200, 160)">
                  <circle cx="0" cy="0" r="7" fill="#EF4444" fillOpacity="0.8" className="node-p-1" />
                </g>
                {/* Node 10: Blue */}
                <g transform="translate(280, 320)">
                  <circle cx="0" cy="0" r="7" fill="#1D4ED8" fillOpacity="0.8" className="node-p-2" />
                </g>
                {/* Node 11: Orange */}
                <g transform="translate(160, 280)">
                  <circle cx="0" cy="0" r="7" fill="#F97316" fillOpacity="0.8" className="node-p-2" />
                </g>
                {/* Node 12: Green */}
                <g transform="translate(320, 200)">
                  <circle cx="0" cy="0" r="7" fill="#10B981" fillOpacity="0.8" className="node-p-1" />
                </g>
              </svg>

              {/* FLOATING CARD 1: AI Triage (Top-Left) */}
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.6, delay: 0.2 }}
                whileHover={{ scale: 1.05 }}
                className="absolute top-4 left-4 bg-white rounded-[12px] shadow-lg border border-slate-100 p-3.5 flex items-center gap-3 max-w-[190px] text-left animate-card-float z-10"
              >
                <div className="h-8 w-8 rounded-full bg-red-50 flex items-center justify-center text-red-600 relative flex-shrink-0">
                  <AlertTriangle size={16} />
                  <span className="absolute inset-0 rounded-full bg-red-500 animate-sonar" />
                </div>
                <div className="flex flex-col">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">AI TRIAGE</span>
                  <span className="text-xs font-black text-slate-800">Critical Priority</span>
                </div>
              </motion.div>

              {/* FLOATING CARD 2: Volunteers (Middle-Left) */}
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.6, delay: 0.4 }}
                whileHover={{ scale: 1.05 }}
                className="absolute top-[45%] -left-12 bg-white rounded-[12px] shadow-lg border border-slate-100 p-3.5 flex items-center gap-3 max-w-[190px] text-left animate-card-float-delayed z-10"
              >
                <div className="h-8 w-8 rounded-full bg-teal-50 flex items-center justify-center text-teal-600 flex-shrink-0">
                  <Users size={16} />
                </div>
                <div className="flex flex-col">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">VOLUNTEERS</span>
                  <span className="text-xs font-black text-slate-800">12 Active Nearby</span>
                </div>
              </motion.div>

              {/* FLOATING CARD 3: Dispatch Queue (Bottom-Right) - Slides in from right after 2s, then floats permanently */}
              <motion.div
                initial={{ opacity: 0, x: 100 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 1, delay: 2, ease: [0.16, 1, 0.3, 1] }}
                whileHover={{ scale: 1.05 }}
                className="absolute bottom-4 right-4 bg-slate-950 text-white rounded-[12px] shadow-xl border border-slate-800 p-3.5 flex items-center gap-3 max-w-[220px] text-left animate-card-float z-10"
              >
                <div className="h-8 w-8 rounded-full bg-teal-500/10 flex items-center justify-center text-teal-400 flex-shrink-0">
                  <Truck size={16} />
                </div>
                <div className="flex flex-col">
                  <span className="text-[9px] font-bold text-slate-400 uppercase tracking-wider">DISPATCH QUEUE</span>
                  <span className="text-xs font-black text-white">Water Truck Dispatched</span>
                </div>
              </motion.div>

              {/* FLOATING CARD 4: Resources (Top-Right) */}
              <motion.div
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.6, delay: 0.6 }}
                whileHover={{ scale: 1.05 }}
                className="absolute top-8 right-0 bg-white rounded-[12px] shadow-lg border border-slate-100 p-3.5 flex items-center gap-3 max-w-[210px] text-left animate-card-float-delayed z-10"
              >
                <div className="h-8 w-8 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-600 relative flex-shrink-0">
                  <CheckCircle size={16} />
                  <span className="absolute inset-0 rounded-full bg-emerald-500 animate-sonar" />
                </div>
                <div className="flex flex-col">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">RESOURCES</span>
                  <span className="text-xs font-black text-slate-800">367 Delivered Today</span>
                </div>
              </motion.div>

            </div>

          </div>

        </div>
      </section>

      {/* ==========================================
          PREMIUM 3D EARTH GLOBE VISUAL BRIDGE
          ========================================== */}
      <EarthGlobeSection />

      {/* ==========================================
          STATS ROW / DARK STATS SECTION
          ========================================== */}
      <section id="impact" className="py-20 bg-slate-950 text-white relative overflow-hidden">
        
        {/* Subtle pulsing teal radial gradients in background */}
        <div className="absolute top-1/2 left-1/4 -translate-y-1/2 w-96 h-96 rounded-full bg-teal-500/5 blur-3xl pointer-events-none animate-ambient-1" />
        <div className="absolute top-1/2 right-1/4 -translate-y-1/2 w-96 h-96 rounded-full bg-blue-500/5 blur-3xl pointer-events-none animate-ambient-2" />

        <div className="max-w-7xl mx-auto px-6 relative z-10">
          <div className="grid grid-cols-1 md:grid-cols-4 gap-8 items-stretch">
            
            {/* Stat 1: Active Cases */}
            <ScrollReveal delay={0}>
              <div className="flex flex-col items-center text-center p-6 h-full rounded-2xl bg-slate-900/40 border border-slate-800/60 hover:scale-104 hover:bg-slate-900/60 transition-all duration-300 group">
                <div className="h-14 w-14 rounded-full bg-blue-500/10 flex items-center justify-center text-blue-400 mb-4 group-hover:shadow-[0_0_15px_rgba(29,78,216,0.3)] transition-all">
                  <Activity size={26} />
                </div>
                <span className="text-4xl sm:text-5xl font-black tracking-tight mb-2 text-white">
                  <CountUpStat value={stats.activeCases} />
                </span>
                <span className="text-xs text-slate-400 font-bold tracking-wider uppercase mb-4">Active Emergency Cases</span>
                <div className="h-0.5 bg-teal-500/80 rounded-full reveal-underline" />
              </div>
            </ScrollReveal>

            {/* Stat 2: Volunteers */}
            <ScrollReveal delay={0.1}>
              <div className="flex flex-col items-center text-center p-6 h-full rounded-2xl bg-slate-900/40 border border-slate-800/60 hover:scale-104 hover:bg-slate-900/60 transition-all duration-300 group">
                <div className="h-14 w-14 rounded-full bg-teal-500/10 flex items-center justify-center text-teal-400 mb-4 group-hover:shadow-[0_0_15px_rgba(13,148,136,0.3)] transition-all">
                  <Users size={26} />
                </div>
                <span className="text-4xl sm:text-5xl font-black tracking-tight mb-2 text-white">
                  <CountUpStat value={stats.totalVolunteers} />
                </span>
                <span className="text-xs text-slate-400 font-bold tracking-wider uppercase mb-4">Registered Volunteers</span>
                <div className="h-0.5 bg-teal-500/80 rounded-full reveal-underline" />
              </div>
            </ScrollReveal>

            {/* Stat 3: Resources Delivered */}
            <ScrollReveal delay={0.2}>
              <div className="flex flex-col items-center text-center p-6 h-full rounded-2xl bg-slate-900/40 border border-slate-800/60 hover:scale-104 hover:bg-slate-900/60 transition-all duration-300 group">
                <div className="h-14 w-14 rounded-full bg-green-500/10 flex items-center justify-center text-green-400 mb-4 group-hover:shadow-[0_0_15px_rgba(16,185,129,0.3)] transition-all">
                  <Truck size={26} />
                </div>
                <span className="text-4xl sm:text-5xl font-black tracking-tight mb-2 text-white">
                  <CountUpStat value={stats.resourcesDelivered} blinkPlus />
                </span>
                <span className="text-xs text-slate-400 font-bold tracking-wider uppercase mb-4">Resources Delivered</span>
                <div className="h-0.5 bg-teal-500/80 rounded-full reveal-underline" />
              </div>
            </ScrollReveal>

            {/* Stat 4: Critical Reports */}
            <ScrollReveal delay={0.3}>
              <div className="flex flex-col items-center text-center p-6 h-full rounded-2xl bg-slate-900/40 border border-slate-800/60 hover:scale-104 hover:bg-slate-900/60 transition-all duration-300 group">
                <div className="h-14 w-14 rounded-full bg-red-500/10 flex items-center justify-center text-red-400 mb-4 group-hover:shadow-[0_0_15px_rgba(239,68,68,0.3)] transition-all animate-pulse">
                  <AlertTriangle size={26} />
                </div>
                <span className="text-4xl sm:text-5xl font-black tracking-tight mb-2 text-white">
                  <CountUpStat value={stats.criticalReports} />
                </span>
                <span className="text-xs text-slate-400 font-bold tracking-wider uppercase mb-4">Critical High-Risk Cases</span>
                <div className="h-0.5 bg-teal-500/80 rounded-full reveal-underline" />
              </div>
            </ScrollReveal>

          </div>
        </div>
      </section>

      {/* ==========================================
          FEATURES SECTION
          ========================================== */}
      <section id="features" className="py-24 bg-slate-50 relative">
        <div className="max-w-7xl mx-auto px-6">
          
          <ScrollReveal>
            <div className="text-center max-w-3xl mx-auto mb-20 flex flex-col gap-3">
              <span className="text-xs font-bold text-teal-600 tracking-widest uppercase">System Architecture</span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">
                Enterprise Features Crafted for Humanitarian Response
              </h2>
              <p className="text-slate-600 text-sm sm:text-base leading-relaxed">
                SevaAi integrates multiple advanced coordination tools into a secure, cohesive dashboard, enabling NGOs to act rapidly and intelligently.
              </p>
            </div>
          </ScrollReveal>

          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
            
            {/* Feature 1: AI Prioritization */}
            <ScrollReveal delay={0}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-blue-50 text-blue-700 flex items-center justify-center">
                  <Sparkles size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">AI Prioritization</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Our NLP prioritization engine automatically parses incident descriptions to evaluate life-safety risks, assigning Critical, High, Medium, or Low priority tags in under 30 seconds.
                </p>
              </Card>
            </ScrollReveal>

            {/* Feature 2: Volunteer Coordination */}
            <ScrollReveal delay={0.1}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-teal-50 text-teal-700 flex items-center justify-center">
                  <Users size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Volunteer Coordination</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Seamlessly match active incidents with qualified volunteers based on location and specialty (Medical, Rescue, Shelter, Logistics) to optimize response times.
                </p>
              </Card>
            </ScrollReveal>

            {/* Feature 3: Resource Tracking */}
            <ScrollReveal delay={0.2}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-green-50 text-green-700 flex items-center justify-center">
                  <Truck size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Resource Tracking</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Maintain real-time inventories of critical assets (Water, Medical kits, Rescue boats, Emergency rations). Automatic deductions trigger upon deployment.
                </p>
              </Card>
            </ScrollReveal>

            {/* Feature 4: Emergency Alerts */}
            <ScrollReveal delay={0.3}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-red-50 text-red-700 flex items-center justify-center">
                  <AlertTriangle size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Emergency Alerts</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Broadcast instant crisis alerts to volunteers and community members in designated geofenced sectors during critical, high-risk events.
                </p>
              </Card>
            </ScrollReveal>

            {/* Feature 5: Analytics Dashboard */}
            <ScrollReveal delay={0.4}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center">
                  <TrendingUp size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Analytics Dashboard</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Track operational health metrics, case resolution velocities, volunteer activity levels, and resource supply chains in a professional dashboard.
                </p>
              </Card>
            </ScrollReveal>

            {/* Feature 6: Heatmaps & Mapping */}
            <ScrollReveal delay={0.5}>
              <Card hoverLift className="flex flex-col gap-4 h-full text-left bg-white border border-slate-100 p-8">
                <div className="h-12 w-12 rounded-xl bg-indigo-50 text-indigo-700 flex items-center justify-center">
                  <Map size={24} />
                </div>
                <h3 className="text-lg font-bold text-slate-900">Incident Heatmaps</h3>
                <p className="text-xs sm:text-sm text-slate-600 leading-relaxed">
                  Visualize active incidents and historical hot spots geographically to deploy personnel and resources to the areas of greatest need.
                </p>
              </Card>
            </ScrollReveal>

          </div>
        </div>
      </section>

      {/* ==========================================
          HOW IT WORKS SECTION
          ========================================== */}
      <section id="how-it-works" className="py-24 bg-white relative">
        <div className="max-w-7xl mx-auto px-6">
          
          <ScrollReveal>
            <div className="text-center max-w-3xl mx-auto mb-20 flex flex-col gap-3">
              <span className="text-xs font-bold text-teal-600 tracking-widest uppercase">Workflow Pipeline</span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">
                A Highly Coordinated Response, Automated
              </h2>
              <p className="text-slate-600 text-sm sm:text-base leading-relaxed">
                How SevaAi closes the gap between an emergency report and resource delivery in four structured steps.
              </p>
            </div>
          </ScrollReveal>

          <div className="relative">
            {/* Timeline connection line (desktop only) */}
            <div className="hidden lg:block absolute top-1/2 left-12 right-12 h-0.5 bg-slate-100 -translate-y-1/2 z-0" />

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8 relative z-10">
              
              {/* Step 1 */}
              <ScrollReveal delay={0}>
                <div className="flex flex-col items-center text-center group">
                  <div className="h-16 w-16 rounded-full bg-blue-50 text-blue-700 border-2 border-blue-100 flex items-center justify-center font-bold text-xl shadow-md group-hover:bg-blue-700 group-hover:text-white transition-all duration-300">
                    01
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mt-5 mb-2">Submit Need</h3>
                  <p className="text-xs sm:text-sm text-slate-500 max-w-[240px] leading-relaxed">
                    Citizens or local coordinators submit an emergency report specifying category, location, and details.
                  </p>
                </div>
              </ScrollReveal>

              {/* Step 2 */}
              <ScrollReveal delay={0.15}>
                <div className="flex flex-col items-center text-center group">
                  <div className="h-16 w-16 rounded-full bg-teal-50 text-teal-700 border-2 border-teal-100 flex items-center justify-center font-bold text-xl shadow-md group-hover:bg-teal-700 group-hover:text-white transition-all duration-300">
                    02
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mt-5 mb-2">AI Prioritizes</h3>
                  <p className="text-xs sm:text-sm text-slate-500 max-w-[240px] leading-relaxed">
                    Our natural language processing algorithm instantly assesses risks and tags the report with the correct priority.
                  </p>
                </div>
              </ScrollReveal>

              {/* Step 3 */}
              <ScrollReveal delay={0.3}>
                <div className="flex flex-col items-center text-center group">
                  <div className="h-16 w-16 rounded-full bg-orange-50 text-orange-600 border-2 border-orange-100 flex items-center justify-center font-bold text-xl shadow-md group-hover:bg-orange-600 group-hover:text-white transition-all duration-300">
                    03
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mt-5 mb-2">Volunteers Assigned</h3>
                  <p className="text-xs sm:text-sm text-slate-500 max-w-[240px] leading-relaxed">
                    Local standby volunteers with fitting specialties are matched and dispatched immediately.
                  </p>
                </div>
              </ScrollReveal>

              {/* Step 4 */}
              <ScrollReveal delay={0.45}>
                <div className="flex flex-col items-center text-center group">
                  <div className="h-16 w-16 rounded-full bg-green-50 text-green-700 border-2 border-green-100 flex items-center justify-center font-bold text-xl shadow-md group-hover:bg-green-700 group-hover:text-white transition-all duration-300">
                    04
                  </div>
                  <h3 className="text-lg font-bold text-slate-900 mt-5 mb-2">Resources Delivered</h3>
                  <p className="text-xs sm:text-sm text-slate-500 max-w-[240px] leading-relaxed">
                    Critical inventories are distributed, the case is marked resolved, and analytics are compiled.
                  </p>
                </div>
              </ScrollReveal>

            </div>
          </div>
        </div>
      </section>

      {/* ==========================================
          TESTIMONIALS SECTION
          ========================================== */}
      <section id="testimonials" className="py-24 bg-slate-50">
        <div className="max-w-7xl mx-auto px-6">
          
          <ScrollReveal>
            <div className="text-center max-w-3xl mx-auto mb-20 flex flex-col gap-3">
              <span className="text-xs font-bold text-blue-700 tracking-widest uppercase">Community Voices</span>
              <h2 className="text-3xl sm:text-4xl font-black text-slate-900 tracking-tight">
                Trusted by Coordinators & Local Responders
              </h2>
              <p className="text-slate-600 text-sm sm:text-base leading-relaxed">
                Hear how SevaAi has changed the speed and effectiveness of emergency response on the ground.
              </p>
            </div>
          </ScrollReveal>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            
            {/* Testimonial 1 */}
            <ScrollReveal delay={0}>
              <Card className="flex flex-col justify-between h-full bg-white border border-slate-100 p-8 text-left">
                <div className="flex flex-col gap-4">
                  <div className="flex text-orange-500 gap-1">
                    {"★".repeat(5)}
                  </div>
                  <p className="text-xs sm:text-sm text-slate-600 italic leading-relaxed">
                    "During the recent flash floods, we were overwhelmed with requests. SevaAi's AI triage system allowed us to instantly identify the three families trapped on roofs amidst 200 other minor supply calls. It literally saved lives."
                  </p>
                </div>
                <div className="flex items-center gap-3 mt-8 pt-4 border-t border-slate-100">
                  <div className="h-10 w-10 rounded-full bg-blue-100 text-blue-800 font-bold flex items-center justify-center text-xs">
                    SJ
                  </div>
                  <div className="flex flex-col">
                    <span className="text-xs sm:text-sm font-bold text-slate-900">Sarah Jenkins</span>
                    <span className="text-[10px] text-slate-500 font-semibold">Regional Disaster Coordinator</span>
                  </div>
                </div>
              </Card>
            </ScrollReveal>

            {/* Testimonial 2 */}
            <ScrollReveal delay={0.15}>
              <Card className="flex flex-col justify-between h-full bg-white border border-slate-100 p-8 text-left">
                <div className="flex flex-col gap-4">
                  <div className="flex text-orange-500 gap-1">
                    {"★".repeat(5)}
                  </div>
                  <p className="text-xs sm:text-sm text-slate-600 italic leading-relaxed">
                    "As a volunteer doctor, I want to spend my time treating patients, not reading through logs. The system alerts me only when a medical need matches my location and specialty. The secure OTP sign-in takes 5 seconds."
                  </p>
                </div>
                <div className="flex items-center gap-3 mt-8 pt-4 border-t border-slate-100">
                  <div className="h-10 w-10 rounded-full bg-teal-100 text-teal-800 font-bold flex items-center justify-center text-xs">
                    EM
                  </div>
                  <div className="flex flex-col">
                    <span className="text-xs sm:text-sm font-bold text-slate-900">Dr. Evelyn Martinez</span>
                    <span className="text-[10px] text-slate-500 font-semibold">Standby Medical Volunteer</span>
                  </div>
                </div>
              </Card>
            </ScrollReveal>

            {/* Testimonial 3 */}
            <ScrollReveal delay={0.3}>
              <Card className="flex flex-col justify-between h-full bg-white border border-slate-100 p-8 text-left">
                <div className="flex flex-col gap-4">
                  <div className="flex text-orange-500 gap-1">
                    {"★".repeat(5)}
                  </div>
                  <p className="text-xs sm:text-sm text-slate-600 italic leading-relaxed">
                    "We were able to coordinate 1,200 liters of water delivery in under 4 hours. The inventory tracking system auto-deducts supplies so we always know exactly what we have on hand. No more manual spreadsheets!"
                  </p>
                </div>
                <div className="flex items-center gap-3 mt-8 pt-4 border-t border-slate-100">
                  <div className="h-10 w-10 rounded-full bg-orange-100 text-orange-800 font-bold flex items-center justify-center text-xs">
                    MV
                  </div>
                  <div className="flex flex-col">
                    <span className="text-xs sm:text-sm font-bold text-slate-900">Marcus Vance</span>
                    <span className="text-[10px] text-slate-500 font-semibold">Logistics Lead, Red Cross</span>
                  </div>
                </div>
              </Card>
            </ScrollReveal>

          </div>
        </div>
      </section>

      {/* ==========================================
          JOIN AS VOLUNTEER MODAL
          ========================================== */}
      {isVolModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4">
          <div className="fixed inset-0 bg-slate-900/60 backdrop-blur-xs" onClick={() => setIsVolModalOpen(false)} />
          <motion.div 
            initial={{ opacity: 0, scale: 0.95, y: 20 }}
            animate={{ opacity: 1, scale: 1, y: 0 }}
            className="bg-white rounded-xl shadow-xl border border-slate-100 p-6 max-w-md w-full relative z-10 text-left"
          >
            <h3 className="text-lg font-bold text-slate-900 mb-2">Register as Standby Volunteer</h3>
            <p className="text-xs text-slate-500 mb-4">
              Join our network of community responders. You will be notified when local needs match your selected specialty.
            </p>
            <form onSubmit={handleVolSubmit} className="flex flex-col gap-4">
              <div>
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-1">Full Name</label>
                <input 
                  type="text" 
                  required 
                  value={volName} 
                  onChange={(e) => setVolName(e.target.value)}
                  placeholder="e.g. Jane Doe"
                  className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-1">Specialty</label>
                <select 
                  value={volSpecialty} 
                  onChange={(e: any) => setVolSpecialty(e.target.value)}
                  className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="General">General Support / Logistics</option>
                  <option value="Medical">Medical Care (Doctor/Nurse/EMT)</option>
                  <option value="Rescue">Search & Rescue Operations</option>
                  <option value="Water">Water Logistics & Plumbing</option>
                  <option value="Shelter">Shelter Assembly & Care</option>
                  <option value="Power">Electrical & Power Systems</option>
                </select>
              </div>
              <div>
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-1">Phone Number</label>
                <input 
                  type="tel" 
                  required 
                  value={volPhone} 
                  onChange={(e) => setVolPhone(e.target.value)}
                  placeholder="e.g. +1 (555) 019-2834"
                  className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none"
                />
              </div>
              <div>
                <label className="text-xs font-bold text-slate-700 uppercase tracking-wider block mb-1">District / Location</label>
                <input 
                  type="text" 
                  required 
                  value={volLocation} 
                  onChange={(e) => setVolLocation(e.target.value)}
                  placeholder="e.g. Northside District"
                  className="w-full bg-white border border-slate-200 rounded-lg px-3 py-2 text-sm focus:outline-none"
                />
              </div>
              <div className="flex gap-3 justify-end mt-2">
                <button 
                  type="button" 
                  onClick={() => setIsVolModalOpen(false)}
                  className="px-4 py-2 text-xs font-bold text-slate-500 hover:bg-slate-100 rounded-lg transition-colors cursor-pointer"
                >
                  Cancel
                </button>
                <Button type="submit" variant="success" size="sm">
                  Complete Registration
                </Button>
              </div>
            </form>
          </motion.div>
        </div>
      )}

      {/* ==========================================
          FOOTER
          ========================================== */}
      <footer className="bg-slate-900 border-t border-slate-800 text-slate-400 text-xs sm:text-sm text-left">
        <div className="max-w-7xl mx-auto px-6 py-12 grid grid-cols-1 md:grid-cols-12 gap-8">
          
          {/* Footer Left Brand */}
          <div className="md:col-span-4 flex flex-col gap-4">
            <div className="flex items-center gap-2">
              <Logo size={32} />
              <span className="text-base font-black text-white tracking-tight">SevaAi</span>
            </div>
            <p className="text-xs text-slate-400 leading-relaxed max-w-xs">
              Empowering global humanitarian organizations with intelligent, secure, and accessible resource coordination. Built for speed, trust, and community resilience.
            </p>
          </div>

          {/* Footer Mid Links */}
          <div className="md:col-span-2 flex flex-col gap-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Resources</h4>
            <a href="#" className="hover:text-white transition-colors">Privacy Policy</a>
            <a href="#" className="hover:text-white transition-colors">Terms of Service</a>
            <a href="#" className="hover:text-white transition-colors">Support Portal</a>
            <a href="#" className="hover:text-white transition-colors">SLA Agreement</a>
          </div>

          <div className="md:col-span-2 flex flex-col gap-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Deployments</h4>
            <a href="#" className="hover:text-white transition-colors">Riverside Sector</a>
            <a href="#" className="hover:text-white transition-colors">Northside District</a>
            <a href="#" className="hover:text-white transition-colors">Westside Gym</a>
            <a href="#" className="hover:text-white transition-colors">Central Plaza</a>
          </div>

          {/* Footer Right Contact */}
          <div className="md:col-span-4 flex flex-col gap-3">
            <h4 className="text-xs font-bold text-white uppercase tracking-wider">Contact Headquarters</h4>
            <p className="text-xs leading-relaxed">
              Global Crisis Coordination Center<br />
              100 Humanitarian Way, Suite 400<br />
              Geneva, Switzerland
            </p>
            <p className="text-xs mt-1">
              Email: <span className="text-teal-400 font-semibold">support@sevaai-ngo.org</span><br />
              Emergency Line: <span className="text-orange-400 font-semibold">+41 22 555 0190</span>
            </p>
          </div>

        </div>

        {/* Footer Bottom copyright */}
        <div className="border-t border-slate-800 py-6 text-center text-xs text-slate-500 px-6">
          <div className="max-w-7xl mx-auto flex flex-col sm:flex-row justify-between items-center gap-4">
            <span>&copy; {new Date().getFullYear()} SevaAi Organization. All rights reserved.</span>
            <div className="flex gap-4">
              <span>WCAG AA Compliant</span>
              <span>•</span>
              <span>ISO 27001 Certified</span>
              <span>•</span>
              <span>AES-256 Encryption</span>
            </div>
          </div>
        </div>
      </footer>
    </div>
  );
};
