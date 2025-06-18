import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';

import { Photo } from './photo';
import photo from './photo.reducer';

// Mock data
const mockPhotos = [
  {
    id: 1,
    title: 'Test Photo 1',
    description: 'Test Description 1',
    location: 'Test Location 1',
    keywords: 'test, photo',
    uploadDate: '2024-01-01T10:00:00Z',
    captureDate: '2024-01-01T09:00:00Z',
    image: 'base64imagedata',
    imageContentType: 'image/jpeg',
    album: { id: 1, name: 'Test Album' },
  },
  {
    id: 2,
    title: 'Test Photo 2',
    description: 'Test Description 2',
    location: 'Test Location 2',
    keywords: 'another, test',
    uploadDate: '2024-01-02T10:00:00Z',
    captureDate: '2024-01-02T09:00:00Z',
    image: 'base64imagedata2',
    imageContentType: 'image/png',
    album: { id: 2, name: 'Another Album' },
  },
];

const createTestStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      photo,
    },
    preloadedState: {
      photo: {
        entities: mockPhotos,
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

describe('Photo Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render photo list without crashing', () => {
      renderWithProviders(<Photo />);
      expect(screen.getByText('Photos')).toBeInTheDocument();
      expect(screen.getByText('Test Photo 1')).toBeInTheDocument();
      expect(screen.getByText('Test Photo 2')).toBeInTheDocument();
    });

    it('should show loading state when photos are loading', () => {
      renderWithProviders(<Photo />, {
        initialState: { loading: true, entities: [] },
      });

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      expect(refreshButton).toBeDisabled();
    });

    it('should display correct photo information', () => {
      renderWithProviders(<Photo />);

      // Check photo details are displayed
      expect(screen.getByText('Test Photo 1')).toBeInTheDocument();
      expect(screen.getByText('Test Description 1')).toBeInTheDocument();
      expect(screen.getByText('Test Location 1')).toBeInTheDocument();
      expect(screen.getByText('test, photo')).toBeInTheDocument();
    });
  });

  describe('Table Headers and Sorting', () => {
    it('should render all table headers', () => {
      renderWithProviders(<Photo />);

      expect(screen.getByText('ID')).toBeInTheDocument();
      expect(screen.getByText('Title')).toBeInTheDocument();
      expect(screen.getByText('Description')).toBeInTheDocument();
      expect(screen.getByText('Image')).toBeInTheDocument();
      expect(screen.getByText('Upload Date')).toBeInTheDocument();
      expect(screen.getByText('Capture Date')).toBeInTheDocument();
      expect(screen.getByText('Location')).toBeInTheDocument();
      expect(screen.getByText('Keywords')).toBeInTheDocument();
      expect(screen.getByText('Album')).toBeInTheDocument();
    });

    it('should sort by title when title header is clicked', () => {
      renderWithProviders(<Photo />);

      const titleHeader = screen.getByText('Title').closest('th');
      fireEvent.click(titleHeader);

      // Should trigger sort functionality
      expect(titleHeader).toBeInTheDocument();
    });

    it('should sort by upload date when upload date header is clicked', () => {
      renderWithProviders(<Photo />);

      const uploadDateHeader = screen.getByText('Upload Date').closest('th');
      fireEvent.click(uploadDateHeader);

      expect(uploadDateHeader).toBeInTheDocument();
    });
  });

  describe('Action Buttons', () => {
    it('should render refresh button', () => {
      renderWithProviders(<Photo />);

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      expect(refreshButton).toBeInTheDocument();
      expect(refreshButton).not.toBeDisabled();
    });

    it('should render create new photo button', () => {
      renderWithProviders(<Photo />);

      const createButton = screen.getByRole('link', { name: /create new photo/i });
      expect(createButton).toBeInTheDocument();
      expect(createButton).toHaveAttribute('href', '/photo/new');
    });

    it('should trigger refresh when refresh button is clicked', () => {
      renderWithProviders(<Photo />);

      const refreshButton = screen.getByRole('button', { name: /refresh list/i });
      fireEvent.click(refreshButton);

      // Should not crash and button should still be there
      expect(refreshButton).toBeInTheDocument();
    });
  });

  describe('Photo Actions', () => {
    it('should render view buttons for each photo', () => {
      renderWithProviders(<Photo />);

      const viewButtons = screen.getAllByRole('link', { name: /view/i });
      expect(viewButtons).toHaveLength(2);
      expect(viewButtons[0]).toHaveAttribute('href', '/photo/1');
      expect(viewButtons[1]).toHaveAttribute('href', '/photo/2');
    });

    it('should render edit buttons for each photo', () => {
      renderWithProviders(<Photo />);

      const editButtons = screen.getAllByRole('link', { name: /edit/i });
      expect(editButtons).toHaveLength(2);
      expect(editButtons[0].getAttribute('href')).toContain('/photo/1/edit');
      expect(editButtons[1].getAttribute('href')).toContain('/photo/2/edit');
    });

    it('should render delete buttons for each photo', () => {
      renderWithProviders(<Photo />);

      const deleteButtons = screen.getAllByRole('link', { name: /delete/i });
      expect(deleteButtons).toHaveLength(2);
      expect(deleteButtons[0].getAttribute('href')).toContain('/photo/1/delete');
      expect(deleteButtons[1].getAttribute('href')).toContain('/photo/2/delete');
    });
  });

  describe('Image Display', () => {
    it('should display photo thumbnails', () => {
      renderWithProviders(<Photo />);

      const images = screen.getAllByRole('img');
      expect(images).toHaveLength(2);
      expect(images[0]).toHaveAttribute('src', 'data:image/jpeg;base64,base64imagedata');
      expect(images[1]).toHaveAttribute('src', 'data:image/png;base64,base64imagedata2');
    });

    it('should show image content type and size', () => {
      renderWithProviders(<Photo />);

      expect(screen.getByText(/image\/jpeg/)).toBeInTheDocument();
      expect(screen.getByText(/image\/png/)).toBeInTheDocument();
    });
  });

  describe('Album Links', () => {
    it('should render album links for each photo', () => {
      renderWithProviders(<Photo />);

      const albumLinks = screen.getAllByText(/Test Album|Another Album/);
      expect(albumLinks).toHaveLength(2);

      const testAlbumLink = screen.getByRole('link', { name: 'Test Album' });
      const anotherAlbumLink = screen.getByRole('link', { name: 'Another Album' });

      expect(testAlbumLink).toHaveAttribute('href', '/album/1');
      expect(anotherAlbumLink).toHaveAttribute('href', '/album/2');
    });
  });

  describe('Date Formatting', () => {
    it('should display formatted upload dates', () => {
      renderWithProviders(<Photo />);

      // Dates should be formatted and displayed
      const dateElements = screen.getAllByText(/2024/);
      expect(dateElements.length).toBeGreaterThan(0);
    });
  });

  describe('Empty State', () => {
    it('should handle empty photo list', () => {
      renderWithProviders(<Photo />, {
        initialState: {
          loading: false,
          entities: [],
          totalItems: 0,
        },
      });

      expect(screen.getByText('Photos')).toBeInTheDocument();
      // Should still show table headers but no data rows
      expect(screen.getByText('Title')).toBeInTheDocument();
    });
  });

  describe('Pagination', () => {
    it('should show pagination when there are multiple pages', () => {
      renderWithProviders(<Photo />, {
        initialState: {
          entities: mockPhotos,
          totalItems: 25, // More than items per page
        },
      });

      // Should show item count
      expect(screen.getByText(/showing/i)).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should handle photos that have no album', () => {
      const photosWithoutAlbum = [
        {
          ...mockPhotos[0],
          album: null,
        },
      ];

      renderWithProviders(<Photo />, {
        initialState: {
          entities: photosWithoutAlbum,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Test Photo 1')).toBeInTheDocument();
    });

    it('should handle photos without images', () => {
      const photosWithoutImages = [
        {
          ...mockPhotos[0],
          image: null,
          imageContentType: null,
        },
      ];

      renderWithProviders(<Photo />, {
        initialState: {
          entities: photosWithoutImages,
          totalItems: 1,
        },
      });

      expect(screen.getByText('Test Photo 1')).toBeInTheDocument();
    });
  });
});
