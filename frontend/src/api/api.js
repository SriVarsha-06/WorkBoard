

const API_BASE_URL = "https://workboard-lyv7.onrender.com";


function getAuthHeader(email, password) {
  return "Basic " + btoa(`${email}:${password}`);
}

async function request(path, options = {}, credentials = null) {
  const headers = {
    "Content-Type": "application/json",
    ...options.headers,
  };

  if (credentials) {
    headers.Authorization = getAuthHeader(
      credentials.email,
      credentials.password
    );
  }

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    const message = await response.text();
    throw new Error(
      message || `Request failed with status ${response.status}`
    );
  }

  if (response.status === 204) {
    return null;
  }

  const contentType = response.headers.get("content-type");

  if (contentType && contentType.includes("application/json")) {
    return response.json();
  }

  return null;
}

export async function registerUser(userData) {
  return request("/auth/register", {
    method: "POST",
    body: JSON.stringify(userData),
  });
}

export async function getTasks(credentials) {
  return request("/tasks", {}, credentials);
}


export async function createTask(taskData, credentials) {
  const taskWithPriority = {
    ...taskData,
    priority: taskData.priority || "MEDIUM",
  };

  return request(
    "/tasks",
    {
      method: "POST",
      body: JSON.stringify(taskWithPriority),
    },
    credentials
  );
}

export async function updateTask(id, taskData, credentials) {
  return request(
    `/tasks/${id}`,
    {
      method: "PUT",
      body: JSON.stringify(taskData),
    },
    credentials
  );
}



export async function updateTaskStatus(id, status, credentials) {
  return request(
    `/tasks/${id}/status`,
    {
      method: "PATCH",
      body: JSON.stringify({ status }),
    },
    credentials
  );
}

export async function deleteTask(id, credentials) {
  return request(
    `/tasks/${id}`,
    { method: "DELETE" },
    credentials
  );
}
