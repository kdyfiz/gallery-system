import React from 'react';
import { Route } from 'react-router';

import ErrorBoundaryRoutes from 'app/shared/error/error-boundary-routes';

import Album from './album';
import Tag from './tag';
import Photo from './photo';
import AlbumGallery from './album/album-gallery';
/* jhipster-needle-add-route-import - JHipster will add routes here */

export default () => {
  return (
    <div>
      <ErrorBoundaryRoutes>
        {/* prettier-ignore */}
        <Route path="album-gallery" element={<AlbumGallery />} />
        <Route path="album/*" element={<Album />} />
        <Route path="tag/*" element={<Tag />} />
        <Route path="photo/*" element={<Photo />} />
        {/* jhipster-needle-add-route-path - JHipster will add routes here */}
      </ErrorBoundaryRoutes>
    </div>
  );
};
