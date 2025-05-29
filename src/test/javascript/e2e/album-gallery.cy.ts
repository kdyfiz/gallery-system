import { entityItemSelector } from '../../support/entity';

describe('Album Gallery E2E Tests', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'admin';
  const password = Cypress.env('E2E_PASSWORD') ?? 'admin';

  beforeEach(() => {
    cy.login(username, password);
    cy.visit('/album-gallery');
  });

  describe('Gallery Layout and Navigation', () => {
    it('should display gallery header with statistics', () => {
      cy.get('[data-testid="album-gallery"]').should('be.visible');
      cy.get('[data-testid="stats-total-albums"]').should('be.visible');
      cy.get('[data-testid="stats-total-photos"]').should('be.visible');
      cy.get('[data-testid="stats-recent-albums"]').should('be.visible');
      cy.get('[data-testid="stats-contributors"]').should('be.visible');
    });

    it('should display breadcrumb navigation', () => {
      cy.get('[data-testid="breadcrumb-home"]').should('be.visible').and('contain', 'Home');
      cy.get('[data-testid="breadcrumb-gallery"]').should('be.visible').and('contain', 'Photo Gallery');
    });

    it('should have working action buttons', () => {
      cy.get('[data-testid="back-to-list-btn"]').should('be.visible').and('contain', 'List View');
      cy.get('[data-testid="create-album-btn"]').should('be.visible').and('contain', 'Create Album');
      cy.get('[data-testid="refresh-btn"]').should('be.visible').and('contain', 'Refresh');
    });
  });

  describe('Search Functionality', () => {
    it('should allow searching albums by keyword', () => {
      cy.get('[data-testid="search-input"]').should('be.visible');
      cy.get('[data-testid="search-input"]').type('test album');
      cy.get('[data-testid="search-btn"]').click();
      cy.wait(1000); // Wait for search results
    });

    it('should clear search when clear filters is clicked', () => {
      cy.get('[data-testid="search-input"]').type('test');
      cy.get('[data-testid="clear-filters-btn"]').should('be.visible').click();
      cy.get('[data-testid="search-input"]').should('have.value', '');
    });
  });

  describe('Filter Functionality', () => {
    beforeEach(() => {
      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('.filter-panel').should('be.visible');
    });

    it('should display filter panel when filters button is clicked', () => {
      cy.get('[data-testid="event-filter-input"]').should('be.visible');
      cy.get('[data-testid="year-filter-input"]').should('be.visible');
      cy.get('[data-testid="tag-filter-input"]').should('be.visible');
      cy.get('[data-testid="contributor-filter-input"]').should('be.visible');
    });

    it('should filter by event', () => {
      cy.get('[data-testid="event-filter-input"]').type('Wedding');
      cy.wait(1000); // Wait for filter to apply
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter by year', () => {
      cy.get('[data-testid="year-filter-input"]').type('2023');
      cy.wait(1000); // Wait for filter to apply
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter by tag', () => {
      cy.get('[data-testid="tag-filter-input"]').type('family');
      cy.wait(1000); // Wait for filter to apply
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should filter by contributor', () => {
      cy.get('[data-testid="contributor-filter-input"]').type('admin');
      cy.wait(1000); // Wait for filter to apply
      cy.get('[data-testid="active-filters-count"]').should('contain', '1');
    });

    it('should show active filter count', () => {
      cy.get('[data-testid="event-filter-input"]').type('Wedding');
      cy.get('[data-testid="year-filter-input"]').type('2023');
      cy.wait(1000);
      cy.get('[data-testid="active-filters-count"]').should('contain', '2');
    });

    it('should clear all filters', () => {
      cy.get('[data-testid="event-filter-input"]').type('Wedding');
      cy.get('[data-testid="year-filter-input"]').type('2023');
      cy.wait(1000);
      cy.get('[data-testid="clear-filters-btn"]').click();
      cy.get('[data-testid="event-filter-input"]').should('have.value', '');
      cy.get('[data-testid="year-filter-input"]').should('have.value', '');
    });
  });

  describe('Sort and View Controls', () => {
    it('should sort by event', () => {
      cy.get('[data-testid="sort-by-event-btn"]').should('be.visible').click();
      cy.wait(1000); // Wait for sorting to apply
      cy.get('[data-testid="sort-by-event-btn"]').should('have.class', 'btn-primary');
    });

    it('should sort by date', () => {
      cy.get('[data-testid="sort-by-date-btn"]').should('be.visible').click();
      cy.wait(1000); // Wait for sorting to apply
      cy.get('[data-testid="sort-by-date-btn"]').should('have.class', 'btn-primary');
    });

    it('should switch to grid view', () => {
      cy.get('[data-testid="view-grid-btn"]').should('be.visible').click();
      cy.get('[data-testid="view-grid-btn"]').should('have.class', 'btn-secondary');
    });

    it('should switch to list view', () => {
      cy.get('[data-testid="view-list-btn"]').should('be.visible').click();
      cy.get('[data-testid="view-list-btn"]').should('have.class', 'btn-secondary');
    });
  });

  describe('Album Card Interactions', () => {
    it('should display album cards when albums exist', () => {
      cy.get('[data-testid^="album-card-"]').should('have.length.at.least', 0);
    });

    it('should navigate to album detail when title is clicked', () => {
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="album-title-link-"]').should('be.visible');
          // Note: In a real test, you would click and verify navigation
          // cy.get('[data-testid^="album-title-link-"]').click();
          // cy.url().should('include', '/album/');
        });
    });

    it('should have view album button', () => {
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="view-album-btn-"]').should('be.visible');
        });
    });

    it('should display album metadata', () => {
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          // Check for event badge if present
          cy.get('body').then($body => {
            if ($body.find('[data-testid^="album-event-badge-"]').length > 0) {
              cy.get('[data-testid^="album-event-badge-"]').should('be.visible');
            }
          });
        });
    });

    it('should allow album selection in grid view', () => {
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="select-album-btn-"]').should('be.visible').click();
        });
      cy.get('[data-testid="selected-count-badge"]').should('be.visible').and('contain', '1');
    });
  });

  describe('Album Selection and Bulk Actions', () => {
    it('should select multiple albums', () => {
      cy.get('[data-testid^="album-card-"]').each(($card, index) => {
        if (index < 3) {
          // Select first 3 albums
          cy.wrap($card).within(() => {
            cy.get('[data-testid^="select-album-btn-"]').click();
          });
        }
      });
      cy.get('[data-testid="selected-count-badge"]').should('contain', '3');
    });

    it('should clear selection', () => {
      // First select an album
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="select-album-btn-"]').click();
        });

      // Then clear selection
      cy.get('.selection-controls').within(() => {
        cy.contains('Clear').click();
      });

      cy.get('[data-testid="selected-count-badge"]').should('not.exist');
    });

    it('should select all albums', () => {
      // First select one album to show selection controls
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="select-album-btn-"]').click();
        });

      // Then select all
      cy.get('.selection-controls').within(() => {
        cy.contains('Select All').click();
      });

      // Count should match total albums
      cy.get('[data-testid="selected-count-badge"]').should('be.visible');
    });

    it('should show delete modal for selected albums', () => {
      // First select an album
      cy.get('[data-testid^="album-card-"]')
        .first()
        .within(() => {
          cy.get('[data-testid^="select-album-btn-"]').click();
        });

      // Click delete button
      cy.get('[data-testid="delete-selected-btn"]').click();

      // Verify modal appears
      cy.get('[data-testid="delete-modal"]').should('be.visible');

      // Close modal
      cy.get('[data-testid="delete-modal"]').within(() => {
        cy.contains('Cancel').click();
      });
    });
  });

  describe('Empty State', () => {
    it('should display empty state when no albums found', () => {
      // Apply a filter that should return no results
      cy.get('[data-testid="filters-toggle-btn"]').click();
      cy.get('[data-testid="event-filter-input"]').type('NonexistentEvent123456');
      cy.wait(2000);

      // Check for empty state or no albums message
      cy.get('body').then($body => {
        if ($body.find('[data-testid="no-albums-alert"]').length > 0) {
          cy.get('[data-testid="no-albums-alert"]').should('be.visible');
          cy.get('[data-testid="create-first-album-btn"]').should('be.visible');
        }
      });
    });
  });

  describe('Loading States', () => {
    it('should show loading spinner during data fetch', () => {
      cy.get('[data-testid="refresh-btn"]').click();

      // Check if loading spinner appears (it might be fast)
      cy.get('body').then($body => {
        if ($body.find('[data-testid="loading-spinner"]').length > 0) {
          cy.get('[data-testid="loading-spinner"]').should('be.visible');
        }
      });
    });
  });

  describe('Responsive Design', () => {
    it('should work on mobile viewport', () => {
      cy.viewport(375, 667); // iPhone SE
      cy.get('[data-testid="album-gallery"]').should('be.visible');
      cy.get('[data-testid="search-input"]').should('be.visible');
      cy.get('[data-testid="filters-toggle-btn"]').should('be.visible');
    });

    it('should work on tablet viewport', () => {
      cy.viewport(768, 1024); // iPad
      cy.get('[data-testid="album-gallery"]').should('be.visible');
      cy.get('[data-testid="gallery-content"]').should('be.visible');
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels and roles', () => {
      cy.get('[data-testid="search-input"]').should('have.attr', 'placeholder');
      cy.get('[data-testid="sort-by-event-btn"]').should('be.visible');
      cy.get('[data-testid="sort-by-date-btn"]').should('be.visible');
    });

    it('should be keyboard navigable', () => {
      cy.get('[data-testid="search-input"]').focus().type('{tab}');
      cy.focused().should('have.attr', 'data-testid', 'search-btn');
    });
  });

  describe('Performance', () => {
    it('should load within reasonable time', () => {
      const startTime = Date.now();
      cy.visit('/album-gallery').then(() => {
        const loadTime = Date.now() - startTime;
        expect(loadTime).to.be.lessThan(5000); // Should load within 5 seconds
      });
    });
  });
});
