**User Story : Add Comments/Feedback to Albums and Photos**

**Title**: Add Comments and Feedback to Albums and Photos

**As a** User

**I want** to be able to leave comments and feedback on albums and individual photos

**So that** I can share my thoughts, ask questions, and engage with the content and other users.

**Business Logic**:

- Comments are associated with either an album or a specific photo.
- Users must be logged in to leave comments.
- Comments are displayed in chronological order (newest first).
- Administrators have the ability to moderate comments (e.g., delete inappropriate content).
- Potentially implement a character limit for comments.

**Acceptance Criteria**:

1.  A logged-in user can add a comment to an album.
2.  A logged-in user can add a comment to a photo.
3.  Comments are displayed below the album or photo, along with the author's name and timestamp.
4.  Comments are displayed in chronological order (newest first).
5.  Administrators can delete comments.
6.  Error handling for cases where the user is not logged in or the comment is invalid.

**Functional Requirements**:

- Create a database table to store comments (commentId, content, authorName, timestamp, albumId/photoId).
- Implement functionality to add a comment to an album or photo.
- Implement functionality to retrieve and display comments for an album or photo.
- Implement administrator functionality to delete comments.
- Implement user authentication to ensure only logged-in users can comment.

**Non-Functional Requirements**:

- Adding and displaying comments should be performant (load quickly).
- The commenting system should be secure and prevent XSS attacks.
- The commenting system should be scalable to handle a large number of comments.

**UI Design**:

- A "Comment" section is displayed below each album and photo.
- A text input field is provided for users to enter their comments.
- A "Submit" button is provided to submit the comment.
- Comments are displayed in a clear and readable format, with the author's name and timestamp.
- A visual indicator (e.g., a delete icon) is provided for administrators to delete comments.
- Display a clear message if the user is not logged in and needs to log in to comment.
