import React, { useState, useEffect, useCallback } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import type { CourtResponse } from "../../api/types/court.types.ts";
import { courtService } from "../../api/services/courtService.ts";

const CourtsPage: React.FC = () => {
  const [searchParams] = useSearchParams();
  const [courts, setCourts] = useState<CourtResponse[]>([]);
  const [filteredCourts, setFilteredCourts] = useState<CourtResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [searchTerm, setSearchTerm] = useState(searchParams.get('search') || '');
  const [_selectedSport, setSelectedSport] = useState<string>('all');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [minPrice, setMinPrice] = useState<number>(0);
  const [maxPrice, setMaxPrice] = useState<number>(5000);
  const [dateFilter, setDateFilter] = useState<string>('');

  const types = ['OUTDOOR', 'INDOOR'];

  const handleClearFilters = () => {
    setSearchTerm('');
    setSelectedSport('all');
    setSelectedType('all');
    setMinPrice(0);
    setMaxPrice(5000);
    setDateFilter('');
  };

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        setError(null);

        console.log('📥 Cargando canchas...');
        const courtsData = await courtService.getAllCourts();
        console.log(`✅ ${courtsData.length} canchas cargadas`);

        // Verificar datos recibidos
        if (courtsData.length > 0) {
          const firstCourt = courtsData[0];
          console.log('📋 Ejemplo de datos recibidos:', {
            nombre: firstCourt.name,
            clubName: firstCourt.clubName,
            clubId: firstCourt.clubId,
            tipo: firstCourt.type,
            precio: firstCourt.pricePerHour
          });
        }

        setCourts(courtsData);
        setFilteredCourts(courtsData);

      } catch (err: any) {
        console.error('❌ Error cargando canchas:', err);
        setError(err.message || 'Error al cargar las canchas');
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Filtrar automáticamente
  useEffect(() => {
    filterCourts();
  }, [searchTerm, selectedType, minPrice, maxPrice, courts]);

  const filterCourts = useCallback(() => {
    let filtered = [...courts];

    if (searchTerm) {
      filtered = filtered.filter(court =>
        court.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (court.clubName && court.clubName.toLowerCase().includes(searchTerm.toLowerCase()))
      );
    }

    if (selectedType !== 'all') {
      filtered = filtered.filter(court => court.type === selectedType);
    }

    filtered = filtered.filter(court =>
      Number(court.pricePerHour) >= minPrice && Number(court.pricePerHour) <= maxPrice
    );

    setFilteredCourts(filtered);
  }, [courts, searchTerm, selectedType, minPrice, maxPrice]);

  const getClubDisplayName = (court: CourtResponse): string => {
    // Prioridad 1: clubName del backend
    if (court.clubName && court.clubName.trim() !== '' && !court.clubName.startsWith('Club #')) {
      return court.clubName;
    }

    // Prioridad 2: Si existe clubId, mostrar formato genérico
    if (court.clubId) {
      return `Club #${court.clubId}`;
    }

    // Fallback
    return "Sin club";
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'OUTDOOR': return 'wb_sunny';
      case 'INDOOR': return 'home';
      default: return 'help';
    }
  };

  const getTypeName = (type: string) => {
    const typeNames: Record<string, string> = {
      'OUTDOOR': 'Exterior',
      'INDOOR': 'Interior'
    };
    return typeNames[type] || type;
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100">
        <div className="text-center">
          <div className="inline-block animate-spin rounded-full h-16 w-16 border-b-2 border-green-600"></div>
          <p className="mt-4 text-gray-600 text-lg">Cargando canchas...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-gray-50 to-gray-100 px-4">
        <div className="bg-white rounded-2xl p-8 max-w-md text-center shadow-2xl">
          <div className="w-20 h-20 bg-gradient-to-br from-red-500 to-pink-500 rounded-full flex items-center justify-center mx-auto mb-6">
            <span className="material-icons text-3xl text-white">error</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar las canchas</h3>
          <p className="text-gray-600 mb-8">{error}</p>
          <button
            onClick={() => window.location.reload()}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors flex items-center gap-2 justify-center mx-auto"
          >
            <span className="material-icons">refresh</span>
            Reintentar
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 to-gray-100 py-8 px-4">
      <div className="max-w-7xl mx-auto">
        {/* Header */}
        <div className="bg-white rounded-3xl shadow-xl p-8 mb-8 relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-sport"></div>
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              Canchas Disponibles
            </h1>
            <p className="text-xl text-gray-600 flex items-center justify-center gap-2">
              <span className="material-icons text-green-600">search</span>
              Encuentra la cancha perfecta para tu próximo partido
            </p>
          </div>
        </div>

        <div className="lg:grid lg:grid-cols-4 gap-8">
          {/* Sidebar de filtros */}
          <aside className="lg:col-span-1 mb-8 lg:mb-0">
            <div className="bg-white rounded-2xl shadow-xl p-6 sticky top-8">
              <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-3">
                <span className="material-icons text-green-600">filter_list</span>
                Filtros
              </h3>

              {/* Búsqueda */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                  <span className="material-icons text-gray-500 text-sm">search</span>
                  Buscar
                </label>
                <input
                  type="text"
                  placeholder="Nombre de cancha o club..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                />
              </div>

              {/* Tipo */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                  <span className="material-icons text-gray-500 text-sm">category</span>
                  Tipo
                </label>
                <select
                  value={selectedType}
                  onChange={(e) => setSelectedType(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                >
                  <option value="all">Todos los tipos</option>
                  {types.map(type => (
                    <option key={type} value={type}>
                      {getTypeName(type)}
                    </option>
                  ))}
                </select>
              </div>

              {/* Precio */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                  <span className="material-icons text-gray-500 text-sm">payments</span>
                  Precio: ${minPrice} - ${maxPrice}
                </label>
                <div className="space-y-4">
                  <input
                    type="range"
                    min="0"
                    max="5000"
                    step="100"
                    value={minPrice}
                    onChange={(e) => setMinPrice(Number(e.target.value))}
                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                  />
                  <input
                    type="range"
                    min="0"
                    max="5000"
                    step="100"
                    value={maxPrice}
                    onChange={(e) => setMaxPrice(Number(e.target.value))}
                    className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer"
                  />
                </div>
                <div className="flex justify-between text-sm text-gray-600 mt-2">
                  <span>$0</span>
                  <span>$2500</span>
                  <span>$5000</span>
                </div>
              </div>

              {/* Fecha */}
              <div className="mb-8">
                <label className="block text-sm font-medium text-gray-700 mb-2 flex items-center gap-2">
                  <span className="material-icons text-gray-500 text-sm">event</span>
                  Fecha
                </label>
                <input
                  type="date"
                  value={dateFilter}
                  onChange={(e) => setDateFilter(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                  min={new Date().toISOString().split('T')[0]}
                />
              </div>

              {/* Botones */}
              <div className="flex gap-3">
                <button
                  onClick={handleClearFilters}
                  className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-4 rounded-xl transition-colors flex items-center justify-center gap-2"
                >
                  <span className="material-icons">clear</span>
                  Limpiar
                </button>
                <button
                  onClick={filterCourts}
                  className="flex-1 bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-4 rounded-xl transition-colors flex items-center justify-center gap-2"
                >
                  <span className="material-icons">check</span>
                  Aplicar
                </button>
              </div>
            </div>
          </aside>

          {/* Lista de canchas */}
          <main className="lg:col-span-3">
            <div className="bg-white rounded-2xl shadow-xl p-6 mb-6">
              <div className="flex justify-between items-center">
                <p className="text-gray-700 flex items-center gap-2">
                  <span className="material-icons text-green-600">list</span>
                  Mostrando <strong className="text-green-600">{filteredCourts.length}</strong> de{' '}
                  <strong className="text-gray-900">{courts.length}</strong> canchas
                </p>
              </div>
            </div>

            {filteredCourts.length === 0 ? (
              <div className="bg-white rounded-2xl shadow-xl p-12 text-center">
                <div className="w-24 h-24 bg-gradient-to-br from-gray-200 to-gray-300 rounded-full flex items-center justify-center mx-auto mb-6">
                  <span className="material-icons text-4xl text-gray-600">search_off</span>
                </div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">No se encontraron canchas</h3>
                <p className="text-gray-600 mb-8">Intenta cambiar los filtros de búsqueda</p>
                <button
                  onClick={handleClearFilters}
                  className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-8 rounded-xl transition-colors flex items-center gap-2 justify-center mx-auto"
                >
                  <span className="material-icons">clear_all</span>
                  Limpiar filtros
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {filteredCourts.map((court) => (
                  <div
                    key={court.id.toString()}
                    className="bg-white rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-500 transform hover:-translate-y-2 border border-gray-100 overflow-hidden group"
                  >
                    <div className="h-48 bg-gradient-sport flex items-center justify-center relative group-hover:scale-105 transition-transform duration-500">
                      <span className="material-icons text-6xl text-white group-hover:scale-110 transition-transform duration-500">
                        {getTypeIcon(court.type)}
                      </span>
                      <div className="absolute top-4 right-4">
                        <span className={`px-4 py-2 rounded-full text-white font-bold text-sm flex items-center gap-2 ${court.isActive ? 'bg-gradient-sport' : 'bg-gradient-to-r from-red-500 to-pink-500'}`}>
                          <span className="material-icons text-sm">circle</span>
                          {court.isActive ? 'Disponible' : 'No disponible'}
                        </span>
                      </div>
                    </div>

                    <div className="p-6">
                      <h3 className="text-xl font-bold text-gray-900 mb-2">{court.name}</h3>

                      {/* Información del club */}
                      <div className="flex items-center gap-2 mb-4">
                        <span className="material-icons text-gray-600">business</span>
                        <span className="text-gray-700 font-medium">
                          {getClubDisplayName(court)}
                        </span>
                      </div>

                      <p className="text-gray-600 mb-6">
                        Cancha {getTypeName(court.type)}
                      </p>

                      <div className="grid grid-cols-2 gap-4 mb-6">
                        <div className="flex items-center gap-2 text-gray-700">
                          <span className="material-icons text-green-600">{getTypeIcon(court.type)}</span>
                          <span>{getTypeName(court.type)}</span>
                        </div>
                        <div className="flex items-center gap-2 text-gray-700">
                          <span className="material-icons text-green-600">payments</span>
                          <span className="font-semibold">${Number(court.pricePerHour).toFixed(2)}/hora</span>
                        </div>
                      </div>

                      <div className="flex gap-3">
                        <Link
                          to={`/courts/${court.id}`}
                          className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 hover:text-green-600 font-bold py-3 px-4 rounded-lg text-center transition-all duration-300 hover:scale-105 active:scale-95 flex items-center justify-center gap-2"
                        >
                          <span className="material-icons text-sm">visibility</span>
                          Detalles
                        </Link>
                        <Link
                          to={`/reservations/new?courtId=${court.id}`}
                          className={`flex-1 font-bold py-3 px-4 rounded-lg text-center transition-all duration-300 hover:scale-105 active:scale-95 flex items-center justify-center gap-2 ${court.isActive
                            ? 'bg-green-600 hover:bg-green-700 text-white'
                            : 'bg-gray-200 text-gray-400 cursor-not-allowed'}`}
                          onClick={(e) => !court.isActive && e.preventDefault()}
                        >
                          <span className="material-icons text-sm">book_online</span>
                          Reservar
                        </Link>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </main>
        </div>
      </div>
    </div>
  );
};

export default CourtsPage;