import {
  entityConfirmDeleteButtonSelector,
  entityCreateButtonSelector,
  entityCreateCancelButtonSelector,
  entityCreateSaveButtonSelector,
  entityDeleteButtonSelector,
  entityDetailsBackButtonSelector,
  entityDetailsButtonSelector,
  entityEditButtonSelector,
  entityTableSelector,
} from '../../support/entity';

describe('Album e2e test', () => {
  const albumPageUrl = '/album';
  const albumGalleryUrl = '/album-gallery';
  const albumPageUrlPattern = new RegExp('/album(\\?.*)?$');
  const albumGalleryUrlPattern = new RegExp('/album-gallery(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const albumSample = { name: 'cooperative', creationDate: '2025-05-22T04:08:07.608Z' };

  let album;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/albums+(?*|)').as('entitiesRequest');
    cy.intercept('GET', '/api/albums/gallery+(?*|)').as('galleryRequest');
    cy.intercept('GET', '/api/albums/search+(?*|)').as('searchRequest');
    cy.intercept('GET', '/api/albums/filter-options+(?*|)').as('filterOptionsRequest');
    cy.intercept('POST', '/api/albums').as('postEntityRequest');
    cy.intercept('DELETE', '/api/albums/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (album) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/albums/${album.id}`,
      }).then(() => {
        album = undefined;
      });
    }
  });

  it('Albums menu should load Albums page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('album');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Album').should('exist');
    cy.url().should('match', albumPageUrlPattern);
  });

  describe('Album Gallery page', () => {
    it('should load album gallery page', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.get('[data-testid="album-gallery"]').should('exist');
      cy.url().should('match', albumGalleryUrlPattern);
    });

    it('should display gallery statistics', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.get('[data-testid="stats-total-albums"]').should('exist');
      cy.get('[data-testid="stats-total-photos"]').should('exist');
      cy.get('[data-testid="stats-recent-albums"]').should('exist');
      cy.get('[data-testid="stats-contributors"]').should('exist');
    });

    it('should allow sorting by event and date', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      // Test sort by event
      cy.get('[data-testid="sort-by-event-btn"]').should('have.class', 'btn-primary');

      // Test sort by date
      cy.get('[data-testid="sort-by-date-btn"]').click();
      cy.wait('@galleryRequest');
      cy.get('[data-testid="sort-by-date-btn"]').should('have.class', 'btn-primary');
      cy.get('[data-testid="sort-by-event-btn"]').should('have.class', 'btn-outline-primary');
    });

    it('should allow switching view modes', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      // Test grid view (default)
      cy.get('[data-testid="view-grid-btn"]').should('have.class', 'btn-secondary');

      // Test list view
      cy.get('[data-testid="view-list-btn"]').click();
      cy.get('[data-testid="view-list-btn"]').should('have.class', 'btn-secondary');
      cy.get('[data-testid="view-grid-btn"]').should('have.class', 'btn-outline-secondary');
    });

    it('should allow searching albums', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      cy.get('[data-testid="search-input"]').type('test album');
      cy.get('[data-testid="search-btn"]').click();
      cy.wait('@searchRequest');
    });

    it('should show and hide filters', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.wait('@filterOptionsRequest');

      // Filters should be hidden initially
      cy.get('[data-testid="event-filter-input"]').should('not.be.visible');

      // Show filters
      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="event-filter-input"]').should('be.visible');
      cy.get('[data-testid="year-filter-input"]').should('be.visible');
      cy.get('[data-testid="tag-filter-input"]').should('be.visible');
      cy.get('[data-testid="contributor-filter-input"]').should('be.visible');
    });

    it('should filter albums by event', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.wait('@filterOptionsRequest');

      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="event-filter-input"]').select(1); // Select first option (not "All Events")
      cy.wait('@searchRequest');
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter albums by year', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.wait('@filterOptionsRequest');

      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="year-filter-input"]').select(1); // Select first option (not "All Years")
      cy.wait('@searchRequest');
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter albums by tag', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.wait('@filterOptionsRequest');

      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="tag-filter-input"]').select(1); // Select first option (not "All Tags")
      cy.wait('@searchRequest');
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter albums by contributor', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.wait('@filterOptionsRequest');

      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="contributor-filter-input"]').select(1); // Select first option (not "All Contributors")
      cy.wait('@searchRequest');
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should clear all filters', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      // Apply some filters
      cy.get('[data-testid="search-input"]').type('test');
      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="event-filter-input"]').type('wedding');
      cy.wait('@searchRequest');

      // Clear all filters
      cy.get('[data-testid="clear-filters-btn"]').click();
      cy.wait('@galleryRequest');
      cy.get('[data-testid="search-input"]').should('have.value', '');
      cy.get('[data-testid="event-filter-input"]').should('have.value', '');
    });

    it('should refresh gallery', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      cy.get('[data-testid="refresh-btn"]').click();
      cy.wait('@galleryRequest');
    });

    it('should navigate to create album', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      cy.get('[data-testid="create-album-btn"]').click();
      cy.url().should('match', new RegExp('/album/new$'));
    });

    it('should navigate back to list view', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      cy.get('[data-testid="back-to-list-btn"]').click();
      cy.url().should('match', albumPageUrlPattern);
    });
  });

  describe('Album page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(albumPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Album page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/album/new$'));
        cy.getEntityCreateUpdateHeading('Album');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', albumPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/albums',
          body: albumSample,
        }).then(({ body }) => {
          album = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/albums+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/albums?page=0&size=20>; rel="last",<http://localhost/api/albums?page=0&size=20>; rel="first"',
              },
              body: [album],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(albumPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Album page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('album');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', albumPageUrlPattern);
      });

      it('edit button click should load edit Album page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Album');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', albumPageUrlPattern);
      });

      it('edit button click should load edit Album page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Album');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', albumPageUrlPattern);
      });

      it('last delete button click should delete instance of Album', () => {
        cy.intercept('GET', '/api/albums/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('album').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', albumPageUrlPattern);

        album = undefined;
      });

      it('should display album card with proper test IDs in gallery', () => {
        cy.visit(albumGalleryUrl);
        cy.wait('@galleryRequest');

        if (album) {
          cy.get(`[data-testid="album-card-${album.id}"]`).should('exist');
          cy.get(`[data-testid="album-title-link-${album.id}"]`).should('exist');
          cy.get(`[data-testid="view-album-btn-${album.id}"]`).should('exist');
          cy.get(`[data-testid="edit-album-btn-${album.id}"]`).should('exist');
        }
      });
    });
  });

  describe('new Album page', () => {
    beforeEach(() => {
      cy.visit(`${albumPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Album');
    });

    it('should create an instance of Album', () => {
      cy.get(`[data-cy="name"]`).type('reprimand');
      cy.get(`[data-cy="name"]`).should('have.value', 'reprimand');

      cy.get(`[data-cy="event"]`).type('badly restfully concerning');
      cy.get(`[data-cy="event"]`).should('have.value', 'badly restfully concerning');

      cy.get(`[data-cy="creationDate"]`).type('2025-05-22T02:30');
      cy.get(`[data-cy="creationDate"]`).blur();
      cy.get(`[data-cy="creationDate"]`).should('have.value', '2025-05-22T02:30');

      cy.get(`[data-cy="overrideDate"]`).type('2025-05-22T04:53');
      cy.get(`[data-cy="overrideDate"]`).blur();
      cy.get(`[data-cy="overrideDate"]`).should('have.value', '2025-05-22T04:53');

      cy.setFieldImageAsBytesOfEntity('thumbnail', 'integration-test.png', 'image/png');

      cy.get(`[data-cy="keywords"]`).type('consequently');
      cy.get(`[data-cy="keywords"]`).should('have.value', 'consequently');

      cy.get(`[data-cy="description"]`).type('../fake-data/blob/hipster.txt');
      cy.get(`[data-cy="description"]`).invoke('val').should('match', new RegExp('../fake-data/blob/hipster.txt'));

      // since cypress clicks submit too fast before the blob fields are validated
      cy.wait(200); // eslint-disable-line cypress/no-unnecessary-waiting
      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        album = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', albumPageUrlPattern);
    });
  });
});
