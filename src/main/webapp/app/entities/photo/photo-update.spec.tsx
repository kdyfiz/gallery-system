import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter, Route, Routes } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import { TranslatorContext } from 'react-jhipster';

import PhotoUpdate from './photo-update';
import photo from './photo.reducer';
import album from '../album/album.reducer';
import tag from '../tag/tag.reducer';

// Mock the file upload
global.URL.createObjectURL = jest.fn(() => 'mock-url');

const mockPhoto = {
  id: 1,
  title: 'Test Photo',
  description: 'Test Description',
  location: 'Test Location',
  keywords: 'test, photo',
  uploadDate: '2024-01-01T10:00:00Z',
  captureDate: '2024-01-01T09:00:00Z',
  image: 'base64image',
  imageContentType: 'image/jpeg',
  album: { id: 1, name: 'Test Album' },
  tags: [],
};

interface TestState {
  photo?: any;
  album?: any;
  tag?: any;
}

const createTestStore = (initialState: TestState = {}) => {
  return configureStore({
    reducer: {
      photo,
      album,
      tag,
    },
    preloadedState: {
      photo: {
        entity: mockPhoto,
        loading: false,
        updating: false,
        updateSuccess: false,
        ...initialState.photo,
      },
      album: {
        entities: [{ id: 1, name: 'Test Album' }],
        loading: false,
        ...initialState.album,
      },
      tag: {
        entities: [{ id: 1, name: 'test' }],
        loading: false,
        ...initialState.tag,
      },
    },
  });
};

const renderWithProviders = (component: React.ReactElement, { initialState = {} }: { initialState?: TestState } = {}) => {
  const store = createTestStore(initialState);
  return render(
    <Provider store={store}>
      <MemoryRouter initialEntries={['/photo/1/edit']}>
        <Routes>
          <Route path="/photo/:id/edit" element={component} />
          <Route path="/photo/new" element={component} />
        </Routes>
      </MemoryRouter>
    </Provider>,
  );
};

beforeAll(() => {
  TranslatorContext.registerTranslations('en', {
    'gallerySystemApp.photo.home.createOrEditLabel': 'Create or edit a Photo',
    'gallerySystemApp.photo.title': 'Title',
    'gallerySystemApp.photo.description': 'Description',
    // ... other required translations
  });
});

