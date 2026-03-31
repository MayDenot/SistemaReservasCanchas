import React, {useEffect, useState} from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import FormField from '../../components/common/Layout/FormField';
import api from '../../api/axiosConfig';

interface ClubData {
  name: string;
  address: string;
  description: string;
  phone: string;
  email: string;
  openingTime: string;
  closingTime: string;
}

const CreateClubPage: React.FC = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [clubData, setClubData] = useState<ClubData>({
    name: '',
    address: '',
    description: '',
    phone: '',
    email: '',
    openingTime: '08:00',
    closingTime: '22:00',
  });

  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [formError, setFormError] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [_club, setClub] = useState<any>({});

  useEffect(() => {
    const fetchMyClub = async () => {
      try {
        const response = await api.get(`/clubs/my-club?userId=${user?.id}`);
        setClub(response.data);
      } catch (error) {
        console.error('Error cargando club:', error);
      }
    };

    if (user?.id) {
      fetchMyClub();
    }
  }, [user]);

  const validateForm = () => {
    const errors: Record<string, string> = {};

    if (!clubData.name.trim()) {
      errors.name = 'El nombre del club es requerido';
    }

    if (!clubData.address.trim()) {
      errors.address = 'La dirección del club es requerida';
    }

    if (!clubData.description.trim()) {
      errors.description = 'La descripción del club es requerida';
    }

    if (!clubData.phone.trim()) {
      errors.phone = 'El teléfono del club es requerido';
    }

    if (!clubData.email.trim()) {
      errors.email = 'El email del club es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(clubData.email)) {
      errors.email = 'Email inválido';
    }

    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setFormError('');

    if (!validateForm()) {
      return;
    }

    setIsSubmitting(true);

    try {
      const token = localStorage.getItem('authToken');

      const clubRequest = {
        name: clubData.name,
        address: clubData.address,
        phone: clubData.phone,
        email: clubData.email,
        description: clubData.description,
        openingTime: clubData.openingTime,
        closingTime: clubData.closingTime,
      };

      console.log('Creando club para usuario:', user?.email);
      console.log('Datos del club:', clubRequest);

      const response = await api.post('/clubs', clubRequest, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'X-User-Email': user?.email || '',
        }
      });

      console.log('Club creado exitosamente:', response.data);

      navigate('/club/dashboard', {
        state: {
          clubCreated: true,
          clubId: response.data.id,
          clubName: response.data.name
        }
      });

    } catch (err: any) {
      console.error('Error creando club:', err);
      setFormError(err.response?.data || err.message || 'Error al crear el club');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleFieldChange = (field: keyof ClubData) => (value: string) => {
    setClubData(prev => ({ ...prev, [field]: value }));
    if (fieldErrors[field]) {
      setFieldErrors(prev => ({ ...prev, [field]: '' }));
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        <div className="bg-white shadow-xl rounded-2xl p-8">
          <div className="text-center mb-8">
            <div className="mx-auto h-16 w-16 bg-gradient-to-br from-blue-600 to-cyan-600 rounded-2xl flex items-center justify-center mb-6">
              <span className="material-icons text-3xl text-white">add_business</span>
            </div>
            <h2 className="text-3xl font-extrabold text-gray-900">
              Crear Nuevo Club
            </h2>
            <p className="mt-2 text-gray-600">
              Hola <span className="font-semibold text-blue-600">{user?.name}</span>, completa los datos de tu club
            </p>
            <p className="text-sm text-gray-500 mt-2">
              Tu usuario será automáticamente asignado como administrador
            </p>
          </div>

          {formError && (
            <div className="mb-6 p-4 bg-red-50 border-l-4 border-red-500 rounded-lg">
              <div className="flex items-center">
                <span className="material-icons text-red-500 mr-3">error</span>
                <p className="text-red-700">{formError}</p>
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="md:col-span-2">
                <FormField
                  name="name"
                  label="Nombre del club *"
                  value={clubData.name}
                  onChange={handleFieldChange('name')}
                  placeholder="Mi Club Deportivo"
                  required
                  icon="apartment"
                  error={fieldErrors.name}
                  iconType="material"
                />
              </div>

              <div className="md:col-span-2">
                <FormField
                  name="address"
                  label="Dirección completa *"
                  value={clubData.address}
                  onChange={handleFieldChange('address')}
                  placeholder="Calle Principal 123, Ciudad, País"
                  required
                  icon="location_on"
                  error={fieldErrors.address}
                  iconType="material"
                />
              </div>

              <div className="md:col-span-2">
                <FormField
                  name="description"
                  label="Descripción del club *"
                  type="textarea"
                  value={clubData.description}
                  onChange={handleFieldChange('description')}
                  placeholder="Describe tu club, instalaciones, deportes disponibles, etc."
                  required
                  icon="description"
                  error={fieldErrors.description}
                  iconType="material"
                />
              </div>

              <FormField
                name="phone"
                label="Teléfono del club *"
                type="tel"
                value={clubData.phone}
                onChange={handleFieldChange('phone')}
                placeholder="+54 11 8765-4321"
                required
                icon="phone"
                error={fieldErrors.phone}
                iconType="material"
              />

              <FormField
                name="email"
                label="Email del club *"
                type="email"
                value={clubData.email}
                onChange={handleFieldChange('email')}
                placeholder="club@email.com"
                required
                icon="alternate_email"
                error={fieldErrors.email}
                iconType="material"
              />

              <FormField
                name="openingTime"
                label="Hora de apertura *"
                type="time"
                value={clubData.openingTime}
                onChange={handleFieldChange('openingTime')}
                required
                icon="schedule"
                error={fieldErrors.openingTime}
                iconType="material"
              />

              <FormField
                name="closingTime"
                label="Hora de cierre *"
                type="time"
                value={clubData.closingTime}
                onChange={handleFieldChange('closingTime')}
                required
                icon="schedule"
                error={fieldErrors.closingTime}
                iconType="material"
              />
            </div>

            <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded-lg mt-6">
              <div className="flex items-start">
                <span className="material-icons text-blue-500 mr-3">info</span>
                <div>
                  <p className="text-sm text-blue-700">
                    <strong>Nota:</strong> Como administrador del club, podrás:
                  </p>
                  <ul className="text-sm text-blue-700 mt-2 ml-4 list-disc">
                    <li>Gestionar canchas y horarios</li>
                    <li>Ver y confirmar reservas</li>
                    <li>Administrar precios y promociones</li>
                    <li>Recibir pagos y generar reportes</li>
                  </ul>
                </div>
              </div>
            </div>

            <div className="flex gap-4 pt-6">
              <button
                type="button"
                onClick={() => navigate('/dashboard')}
                className="flex-1 flex justify-center py-3 px-4 border border-gray-300 text-lg font-medium rounded-xl text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500 transition-all duration-300"
                disabled={isSubmitting}
              >
                <div className="flex items-center">
                  <span className="material-icons mr-2">arrow_back</span>
                  Cancelar
                </div>
              </button>

              <button
                type="submit"
                disabled={isSubmitting}
                className="flex-1 flex justify-center py-3 px-4 border border-transparent text-lg font-bold rounded-xl text-white bg-gradient-to-br from-blue-600 to-cyan-600 hover:from-blue-700 hover:to-cyan-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-all duration-300 transform hover:scale-[1.02]"
              >
                {isSubmitting ? (
                  <div className="flex items-center">
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white mr-3"></div>
                    Creando...
                  </div>
                ) : (
                  <div className="flex items-center">
                    <span className="material-icons mr-2">check_circle</span>
                    Crear Club
                  </div>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default CreateClubPage;