import React from 'react';

export default function Layout({ children }: { children?: React.ReactNode }) {
  return <div className="app-layout">{children ?? <div>Layout</div>}</div>;
}
