import React from 'react';

interface LogoProps extends React.SVGProps<SVGSVGElement> {
  size?: 'sm' | 'md' | 'lg' | 'xl';
}

export const Logo: React.FC<LogoProps> = ({ size = 'md', className = '', ...props }) => {
  const sizes = {
    sm: 'h-6 w-6',
    md: 'h-9 w-9',
    lg: 'h-12 w-12',
    xl: 'h-16 w-16',
  };

  return (
    <svg 
      xmlns="http://www.w3.org/2000/svg" 
      viewBox="0 0 32 32" 
      className={`${sizes[size]} ${className} shrink-0`}
      {...props}
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
