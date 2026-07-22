import React, { createContext, useContext, useState, useEffect } from 'react';
import { useToast } from '../components/UI';

export interface EmergencyCase {
  id: string;
  title: string;
  category: 'Rescue' | 'Medical' | 'Food' | 'Water' | 'Shelter' | 'Power';
  location: string;
  coordinates: { x: number; y: number }; // Percentage coordinate on mock map (0-100)
  priority: 'Critical' | 'High' | 'Medium' | 'Low';
  status: 'Pending' | 'Assigned' | 'In Progress' | 'Resolved';
  description: string;
  contactInfo: string;
  dateSubmitted: string;
  assignedVolunteerId?: string;
  resolutionNotes?: string;
}

export interface Volunteer {
  id: string;
  name: string;
  specialty: 'Rescue' | 'Medical' | 'Food' | 'Water' | 'Shelter' | 'Power' | 'General';
  location: string;
  phone: string;
  status: 'Available' | 'Active' | 'Offline';
  casesCompleted: number;
}

export interface ResourceInventory {
  category: string;
  total: number;
  available: number;
  unit: string;
}

interface PlatformContextType {
  cases: EmergencyCase[];
  volunteers: Volunteer[];
  resources: ResourceInventory[];
  submitNeed: (need: Omit<EmergencyCase, 'id' | 'status' | 'dateSubmitted' | 'priority' | 'coordinates'>) => Promise<EmergencyCase>;
  assignVolunteer: (caseId: string, volunteerId: string) => void;
  updateCaseStatus: (caseId: string, status: EmergencyCase['status'], notes?: string) => void;
  registerVolunteer: (volunteer: Omit<Volunteer, 'id' | 'status' | 'casesCompleted'>) => void;
  stats: {
    activeCases: number;
    totalVolunteers: number;
    resourcesDelivered: number;
    criticalReports: number;
  };
}

const PlatformContext = createContext<PlatformContextType | undefined>(undefined);

const INITIAL_CASES: EmergencyCase[] = [
  {
    id: 'case_001',
    title: 'Family Trapped on Roof - Flooding',
    category: 'Rescue',
    location: 'Riverside Sector B',
    coordinates: { x: 28, y: 35 },
    priority: 'Critical',
    status: 'Pending',
    description: 'Rapid water level rise. Family of 4, including an infant and an elderly person, trapped on roof. Power lines are down in the vicinity.',
    contactInfo: '+1 (555) 982-1102',
    dateSubmitted: new Date(Date.now() - 45 * 60000).toISOString(), // 45m ago
  },
  {
    id: 'case_002',
    title: 'Critical Insulin Delivery Required',
    category: 'Medical',
    location: 'Northside District, Ave 4',
    coordinates: { x: 62, y: 18 },
    priority: 'High',
    status: 'Assigned',
    assignedVolunteerId: 'vol_001',
    description: 'Elderly diabetic patient with mobility issues has completely run out of insulin. Requires delivery of standard Humalog insulin cartridge.',
    contactInfo: 'patient-care@medlink.org',
    dateSubmitted: new Date(Date.now() - 120 * 60000).toISOString(), // 2h ago
  },
  {
    id: 'case_003',
    title: 'Emergency Shelter Cots Setup',
    category: 'Shelter',
    location: 'Westside Community Gym',
    coordinates: { x: 15, y: 65 },
    priority: 'Medium',
    status: 'In Progress',
    assignedVolunteerId: 'vol_003',
    description: 'Setting up 80 temporary sleeping cots and distributing blankets for citizens displaced by the storm. 3 volunteers active, 2 more needed.',
    contactInfo: '+1 (555) 441-2890',
    dateSubmitted: new Date(Date.now() - 180 * 60000).toISOString(), // 3h ago
  },
  {
    id: 'case_004',
    title: 'Water Supply Contamination Response',
    category: 'Water',
    location: 'Southside Ward, Block 12',
    coordinates: { x: 45, y: 82 },
    priority: 'High',
    status: 'Pending',
    description: 'Water main fracture has contaminated local drinking water supply. High urgency for delivery of clean drinking water jugs for 45 families.',
    contactInfo: 'block12-association@gmail.com',
    dateSubmitted: new Date(Date.now() - 60 * 60000).toISOString(), // 1h ago
  },
  {
    id: 'case_005',
    title: 'Ventilator Power Outage Emergency',
    category: 'Power',
    location: 'Central Plaza Medical Annex',
    coordinates: { x: 50, y: 48 },
    priority: 'Critical',
    status: 'Resolved',
    assignedVolunteerId: 'vol_002',
    description: 'Backup generator failure in home-care clinic. Patient on a critical ventilator needs immediate backup power station.',
    contactInfo: '+1 (555) 120-4499',
    dateSubmitted: new Date(Date.now() - 240 * 60000).toISOString(), // 4h ago
    resolutionNotes: 'Successfully dispatched a 2kW portable solar generator. Medical ventilator power restored in under 22 minutes.',
  }
];

