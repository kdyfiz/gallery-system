import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate, byteSize, openFile } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './photo.reducer';
import CommentSection from '../comment/comment-section';

export const PhotoDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const photoEntity = useAppSelector(state => state.photo.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="photoDetailsHeading">
          <Translate contentKey="gallerySystemApp.photo.detail.title">Photo</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{photoEntity.id}</dd>
          <dt>
            <span id="title">
              <Translate contentKey="gallerySystemApp.photo.title">Title</Translate>
            </span>
          </dt>
          <dd>{photoEntity.title}</dd>
          <dt>
            <span id="description">
              <Translate contentKey="gallerySystemApp.photo.description">Description</Translate>
            </span>
          </dt>
          <dd>{photoEntity.description}</dd>
          <dt>
            <span id="image">
              <Translate contentKey="gallerySystemApp.photo.image">Image</Translate>
            </span>
          </dt>
          <dd>
            {photoEntity.image ? (
              <div>
                {photoEntity.imageContentType ? (
                  <a onClick={openFile(photoEntity.imageContentType, photoEntity.image)}>
                    <img src={`data:${photoEntity.imageContentType};base64,${photoEntity.image}`} style={{ maxHeight: '30px' }} />
                  </a>
                ) : null}
                <span>
                  {photoEntity.imageContentType}, {byteSize(photoEntity.image)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="uploadDate">
              <Translate contentKey="gallerySystemApp.photo.uploadDate">Upload Date</Translate>
            </span>
          </dt>
          <dd>{photoEntity.uploadDate ? <TextFormat value={photoEntity.uploadDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="captureDate">
              <Translate contentKey="gallerySystemApp.photo.captureDate">Capture Date</Translate>
            </span>
          </dt>
          <dd>{photoEntity.captureDate ? <TextFormat value={photoEntity.captureDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="location">
              <Translate contentKey="gallerySystemApp.photo.location">Location</Translate>
            </span>
          </dt>
          <dd>{photoEntity.location}</dd>
          <dt>
            <span id="keywords">
              <Translate contentKey="gallerySystemApp.photo.keywords">Keywords</Translate>
            </span>
          </dt>
          <dd>{photoEntity.keywords}</dd>
          <dt>
            <Translate contentKey="gallerySystemApp.photo.album">Album</Translate>
          </dt>
          <dd>{photoEntity.album ? photoEntity.album.name : ''}</dd>
          <dt>
            <Translate contentKey="gallerySystemApp.photo.tags">Tags</Translate>
          </dt>
          <dd>
            {photoEntity.tags
              ? photoEntity.tags.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.name}</a>
                    {photoEntity.tags && i === photoEntity.tags.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/photo" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/photo/${photoEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
        {/* Comment Section */}
        {photoEntity.id && <CommentSection photoId={photoEntity.id} photoTitle={photoEntity.title} />}
      </Col>
    </Row>
  );
};

export default PhotoDetail;
