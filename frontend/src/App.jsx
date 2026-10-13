
import { useState } from "react";
import {
  registerUser,
  getTasks,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
} from "./api/api";
import "./App.css";

function getTodayLocalDate() {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, "0");
  const day = String(now.getDate()).padStart(2, "0");

  return `${year}-${month}-${day}`;
}

function formatDate(dateString) {
  if (!dateString) return "";

  const [year, month, day] = dateString.split("-");
  return `${day}-${month}-${year}`;
}

function App() {
  const [mode, setMode] = useState("login");
  const [credentials, setCredentials] = useState(null);

  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [tasks, setTasks] = useState([]);
  const [filter, setFilter] = useState("ALL");
  const [search, setSearch] = useState("");
  const [sortBy, setSortBy] = useState("PRIORITY");

  const [activePage, setActivePage] = useState("tasks");
  const [showForm, setShowForm] = useState(false);

  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [priority, setPriority] = useState("MEDIUM");
  const [dueDate, setDueDate] = useState("");
const [darkMode, setDarkMode] = useState(false);
  const [editingTaskId, setEditingTaskId] = useState(null);
  const [editForm, setEditForm] = useState({
    title: "",
    description: "",
    priority: "MEDIUM",
    dueDate: "",
  });

  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  // Login and registration
  async function handleAuth(event) {
    event.preventDefault();
    setError("");
    setNotice("");
    setBusy(true);

    try {
      if (mode === "register") {
        await registerUser({
          name: name.trim(),
          email: email.trim(),
          password,
        });

        setMode("login");
        setPassword("");
        setNotice("Account created. Please log in.");
      } else {
        const nextCredentials = {
          email: email.trim(),
          password,
        };

        const result = await getTasks(nextCredentials);

        setCredentials(nextCredentials);
        setTasks(Array.isArray(result) ? result : []);
        setPassword("");
        setActivePage("tasks");
      }
    } catch (err) {
      setError(err.message || "Something went wrong.");
    } finally {
      setBusy(false);
    }
  }

  // Reload tasks from the backend
  async function refreshTasks() {
    if (!credentials) return;

    const result = await getTasks(credentials);
    setTasks(Array.isArray(result) ? result : []);
  }

  // Create a task
  async function addTask(event) {
    event.preventDefault();

    if (!title.trim() || !credentials) return;

    setError("");
    setNotice("");
    setBusy(true);

    try {
      await createTask(
        {
          title: title.trim(),
          description: description.trim(),
          priority,
          dueDate: dueDate || null,
        },
        credentials
      );

      await refreshTasks();

      setTitle("");
      setDescription("");
      setPriority("MEDIUM");
      setDueDate("");
      setShowForm(false);
      setNotice("Task created successfully.");
    } catch (err) {
      setError(err.message || "Could not create the task.");
    } finally {
      setBusy(false);
    }
  }

  // Begin editing a task
  function startEditing(task) {
    setEditingTaskId(task.id);

    setEditForm({
      title: task.title || "",
      description: task.description || "",
      priority: task.priority || "MEDIUM",
      dueDate: task.dueDate || "",
    });

    setError("");
    setNotice("");
  }

  // Save edited task
  async function saveTaskEdit(event) {
    event.preventDefault();

    if (!editForm.title.trim()) {
      setError("Task title cannot be empty.");
      return;
    }

    setBusy(true);
    setError("");
    setNotice("");

    try {
      await updateTask(
        editingTaskId,
        {
          title: editForm.title.trim(),
          description: editForm.description.trim(),
          priority: editForm.priority,
          dueDate: editForm.dueDate || null,
        },
        credentials
      );

      await refreshTasks();

      setEditingTaskId(null);
      setNotice("Task updated successfully.");
    } catch (err) {
      setError(err.message || "Could not update task.");
    } finally {
      setBusy(false);
    }
  }

  // Change task status
  async function updateStatus(id, status) {
    if (!credentials) return;

    setError("");
    setNotice("");

    try {
      await updateTaskStatus(id, status, credentials);
      await refreshTasks();
    } catch (err) {
      setError(err.message || "Could not update the task status.");
    }
  }

  // Delete task
  async function removeTask(id) {
    if (!credentials) return;

    setError("");
    setNotice("");

    try {
      await deleteTask(id, credentials);
      await refreshTasks();
      setNotice("Task deleted successfully.");
    } catch (err) {
      setError(err.message || "Could not delete the task.");
    }
  }

  // Log out
  function logout() {
    setCredentials(null);
    setTasks([]);
    setEmail("");
    setPassword("");
    setError("");
    setNotice("");
    setMode("login");
    setActivePage("tasks");
    setShowForm(false);
    setEditingTaskId(null);
  }

  // Task sorting
  const priorityOrder = {
    HIGH: 1,
    MEDIUM: 2,
    LOW: 3,
  };

  // Search, filter, and sort tasks
  const visibleTasks = tasks
    .filter((task) => {
      const matchesFilter =
        filter === "ALL" || task.status === filter;

      const matchesSearch =
        `${task.title} ${task.description || ""}`
          .toLowerCase()
          .includes(search.toLowerCase());

      return matchesFilter && matchesSearch;
    })
    .sort((a, b) => {
      if (sortBy === "PRIORITY") {
        return (
          (priorityOrder[a.priority] ?? 2) -
          (priorityOrder[b.priority] ?? 2)
        );
      }

      if (sortBy === "TITLE") {
        return (a.title || "").localeCompare(b.title || "");
      }

      return 0;
    });

  // Dashboard statistics
  const completed = tasks.filter(
    (task) => task.status === "COMPLETED"
  ).length;

  const pending = tasks.filter(
    (task) => task.status === "PENDING"
  ).length;

  const inProgress = tasks.filter(
    (task) => task.status === "IN_PROGRESS"
  ).length;

  const overdue = tasks.filter(
    (task) =>
      task.dueDate &&
      task.dueDate < getTodayLocalDate() &&
      task.status !== "COMPLETED"
  ).length;

  const completionPercentage = tasks.length
    ? Math.round((completed / tasks.length) * 100)
    : 0;

  // Login and registration screen
  if (!credentials) {
    return (
      <div className={`auth-page ${darkMode ? "dark-mode" : ""}`}>
        
        <form className="auth-card" onSubmit={handleAuth}>
          <a className="brand auth-brand" href="#home">
            <span className="brand-icon">W</span>
            <span>WorkBoard</span>
          </a>

          <p className="eyebrow">YOUR SPACE, YOUR PACE</p>

          <h1>
            {mode === "register"
              ? "Create your account"
              : "Welcome back"}
          </h1>

          <p className="welcome-copy">
            {mode === "register"
              ? "Create an account to organize your work."
              : "Log in to continue to your workspace."}
          </p>

          {mode === "register" && (
            <>
              <label htmlFor="name">Name</label>
              <input
                id="name"
                value={name}
                onChange={(event) => setName(event.target.value)}
                maxLength={100}
                autoComplete="name"
                required
              />
            </>
          )}

          <label htmlFor="email">Email</label>
          <input
            id="email"
            type="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            autoComplete="email"
            required
          />

          
<label htmlFor="password">Password</label>

<div className="password-input-wrapper">
  <input
    id="password"
    type={showPassword ? "text" : "password"}
    value={password}
    onChange={(event) => setPassword(event.target.value)}
    minLength={8}
    maxLength={72}
    autoComplete={
      mode === "register"
        ? "new-password"
        : "current-password"
    }
    required
  />

  <button
    type="button"
    className="password-toggle"
    onClick={() => setShowPassword((visible) => !visible)}
    aria-label={showPassword ? "Hide password" : "Show password"}
    aria-pressed={showPassword}
  >
    {showPassword ? "🙈" : "👁️"}
  </button>
</div>


          {error && <p className="error-message">{error}</p>}
          {notice && <p className="success-message">{notice}</p>}

          <button
            className="primary-button auth-submit"
            type="submit"
            disabled={busy}
          >
            {busy
              ? "Please wait..."
              : mode === "register"
                ? "Create account"
                : "Log in"}
          </button>

          <p className="auth-switch">
            {mode === "register"
              ? "Already have an account?"
              : "New to WorkBoard?"}{" "}
            <button
              type="button"
              className="text-button"
              onClick={() => {
                setMode(
                  mode === "register" ? "login" : "register"
                );
                setError("");
                setNotice("");
              }}
            >
              {mode === "register" ? "Log in" : "Create account"}
            </button>
          </p>
        </form>
      </div>
    );
  }

  return (
    <div className={`app-shell ${darkMode ? "dark-mode" : ""}`}>
      {/* Sidebar */}
      <aside className="sidebar">
        <a
          className="brand"
          href="#home"
          onClick={() => setActivePage("overview")}
        >
          <span className="brand-icon">W</span>
          <span>WorkBoard</span>
        </a>

        <p className="nav-label">WORKSPACE</p>

        <nav className="navigation">
          <button
            type="button"
            className={`nav-item ${
              activePage === "tasks" ? "active" : ""
            }`}
            onClick={() => {
              setActivePage("tasks");
              setNotice("");
              setError("");
            }}
          >
            <span>▦</span> My Tasks
          </button>

          <button
            type="button"
            className={`nav-item ${
              activePage === "overview" ? "active" : ""
            }`}
            onClick={() => {
              setActivePage("overview");
              setShowForm(false);
              setNotice("");
              setError("");
            }}
          >
            <span>◷</span> Overview
          </button>
        </nav>

        <div className="sidebar-bottom">
          <div className="profile-avatar">
            {(credentials.email[0] || "U").toUpperCase()}
          </div>

          <div>
            <strong>{credentials.email}</strong>
            <p>Personal workspace</p>

            <button
              type="button"
              className="text-button"
              onClick={logout}
            >
              Log out
            </button>
          </div>
        </div>
      </aside>

      {/* Main content */}
      <main className="main-content" id="home">
        <header className="topbar">
          <span className="breadcrumb">
            Workspace /{" "}
            {activePage === "overview" ? "Overview" : "My Tasks"}
          </span>

          <span className="today-label">
            YOUR SPACE, YOUR PACE
          </span>
          <button
  type="button"
  className="theme-toggle"
  onClick={() => setDarkMode(!darkMode)}
  aria-label={darkMode ? "Switch to light mode" : "Switch to dark mode"}
>
  {darkMode ? "☀️ Light mode" : "🌙 Dark mode"}
</button>
        </header>

        {error && <p className="error-message">{error}</p>}
        {notice && <p className="success-message">{notice}</p>}

        {/* OVERVIEW DASHBOARD */}
        {activePage === "overview" && (
          <div className="overview-page">
            <section className="welcome-section">
              <div>
                <p className="eyebrow">
                  YOUR WORKSPACE AT A GLANCE
                </p>

                <h1>Overview</h1>

                <p className="welcome-copy">
                  A little progress adds up. Here's how your work
                  is going.
                </p>
              </div>

              <button
                type="button"
                className="primary-button"
                onClick={() => {
                  setActivePage("tasks");
                  setShowForm(true);
                  setError("");
                  setNotice("");
                }}
              >
                <span>＋</span> New task
              </button>
            </section>

            {/* Summary cards */}
            <section className="stats-grid">
              <article className="stat-card">
                <span className="stat-icon lavender">▦</span>
                <p>Total Tasks</p>
                <strong>{tasks.length}</strong>
                <span className="stat-note">All your tasks</span>
              </article>

              <article className="stat-card">
                <span className="stat-icon peach">◷</span>
                <p>Pending</p>
                <strong>{pending}</strong>
                <span className="stat-note">
                  Waiting to be started
                </span>
              </article>

              <article className="stat-card">
                <span className="stat-icon mint">✓</span>
                <p>Completed</p>
                <strong>{completed}</strong>
                <span className="stat-note">Tasks finished</span>
              </article>

              <article className="stat-card">
                <span className="stat-icon peach">!</span>
                <p>Overdue</p>
                <strong>{overdue}</strong>
                <span className="stat-note">
                  Past due, unfinished
                </span>
              </article>
            </section>

            {/* Progress panel */}
            <section className="overview-panel">
              <div className="overview-panel-heading">
                <div>
                  <p className="eyebrow">YOUR PROGRESS</p>
                  <h2>Productivity snapshot</h2>
                </div>

                <span className="progress-percentage">
                  {completionPercentage}%
                </span>
              </div>

              <p>
                {tasks.length === 0
                  ? "Your workspace is ready. Create your first task to get started."
                  : completed === tasks.length
                    ? "Wonderful work! All your tasks are completed."
                    : `${completed} of ${tasks.length} tasks completed. Keep making progress at your own pace.`}
              </p>

              <div
                className="overview-progress"
                role="progressbar"
                aria-label="Task completion"
                aria-valuenow={completionPercentage}
                aria-valuemin={0}
                aria-valuemax={100}
              >
                <div
                  className="overview-progress-fill"
                  style={{ width: `${completionPercentage}%` }}
                />
              </div>

              <div className="overview-status-summary">
                <span>
                  <span className="summary-dot pending-dot" />
                  Pending: {pending}
                </span>

                <span>
                  <span className="summary-dot progress-dot" />
                  In progress: {inProgress}
                </span>

                <span>
                  <span className="summary-dot completed-dot" />
                  Completed: {completed}
                </span>
              </div>
            </section>

            {/* Task preview */}
            <section className="overview-panel">
              <div className="section-heading">
                <div>
                  <h2>Your tasks</h2>
                  <p>A quick look at your workspace.</p>
                </div>

                <button
                  type="button"
                  className="secondary-button"
                  onClick={() => setActivePage("tasks")}
                >
                  View all tasks
                </button>
              </div>

              {tasks.length === 0 ? (
                <div className="empty-state">
                  <span>✧</span>
                  <h3>No tasks yet</h3>
                  <p>
                    Create your first task to see it here.
                  </p>

                  <button
                    type="button"
                    className="primary-button"
                    onClick={() => {
                      setActivePage("tasks");
                      setShowForm(true);
                    }}
                  >
                    ＋ Create your first task
                  </button>
                </div>
              ) : (
                <div className="task-list">
                  {tasks.slice(0, 5).map((task) => (
                    <article
                      className="task-card"
                      key={task.id}
                    >
                      <div className="task-marker">
                        {task.status === "COMPLETED" ? "✓" : "○"}
                      </div>

                      <div className="task-details">
                        <h3>{task.title}</h3>

                        <p>
                          {task.description ||
                            "No description provided."}
                        </p>

                        <div className="task-meta">
                          <span
                            className={`status status-${(
                              task.status || "PENDING"
                            ).toLowerCase()}`}
                          >
                            {(task.status || "PENDING").replaceAll(
                              "_",
                              " "
                            )}
                          </span>

                          <span
                            className={`priority priority-${(
                              task.priority || "MEDIUM"
                            ).toLowerCase()}`}
                          >
                            {task.priority || "MEDIUM"} PRIORITY
                          </span>
                        </div>

                        {task.dueDate && (
                          <div className="task-deadline">
                            <span>
                              Due: {formatDate(task.dueDate)}
                            </span>

                            {task.status !== "COMPLETED" &&
                              task.dueDate < getTodayLocalDate() && (
                                <span className="overdue-badge">
                                  Overdue
                                </span>
                              )}
                          </div>
                        )}
                      </div>
                    </article>
                  ))}
                </div>
              )}

              {tasks.length > 5 && (
                <button
                  type="button"
                  className="text-button overview-more-button"
                  onClick={() => setActivePage("tasks")}
                >
                  View all {tasks.length} tasks →
                </button>
              )}
            </section>
          </div>
        )}

        {/* MY TASKS PAGE */}
        {activePage === "tasks" && (
          <div className="tasks-page">
            <section className="welcome-section">
              <div>
                <p className="eyebrow">
                  MONDAY MOTIVATION, EVERY DAY
                </p>

                <h1>Make room for great work.</h1>

                <p className="welcome-copy">
                  Your tasks, your priorities, all in one calm
                  space.
                </p>
              </div>

              <button
                type="button"
                className="primary-button"
                onClick={() => {
                  setShowForm(!showForm);
                  setError("");
                  setNotice("");
                }}
              >
                <span>＋</span> New task
              </button>
            </section>

            {/* Summary cards */}
            <section className="stats-grid">
              <article className="stat-card">
                <span className="stat-icon lavender">▦</span>
                <p>Total Tasks</p>
                <strong>{tasks.length}</strong>
                <span className="stat-note">All your tasks</span>
              </article>

              <article className="stat-card">
                <span className="stat-icon peach">◷</span>
                <p>Pending</p>
                <strong>{pending}</strong>
                <span className="stat-note">
                  Waiting to be started
                </span>
              </article>

              <article className="stat-card">
                <span className="stat-icon mint">✓</span>
                <p>Completed</p>
                <strong>{completed}</strong>
                <span className="stat-note">
                  Tasks finished
                </span>
              </article>

              <article className="stat-card">
                <span className="stat-icon peach">!</span>
                <p>Overdue</p>
                <strong>{overdue}</strong>
                <span className="stat-note">
                  Past due, unfinished
                </span>
              </article>
            </section>

            {/* Create task form */}
            {showForm && (
              <form className="task-form" onSubmit={addTask}>
                <h2>Create a task</h2>

                <label htmlFor="task-title">Task title</label>
                <input
                  id="task-title"
                  value={title}
                  onChange={(event) =>
                    setTitle(event.target.value)
                  }
                  maxLength={100}
                  placeholder="What needs to get done?"
                  required
                />

                <label htmlFor="task-description">
                  Description
                </label>
                <textarea
                  id="task-description"
                  value={description}
                  onChange={(event) =>
                    setDescription(event.target.value)
                  }
                  maxLength={1000}
                  placeholder="Add a few details (optional)"
                  rows={3}
                />

                <label htmlFor="task-priority">Priority</label>
                <select
                  id="task-priority"
                  value={priority}
                  onChange={(event) =>
                    setPriority(event.target.value)
                  }
                >
                  <option value="HIGH">High</option>
                  <option value="MEDIUM">Medium</option>
                  <option value="LOW">Low</option>
                </select>

                <label htmlFor="task-due-date">
                  Due date (optional)
                </label>
                <input
                  id="task-due-date"
                  type="date"
                  value={dueDate}
                  onChange={(event) =>
                    setDueDate(event.target.value)
                  }
                />

                <div className="form-actions">
                  <button
                    type="button"
                    className="secondary-button"
                    onClick={() => setShowForm(false)}
                  >
                    Cancel
                  </button>

                  <button
                    className="primary-button"
                    type="submit"
                    disabled={busy}
                  >
                    {busy ? "Saving..." : "Create task"}
                  </button>
                </div>
              </form>
            )}

            {/* Task list, filters and sorting */}
            <section className="tasks-section" id="tasks">
              <div className="section-heading">
                <div>
                  <h2>Your tasks</h2>
                  <p>A little progress adds up.</p>
                </div>

                <input
                  className="search-input"
                  value={search}
                  onChange={(event) =>
                    setSearch(event.target.value)
                  }
                  placeholder="Search tasks..."
                  aria-label="Search tasks"
                />
              </div>

              <div className="filter-row">
                {[
                  ["ALL", "All tasks"],
                  ["PENDING", "To do"],
                  ["IN_PROGRESS", "In progress"],
                  ["COMPLETED", "Completed"],
                ].map(([value, label]) => (
                  <button
                    type="button"
                    key={value}
                    className={`filter-button ${
                      filter === value ? "selected" : ""
                    }`}
                    onClick={() => setFilter(value)}
                  >
                    {label}
                  </button>
                ))}
              </div>

              <div className="sort-row">
                <label htmlFor="sortTasks">Sort by:</label>

                <select
                  id="sortTasks"
                  value={sortBy}
                  onChange={(event) =>
                    setSortBy(event.target.value)
                  }
                  className="sort-select"
                >
                  <option value="PRIORITY">
                    Priority: High to Low
                  </option>
                  <option value="TITLE">Title: A to Z</option>
                </select>
              </div>

              <div className="task-list">
                {visibleTasks.map((task) => (
                  <article
                    className="task-card"
                    key={task.id}
                  >
                    <div className="task-marker">
                      {task.status === "COMPLETED" ? "✓" : "○"}
                    </div>

                    {/* Inline edit form */}
                    {editingTaskId === task.id ? (
                      <form
                        className="task-form"
                        onSubmit={saveTaskEdit}
                      >
                        <h3>Edit task</h3>

                        <label htmlFor={`edit-title-${task.id}`}>
                          Task title
                        </label>
                        <input
                          id={`edit-title-${task.id}`}
                          value={editForm.title}
                          onChange={(event) =>
                            setEditForm({
                              ...editForm,
                              title: event.target.value,
                            })
                          }
                          maxLength={100}
                          required
                        />

                        <label
                          htmlFor={`edit-description-${task.id}`}
                        >
                          Description
                        </label>
                        <textarea
                          id={`edit-description-${task.id}`}
                          value={editForm.description}
                          onChange={(event) =>
                            setEditForm({
                              ...editForm,
                              description: event.target.value,
                            })
                          }
                          maxLength={1000}
                          rows={3}
                        />

                        <label
                          htmlFor={`edit-priority-${task.id}`}
                        >
                          Priority
                        </label>
                        <select
                          id={`edit-priority-${task.id}`}
                          value={editForm.priority}
                          onChange={(event) =>
                            setEditForm({
                              ...editForm,
                              priority: event.target.value,
                            })
                          }
                        >
                          <option value="HIGH">High</option>
                          <option value="MEDIUM">Medium</option>
                          <option value="LOW">Low</option>
                        </select>

                        <label
                          htmlFor={`edit-due-date-${task.id}`}
                        >
                          Due date (optional)
                        </label>
                        <input
                          id={`edit-due-date-${task.id}`}
                          type="date"
                          value={editForm.dueDate}
                          onChange={(event) =>
                            setEditForm({
                              ...editForm,
                              dueDate: event.target.value,
                            })
                          }
                        />

                        <div className="form-actions">
                          <button
                            type="button"
                            className="secondary-button"
                            onClick={() =>
                              setEditingTaskId(null)
                            }
                            disabled={busy}
                          >
                            Cancel
                          </button>

                          <button
                            type="submit"
                            className="primary-button"
                            disabled={busy}
                          >
                            {busy
                              ? "Saving..."
                              : "Save changes"}
                          </button>
                        </div>
                      </form>
                    ) : (
                      <>
                        <div className="task-details">
                          <h3>{task.title}</h3>

                          <p>
                            {task.description ||
                              "No description provided."}
                          </p>

                          <div className="task-meta">
                            <span
                              className={`status status-${(
                                task.status || "PENDING"
                              ).toLowerCase()}`}
                            >
                              {(task.status || "PENDING").replaceAll(
                                "_",
                                " "
                              )}
                            </span>

                            <span
                              className={`priority priority-${(
                                task.priority || "MEDIUM"
                              ).toLowerCase()}`}
                            >
                              {task.priority || "MEDIUM"} PRIORITY
                            </span>
                          </div>

                          {task.dueDate && (
                            <div className="task-deadline">
                              <span>
                                Due: {formatDate(task.dueDate)}
                              </span>

                              {task.status !== "COMPLETED" &&
                                task.dueDate <
                                  getTodayLocalDate() && (
                                  <span className="overdue-badge">
                                    Overdue
                                  </span>
                                )}
                            </div>
                          )}
                        </div>

                        <select
                          className="status-select"
                          value={task.status}
                          aria-label={`Status for ${task.title}`}
                          onChange={(event) =>
                            updateStatus(
                              task.id,
                              event.target.value
                            )
                          }
                        >
                          <option value="PENDING">To do</option>
                          <option value="IN_PROGRESS">
                            In progress
                          </option>
                          <option value="COMPLETED">
                            Completed
                          </option>
                        </select>

                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => startEditing(task)}
                          disabled={busy}
                          aria-label={`Edit ${task.title}`}
                        >
                          Edit
                        </button>

                        <button
                          type="button"
                          className="secondary-button"
                          onClick={() => removeTask(task.id)}
                          disabled={busy}
                          aria-label={`Delete ${task.title}`}
                        >
                          Delete
                        </button>
                      </>
                    )}
                  </article>
                ))}

                {visibleTasks.length === 0 && (
                  <div className="empty-state">
                    <span>✧</span>
                    <h3>No tasks found</h3>
                    <p>
                      Try another search or create a new task.
                    </p>
                  </div>
                )}
              </div>

              <p className="demo-notice">
                Connected to your WorkBoard backend.
              </p>
            </section>
          </div>
        )}
      </main>
    </div>
  );
}

export default App;
