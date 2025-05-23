```
**Title**: View Gallery Albums by Event or Date

**As a** User

**I want** to be able to view gallery albums organized by either event name or date

**So that** I can easily find and browse photos relevant to a specific event or time period.

**Business Logic**:
- Albums should be sortable by both event name (alphabetically) and date (chronologically).
- If an album has no event name, it should be grouped under a "Miscellaneous" or "Uncategorized" category.
- Date sorting should prioritize the album creation date, but allow for manual date overrides by the admin.

**Acceptance Criteria**:
1.  The user can select to view albums organized by "Event" or "Date" via a clear and intuitive interface element (e.g., radio buttons, dropdown).
2.  When "Event" is selected, albums are displayed alphabetically by event name. Albums with no event name are grouped under "Miscellaneous/Uncategorized" and displayed alphabetically within that group.
3.  When "Date" is selected, albums are displayed chronologically, with the most recent albums appearing first.
4.  Each album displayed should show a representative thumbnail image and the album's name (and event name, if applicable).

**Functional Requirements**:
- The system must retrieve album data (name, event, date, thumbnail) from the database.
- The system must provide sorting functionality based on user selection (Event or Date).
- The system must handle cases where an album has no associated event name.
- The system must display a default thumbnail image if an album has no photos yet.

**Non-Functional Requirements**:
- The album listing should load quickly (within 2 seconds).
- The system should be responsive and work well on different screen sizes (desktop, tablet, mobile).
- The system should be accessible to users with disabilities, adhering to WCAG guidelines.

**UI Design**:
- A clear and prominent control (e.g., radio buttons or a dropdown menu) should allow the user to switch between "Event" and "Date" views.
- Albums should be displayed in a grid or list format, with each album tile containing a thumbnail image and the album's name.
- If viewing by "Event," the event name should be clearly displayed above the albums belonging to that event. The "Miscellaneous/Uncategorized" section should be clearly labeled.
- The thumbnail image should be visually appealing and representative of the album's content.
```
