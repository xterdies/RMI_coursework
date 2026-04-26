import { create } from 'zustand';

export type SortOrder = 'asc' | 'desc';

type TableState = {
  page: number;
  size: number;
  filterText: string;
  sortField: string;
  sortOrder: SortOrder;
  setPage: (page: number) => void;
  setFilterText: (text: string) => void;
  setSort: (field: string, order: SortOrder) => void;
  reset: () => void;
};

const defaultState = {
  page: 0,
  size: 20,
  filterText: '',
  sortField: 'createdAt',
  sortOrder: 'desc' as SortOrder,
};

export const createTableStore = (overrides?: Partial<typeof defaultState>) =>
  create<TableState>((set) => ({
    ...defaultState,
    ...overrides,
    setPage: (page) => set({ page }),
    setFilterText: (filterText) => set({ filterText, page: 0 }),
    setSort: (sortField, sortOrder) => set({ sortField, sortOrder, page: 0 }),
    reset: () => set({ ...defaultState, ...overrides }),
  }));

export const useRegionsTableStore = createTableStore({ size: 20, sortField: 'name', sortOrder: 'asc' });
export const useClusteringTableStore = createTableStore({ size: 20, sortField: 'createdAt', sortOrder: 'desc' });
export const useUsersTableStore = createTableStore({ size: 20, sortField: 'createdAt', sortOrder: 'desc' });
export const useLogsTableStore = createTableStore({ size: 50, sortField: 'createdAt', sortOrder: 'desc' });

