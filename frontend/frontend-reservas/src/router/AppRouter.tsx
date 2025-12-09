import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import PrivateRoute from './PrivateRoute';
import AdminRoute from './AdminRoute';

// Pages
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import HomePage from '../pages/public/HomePage';
import CourtsPage from '../pages/courts/CourtsPage';
import CourtDetailPage from '../pages/courts/CourtDetailPage';
import ReservationsPage from '../pages/reservations/ReservationsPage';
import CreateReservationPage from '../pages/reservations/CreateReservationPage';
import ProfilePage from '../pages/profile/ProfilePage';
import AdminDashboardPage from '../pages/admin/DashboardPage';
import ReservationDetailPage from '../pages/reservations/ReservationDetailPage';

const AppRouter: React.FC = () => {
    return (
        <Routes>
            {/* Public routes */}
            <Route path="/" element={<HomePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/courts" element={<CourtsPage />} />
            <Route path="/courts/:id" element={<CourtDetailPage />} />
            <Route path="/reservations/:id" element={<ReservationDetailPage />} />

            {/* Protected routes */}
            <Route path="/reservations" element={
                <PrivateRoute>
                    <ReservationsPage />
                </PrivateRoute>
            } />
            <Route path="/reservations/new" element={
                <PrivateRoute>
                    <CreateReservationPage />
                </PrivateRoute>
            } />
            <Route path="/profile" element={
                <PrivateRoute>
                    <ProfilePage />
                </PrivateRoute>
            } />

            {/* Admin routes */}
            <Route path="/admin" element={
                <AdminRoute>
                    <AdminDashboardPage />
                </AdminRoute>
            } />

            {/* 404 */}
            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
    );
};

export default AppRouter;