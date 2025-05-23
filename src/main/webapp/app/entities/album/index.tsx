import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Album from './album';
import AlbumGallery from './album-gallery';
import AlbumDetail from './album-detail';
import AlbumUpdate from './album-update';
import AlbumDeleteDialog from './album-delete-dialog';

const AlbumRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route path="gallery" element={<AlbumGallery />} />
    <Route path="list" element={<Album />} />
    <Route path="new" element={<AlbumUpdate />} />
    <Route path=":id">
      <Route index element={<AlbumDetail />} />
      <Route path="edit" element={<AlbumUpdate />} />
      <Route path="delete" element={<AlbumDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default AlbumRoutes;
