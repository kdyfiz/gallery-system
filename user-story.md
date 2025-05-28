**Title**: View and Filter Gallery Albums

**As a** User

**I want** to be able to view gallery albums organized by either event name or date, and search or filter them by tags, event, year, or contributor

**So that** I can easily find and browse photos relevant to a specific event, time period, or theme.

**Business Logic**:

- Albums are sortable by event name (alphabetically) and date (chronologically).
- Albums without an event name are grouped under a "Miscellaneous" or "Uncategorized" category.
- Date sorting prioritizes album creation date, with admin override capability.
- Users can search albums by name or keyword.
- Users can filter albums by tags, event name, year, or contributor.

**Acceptance Criteria**:

1.  The user can select to view albums organized by "Event" or "Date" via a clear and intuitive interface element (e.g., radio buttons, dropdown).
2.  When "Event" is selected, albums are displayed alphabetically by event name. Albums with no event name are grouped under "Miscellaneous/Uncategorized" and displayed alphabetically within that group.
3.  When "Date" is selected, albums are displayed chronologically, with the most recent albums appearing first.
4.  Each album displayed shows a representative thumbnail image and the album's name (and event name, if applicable).
5.  A search bar is available for users to search by album name or keyword.
6.  A filter panel allows users to filter albums by:
    - Tags
    - Event name
    - Year
    - Contributor
7.  Filtered results update the album grid in real-time and respect the selected "Event" or "Date" view mode.

**Functional Requirements**:

- The system must retrieve album data (name, event, date, tags, contributor, thumbnail) from the database.
- The system must provide sorting functionality based on user selection (Event or Date).
- The system must provide a search function that queries albums by name or keyword.
- The system must support filtering of albums based on tags, event, year, or contributor.
- The system must handle cases where an album has no associated event name.
- The system must display a default thumbnail image if an album has no photos yet.

**Non-Functional Requirements**:

- The album listing should load quickly (within 2 seconds).
- The system should be responsive and work well on different screen sizes (desktop, tablet, mobile).
- The system should be accessible to users with disabilities, adhering to WCAG guidelines.

**UI Design**:

- A clear and prominent control (e.g., radio buttons or a dropdown menu) allows the user to switch between "Event" and "Date" views.
- Albums are displayed in a grid or list format, with each album tile containing a thumbnail image and the album's name.
- When viewing by "Event," the event name is clearly displayed above the albums belonging to that event. The "Miscellaneous/Uncategorized" section is clearly labeled.
- A search bar is placed at the top of the album list for quick searches.
- A filter panel (e.g., side panel or collapsible dropdown) offers checkboxes or dropdowns for filtering by tags, event name, year, and contributor.
- The thumbnail image is visually appealing and representative of the album's content.
