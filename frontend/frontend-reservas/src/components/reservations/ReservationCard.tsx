import React from 'react';
import type {ReservationResponse} from '../../api/types/reservation.types';
import './ReservationCard.css';

interface ReservationCardProps {
  reservation: ReservationResponse;
  onEdit?: (id: bigint) => void;
  onCancel?: (id: bigint) => void;
  onViewDetails?: (id: bigint) => void;
}

const ReservationCard: React.FC<ReservationCardProps> = ({
                                                           reservation,
                                                           onEdit,
                                                           onCancel,
                                                           onViewDetails
                                                         }) => {

  // Formatear fecha y hora
  const formatDate = (date: Date): string => {
    return new Date(date).toLocaleDateString('es-ES', {
      weekday: 'long',
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    });
  };

  const formatTime = (date: Date): string => {
    return new Date(date).toLocaleTimeString('es-ES', {
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const formatDateTime = (date: Date): string => {
    return new Date(date).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // Traducir estados
  const getStatusLabel = (status: ReservationResponse['status']): string => {
    const statusMap = {
      'PENDING': 'Pendiente',
      'CONFIRMED': 'Confirmada',
      'CANCELLED': 'Cancelada'
    };
    return statusMap[status];
  };

  const getPaymentStatusLabel = (status: ReservationResponse['paymentStatus']): string => {
    const statusMap = {
      'PENDING': 'Pendiente',
      'CONFIRMED': 'Confirmado',
      'FAILED': 'Fallido',
      'CANCELLED': 'Cancelado',
      'REFUNDED': 'Reembolsado'
    };
    return statusMap[status];
  };

  // Calcular duración
  const calculateDuration = (start: Date, end: Date): string => {
    const startDate = new Date(start);
    const endDate = new Date(end);
    const durationMs = endDate.getTime() - startDate.getTime();
    const hours = Math.floor(durationMs / (1000 * 60 * 60));
    const minutes = Math.floor((durationMs % (1000 * 60 * 60)) / (1000 * 60));

    if (hours === 0) return `${minutes} min`;
    if (minutes === 0) return `${hours} h`;
    return `${hours}h ${minutes}min`;
  };

  // Determinar qué acciones están disponibles
  const canEdit = reservation.status === 'PENDING';
  const canCancel = reservation.status === 'PENDING' || reservation.status === 'CONFIRMED';
  const isUpcoming = new Date(reservation.startTime) > new Date();

  return (
    <div className={`reservation-card status-${reservation.status.toLowerCase()}`}>
      <div className="reservation-header">
        <div className="header-left">
          <h4 className="reservation-title">
            Reserva #{reservation.id.toString().slice(-8)}
          </h4>
          <span className="reservation-date">
            Creada: {formatDateTime(reservation.createdAt)}
          </span>
        </div>
        <div className="header-right">
          <span className={`status-badge status-${reservation.status.toLowerCase()}`}>
            {getStatusLabel(reservation.status)}
          </span>
          <span className={`payment-badge payment-${reservation.paymentStatus.toLowerCase()}`}>
            {getPaymentStatusLabel(reservation.paymentStatus)}
          </span>
        </div>
      </div>

      <div className="reservation-details">
        <div className="detail-section">
          <h5>Información de la reserva</h5>
          <div className="detail-grid">
            <div className="detail-item">
              <span className="detail-label">Duración:</span>
              <span className="detail-value">
                {calculateDuration(reservation.startTime, reservation.endTime)}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Fecha:</span>
              <span className="detail-value">
                {formatDate(reservation.startTime)}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">Horario:</span>
              <span className="detail-value">
                {formatTime(reservation.startTime)} - {formatTime(reservation.endTime)}
              </span>
            </div>
            <div className="detail-item">
              <span className="detail-label">ID Cancha:</span>
              <span className="detail-value">{reservation.courtId.toString()}</span>
            </div>
            <div className="detail-item">
              <span className="detail-label">ID Club:</span>
              <span className="detail-value">{reservation.clubId.toString()}</span>
            </div>
          </div>
        </div>
      </div>

      <div className="reservation-footer">
        <div className="footer-left">
          {!isUpcoming && (
            <span className="past-badge">Reserva pasada</span>
          )}
          {isUpcoming && reservation.status === 'CONFIRMED' && (
            <span className="upcoming-badge">Próxima reserva</span>
          )}
        </div>

        <div className="footer-actions">
          {onViewDetails && (
            <button
              className="btn-view"
              onClick={() => onViewDetails(reservation.id)}
            >
              Ver detalles
            </button>
          )}

          {canEdit && onEdit && (
            <button
              className="btn-edit"
              onClick={() => onEdit(reservation.id)}
              disabled={!isUpcoming}
              title={!isUpcoming ? "No se puede editar reservas pasadas" : ""}
            >
              Editar
            </button>
          )}

          {canCancel && onCancel && (
            <button
              className="btn-cancel"
              onClick={() => onCancel(reservation.id)}
              disabled={!isUpcoming}
              title={!isUpcoming ? "No se puede cancelar reservas pasadas" : ""}
            >
              Cancelar
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default ReservationCard;