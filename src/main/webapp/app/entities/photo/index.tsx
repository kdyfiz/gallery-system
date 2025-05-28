import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Photo from './photo';
import PhotoDetail from './photo-detail';
import PhotoUpdate from './photo-update';
import PhotoDeleteDialog from './photo-delete-dialog';

const PhotoRoutes = () => (
  <ErrorBoundaryRoutes>
    <Route index element={<Photo />} />
    <Route path="new" element={<PhotoUpdate />} />
    <Route path=":id">
      <Route index element={<PhotoDetail />} />
      <Route path="edit" element={<PhotoUpdate />} />
      <Route path="delete" element={<PhotoDeleteDialog />} />
    </Route>
  </ErrorBoundaryRoutes>
);

export default PhotoRoutes;