const INITIAL_VOLUNTEERS: Volunteer[] = [
  {
    id: 'vol_001',
    name: 'Dr. Evelyn Martinez',
    specialty: 'Medical',
    location: 'Northside District',
    phone: '+1 (555) 321-4499',
    status: 'Active',
    casesCompleted: 14
  },
  {
    id: 'vol_002',
    name: 'Marcus Vance',
    specialty: 'Power',
    location: 'Central Plaza Area',
    phone: '+1 (555) 789-1122',
    status: 'Available',
    casesCompleted: 29
  },
  {
    id: 'vol_003',
    name: 'Cody Fisher',
    specialty: 'Shelter',
    location: 'Westside District',
    phone: '+1 (555) 444-5566',
    status: 'Active',
    casesCompleted: 8
  },
  {
    id: 'vol_004',
    name: 'Sarah Jenkins',
    specialty: 'Rescue',
    location: 'Riverside Sector B',
    phone: '+1 (555) 909-0808',
    status: 'Available',
    casesCompleted: 42
  },
  {
    id: 'vol_005',
    name: 'David Kim',
    specialty: 'Water',
    location: 'Southside Ward',
    phone: '+1 (555) 123-9876',
    status: 'Offline',
    casesCompleted: 19
  }
];

const INITIAL_RESOURCES: ResourceInventory[] = [
  { category: 'Rescue Boats', total: 12, available: 4, unit: 'units' },
  { category: 'Medical Kits', total: 150, available: 64, unit: 'kits' },
  { category: 'Drinking Water', total: 1200, available: 450, unit: 'liters' },
  { category: 'Emergency Rations', total: 800, available: 320, unit: 'packs' },
  { category: 'Power Generators', total: 15, available: 3, unit: 'units' },
  { category: 'Shelter Cots', total: 300, available: 110, unit: 'beds' }
];

const safeLocalStorage = {
  getItem: (key: string): string | null => {
    try {
      return localStorage.getItem(key);
    } catch (e) {
      return null;
    }
  },
  setItem: (key: string, value: string): void => {
    try {
      localStorage.setItem(key, value);
    } catch (e) {
      // ignore
    }
  }
};

