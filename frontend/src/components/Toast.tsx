import { useToastStore } from "../store/toastStore";

// ─── Toast Component ──────────────────────────────────────────────────────────
const Toast = () => {
  const { message, type, hide } = useToastStore();

  if (!message) return null;

  return (
    <div className="fixed bottom-6 right-6 z-50 animate-[slideIn_0.2s_ease-out]">
      <div className="flex items-center gap-3 bg-gray-900 border border-gray-700 rounded-xl px-4 py-3 shadow-2xl">
        <div
          className={`w-2 h-2 rounded-full flex-shrink-0 ${
            type === "success" ? "bg-green-400" : "bg-red-400"
          }`}
        />
        <p className="text-white text-sm">{message}</p>
        <button
          onClick={hide}
          className="text-gray-500 hover:text-white ml-2 text-sm transition"
          aria-label="Dismiss"
        >
          ✕
        </button>
      </div>
    </div>
  );
};

export default Toast;
