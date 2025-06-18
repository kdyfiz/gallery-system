import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';

import AlbumGallery from './album-gallery';
import album from './album.reducer';

// Mock data
const mockAlbums = [
  {
    id: 1,
    name: 'Test Album 1',
    description: 'Test Description 1',
    creationDate: '2024-01-01T00:00:00Z',
    event: 'Test Event 1',
    user: { login: 'testuser1' },
    photos: [{ id: 1 }, { id: 2 }],
  },
  {
    id: 2,
    name: 'Test Album 2',
    description: 'Test Description 2',
    creationDate: '2024-01-02T00:00:00Z',
    event: 'Test Event 2',
    user: { login: 'testuser2' },
    photos: [{ id: 3 }],
  },
];

const createTestStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      album,
    },
    preloadedState: {
      album: {
        entities: mockAlbums,
        loading: false,
        totalItems: 2,
        ...initialState,
      },
    },
  });
};

const renderWithProviders = (component, { initialState = {} } = {}) => {
  const store = createTestStore(initialState);
  return render(
    <Provider store={store}>
      <MemoryRouter>{component}</MemoryRouter>
    </Provider>,
  );
};

describe('AlbumGallery Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render album gallery without crashing', () => {
      renderWithProviders(<AlbumGallery />);
      expect(screen.getByText('Test Album 1')).toBeInTheDocument();
      expect(screen.getByText('Test Album 2')).toBeInTheDocument();
    });

    it('should show loading spinner when loading', () => {
      renderWithProviders(<AlbumGallery />, {
        initialState: { loading: true, entities: [] },
      });
      expect(screen.getByTestId('loading-spinner')).toBeInTheDocument();
    });

    it('should display album stats correctly', () => {
      renderWithProviders(<AlbumGallery />);
      expect(screen.getByTestId('stats-total-albums')).toBeInTheDocument();
      expect(screen.getByTestId('stats-total-photos')).toBeInTheDocument();
    });
  });

  describe('View Mode Toggle', () => {
    it('should switch between grid and list view modes', () => {
      renderWithProviders(<AlbumGallery />);

      const listViewButton = screen.getByTestId('view-list-btn');
      const gridViewButton = screen.getByTestId('view-grid-btn');

      expect(gridViewButton).toHaveClass('btn-secondary');

      fireEvent.click(listViewButton);
      expect(listViewButton).toHaveClass('btn-secondary');
    });
  });

  describe('Search Functionality', () => {
    it('should handle search input changes', () => {
      renderWithProviders(<AlbumGallery />);

      const searchInput = screen.getByTestId('search-input');
      fireEvent.change(searchInput, { target: { value: 'Test Album 1' } });

      expect(searchInput).toHaveValue('Test Album 1');
    });

    it('should submit search form', async () => {
      renderWithProviders(<AlbumGallery />);

      const searchInput = screen.getByTestId('search-input');
      const searchForm = searchInput.closest('form');

      fireEvent.change(searchInput, { target: { value: 'Test' } });
      if (searchForm) {
        fireEvent.submit(searchForm);
      }

      await waitFor(() => {
        expect(searchInput).toHaveValue('Test');
      });
    });
  });

  describe('Filtering', () => {
    it('should toggle filter panel', () => {
      renderWithProviders(<AlbumGallery />);

      const filterButton = screen.getByTestId('filters-toggle-btn');
      fireEvent.click(filterButton);

      expect(screen.getByTestId('event-filter-input')).toBeInTheDocument();
    });

    it('should apply event filter', async () => {
      renderWithProviders(<AlbumGallery />);

      const filterButton = screen.getByTestId('filters-toggle-btn');
      fireEvent.click(filterButton);

      const eventFilter = screen.getByTestId('event-filter-input');
      fireEvent.change(eventFilter, { target: { value: 'Test Event 1' } });

      await waitFor(() => {
        expect(eventFilter).toHaveValue('Test Event 1');
      });
    });

    it('should clear all filters', () => {
      renderWithProviders(<AlbumGallery />);

      const filterButton = screen.getByTestId('filters-toggle-btn');
      fireEvent.click(filterButton);

      const eventFilter = screen.getByTestId('event-filter-input');
      fireEvent.change(eventFilter, { target: { value: 'Test Event' } });

      const clearFiltersButton = screen.getByTestId('clear-filters-btn');
      fireEvent.click(clearFiltersButton);

      expect(eventFilter).toHaveValue('');
    });
  });

  describe('Sorting', () => {
    it('should change sort type to DATE', () => {
      renderWithProviders(<AlbumGallery />);

      const sortByDateButton = screen.getByTestId('sort-by-date-btn');
      fireEvent.click(sortByDateButton);

      expect(sortByDateButton).toHaveClass('btn-secondary');
    });

    it('should change sort type to EVENT', () => {
      renderWithProviders(<AlbumGallery />);

      const sortByEventButton = screen.getByTestId('sort-by-event-btn');
      fireEvent.click(sortByEventButton);

      expect(sortByEventButton).toHaveClass('btn-secondary');
    });
  });

  describe('Album Selection', () => {
    it('should select individual albums', () => {
      renderWithProviders(<AlbumGallery />);

      const albumSelectButton = screen.getByTestId('select-album-btn-1');
      fireEvent.click(albumSelectButton);

      expect(screen.getByTestId('selected-count-badge')).toBeInTheDocument();
      expect(screen.getByText('1 selected')).toBeInTheDocument();
    });
  });

  describe('Refresh and Actions', () => {
    it('should refresh albums', () => {
      renderWithProviders(<AlbumGallery />);

      const refreshButton = screen.getByTestId('refresh-btn');
      fireEvent.click(refreshButton);

      expect(refreshButton).toBeInTheDocument();
    });
  });

  describe('Loading and Error States', () => {
    it('should show error state when loading fails', () => {
      renderWithProviders(<AlbumGallery />, {
        initialState: { loading: false, entities: [], error: 'Failed to load' },
      });

      expect(screen.getByTestId('no-albums-alert')).toBeInTheDocument();
    });

    it('should show no albums message when empty', () => {
      renderWithProviders(<AlbumGallery />, {
        initialState: { loading: false, entities: [] },
      });

      expect(screen.getByTestId('no-albums-alert')).toBeInTheDocument();
    });
  });

  describe('Album Display', () => {
    it('should display album information correctly', () => {
      renderWithProviders(<AlbumGallery />);

      expect(screen.getByText('Test Album 1')).toBeInTheDocument();
      expect(screen.getByText('Test Description 1')).toBeInTheDocument();
      expect(screen.getByText('Test Event 1')).toBeInTheDocument();

      expect(screen.getByText('Test Album 2')).toBeInTheDocument();
      expect(screen.getByText('Test Description 2')).toBeInTheDocument();
      expect(screen.getByText('Test Event 2')).toBeInTheDocument();
    });

    it('should display photo counts correctly', () => {
      renderWithProviders(<AlbumGallery />);

      expect(screen.getByText('2 photos')).toBeInTheDocument();
      expect(screen.getByText('1 photo')).toBeInTheDocument();
    });
  });
});
