import React from 'react';
import {Navigate} from 'react-router-dom';

interface AdminRouteProps {
  children: React.ReactElement;
}

const AdminRoute: React.FC<AdminRouteProps> = ({ children }) => {
  const token = localStorage.getItem('token');
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  const isAdmin = userInfo.roles && Array.isArray(userInfo.roles) &&
    userInfo.roles.some((role: string) => role.toUpperCase() === 'ADMIN');

  if (!isAdmin) {
    return <Navigate to="/" replace />;
  }

  return children;
};

export default AdminRoute;
