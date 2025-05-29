#update 3

- done run jdl
- done run ci-cd
- done update gherkin file
- success in testing
- done run prompt to update test case in src/test
- done update performance testing in ci
- can run ./mvnw gatling:test - to open gatling file .html

# Gallery System

This is a **JHipster-based photo gallery management application** built with Spring Boot and React. The application provides a comprehensive solution for organizing, searching, and displaying photo albums with advanced filtering capabilities.

## 🌟 Key Features

### Enhanced Album Gallery Interface

- **Modern, responsive design** with mobile-first approach
- **Multiple view modes**: Grid, List, and Masonry layouts
- **Advanced search functionality** with full-text search across names, keywords, and descriptions
- **Multi-criteria filtering** by event, year, tag, and contributor
- **Real-time statistics dashboard** showing total albums, photos, recent albums, and contributors
- **Album selection and bulk operations** with visual feedback
- **Smooth animations and transitions** for enhanced user experience

### Comprehensive Search & Filter System

- **Keyword Search**: Search across album names, keywords, and descriptions
- **Event Filter**: Filter albums by event name
- **Year Filter**: Filter albums by creation year
- **Tag Filter**: Filter albums by associated tags
- **Contributor Filter**: Filter albums by creator
- **Sort Options**: Sort by event (alphabetical) or date (chronological)
- **Combined Filtering**: Apply multiple filters simultaneously

### Album Management

- **Album CRUD Operations**: Create, read, update, delete albums
- **Photo Associations**: Link multiple photos to albums
- **Tag System**: Organize albums with customizable tags
- **Thumbnail Support**: Upload and display album thumbnails
- **Description Support**: Add detailed descriptions to albums
- **User Attribution**: Track album creators and contributors

### Technical Features

- **REST API**: Comprehensive RESTful endpoints with proper HTTP status codes
- **Database Optimization**: Efficient queries with JPA/Hibernate
- **Data Transfer Objects (DTOs)**: Clean separation of API and domain models
- **Liquibase Integration**: Database migration management
- **TypeScript Support**: Type-safe frontend development
- **Redux State Management**: Predictable state management
- **Responsive Design**: Works on desktop, tablet, and mobile devices

## 🛠 Technology Stack

### Backend

- **Java 21** - Programming language
- **Spring Boot 3.2.x** - Application framework
- **Spring Data JPA** - Data persistence
- **Hibernate** - ORM framework
- **H2/PostgreSQL** - Database options
- **Liquibase** - Database migration
- **Maven** - Build tool
- **JUnit 5** - Testing framework

### Frontend

- **React 18** - Frontend framework
- **TypeScript** - Type-safe JavaScript
- **Redux Toolkit** - State management
- **React Router** - Navigation
- **Bootstrap 5** - CSS framework
- **ReactStrap** - React Bootstrap components
- **Sass** - CSS preprocessor
- **Webpack** - Module bundler

### Testing

- **JUnit 5** - Backend unit testing
- **Mockito** - Mocking framework
- **Spring Boot Test** - Integration testing
- **Cypress** - End-to-end testing
- **Jest** - Frontend unit testing

## 🚀 Getting Started

### Prerequisites

- **Java 21** or higher
- **Node.js 22.14.0** or higher
- **npm** or **yarn**
- **Git**

### Installation

1. **Clone the repository**

   ```bash
   git clone <repository-url>
   cd gallery-system
   ```

2. **Install backend dependencies**

   ```bash
   ./mvnw clean install
   ```

3. **Install frontend dependencies**

   ```bash
   npm install
   ```

4. **Start the application**

   ```bash
   # Start backend (Spring Boot)
   ./mvnw spring-boot:run

   # In another terminal, start frontend (React)
   npm start
   ```

5. **Access the application**
   - Frontend: http://localhost:3000
   - Backend API: http://localhost:8080
   - Database Console (H2): http://localhost:8080/h2-console

### Development

#### Running Tests

```bash
# Backend tests
./mvnw test

# Frontend tests
npm test

# E2E tests
npm run e2e
```

#### Building for Production

```bash
# Build frontend
npm run webapp:build

# Build backend
./mvnw clean package -Pprod

# Run production build
java -jar target/*.jar
```

## 📚 API Documentation

### Album Endpoints

| Method   | Endpoint               | Description                    |
| -------- | ---------------------- | ------------------------------ |
| `GET`    | `/api/albums`          | Get all albums with pagination |
| `GET`    | `/api/albums/{id}`     | Get specific album by ID       |
| `POST`   | `/api/albums`          | Create new album               |
| `PUT`    | `/api/albums/{id}`     | Update existing album          |
| `DELETE` | `/api/albums/{id}`     | Delete album                   |
| `GET`    | `/api/albums/gallery`  | Get albums for gallery view    |
| `GET`    | `/api/albums/search`   | Search albums by keyword       |
| `GET`    | `/api/albums/filter`   | Filter albums by criteria      |
| `GET`    | `/api/albums/by-event` | Get albums by event name       |
| `GET`    | `/api/albums/by-date`  | Get albums by year             |

### Filter Parameters

- `keyword`: Search in name, keywords, description
- `event`: Filter by event name
- `year`: Filter by creation year
- `tagName`: Filter by tag name
- `contributorLogin`: Filter by contributor username
- `sortBy`: Sort by `EVENT` or `DATE`

