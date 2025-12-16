# Enjoy Reading Library Management System

## Project Overview
Enjoy Reading Library is a modern library management system built on Java EE standards, providing one-stop self-service for readers and efficient management tools for administrators, creating a smart reading space.

## Key Features
- **Reader Functions**: Registration/Login, Book Search, Borrow/Return, Comment Management, Seat Reservation, Booklist Management, Personal Information
- **Administrator Functions**: Book Management, Category Management, Announcement Publishing, Report Handling, Purchase Analysis

## Tech Stack
- **Backend**: Java EE 10, Servlet, JSP, JPA/Hibernate
- **Frontend**: HTML, CSS, JavaScript
- **Database**: PostgreSQL 12+
- **Application Server**: WildFly 27+
- **Build Tool**: Maven 3.8+

## System Architecture
Layered architecture design:
- **Presentation Layer**: Servlet + JSP + REST API
- **Business Layer**: Service + DTO
- **Data Access Layer**: Repository + Entity
- **Data Storage Layer**: PostgreSQL + JTA

## Quick Deployment
1. Install JDK 21, Maven, PostgreSQL, WildFly
2. Create database `library_db`
3. Configure WildFly data source `java:/jdbc/LibraryDS`
4. Run `mvn clean package` for build
5. Deploy WAR package to WildFly

## Project Highlights
- Complete CI/CD pipeline (GitHub Actions)
- Comprehensive unit test coverage (JUnit 5 + Mockito)
- Integrated monitoring with Prometheus + Grafana
- Standardized OpenAPI 3.0 documentation
- Modular layered design for maintainability and scalability


## Usage Guide
For detailed operational instructions, refer to the "User Manual & Deployment Configuration" section in the documentation.