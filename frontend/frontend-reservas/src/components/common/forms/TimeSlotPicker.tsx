import React from 'react';
import './TimeSlotPicker.css';

interface TimeSlot {
  id: string;
  time: string;
  available: boolean;
  price?: number;
}

interface TimeSlotPickerProps {
  slots: TimeSlot[];
  selectedSlot: string | null;
  onSelect: (slotId: string) => void;
  date: Date;
}

const TimeSlotPicker: React.FC<TimeSlotPickerProps> = ({
                                                         slots,
                                                         selectedSlot,
                                                         onSelect,
                                                         date
                                                       }) => {
  return (
    <div className="time-slot-picker">
      <h3>Horarios disponibles para {date.toLocaleDateString('es-ES')}</h3>
      <div className="time-slots-grid">
        {slots.map((slot) => (
          <button
            key={slot.id}
            className={`time-slot ${selectedSlot === slot.id ? 'selected' : ''} ${
              !slot.available ? 'unavailable' : ''
            }`}
            onClick={() => slot.available && onSelect(slot.id)}
            disabled={!slot.available}
          >
            <span className="time">{slot.time}</span>
            {slot.price && <span className="price">${slot.price}</span>}
          </button>
        ))}
      </div>
    </div>
  );
};

export default TimeSlotPicker;