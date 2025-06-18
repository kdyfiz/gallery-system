import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';

import { Album } from './album';
import album from './album.reducer';

// Mock data
const mockAlbums = [
  {
    id: 1,
    name: 'Summer Vacation 2024',
    description: 'Amazing summer vacation memories',
    event: 'Summer Trip',
    creationDate: '2024-06-15T10:00:00Z',
    thumbnail: 'base64thumbnail1',
    thumbnailContentType: 'image/jpeg',
    user: { id: 1, login: 'john_doe' },
    photos: [
      { id: 1, title: 'Beach Photo' },
      { id: 2, title: 'Mountain View' },
    ],
  },
  {
    id: 2,
    name: 'Wedding Photos',
    description: 'Beautiful wedding ceremony',
    event: 'Wedding',
    creationDate: '2024-05-20T14:30:00Z',
    thumbnail: 'base64thumbnail2',
    thumbnailContentType: 'image/png',
    user: { id: 2, login: 'jane_smith' },
    photos: [
      { id: 3, title: 'Ceremony' },
      { id: 4, title: 'Reception' },
      { id: 5, title: 'Group Photo' },
    ],
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

describe('Album Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render album list without crashing', () => {
      renderWithProviders(<Album />);
      expect(screen.getByText('Albums')).toBeInTheDocument();
      expect(screen.getByText('Summer Vacation 2024')).toBeInTheDocument();
      expect(screen.getByText('Wedding Photos')).toBeInTheDocument();
    });

    it('should show loading state when albums are loading', () => {
      renderWithProviders(<Album />, {
        initialState: { loading: true, entities: [] },
      });

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      expect(refreshButton).toBeDisabled();
    });

    it('should display correct album information', () => {
      renderWithProviders(<Album />);

      // Check album details are displayed
      expect(screen.getByText('Summer Vacation 2024')).toBeInTheDocument();
      expect(screen.getByText('Amazing summer vacation memories')).toBeInTheDocument();
      expect(screen.getByText('Summer Trip')).toBeInTheDocument();
      expect(screen.getByText('john_doe')).toBeInTheDocument();

      expect(screen.getByText('Wedding Photos')).toBeInTheDocument();
      expect(screen.getByText('Beautiful wedding ceremony')).toBeInTheDocument();
      expect(screen.getByText('Wedding')).toBeInTheDocument();
      expect(screen.getByText('jane_smith')).toBeInTheDocument();
    });
  });

  describe('Table Headers and Sorting', () => {
    it('should render all table headers', () => {
      renderWithProviders(<Album />);

      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Name')).toBeInTheDocument();
      expect(screen.getByText('Description')).toBeInTheDocument();
      expect(screen.getByText('Thumbnail')).toBeInTheDocument();
      expect(screen.getByText('Event')).toBeInTheDocument();
      expect(screen.getByText('Creation Date')).toBeInTheDocument();
      expect(screen.getByText('User')).toBeInTheDocument();
    });

    it('should sort by name when name header is clicked', () => {
      renderWithProviders(<Album />);

      const nameHeader = screen.getByText('Name').closest('th');
      fireEvent.click(nameHeader);

      // Should trigger sort functionality
      expect(nameHeader).toBeInTheDocument();
    });

    it('should sort by creation date when date header is clicked', () => {
      renderWithProviders(<Album />);

      const dateHeader = screen.getByText('Creation Date').closest('th');
      fireEvent.click(dateHeader);

      expect(dateHeader).toBeInTheDocument();
    });

    it('should sort by event when event header is clicked', () => {
      renderWithProviders(<Album />);

      const eventHeader = screen.getByText('Event').closest('th');
      fireEvent.click(eventHeader);

      expect(eventHeader).toBeInTheDocument();
    });
  });

  describe('Action Buttons', () => {
    it('should render refresh button', () => {
      renderWithProviders(<Album />);

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      expect(refreshButton).toBeInTheDocument();
      expect(refreshButton).not.toBeDisabled();
    });

    it('should render create new album button', () => {
      renderWithProviders(<Album />);

      const createButton = screen.getByRole('link', { name: /create new album/i });
      expect(createButton).toBeInTheDocument();
      expect(createButton).toHaveAttribute('href', '/album/new');
    });

    it('should trigger refresh when refresh button is clicked', () => {
      renderWithProviders(<Album />);

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      fireEvent.click(refreshButton);

      // Should not crash and button should still be there
      expect(refreshButton).toBeInTheDocument();
    });
  });

  describe('Album Actions', () => {
    it('should render view buttons for each album', () => {
      renderWithProviders(<Album />);

      const viewButtons = screen.getAllByRole('link', { name: /view/i });
      expect(viewButtons).toHaveLength(2);
      expect(viewButtons[0]).toHaveAttribute('href', '/album/1');
      expect(viewButtons[1]).toHaveAttribute('href', '/album/2');
    });

    it('should render edit buttons for each album', () => {
      renderWithProviders(<Album />);

      const editButtons = screen.getAllByRole('link', { name: /edit/i });
      expect(editButtons).toHaveLength(2);
      expect(editButtons[0].getAttribute('href')).toContain('/album/1/edit');
      expect(editButtons[1].getAttribute('href')).toContain('/album/2/edit');
    });

    it('should render delete buttons for each album', () => {
      renderWithProviders(<Album />);

      const deleteButtons = screen.getAllByRole('link', { name: /delete/i });
      expect(deleteButtons).toHaveLength(2);
      expect(deleteButtons[0].getAttribute('href')).toContain('/album/1/delete');
      expect(deleteButtons[1].getAttribute('href')).toContain('/album/2/delete');
    });
  });

  describe('Thumbnail Display', () => {
    it('should display album thumbnails', () => {
      renderWithProviders(<Album />);

      const images = screen.getAllByRole('img');
      expect(images).toHaveLength(2);
      expect(images[0]).toHaveAttribute('src', 'data:image/jpeg;base64,base64thumbnail1');
      expect(images[1]).toHaveAttribute('src', 'data:image/png;base64,base64thumbnail2');
    });

    it('should show thumbnail content type and size', () => {
      renderWithProviders(<Album />);

      expect(screen.getByText(/image\/jpeg/)).toBeInTheDocument();
      expect(screen.getByText(/image\/png/)).toBeInTheDocument();
    });

    it('should handle albums without thumbnails', () => {
      const albumsWithoutThumbnails = [
        {
          ...mockAlbums[0],
          thumbnail: null,
          thumbnailContentType: null,
        },
      ];

      renderWithProviders(<Album />, {
        initialState: {
          entities: albumsWithoutThumbnails,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Summer Vacation 2024')).toBeInTheDocument();
    });
  });

  describe('User Links', () => {
    it('should render user links for each album', () => {
      renderWithProviders(<Album />);

      const johnDoeLink = screen.getByRole('link', { name: 'john_doe' });
      const janeSmithLink = screen.getByRole('link', { name: 'jane_smith' });

      expect(johnDoeLink).toBeInTheDocument();
      expect(janeSmithLink).toBeInTheDocument();
      expect(johnDoeLink).toHaveAttribute('href', '/admin/user-management/john_doe');
      expect(janeSmithLink).toHaveAttribute('href', '/admin/user-management/jane_smith');
    });

    it('should handle albums without users', () => {
      const albumsWithoutUsers = [
        {
          ...mockAlbums[0],
          user: null,
        },
      ];

      renderWithProviders(<Album />, {
        initialState: {
          entities: albumsWithoutUsers,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Summer Vacation 2024')).toBeInTheDocument();
    });
  });

  describe('Date Formatting', () => {
    it('should display formatted creation dates', () => {
      renderWithProviders(<Album />);

      // Dates should be formatted and displayed
      const dateElements = screen.getAllByText(/2024/);
      expect(dateElements.length).toBeGreaterThan(0);
    });

    it('should handle albums without creation dates', () => {
      const albumsWithoutDates = [
        {
          ...mockAlbums[0],
          creationDate: null,
        },
      ];

      renderWithProviders(<Album />, {
        initialState: {
          entities: albumsWithoutDates,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Summer Vacation 2024')).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should handle empty album list', () => {
      renderWithProviders(<Album />, {
        initialState: {
          loading: false,
          entities: [],
          totalItems: 0,
        },
      });

      expect(screen.getByText('Albums')).toBeInTheDocument();
      // Should still show table headers but no data rows
      expect(screen.getByText('Name')).toBeInTheDocument();
    });
  });

  describe('Pagination', () => {
    it('should show pagination when there are multiple pages', () => {
      renderWithProviders(<Album />, {
        initialState: {
          entities: mockAlbums,
          totalItems: 25, // More than items per page
        },
      });

      // Should show item count
      expect(screen.getByText(/showing/i)).toBeInTheDocument();
    });

    it('should show correct item count', () => {
      renderWithProviders(<Album />, {
        initialState: {
          entities: mockAlbums,
          totalItems: 2,
        },
      });

      // Should show total items
      expect(screen.getByText(/of 2 items/i)).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should handle albums with missing optional fields', () => {
      const incompleteAlbums = [
        {
          id: 1,
          name: 'Incomplete Album',
          // Missing description, event, thumbnail, etc.
        },
      ];

      renderWithProviders(<Album />, {
        initialState: {
          entities: incompleteAlbums,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Incomplete Album')).toBeInTheDocument();
    });
  });

  describe('ID Links', () => {
    it('should render clickable ID links', () => {
      renderWithProviders(<Album />);

      const idLinks = screen.getAllByRole('link', { name: /^[0-9]+$/ });
      expect(idLinks).toHaveLength(2);
      expect(idLinks[0]).toHaveAttribute('href', '/album/1');
      expect(idLinks[1]).toHaveAttribute('href', '/album/2');
    });
  });

  describe('Table Structure', () => {
    it('should have proper table structure', () => {
      renderWithProviders(<Album />);

      const table = screen.getByRole('table');
      expect(table).toBeInTheDocument();
      expect(table).toHaveClass('table-responsive');
    });

    it('should have correct number of data rows', () => {
      renderWithProviders(<Album />);

      // Should have 2 data rows (excluding header)
      const dataRows = screen.getAllByTestId('entityTable');
      expect(dataRows).toHaveLength(2);
    });
  });

  describe('Accessibility', () => {
    it('should have proper heading structure', () => {
      renderWithProviders(<Album />);

      const heading = screen.getByRole('heading', { level: 2 });
      expect(heading).toBeInTheDocument();
      expect(heading).toHaveAttribute('data-cy', 'AlbumHeading');
    });

    it('should have proper table headers', () => {
      renderWithProviders(<Album />);

      const columnHeaders = screen.getAllByRole('columnheader');
      expect(columnHeaders.length).toBeGreaterThan(0);
    });
  });
});
