import { create } from "zustand";

// ─── Toast Store ──────────────────────────────────────────────────────────────
export interface ToastState {
  message: string | null;
  type: "success" | "error";
  show: (message: string, type?: "success" | "error") => void;
  hide: () => void;
}

export const useToastStore = create<ToastState>((set) => ({
  message: null,
  type: "success",
  show: (message, type = "success") => {
    set({ message, type });
    setTimeout(() => set({ message: null }), 3000);
  },
  hide: () => set({ message: null }),
}));