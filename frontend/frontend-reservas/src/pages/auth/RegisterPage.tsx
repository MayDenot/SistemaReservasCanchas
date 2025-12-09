import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import type { UserRequest } from "../../api/types/user.types.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import FormField from '../../components/common/Layout/FormField.tsx';

const RegisterPage: React.FC = () => {
  const [formData, setFormData] = useState<UserRequest>({
    email: '',
    password: '',
    userRole: 'USER',
    name: '',
    phone: '',
    createdAt: new Date().toISOString(),
  });

  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');

  const { register, isLoading, error } = useAuth();
  const navigate = useNavigate();

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!formData.name.trim()) {
      errors.name = 'El nombre es requerido';
    }

    if (!formData.email.trim()) {
      errors.email = 'El email es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      errors.email = 'Email inválido';
    }

    if (!formData.password) {
      errors.password = 'La contraseña es requerida';
    } else if (formData.password.length < 6) {
      errors.password = 'La contraseña debe tener al menos 6 caracteres';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Confirma tu contraseña';
    } else if (formData.password !== confirmPassword) {
      errors.confirmPassword = 'Las contraseñas no coinciden';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    setFieldErrors({});

    if (!validateForm()) {
      return;
    }

    try {
      await register(formData);
      navigate('/login', {
        state: { message: 'Cuenta creada exitosamente. Por favor inicia sesión.' }
      });
    } catch (err: any) {
      setFormError(err.message || 'Error al registrar usuario');
    }
  };

  const handleFieldChange = (field: keyof UserRequest) => (value: string) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));

    if (fieldErrors[field]) {
      setFieldErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleConfirmPasswordChange = (value: string) => {
    setConfirmPassword(value);

    if (fieldErrors.confirmPassword) {
      setFieldErrors(prev => ({ ...prev, confirmPassword: '' }));
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-green-50 via-white to-emerald-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-lg w-full bg-white rounded-3xl shadow-2xl overflow-hidden">
        <div className="px-8 py-10 sm:p-12">
          <div className="text-center mb-8">
            <h2 className="text-3xl font-extrabold text-gray-900">
              Crear cuenta nueva
            </h2>
          </div>

          {(error || formError) && (
            <div className="mb-8 bg-gradient-to-r from-red-50 to-red-100 border-l-4 border-red-500 p-4 rounded-lg">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <span className="text-red-500">⚠️</span>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-red-700">{error || formError}</p>
                </div>
              </div>
            </div>
          )}

          <form className="space-y-6" onSubmit={handleSubmit} noValidate>
            <div className="grid grid-cols-1 gap-5">
              <FormField
                name="name"
                label="Nombre completo"
                value={formData.name}
                onChange={handleFieldChange('name')}
                placeholder="Juan Pérez"
                required
                icon="👤"
                error={fieldErrors.name}
              />

              <FormField
                name="email"
                label="Email"
                type="email"
                value={formData.email}
                onChange={handleFieldChange('email')}
                placeholder="tu@email.com"
                required
                icon="📧"
                error={fieldErrors.email}
              />

              <FormField
                name="phone"
                label="Teléfono (opcional)"
                type="tel"
                value={formData.phone || ''}
                onChange={handleFieldChange('phone')}
                placeholder="+54 11 1234-5678"
                icon="📱"
              />

              <FormField
                name="password"
                label="Contraseña"
                type="password"
                value={formData.password}
                onChange={handleFieldChange('password')}
                placeholder="Mínimo 6 caracteres"
                required
                icon="🔒"
                error={fieldErrors.password}
              />

              <FormField
                name="confirmPassword"
                label="Confirmar Contraseña"
                type="password"
                value={confirmPassword}
                onChange={handleConfirmPasswordChange}
                placeholder="Repite tu contraseña"
                required
                icon="🔐"
                error={fieldErrors.confirmPassword}
              />
            </div>

            <button
              type="submit"
              disabled={isLoading}
              className="w-full flex justify-center py-3 px-3 border border-transparent text-lg font-bold rounded-xl text-white bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02]"
            >
              {isLoading ? (
                <div className="flex items-center">
                  <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
                  Registrando...
                </div>
              ) : (
                'Crear cuenta'
              )}
            </button>
          </form>

          <div className="mt-8 text-center">
            <p className="text-sm text-gray-600">
              ¿Ya tienes cuenta?{' '}
              <Link to="/login" className="font-medium text-green-600 hover:text-green-500">
                Inicia sesión
              </Link>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default RegisterPage;