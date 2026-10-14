# 💜 WorkBoard — Task Management Application

A full-stack task management application designed to help users organize their work, prioritize tasks, and track their progress through a clean, elegant interface.

🌐 **Live Demo:** [WorkBoard](https://work-board-varsha.vercel.app/)

## ✨ Features

- **User Authentication:** Register an account and log in to access your tasks.
- **Task Management:** Create, view, edit, and delete tasks.
- **Task Prioritization:** Organize tasks using High, Medium, and Low priority levels.
- **Task Status Tracking:** Update task status and mark tasks as completed.
- **Password Visibility Toggle:** Show or hide your password while logging in or registering.
- **Responsive Interface:** A clean interface with an elegant lavender theme.
- **Persistent Storage:** Store application data using PostgreSQL.

## 🛠️ Tech Stack

| Layer | Technologies |
|---|---|
| Frontend | React 19, Vite, JavaScript, CSS |
| Backend | Java 21, Spring Boot |
| Database | PostgreSQL |
| Frontend Hosting | Vercel |
| Backend Hosting | Render |
| Database Hosting | Neon |
| Version Control | Git and GitHub |

## 🏗️ Application Architecture

WorkBoard follows a full-stack architecture:

1. **Frontend:** React provides the user interface and handles user interactions.
2. **Backend:** Spring Boot exposes API endpoints and handles authentication and task-related operations.
3. **Database:** PostgreSQL stores user accounts and task information.
4. **Deployment:** Vercel hosts the frontend, Render hosts the backend, and Neon provides the PostgreSQL database.

## 🚀 Getting Started

### Prerequisites

Install the following tools before running the project locally:

- Java 21
- Node.js and npm
- Git
- PostgreSQL, or access to a configured PostgreSQL database

### 1. Clone the repository

```bash
git clone https://github.com/SriVarsha-06/WorkBoard.git
cd WorkBoard
```

### 2. Configure the backend

Configure the database connection in the backend's `application.properties` file or through environment variables.

Set the appropriate values for:

- Database URL
- Database username
- Database password

**Important:** Never commit database credentials, API secrets, or other sensitive configuration to GitHub.

Start the Spring Boot backend from the directory containing `mvnw.cmd`:

```powershell
.\mvnw.cmd spring-boot:run
```

The backend uses port `8080` by default.

### 3. Run the frontend

Open a separate terminal and navigate to the directory containing the frontend's `package.json` file.

Install the dependencies:

```bash
npm install
```

Start the development server:

```bash
npm run dev
```

Open the local URL printed by Vite in your terminal, usually `http://localhost:5173`.

Make sure the frontend API configuration points to your local backend when testing locally.

## ☁️ Deployment

The application is deployed using the following services:

- **Frontend:** [Vercel](https://work-board-varsha.vercel.app/)
- **Backend:** [Render](https://workboard-lyv7.onrender.com)
- **Database:** Neon PostgreSQL

## 🎯 Project Objective

The objective of WorkBoard is to provide a simple and intuitive platform for managing daily tasks, setting priorities, and tracking completion status. The project demonstrates full-stack application development, REST API integration, database connectivity, authentication, and cloud deployment.

## 🔮 Future Enhancements

Potential improvements include:

- Password recovery functionality
- Search, filtering, and sorting of tasks
- Task deadlines and reminders
- Dashboard analytics and progress summaries
- Enhanced automated testing

## 👩‍💻 Author

**Sri Varsha Baliwada**

GitHub: [SriVarsha-06](https://github.com/SriVarsha-06)

---

*Built with Java, Spring Boot, React, and PostgreSQL.* 💜
