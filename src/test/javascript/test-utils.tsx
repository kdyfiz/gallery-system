import React from 'react';
import { render, RenderOptions } from '@testing-library/react';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import { configureStore } from '@reduxjs/toolkit';
import { TranslatorContext } from 'react-jhipster';

// Import all reducers
import photo from 'app/entities/photo/photo.reducer';
import album from 'app/entities/album/album.reducer';
import tag from 'app/entities/tag/tag.reducer';
import authentication from 'app/shared/reducers/authentication';
import locale from 'app/shared/reducers/locale';

// Default test store configuration
export const createTestStore = (preloadedState = {}) => {
  return configureStore({
    reducer: {
      photo,
      album,
      tag,
      authentication,
      locale,
    },
    preloadedState,
  });
};

// Custom render function that includes providers
interface CustomRenderOptions extends Omit<RenderOptions, 'wrapper'> {
  preloadedState?: any;
  store?: any;
  initialEntries?: string[];
}

export const renderWithProviders = (
  ui: React.ReactElement,
  { preloadedState = {}, store = createTestStore(preloadedState), initialEntries = ['/'], ...renderOptions }: CustomRenderOptions = {},
) => {
  function Wrapper({ children }: { children?: React.ReactNode }) {
    return (
      <Provider store={store}>
        <MemoryRouter initialEntries={initialEntries}>{children}</MemoryRouter>
      </Provider>
    );
  }

  return { store, ...render(ui, { wrapper: Wrapper, ...renderOptions }) };
};

// Setup translations for tests
export const setupTestTranslations = () => {
  TranslatorContext.registerTranslations('en', {
    // Photo entity translations
    'gallerySystemApp.photo.home.title': 'Photos',
    'gallerySystemApp.photo.home.createLabel': 'Create a new Photo',
    'gallerySystemApp.photo.home.createOrEditLabel': 'Create or edit a Photo',
    'gallerySystemApp.photo.home.refreshListLabel': 'Refresh List',
    'gallerySystemApp.photo.title': 'Title',
    'gallerySystemApp.photo.description': 'Description',
    'gallerySystemApp.photo.location': 'Location',
    'gallerySystemApp.photo.keywords': 'Keywords',
    'gallerySystemApp.photo.uploadDate': 'Upload Date',
    'gallerySystemApp.photo.captureDate': 'Capture Date',
    'gallerySystemApp.photo.image': 'Image',
    'gallerySystemApp.photo.album': 'Album',
    'gallerySystemApp.photo.tags': 'Tags',

    // Album entity translations
    'gallerySystemApp.album.home.title': 'Albums',
    'gallerySystemApp.album.home.createLabel': 'Create a new Album',
    'gallerySystemApp.album.home.createOrEditLabel': 'Create or edit an Album',
    'gallerySystemApp.album.name': 'Name',
    'gallerySystemApp.album.description': 'Description',
    'gallerySystemApp.album.createdDate': 'Created Date',
    'gallerySystemApp.album.event': 'Event',

    // Tag entity translations
    'gallerySystemApp.tag.home.title': 'Tags',
    'gallerySystemApp.tag.name': 'Name',
    'gallerySystemApp.tag.color': 'Color',

    // Common entity actions
    'entity.action.save': 'Save',
    'entity.action.cancel': 'Cancel',
    'entity.action.delete': 'Delete',
    'entity.action.edit': 'Edit',
    'entity.action.view': 'View',
    'entity.action.back': 'Back',

    // Validation messages
    'entity.validation.required': 'This field is required.',
    'entity.validation.minlength': 'This field is required to be at least {{min}} characters.',
    'entity.validation.maxlength': 'This field cannot be longer than {{max}} characters.',

    // Error messages
    'error.title': 'Error',
    'error.http.400': 'Bad request',
    'error.http.401': 'Unauthorized',
    'error.http.403': 'Forbidden',
    'error.http.404': 'Not found',
    'error.http.500': 'Internal server error',

    // General UI
    'global.title': 'Gallery System',
    'global.menu.home': 'Home',
  });
};

// Mock data factories
export const createMockPhoto = (overrides = {}) => ({
  id: 1,
  title: 'Test Photo',
  description: 'Test Description',
  location: 'Test Location',
  keywords: 'test, photo',
  uploadDate: '2024-01-01T10:00:00Z',
  captureDate: '2024-01-01T09:00:00Z',
  image: 'base64imagedata',
  imageContentType: 'image/jpeg',
  album: { id: 1, name: 'Test Album' },
  tags: [],
  ...overrides,
});

export const createMockAlbum = (overrides = {}) => ({
  id: 1,
  name: 'Test Album',
  description: 'Test Description',
  creationDate: '2024-01-01T00:00:00Z',
  event: 'Test Event',
  user: { login: 'testuser' },
  photos: [],
  ...overrides,
});

export const createMockTag = (overrides = {}) => ({
  id: 1,
  name: 'test',
  color: '#FF0000',
  ...overrides,
});

// Common test scenarios
export const getDefaultPhotoState = (overrides = {}) => ({
  entities: [createMockPhoto()],
  entity: createMockPhoto(),
  loading: false,
  updating: false,
  updateSuccess: false,
  totalItems: 1,
  ...overrides,
});

export const getDefaultAlbumState = (overrides = {}) => ({
  entities: [createMockAlbum()],
  entity: createMockAlbum(),
  loading: false,
  updating: false,
  updateSuccess: false,
  totalItems: 1,
  ...overrides,
});

export const getDefaultTagState = (overrides = {}) => ({
  entities: [createMockTag()],
  entity: createMockTag(),
  loading: false,
  updating: false,
  updateSuccess: false,
  totalItems: 1,
  ...overrides,
});

// Setup function to be called in beforeAll
export const setupTestEnvironment = () => {
  setupTestTranslations();

  // Mock URL.createObjectURL
  global.URL.createObjectURL = jest.fn(() => 'mock-url');
  global.URL.revokeObjectURL = jest.fn();
};
