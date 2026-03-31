import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import type { UserRequest } from "../../api/types/user.types.ts";
import { useAuth } from "../../context/AuthContext.tsx";
import FormField from '../../components/common/Layout/FormField.tsx';

interface ClubData {
  name: string;
  address: string;
  description: string;
  phone: string;
  email: string;
}

const RegisterClubAdminPage: React.FC = () => {
  const [step, setStep] = useState(1);
  const [userData, setUserData] = useState({
    email: '',
    password: '',
    name: '',
    phone: '',
  });

  const [clubData, setClubData] = useState<ClubData>({
    name: '',
    address: '',
    description: '',
    phone: '',
    email: '',
  });

  const [confirmPassword, setConfirmPassword] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');

  const { isLoading } = useAuth();
  const navigate = useNavigate();

  const validateStep1 = () => {
    const errors: Record<string, string> = {};

    if (!userData.name.trim()) {
      errors.name = 'Tu nombre es requerido';
    }

    if (!userData.email.trim()) {
      errors.email = 'Tu email es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(userData.email)) {
      errors.email = 'Email inválido';
    }

    if (!userData.password) {
      errors.password = 'La contraseña es requerida';
    } else if (userData.password.length < 6) {
      errors.password = 'La contraseña debe tener al menos 6 caracteres';
    }

    if (!confirmPassword) {
      errors.confirmPassword = 'Confirma tu contraseña';
    } else if (userData.password !== confirmPassword) {
      errors.confirmPassword = 'Las contraseñas no coinciden';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const validateStep2 = () => {
    const errors: Record<string, string> = {};

    if (!clubData.name.trim()) {
      errors.clubName = 'El nombre del club es requerido';
    }

    if (!clubData.address.trim()) {
      errors.clubAddress = 'La dirección del club es requerida';
    }

    if (!clubData.description.trim()) {
      errors.clubDescription = 'La descripción del club es requerida';
    }

    if (!clubData.phone.trim()) {
      errors.clubPhone = 'El teléfono del club es requerido';
    }

    if (!clubData.email.trim()) {
      errors.clubEmail = 'El email del club es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(clubData.email)) {
      errors.clubEmail = 'Email inválido';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleStep1Submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (validateStep1()) {
      setStep(2);
    }
  };

  const handleStep2Submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');
    setFieldErrors({});

    if (!validateStep2()) {
      return;
    }

    try {
      console.log('=== INICIANDO REGISTRO DE CLUB ===');

      // 1. Registrar usuario
      const userRequest: UserRequest = {
        email: userData.email,
        password: userData.password,
        userRole: 'CLUB_ADMIN',
        name: userData.name,
        phone: userData.phone,
        createdAt: new Date().toISOString(),
      };

      console.log('Registrando usuario:', userData.email);

      // PRUEBA: Usar fetch directamente con URL correcta
      const registerResponse = await fetch('http://localhost:8080/api/auth/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(userRequest)
      });

      if (!registerResponse.ok) {
        const errorText = await registerResponse.text();
        console.error('Error en registro:', errorText);
        throw new Error(`Registro falló: ${errorText}`);
      }

      const userDataResponse = await registerResponse.json();
      console.log('Usuario registrado:', userDataResponse);

      // Asegúrate de que el response tiene el ID
      const userId = userDataResponse.id || userDataResponse.userId;
      console.log('UserId obtenido:', userId);

      if (!userId) {
        console.warn('No se encontró userId en la respuesta, usando email para buscar...');
        // Si no viene el ID, podríamos obtenerlo de otro endpoint
      }

      // 2. Login automático
      console.log('Realizando login automático...');
      const loginResponse = await fetch('http://localhost:8080/api/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: userData.email,
          password: userData.password,
        })
      });

      if (!loginResponse.ok) {
        const errorText = await loginResponse.text();
        throw new Error(`Login falló: ${errorText}`);
      }

      const loginData = await loginResponse.json();
      const token = loginData.token;
      localStorage.setItem('token', token);
      console.log('Login exitoso, token guardado');

      // 3. Crear club con el userId
      const clubRequest = {
        name: clubData.name,
        address: clubData.address,
        phone: clubData.phone,
        email: clubData.email,
        description: clubData.description,
        adminId: userId || 1, // ← Si no hay userId, usar 1 temporalmente
        openingTime: "08:00",
        closingTime: "22:00",
      };

      console.log('Creando club con datos:', clubRequest);

      // 4. Enviar request para crear club
      const clubResponse = await fetch('http://localhost:8080/api/clubs', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify(clubRequest)
      });

      if (!clubResponse.ok) {
        const errorText = await clubResponse.text();
        console.error('Error del servidor al crear club:', errorText);

        // Intenta sin adminId
        const { adminId, ...clubRequestWithoutAdmin } = clubRequest;
        console.log('Intentando sin adminId...');

        const retryResponse = await fetch('http://localhost:8080/api/clubs', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`
          },
          body: JSON.stringify(clubRequestWithoutAdmin)
        });

        if (!retryResponse.ok) {
          const retryError = await retryResponse.text();
          throw new Error(`Error creando club: ${retryError}`);
        }

        const clubResult = await retryResponse.json();
        console.log('Club creado exitosamente (sin adminId):', clubResult);
      } else {
        const clubResult = await clubResponse.json();
        console.log('Club creado exitosamente:', clubResult);
      }

      navigate('/club/dashboard');

    } catch (err: any) {
      console.error('Error completo:', err);

      // Mostrar error más específico
      let errorMessage = 'Error al registrar';
      if (err.message) {
        errorMessage = err.message;
      }

      setFormError(errorMessage);
    }
  };

  const handleUserFieldChange = (field: keyof typeof userData) => (value: string) => {
    setUserData(prev => ({
      ...prev,
      [field]: value
    }));

    if (fieldErrors[field]) {
      setFieldErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  const handleClubFieldChange = (field: keyof ClubData) => (value: string) => {
    setClubData(prev => ({
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
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 via-white to-cyan-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-3xl w-full bg-white rounded-3xl shadow-2xl overflow-hidden">
        <div className="px-8 py-10 sm:p-12">
          <div className="text-center mb-8">
            <div className="mx-auto h-16 w-16 bg-gradient-to-br from-blue-600 to-cyan-600 rounded-2xl flex items-center justify-center mb-6">
              <span className="material-icons text-3xl text-white">business</span>
            </div>
            <h2 className="text-3xl font-extrabold text-gray-900">
              Registrar Club
            </h2>
            <p className="mt-2 text-gray-600">
              Crea una cuenta como administrador de club y comienza a recibir reservas
            </p>
          </div>

          {/* Indicador de pasos */}
          <div className="flex mb-12">
            <div className="flex-1 text-center">
              <div className={`w-10 h-10 mx-auto rounded-full flex items-center justify-center mb-2 ${
                step >= 1 ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-400'
              }`}>
                1
              </div>
              <span className={`text-sm ${step >= 1 ? 'text-blue-600 font-medium' : 'text-gray-400'}`}>
                Datos Personales
              </span>
            </div>
            <div className="flex-1 flex items-center justify-center">
              <div className={`h-1 w-16 ${step >= 2 ? 'bg-blue-600' : 'bg-gray-200'}`}></div>
            </div>
            <div className="flex-1 text-center">
              <div className={`w-10 h-10 mx-auto rounded-full flex items-center justify-center mb-2 ${
                step >= 2 ? 'bg-blue-600 text-white' : 'bg-gray-200 text-gray-400'
              }`}>
                2
              </div>
              <span className={`text-sm ${step >= 2 ? 'text-blue-600 font-medium' : 'text-gray-400'}`}>
                Datos del Club
              </span>
            </div>
          </div>

          {formError && (
            <div className="error border-l-4 border-red-500 p-4 rounded-lg mb-8">
              <div className="flex items-center">
                <div className="flex-shrink-0">
                  <span className="material-icons text-red-500">error</span>
                </div>
                <div className="ml-3">
                  <p className="text-sm text-red-700">{formError}</p>
                </div>
              </div>
            </div>
          )}

          {/* Paso 1: Datos personales */}
          {step === 1 && (
            <form className="space-y-6" onSubmit={handleStep1Submit} noValidate>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="md:col-span-2">
                  <h3 className="text-xl font-bold text-gray-900 mb-4">Tus datos personales</h3>
                </div>

                <div className="md:col-span-2">
                  <FormField
                    name="name"
                    label="Tu nombre completo"
                    value={userData.name}
                    onChange={handleUserFieldChange('name')}
                    placeholder="Juan Pérez"
                    required
                    icon="person"
                    error={fieldErrors.name}
                    iconType="material"
                  />
                </div>

                <FormField
                  name="email"
                  label="Tu email"
                  type="email"
                  value={userData.email}
                  onChange={handleUserFieldChange('email')}
                  placeholder="tu@email.com"
                  required
                  icon="mail_outline"
                  error={fieldErrors.email}
                  iconType="material"
                />

                <FormField
                  name="phone"
                  label="Tu teléfono"
                  type="tel"
                  value={userData.phone}
                  onChange={handleUserFieldChange('phone')}
                  placeholder="+54 11 1234-5678"
                  required
                  icon="phone"
                  error={fieldErrors.phone}
                  iconType="material"
                />

                <FormField
                  name="password"
                  label="Contraseña"
                  type="password"
                  value={userData.password}
                  onChange={handleUserFieldChange('password')}
                  placeholder="Mínimo 6 caracteres"
                  required
                  icon="lock"
                  error={fieldErrors.password}
                  iconType="material"
                />

                <FormField
                  name="confirmPassword"
                  label="Confirmar Contraseña"
                  type="password"
                  value={confirmPassword}
                  onChange={handleConfirmPasswordChange}
                  placeholder="Repite tu contraseña"
                  required
                  icon="lock_reset"
                  error={fieldErrors.confirmPassword}
                  iconType="material"
                />
              </div>

              <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded-lg">
                <div className="flex items-start">
                  <span className="material-icons text-blue-500 mr-3">business</span>
                  <div>
                    <p className="text-sm text-blue-700">
                      Como administrador de club podrás gestionar tus canchas, ver reservas,
                      administrar horarios y precios, y recibir pagos.
                    </p>
                  </div>
                </div>
              </div>

              <button
                type="submit"
                disabled={isLoading}
                className="w-full flex justify-center py-3 px-3 border border-transparent text-lg font-bold rounded-xl text-white bg-gradient-to-br from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02]"
              >
                <div className="flex items-center">
                  <span className="material-icons mr-2">arrow_forward</span>
                  Continuar al siguiente paso
                </div>
              </button>

              <div className="text-center">
                <Link to="/register" className="text-sm text-blue-600 hover:text-blue-500">
                  ← Volver a registro de usuario
                </Link>
              </div>
            </form>
          )}

          {/* Paso 2: Datos del club */}
          {step === 2 && (
            <form className="space-y-6" onSubmit={handleStep2Submit} noValidate>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                <div className="md:col-span-2">
                  <h3 className="text-xl font-bold text-gray-900 mb-4">Datos de tu club</h3>
                </div>

                <div className="md:col-span-2">
                  <FormField
                    name="clubName"
                    label="Nombre del club"
                    value={clubData.name}
                    onChange={handleClubFieldChange('name')}
                    placeholder="Mi Club Deportivo"
                    required
                    icon="apartment"
                    error={fieldErrors.clubName}
                    iconType="material"
                  />
                </div>

                <div className="md:col-span-2">
                  <FormField
                    name="clubAddress"
                    label="Dirección completa"
                    value={clubData.address}
                    onChange={handleClubFieldChange('address')}
                    placeholder="Calle Principal 123, Ciudad, País"
                    required
                    icon="location_on"
                    error={fieldErrors.clubAddress}
                    iconType="material"
                  />
                </div>

                <div className="md:col-span-2">
                  <FormField
                    name="clubDescription"
                    label="Descripción del club"
                    type="textarea"
                    value={clubData.description}
                    onChange={handleClubFieldChange('description')}
                    placeholder="Describe tu club, instalaciones, deportes disponibles, etc."
                    required
                    icon="description"
                    error={fieldErrors.clubDescription}
                    iconType="material"
                  />
                </div>

                <FormField
                  name="clubPhone"
                  label="Teléfono del club"
                  type="tel"
                  value={clubData.phone}
                  onChange={handleClubFieldChange('phone')}
                  placeholder="+54 11 8765-4321"
                  required
                  icon="phone"
                  error={fieldErrors.clubPhone}
                  iconType="material"
                />

                <FormField
                  name="clubEmail"
                  label="Email del club"
                  type="email"
                  value={clubData.email}
                  onChange={handleClubFieldChange('email')}
                  placeholder="club@email.com"
                  required
                  icon="alternate_email"
                  error={fieldErrors.clubEmail}
                  iconType="material"
                />
              </div>

              <div className="flex gap-4">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="flex-1 flex justify-center py-3 px-3 border border-gray-300 text-lg font-bold rounded-xl text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-all duration-300"
                >
                  <div className="flex items-center">
                    <span className="material-icons mr-2">arrow_back</span>
                    Volver
                  </div>
                </button>

                <button
                  type="submit"
                  disabled={isLoading}
                  className="flex-1 flex justify-center py-3 px-3 border border-transparent text-lg font-bold rounded-xl text-white bg-gradient-to-br from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02]"
                >
                  {isLoading ? (
                    <div className="flex items-center">
                      <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
                      Registrando...
                    </div>
                  ) : (
                    <div className="flex items-center">
                      <span className="material-icons mr-2">check_circle</span>
                      Registrar Club
                    </div>
                  )}
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

export default RegisterClubAdminPage;