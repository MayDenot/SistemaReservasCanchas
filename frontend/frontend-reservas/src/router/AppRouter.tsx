// frontend/src/router/AppRouter.tsx
import React from 'react';
import { Routes, Route } from 'react-router-dom';
import PrivateRoute from './PrivateRoute';

// Pages
import LoginPage from '../pages/auth/LoginPage';
import RegisterPage from '../pages/auth/RegisterPage';
import RegisterUserPage from '../pages/auth/RegisterUserPage';
import RegisterClubAdminPage from '../pages/auth/RegisterClubAdminPage';
import BecomeClubAdminPage from '../pages/auth/BecomeClubAdminPage'; // Nueva
import CreateClubPage from '../pages/clubs/CreateClubPage'; // Nueva
import HomePage from '../pages/public/HomePage';
import CourtsPage from '../pages/courts/CourtsPage';
import CourtDetailPage from '../pages/courts/CourtDetailPage';
import ReservationsPage from '../pages/reservations/ReservationsPage';
import CreateReservationPage from '../pages/reservations/CreateReservationPage';
import ProfilePage from '../pages/profile/ProfilePage';
import AdminDashboardPage from '../pages/admin/DashboardPage';
import ReservationDetailPage from '../pages/reservations/ReservationDetailPage';
import ClubDashboardPage from "../pages/clubs/ClubDashboardPage";
import UnauthorizedPage from "../pages/errors/UnauthorizedPage";
import NotFoundPage from "../pages/errors/NotFoundPage";
import RoleBasedRoute from "../components/auth/RoleBasedRoute.tsx";

const AppRouter: React.FC = () => {
  return (
    <Routes>
      {/* Rutas públicas */}
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/register-user" element={<RegisterUserPage />} />

      {/* Rutas para convertirse en administrador de club */}
      <Route path="/become-club-admin" element={<BecomeClubAdminPage />} />
      <Route path="/register-club" element={<RegisterClubAdminPage />} />

      <Route path="/courts" element={<CourtsPage />} />
      <Route path="/courts/:id" element={<CourtDetailPage />} />

      {/* Rutas protegidas por autenticación */}
      <Route path="/profile" element={
        <PrivateRoute>
          <ProfilePage />
        </PrivateRoute>
      } />

      {/* Ruta para crear club cuando ya estás logueado */}
      <Route path="/create-club" element={
        <PrivateRoute>
          <CreateClubPage />
        </PrivateRoute>
      } />

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

      <Route path="/reservations/:id" element={
        <PrivateRoute>
          <ReservationDetailPage />
        </PrivateRoute>
      } />

      {/* Rutas protegidas por roles específicos */}
      {/* Solo SUPER_ADMIN */}
      <Route path="/admin/dashboard" element={
        <RoleBasedRoute allowedRoles={['SUPER_ADMIN']}>
          <AdminDashboardPage />
        </RoleBasedRoute>
      } />

      {/* Solo CLUB_ADMIN y SUPER_ADMIN */}
      <Route path="/club/dashboard" element={
        <RoleBasedRoute allowedRoles={['CLUB_ADMIN', 'SUPER_ADMIN']}>
          <ClubDashboardPage />
        </RoleBasedRoute>
      } />

      {/* Otras rutas con roles específicos */}
      <Route path="/admin/users" element={
        <RoleBasedRoute allowedRoles={['SUPER_ADMIN']}>
          <div>Página de gestión de usuarios (solo SUPER_ADMIN)</div>
        </RoleBasedRoute>
      } />

      <Route path="/admin/clubs" element={
        <RoleBasedRoute allowedRoles={['SUPER_ADMIN']}>
          <div>Página de gestión de clubes (solo SUPER_ADMIN)</div>
        </RoleBasedRoute>
      } />

      <Route path="/club/courts" element={
        <RoleBasedRoute allowedRoles={['CLUB_ADMIN', 'SUPER_ADMIN']}>
          <div>Página de gestión de canchas del club</div>
        </RoleBasedRoute>
      } />

      <Route path="/club/reservations" element={
        <RoleBasedRoute allowedRoles={['CLUB_ADMIN', 'SUPER_ADMIN']}>
          <div>Página de reservas del club</div>
        </RoleBasedRoute>
      } />

      {/* Rutas de error */}
      <Route path="/unauthorized" element={<UnauthorizedPage />} />
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  );
};

export default AppRouter;