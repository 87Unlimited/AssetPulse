import axiosClient from './axiosClient';
import type { HoldingResponse, HoldingRequest } from '../types/holding';

export const getHoldings = async (): Promise<HoldingResponse[]> => {
  const response = await axiosClient.get('/api/holdings');
  return response.data;
};

export const createHolding = async (
  data: HoldingRequest
): Promise<HoldingResponse> => {
  const response = await axiosClient.post('/api/holdings', data);
  return response.data;
};

export const deleteHolding = async (id: number): Promise<void> => {
  await axiosClient.delete(`/api/holdings/${id}`);
};