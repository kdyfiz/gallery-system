**Title**: View and Filter Gallery Albums

**As a** User

**I want** to be able to view gallery albums organized by either event name or date, and search or filter them by tags, event, year, or contributor

**So that** I can easily find and browse photos relevant to a specific event, time period, or theme.

**Business Logic**:

- Albums are sortable by event name (alphabetically) and date (chronologically).
- Albums without an event name are grouped under a "Miscellaneous" or "Uncategorized" category.
- Date sorting prioritizes album creation date, with admin override capability.
- Users can search albums by name or keyword.
- Users can filter albums using tags, event name, year, or contributor.

**Acceptance Criteria**:

1. The user can select to view albums organized by "Event" or "Date" via a clear interface element.
2. When "Event" is selected, albums are displayed alphabetically by event name, with "Miscellaneous/Uncategorized" albums grouped and displayed alphabetically within that group.
3. When "Date" is selected, albums are displayed chronologically, most recent first.
4. Each album displays a representative thumbnail and the album's name (and event name, if applicable).
5. A search bar is available to search by album name or keyword.
6. A filter panel allows filtering by tags, event name, year, and contributor.
7. Filtered results update the album grid in real-time and respect the selected "Event" or "Date" view mode.

**Functional Requirements**:

- Retrieve album data (name, event, date, tags, contributor, thumbnail) from the database.
- Provide sorting functionality based on user selection (Event or Date).
- Provide a search function that queries albums by name or keyword.
- Support filtering of albums based on tags, event, year, or contributor.
- Handle cases where an album has no associated event name.
- Display a default thumbnail image if an album has no photos yet.

**Non-Functional Requirements**:

- Album listing loads quickly (within 2 seconds).
- System is responsive and works well on different screen sizes (desktop, tablet, mobile).
- System is accessible to users with disabilities, adhering to WCAG guidelines.

**UI Design**:

- A clear control (e.g., radio buttons or a dropdown menu) allows switching between "Event" and "Date" views.
- Albums are displayed in a grid or list format, with each album tile containing a thumbnail image and the album's name.
- When viewing by "Event," the event name is clearly displayed above the albums belonging to that event. The "Miscellaneous/Uncategorized" section is clearly labeled.
- A search bar is placed at the top of the album list.
- A filter panel (e.g., side panel or collapsible dropdown) offers checkboxes or dropdowns for filtering by tags, event name, year, and contributor.
- The thumbnail image is visually appealing and representative of the album's content.
