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
  const albumPageUrl = '/album/list';
  const albumGalleryUrl = '/album';
  const albumPageUrlPattern = new RegExp('/album/(list)?(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const albumSample = {
    name: 'Test Album for E2E',
    creationDate: '2025-01-23T10:00:00.000Z',
    event: 'Test Event',
  };

  let album;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/albums+(?*|)').as('entitiesRequest');
    cy.intercept('GET', '/api/albums/gallery+(?*|)').as('galleryRequest');
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

  it('Albums menu should load Albums gallery page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('album');
    cy.wait('@galleryRequest').then(({ response }) => {
      expect(response?.statusCode).to.equal(200);
    });
    cy.url().should('match', new RegExp('/album(\\?.*)?$'));
    cy.contains('Photo Gallery').should('exist');
  });

  describe('Album list page', () => {
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
    });
  });

  describe('Album gallery view', () => {
    beforeEach(() => {
      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/albums',
        body: albumSample,
      }).then(({ body }) => {
        album = body;
      });
    });

    it('should display albums in gallery format', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.contains('Photo Gallery').should('exist');
      cy.get('[data-cy="gallery-content"]').should('exist');
    });

    it('should allow switching between event and date sorting', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');

      cy.get('[data-cy="sort-by-event"]').click();
      cy.wait('@galleryRequest');

      cy.get('[data-cy="sort-by-date"]').click();
      cy.wait('@galleryRequest');
    });

    it('should display create album button', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.get('a[href="/album/new"]').should('exist').and('contain', 'Create Album');
    });

    it('should navigate to list view', () => {
      cy.visit(albumGalleryUrl);
      cy.wait('@galleryRequest');
      cy.get('a[href="/album/list"]').should('exist').and('contain', 'Back to List');
      cy.get('a[href="/album/list"]').click();
      cy.url().should('include', '/album/list');
    });
  });

  describe('new Album page', () => {
    beforeEach(() => {
      cy.visit(albumPageUrl);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Album');
    });

    it('should create an instance of Album with all fields', () => {
      cy.get(`[data-cy="name"]`).type('Comprehensive Test Album');
      cy.get(`[data-cy="name"]`).should('have.value', 'Comprehensive Test Album');

      cy.get(`[data-cy="event"]`).type('Test Event 2025');
      cy.get(`[data-cy="event"]`).should('have.value', 'Test Event 2025');

      cy.get(`[data-cy="creationDate"]`).type('2025-01-23T10:30');
      cy.get(`[data-cy="creationDate"]`).blur();
      cy.get(`[data-cy="creationDate"]`).should('have.value', '2025-01-23T10:30');

      cy.get(`[data-cy="overrideDate"]`).type('2025-01-24T11:00');
      cy.get(`[data-cy="overrideDate"]`).blur();
      cy.get(`[data-cy="overrideDate"]`).should('have.value', '2025-01-24T11:00');

      cy.setFieldImageAsBytesOfEntity('thumbnail', 'integration-test.png', 'image/png');

      cy.wait(200);
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

    it('should validate required fields', () => {
      cy.get(entityCreateSaveButtonSelector).click();

      cy.get(`[data-cy="name"]`).should('have.class', 'is-invalid');
    });

    it('should validate name length constraints', () => {
      cy.get(`[data-cy="name"]`).type('AB');
      cy.get(`[data-cy="creationDate"]`).type('2025-01-23T10:30');
      cy.get(entityCreateSaveButtonSelector).click();

      cy.get(`[data-cy="name"]`).should('have.class', 'is-invalid');
    });
  });
});
