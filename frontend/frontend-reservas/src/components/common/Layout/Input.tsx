import React from 'react';

interface InputProps {
  id: string;
  label: string;
  type?: string;
  value: string;
  onChange: (e: React.ChangeEvent<HTMLInputElement>) => void;
  placeholder?: string;
  required?: boolean;
  icon?: string;
  className?: string;
  error?: string;
  disabled?: boolean;
  autoComplete?: string;
}

const Input: React.FC<InputProps> = ({
                                       id,
                                       label,
                                       type = 'text',
                                       value,
                                       onChange,
                                       placeholder = '',
                                       required = false,
                                       icon,
                                       className = '',
                                       error,
                                       disabled = false,
                                       autoComplete = 'off',
                                     }) => {
  return (
    <div className="w-full">
      {label && (
        <label
          htmlFor={id}
          className="text-left block text-sm font-medium text-gray-700 mb-1"
        >
          {label}
        </label>
      )}
      <div className="relative">
        {icon && (
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <span className="text-gray-400">{icon}</span>
          </div>
        )}
        <input
          id={id}
          type={type}
          value={value}
          onChange={onChange}
          placeholder={placeholder}
          required={required}
          disabled={disabled}
          autoComplete={autoComplete}
          className={`
            text-black
            block w-full px-4 py-2 
            border-2 ${error ? 'border-red-500' : 'border-gray-200'} 
            rounded-xl focus:outline-none 
            focus:ring-2 focus:ring-green-500 
            focus:border-green-500 
            text-md transition-colors
            ${icon ? 'pl-10' : 'pl-4'}
            ${disabled ? 'bg-gray-100 cursor-not-allowed' : 'bg-white'}
            ${className}
          `}
        />
      </div>
      {error && (
        <p className="mt-2 text-sm text-red-600">{error}</p>
      )}
    </div>
  );
};

export default Input;