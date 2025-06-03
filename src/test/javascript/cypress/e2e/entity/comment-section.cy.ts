describe('Comment Section e2e test', () => {
  const username = Cypress.env('E2E_USERNAME') ?? 'user';
  const password = Cypress.env('E2E_PASSWORD') ?? 'user';

  let album;
  let photo;
  let comment;

  beforeEach(() => {
    cy.login(username, password);
  });

  beforeEach(() => {
    // Create test album
    cy.authenticatedRequest({
      method: 'POST',
      url: '/api/albums',
      body: {
        name: 'Test Album for Comments',
        event: 'Test Event',
        creationDate: new Date().toISOString(),
      },
    }).then(({ body }) => {
      album = body;

      // Create test photo in the album
      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/photos',
        body: {
          title: 'Test Photo for Comments',
          description: 'Test photo description',
          image: 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==',
          imageContentType: 'image/png',
          uploadDate: new Date().toISOString(),
          album: { id: album.id },
        },
      }).then(({ body: photoBody }) => {
        photo = photoBody;
      });
    });

    // Intercept comment-related API calls
    cy.intercept('GET', '/api/comments/album/*').as('getAlbumComments');
    cy.intercept('GET', '/api/comments/photo/*').as('getPhotoComments');
    cy.intercept('POST', '/api/comments').as('createComment');
  });

  afterEach(() => {
    // Clean up created entities
    if (comment) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/comments/${comment.id}`,
      }).catch(() => {
        // Comment might already be deleted
      });
    }
    if (photo) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/photos/${photo.id}`,
      }).catch(() => {
        // Photo might already be deleted
      });
    }
    if (album) {
      cy.authenticatedRequest({
        method: 'DELETE',
        url: `/api/albums/${album.id}`,
      }).catch(() => {
        // Album might already be deleted
      });
    }
  });

  describe('Album Comment Section', () => {
    it('should display comment section on album detail page', () => {
      cy.visit(`/album/${album.id}`);
      cy.wait('@getAlbumComments');

      // Check that comment section is visible
      cy.get('[data-cy="commentSection"]').should('exist');
      cy.get('[data-cy="commentSection"]').should('contain', 'Comments (0)');

      // Check that comment input is visible for logged-in user
      cy.get('[data-cy="commentInput"]').should('exist');
      cy.get('[data-cy="submitComment"]').should('exist');

      // Check no comments message
      cy.get('[data-cy="noComments"]').should('exist');
      cy.get('[data-cy="noComments"]').should('contain', 'No comments yet');
    });

    it('should allow adding a comment to an album', () => {
      cy.visit(`/album/${album.id}`);
      cy.wait('@getAlbumComments');

      const commentText = 'This is a test comment on the album';

      // Add a comment
      cy.get('[data-cy="commentInput"]').type(commentText);
      cy.get('[data-cy="submitComment"]').click();

      cy.wait('@createComment').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        comment = response.body;
      });

      cy.wait('@getAlbumComments');

      // Verify comment appears
      cy.get('[data-cy="commentsList"]').should('exist');
      cy.get(`[data-cy="comment-${comment.id}"]`).should('exist');
      cy.get(`[data-cy="comment-${comment.id}"]`).should('contain', commentText);
      cy.get(`[data-cy="comment-${comment.id}"]`).find('[data-cy="commentAuthor"]').should('contain', username);

      // Verify comment count updated
      cy.get('[data-cy="commentSection"]').should('contain', 'Comments (1)');
    });

    it('should display existing comments for an album', () => {
      // Create a comment first
      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/comments',
        body: {
          content: 'Existing comment on album',
          createdDate: new Date().toISOString(),
          album: { id: album.id },
        },
      }).then(({ body }) => {
        comment = body;

        cy.visit(`/album/${album.id}`);
        cy.wait('@getAlbumComments');

        // Verify existing comment displays
        cy.get('[data-cy="commentsList"]').should('exist');
        cy.get(`[data-cy="comment-${comment.id}"]`).should('exist');
        cy.get(`[data-cy="comment-${comment.id}"]`).should('contain', 'Existing comment on album');
        cy.get('[data-cy="commentSection"]').should('contain', 'Comments (1)');
      });
    });
  });

  describe('Photo Comment Section', () => {
    it('should display comment section on photo detail page', () => {
      cy.visit(`/photo/${photo.id}`);
      cy.wait('@getPhotoComments');

      // Check that comment section is visible
      cy.get('[data-cy="commentSection"]').should('exist');
      cy.get('[data-cy="commentSection"]').should('contain', 'Comments (0)');

      // Check that comment input is visible for logged-in user
      cy.get('[data-cy="commentInput"]').should('exist');
      cy.get('[data-cy="submitComment"]').should('exist');

      // Check no comments message
      cy.get('[data-cy="noComments"]').should('exist');
      cy.get('[data-cy="noComments"]').should('contain', 'No comments yet');
    });

    it('should allow adding a comment to a photo', () => {
      cy.visit(`/photo/${photo.id}`);
      cy.wait('@getPhotoComments');

      const commentText = 'This is a test comment on the photo';

      // Add a comment
      cy.get('[data-cy="commentInput"]').type(commentText);
      cy.get('[data-cy="submitComment"]').click();

      cy.wait('@createComment').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        comment = response.body;
      });

      cy.wait('@getPhotoComments');

      // Verify comment appears
      cy.get('[data-cy="commentsList"]').should('exist');
      cy.get(`[data-cy="comment-${comment.id}"]`).should('exist');
      cy.get(`[data-cy="comment-${comment.id}"]`).should('contain', commentText);
      cy.get(`[data-cy="comment-${comment.id}"]`).find('[data-cy="commentAuthor"]').should('contain', username);

      // Verify comment count updated
      cy.get('[data-cy="commentSection"]').should('contain', 'Comments (1)');
    });

    it('should display existing comments for a photo', () => {
      // Create a comment first
      cy.authenticatedRequest({
        method: 'POST',
        url: '/api/comments',
        body: {
          content: 'Existing comment on photo',
          createdDate: new Date().toISOString(),
          photo: { id: photo.id },
        },
      }).then(({ body }) => {
        comment = body;

        cy.visit(`/photo/${photo.id}`);
        cy.wait('@getPhotoComments');

        // Verify existing comment displays
        cy.get('[data-cy="commentsList"]').should('exist');
        cy.get(`[data-cy="comment-${comment.id}"]`).should('exist');
        cy.get(`[data-cy="comment-${comment.id}"]`).should('contain', 'Existing comment on photo');
        cy.get('[data-cy="commentSection"]').should('contain', 'Comments (1)');
      });
    });
  });

  describe('Comment Section UI Tests', () => {
    it('should validate comment input', () => {
      cy.visit(`/album/${album.id}`);
      cy.wait('@getAlbumComments');

      // Submit button should be disabled when input is empty
      cy.get('[data-cy="submitComment"]').should('be.disabled');

      // Submit button should be enabled when input has content
      cy.get('[data-cy="commentInput"]').type('Test comment');
      cy.get('[data-cy="submitComment"]').should('not.be.disabled');

      // Clear input, submit button should be disabled again
      cy.get('[data-cy="commentInput"]').clear();
      cy.get('[data-cy="submitComment"]').should('be.disabled');
    });

    it('should show character count', () => {
      cy.visit(`/album/${album.id}`);
      cy.wait('@getAlbumComments');

      const testText = 'Test comment';
      cy.get('[data-cy="commentInput"]').type(testText);

      // Should show character count
      cy.get('[data-cy="commentSection"]').should('contain', `${testText.length}/1000 characters`);
    });

    it('should clear input after successful comment submission', () => {
      cy.visit(`/album/${album.id}`);
      cy.wait('@getAlbumComments');

      const commentText = 'Comment that should be cleared';

      cy.get('[data-cy="commentInput"]').type(commentText);
      cy.get('[data-cy="commentInput"]').should('have.value', commentText);

      cy.get('[data-cy="submitComment"]').click();

      cy.wait('@createComment').then(({ response }) => {
        expect(response?.statusCode).to.equal(201);
        comment = response.body;
      });

      // Input should be cleared after successful submission
      cy.get('[data-cy="commentInput"]').should('have.value', '');
    });
  });
});
