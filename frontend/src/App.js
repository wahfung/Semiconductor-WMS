import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { useAuthStore } from './utils/store';
import MainLayout from './components/MainLayout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Materials from './pages/Materials';
import Warehouses from './pages/Warehouses';
import Shelves from './pages/Shelves';
import Warehouse3D from './pages/Warehouse3D';
import Inventory from './pages/Inventory';
import PurchaseOrders from './pages/PurchaseOrders';
import InboundOrders from './pages/InboundOrders';
import OutboundOrders from './pages/OutboundOrders';
import StocktakingOrders from './pages/StocktakingOrders';

const PrivateRoute = ({ children }) => {
  const { token } = useAuthStore();
  return token ? children : <Navigate to="/login" replace />;
};

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route
          path="/*"
          element={
            <PrivateRoute>
              <MainLayout>
                <Routes>
                  <Route path="/" element={<Dashboard />} />
                  <Route path="/dashboard" element={<Dashboard />} />
                  <Route path="/materials" element={<Materials />} />
                  <Route path="/warehouses" element={<Warehouses />} />
                  <Route path="/shelves" element={<Shelves />} />
                  <Route path="/warehouse-3d" element={<Warehouse3D />} />
                  <Route path="/inventory" element={<Inventory />} />
                  <Route path="/purchase-orders" element={<PurchaseOrders />} />
                  <Route path="/inbound-orders" element={<InboundOrders />} />
                  <Route path="/outbound-orders" element={<OutboundOrders />} />
                  <Route path="/stocktaking-orders" element={<StocktakingOrders />} />
                </Routes>
              </MainLayout>
            </PrivateRoute>
          }
        />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
