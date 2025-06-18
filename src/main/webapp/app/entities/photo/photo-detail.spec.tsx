import React from 'react';
import { render, screen } from '@testing-library/react';
import '@testing-library/jest-dom';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';

import { PhotoDetail } from './photo-detail';
import photo from './photo.reducer';

// Mock data
const mockPhoto = {
  id: 1,
  title: 'Beautiful Sunset',
  description: 'A stunning sunset over the mountains with vibrant colors',
  location: 'Mountain View, California',
  keywords: 'sunset, mountains, nature, photography',
  uploadDate: '2024-01-15T14:30:00Z',
  captureDate: '2024-01-15T18:45:00Z',
  image: 'base64encodedimagedata',
  imageContentType: 'image/jpeg',
  album: {
    id: 5,
    name: 'Nature Photography',
    description: 'Collection of nature photos',
  },
  tags: [
    { id: 1, name: 'sunset' },
    { id: 2, name: 'nature' },
  ],
};

const createTestStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      photo,
    },
    preloadedState: {
      photo: {
        entity: mockPhoto,
        loading: false,
        ...initialState,
      },
    },
  });
};

const renderWithProviders = (component, { initialState = {} } = {}) => {
  const store = createTestStore(initialState);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/photo/1']}>
        <Routes>
          <Route path="/photo/:id" element={component} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  );
};

describe('PhotoDetail Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render photo details without crashing', () => {
      renderWithProviders(<PhotoDetail />);
      expect(screen.getByText('Photo')).toBeInTheDocument();
      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
    });

    it('should show loading state when photo is loading', () => {
      renderWithProviders(<PhotoDetail />, {
        initialState: {
          loading: true,
          entity: {},
        },
      });

      // Should still render the component structure
      expect(screen.getByText('Photo')).toBeInTheDocument();
    });

    it('should display all photo information', () => {
      renderWithProviders(<PhotoDetail />);

      // Check all photo details are displayed
      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
      expect(screen.getByText('A stunning sunset over the mountains with vibrant colors')).toBeInTheDocument();
      expect(screen.getByText('Mountain View, California')).toBeInTheDocument();
      expect(screen.getByText('sunset, mountains, nature, photography')).toBeInTheDocument();
    });
  });

  describe('Photo Image Display', () => {
    it('should display the photo image', () => {
      renderWithProviders(<PhotoDetail />);

      const image = screen.getByRole('img');
      expect(image).toBeInTheDocument();
      expect(image).toHaveAttribute('src', 'data:image/jpeg;base64,base64encodedimagedata');
    });

    it('should handle photos without images gracefully', () => {
      const photoWithoutImage = {
        ...mockPhoto,
        image: null,
        imageContentType: null,
      };

      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: photoWithoutImage,
          loading: false,
        },
      });

      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
      // Should not crash when no image is present
    });

    it('should show image content type and allow download', () => {
      renderWithProviders(<PhotoDetail />);

      // Should display image content type
      expect(screen.getByText(/image\/jpeg/)).toBeInTheDocument();
    });
  });

  describe('Date Display', () => {
    it('should display formatted upload date', () => {
      renderWithProviders(<PhotoDetail />);

      // Should show formatted date
      expect(screen.getByText(/2024/)).toBeInTheDocument();
    });

    it('should display formatted capture date', () => {
      renderWithProviders(<PhotoDetail />);

      // Should show formatted capture date
      const dateElements = screen.getAllByText(/2024/);
      expect(dateElements.length).toBeGreaterThan(0);
    });

    it('should handle missing dates gracefully', () => {
      const photoWithoutDates = {
        ...mockPhoto,
        uploadDate: null,
        captureDate: null,
      };

      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: photoWithoutDates,
          loading: false,
        },
      });

      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
    });
  });

  describe('Album Information', () => {
    it('should display album link when photo belongs to an album', () => {
      renderWithProviders(<PhotoDetail />);

      const albumLink = screen.getByRole('link', { name: 'Nature Photography' });
      expect(albumLink).toBeInTheDocument();
      expect(albumLink).toHaveAttribute('href', '/album/5');
    });

    it('should handle photos without albums', () => {
      const photoWithoutAlbum = {
        ...mockPhoto,
        album: null,
      };

      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: photoWithoutAlbum,
          loading: false,
        },
      });

      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
    });
  });

  describe('Tags Display', () => {
    it('should display photo tags when available', () => {
      renderWithProviders(<PhotoDetail />);

      // Should show tags if they exist
      if (mockPhoto.tags) {
        expect(screen.getByText('sunset')).toBeInTheDocument();
        expect(screen.getByText('nature')).toBeInTheDocument();
      }
    });

    it('should handle photos without tags', () => {
      const photoWithoutTags = {
        ...mockPhoto,
        tags: [],
      };

      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: photoWithoutTags,
          loading: false,
        },
      });

      expect(screen.getByText('Beautiful Sunset')).toBeInTheDocument();
    });
  });

  describe('Action Buttons', () => {
    it('should render back button', () => {
      renderWithProviders(<PhotoDetail />);

      const backButton = screen.getByRole('button', { name: /back/i });
      expect(backButton).toBeInTheDocument();
    });

    it('should render edit button', () => {
      renderWithProviders(<PhotoDetail />);

      const editButton = screen.getByRole('link', { name: /edit/i });
      expect(editButton).toBeInTheDocument();
      expect(editButton.getAttribute('href')).toContain('/photo/1/edit');
    });
  });

  describe('Field Labels', () => {
    it('should display all field labels', () => {
      renderWithProviders(<PhotoDetail />);

      // Check that labels are present
      expect(screen.getByText(/title/i)).toBeInTheDocument();
      expect(screen.getByText(/description/i)).toBeInTheDocument();
      expect(screen.getByText(/location/i)).toBeInTheDocument();
      expect(screen.getByText(/keywords/i)).toBeInTheDocument();
      expect(screen.getByText(/upload date/i)).toBeInTheDocument();
      expect(screen.getByText(/capture date/i)).toBeInTheDocument();
    });
  });

  describe('Empty State', () => {
    it('should handle empty photo entity', () => {
      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: {},
          loading: false,
        },
      });

      expect(screen.getByText('Photo')).toBeInTheDocument();
    });
  });

  describe('Navigation', () => {
    it('should handle navigation with photo ID in URL', () => {
      // Component should work with URL parameter
      renderWithProviders(<PhotoDetail />);
      expect(screen.getByText('Photo')).toBeInTheDocument();
    });
  });

  describe('Error Handling', () => {
    it('should handle photo with missing required fields', () => {
      const incompletePhoto = {
        id: 1,
        // Missing title and other fields
      };

      renderWithProviders(<PhotoDetail />, {
        initialState: {
          entity: incompletePhoto,
          loading: false,
        },
      });

      expect(screen.getByText('Photo')).toBeInTheDocument();
    });
  });

  describe('Accessibility', () => {
    it('should have proper heading structure', () => {
      renderWithProviders(<PhotoDetail />);

      const heading = screen.getByRole('heading', { level: 2 });
      expect(heading).toBeInTheDocument();
      expect(heading).toHaveAttribute('data-cy', 'photoDetailsHeading');
    });

    it('should have proper button roles', () => {
      renderWithProviders(<PhotoDetail />);

      const backButton = screen.getByRole('button');
      expect(backButton).toBeInTheDocument();
    });
  });
});
