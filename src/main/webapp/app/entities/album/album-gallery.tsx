import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  Button,
  Card,
  CardImg,
  CardBody,
  CardTitle,
  CardText,
  Row,
  Col,
  Container,
  ButtonGroup,
  Badge,
  Spinner,
  Alert,
  Input,
  InputGroup,
  Form,
  FormGroup,
  Label,
  Collapse,
  UncontrolledDropdown,
  DropdownToggle,
  DropdownMenu,
  DropdownItem,
} from 'reactstrap';
import { TextFormat, Translate, openFile } from 'react-jhipster';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import {
  faCalendarAlt,
  faImage,
  faSync,
  faPlus,
  faList,
  faThLarge,
  faSortAlphaDown,
  faSortNumericDown,
  faSearch,
  faFilter,
  faTimes,
  faChevronDown,
  faChevronUp,
} from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getGalleryEntities, searchAndFilterAlbums } from './album.reducer';
import './album-gallery.scss';

export type AlbumSortType = 'EVENT' | 'DATE';

interface FilterCriteria {
  keyword?: string;
  event?: string;
  year?: number;
  tagName?: string;
  contributorLogin?: string;
}

export const AlbumGallery = () => {
  const dispatch = useAppDispatch();
  const location = useLocation();
  const navigate = useNavigate();

  const [sortType, setSortType] = useState<AlbumSortType>('EVENT');
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filters, setFilters] = useState<FilterCriteria>({});
  const [activeFiltersCount, setActiveFiltersCount] = useState(0);

  const albumList = useAppSelector(state => state.album.entities);
  const loading = useAppSelector(state => state.album.loading);
  const totalItems = useAppSelector(state => state.album.totalItems);

  useEffect(() => {
    loadAlbums();
  }, [dispatch, sortType, filters]);

  useEffect(() => {
    // Count active filters
    const count = Object.values(filters).filter(value => value !== undefined && value !== '').length;
    setActiveFiltersCount(count);
  }, [filters]);

  const loadAlbums = () => {
    const hasFilters = Object.values(filters).some(value => value !== undefined && value !== '');

    if (hasFilters || searchKeyword) {
      // Use search and filter endpoint
      dispatch(
        searchAndFilterAlbums({
          ...filters,
          keyword: searchKeyword || undefined,
          sortBy: sortType,
        }),
      );
    } else {
      // Use regular gallery endpoint
      dispatch(
        getGalleryEntities({
          sortBy: sortType,
        }),
      );
    }
  };

  const handleSortChange = (newSortType: AlbumSortType) => {
    setSortType(newSortType);
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadAlbums();
  };

  const handleFilterChange = (filterKey: keyof FilterCriteria, value: any) => {
    setFilters(prev => ({
      ...prev,
      [filterKey]: value || undefined,
    }));
  };

  const clearAllFilters = () => {
    setFilters({});
    setSearchKeyword('');
  };

  const handleRefresh = () => {
    loadAlbums();
  };

  const getGroupedAlbums = () => {
    if (!albumList || albumList.length === 0) return {};

    if (sortType === 'EVENT') {
      // Group by event, with "Miscellaneous" for albums without events
      const grouped = albumList.reduce(
        (acc, album) => {
          const eventKey = album.event && album.event.trim() !== '' ? album.event : 'Miscellaneous';
          if (!acc[eventKey]) {
            acc[eventKey] = [];
          }
          acc[eventKey].push(album);
          return acc;
        },
        {} as Record<string, any[]>,
      );

      // Sort each group's albums alphabetically by name
      Object.keys(grouped).forEach(key => {
        grouped[key].sort((a, b) => a.name.localeCompare(b.name));
      });

      return grouped;
    } else {
      // Group by date (year-month for better organization)
      const grouped = albumList.reduce(
        (acc, album) => {
          const date = album.overrideDate || album.creationDate;
          const dateKey = date ? new Date(date).toLocaleDateString('en-US', { year: 'numeric', month: 'long' }) : 'No Date';
          if (!acc[dateKey]) {
            acc[dateKey] = [];
          }
          acc[dateKey].push(album);
          return acc;
        },
        {} as Record<string, any[]>,
      );

      // Sort each group's albums by date (newest first)
      Object.keys(grouped).forEach(key => {
        grouped[key].sort((a, b) => {
          const dateA = new Date(a.overrideDate || a.creationDate);
          const dateB = new Date(b.overrideDate || b.creationDate);
          return dateB.getTime() - dateA.getTime();
        });
      });

      return grouped;
    }
  };

  const getDefaultThumbnail = () => {
    return 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgdmlld0JveD0iMCAwIDIwMCAyMDAiIGZpbGw9Im5vbmUiIHhtbG5zPSJodHRwOi8vd3d3LnczLm9yZy8yMDAwL3N2ZyI+CjxyZWN0IHdpZHRoPSIyMDAiIGhlaWdodD0iMjAwIiBmaWxsPSIjRkFGQUZBIi8+CjxwYXRoIGQ9Ik02MCA4MEwxMDAgNjBMMTQwIDgwTDE2MCA2MEwxNjAgMTQwSDQwVjgwWiIgZmlsbD0iI0U5RUNFRiIvPgo8Y2lyY2xlIGN4PSI3MCIgY3k9IjkwIiByPSIxMCIgZmlsbD0iI0Q1RDVENSIvPgo8L3N2Zz4K';
  };

  const renderAlbumCard = (album: any) => (
    <Col key={album.id} xs={12} sm={6} md={4} lg={3} className={`mb-4 ${viewMode === 'list' ? 'col-12' : ''}`}>
      <Card className="album-card h-100 shadow-sm">
        <div className="album-thumbnail-container">
          {album.thumbnail ? (
            <CardImg
              top
              src={`data:${album.thumbnailContentType};base64,${album.thumbnail}`}
              alt={album.name}
              className="album-thumbnail"
              onClick={() => album.thumbnailContentType && openFile(album.thumbnailContentType, album.thumbnail)()}
            />
          ) : (
            <CardImg top src={getDefaultThumbnail()} alt="Default thumbnail" className="album-thumbnail default-thumbnail" />
          )}
          <div className="album-overlay">
            <Button tag={Link} to={`/album/${album.id}`} color="light" size="sm" className="view-album-btn">
              <FontAwesomeIcon icon="eye" /> View
            </Button>
          </div>
        </div>
        <CardBody>
          <CardTitle tag="h5" className="album-title">
            <Link to={`/album/${album.id}`} className="text-decoration-none">
              {album.name}
            </Link>
          </CardTitle>
          {album.event && (
            <Badge color="primary" className="mb-2">
              <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
              {album.event}
            </Badge>
          )}
          <CardText className="album-date text-muted small">
            <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
            {album.overrideDate ? (
              <TextFormat type="date" value={album.overrideDate} format={APP_DATE_FORMAT} />
            ) : album.creationDate ? (
              <TextFormat type="date" value={album.creationDate} format={APP_DATE_FORMAT} />
            ) : (
              <>No date</>
            )}
          </CardText>
          {album.user && <CardText className="album-owner text-muted small">Created by: {album.user.login}</CardText>}
        </CardBody>
      </Card>
    </Col>
  );

  const groupedAlbums = getGroupedAlbums();
  const groupKeys = Object.keys(groupedAlbums).sort();

  return (
    <Container fluid className="album-gallery">
      {/* Header */}
      <div className="gallery-header mb-4">
        <Row className="align-items-center">
          <Col md={6}>
            <h1 className="gallery-title">
              <FontAwesomeIcon icon={faImage} className="me-2" />
              <Translate contentKey="gallerySystemApp.album.gallery.title">Photo Gallery</Translate>
            </h1>
            <p className="text-muted">
              <Translate contentKey="gallerySystemApp.album.gallery.subtitle">Browse and organize your photo albums</Translate>
            </p>
          </Col>
          <Col md={6} className="text-end">
            <Button tag={Link} to="/album/list" color="outline-info" className="me-2">
              <FontAwesomeIcon icon="list" className="me-1" />
              <Translate contentKey="gallerySystemApp.album.gallery.backToList">Back to List</Translate>
            </Button>
            <Button color="primary" tag={Link} to="/album/new" className="me-2">
              <FontAwesomeIcon icon={faPlus} className="me-1" />
              <Translate contentKey="gallerySystemApp.album.gallery.createAlbum">Create Album</Translate>
            </Button>
            <Button color="outline-secondary" onClick={handleRefresh} disabled={loading}>
              <FontAwesomeIcon icon={faSync} spin={loading} className="me-1" />
              <Translate contentKey="gallerySystemApp.album.gallery.refresh">Refresh</Translate>
            </Button>
          </Col>
        </Row>
      </div>

      {/* Controls */}
      <div className="gallery-controls mb-4">
        <Row className="align-items-center">
          <Col md={6}>
            <div className="sort-controls">
              <span className="me-3 fw-bold">
                <Translate contentKey="gallerySystemApp.album.gallery.sortBy">Sort by:</Translate>
              </span>
              <ButtonGroup>
                <Button
                  color={sortType === 'EVENT' ? 'primary' : 'outline-primary'}
                  onClick={() => handleSortChange('EVENT')}
                  className="sort-btn"
                >
                  <FontAwesomeIcon icon={faSortAlphaDown} className="me-1" />
                  <Translate contentKey="gallerySystemApp.album.gallery.sortByEvent">Event</Translate>
                </Button>
                <Button
                  color={sortType === 'DATE' ? 'primary' : 'outline-primary'}
                  onClick={() => handleSortChange('DATE')}
                  className="sort-btn"
                >
                  <FontAwesomeIcon icon={faSortNumericDown} className="me-1" />
                  <Translate contentKey="gallerySystemApp.album.gallery.sortByDate">Date</Translate>
                </Button>
              </ButtonGroup>
            </div>
          </Col>
          <Col md={6} className="text-end">
            <div className="view-controls">
              <span className="me-3 fw-bold">
                <Translate contentKey="gallerySystemApp.album.gallery.view">View:</Translate>
              </span>
              <ButtonGroup>
                <Button color={viewMode === 'grid' ? 'secondary' : 'outline-secondary'} onClick={() => setViewMode('grid')} size="sm">
                  <FontAwesomeIcon icon={faThLarge} />
                </Button>
                <Button color={viewMode === 'list' ? 'secondary' : 'outline-secondary'} onClick={() => setViewMode('list')} size="sm">
                  <FontAwesomeIcon icon={faList} />
                </Button>
              </ButtonGroup>
            </div>
          </Col>
        </Row>
      </div>

      {/* Search and Filter Section */}
      <div className="search-filter-section mb-4">
        <Row>
          <Col md={8}>
            <Form onSubmit={handleSearch}>
              <InputGroup>
                <Input
                  type="text"
                  placeholder="Search albums by name, keywords, or description..."
                  value={searchKeyword}
                  onChange={e => setSearchKeyword(e.target.value)}
                />
                <Button type="submit" color="primary">
                  <FontAwesomeIcon icon={faSearch} className="me-1" />
                  <Translate contentKey="gallerySystemApp.album.gallery.search">Search</Translate>
                </Button>
              </InputGroup>
            </Form>
          </Col>
          <Col md={4} className="text-end">
            <Button color="outline-secondary" onClick={() => setShowFilters(!showFilters)} className="me-2">
              <FontAwesomeIcon icon={faFilter} className="me-1" />
              <Translate contentKey="gallerySystemApp.album.gallery.filters">Filters</Translate>
              {activeFiltersCount > 0 && (
                <Badge color="primary" className="ms-1">
                  {activeFiltersCount}
                </Badge>
              )}
              <FontAwesomeIcon icon={showFilters ? faChevronUp : faChevronDown} className="ms-1" />
            </Button>
            {(activeFiltersCount > 0 || searchKeyword) && (
              <Button color="outline-danger" onClick={clearAllFilters} size="sm">
                <FontAwesomeIcon icon={faTimes} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.clearFilters">Clear All</Translate>
              </Button>
            )}
          </Col>
        </Row>

        <Collapse isOpen={showFilters}>
          <div className="filter-panel mt-3 p-3 border rounded bg-light">
            <Row>
              <Col md={3}>
                <FormGroup>
                  <Label for="eventFilter">
                    <Translate contentKey="gallerySystemApp.album.gallery.filterByEvent">Event</Translate>
                  </Label>
                  <Input
                    type="text"
                    id="eventFilter"
                    placeholder="Filter by event name..."
                    value={filters.event || ''}
                    onChange={e => handleFilterChange('event', e.target.value)}
                  />
                </FormGroup>
              </Col>
              <Col md={3}>
                <FormGroup>
                  <Label for="yearFilter">
                    <Translate contentKey="gallerySystemApp.album.gallery.filterByYear">Year</Translate>
                  </Label>
                  <Input
                    type="number"
                    id="yearFilter"
                    placeholder="Filter by year..."
                    value={filters.year || ''}
                    onChange={e => handleFilterChange('year', e.target.value ? parseInt(e.target.value, 10) : undefined)}
                  />
                </FormGroup>
              </Col>
              <Col md={3}>
                <FormGroup>
                  <Label for="tagFilter">
                    <Translate contentKey="gallerySystemApp.album.gallery.filterByTag">Tag</Translate>
                  </Label>
                  <Input
                    type="text"
                    id="tagFilter"
                    placeholder="Filter by tag name..."
                    value={filters.tagName || ''}
                    onChange={e => handleFilterChange('tagName', e.target.value)}
                  />
                </FormGroup>
              </Col>
              <Col md={3}>
                <FormGroup>
                  <Label for="contributorFilter">
                    <Translate contentKey="gallerySystemApp.album.gallery.filterByContributor">Contributor</Translate>
                  </Label>
                  <Input
                    type="text"
                    id="contributorFilter"
                    placeholder="Filter by contributor..."
                    value={filters.contributorLogin || ''}
                    onChange={e => handleFilterChange('contributorLogin', e.target.value)}
                  />
                </FormGroup>
              </Col>
            </Row>
          </div>
        </Collapse>
      </div>

      {/* Loading */}
      {loading && (
        <div className="text-center py-5">
          <Spinner color="primary" size="lg" />
          <p className="mt-2">
            <Translate contentKey="gallerySystemApp.album.gallery.loading">Loading albums...</Translate>
          </p>
        </div>
      )}

      {/* Gallery Content */}
      {!loading && (
        <>
          {albumList && albumList.length > 0 ? (
            <div className="gallery-content">
              {groupKeys.map(groupKey => (
                <div key={groupKey} className="album-group mb-5">
                  <div className="group-header mb-3">
                    <h3 className="group-title">
                      {groupKey}
                      <Badge color="secondary" className="ms-2">
                        {groupedAlbums[groupKey].length} albums
                      </Badge>
                    </h3>
                    {sortType === 'EVENT' && groupKey === 'Miscellaneous' && (
                      <p className="text-muted small">
                        <Translate contentKey="gallerySystemApp.album.gallery.miscellaneousDesc">Albums without a specific event</Translate>
                      </p>
                    )}
                  </div>
                  <Row>{groupedAlbums[groupKey].map(renderAlbumCard)}</Row>
                </div>
              ))}
            </div>
          ) : (
            <Alert color="info" className="text-center py-5">
              <FontAwesomeIcon icon={faImage} size="3x" className="mb-3 text-muted" />
              <h4>
                <Translate contentKey="gallerySystemApp.album.gallery.noAlbums">No albums found</Translate>
              </h4>
              <p>
                <Translate contentKey="gallerySystemApp.album.gallery.createFirst">Create your first album to get started!</Translate>
              </p>
              <Button color="primary" tag={Link} to="/album/new" className="mt-2">
                <FontAwesomeIcon icon={faPlus} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.createAlbum">Create Album</Translate>
              </Button>
            </Alert>
          )}
        </>
      )}
    </Container>
  );
};

export default AlbumGallery;
