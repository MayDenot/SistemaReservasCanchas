import React from 'react';

const Footer: React.FC = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="bg-gradient-to-br from-gray-900 to-gray-950 text-white pt-12 pb-8 px-4">
      <div className="max-w-7xl mx-auto">

        {/* Sección principal */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">

          {/* Logo y descripción */}
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-12 h-12 bg-gradient-to-br from-green-500 to-emerald-500 rounded-xl flex items-center justify-center">
                <span className="material-icons text-2xl text-white">sports</span>
              </div>
              <h2 className="text-2xl font-black bg-gradient-to-r from-green-400 to-emerald-400 bg-clip-text text-transparent">
                Reservas
              </h2>
            </div>
            <p className="text-gray-400 text-sm leading-relaxed">
              La plataforma líder para reservar canchas deportivas. Conectamos jugadores con los mejores clubes de la ciudad.
            </p>
            <div className="flex gap-4 pt-4">
              {[
                { platform: 'twitter', icon: 'X', color: 'hover:bg-blue-400', bgColor: 'bg-gray-800' },
                { platform: 'facebook', icon: 'f', color: 'hover:bg-blue-600', bgColor: 'bg-gray-800' },
                { platform: 'instagram', icon: '📸', color: 'hover:bg-gradient-to-br hover:from-pink-500 hover:to-purple-500', bgColor: 'bg-gray-800' },
                { platform: 'linkedin', icon: 'in', color: 'hover:bg-blue-700', bgColor: 'bg-gray-800' }
              ].map((social) => (
                <a
                  key={social.platform}
                  href="#"
                  className={`w-10 h-10 ${social.bgColor} ${social.color} rounded-full flex items-center justify-center transition-all duration-300 hover:scale-110`}
                  aria-label={`Síguenos en ${social.platform}`}
                >
                  <span className="text-white font-semibold">
                    {social.icon}
                  </span>
                </a>
              ))}
            </div>
          </div>

          {/* Enlaces rápidos */}
          <div>
            <h3 className="text-lg font-bold mb-6 pb-2 border-b-2 border-green-500 inline-block">
              Enlaces Rápidos
            </h3>
            <ul className="space-y-3">
              {[
                { label: 'Buscar Canchas', href: '/courts', icon: 'search' },
                { label: 'Mis Reservas', href: '/reservations', icon: 'calendar_today' },
                { label: 'Clubes Destacados', href: '/clubs', icon: 'star' },
                { label: 'Cómo Funciona', href: '/how-it-works', icon: 'help' },
                { label: 'Precios', href: '/pricing', icon: 'attach_money' },
              ].map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-gray-400 hover:text-green-400 hover:pl-2 transition-all duration-300 flex items-center gap-2"
                  >
                    <span className="material-icons text-green-500 text-sm">{link.icon}</span>
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Para Propietarios */}
          <div>
            <h3 className="text-lg font-bold mb-6 pb-2 border-b-2 border-emerald-500 inline-block">
              Para Propietarios
            </h3>
            <ul className="space-y-3">
              {[
                { label: 'Registrar Club', href: '/register-club', icon: 'business' },
                { label: 'Panel de Control', href: '/dashboard', icon: 'dashboard' },
                { label: 'Tarifas', href: '/owner-pricing', icon: 'payments' },
                { label: 'Recursos', href: '/resources', icon: 'folder' },
                { label: 'Soporte', href: '/support', icon: 'support' },
              ].map((link) => (
                <li key={link.label}>
                  <a
                    href={link.href}
                    className="text-gray-400 hover:text-emerald-400 hover:pl-2 transition-all duration-300 flex items-center gap-2"
                  >
                    <span className="material-icons text-emerald-500 text-sm">{link.icon}</span>
                    {link.label}
                  </a>
                </li>
              ))}
            </ul>
            <div className="mt-8 bg-gradient-to-r from-gray-800 to-gray-900 p-4 rounded-xl border border-gray-700">
              <div className="flex items-start gap-3 mb-4">
                <span className="material-icons text-emerald-400 mt-1">business</span>
                <div className="flex-1">
                  <p className="text-sm font-bold mb-1">¿Eres dueño de un club?</p>
                  <p className="text-xs text-gray-400">
                    Registra tu club y empieza a recibir reservas hoy mismo
                  </p>
                </div>
              </div>
              <div className="text-center">
                <a
                  href="/register-club"
                  className="inline-flex items-center justify-center gap-2 bg-gradient-to-r from-green-600 to-emerald-600 hover:from-green-700 hover:to-emerald-700 text-white hover:text-white font-bold py-2 px-6 rounded-lg text-sm transition-all duration-300 transform hover:scale-105 w-full md:w-auto"
                >
                  <span className="material-icons text-base">add_business</span>
                  Registrar club
                </a>
              </div>
            </div>
          </div>

          {/* Contacto */}
          <div>
            <h3 className="text-lg font-bold mb-6 pb-2 border-b-2 border-teal-500 inline-block">
              Contacto
            </h3>
            <ul className="space-y-4">
              <li className="flex items-start gap-3">
                <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center flex-shrink-0">
                  <span className="material-icons text-green-400">email</span>
                </div>
                <div>
                  <p className="font-bold text-sm">Email</p>
                  <a href="mailto:soporte@reservas.com" className="text-gray-400 text-sm hover:text-green-400 transition-colors">
                    soporte@reservas.com
                  </a>
                </div>
              </li>
              <li className="flex items-start gap-3">
                <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center flex-shrink-0">
                  <span className="material-icons text-green-400">phone</span>
                </div>
                <div>
                  <p className="font-bold text-sm">Teléfono</p>
                  <a href="tel:+541112345678" className="text-gray-400 text-sm hover:text-green-400 transition-colors">
                    +54 11 1234-5678
                  </a>
                </div>
              </li>
              <li className="flex items-start gap-3">
                <div className="w-10 h-10 bg-gray-800 rounded-lg flex items-center justify-center flex-shrink-0">
                  <span className="material-icons text-green-400">location_on</span>
                </div>
                <div>
                  <p className="font-bold text-sm">Dirección</p>
                  <p className="text-gray-400 text-sm">
                    Av. Corrientes 1234<br />
                    Buenos Aires, Argentina
                  </p>
                </div>
              </li>
            </ul>
          </div>
        </div>

        {/* Línea divisoria */}
        <div className="border-t border-gray-800 mb-8"></div>

        {/* Pie inferior */}
        <div className="flex flex-col md:flex-row justify-between items-center gap-6">
          <div className="text-gray-500 text-sm text-center md:text-left">
            <p>© {currentYear} Reservas. Todos los derechos reservados.</p>
            <p className="mt-1 flex items-center justify-center md:justify-start gap-1">
              <span className="material-icons text-xs">sports</span>
              <span>Deportes • Comunidad • Tecnología</span>
            </p>
          </div>

          <div className="flex flex-wrap gap-4 justify-center">
            {[
              { label: 'Términos de Uso', icon: 'description' },
              { label: 'Política de Privacidad', icon: 'privacy_tip' },
              { label: 'Cookies', icon: 'cookie' },
              { label: 'Aviso Legal', icon: 'gavel' }
            ].map((item) => (
              <a
                key={item.label}
                href="#"
                className="text-gray-500 hover:text-green-400 text-sm transition-colors flex items-center gap-1"
              >
                <span className="material-icons text-xs">{item.icon}</span>
                {item.label}
              </a>
            ))}
          </div>

          <div className="flex items-center gap-2 text-gray-500 text-sm">
            <div className="flex items-center gap-1">
              <span className="material-icons text-green-500 text-sm">flash_on</span>
              <span>Hecho con</span>
            </div>
            <div className="w-1 h-1 bg-green-500 rounded-full"></div>
            <div className="flex items-center gap-1">
              <span className="material-icons text-blue-400 text-sm">code</span>
              <span>React & Tailwind CSS</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;