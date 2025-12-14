# GrabTrash Backend API

<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen" alt="Spring Boot">
  <img src="https://img.shields.io/badge/Java-17-orange" alt="Java">
  <img src="https://img.shields.io/badge/Firebase-9.3.0-yellow" alt="Firebase">
  <img src="https://img.shields.io/badge/License-MIT-blue" alt="License">
</p>

A robust backend REST API for the **GrabTrash** waste management and collection system. This application provides a comprehensive platform for managing waste pickup requests, scheduling collections, fleet management, driver assignments, and user management with role-based access control.

---

## 📋 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running the Application](#-running-the-application)
- [Docker Deployment](#-docker-deployment)
- [User Roles](#-user-roles)
- [Project Structure](#-project-structure)
- [Testing](#-testing)
- [Troubleshooting](#-troubleshooting)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### User Management
- User registration and authentication with JWT tokens
- Role-based access control (Customer, Driver, Private Entity, Admin)
- Profile management with image upload
- Password recovery via security questions
- Email and password updates

### Waste Collection Management
- Pickup request creation and tracking
- Collection schedule management (one-time and recurring)
- Barangay-based scheduling
- Real-time status updates
- Waste type categorization (Recyclable, Non-recyclable)

### Fleet & Driver Management
- Truck registration and management
- Driver assignment to trucks
- Automated truck and driver assignment for pickups
- Truck availability tracking
- Capacity management

### Payment System
- Quote generation with automated pricing
- Multiple payment method support (GCash, etc.)
- Payment status tracking
- Order confirmation with image proof
- Service rating system

### Notifications
- Firebase Cloud Messaging (FCM) integration
- Push notifications for collection reminders
- Real-time status updates
- Barangay and role-based notifications

### Dashboard & Analytics
- Collection statistics
- Driver daily reports
- Barangay performance metrics
- Top barangays by pickup frequency
- Overall system statistics

---

## 🛠 Tech Stack

| Technology | Version | Description |
|------------|---------|-------------|
| **Java** | 17 | Programming language |
| **Spring Boot** | 3.4.4 | Application framework |
| **Spring Security** | - | Authentication & authorization |
| **Firebase Admin SDK** | 9.3.0 | Real-time database & cloud messaging |
| **Google Cloud Firestore** | 3.13.7 | NoSQL document database |
| **JWT (jjwt)** | 0.11.5 | Token-based authentication |
| **Lombok** | - | Boilerplate code reduction |
| **Maven** | - | Build and dependency management |
| **Docker** | - | Containerization |

---

## 🏗 Architecture

The application follows a layered architecture pattern:
┌─────────────────────────────────────────────────────────┐
│ Controllers │
│ (REST API endpoints - Handle HTTP requests/responses) │
├─────────────────────────────────────────────────────────┤
│ Services │
│ (Business logic and orchestration) │
├─────────────────────────────────────────────────────────┤
│ Models │
│ (Data entities and DTOs) │
├─────────────────────────────────────────────────────────┤
│ Firebase/Firestore │
│ (Data persistence) │
└─────────────────────────────────────────────────────────┘


---

## 📦 Prerequisites

Before running the application, ensure you have:

- **Java Development Kit (JDK) 17** or higher
- **Maven 3.6+** or use the included Maven wrapper
- **Firebase Project** with:
  - Firestore Database enabled
  - Firebase Cloud Messaging configured
  - Service Account Key (JSON file)

---

## 🚀 Installation

### Step 1: Clone the Repository

```bash
git clone https://github.com/your-username/GrabTrash-backend.git
cd GrabTrash-backend/GrabTrash 
```

Step 2: Configure Firebase

  1. Go to Firebase Console
  2. Select your project (or create a new one)
  3. Navigate to Project Settings > Service Accounts
  4. Click Generate New Private Key
  5. Download the JSON file
  6. Rename it to serviceAccountKey.json
  7. Place it in serviceAccountKey.json
  
Step 3: Update Application Properties
Edit application.properties:
  spring.application.name=GrabTrash
  server.port=8080
  firebase.database.url=https://your-firebase-project.firebaseio.com

Step 4: Install Dependencies
# Using Maven wrapper (recommended)
./mvnw clean install

# Windows
mvnw.cmd clean install

# Or with Maven installed globally
mvn clean install

⚙️ Configuration
Application Properties
Property	                              Description        	        Default
spring.application.name	                Application name	          GrabTrash
server.port	                            Server port	                8080
firebase.database.url	                  Firebase Realtime           Database URL	-
logging.level.root	                    Root logging level	        INFO
logging.level.com.capstone.GrabTrash	  Application logging level	  DEBUG

Environment Variables (Production)
Variable	              Description
FIREBASE_DATABASE_URL	  Firebase Realtime Database URL
JWT_SECRET	            Secret key for JWT token signing
SERVER_PORT	            Server port (default: 8080)

▶️ Running the Application
Development Mode
# Using Maven wrapper (Unix/Mac)
./mvnw spring-boot:run

# Using Maven wrapper (Windows)
mvnw.cmd spring-boot:run

# Using Maven
mvn spring-boot:run

Production Mode
# Build the JAR
./mvnw clean package -DskipTests

# Run the JAR
java -jar target/GrabTrash-0.0.1-SNAPSHOT.jar

Verify Installation
Once running, the API will be available at:
http://localhost:8080

Check the logs for:
Started GrabTrashApplication in X.XXX seconds

🐳 Docker Deployment
Build Docker Image
docker build -t grabtrash-backend .

Run Container
docker run -p 8080:8080 grabtrash-backend

Docker Compose
Create a docker-compose.yml file:

version: '3.8'
services:
  grabtrash-api:
    build: .
    ports:
      - "8080:8080"
    environment:
      - FIREBASE_DATABASE_URL=https://your-project.firebaseio.com
    volumes:
      - ./src/main/resources/serviceAccountKey.json:/app/serviceAccountKey.json
      
Run with Docker Compose:
docker-compose up -d

🧪 Testing
# Run all tests
./mvnw test

# Run tests with coverage
./mvnw test jacoco:report

# Run specific test class
./mvnw test -Dtest=GrabTrashApplicationTests

🔧 Troubleshooting
Common Issues
1. Firebase Connection Error
Make sure serviceAccountKey.json is in src/main/resources/
Verify the firebase.database.url in application.properties

2. Port Already in Use
# Change port in application.properties
server.port=8081

# Or kill the process using port 8080 (Windows)
netstat -ano | findstr :8080
taskkill /PID <PID> /F

3. Maven Build Failure
# Clear Maven cache and rebuild
./mvnw clean install -U

🤝 Contributing
  1. Fork the repository
  2. Create a feature branch (git checkout -b feature/amazing-feature)
  3. Commit your changes (git commit -m 'Add amazing feature')
  4. Push to the branch (git push origin feature/amazing-feature)
  5. Open a Pull Request
Code Style Guidelines
  1. Follow Java naming conventions
  2. Use meaningful variable and method names
  3. Add comments for complex logic
  4. Write unit tests for new features
  5. Use Lombok annotations to reduce boilerplate

📝 License
 - This project is licensed under the MIT License - see the LICENSE file for details.

👨‍💻 Authors
 - Capstone Team - Initial Development

🙏 Acknowledgments
 - Spring Boot team for the excellent framework
 - Firebase for cloud services
 - All contributors who helped build this project
