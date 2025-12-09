import React from 'react';
import type { CourtResponse } from '../../api/types/court.types';
import './CourtCard.css';

interface CourtCardProps {
  court: CourtResponse;
  onClick: (courtId: bigint) => void;
}

const CourtCard: React.FC<CourtCardProps> = ({ court, onClick }) => {

  // Función para formatear el precio (bigint a string legible)
  const formatPrice = (price: bigint): string => {
    return price.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
  };

  // Traducción del tipo de cancha
  const getCourtTypeLabel = (type: "OUTDOOR" | "INDOOR"): string => {
    return type === "OUTDOOR" ? "Al Aire Libre" : "Cubierta";
  };

  // Estado de la cancha (activa/inactiva)
  const getStatusLabel = (isActive: boolean): string => {
    return isActive ? "Disponible" : "No Disponible";
  };

  return (
    <div
      className={`court-card ${!court.isActive ? 'court-inactive' : ''}`}
      onClick={() => court.isActive && onClick(court.id)}
    >
      <div className="court-image">
        {!court.isActive && (
          <div className="court-status-badge">No Disponible</div>
        )}
      </div>

      <div className="court-info">
        <div className="court-header">
          <h3>{court.name}</h3>
          <span className={`court-type ${court.type.toLowerCase()}`}>
            {getCourtTypeLabel(court.type)}
          </span>
        </div>

        <div className="court-details">
          <div className="detail-row">
            <span className="detail-label">Precio:</span>
            <span className="detail-value">${formatPrice(court.pricePerHour)} / hora</span>
          </div>

          <div className="detail-row">
            <span className="detail-label">Estado:</span>
            <span className={`status-indicator ${court.isActive ? 'active' : 'inactive'}`}>
              {getStatusLabel(court.isActive)}
            </span>
          </div>
        </div>

        {/* Botón de acción */}
        <div className="court-actions">
          <button
            className={`reserve-btn ${!court.isActive ? 'disabled' : ''}`}
            onClick={(e) => {
              e.stopPropagation();
              court.isActive && onClick(court.id);
            }}
            disabled={!court.isActive}
          >
            {court.isActive ? 'Ver Detalles' : 'No Disponible'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default CourtCard;