export interface HoldingResponse {
  id: number;
  symbol: string;
  name: string;
  quantity: number;
  averageCost: number;
  currentPrice: number;
  marketValue: number;
  gainLoss: number;
  gainLossPercent: number;
  createdAt: string;
  updatedAt: string;
}

export interface HoldingRequest {
  symbol: string;
  name: string;
  quantity: number;
  averageCost: number;
  currentPrice: number;
  market: number;
}