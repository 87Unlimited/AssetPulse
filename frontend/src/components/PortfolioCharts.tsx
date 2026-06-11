import {
  PieChart,
  Pie,
  Cell,
  Tooltip,
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Cell as BarCell,
} from "recharts";
import type { HoldingResponse } from "../types/holding";

const COLORS = [
  "#3b82f6",
  "#10b981",
  "#f59e0b",
  "#ec4899",
  "#8b5cf6",
  "#06b6d4",
];

const formatCurrency = (value: number) =>
  new Intl.NumberFormat("en-US", { style: "currency", currency: "USD" }).format(
    value,
  );

interface ChartTooltipProps {
  active?: boolean;
  payload?: Array<{
    name?: string;
    value?: number | string;
    payload?: { symbol?: string };
  }>;
}

const ChartTooltip = ({ active, payload }: ChartTooltipProps) => {
  if (!active || !payload || payload.length === 0) return null;

  const item = payload[0];
  const value = typeof item.value === "number" ? item.value : 0;

  return (
    <div className="bg-gray-900 border border-gray-700 rounded-lg px-3 py-2">
      <p className="text-white text-xs font-medium">
        {item.name ?? item.payload?.symbol}
      </p>
      <p className="text-gray-300 text-xs">{formatCurrency(value)}</p>
    </div>
  );
};

interface Props {
  holdings: HoldingResponse[];
}

const PortfolioCharts = ({ holdings }: Props) => {
  if (holdings.length === 0) return null;

  // Donut chart data
  const allocationData = holdings.map((h) => ({
    name: h.symbol,
    value: Number(h.marketValue),
  }));

  // Bar chart data
  const gainLossData = holdings.map((h) => ({
    symbol: h.symbol,
    gainLoss: Number(h.gainLoss),
  }));

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
      {/* Allocation donut chart */}
      <div className="bg-gray-900 rounded-2xl p-5 border border-gray-800">
        <h2 className="text-sm font-medium text-white mb-4">Allocation</h2>
        <div className="flex items-center gap-6">
          <ResponsiveContainer width={160} height={160}>
            <PieChart>
              <Pie
                data={allocationData}
                cx="50%"
                cy="50%"
                innerRadius={48}
                outerRadius={72}
                paddingAngle={2}
                dataKey="value"
              >
                {allocationData.map((_, index) => (
                  <Cell key={index} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip content={<ChartTooltip />} />
            </PieChart>
          </ResponsiveContainer>

          {/* Legend */}
          <div className="flex flex-col gap-2 flex-1">
            {allocationData.map((entry, index) => {
              const percent = (
                (entry.value /
                  allocationData.reduce((sum, d) => sum + d.value, 0)) *
                100
              ).toFixed(1);
              return (
                <div
                  key={entry.name}
                  className="flex items-center justify-between"
                >
                  <div className="flex items-center gap-2">
                    <div
                      className="w-2 h-2 rounded-full flex-shrink-0"
                      style={{ background: COLORS[index % COLORS.length] }}
                    />
                    <span className="text-gray-300 text-xs">{entry.name}</span>
                  </div>
                  <span className="text-gray-400 text-xs">{percent}%</span>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Gain/Loss bar chart */}
      <div className="bg-gray-900 rounded-2xl p-5 border border-gray-800">
        <h2 className="text-sm font-medium text-white mb-4">
          Gain / loss by holding
        </h2>
        <ResponsiveContainer width="100%" height={160}>
          <BarChart
            data={gainLossData}
            layout="vertical"
            margin={{ top: 0, right: 60, left: 0, bottom: 0 }}
          >
            <CartesianGrid horizontal={false} stroke="#1f2937" />
            <XAxis
              type="number"
              tick={{ fill: "#6b7280", fontSize: 11 }}
              tickFormatter={(v) => `$${v}`}
              axisLine={false}
              tickLine={false}
            />
            <YAxis
              type="category"
              dataKey="symbol"
              tick={{ fill: "#9ca3af", fontSize: 12 }}
              axisLine={false}
              tickLine={false}
              width={40}
            />
            <Tooltip
              content={<ChartTooltip />}
              cursor={{ fill: "#1f2937", opacity: 0.4 }}
            />
            <Bar dataKey="gainLoss" radius={[0, 4, 4, 0]} maxBarSize={18}>
              {gainLossData.map((entry, index) => (
                <BarCell
                  key={index}
                  fill={entry.gainLoss >= 0 ? "#10b981" : "#ef4444"}
                />
              ))}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default PortfolioCharts;
