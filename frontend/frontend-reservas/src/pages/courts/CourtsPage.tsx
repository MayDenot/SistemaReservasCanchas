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
  const [selectedSport, setSelectedSport] = useState<string>('all');
  const [selectedType, setSelectedType] = useState<string>('all');
  const [minPrice, setMinPrice] = useState<number>(0);
  const [maxPrice, setMaxPrice] = useState<number>(5000);
  const [dateFilter, setDateFilter] = useState<string>('');

  const types = ['OUTDOOR', 'INDOOR'];

  useEffect(() => {
    loadCourts();
  }, []);

  useEffect(() => {
    filterCourts();
  }, [courts, searchTerm, selectedSport, selectedType, minPrice, maxPrice]);

  const loadCourts = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await courtService.getAllCourts();
      setCourts(data);
      setFilteredCourts(data);
    } catch (err: any) {
      setError(err.message || 'Error al cargar las canchas');
      console.error('Error loading courts:', err);
    } finally {
      setLoading(false);
    }
  };

  const filterCourts = useCallback(() => {
    let filtered = [...courts];

    if (searchTerm) {
      filtered = filtered.filter(court =>
        court.name.toLowerCase().includes(searchTerm.toLowerCase())
      );
    }

    if (selectedType !== 'all') {
      filtered = filtered.filter(court => court.type === selectedType);
    }

    filtered = filtered.filter(court =>
      Number(court.pricePerHour) >= minPrice && Number(court.pricePerHour) <= maxPrice
    );

    setFilteredCourts(filtered);
  }, [courts, searchTerm, selectedSport, selectedType, minPrice, maxPrice]);

  const handleClearFilters = () => {
    setSearchTerm('');
    setSelectedSport('all');
    setSelectedType('all');
    setMinPrice(0);
    setMaxPrice(5000);
    setDateFilter('');
  };

  const getTypeIcon = (type: string) => {
    switch (type) {
      case 'OUTDOOR': return '☀️';
      case 'INDOOR': return '🏠';
      default: return '❓';
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
            <span className="text-3xl">⚠️</span>
          </div>
          <h3 className="text-2xl font-bold text-gray-900 mb-3">Error al cargar las canchas</h3>
          <p className="text-gray-600 mb-8">{error}</p>
          <button
            onClick={loadCourts}
            className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-6 rounded-xl transition-colors"
          >
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
          <div className="absolute top-0 left-0 right-0 h-1 bg-gradient-to-r from-green-500 via-emerald-500 to-teal-500"></div>
          <div className="text-center">
            <h1 className="text-4xl md:text-5xl font-black text-gray-900 mb-4">
              Canchas Disponibles
            </h1>
            <p className="text-xl text-gray-600">
              Encuentra la cancha perfecta para tu próximo partido
            </p>
          </div>
        </div>

        <div className="lg:grid lg:grid-cols-4 gap-8">
          {/* Sidebar de filtros */}
          <aside className="lg:col-span-1 mb-8 lg:mb-0">
            <div className="bg-white rounded-2xl shadow-xl p-6 sticky top-8">
              <h3 className="text-xl font-bold text-gray-900 mb-6 flex items-center gap-3">
                <span className="text-2xl">🔍</span>
                Filtros
              </h3>

              {/* Búsqueda */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">Buscar</label>
                <input
                  type="text"
                  placeholder="Nombre de cancha..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-green-500 focus:border-green-500 transition-colors"
                />
              </div>

              {/* Tipo */}
              <div className="mb-6">
                <label className="block text-sm font-medium text-gray-700 mb-2">Tipo</label>
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
                <label className="block text-sm font-medium text-gray-700 mb-2">
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
                <label className="block text-sm font-medium text-gray-700 mb-2">Fecha</label>
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
                  className="flex-1 bg-gray-200 hover:bg-gray-300 text-gray-800 font-bold py-3 px-4 rounded-xl transition-colors"
                >
                  Limpiar
                </button>
                <button
                  onClick={filterCourts}
                  className="flex-1 bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-4 rounded-xl transition-colors"
                >
                  Aplicar
                </button>
              </div>
            </div>
          </aside>

          {/* Lista de canchas */}
          <main className="lg:col-span-3">
            <div className="bg-white rounded-2xl shadow-xl p-6 mb-6">
              <p className="text-gray-700">
                Mostrando <strong className="text-green-600">{filteredCourts.length}</strong> de{' '}
                <strong className="text-gray-900">{courts.length}</strong> canchas
              </p>
            </div>

            {filteredCourts.length === 0 ? (
              <div className="bg-white rounded-2xl shadow-xl p-12 text-center">
                <div className="w-24 h-24 bg-gradient-to-br from-gray-200 to-gray-300 rounded-full flex items-center justify-center mx-auto mb-6">
                  <span className="text-4xl">🔍</span>
                </div>
                <h3 className="text-2xl font-bold text-gray-900 mb-3">No se encontraron canchas</h3>
                <p className="text-gray-600 mb-8">Intenta cambiar los filtros de búsqueda</p>
                <button
                  onClick={handleClearFilters}
                  className="bg-green-600 hover:bg-green-700 text-white font-bold py-3 px-8 rounded-xl transition-colors"
                >
                  Limpiar filtros
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                {filteredCourts.map((court) => (
                  <div
                    key={court.id.toString()}
                    className="bg-white rounded-2xl shadow-xl hover:shadow-2xl transition-all duration-500 transform hover:-translate-y-2 border border-gray-100 overflow-hidden"
                  >
                    <div className="h-48 bg-gradient-to-br from-green-400 to-emerald-500 flex items-center justify-center relative">
                      <span className="text-5xl">{getTypeIcon(court.type)}</span>
                      <div className="absolute top-4 right-4">
                        <span className={`px-4 py-2 rounded-full text-white font-bold text-sm ${court.isActive ? 'bg-gradient-to-r from-green-500 to-emerald-500' : 'bg-gradient-to-r from-red-500 to-pink-500'}`}>
                          {court.isActive ? 'Disponible' : 'No disponible'}
                        </span>
                      </div>
                    </div>

                    <div className="p-6">
                      <h3 className="text-xl font-bold text-gray-900 mb-3">{court.name}</h3>
                      <p className="text-gray-600 mb-6">
                        Cancha {getTypeName(court.type)} - Club ID: {court.clubId.toString()}
                      </p>

                      <div className="grid grid-cols-2 gap-4 mb-6">
                        <div className="flex items-center gap-2 text-gray-700">
                          <span className="text-xl">{getTypeIcon(court.type)}</span>
                          <span>{getTypeName(court.type)}</span>
                        </div>
                        <div className="flex items-center gap-2 text-gray-700">
                          <span className="text-xl">💰</span>
                          <span>${Number(court.pricePerHour).toFixed(2)}/hora</span>
                        </div>
                      </div>

                      <div className="flex gap-3">
                        <Link
                          to={`/courts/${court.id}`}
                          className="flex-1 bg-blue-600 hover:bg-blue-700 text-white font-bold py-3 px-4 rounded-lg text-center transition-colors"
                        >
                          Detalles
                        </Link>
                        <Link
                          to={`/reservations/new?courtId=${court.id}`}
                          className={`flex-1 font-bold py-3 px-4 rounded-lg text-center transition-colors ${court.isActive ? 'bg-gradient-to-r from-green-500 to-emerald-500 hover:from-green-600 hover:to-emerald-600 text-white' : 'bg-gray-200 text-gray-500 cursor-not-allowed'}`}
                          onClick={(e) => !court.isActive && e.preventDefault()}
                        >
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