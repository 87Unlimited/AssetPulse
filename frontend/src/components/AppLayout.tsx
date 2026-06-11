import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { useAuthStore } from "../store/authStore";
import Toast from "./Toast";

const navItems = [
  { to: "/dashboard", icon: "ti-layout-dashboard", label: "Dashboard" },
  { to: "/transactions", icon: "ti-arrows-exchange", label: "Transactions" },
  { to: "/markets", icon: "ti-chart-candle", label: "Markets" },
  { to: "/alerts", icon: "ti-bell", label: "Alerts" },
];

const AppLayout = () => {
  const logout = useAuthStore((state) => state.logout);
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="flex h-screen bg-gray-950 overflow-hidden">
      {/* Sidebar */}
      <aside className="w-52 bg-gray-900 border-r border-gray-800 flex flex-col flex-shrink-0">
        {/* Logo */}
        <div className="flex items-center gap-2 px-5 py-6">
          <i
            className="ti ti-chart-line text-blue-400 text-xl"
            aria-hidden="true"
          />
          <span className="text-white font-semibold text-base">AssetPulse</span>
        </div>

        {/* Nav items */}
        <nav className="flex flex-col gap-1 px-3 flex-1">
          {navItems.map(({ to, icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition
                ${
                  isActive
                    ? "bg-blue-500/10 text-blue-400 font-medium"
                    : "text-gray-400 hover:text-white hover:bg-gray-800"
                }`
              }
            >
              <i className={`ti ${icon} text-base`} aria-hidden="true" />
              {label}
            </NavLink>
          ))}
        </nav>

        {/* Bottom nav */}
        <div className="border-t border-gray-800 px-3 py-4 flex flex-col gap-1">
          <NavLink
            to="/settings"
            className={({ isActive }) =>
              `flex items-center gap-3 px-3 py-2 rounded-lg text-sm transition
              ${
                isActive
                  ? "bg-blue-500/10 text-blue-400 font-medium"
                  : "text-gray-400 hover:text-white hover:bg-gray-800"
              }`
            }
          >
            <i className="ti ti-settings text-base" aria-hidden="true" />
            Settings
          </NavLink>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2 rounded-lg text-sm text-gray-400 hover:text-white hover:bg-gray-800 transition w-full text-left"
          >
            <i className="ti ti-logout text-base" aria-hidden="true" />
            Sign out
          </button>
        </div>
      </aside>

      {/* Main content */}
      <main className="flex-1 overflow-y-auto">
        <Outlet />
      </main>

      <Toast />
    </div>
  );
};

export default AppLayout;
