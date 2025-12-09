import React, { useState } from 'react';
import './PaymentForm.css';

interface PaymentFormProps {
  amount: number;
  onPaymentSuccess: (paymentData: any) => void;
  onPaymentError: (error: string) => void;
}

const PaymentForm: React.FC<PaymentFormProps> = ({
                                                   amount,
                                                   onPaymentSuccess,
                                                   onPaymentError
                                                 }) => {
  const [cardNumber, setCardNumber] = useState('');
  const [expiry, setExpiry] = useState('');
  const [cvv, setCvv] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    // Lógica de pago aquí (integrar con Stripe/MercadoPago/etc)
    try {
      // Simulación de pago exitoso
      onPaymentSuccess({ transactionId: 'txn_12345' });
    } catch (error) {
      onPaymentError('Error en el pago');
    }
  };

  return (
    <div className="payment-form">
      <h3>Pago: ${amount}</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-group">
          <label>Número de tarjeta</label>
          <input
            type="text"
            value={cardNumber}
            onChange={(e) => setCardNumber(e.target.value)}
            placeholder="1234 5678 9012 3456"
            maxLength={19}
          />
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Fecha de expiración</label>
            <input
              type="text"
              value={expiry}
              onChange={(e) => setExpiry(e.target.value)}
              placeholder="MM/AA"
              maxLength={5}
            />
          </div>
          <div className="form-group">
            <label>CVV</label>
            <input
              type="text"
              value={cvv}
              onChange={(e) => setCvv(e.target.value)}
              placeholder="123"
              maxLength={3}
            />
          </div>
        </div>
        <button type="submit" className="pay-button">
          Pagar ${amount}
        </button>
      </form>
    </div>
  );
};

export default PaymentForm;