export const formatDate = (date: Date): string => {
  return new Date(date).toLocaleDateString('es-ES', {
    weekday: 'long',
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  });
};

export const formatTime = (date: Date): string => {
  return new Date(date).toLocaleTimeString('es-ES', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

export const addDays = (date: Date, days: number): Date => {
  const result = new Date(date);
  result.setDate(result.getDate() + days);
  return result;
};

export const getTimeSlots = (start: string, end: string, interval: number): string[] => {
  const slots: string[] = [];
  let current = new Date(`2000-01-01T${start}`);
  const endTime = new Date(`2000-01-01T${end}`);

  while (current < endTime) {
    slots.push(current.toTimeString().slice(0, 5));
    current = new Date(current.getTime() + interval * 60000);
  }

  return slots;
};