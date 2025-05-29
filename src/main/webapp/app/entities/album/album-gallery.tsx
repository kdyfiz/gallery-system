import React, { useEffect, useState, useMemo } from 'react';
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
  Modal,
  ModalHeader,
  ModalBody,
  ModalFooter,
  Progress,
  Breadcrumb,
  BreadcrumbItem,
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
  faEye,
  faUser,
  faTag,
  faHome,
  faCog,
  faDownload,
  faShare,
  faHeart,
  faEdit,
  faTrash,
} from '@fortawesome/free-solid-svg-icons';
import { APP_DATE_FORMAT } from 'app/config/constants';
import { useAppDispatch, useAppSelector } from 'app/config/store';
import { getGalleryEntities, searchAndFilterAlbums } from './album.reducer';
import './album-gallery.scss';

export type AlbumSortType = 'EVENT' | 'DATE';
export type ViewMode = 'grid' | 'list' | 'masonry';

interface FilterCriteria {
  keyword?: string;
  event?: string;
  year?: number;
  tagName?: string;
  contributorLogin?: string;
}

interface AlbumStats {
  totalAlbums: number;
  totalPhotos: number;
  recentAlbums: number;
  contributors: number;
}

export const AlbumGallery = () => {
  const dispatch = useAppDispatch();
  const location = useLocation();
  const navigate = useNavigate();

  // Core state
  const [sortType, setSortType] = useState<AlbumSortType>('EVENT');
  const [viewMode, setViewMode] = useState<ViewMode>('grid');
  const [showFilters, setShowFilters] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [filters, setFilters] = useState<FilterCriteria>({});
  const [activeFiltersCount, setActiveFiltersCount] = useState(0);

  // UI state
  const [showQuickActions, setShowQuickActions] = useState(false);
  const [selectedAlbums, setSelectedAlbums] = useState<Set<number>>(new Set());
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [isSearchFocused, setIsSearchFocused] = useState(false);

  // Redux state
  const albumList = useAppSelector(state => state.album.entities);
  const loading = useAppSelector(state => state.album.loading);
  const totalItems = useAppSelector(state => state.album.totalItems);

  // Computed values
  const albumStats = useMemo<AlbumStats>(() => {
    if (!albumList || albumList.length === 0) {
      return { totalAlbums: 0, totalPhotos: 0, recentAlbums: 0, contributors: 0 };
    }

    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const contributors = new Set(albumList.map(album => album.user?.login).filter(Boolean));

    return {
      totalAlbums: albumList.length,
      totalPhotos: albumList.reduce((sum, album) => sum + (album.photos?.length || 0), 0),
      recentAlbums: albumList.filter(album => album.creationDate && new Date(album.creationDate) > oneWeekAgo).length,
      contributors: contributors.size,
    };
  }, [albumList]);

  useEffect(() => {
    loadAlbums();
  }, [dispatch, sortType, filters]);

  useEffect(() => {
    const count = Object.values(filters).filter(value => value !== undefined && value !== '').length;
    setActiveFiltersCount(count);
  }, [filters]);

  const loadAlbums = () => {
    const hasFilters = Object.values(filters).some(value => value !== undefined && value !== '');

    if (hasFilters || searchKeyword) {
      dispatch(
        searchAndFilterAlbums({
          ...filters,
          keyword: searchKeyword || undefined,
          sortBy: sortType,
        }),
      );
    } else {
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

  const handleViewModeChange = (newViewMode: ViewMode) => {
    setViewMode(newViewMode);
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

  const toggleAlbumSelection = (albumId: number) => {
    setSelectedAlbums(prev => {
      const newSet = new Set(prev);
      if (newSet.has(albumId)) {
        newSet.delete(albumId);
      } else {
        newSet.add(albumId);
      }
      return newSet;
    });
  };

  const selectAllAlbums = () => {
    if (albumList) {
      setSelectedAlbums(new Set(albumList.map(album => album.id)));
    }
  };

  const clearSelection = () => {
    setSelectedAlbums(new Set());
  };

  const getGroupedAlbums = () => {
    if (!albumList || albumList.length === 0) return {};

    if (sortType === 'EVENT') {
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

      Object.keys(grouped).forEach(key => {
        grouped[key].sort((a, b) => a.name.localeCompare(b.name));
      });

      return grouped;
    } else {
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

  const renderAlbumCard = (album: any, index: number) => {
    const isSelected = selectedAlbums.has(album.id);
    const cardClass = `album-card h-100 shadow-sm ${isSelected ? 'selected' : ''} ${viewMode === 'list' ? 'list-view' : ''}`;

    return (
      <Col
        key={album.id}
        xs={12}
        sm={viewMode === 'list' ? 12 : 6}
        md={viewMode === 'list' ? 12 : viewMode === 'masonry' ? 6 : 4}
        lg={viewMode === 'list' ? 12 : viewMode === 'masonry' ? 4 : 3}
        className="mb-4"
        data-testid={`album-card-${album.id}`}
      >
        <Card className={cardClass}>
          {viewMode === 'list' ? (
            // List view layout
            <Row className="g-0">
              <Col md={3}>
                <div className="album-thumbnail-container">
                  {album.thumbnail ? (
                    <CardImg
                      src={`data:${album.thumbnailContentType};base64,${album.thumbnail}`}
                      alt={album.name}
                      className="album-thumbnail list-thumbnail"
                      data-testid={`album-thumbnail-${album.id}`}
                      onClick={() => album.thumbnailContentType && openFile(album.thumbnailContentType, album.thumbnail)()}
                    />
                  ) : (
                    <CardImg
                      src={getDefaultThumbnail()}
                      alt="Default thumbnail"
                      className="album-thumbnail list-thumbnail default-thumbnail"
                      data-testid={`album-default-thumbnail-${album.id}`}
                    />
                  )}
                </div>
              </Col>
              <Col md={9}>
                <CardBody className="d-flex justify-content-between align-items-center">
                  <div className="album-info">
                    <CardTitle tag="h5" className="album-title mb-2">
                      <Link to={`/album/${album.id}`} className="text-decoration-none" data-testid={`album-title-link-${album.id}`}>
                        {album.name}
                      </Link>
                    </CardTitle>
                    <div className="album-meta d-flex flex-wrap gap-2 mb-2">
                      {album.event && (
                        <Badge color="primary" data-testid={`album-event-badge-${album.id}`}>
                          <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                          {album.event}
                        </Badge>
                      )}
                      {album.tags && album.tags.length > 0 && (
                        <Badge color="secondary" data-testid={`album-tags-badge-${album.id}`}>
                          <FontAwesomeIcon icon={faTag} className="me-1" />
                          {album.tags.length} tags
                        </Badge>
                      )}
                      {album.photos && (
                        <Badge color="info" data-testid={`album-photos-count-${album.id}`}>
                          <FontAwesomeIcon icon={faImage} className="me-1" />
                          {album.photos.length} photos
                        </Badge>
                      )}
                    </div>
                    <CardText className="album-date text-muted small mb-1">
                      <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                      {album.overrideDate ? (
                        <TextFormat type="date" value={album.overrideDate} format={APP_DATE_FORMAT} />
                      ) : album.creationDate ? (
                        <TextFormat type="date" value={album.creationDate} format={APP_DATE_FORMAT} />
                      ) : (
                        'No date'
                      )}
                    </CardText>
                    {album.user && (
                      <CardText className="album-owner text-muted small mb-0">
                        <FontAwesomeIcon icon={faUser} className="me-1" />
                        Created by: {album.user.login}
                      </CardText>
                    )}
                    {album.description && (
                      <CardText className="album-description text-muted small mt-2">
                        {album.description.length > 100 ? `${album.description.substring(0, 100)}...` : album.description}
                      </CardText>
                    )}
                  </div>
                  <div className="album-actions">
                    <ButtonGroup vertical>
                      <Button tag={Link} to={`/album/${album.id}`} color="primary" size="sm" data-testid={`view-album-btn-${album.id}`}>
                        <FontAwesomeIcon icon={faEye} className="me-1" />
                        View
                      </Button>
                      <Button
                        tag={Link}
                        to={`/album/${album.id}/edit`}
                        color="outline-secondary"
                        size="sm"
                        data-testid={`edit-album-btn-${album.id}`}
                      >
                        <FontAwesomeIcon icon={faEdit} className="me-1" />
                        Edit
                      </Button>
                    </ButtonGroup>
                  </div>
                </CardBody>
              </Col>
            </Row>
          ) : (
            // Grid/Masonry view layout
            <>
              <div className="album-thumbnail-container position-relative">
                {album.thumbnail ? (
                  <CardImg
                    top
                    src={`data:${album.thumbnailContentType};base64,${album.thumbnail}`}
                    alt={album.name}
                    className="album-thumbnail"
                    data-testid={`album-thumbnail-${album.id}`}
                    onClick={() => album.thumbnailContentType && openFile(album.thumbnailContentType, album.thumbnail)()}
                  />
                ) : (
                  <CardImg
                    top
                    src={getDefaultThumbnail()}
                    alt="Default thumbnail"
                    className="album-thumbnail default-thumbnail"
                    data-testid={`album-default-thumbnail-${album.id}`}
                  />
                )}
                <div className="album-overlay">
                  <div className="overlay-actions">
                    <Button
                      tag={Link}
                      to={`/album/${album.id}`}
                      color="light"
                      size="sm"
                      className="me-2"
                      data-testid={`view-album-btn-${album.id}`}
                    >
                      <FontAwesomeIcon icon={faEye} />
                    </Button>
                    <Button
                      color="outline-light"
                      size="sm"
                      onClick={() => toggleAlbumSelection(album.id)}
                      data-testid={`select-album-btn-${album.id}`}
                    >
                      <FontAwesomeIcon icon={isSelected ? faTimes : faHeart} />
                    </Button>
                  </div>
                  {album.photos && (
                    <Badge color="dark" className="photo-count-badge">
                      <FontAwesomeIcon icon={faImage} className="me-1" />
                      {album.photos.length}
                    </Badge>
                  )}
                </div>
              </div>
              <CardBody>
                <CardTitle tag="h5" className="album-title">
                  <Link to={`/album/${album.id}`} className="text-decoration-none" data-testid={`album-title-link-${album.id}`}>
                    {album.name}
                  </Link>
                </CardTitle>

                {album.event && (
                  <Badge color="primary" className="mb-2" data-testid={`album-event-badge-${album.id}`}>
                    <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                    {album.event}
                  </Badge>
                )}

                {album.tags && album.tags.length > 0 && (
                  <div className="album-tags mb-2">
                    {album.tags.slice(0, 3).map((tag: any, tagIndex: number) => (
                      <Badge key={tag.id} color="outline-secondary" className="me-1 mb-1" data-testid={`album-tag-${album.id}-${tag.id}`}>
                        <FontAwesomeIcon icon={faTag} className="me-1" />
                        {tag.name}
                      </Badge>
                    ))}
                    {album.tags.length > 3 && (
                      <Badge color="light" className="me-1 mb-1">
                        +{album.tags.length - 3} more
                      </Badge>
                    )}
                  </div>
                )}

                <CardText className="album-date text-muted small">
                  <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                  {album.overrideDate ? (
                    <TextFormat type="date" value={album.overrideDate} format={APP_DATE_FORMAT} />
                  ) : album.creationDate ? (
                    <TextFormat type="date" value={album.creationDate} format={APP_DATE_FORMAT} />
                  ) : (
                    'No date'
                  )}
                </CardText>

                {album.user && (
                  <CardText className="album-owner text-muted small">
                    <FontAwesomeIcon icon={faUser} className="me-1" />
                    Created by: {album.user.login}
                  </CardText>
                )}

                {album.description && (
                  <CardText className="album-description text-muted small mt-2">
                    {album.description.length > 60 ? `${album.description.substring(0, 60)}...` : album.description}
                  </CardText>
                )}
              </CardBody>
            </>
          )}
        </Card>
      </Col>
    );
  };

  const groupedAlbums = getGroupedAlbums();
  const groupKeys = Object.keys(groupedAlbums).sort();

  return (
    <Container fluid className="album-gallery" data-testid="album-gallery">
      {/* Breadcrumb */}
      <Breadcrumb className="mb-3">
        <BreadcrumbItem>
          <Link to="/" data-testid="breadcrumb-home">
            <FontAwesomeIcon icon={faHome} className="me-1" />
            Home
          </Link>
        </BreadcrumbItem>
        <BreadcrumbItem active data-testid="breadcrumb-gallery">
          Photo Gallery
        </BreadcrumbItem>
      </Breadcrumb>

      {/* Enhanced Header with Statistics */}
      <div className="gallery-header mb-4">
        <Row className="align-items-center">
          <Col lg={8}>
            <div className="d-flex align-items-center mb-3">
              <h1 className="gallery-title mb-0 me-3">
                <FontAwesomeIcon icon={faImage} className="me-2 text-primary" />
                <Translate contentKey="gallerySystemApp.album.gallery.title">Photo Gallery</Translate>
              </h1>
              {loading && <Spinner size="sm" color="primary" data-testid="loading-spinner" />}
            </div>

            {/* Statistics Cards */}
            <Row className="mb-3">
              <Col xs={6} sm={3}>
                <Card className="stats-card border-0 bg-light">
                  <CardBody className="p-2 text-center">
                    <h6 className="text-primary mb-0" data-testid="stats-total-albums">
                      {albumStats.totalAlbums}
                    </h6>
                    <small className="text-muted">Albums</small>
                  </CardBody>
                </Card>
              </Col>
              <Col xs={6} sm={3}>
                <Card className="stats-card border-0 bg-light">
                  <CardBody className="p-2 text-center">
                    <h6 className="text-success mb-0" data-testid="stats-total-photos">
                      {albumStats.totalPhotos}
                    </h6>
                    <small className="text-muted">Photos</small>
                  </CardBody>
                </Card>
              </Col>
              <Col xs={6} sm={3}>
                <Card className="stats-card border-0 bg-light">
                  <CardBody className="p-2 text-center">
                    <h6 className="text-warning mb-0" data-testid="stats-recent-albums">
                      {albumStats.recentAlbums}
                    </h6>
                    <small className="text-muted">Recent</small>
                  </CardBody>
                </Card>
              </Col>
              <Col xs={6} sm={3}>
                <Card className="stats-card border-0 bg-light">
                  <CardBody className="p-2 text-center">
                    <h6 className="text-info mb-0" data-testid="stats-contributors">
                      {albumStats.contributors}
                    </h6>
                    <small className="text-muted">Contributors</small>
                  </CardBody>
                </Card>
              </Col>
            </Row>
          </Col>

          <Col lg={4} className="text-lg-end">
            <div className="action-buttons">
              <Button tag={Link} to="/album/list" color="outline-info" size="sm" className="me-2" data-testid="back-to-list-btn">
                <FontAwesomeIcon icon={faList} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.backToList">List View</Translate>
              </Button>
              <Button color="primary" tag={Link} to="/album/new" className="me-2" data-testid="create-album-btn">
                <FontAwesomeIcon icon={faPlus} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.createAlbum">Create Album</Translate>
              </Button>
              <Button color="outline-secondary" onClick={handleRefresh} disabled={loading} data-testid="refresh-btn">
                <FontAwesomeIcon icon={faSync} spin={loading} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.refresh">Refresh</Translate>
              </Button>
            </div>
          </Col>
        </Row>
      </div>

      {/* Enhanced Search and Filter Section */}
      <Card className="search-filter-card mb-4">
        <CardBody>
          <Row>
            <Col lg={8}>
              <Form onSubmit={handleSearch}>
                <InputGroup size="lg">
                  <Input
                    type="text"
                    placeholder="Search albums by name, keywords, or description..."
                    value={searchKeyword}
                    onChange={e => setSearchKeyword(e.target.value)}
                    onFocus={() => setIsSearchFocused(true)}
                    onBlur={() => setIsSearchFocused(false)}
                    className={isSearchFocused ? 'search-focused' : ''}
                    data-testid="search-input"
                  />
                  <Button type="submit" color="primary" data-testid="search-btn">
                    <FontAwesomeIcon icon={faSearch} className="me-1" />
                    <Translate contentKey="gallerySystemApp.album.gallery.search">Search</Translate>
                  </Button>
                </InputGroup>
              </Form>
            </Col>
            <Col lg={4} className="text-lg-end">
              <Button
                color="outline-secondary"
                onClick={() => setShowFilters(!showFilters)}
                className="me-2"
                data-testid="filters-toggle-btn"
              >
                <FontAwesomeIcon icon={faFilter} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.filters">Filters</Translate>
                {activeFiltersCount > 0 && (
                  <Badge color="primary" className="ms-2" data-testid="active-filters-count">
                    {activeFiltersCount}
                  </Badge>
                )}
                <FontAwesomeIcon icon={showFilters ? faChevronUp : faChevronDown} className="ms-1" />
              </Button>
              {(activeFiltersCount > 0 || searchKeyword) && (
                <Button color="outline-danger" onClick={clearAllFilters} size="sm" data-testid="clear-filters-btn">
                  <FontAwesomeIcon icon={faTimes} className="me-1" />
                  <Translate contentKey="gallerySystemApp.album.gallery.clearFilters">Clear All</Translate>
                </Button>
              )}
            </Col>
          </Row>

          <Collapse isOpen={showFilters}>
            <div className="filter-panel mt-4 p-3 border rounded bg-light">
              <Row>
                <Col lg={3} md={6}>
                  <FormGroup>
                    <Label for="eventFilter" className="fw-bold">
                      <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                      <Translate contentKey="gallerySystemApp.album.gallery.filterByEvent">Event</Translate>
                    </Label>
                    <Input
                      type="text"
                      id="eventFilter"
                      placeholder="Filter by event name..."
                      value={filters.event || ''}
                      onChange={e => handleFilterChange('event', e.target.value)}
                      data-testid="event-filter-input"
                    />
                  </FormGroup>
                </Col>
                <Col lg={3} md={6}>
                  <FormGroup>
                    <Label for="yearFilter" className="fw-bold">
                      <FontAwesomeIcon icon={faCalendarAlt} className="me-1" />
                      <Translate contentKey="gallerySystemApp.album.gallery.filterByYear">Year</Translate>
                    </Label>
                    <Input
                      type="number"
                      id="yearFilter"
                      placeholder="Filter by year..."
                      value={filters.year || ''}
                      onChange={e => handleFilterChange('year', e.target.value ? parseInt(e.target.value, 10) : undefined)}
                      data-testid="year-filter-input"
                    />
                  </FormGroup>
                </Col>
                <Col lg={3} md={6}>
                  <FormGroup>
                    <Label for="tagFilter" className="fw-bold">
                      <FontAwesomeIcon icon={faTag} className="me-1" />
                      <Translate contentKey="gallerySystemApp.album.gallery.filterByTag">Tag</Translate>
                    </Label>
                    <Input
                      type="text"
                      id="tagFilter"
                      placeholder="Filter by tag name..."
                      value={filters.tagName || ''}
                      onChange={e => handleFilterChange('tagName', e.target.value)}
                      data-testid="tag-filter-input"
                    />
                  </FormGroup>
                </Col>
                <Col lg={3} md={6}>
                  <FormGroup>
                    <Label for="contributorFilter" className="fw-bold">
                      <FontAwesomeIcon icon={faUser} className="me-1" />
                      <Translate contentKey="gallerySystemApp.album.gallery.filterByContributor">Contributor</Translate>
                    </Label>
                    <Input
                      type="text"
                      id="contributorFilter"
                      placeholder="Filter by contributor..."
                      value={filters.contributorLogin || ''}
                      onChange={e => handleFilterChange('contributorLogin', e.target.value)}
                      data-testid="contributor-filter-input"
                    />
                  </FormGroup>
                </Col>
              </Row>
            </div>
          </Collapse>
        </CardBody>
      </Card>

      {/* Enhanced Controls */}
      <div className="gallery-controls mb-4">
        <Row className="align-items-center">
          <Col md={6}>
            <div className="d-flex align-items-center flex-wrap gap-3">
              {/* Sort Controls */}
              <div className="sort-controls">
                <span className="me-2 fw-bold">
                  <Translate contentKey="gallerySystemApp.album.gallery.sortBy">Sort by:</Translate>
                </span>
                <ButtonGroup>
                  <Button
                    color={sortType === 'EVENT' ? 'primary' : 'outline-primary'}
                    onClick={() => handleSortChange('EVENT')}
                    size="sm"
                    data-testid="sort-by-event-btn"
                  >
                    <FontAwesomeIcon icon={faSortAlphaDown} className="me-1" />
                    <Translate contentKey="gallerySystemApp.album.gallery.sortByEvent">Event</Translate>
                  </Button>
                  <Button
                    color={sortType === 'DATE' ? 'primary' : 'outline-primary'}
                    onClick={() => handleSortChange('DATE')}
                    size="sm"
                    data-testid="sort-by-date-btn"
                  >
                    <FontAwesomeIcon icon={faSortNumericDown} className="me-1" />
                    <Translate contentKey="gallerySystemApp.album.gallery.sortByDate">Date</Translate>
                  </Button>
                </ButtonGroup>
              </div>

              {/* View Mode Controls */}
              <div className="view-controls">
                <span className="me-2 fw-bold">
                  <Translate contentKey="gallerySystemApp.album.gallery.view">View:</Translate>
                </span>
                <ButtonGroup>
                  <Button
                    color={viewMode === 'grid' ? 'secondary' : 'outline-secondary'}
                    onClick={() => handleViewModeChange('grid')}
                    size="sm"
                    data-testid="view-grid-btn"
                  >
                    <FontAwesomeIcon icon={faThLarge} />
                  </Button>
                  <Button
                    color={viewMode === 'list' ? 'secondary' : 'outline-secondary'}
                    onClick={() => handleViewModeChange('list')}
                    size="sm"
                    data-testid="view-list-btn"
                  >
                    <FontAwesomeIcon icon={faList} />
                  </Button>
                </ButtonGroup>
              </div>
            </div>
          </Col>

          <Col md={6} className="text-md-end">
            {/* Selection Controls */}
            {selectedAlbums.size > 0 && (
              <div className="selection-controls">
                <Badge color="info" className="me-2" data-testid="selected-count-badge">
                  {selectedAlbums.size} selected
                </Badge>
                <Button color="outline-secondary" size="sm" onClick={clearSelection} className="me-2">
                  Clear
                </Button>
                <Button color="outline-primary" size="sm" onClick={selectAllAlbums} className="me-2">
                  Select All
                </Button>
                <Button color="outline-danger" size="sm" onClick={() => setShowDeleteModal(true)} data-testid="delete-selected-btn">
                  <FontAwesomeIcon icon={faTrash} />
                </Button>
              </div>
            )}

            {/* Results Info */}
            <small className="text-muted">
              Showing {albumList?.length || 0} of {totalItems} albums
            </small>
          </Col>
        </Row>
      </div>

      {/* Loading State */}
      {loading && (
        <div className="text-center py-5">
          <Spinner color="primary" size="lg" />
          <div className="mt-3">
            <Progress animated value={75} className="mb-2" style={{ height: '4px' }} />
            <p className="text-muted">
              <Translate contentKey="gallerySystemApp.album.gallery.loading">Loading albums...</Translate>
            </p>
          </div>
        </div>
      )}

      {/* Gallery Content */}
      {!loading && (
        <>
          {albumList && albumList.length > 0 ? (
            <div className="gallery-content" data-testid="gallery-content">
              {groupKeys.map(groupKey => (
                <div key={groupKey} className="album-group mb-5">
                  <div className="group-header mb-4 d-flex justify-content-between align-items-center">
                    <div>
                      <h3 className="group-title mb-1">
                        {groupKey}
                        <Badge color="secondary" className="ms-2" data-testid={`group-count-${groupKey}`}>
                          {groupedAlbums[groupKey].length} albums
                        </Badge>
                      </h3>
                      {sortType === 'EVENT' && groupKey === 'Miscellaneous' && (
                        <p className="text-muted small mb-0">
                          <Translate contentKey="gallerySystemApp.album.gallery.miscellaneousDesc">
                            Albums without a specific event
                          </Translate>
                        </p>
                      )}
                    </div>
                  </div>
                  <Row className={viewMode === 'masonry' ? 'masonry-grid' : ''}>
                    {groupedAlbums[groupKey].map((album, index) => renderAlbumCard(album, index))}
                  </Row>
                </div>
              ))}
            </div>
          ) : (
            <Alert color="info" className="text-center py-5" data-testid="no-albums-alert">
              <FontAwesomeIcon icon={faImage} size="3x" className="mb-3 text-muted" />
              <h4>
                <Translate contentKey="gallerySystemApp.album.gallery.noAlbums">No albums found</Translate>
              </h4>
              <p>
                <Translate contentKey="gallerySystemApp.album.gallery.createFirst">Create your first album to get started!</Translate>
              </p>
              <Button color="primary" tag={Link} to="/album/new" className="mt-2" data-testid="create-first-album-btn">
                <FontAwesomeIcon icon={faPlus} className="me-1" />
                <Translate contentKey="gallerySystemApp.album.gallery.createAlbum">Create Album</Translate>
              </Button>
            </Alert>
          )}
        </>
      )}

      {/* Delete Confirmation Modal */}
      <Modal isOpen={showDeleteModal} toggle={() => setShowDeleteModal(false)} data-testid="delete-modal">
        <ModalHeader toggle={() => setShowDeleteModal(false)}>
          <Translate contentKey="gallerySystemApp.album.gallery.deleteSelected">Delete Selected Albums</Translate>
        </ModalHeader>
        <ModalBody>
          <p>Are you sure you want to delete {selectedAlbums.size} selected album(s)? This action cannot be undone.</p>
        </ModalBody>
        <ModalFooter>
          <Button color="secondary" onClick={() => setShowDeleteModal(false)}>
            Cancel
          </Button>
          <Button
            color="danger"
            onClick={() => {
              // TODO: Implement bulk delete functionality
              setShowDeleteModal(false);
              clearSelection();
            }}
          >
            Delete
          </Button>
        </ModalFooter>
      </Modal>
    </Container>
  );
};

export default AlbumGallery;
