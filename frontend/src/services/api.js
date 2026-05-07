import axios from 'axios';
import { message } from 'antd';
import { useAuthStore } from '../utils/store';

const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
});

api.interceptors.request.use(
  (config) => {
    const { token } = useAuthStore.getState();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

api.interceptors.response.use(
  (response) => response.data,
  (error) => {
    if (error.response) {
      const { status, data } = error.response;
      if (status === 401) {
        useAuthStore.getState().logout();
        window.location.href = '/login';
        message.error('登录已过期，请重新登录');
      } else if (status === 403) {
        message.error('没有权限执行此操作');
      } else {
        message.error(data?.message || '请求失败');
      }
    } else {
      message.error('网络错误，请检查网络连接');
    }
    return Promise.reject(error);
  }
);

export const authAPI = {
  login: (data) => api.post('/auth/login', data),
  getCurrentUser: () => api.get('/auth/current'),
};

export const materialAPI = {
  getAll: (params) => api.get('/materials', { params }),
  getAllEnabled: () => api.get('/materials/enabled'),
  getByType: (type) => api.get(`/materials/type/${type}`),
  getById: (id) => api.get(`/materials/${id}`),
  create: (data) => api.post('/materials', data),
  update: (id, data) => api.put(`/materials/${id}`, data),
  delete: (id) => api.delete(`/materials/${id}`),
};

export const warehouseAPI = {
  getAll: (params) => api.get('/warehouses', { params }),
  getAllEnabled: () => api.get('/warehouses/enabled'),
  getById: (id) => api.get(`/warehouses/${id}`),
  create: (data) => api.post('/warehouses', data),
  update: (id, data) => api.put(`/warehouses/${id}`, data),
  delete: (id) => api.delete(`/warehouses/${id}`),
};

export const shelfAPI = {
  getAll: (params) => api.get('/shelves', { params }),
  getAllWithWarehouse: () => api.get('/shelves/all'),
  getByWarehouseId: (warehouseId) => api.get(`/shelves/warehouse/${warehouseId}`),
  getById: (id) => api.get(`/shelves/${id}`),
  create: (data) => api.post('/shelves', data),
  update: (id, data) => api.put(`/shelves/${id}`, data),
  delete: (id) => api.delete(`/shelves/${id}`),
};

export const inventoryAPI = {
  getAll: (params) => api.get('/inventories', { params }),
  getByWarehouseId: (warehouseId) => api.get(`/inventories/warehouse/${warehouseId}`),
  getByMaterialId: (materialId) => api.get(`/inventories/material/${materialId}`),
  getById: (id) => api.get(`/inventories/${id}`),
};

export const purchaseOrderAPI = {
  getAll: (params) => api.get('/purchase-orders', { params }),
  getById: (id) => api.get(`/purchase-orders/${id}`),
  create: (data) => api.post('/purchase-orders', data),
  approve: (id) => api.post(`/purchase-orders/${id}/approve`),
  cancel: (id) => api.post(`/purchase-orders/${id}/cancel`),
};

export const inboundOrderAPI = {
  getAll: (params) => api.get('/inbound-orders', { params }),
  getById: (id) => api.get(`/inbound-orders/${id}`),
  create: (data) => api.post('/inbound-orders', data),
  confirm: (id) => api.post(`/inbound-orders/${id}/confirm`),
};

export const outboundOrderAPI = {
  getAll: (params) => api.get('/outbound-orders', { params }),
  getById: (id) => api.get(`/outbound-orders/${id}`),
  create: (data) => api.post('/outbound-orders', data),
  confirm: (id) => api.post(`/outbound-orders/${id}/confirm`),
};

export const stocktakingOrderAPI = {
  getAll: (params) => api.get('/stocktaking-orders', { params }),
  getById: (id) => api.get(`/stocktaking-orders/${id}`),
  create: (data) => api.post('/stocktaking-orders', data),
  updateItem: (orderId, itemId, data) => api.put(`/stocktaking-orders/${orderId}/items/${itemId}`, data),
  complete: (id) => api.post(`/stocktaking-orders/${id}/complete`),
};

export const dashboardAPI = {
  getData: () => api.get('/dashboard'),
};

export default api;
