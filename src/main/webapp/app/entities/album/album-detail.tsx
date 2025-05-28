import React, { useEffect } from 'react';
import { Link, useParams } from 'react-router-dom';
import { Button, Col, Row } from 'reactstrap';
import { TextFormat, Translate, byteSize, openFile } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntity } from './album.reducer';

export const AlbumDetail = () => {
  const dispatch = useAppDispatch();

  const { id } = useParams<'id'>();

  useEffect(() => {
    dispatch(getEntity(id));
  }, []);

  const albumEntity = useAppSelector(state => state.album.entity);
  return (
    <Row>
      <Col md="8">
        <h2 data-cy="albumDetailsHeading">
          <Translate contentKey="gallerySystemApp.album.detail.title">Album</Translate>
        </h2>
        <dl className="jh-entity-details">
          <dt>
            <span id="id">
              <Translate contentKey="global.field.id">ID</Translate>
            </span>
          </dt>
          <dd>{albumEntity.id}</dd>
          <dt>
            <span id="name">
              <Translate contentKey="gallerySystemApp.album.name">Name</Translate>
            </span>
          </dt>
          <dd>{albumEntity.name}</dd>
          <dt>
            <span id="event">
              <Translate contentKey="gallerySystemApp.album.event">Event</Translate>
            </span>
          </dt>
          <dd>{albumEntity.event}</dd>
          <dt>
            <span id="creationDate">
              <Translate contentKey="gallerySystemApp.album.creationDate">Creation Date</Translate>
            </span>
          </dt>
          <dd>{albumEntity.creationDate ? <TextFormat value={albumEntity.creationDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="overrideDate">
              <Translate contentKey="gallerySystemApp.album.overrideDate">Override Date</Translate>
            </span>
          </dt>
          <dd>{albumEntity.overrideDate ? <TextFormat value={albumEntity.overrideDate} type="date" format={APP_DATE_FORMAT} /> : null}</dd>
          <dt>
            <span id="thumbnail">
              <Translate contentKey="gallerySystemApp.album.thumbnail">Thumbnail</Translate>
            </span>
          </dt>
          <dd>
            {albumEntity.thumbnail ? (
              <div>
                {albumEntity.thumbnailContentType ? (
                  <a onClick={openFile(albumEntity.thumbnailContentType, albumEntity.thumbnail)}>
                    <img src={`data:${albumEntity.thumbnailContentType};base64,${albumEntity.thumbnail}`} style={{ maxHeight: '30px' }} />
                  </a>
                ) : null}
                <span>
                  {albumEntity.thumbnailContentType}, {byteSize(albumEntity.thumbnail)}
                </span>
              </div>
            ) : null}
          </dd>
          <dt>
            <span id="keywords">
              <Translate contentKey="gallerySystemApp.album.keywords">Keywords</Translate>
            </span>
          </dt>
          <dd>{albumEntity.keywords}</dd>
          <dt>
            <Translate contentKey="gallerySystemApp.album.user">User</Translate>
          </dt>
          <dd>{albumEntity.user ? albumEntity.user.login : ''}</dd>
          <dt>
            <Translate contentKey="gallerySystemApp.album.tags">Tags</Translate>
          </dt>
          <dd>
            {albumEntity.tags
              ? albumEntity.tags.map((val, i) => (
                  <span key={val.id}>
                    <a>{val.name}</a>
                    {albumEntity.tags && i === albumEntity.tags.length - 1 ? '' : ', '}
                  </span>
                ))
              : null}
          </dd>
        </dl>
        <Button tag={Link} to="/album" replace color="info" data-cy="entityDetailsBackButton">
          <FontAwesomeIcon icon="arrow-left" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.back">Back</Translate>
          </span>
        </Button>
        &nbsp;
        <Button tag={Link} to={`/album/${albumEntity.id}/edit`} replace color="primary">
          <FontAwesomeIcon icon="pencil-alt" />{' '}
          <span className="d-none d-md-inline">
            <Translate contentKey="entity.action.edit">Edit</Translate>
          </span>
        </Button>
      </Col>
    </Row>
  );
};

export default AlbumDetail;