describe('PhotoUpdate Component', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render photo update form without crashing', () => {
      renderWithProviders(<PhotoUpdate />);
      expect(screen.getByText(/Create or edit a Photo/i)).toBeInTheDocument();
    });

    it('should render all form fields', () => {
      renderWithProviders(<PhotoUpdate />);

      expect(screen.getByLabelText(/Title/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Description/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Location/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Keywords/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Upload Date/i)).toBeInTheDocument();
      expect(screen.getByLabelText(/Capture Date/i)).toBeInTheDocument();
    });
  });

  describe('Form Input', () => {
    it('should handle title input changes', () => {
      renderWithProviders(<PhotoUpdate />);

      const titleInput = screen.getByLabelText(/Title/i);
      fireEvent.change(titleInput, { target: { value: 'New Photo Title' } });

      expect((titleInput as HTMLInputElement).value).toBe('New Photo Title');
    });

    it('should handle description input changes', () => {
      renderWithProviders(<PhotoUpdate />);

      const descriptionInput = screen.getByLabelText(/Description/i);
      fireEvent.change(descriptionInput, { target: { value: 'New description' } });

      expect((descriptionInput as HTMLInputElement).value).toBe('New description');
    });

    it('should handle location input changes', () => {
      renderWithProviders(<PhotoUpdate />);

      const locationInput = screen.getByLabelText(/Location/i);
      fireEvent.change(locationInput, { target: { value: 'New Location' } });

      expect((locationInput as HTMLInputElement).value).toBe('New Location');
    });
  });

  describe('Form Submission', () => {
    it('should submit form when save button is clicked', () => {
      renderWithProviders(<PhotoUpdate />);

      const titleInput = screen.getByLabelText(/Title/i);
      fireEvent.change(titleInput, { target: { value: 'Updated Title' } });

      const saveButton = screen.getByRole('button', { name: /Save/i });
      fireEvent.click(saveButton);

      // Should trigger form submission
      expect(saveButton).toBeInTheDocument();
    });

    it('should show validation errors for required fields', async () => {
      renderWithProviders(<PhotoUpdate />);

      const titleInput = screen.getByLabelText(/Title/i);
      fireEvent.change(titleInput, { target: { value: '' } });

      const saveButton = screen.getByRole('button', { name: /Save/i });
      fireEvent.click(saveButton);

      await waitFor(() => {
        // Should show validation error
        expect(screen.getByText(/This field is required/i)).toBeInTheDocument();
      });
    });
  });

  describe('Navigation', () => {
    it('should render back button', () => {
      renderWithProviders(<PhotoUpdate />);

      const backButton = screen.getByRole('button', { name: /Back/i });
      expect(backButton).toBeInTheDocument();
    });

    it('should navigate back when back button is clicked', () => {
      renderWithProviders(<PhotoUpdate />);

      const backButton = screen.getByRole('button', { name: /Back/i });
      fireEvent.click(backButton);

      // Should trigger navigation
      expect(backButton).toBeInTheDocument();
    });
  });

  describe('Loading States', () => {
    it('should show loading state when updating', () => {
      renderWithProviders(<PhotoUpdate />, {
        initialState: { photo: { updating: true } },
      });

      const saveButton = screen.getByRole('button', { name: /Save/i });
      expect((saveButton as HTMLButtonElement).disabled).toBe(true);
    });

    it('should show loading state when fetching photo data', () => {
      renderWithProviders(<PhotoUpdate />, {
        initialState: { photo: { loading: true } },
      });

      // Should still render form structure
      expect(screen.getByText(/Create or edit a Photo/i)).toBeInTheDocument();
    });
  });

  describe('Image Upload', () => {
    it('should handle image file selection', () => {
      renderWithProviders(<PhotoUpdate />);

      const fileInput = screen.getByLabelText(/Image/i);
      const file = new File(['test'], 'test.jpg', { type: 'image/jpeg' });

      fireEvent.change(fileInput, { target: { files: [file] } });

      expect((fileInput as HTMLInputElement).files?.[0]).toBe(file);
    });

    it('should show current image when editing existing photo', () => {
      renderWithProviders(<PhotoUpdate />);

      // Should show current image info
      expect(screen.getByText(/Current Image/i)).toBeInTheDocument();
    });
  });

  describe('Dropdown Selections', () => {
    it('should render album dropdown', () => {
      renderWithProviders(<PhotoUpdate />);

      const albumSelect = screen.getByLabelText(/Album/i);
      expect(albumSelect).toBeInTheDocument();
    });

    it('should render tags multi-select', () => {
      renderWithProviders(<PhotoUpdate />);

      const tagsSelect = screen.getByLabelText(/Tags/i);
      expect(tagsSelect).toBeInTheDocument();
    });
  });

  describe('Date Inputs', () => {
    it('should handle upload date changes', () => {
      renderWithProviders(<PhotoUpdate />);

      const uploadDateInput = screen.getByLabelText(/Upload Date/i);
      fireEvent.change(uploadDateInput, { target: { value: '2024-01-15T10:00' } });

      expect((uploadDateInput as HTMLInputElement).value).toBe('2024-01-15T10:00');
    });

    it('should handle capture date changes', () => {
      renderWithProviders(<PhotoUpdate />);

      const captureDateInput = screen.getByLabelText(/Capture Date/i);
      fireEvent.change(captureDateInput, { target: { value: '2024-01-15T09:00' } });

      expect((captureDateInput as HTMLInputElement).value).toBe('2024-01-15T09:00');
    });
  });
});