### Example API Calls

```bash
# Get all albums
curl "http://localhost:8080/api/albums"

# Search albums
curl "http://localhost:8080/api/albums/search?keyword=vacation"

# Filter albums with multiple criteria
curl "http://localhost:8080/api/albums/filter?event=Wedding&year=2023&sortBy=DATE"

# Get albums for gallery view
curl "http://localhost:8080/api/albums/gallery?sortBy=EVENT"
```

## 🎨 User Interface Features

### Gallery View Features

1. **Statistics Dashboard**

   - Total albums count
   - Total photos count
   - Recent albums (last 7 days)
   - Active contributors count

2. **Search and Filter Panel**

   - Global search bar with real-time results
   - Collapsible filter panel
   - Active filter count badge
   - Clear all filters option

3. **View Controls**

   - Grid view (default)
   - List view (detailed)
   - Masonry view (coming soon)
   - Sort by event or date

4. **Album Cards**

   - Thumbnail preview with hover effects
   - Album metadata (name, event, date, owner)
   - Tag display with overflow handling
   - Action buttons (view, edit, select)

5. **Bulk Operations**
   - Multi-select albums
   - Select all functionality
   - Clear selection
   - Bulk delete (with confirmation)

### Responsive Design

- **Mobile (< 768px)**: Single column layout, touch-optimized controls
- **Tablet (768px - 992px)**: Two-column grid, optimized spacing
- **Desktop (> 992px)**: Multi-column grid, full feature set

## 🧪 Testing

### E2E Test Coverage

The application includes comprehensive Cypress E2E tests covering:

- Gallery layout and navigation
- Search functionality
- Filter operations
- Sort and view controls
- Album card interactions
- Bulk selection and operations
- Responsive design
- Accessibility features
- Performance metrics

### Test Data IDs

All interactive elements include `data-testid` attributes for reliable testing:

```typescript
// Search components
data-testid="search-input"
data-testid="search-btn"

// Filter components
data-testid="filters-toggle-btn"
data-testid="event-filter-input"
data-testid="year-filter-input"

// Album cards
data-testid="album-card-{id}"
data-testid="album-title-link-{id}"
data-testid="view-album-btn-{id}"
```

### Running E2E Tests

```bash
# Open Cypress Test Runner
npm run e2e:open

# Run tests headlessly
npm run e2e:run

# Run specific test file
npx cypress run --spec "src/test/javascript/e2e/album-gallery.cy.ts"
```

## 🔧 Configuration

### Database Configuration

The application supports multiple database configurations:

**Development (H2 in-memory)**

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password:
```

**Production (PostgreSQL)**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/gallerydb
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### JHipster Configuration

Key JHipster configuration options:

- **Authentication**: JWT-based
- **Database**: H2 (dev), PostgreSQL (prod)
- **Build Tool**: Maven
- **Frontend**: React with TypeScript
- **Testing**: Jest + Cypress
- **Internationalization**: Enabled
- **API Documentation**: OpenAPI/Swagger

## 📈 Performance Optimizations

### Frontend Optimizations

- **Code Splitting**: Lazy loading of routes and components
- **Image Optimization**: Responsive images with proper sizing
- **Bundle Optimization**: Tree shaking and minification
- **Caching**: Browser caching for static assets
- **Virtual Scrolling**: For large album lists (coming soon)

### Backend Optimizations

- **Database Indexing**: Optimized queries with proper indexes
- **Pagination**: Efficient data loading
- **Caching**: Spring Cache for frequently accessed data
- **Connection Pooling**: Optimized database connections
- **Query Optimization**: JPA query optimization

## 🔒 Security Features

- **JWT Authentication**: Secure token-based authentication
- **CORS Configuration**: Proper cross-origin resource sharing
- **Input Validation**: Server-side validation for all inputs
- **SQL Injection Prevention**: Parameterized queries
- **XSS Protection**: Output encoding and sanitization

## 🌍 Internationalization

The application supports multiple languages:

- **English** (default)
- **Additional languages** can be added via JHipster i18n

Translation keys are organized by feature:

```
gallerySystemApp.album.gallery.title
gallerySystemApp.album.gallery.search
gallerySystemApp.album.gallery.filters
```

## 🚀 Deployment

### Docker Deployment

```bash
# Build Docker image
./mvnw clean package -Pprod jib:dockerBuild

# Run with Docker Compose
docker-compose up
```

### Cloud Deployment

The application is ready for deployment on:

- **Heroku**: With buildpacks for Java and Node.js
- **AWS**: EC2, ECS, or Elastic Beanstalk
- **Google Cloud**: App Engine or Compute Engine
- **Azure**: App Service or Container Instances

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Development Guidelines

- Follow the existing code style and conventions
- Add tests for new features
- Update documentation as needed
- Use meaningful commit messages
- Ensure all tests pass before submitting PR

## 📝 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- **JHipster**: Application generator and framework
- **Spring Boot**: Backend framework
- **React**: Frontend framework
- **Bootstrap**: CSS framework
- **FontAwesome**: Icon library

---

## 📞 Support

For support and questions:

1. Check the [FAQ](docs/FAQ.md)
2. Review [troubleshooting guide](docs/TROUBLESHOOTING.md)
3. Open an issue on GitHub
4. Contact the development team

**Happy coding! 🎉**
