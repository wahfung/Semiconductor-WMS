import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export const useAuthStore = create(
  persist(
    (set) => ({
      token: null,
      user: null,
      setAuth: (token, user) => set({ token, user }),
      logout: () => set({ token: null, user: null }),
    }),
    {
      name: 'auth-storage',
    }
  )
);

export const useWarehouseStore = create((set) => ({
  selectedWarehouse: null,
  selectedShelf: null,
  selectedSlot: null,
  setSelectedWarehouse: (warehouse) => set({ selectedWarehouse: warehouse }),
  setSelectedShelf: (shelf) => set({ selectedShelf: shelf }),
  setSelectedSlot: (slot) => set({ selectedSlot: slot }),
  clearSelection: () => set({ selectedWarehouse: null, selectedShelf: null, selectedSlot: null }),
}));
