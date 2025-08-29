# EventFlow - Smart Event Management

EventFlow is a modern, full-stack event reminder and management system. It features a beautiful web interface, a Java backend with data structures, and real-time event management capabilities.

## Features

- Add, view, complete, and delete events
- Smart reminders and analytics dashboard
- Undo last delete
- Responsive, animated UI with pastel dark theme
- Keyboard shortcuts and accessibility features
- Java backend with custom data structures (ArrayList, LinkedList, Stack, Queue, BinaryTree)
- RESTful API endpoints for event management

## Project Structure

```
Event_Reminder_System/
├── readme.md.txt
├── lib/
│   └── gson-2.8.9.jar
├── src/
│   └── Event_Reminder_System/
│       ├── Event.class
│       ├── EventWebServer.class
│       ├── Executer.java
│       └── ... (other Java classes)
└── web/
    ├── index.html
    ├── script.js
    └── styles.css
```

## Getting Started

### Prerequisites

- Java 11 or higher
- Node.js (optional, for frontend development)

### Running the Application

1. **Compile the Java backend:**

   ```
   javac -cp "lib/gson-2.8.9.jar" -d bin src/Event_Reminder_System/Executer.java
   ```

2. **Start the server:**

   ```
   java -cp "bin;lib/gson-2.8.9.jar" Event_Reminder_System.Executer
   ```

   The server will start at [http://localhost:8080](http://localhost:8080).

3. **Open the web interface:**

   Visit [http://localhost:8080](http://localhost:8080) in your browser.

### Keyboard Shortcuts

- <kbd>Ctrl</kbd> + <kbd>N</kbd>: Add new event
- <kbd>Ctrl</kbd> + <kbd>F</kbd> or <kbd>/</kbd>: Focus search
- <kbd>Esc</kbd>: Close modal

## API Endpoints

- `GET /api/events` - List all events
- `POST /api/events/add` - Add new event
- `POST /api/events/complete` - Mark event as completed
- `DELETE /api/events/delete?index={n}` - Delete event by index
- `GET /api/events/search?keyword={kw}` - Search events
- `POST /api/events/undo` - Undo last delete

## Technologies Used

- Java (backend, data structures, HTTP server)
- HTML, CSS, JavaScript (frontend)
- [AOS](https://michalsnik.github.io/aos/) for animations
- [Font Awesome](https://fontawesome.com/) for icons

## Credits

- Design and development by your team.
- Inspired by modern productivity tools.
