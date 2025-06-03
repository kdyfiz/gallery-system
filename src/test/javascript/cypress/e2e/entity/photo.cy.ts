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

describe('Photo e2e test', () => {
  const photoPageUrl = '/photo';
  const photoPageUrlPattern = new RegExp('/photo(\\?.*)?$');
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';
  const photoSample = {
    image: 'Li4vZmFrZS1kYXRhL2Jsb2IvaGlwc3Rlci5wbmc=',
    imageContentType: 'unknown',
    uploadDate: '2025-05-28T00:35:44.351Z',
  };

  let photo;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    cy.intercept('GET', '/api/photos+(?*|)').as('entitiesRequest');
    cy.intercept('POST', '/api/photos').as('postEntityRequest');
    cy.intercept('DELETE', '/api/photos/*').as('deleteEntityRequest');
  });

  afterEach(() => {
    if (photo) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/photos/${photo.id}`,
      }).then(() => {
        photo = undefined;
      });
    }
  });

  it('Photos menu should load Photos page', () => {
    cy.visit('/');
    cy.clickOnEntityMenuItem('photo');
    cy.wait('@entitiesRequest').then(({ response }) => {
      if (response?.body.length === 0) {
        cy.get(entityTableSelector).should('not.exist');
      } else {
        cy.get(entityTableSelector).should('exist');
      }
    });
    cy.getEntityHeading('Photo').should('exist');
    cy.url().should('match', photoPageUrlPattern);
  });

  describe('Photo page', () => {
    describe('create button click', () => {
      beforeEach(() => {
        cy.visit(photoPageUrl);
        cy.wait('@entitiesRequest');
      });

      it('should load create Photo page', () => {
        cy.get(entityCreateButtonSelector).click();
        cy.url().should('match', new RegExp('/photo/new$'));
        cy.getEntityCreateUpdateHeading('Photo');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', photoPageUrlPattern);
      });
    });

    describe('with existing value', () => {
      beforeEach(() => {
        cy.authenticatedRequest({
          method: 'POST',
          url: '/api/photos',
          body: photoSample,
        }).then(({ body }) => {
          photo = body;

          cy.intercept(
            {
              method: 'GET',
              url: '/api/photos+(?*|)',
              times: 1,
            },
            {
              statusCode: 200,
              headers: {
                link: '<http://localhost/api/photos?page=0&size=20>; rel="last",<http://localhost/api/photos?page=0&size=20>; rel="first"',
              },
              body: [photo],
            },
          ).as('entitiesRequestInternal');
        });

        cy.visit(photoPageUrl);

        cy.wait('@entitiesRequestInternal');
      });

      it('detail button click should load details Photo page', () => {
        cy.get(entityDetailsButtonSelector).first().click();
        cy.getEntityDetailsHeading('photo');
        cy.get(entityDetailsBackButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', photoPageUrlPattern);
      });

      it('edit button click should load edit Photo page and go back', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Photo');
        cy.get(entityCreateSaveButtonSelector).should('exist');
        cy.get(entityCreateCancelButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', photoPageUrlPattern);
      });

      it('edit button click should load edit Photo page and save', () => {
        cy.get(entityEditButtonSelector).first().click();
        cy.getEntityCreateUpdateHeading('Photo');
        cy.get(entityCreateSaveButtonSelector).click();
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', photoPageUrlPattern);
      });

      it('last delete button click should delete instance of Photo', () => {
        cy.intercept('GET', '/api/photos/*').as('dialogDeleteRequest');
        cy.get(entityDeleteButtonSelector).last().click();
        cy.wait('@dialogDeleteRequest');
        cy.getEntityDeleteDialogHeading('photo').should('exist');
        cy.get(entityConfirmDeleteButtonSelector).click();
        cy.wait('@deleteEntityRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(204);
        });
        cy.wait('@entitiesRequest').then(({ response }) => {
          expect(response?.statusCode).to.equal(200);
        });
        cy.url().should('match', photoPageUrlPattern);

        photo = undefined;
      });
    });
  });

  describe('new Photo page', () => {
    beforeEach(() => {
      cy.visit(`${photoPageUrl}`);
      cy.get(entityCreateButtonSelector).click();
      cy.getEntityCreateUpdateHeading('Photo');
    });

    it('should create an instance of Photo', () => {
      cy.get(`[data-cy="title"]`).type('pfft designation');
      cy.get(`[data-cy="title"]`).should('have.value', 'pfft designation');

      cy.get(`[data-cy="description"]`).type('bidet shiny beneficial');
      cy.get(`[data-cy="description"]`).should('have.value', 'bidet shiny beneficial');

      cy.setFieldImageAsBytesOfEntity('image', 'integration-test.png', 'image/png');

      cy.get(`[data-cy="uploadDate"]`).type('2025-05-27T15:23');
      cy.get(`[data-cy="uploadDate"]`).blur();
      cy.get(`[data-cy="uploadDate"]`).should('have.value', '2025-05-27T15:23');

      cy.get(`[data-cy="captureDate"]`).type('2025-05-27T16:29');
      cy.get(`[data-cy="captureDate"]`).blur();
      cy.get(`[data-cy="captureDate"]`).should('have.value', '2025-05-27T16:29');

      cy.get(`[data-cy="location"]`).type('abnormally assist amidst');
      cy.get(`[data-cy="location"]`).should('have.value', 'abnormally assist amidst');

      cy.get(`[data-cy="keywords"]`).type('fooey fearless');
      cy.get(`[data-cy="keywords"]`).should('have.value', 'fooey fearless');

      // since cypress clicks submit too fast before the blob fields are validated
      cy.wait(200); // eslint-disable-line cypress/no-unnecessary-waiting
      cy.get(entityCreateSaveButtonSelector).click();

      cy.wait('@postEntityRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        photo = response.body;
      });
      cy.wait('@entitiesRequest').then(({ response }) => {
        expect(response?.statusCode).to.equal(200);
      });
      cy.url().should('match', photoPageUrlPattern);
    });
  });
});
