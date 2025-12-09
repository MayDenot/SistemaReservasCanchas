import React from 'react';

interface ErrorMessageProps {
  message: string;
  retry?: () => void;
}

const ErrorMessage: React.FC<ErrorMessageProps> = ({ message, retry }) => {
  return (
    <div className="flex flex-col items-center justify-center p-4 md:p-6 bg-red-50 border border-red-200 rounded-lg">
      <div className="text-2xl md:text-3xl mb-2 md:mb-3">⚠️</div>
      <p className="text-sm md:text-base text-red-700 font-medium mb-3 md:mb-4">{message}</p>
      {retry && (
        <button
          onClick={retry}
          className="px-3 py-1.5 md:px-4 md:py-2 bg-red-600 text-white text-sm md:text-base font-semibold rounded-md hover:bg-red-700"
        >
          Reintentar
        </button>
      )}
    </div>
  );
};

export default ErrorMessage;