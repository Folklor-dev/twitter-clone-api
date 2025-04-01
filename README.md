# Twitter Clone API

A REST API for a Twitter-like application built with Spring Boot and Groovy. This application supports user registration, authentication, posting content, following other users, liking posts and commenting on posts.

## Technology Stack

- **Programming Language**: Groovy 4.0.26
- **Framework**: Spring Boot 3.2.0
- **Database**: MongoDB
- **Build Tool**: Gradle 8.8
- **Testing**: Spock Framework
- **Deployment**: Docker

## Features

### User Management

- User registration
- User authentication (login/logout)
- User profile editing
- User account deletion
- Following/unfollowing other users

### Content Management

- Creating posts
- Editing posts
- Deleting posts
- Liking/unliking posts
- Commenting on posts

### Feed Management

- Retrieving a user's feed (posts from users they follow)
- Viewing another user's posts
- Retrieving comments for a specific post

## API Endpoints

### User Endpoints

- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - User login
- `POST /api/auth/logout` - User logout (client-side)
- `GET /api/users/me` - Get current user
- `GET /api/users/{id}` - Get user by ID
- `GET /api/users/username/{username}` - Get user by username
- `PUT /api/users/{id}` - Update a user
- `DELETE /api/users/{id}` - Delete a user
- `POST /api/users/{id}/follow` - Follow a user
- `DELETE /api/users/{id}/follow` - Unfollow a user

### Post Endpoints

- `POST /api/posts` - Create a new post
- `GET /api/posts/{id}` - Get post by ID
- `PUT /api/posts/{id}` - Update a post
- `DELETE /api/posts/{id}` - Delete a post
- `POST /api/posts/{id}/like` - Like a post
- `DELETE /api/posts/{id}/like` - Unlike a post
- `GET /api/feed` - Get authenticated user's feed
- `GET /api/users/{id}/posts` - Get posts from a specific user

### Comment Endpoints

- `POST /api/posts/{id}/comments` - Add a comment to a post
- `GET /api/posts/{id}/comments` - Get comments for a post

## Setting Up the Project

### Prerequisites

- JDK 21
- Gradle 8.8 (or use the included Gradle wrapper)
- Docker and Docker Compose (for running MongoDB and the application container)

### Java Version Compatibility

This project is designed to work with Java 21 and Groovy 4.0.26.

### Running Locally with Docker

1. Clone the repository
   ```bash
   git clone <repository-url>
   cd twitter-clone-api
   ```

2. Build the application
   ```bash
   ./gradlew clean build
   ```

3. Start the services using Docker Compose
   ```bash
   cd docker
   docker-compose up -d
   ```

4. The API will be available at `http://localhost:8080`

### Running Without Docker

1. Make sure you have MongoDB running locally on port 27017
   
2. Build and run the application
   ```bash
   ./gradlew bootRun
   ```

3. The API will be available at `http://localhost:8080`

## API Documentation

API documentation is generated using OpenAPI/Swagger and is available at `http://localhost:8080/swagger-ui.html` when the application is running.

## Running Tests

Run the tests using Gradle:

```bash
./gradlew test
```

## Security

The API uses JWT (JSON Web Token) for authentication. When a user logs in, they receive a JWT token that must be included in the Authorization header of subsequent requests as a Bearer token.

Example:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Troubleshooting

### Build Failures

If you encounter build failures related to class file versions, ensure you're using JDK 21. You can check your Java version with:

```bash
java -version
```

### MongoDB Connection Issues

Ensure MongoDB is running and accessible on the configured port (default: 27017). When using Docker, check that the MongoDB container is running:

```bash
docker ps
```
