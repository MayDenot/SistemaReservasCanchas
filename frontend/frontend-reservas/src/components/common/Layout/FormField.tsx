import React from 'react';
import Input from './Input';

interface FormFieldProps {
  name: string;
  label: string;
  type?: string;
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  required?: boolean;
  icon?: string;
  error?: string;
}

const FormField: React.FC<FormFieldProps> = ({
                                               name,
                                               label,
                                               type = 'text',
                                               value,
                                               onChange,
                                               placeholder,
                                               required,
                                               icon,
                                               error,
                                             }) => {
  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onChange(e.target.value);
  };

  return (
    <Input
      id={name}
      label={label}
      type={type}
      value={value}
      onChange={handleChange}
      placeholder={placeholder}
      required={required}
      icon={icon}
      error={error}
    />
  );
};

export default FormField;