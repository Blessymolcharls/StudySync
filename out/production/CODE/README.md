# StudySync 📚

A collaborative study management system that helps students and teachers share files, manage tasks, and track study progress.

## Features 🌟

### File Management 📁
- Upload and store PDF files securely in the database
- Download shared files
- Track file ownership and upload history
- Role-based access control (students/teachers)
- File deletion (teacher only)

### Task Management ✅
- Create and assign tasks with priorities
- Track task status (pending/in-progress/completed)
- Support for multiple assignees per task
- Due date tracking
- Task completion history

### User Management 👥
- Role-based authentication (student/teacher/admin)
- Secure password storage
- Last login tracking
- Email-based user identification

## Tech Stack 💻

- **Language**: Java
- **Database**: MariaDB/MySQL
- **UI Framework**: Java Swing
- **Database Driver**: MariaDB JDBC Driver (3.5.3)
- **Build System**: Java Project Structure

## Database Schema 🗄️

### Core Tables
- `users`: User accounts and authentication
- `files`: Uploaded file storage and metadata
- `tasks`: Task management and tracking
- `task_assignments`: Multiple assignee support
- `file_access`: File permissions and sharing
- `file_comments`: File annotations and comments

## Setup Guide 🚀

### Prerequisites
- Java JDK 25 or later
- MariaDB/MySQL 10.x or later
- IDE (e.g., IntelliJ IDEA)

### Database Setup
1. Install MariaDB/MySQL
2. Create the database and user:
   ```sql
   CREATE DATABASE studysync;
   CREATE USER 'studysync'@'localhost' IDENTIFIED BY 'change_this_password';
   GRANT ALL PRIVILEGES ON studysync.* TO 'studysync'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Import the schema:
   ```bash
   mysql -u root -p studysync < db/schema.sql
   ```

### Application Setup
1. Clone the repository:
   ```bash
   git clone https://github.com/Blessymolcharls/StudySync.git
   ```
2. Open the project in your IDE
3. Add MariaDB JDBC driver to your classpath:
   - Location: `lib/mariadb-java-client-3.5.3.jar`
4. Configure database connection in `db/DBConnection.java`:
   ```java
   private static final String URL = "jdbc:mariadb://localhost:3306/studysync";
   private static final String USER = "studysync";
   private static final String PASS = "change_this_password";
   ```

### Running the Application
1. Compile the project
2. Run `Main.java`
3. Login with default admin credentials:
   - Email: admin@studysync.com
   - Password: changeme (change this after first login)

## Security Notes 🔒

- Change default passwords immediately after setup
- Keep database credentials secure
- Use strong passwords for all accounts
- Regular backups recommended
- File uploads are stored in the database for security

## Project Structure 📂

```
StudySync/
├── src/
│   ├── db/
│   │   ├── DBConnection.java   # Database connectivity
│   │   ├── DBInit.java        # Database initialization
│   │   └── schema.sql         # Database schema
│   ├── gui/
│   │   ├── PomodoroTimer.java # Study timer
│   │   ├── ScheduleManager.java# Schedule handling
│   │   └── TaskManager.java   # Task management
│   ├── FileHandler.java       # File operations
│   ├── Main.java             # Application entry
│   └── UserAuth.java         # Authentication
├── lib/
│   └── mariadb-java-client-3.5.3.jar
└── README.md
```

## Contributing 🤝

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Open a Pull Request

## License 📄

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments 🙏

- MariaDB/MySQL team for the database
- Java Swing for the GUI framework
- All contributors and testers