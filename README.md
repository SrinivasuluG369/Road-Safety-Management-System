# Road Safety Management System

## Project Overview
The Road Safety Management System is a Java-based web application designed to identify road damage from uploaded road images using image processing and deep learning techniques.

The system detects road damage, estimates the damaged area, classifies road conditions, and stores inspection details for future reference.

## Technologies Used
- **Backend:** Java 25, Spring Boot 4.1.2
- **Frontend:** HTML, CSS, JavaScript
- **Database:** MySQL
- **Image Processing:** OpenCV
- **Deep Learning:** ONNX Runtime, YOLO
- **Build Tool:** Maven
- **IDE:** Spring Tool Suite (STS)

## Key Features
- Upload road images for inspection.
- Detect road damage using a trained ONNX model.
- Identify damage regions using bounding boxes.
- Estimate approximate road damage percentage.
- Classify road conditions as GOOD, FAIR, or POOR.
- Store road inspection history in MySQL.
- View inspection information through a web dashboard.

## Road Condition Classification

| Damage Percentage | Classification |
|---|---|
| Below 10% | GOOD |
| 10%–20% | FAIR |
| Above 20% | POOR |

*Classification is based on the project's approximate damage estimation logic.*

## Project Structure
```text
RoadSafetyManagement/
├── src/
│   ├── main/
│   │   ├── java/com/roadsafety/
│   │   │   ├── controller/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── models/
│   │       ├── static/
│   │       └── application.properties
│   └── test/
├── pom.xml
└── README.md
```

## Database Configuration
1. Install MySQL and create a database:

```sql
CREATE DATABASE road_safety_db;
```

2. Configure your database username and password using environment variables.

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/road_safety_db
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Never commit actual database passwords or secrets to GitHub.

## How to Run the Project

1. Clone the repository:

```bash
git clone https://github.com/SrinivasuluG369/Road-Safety-Management-System.git
```

2. Open the project in Spring Tool Suite or another Java IDE.

3. Configure the MySQL database and environment variables.

4. Ensure the required ONNX and YOLO model files are available.

5. Run the main Spring Boot application:

```text
RoadSafetyManagementApplication.java
```

6. Open the application in your browser:

```text
http://localhost:8080
```

## Future Enhancements
- Improve damage detection accuracy.
- Add GPS-based road inspection locations.
- Generate inspection reports.
- Add role-based user authentication.
- Improve dashboard analytics.

## Author
**Srinivasulu Gurram**

GitHub: https://github.com/SrinivasuluG369

## License
Add an appropriate license after checking the redistribution permissions for the included machine-learning models.
