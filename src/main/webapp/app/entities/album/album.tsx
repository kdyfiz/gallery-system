import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Button, Table } from 'reactstrap';
import { JhiItemCount, JhiPagination, TextFormat, Translate, byteSize, getPaginationState, openFile } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faSort, faSortDown, faSortUp, faImages } from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { ASC, DESC, ITEMS_PER_PAGE, SORT } from 'app/shared/util/pagination.constants';
import { overridePaginationStateWithQueryParams } from 'app/shared/util/entity-utils';
import { useAppDispatch, useAppSelector } from 'app/config/store';

import { getEntities } from './album.reducer';

export const Album = () => {
  const dispatch = useAppDispatch();

  const pageLocation = useLocation();
  const navigate = useNavigate();

  const [paginationState, setPaginationState] = useState(
    overridePaginationStateWithQueryParams(getPaginationState(pageLocation, ITEMS_PER_PAGE, 'id'), pageLocation.search),
  );

  const albumList = useAppSelector(state => state.album.entities);
  const loading = useAppSelector(state => state.album.loading);
  const totalItems = useAppSelector(state => state.album.totalItems);

  const getAllEntities = () => {
    dispatch(
      getEntities({
        page: paginationState.activePage - 1,
        size: paginationState.itemsPerPage,
        sort: `${paginationState.sort},${paginationState.order}`,
      }),
    );
  };

  const sortEntities = () => {
    getAllEntities();
    const endURL = `?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`;
    if (pageLocation.search !== endURL) {
      navigate(`${pageLocation.pathname}${endURL}`);
    }
  };

  useEffect(() => {
    sortEntities();
  }, [paginationState.activePage, paginationState.order, paginationState.sort]);

  useEffect(() => {
    const params = new URLSearchParams(pageLocation.search);
    const page = params.get('page');
    const sort = params.get(SORT);
    if (page && sort) {
      const sortSplit = sort.split(',');
      setPaginationState({
        ...paginationState,
        activePage: +page,
        sort: sortSplit[0],
        order: sortSplit[1],
      });
    }
  }, [pageLocation.search]);

  const sort = p => () => {
    setPaginationState({
      ...paginationState,
      order: paginationState.order === ASC ? DESC : ASC,
      sort: p,
    });
  };

  const handlePagination = currentPage =>
    setPaginationState({
      ...paginationState,
      activePage: currentPage,
    });

  const handleSyncList = () => {
    sortEntities();
  };

  const getSortIconByFieldName = (fieldName: string) => {
    const sortFieldName = paginationState.sort;
    const order = paginationState.order;
    if (sortFieldName !== fieldName) {
      return faSort;
    }
    return order === ASC ? faSortUp : faSortDown;
  };

  return (
    <div>
      <h2 id="album-heading" data-cy="AlbumHeading">
        <Translate contentKey="gallerySystemApp.album.home.title">Albums</Translate>
        <div className="d-flex justify-content-end">
          <Button tag={Link} to="/album" className="me-2" color="success">
            <FontAwesomeIcon icon={faImages} className="me-1" />
            Gallery View
          </Button>
          <Button className="me-2" color="info" onClick={handleSyncList} disabled={loading}>
            <FontAwesomeIcon icon="sync" spin={loading} />{' '}
            <Translate contentKey="gallerySystemApp.album.home.refreshListLabel">Refresh List</Translate>
          </Button>
          <Link to="/album/new" className="btn btn-primary jh-create-entity" id="jh-create-entity" data-cy="entityCreateButton">
            <FontAwesomeIcon icon="plus" />
            &nbsp;
            <Translate contentKey="gallerySystemApp.album.home.createLabel">Create new Album</Translate>
          </Link>
        </div>
      </h2>
      <div className="table-responsive">
        {albumList && albumList.length > 0 ? (
          <Table responsive>
            <thead>
              <tr>
                <th className="hand" onClick={sort('id')}>
                  <Translate contentKey="gallerySystemApp.album.id">ID</Translate> <FontAwesomeIcon icon={getSortIconByFieldName('id')} />
                </th>
                <th className="hand" onClick={sort('name')}>
                  <Translate contentKey="gallerySystemApp.album.name">Name</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('name')} />
                </th>
                <th className="hand" onClick={sort('event')}>
                  <Translate contentKey="gallerySystemApp.album.event">Event</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('event')} />
                </th>
                <th className="hand" onClick={sort('creationDate')}>
                  <Translate contentKey="gallerySystemApp.album.creationDate">Creation Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('creationDate')} />
                </th>
                <th className="hand" onClick={sort('overrideDate')}>
                  <Translate contentKey="gallerySystemApp.album.overrideDate">Override Date</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('overrideDate')} />
                </th>
                <th className="hand" onClick={sort('thumbnail')}>
                  <Translate contentKey="gallerySystemApp.album.thumbnail">Thumbnail</Translate>{' '}
                  <FontAwesomeIcon icon={getSortIconByFieldName('thumbnail')} />
                </th>
                <th>
                  <Translate contentKey="gallerySystemApp.album.user">User</Translate> <FontAwesomeIcon icon="sort" />
                </th>
                <th />
              </tr>
            </thead>
            <tbody>
              {albumList.map((album, i) => (
                <tr key={`entity-${i}`} data-cy="entityTable">
                  <td>
                    <Button tag={Link} to={`/album/${album.id}`} color="link" size="sm">
                      {album.id}
                    </Button>
                  </td>
                  <td>{album.name}</td>
                  <td>{album.event}</td>
                  <td>{album.creationDate ? <TextFormat type="date" value={album.creationDate} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>{album.overrideDate ? <TextFormat type="date" value={album.overrideDate} format={APP_DATE_FORMAT} /> : null}</td>
                  <td>
                    {album.thumbnail ? (
                      <div>
                        {album.thumbnailContentType ? (
                          <a onClick={openFile(album.thumbnailContentType, album.thumbnail)}>
                            <img src={`data:${album.thumbnailContentType};base64,${album.thumbnail}`} style={{ maxHeight: '30px' }} />
                            &nbsp;
                          </a>
                        ) : null}
                        <span>
                          {album.thumbnailContentType}, {byteSize(album.thumbnail)}
                        </span>
                      </div>
                    ) : null}
                  </td>
                  <td>{album.user ? album.user.login : ''}</td>
                  <td className="text-end">
                    <div className="btn-group flex-btn-group-container">
                      <Button tag={Link} to={`/album/${album.id}`} color="info" size="sm" data-cy="entityDetailsButton">
                        <FontAwesomeIcon icon="eye" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.view">View</Translate>
                        </span>
                      </Button>
                      <Button
                        tag={Link}
                        to={`/album/${album.id}/edit?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`}
                        color="primary"
                        size="sm"
                        data-cy="entityEditButton"
                      >
                        <FontAwesomeIcon icon="pencil-alt" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.edit">Edit</Translate>
                        </span>
                      </Button>
                      <Button
                        onClick={() =>
                          (window.location.href = `/album/${album.id}/delete?page=${paginationState.activePage}&sort=${paginationState.sort},${paginationState.order}`)
                        }
                        color="danger"
                        size="sm"
                        data-cy="entityDeleteButton"
                      >
                        <FontAwesomeIcon icon="trash" />{' '}
                        <span className="d-none d-md-inline">
                          <Translate contentKey="entity.action.delete">Delete</Translate>
                        </span>
                      </Button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        ) : (
          !loading && (
            <div className="alert alert-warning">
              <Translate contentKey="gallerySystemApp.album.home.notFound">No Albums found</Translate>
            </div>
          )
        )}
      </div>
      {totalItems ? (
        <div className={albumList && albumList.length > 0 ? '' : 'd-none'}>
          <div className="justify-content-center d-flex">
            <JhiItemCount page={paginationState.activePage} total={totalItems} itemsPerPage={paginationState.itemsPerPage} i18nEnabled />
          </div>
          <div className="justify-content-center d-flex">
            <JhiPagination
              activePage={paginationState.activePage}
              onSelect={handlePagination}
              maxButtons={5}
              itemsPerPage={paginationState.itemsPerPage}
              totalItems={totalItems}
            />
          </div>
        </div>
      ) : (
        ''
      )}
    </div>
  );
};

export default Album;