export const PlatformProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [cases, setCases] = useState<EmergencyCase[]>(() => {
    const saved = safeLocalStorage.getItem('sevaai_cases');
    return saved ? JSON.parse(saved) : INITIAL_CASES;
  });

  const [volunteers, setVolunteers] = useState<Volunteer[]>(() => {
    const saved = safeLocalStorage.getItem('sevaai_volunteers');
    return saved ? JSON.parse(saved) : INITIAL_VOLUNTEERS;
  });

  const [resources, setResources] = useState<ResourceInventory[]>(() => {
    const saved = safeLocalStorage.getItem('sevaai_resources');
    return saved ? JSON.parse(saved) : INITIAL_RESOURCES;
  });

  const { showToast } = useToast();

  // Save to localStorage on state changes
  useEffect(() => {
    safeLocalStorage.setItem('sevaai_cases', JSON.stringify(cases));
  }, [cases]);

  useEffect(() => {
    safeLocalStorage.setItem('sevaai_volunteers', JSON.stringify(volunteers));
  }, [volunteers]);

  useEffect(() => {
    safeLocalStorage.setItem('sevaai_resources', JSON.stringify(resources));
  }, [resources]);

  // AI Prioritization Engine
  const evaluatePriorityAndCoordinates = (
    title: string,
    description: string,
    category: string
  ): { priority: EmergencyCase['priority']; coordinates: { x: number; y: number } } => {
    const text = `${title} ${description}`.toLowerCase();
    
    let priority: EmergencyCase['priority'] = 'Medium';

    // 1. Critical Indicators (Immediate danger to life)
    if (
      text.includes('trapped') ||
      text.includes('drowning') ||
      text.includes('breathing') ||
      text.includes('unconscious') ||
      text.includes('dying') ||
      text.includes('bleeding') ||
      text.includes('heart') ||
      text.includes('ventilator') ||
      text.includes('critical') ||
      text.includes('suffocating') ||
      text.includes('explosion')
    ) {
      priority = 'Critical';
    } 
    // 2. High Indicators (Severe distress, time-sensitive)
    else if (
      text.includes('insulin') ||
      text.includes('elderly') ||
      text.includes('infant') ||
      text.includes('baby') ||
      text.includes('starving') ||
      text.includes('contamination') ||
      text.includes('severe pain') ||
      text.includes('broken bone') ||
      text.includes('flooded house') ||
      text.includes('no water')
    ) {
      priority = 'High';
    }
    // 3. Low Indicators (Routine/preventative/logistical)
    else if (
      text.includes('blanket') ||
      text.includes('cot') ||
      text.includes('setup') ||
      text.includes('meeting') ||
      text.includes('routine') ||
      text.includes('registration') ||
      text.includes('information')
    ) {
      priority = 'Low';
    }

    // Generate random coordinates within a bounded visual grid (avoiding edges)
    const x = Math.floor(Math.random() * 70) + 15;
    const y = Math.floor(Math.random() * 70) + 15;

    return { priority, coordinates: { x, y } };
  };

  /**
   * Submit a new need (simulates AI triage)
   */
  const submitNeed = async (
    need: Omit<EmergencyCase, 'id' | 'status' | 'dateSubmitted' | 'priority' | 'coordinates'>
  ): Promise<EmergencyCase> => {
    // Simulate short processing delay
    await new Promise((resolve) => setTimeout(resolve, 1500));

    const { priority, coordinates } = evaluatePriorityAndCoordinates(
      need.title,
      need.description,
      need.category
    );

    const newCase: EmergencyCase = {
      ...need,
      id: `case_${Math.random().toString(36).substr(2, 9)}`,
      status: 'Pending',
      priority,
      coordinates,
      dateSubmitted: new Date().toISOString(),
    };

    setCases((prev) => [newCase, ...prev]);

    // Deduct some resource if applicable
    setResources((prev) =>
      prev.map((r) => {
        if (
          (need.category === 'Water' && r.category === 'Drinking Water') ||
          (need.category === 'Food' && r.category === 'Emergency Rations') ||
          (need.category === 'Medical' && r.category === 'Medical Kits')
        ) {
          return { ...r, available: Math.max(0, r.available - 10) };
        }
        return r;
      })
    );

    // Show custom toast about AI Triaging
    showToast(
      'success',
      `Our AI Coordinator parsed your report and categorized it as ${priority} Priority based on safety urgency indicators. Volunteers have been alerted.`,
      `AI Priority Triage: ${priority}`
    );

    return newCase;
  };

  /**
   * Assign a volunteer to a case
   */
  const assignVolunteer = (caseId: string, volunteerId: string) => {
    setCases((prev) =>
      prev.map((c) => {
        if (c.id === caseId) {
          return { ...c, status: 'Assigned', assignedVolunteerId: volunteerId };
        }
        return c;
      })
    );

    setVolunteers((prev) =>
      prev.map((v) => {
        if (v.id === volunteerId) {
          return { ...v, status: 'Active' };
        }
        return v;
      })
    );

    const volName = volunteers.find((v) => v.id === volunteerId)?.name || 'Volunteer';
    const caseTitle = cases.find((c) => c.id === caseId)?.title || 'Case';
    showToast('info', `Assigned ${volName} to "${caseTitle}".`, 'Resource Dispatched');
  };

  /**
   * Update status of a case (e.g. resolve it)
   */
  const updateCaseStatus = (caseId: string, status: EmergencyCase['status'], notes?: string) => {
    let volIdToUpdate: string | undefined;

    setCases((prev) =>
      prev.map((c) => {
        if (c.id === caseId) {
          volIdToUpdate = c.assignedVolunteerId;
          return { 
            ...c, 
            status, 
            resolutionNotes: notes || c.resolutionNotes 
          };
        }
        return c;
      })
    );

    if (status === 'Resolved' && volIdToUpdate) {
      const vId = volIdToUpdate; // Capture for closure
      setVolunteers((prev) =>
        prev.map((v) => {
          if (v.id === vId) {
            return {
              ...v,
              status: 'Available',
              casesCompleted: v.casesCompleted + 1,
            };
          }
          return v;
        })
      );
      showToast('success', 'Case resolved successfully. Volunteer is now available for next dispatch.', 'Case Resolved');
    } else {
      showToast('info', `Case status updated to ${status}.`, 'Status Updated');
    }
  };

  /**
   * Register a new volunteer
   */
  const registerVolunteer = (volunteer: Omit<Volunteer, 'id' | 'status' | 'casesCompleted'>) => {
    const newVol: Volunteer = {
      ...volunteer,
      id: `vol_${Math.random().toString(36).substr(2, 9)}`,
      status: 'Available',
      casesCompleted: 0,
    };

    setVolunteers((prev) => [...prev, newVol]);
    showToast('success', `Welcome ${newVol.name}! You are now on the standby list for ${newVol.specialty} support.`, 'Volunteer Registered');
  };

  // Derive stats
  const stats = {
    activeCases: cases.filter((c) => c.status !== 'Resolved').length,
    totalVolunteers: volunteers.length,
    resourcesDelivered: cases.filter((c) => c.status === 'Resolved').length * 25 + 342, // Mock baseline + completed cases multiplier
    criticalReports: cases.filter((c) => c.priority === 'Critical' && c.status !== 'Resolved').length,
  };

  return (
    <PlatformContext.Provider
      value={{
        cases,
        volunteers,
        resources,
        submitNeed,
        assignVolunteer,
        updateCaseStatus,
        registerVolunteer,
        stats,
      }}
    >
      {children}
    </PlatformContext.Provider>
  );
};

export const usePlatform = () => {
  const context = useContext(PlatformContext);
  if (!context) throw new Error('usePlatform must be used within a PlatformProvider');
  return context;
};
