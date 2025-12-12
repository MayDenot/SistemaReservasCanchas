import React, {useState, useEffect} from 'react';
import {Link, useNavigate} from 'react-router-dom';
import {courtService} from "../../api/services/courtService.ts";
import type {CourtResponse} from "../../api/types/court.types.ts";
import {useAuth} from "../../context/AuthContext.tsx";
import Header from "../../components/common/Layout/Header.tsx";
import Footer from "../../components/common/Layout/Footer.tsx";

const HomePage: React.FC = () => {
  const {isAuthenticated} = useAuth();
  const navigate = useNavigate();
  const [featuredCourts, setFeaturedCourts] = useState<CourtResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');

  useEffect(() => {
    loadFeaturedCourts();
  }, []);

  const loadFeaturedCourts = async () => {
    try {
      setLoading(true);
      const courts = await courtService.getAllCourts({limit: 4});
      setFeaturedCourts(courts);
    } catch (error) {
      console.error('Error loading courts:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/courts?search=${encodeURIComponent(searchTerm)}`);
    }
  };

  return (
    <div className="realtive">
      <Header/>
      <div
        className="min-h-screen bg-gradient-to-br from-green-600 via-emerald-500 to-teal-500 relative overflow-hidden">
        {/* Hero Section */}
        <section className="relative min-h-screen flex items-center justify-center px-4 py-16 sm:px-6 lg:px-8">
          <div className="absolute inset-0 bg-gradient-to-br from-green-600/20 via-emerald-500/20 to-teal-500/20"/>

          <div className="relative w-full max-w-6xl mx-auto text-center">
            <div className="animate-fade-in">
              <h1
                className="text-4xl sm:text-5xl md:text-6xl lg:text-7xl font-black text-white mb-6 leading-tight drop-shadow-lg">
                Reserva tu cancha favorita
              </h1>
              <p className="text-lg sm:text-xl md:text-2xl text-white/90 mb-10 max-w-3xl mx-auto leading-relaxed">
                Encuentra y reserva canchas de tenis, pádel, fútbol y más en los mejores clubes
              </p>

              {/* Search Bar */}
              <form onSubmit={handleSearch} className="max-w-2xl mx-auto mb-12">
                <div
                  className="flex flex-col sm:flex-row gap-3 bg-white/10 backdrop-blur-lg rounded-2xl p-2 shadow-2xl transform transition-transform hover:scale-[1.02]">
                  <input
                    type="text"
                    placeholder="Buscar canchas por deporte, ubicación o nombre..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="flex-1 bg-transparent border-none text-white placeholder-white/70 px-6 py-4 text-lg rounded-xl focus:outline-none focus:ring-2 focus:ring-white/30"
                  />
                  <button
                    type="submit"
                    className="bg-white text-green-600 hover:bg-gray-100 font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 active:scale-95 shadow-lg flex items-center justify-center gap-2"
                  >
                    <span className="material-icons">search</span>
                    Buscar
                  </button>
                </div>
              </form>

              {!isAuthenticated && (
                <div className="flex flex-wrap gap-4 justify-center">
                  <Link
                    to="/register"
                    className="bg-white text-green-600 hover:bg-gray-50 font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 hover:shadow-2xl shadow-lg flex items-center gap-2"
                  >
                    <span className="material-icons">person_add</span>
                    Crear cuenta
                  </Link>
                  <Link
                    to="/login"
                    className="bg-white/15 backdrop-blur-sm text-white border-2 border-white/30 hover:bg-white/25 hover:border-white/50 font-bold px-8 py-4 rounded-xl transition-all duration-300 transform hover:scale-105 hover:shadow-2xl flex items-center gap-2"
                  >
                    <span className="material-icons">login</span>
                    Iniciar sesión
                  </Link>
                </div>
              )}
            </div>
          </div>
        </section>

        {/* Features Section */}
        <section className="bg-white py-20 px-4 relative">
          <div className="absolute -top-16 left-0 right-0 h-32 bg-white clip-path-polygon"></div>

          <div className="max-w-7xl mx-auto">
            <h2 className="text-3xl sm:text-4xl md:text-5xl font-black text-center text-gray-900 mb-16">
              ¿Por qué elegirnos?
            </h2>

            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {[
                {
                  icon: 'calendar_today',
                  title: 'Reserva fácil',
                  desc: 'Reserva en pocos clics, las 24 horas del día',
                  color: 'from-green-500 to-emerald-500'
                },
                {
                  icon: 'location_on',
                  title: 'Varias ubicaciones',
                  desc: 'Canchas en los mejores clubes de la ciudad',
                  color: 'from-blue-500 to-cyan-500'
                },
                {
                  icon: 'security',
                  title: 'Pago seguro',
                  desc: 'Transacciones protegidas y garantizadas',
                  color: 'from-orange-500 to-amber-500'
                },
                {
                  icon: 'access_time',
                  title: 'Disponibilidad en tiempo real',
                  desc: 'Consulta horarios disponibles al instante',
                  color: 'from-teal-500 to-cyan-500'
                }
              ].map((feature, index) => (
                <div
                  key={index}
                  className="bg-white card hover:shadow-sport hover:-translate-y-3 group"
                >
                  <div
                    className={`w-20 h-20 bg-gradient-to-br ${feature.color} rounded-2xl flex items-center justify-center mb-6 transform group-hover:scale-110 group-hover:rotate-6 transition-transform duration-500 shadow-lg`}>
                    <span className="material-icons text-3xl text-white">{feature.icon}</span>
                  </div>
                  <h3 className="text-xl font-bold text-gray-300 mb-3">{feature.title}</h3>
                  <p className="text-gray-300">{feature.desc}</p>
                </div>
              ))}
            </div>
          </div>
        </section>

        {/* Featured Courts Section */}
        <section className="bg-gray-50 py-20 px-4">
          <div className="max-w-7xl mx-auto">
            <div className="flex justify-between items-center mb-12">
              <h2 className="text-3xl sm:text-4xl md:text-5xl font-black text-gray-900">
                Canchas destacadas
              </h2>
              <Link
                to="/courts"
                className="text-green-600 hover:text-green-700 font-bold flex items-center gap-2 group"
              >
                Ver todas
                <span className="material-icons group-hover:translate-x-2 transition-transform">arrow_forward</span>
              </Link>
            </div>

            {loading ? (
              <div className="text-center py-12">
                <div className="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600"></div>
                <p className="mt-4 text-gray-600">Cargando canchas...</p>
              </div>
            ) : (
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
                {featuredCourts.map((court) => (
                  <div
                    key={court.id.toString()}
                    className="group relative bg-white/10 backdrop-blur-xl border border-white/20 rounded-3xl overflow-hidden
                 shadow-lg hover:shadow-2xl transition-all duration-500 transform hover:-translate-y-2"
                  >
                    {/* Imagen / Ícono */}
                    <div
                      className="relative h-40 bg-gradient-to-br from-green-500/80 to-emerald-600/80 flex items-center justify-center">
                      <div className="w-20 h-20 rounded-3xl bg-white/20 backdrop-blur-md flex items-center justify-center
                        shadow-md transform group-hover:scale-105 group-hover:rotate-3 transition-all duration-500">
                        {/* Icono según el deporte (puedes personalizarlo según court.type) */}
                        {court.type?.toLowerCase().includes('tenis') ? (
                          <span className="material-icons text-4xl text-white">sports_tennis</span>
                        ) : court.type?.toLowerCase().includes('fútbol') || court.type?.toLowerCase().includes('futbol') ? (
                          <span className="material-icons text-4xl text-white">sports_soccer</span>
                        ) : court.type?.toLowerCase().includes('pádel') || court.type?.toLowerCase().includes('padel') ? (
                          <span className="material-icons text-4xl text-white">sports</span>
                        ) : (
                          <span className="material-icons text-4xl text-white">sports</span>
                        )}
                      </div>
                    </div>

                    {/* Contenido */}
                    <div className="p-4">
                      <h3 className="text-lg font-bold text-black mb-2 drop-shadow">
                        {court.name}
                      </h3>

                      <p className="text-black/70 mb-4 flex items-center gap-2 text-base">
                        <span className="material-icons text-green-600">payments</span>
                        <span className="font-semibold">${Number(court.pricePerHour).toFixed(2)}</span>/hora
                      </p>

                      <div className="flex gap-3">
                        <Link
                          to={`/courts/${court.id}`}
                          className="flex-1 bg-white text-green-600 font-semibold py-2 px-3 rounded-lg text-center
                       hover:bg-gray-200 hover:text-green-600 transition-all shadow-sm hover:shadow-md text-sm flex items-center justify-center gap-1"
                        >
                          <span className="material-icons text-sm">visibility</span>
                          Detalles
                        </Link>

                        {isAuthenticated && (
                          <Link
                            to={`/reservations/new?courtId=${court.id}`}
                            className="flex-1 bg-green-600 text-white font-semibold py-2 px-3 rounded-lg text-center
                         hover:bg-green-700 hover:text-white transition-all shadow-sm hover:shadow-md text-sm flex items-center justify-center gap-1"
                          >
                            <span className="material-icons text-sm">book_online</span>
                            Reservar
                          </Link>
                        )}
                      </div>
                    </div>

                    {/* Glow en hover */}
                    <div
                      className="absolute inset-0 pointer-events-none opacity-0 group-hover:opacity-100 transition-opacity duration-700
                   bg-gradient-to-br from-green-400/10 via-transparent to-transparent"
                    ></div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* CTA Section */}
        <section className="bg-gradient-to-br from-blue-600 via-cyan-600 to-teal-600 py-16 px-4">
          <div className="max-w-4xl mx-auto text-center">
            <h2 className="text-3xl sm:text-4xl md:text-5xl font-black text-white mb-4">
              ¿Eres dueño de un club?
            </h2>
            <p className="text-lg sm:text-xl text-white/90 mb-8">
              Registra tu club y empieza a recibir reservas hoy mismo
            </p>
            <Link
              to="/register?type=club_owner"
              className="inline-flex items-center gap-2 bg-white text-blue-600 hover:bg-gray-100 font-bold text-base sm:text-lg px-6 py-3 rounded-xl transition-all duration-300 transform hover:scale-105 hover:shadow-xl shadow-md active:scale-95"
            >
              <span className="material-icons">business</span>
              Registrar club
            </Link>
          </div>
        </section>
      </div>
      <Footer/>
    </div>
  );
};

export default HomePage;