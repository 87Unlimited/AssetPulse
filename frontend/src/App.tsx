import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage.tsx";
import RegisterPage from "./pages/RegisterPage.tsx";
import DashboardPage from "./pages/DashboardPage";
import AppLayout from "./components/AppLayout.tsx";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />

        {/* Protected routes — all nested here require auth */}
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<DashboardPage />} />
            <Route
              path="/transactions"
              element={
                <div className="p-8 text-white">Transactions coming soon</div>
              }
            />
            <Route
              path="/markets"
              element={
                <div className="p-8 text-white">Markets coming soon</div>
              }
            />
            <Route
              path="/alerts"
              element={<div className="p-8 text-white">Alerts coming soon</div>}
            />
            <Route
              path="/settings"
              element={
                <div className="p-8 text-white">Settings coming soon</div>
              }
            />
          </Route>
        </Route>

        {/* Default redirect */}
        <Route path="*" element={<Navigate to="/login" replace />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
