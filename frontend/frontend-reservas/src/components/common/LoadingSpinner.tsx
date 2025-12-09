import React from 'react';

const LoadingSpinner: React.FC<{ size?: 'small' | 'medium' | 'large' }> = ({ size = 'medium' }) => {
  return (
    <div className={`spinner-container spinner-${size}`}>
      <div className="spinner"></div>
      <p>Cargando...</p>
    </div>
  );
};

export default LoadingSpinner;