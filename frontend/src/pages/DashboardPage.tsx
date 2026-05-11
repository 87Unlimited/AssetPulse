import { useNavigate } from "react-router-dom";
import { useState } from "react";
import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { getHoldings, createHolding, deleteHolding } from "../api/holdingsApi";
import type { HoldingRequest, HoldingResponse } from "../types/holding";
import { useAuthStore } from "../store/authStore";
import { useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import axios from "axios";

// ─── Zod Schema ───────────────────────────────────────────────────────────────
const holdingSchema = z.object({
  symbol: z.string().min(1, "Symbol is required").max(10),
  name: z.string().min(1, "Name is required"),
  quantity: z
    .string()
    .min(1, "Required")
    .refine(
      (v) => !isNaN(parseFloat(v)) && parseFloat(v) > 0,
      "Must be greater than 0",
    ),
  averageCost: z
    .string()
    .min(1, "Required")
    .refine(
      (v) => !isNaN(parseFloat(v)) && parseFloat(v) > 0,
      "Must be greater than 0",
    ),
  currentPrice: z
    .string()
    .min(1, "Required")
    .refine(
      (v) => !isNaN(parseFloat(v)) && parseFloat(v) > 0,
      "Must be greater than 0",
    ),
});

type HoldingForm = z.infer<typeof holdingSchema>;

// ─── Helper ───────────────────────────────────────────────────────────────────
const formatCurrency = (value: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(
    value,
  );

const formatPercent = (value: number) =>
  `${value >= 0 ? "+" : ""}${value.toFixed(2)}%`;

// ─── Sub-components ───────────────────────────────────────────────────────────
const SummaryCard = ({
  label,
  value,
  highlight,
}: {
  label: string;
  value: string;
  highlight?: "green" | "red" | "neutral";
}) => {
  const colour =
    highlight === "green"
      ? "text-green-400"
      : highlight === "red"
        ? "text-red-400"
        : "text-white";

  return (
    <div className="bg-gray-900 rounded-2xl p-6 shadow-xl">
      <p className="text-gray-400 text-sm mb-1">{label}</p>
      <p className={`text-2xl font-bold ${colour}`}>{value}</p>
    </div>
  );
};

// ─── Main Page ────────────────────────────────────────────────────────────────
const DashboardPage = () => {
  const navigate = useNavigate();
  const logout = useAuthStore((state) => state.logout);
  const queryClient = useQueryClient();
  const [showForm, setShowForm] = useState(false);
  const [serverError, setServerError] = useState<string | null>(null);

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  // Fetch holdings
  const {
    data: holdings = [],
    isLoading,
    isError,
  } = useQuery({
    queryKey: ["holdings"],
    queryFn: getHoldings,
  });

  // Create holding mutation
  const createMutation = useMutation({
    mutationFn: createHolding,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["holdings"] });
      setShowForm(false);
      reset();
      setServerError(null);
    },
    onError: (error) => {
      if (axios.isAxiosError(error)) {
        if (error.response?.status === 409) {
          setServerError("This symbol already exists in your portfolio");
        } else {
          setServerError("Something went wrong, please try again");
        }
      }
    },
  });

  // Delete holding mutation
  const deleteMutation = useMutation({
    mutationFn: deleteHolding,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["holdings"] });
    },
  });

  // Form
  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<HoldingForm>({
    resolver: zodResolver(holdingSchema),
  });

  const onSubmit = (data: HoldingForm) => {
    const payload: HoldingRequest = {
      symbol: data.symbol,
      name: data.name,
      quantity: parseFloat(data.quantity),
      averageCost: parseFloat(data.averageCost),
      currentPrice: parseFloat(data.currentPrice),
    };
    createMutation.mutate(payload);
  };

  // Portfolio calculations
  const totalValue = holdings.reduce((sum, h) => sum + h.marketValue, 0);
  const totalCost = holdings.reduce(
    (sum, h) => sum + h.quantity * h.averageCost,
    0,
  );
  const totalGainLoss = totalValue - totalCost;
  const totalGainLossPercent =
    totalCost > 0 ? (totalGainLoss / totalCost) * 100 : 0;

  // ─── Render ────────────────────────────────────────────────────────────────
  if (isLoading) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center">
        <p className="text-gray-400">Loading portfolio...</p>
      </div>
    );
  }

  if (isError) {
    return (
      <div className="min-h-screen bg-gray-950 flex items-center justify-center">
        <p className="text-red-400">
          Failed to load portfolio. Please try again.
        </p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-950 p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-3xl font-bold text-white">AssetPulse</h1>
          <p className="text-gray-400 mt-1">Your Portfolio Overview</p>
        </div>
        <button
          onClick={handleLogout}
          className="text-gray-400 hover:text-white text-sm transition"
        >
          Sign out
        </button>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-8">
        <SummaryCard
          label="Total Portfolio Value"
          value={formatCurrency(totalValue)}
        />
        <SummaryCard
          label="Total Gain / Loss"
          value={formatCurrency(totalGainLoss)}
          highlight={totalGainLoss >= 0 ? "green" : "red"}
        />
        <SummaryCard
          label="Overall Return"
          value={formatPercent(totalGainLossPercent)}
          highlight={totalGainLossPercent >= 0 ? "green" : "red"}
        />
      </div>

      {/* Holdings Section */}
      <div className="bg-gray-900 rounded-2xl shadow-xl">
        {/* Table Header */}
        <div className="flex items-center justify-between p-6 border-b border-gray-800">
          <h2 className="text-lg font-semibold text-white">Holdings</h2>
          <button
            onClick={() => {
              setShowForm(!showForm);
              setServerError(null);
            }}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold rounded-lg transition"
          >
            {showForm ? "Cancel" : "+ Add Holding"}
          </button>
        </div>

        {/* Add Holding Form */}
        {showForm && (
          <div className="p-6 border-b border-gray-800">
            {serverError && (
              <div className="mb-4 p-3 bg-red-500/10 border border-red-500/20 rounded-lg">
                <p className="text-red-400 text-sm">{serverError}</p>
              </div>
            )}
            <form
              onSubmit={handleSubmit(onSubmit)}
              className="grid grid-cols-1 sm:grid-cols-5 gap-3"
            >
              {[
                { field: "symbol", placeholder: "AAPL" },
                { field: "name", placeholder: "Apple Inc." },
                { field: "quantity", placeholder: "Quantity" },
                { field: "averageCost", placeholder: "Avg Cost" },
                { field: "currentPrice", placeholder: "Current Price" },
              ].map(({ field, placeholder }) => (
                <div key={field}>
                  <input
                    {...register(field as keyof HoldingForm)}
                    placeholder={placeholder}
                    className="w-full px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
                  />
                  {errors[field as keyof HoldingForm] && (
                    <p className="mt-1 text-xs text-red-400">
                      {errors[field as keyof HoldingForm]?.message}
                    </p>
                  )}
                </div>
              ))}
              <button
                type="submit"
                disabled={createMutation.isPending}
                className="sm:col-span-5 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-blue-800 text-white text-sm font-semibold rounded-lg transition"
              >
                {createMutation.isPending ? "Adding..." : "Add Holding"}
              </button>
            </form>
          </div>
        )}

        {/* Holdings Table */}
        {holdings.length === 0 ? (
          <div className="p-12 text-center">
            <p className="text-gray-400">No holdings yet.</p>
            <p className="text-gray-600 text-sm mt-1">
              Add your first holding to get started.
            </p>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="text-left text-gray-400 text-sm border-b border-gray-800">
                  {[
                    "Symbol",
                    "Name",
                    "Quantity",
                    "Avg Cost",
                    "Current Price",
                    "Market Value",
                    "Gain / Loss",
                    "Return",
                    "",
                  ].map((h) => (
                    <th key={h} className="px-6 py-3 font-medium">
                      {h}
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {holdings.map((holding: HoldingResponse) => {
                  const isGain = holding.gainLoss >= 0;
                  return (
                    <tr
                      key={holding.id}
                      className="border-b border-gray-800 hover:bg-gray-800/50 transition"
                    >
                      <td className="px-6 py-4 font-semibold text-white">
                        {holding.symbol}
                      </td>
                      <td className="px-6 py-4 text-gray-300">
                        {holding.name}
                      </td>
                      <td className="px-6 py-4 text-gray-300">
                        {holding.quantity}
                      </td>
                      <td className="px-6 py-4 text-gray-300">
                        {formatCurrency(holding.averageCost)}
                      </td>
                      <td className="px-6 py-4 text-gray-300">
                        {formatCurrency(holding.currentPrice)}
                      </td>
                      <td className="px-6 py-4 text-white font-medium">
                        {formatCurrency(holding.marketValue)}
                      </td>
                      <td
                        className={`px-6 py-4 font-medium ${isGain ? "text-green-400" : "text-red-400"}`}
                      >
                        {formatCurrency(holding.gainLoss)}
                      </td>
                      <td
                        className={`px-6 py-4 font-medium ${isGain ? "text-green-400" : "text-red-400"}`}
                      >
                        {formatPercent(holding.gainLossPercent)}
                      </td>
                      <td className="px-6 py-4">
                        <button
                          onClick={() => deleteMutation.mutate(holding.id)}
                          disabled={deleteMutation.isPending}
                          className="text-gray-500 hover:text-red-400 text-sm transition"
                        >
                          Remove
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};

export default DashboardPage;
