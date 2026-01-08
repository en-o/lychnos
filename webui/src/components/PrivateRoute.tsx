import React from 'react';
import {Navigate} from 'react-router-dom';

interface PrivateRouteProps {
  children: React.ReactElement;
}

const PrivateRoute: React.FC<PrivateRouteProps> = ({ children }) => {
  const token = localStorage.getItem('token');

  if (!token) {
    // 未登录则重定向到登录页
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default PrivateRoute;
