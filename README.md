# 🚀 JourneyPlanner – Full Stack Microservice App

**Plan smarter, travel better.**  
**Built with 💚 Vaadin + Spring Boot + Microservices.**

---

### 🧠 Overview

**JourneyPlanner** is a full-stack enterprise-grade web application for planning travel routes using microservices architecture. It leverages the power of **Spring Boot 5.4.3**, **OAuth2**, **JWT**, and **Vaadin** to provide a seamless, secure, and responsive user experience.

🔗 Uses multiple microservices like:

- `gateway-service` 
- `user-service` (Create user with spotify credentials)
- `route-service` (Nominatim finding places)
- `music-service` (Spotify playlists and tracks)
- `ui-service` (Vaadin frontend with SecurityFilterChain)
- `eureka-service` (Service Discovery)
- `config-service` (Centralized config)
- `config-repo` 

---

### 🧰 Tech Stack

| Layer         | Tech                                                     |
|---------------|-----------------------------------------------------------|
| Frontend      | 🔵 Vaadin (Java UI Framework)                            |
| Backend       | ☕ Spring Boot 5.4.3, Spring Security, Spring Cloud       |
| Auth          | 🔐 OAuth2, JWT-based auth (no roles)                     |
| DevOps        | 🐳 Docker, Docker Compose, Gradle                        |
| Routing       | 🌐 Spring Cloud Gateway                                  |
| Discovery     | 🧭 Netflix Eureka                                        |
| Data Layer    | 💾 PostgreSQL via Docker                                 |
| Tests         | ❌ No test database currently configured                 |

---

### 🗺️ Microservices Architecture

```
                           +------------------+
                           |  config-service  |
                           +------------------+
                                     |
                                     v
                         +----------------------+
                         |    eureka-service    |
                         +-----------+--------- +
                                     |
                                     v
        +-----------------+------------------+-------------------+
        |                 |                  |                   |
        v                 v                  v                   v  
+---------------+ +---------------+ +------------------+ +-------------+
| user-service  | | route-service | |   music-service  | | ui-service |
+---------------+ +---------------+ +------------------+ +-------------+
        \                 |                  |                   /
         +----------------+------------------+------------------+
                                     |
                          +----------------------+
                          |   gateway-service    |
                          +----------------------+
                                     |
                                     v
                          +----------------------+
                          |       frontend       |
                          +----------------------+
```

---

### 🔐 Security

- **OAuth2 Login** with `.successHandler(...)`
- **Form-based login** fallback (`formLogin().disable()` optional)
- **Logout configuration** in `ui-service` and `gateway-service`
  - Session invalidation
  - Cookie cleanup (`jwt`, `spotify_access_token`)
- **No roles** – JWT grants general access

---

### 🚀 Running the App

```bash
./gradlew clean build
docker-compose up --build
```

---

### 🖼️ Frontend (Vaadin)

- **Reactive UI** using Vaadin 24
- **Spring Security-aware views**
- **JWT-aware access logic**
- Runs inside `ui-service` Docker container

---

### ⚙️ Dev Profiles & Config

Environment config is handled via **Spring Cloud Config**. Supported profiles:

- `docker` (default for compose)
- No test DB profile

Configuration stored in `config-repo` and pulled during service startup.

---

### 🗃️ Folder Structure

```
JourneyPlanner/
├── config-service/
├── config-repo/
├── eureka-service/
├── gateway-service/
├── route-service/
├── music-service/
├── user-service/
├── ui-service/
├── build.gradle
├── docker-compose.yml
└── README.md
```

---

### 🧙 Tips for Deployment

1. Place NGINX or Traefik in front of `gateway-service` for SSL
2. Use centralized logging (e.g., Loki + Grafana)
3. Enable Docker health checks for all services

---

### 🧑‍💻 Maintainer

- **Milosz Podsiadly**
- ✉️ [m.podsiadly99@gmail.com](mailto:m.podsiadly99@gmail.com)
- 🔗 [GitHub – MiloszPodsiadly](https://github.com/MiloszPodsiadly)

---

### 📜 License

Licensed under the [MIT License](https://opensource.org/licenses/MIT).