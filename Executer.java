package Event_Reminder_System;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;

// Topic 1: Java Fundamentals - Basic class structure
// Topic 3: OOP Essentials - Encapsulation, constructors
// Your original Event class (unchanged)
class Event {
    private String title;
    private String Description;
    private LocalDate date;
    private boolean isCompleted;
    
    public Event(String title, String Description, LocalDate date) {
        this.title = title;
        this.Description = Description;
        this.date = date;
        isCompleted = false;
    }
    
    public String showTitle() { return this.title; }
    public String showDescription() { return this.Description; }
    public LocalDate getDate() { return this.date; }
    public boolean getEventStatus() { return isCompleted; }
    public void setEventStatus(boolean flag) { this.isCompleted = flag; }
    
    // Topic 1: Java Fundamentals - Method overriding
    @Override
    public String toString() {
        return String.format("Event{title='%s', description='%s', date=%s, completed=%b}", 
                title, Description, date, isCompleted);
    }
    
    public String toJson() {
        return String.format("{\"title\":\"%s\",\"description\":\"%s\",\"date\":\"%s\",\"isCompleted\":%b}",
                escapeJson(title), escapeJson(Description), date.toString(), isCompleted);
    }
    
    private String escapeJson(String text) {
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }
}

// Topic 4: Advanced OOP - Abstract class
abstract class DataManager {
    protected String name;
    
    public DataManager(String name) {
        this.name = name;
    }
    
    public abstract void displayInfo();
    public abstract int getCurrentSize();
}

// Topic 4: Advanced OOP - Interface
interface EventRepository {
    void store(Event event);
    Event retrieve(int index);
    boolean remove(int index);
    int count();
}

// Topic 7: Linked List Implementation (Internal - not exposed to frontend)
class EventLinkedList extends DataManager implements EventRepository {
    
    // Topic 4: Advanced OOP - Inner class
    private class EventNode {
        Event data;
        EventNode next;
        
        EventNode(Event data) {
            this.data = data;
            this.next = null;
        }
    }
    
    private EventNode head;
    private int size;
    
    public EventLinkedList() {
        super("EventLinkedList");
        this.head = null;
        this.size = 0;
    }
    
    @Override
    public void store(Event event) {
        EventNode newNode = new EventNode(event);
        if (head == null) {
            head = newNode;
        } else {
            EventNode current = head;
            // Topic 2: Control Flow - While loop
            while (current.next != null) {
                current = current.next;
            }
            current.next = newNode;
        }
        size++;
    }
    
    @Override
    public Event retrieve(int index) {
        // Topic 5: Exception Handling
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Invalid index: " + index);
        }
        
        EventNode current = head;
        // Topic 2: Control Flow - For loop
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }
    
    @Override
    public boolean remove(int index) {
        try {
            if (index < 0 || index >= size) return false;
            
            if (index == 0) {
                head = head.next;
                size--;
                return true;
            }
            
            EventNode current = head;
            for (int i = 0; i < index - 1; i++) {
                current = current.next;
            }
            
            current.next = current.next.next;
            size--;
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public int count() { return size; }
    
    @Override
    public void displayInfo() {
        System.out.println("Internal LinkedList - Size: " + size);
    }
    
    @Override
    public int getCurrentSize() { return size; }
    
    // Topic 5: Recursion - Recursive display (internal method)
    public void displayAllRecursively() {
        displayRecursiveHelper(head);
    }
    
    private void displayRecursiveHelper(EventNode node) {
        if (node == null) return;
        displayRecursiveHelper(node.next);
    }
}

// Topic 8: Stack Implementation (Internal - for undo functionality)
class EventStack<T> extends DataManager {
    private T[] stackArray;
    private int top;
    private int maxSize;
    
    @SuppressWarnings("unchecked")
    public EventStack(int maxSize) {
        super("EventStack");
        this.maxSize = maxSize;
        this.stackArray = (T[]) new Object[maxSize];
        this.top = -1;
    }
    
    public boolean push(T item) {
        if (isFull()) return false;
        stackArray[++top] = item;
        return true;
    }
    
    public T pop() {
        if (isEmpty()) return null;
        return stackArray[top--];
    }
    
    public T peek() {
        if (isEmpty()) return null;
        return stackArray[top];
    }
    
    public boolean isEmpty() { return top == -1; }
    public boolean isFull() { return top == maxSize - 1; }
    
    @Override
    public void displayInfo() {
        System.out.println("Internal Stack - Size: " + (top + 1) + "/" + maxSize);
    }
    
    @Override
    public int getCurrentSize() { return top + 1; }
}

// Topic 8: Queue Implementation (Internal - for event processing)
class EventQueue<T> extends DataManager {
    private T[] queueArray;
    private int front, rear, size, maxSize;
    
    @SuppressWarnings("unchecked")
    public EventQueue(int maxSize) {
        super("EventQueue");
        this.maxSize = maxSize;
        this.queueArray = (T[]) new Object[maxSize];
        this.front = 0;
        this.rear = -1;
        this.size = 0;
    }
    
    public boolean enqueue(T item) {
        if (isFull()) return false;
        rear = (rear + 1) % maxSize;
        queueArray[rear] = item;
        size++;
        return true;
    }
    
    public T dequeue() {
        if (isEmpty()) return null;
        T item = queueArray[front];
        front = (front + 1) % maxSize;
        size--;
        return item;
    }
    
    public boolean isEmpty() { return size == 0; }
    public boolean isFull() { return size == maxSize; }
    
    @Override
    public void displayInfo() {
        System.out.println("Internal Queue - Size: " + size + "/" + maxSize);
    }
    
    @Override
    public int getCurrentSize() { return size; }
}

// Topic 9: Binary Tree Implementation (Internal - for date-based organization)
class EventBinaryTree extends DataManager {
    
    private class TreeNode {
        Event data;
        TreeNode left, right;
        
        TreeNode(Event data) {
            this.data = data;
            left = right = null;
        }
    }
    
    private TreeNode root;
    private int nodeCount;
    
    public EventBinaryTree() {
        super("EventBinaryTree");
        root = null;
        nodeCount = 0;
    }
    
    public void insert(Event event) {
        root = insertRecursive(root, event);
        nodeCount++;
    }
    
    // Topic 5: Recursion in tree operations
    private TreeNode insertRecursive(TreeNode root, Event event) {
        if (root == null) {
            return new TreeNode(event);
        }
        
        if (event.getDate().isBefore(root.data.getDate())) {
            root.left = insertRecursive(root.left, event);
        } else {
            root.right = insertRecursive(root.right, event);
        }
        
        return root;
    }
    
    public Event findByDate(LocalDate date) {
        return searchRecursive(root, date);
    }
    
    // Topic 5: Recursive search
    private Event searchRecursive(TreeNode node, LocalDate date) {
        if (node == null) return null;
        
        if (date.equals(node.data.getDate())) {
            return node.data;
        }
        
        if (date.isBefore(node.data.getDate())) {
            return searchRecursive(node.left, date);
        } else {
            return searchRecursive(node.right, date);
        }
    }
    
    public List<Event> getSortedEvents() {
        List<Event> sortedList = new ArrayList<>();
        inOrderTraversal(root, sortedList);
        return sortedList;
    }
    
    // Topic 5: Recursive traversal
    private void inOrderTraversal(TreeNode node, List<Event> result) {
        if (node != null) {
            inOrderTraversal(node.left, result);
            result.add(node.data);
            inOrderTraversal(node.right, result);
        }
    }
    
    @Override
    public void displayInfo() {
        System.out.println("Internal Binary Tree - Nodes: " + nodeCount);
    }
    
    @Override
    public int getCurrentSize() { return nodeCount; }
}

// Topic 5: Custom Exception Classes (Internal)
class EventSystemException extends Exception {
    public EventSystemException(String message) { super(message); }
}

class EventNotFound extends EventSystemException {
    public EventNotFound(String message) { super(message); }
}

class StorageFullException extends EventSystemException {
    public StorageFullException(String message) { super(message); }
}

// Enhanced reminderManager with internal DSA implementations
class reminderManager implements EventRepository {
    // Topic 6: Arrays - Your original ArrayList + custom array
    ArrayList<Event> arr;
    private Event[] customArray;
    private int arrayCapacity = 100;
    private int arraySize = 0;
    
    // Internal DSA structures (not exposed to frontend)
    private EventLinkedList linkedStorage;
    private EventStack<Event> undoStack;
    private EventQueue<Event> processingQueue;
    private EventBinaryTree dateTree;
    
    public reminderManager() {
        // Original ArrayList
        arr = new ArrayList<>();
        
        // Topic 6: Arrays - Custom array
        customArray = new Event[arrayCapacity];
        
        // Internal DSA structures
        linkedStorage = new EventLinkedList();
        undoStack = new EventStack<>(50);
        processingQueue = new EventQueue<>(100);
        dateTree = new EventBinaryTree();
    }
    
    // Your original methods (unchanged)
    public void addEvent(Event e) {
        arr.add(e);
        
        // Internal DSA operations (invisible to user)
        storeInAllStructures(e);
        
        System.out.println("Event added to the list of events.!");
    }
    
    // Topic 2: Control Flow - Method to store in all internal structures
    private void storeInAllStructures(Event event) {
        // Store in custom array
        if (arraySize < arrayCapacity) {
            customArray[arraySize++] = event;
        }
        
        // Store in linked list
        linkedStorage.store(event);
        
        // Add to processing queue
        processingQueue.enqueue(event);
        
        // Insert into binary tree
        dateTree.insert(event);
    }
    
    // Topic 6: Arrays - Internal search methods
    private int linearSearch(String title) {
        // Topic 2: Control Flow - For loop
        for (int i = 0; i < arraySize; i++) {
            if (customArray[i].showTitle().equalsIgnoreCase(title)) {
                return i;
            }
        }
        return -1;
    }
    
    // Topic 6: Arrays - Binary search (internal)
    private int binarySearchByDate(LocalDate date) {
        sortArrayByDate();
        
        int left = 0, right = arraySize - 1;
        // Topic 2: Control Flow - While loop
        while (left <= right) {
            int mid = left + (right - left) / 2;
            
            LocalDate midDate = customArray[mid].getDate();
            if (midDate.equals(date)) {
                return mid;
            }
            if (midDate.isBefore(date)) {
                left = mid + 1;
            } else {
                right = mid - 1;
            }
        }
        return -1;
    }
    
    // Topic 6: Arrays - Bubble sort (internal)
    private void sortArrayByDate() {
        // Topic 2: Control Flow - Nested loops
        for (int i = 0; i < arraySize - 1; i++) {
            for (int j = 0; j < arraySize - i - 1; j++) {
                if (customArray[j].getDate().isAfter(customArray[j + 1].getDate())) {
                    // Swap
                    Event temp = customArray[j];
                    customArray[j] = customArray[j + 1];
                    customArray[j + 1] = temp;
                }
            }
        }
    }
    
    public void viewEvents() {
        if (arr.isEmpty()) {
            System.out.println("They there is no event to be listed!");
            return;
        }
        
        // Topic 2: Control Flow - Enhanced loop
        for (int i = 0; i < arr.size(); i++) {
            System.out.println((i + 1) + ". " + arr.get(i).showTitle());
            System.out.println("The Des: " + arr.get(i).showDescription());
        }
    }
    
    public void showEventAlreadyHappened() {
        if (arr.isEmpty()) {
            System.out.println("No event in the list");
            return;
        }
        
        // Topic 2: Control Flow - Enhanced for loop with conditions
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).getEventStatus()) {
                System.out.println((i + 1) + ". " + arr.get(i).showTitle());
                System.out.println(arr.get(i).showDescription());
            }
        }
    }
    
    public void updateEventStatus(int ind) {
        // Topic 5: Exception Handling
        try {
            if (ind <= 0 || ind > arr.size() || arr.isEmpty()) {
                throw new EventNotFound("Event not found at index: " + ind);
            }
            arr.get(ind - 1).setEventStatus(true);
            System.out.println("Event marked as completed!");
        } catch (EventNotFound e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    public void removeEvent(int ind) {
        try {
            if (ind <= 0 || ind > arr.size() || arr.isEmpty()) {
                throw new EventNotFound("Event not found at index: " + ind);
            }
            
            Event removedEvent = arr.get(ind - 1);
            
            // Internal: Push to undo stack
            undoStack.push(removedEvent);
            
            arr.remove(ind - 1);
            System.out.println("Event removed!");
            
        } catch (EventNotFound e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
    
    // Internal method: Undo using stack (not exposed in UI)
    public boolean undoLastDelete() {
        Event restoredEvent = undoStack.pop();
        if (restoredEvent != null) {
            arr.add(restoredEvent);
            storeInAllStructures(restoredEvent);
            return true;
        }
        return false;
    }
    
    // Internal method: Process next event using queue
    public Event processNextEvent() {
        return processingQueue.dequeue();
    }
    
    // Topic 5: Recursion - Count events recursively (internal)
    public int countEventsRecursively() {
        return countRecursiveHelper(arr, 0);
    }
    
    private int countRecursiveHelper(List<Event> events, int index) {
        if (index >= events.size()) {
            return 0;
        }
        return 1 + countRecursiveHelper(events, index + 1);
    }
    
    // Advanced filtering using internal structures
    public List<Event> getEventsByDateRange(LocalDate start, LocalDate end) {
        List<Event> result = new ArrayList<>();
        List<Event> sortedEvents = dateTree.getSortedEvents();
        
        // Topic 2: Control Flow - Enhanced for loop
        for (Event event : sortedEvents) {
            if (!event.getDate().isBefore(start) && !event.getDate().isAfter(end)) {
                result.add(event);
            }
        }
        return result;
    }
    
    // Smart search using multiple internal structures
    public List<Event> smartSearch(String keyword) {
        Set<Event> results = new HashSet<>();
        
        // Search in main array
        for (Event event : arr) {
            if (event.showTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                event.showDescription().toLowerCase().contains(keyword.toLowerCase())) {
                results.add(event);
            }
        }
        
        return new ArrayList<>(results);
    }
    
    // EventRepository interface implementation
    @Override
    public void store(Event event) { addEvent(event); }
    
    @Override
    public Event retrieve(int index) {
        try {
            return linkedStorage.retrieve(index);
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }
    
    @Override
    public boolean remove(int index) {
        if (index > 0 && index <= arr.size()) {
            removeEvent(index);
            return true;
        }
        return false;
    }
    
    @Override
    public int count() { return arr.size(); }
    
    public List<Event> getAllEvents() { return new ArrayList<>(arr); }
    
    public String getAllEventsJson() {
        if (arr.isEmpty()) return "[]";
        StringBuilder json = new StringBuilder("[");
        
        // Topic 2: Control Flow - For loop with condition
        for (int i = 0; i < arr.size(); i++) {
            json.append(arr.get(i).toJson());
            if (i < arr.size() - 1) json.append(",");
        }
        json.append("]");
        return json.toString();
    }
    
    // Internal diagnostic method
    public void printInternalStats() {
        System.out.println("=== Internal System Status ===");
        System.out.println("ArrayList size: " + arr.size());
        System.out.println("Custom array size: " + arraySize + "/" + arrayCapacity);
        linkedStorage.displayInfo();
        undoStack.displayInfo();
        processingQueue.displayInfo();
        dateTree.displayInfo();
    }
}

// HTTP Server implementation
class EventWebServer {
    private final reminderManager rm;
    
    public EventWebServer() {
        this.rm = new reminderManager();
        
        // Add some initial events
        rm.addEvent(new Event("Team Meeting", "Weekly team sync meeting", LocalDate.now().plusDays(1)));
        rm.addEvent(new Event("Project Deadline", "Submit final project report", LocalDate.now().plusDays(7)));
        rm.addEvent(new Event("Doctor Appointment", "Annual health checkup", LocalDate.now().plusDays(14)));
    }
    
    public void start() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        
        server.createContext("/", new StaticFileHandler());
        server.createContext("/api/events", new EventHandler());
        server.createContext("/api/events/add", new AddEventHandler());
        server.createContext("/api/events/complete", new CompleteEventHandler());
        server.createContext("/api/events/delete", new DeleteEventHandler());
        server.createContext("/api/events/search", new SearchEventHandler());
        server.createContext("/api/events/undo", new UndoHandler());
        
        server.setExecutor(null);
        server.start();
        
        System.out.println("🚀 Event Reminder System Started!");
        System.out.println("📱 Open your browser: http://localhost:8080");
        System.out.println("🛑 Press Ctrl+C to stop server");
    }
    
    // HTTP Handlers
    class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) path = "/index.html";
            
            try {
                byte[] content = Files.readAllBytes(Paths.get("web" + path));
                String contentType = getContentType(path);
                
                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, content.length);
                exchange.getResponseBody().write(content);
            } catch (IOException e) {
                String response = "File not found: " + path;
                exchange.sendResponseHeaders(404, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
        
        private String getContentType(String path) {
            if (path.endsWith(".html")) return "text/html";
            if (path.endsWith(".css")) return "text/css";
            if (path.endsWith(".js")) return "application/javascript";
            return "text/plain";
        }
    }
    
    class EventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                String jsonResponse = rm.getAllEventsJson();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
    
    class AddEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                String title = extractJsonValue(requestBody, "title");
                String description = extractJsonValue(requestBody, "description");
                String dateStr = extractJsonValue(requestBody, "date");
                
                LocalDate date = LocalDate.parse(dateStr);
                Event newEvent = new Event(title, description, date);
                rm.addEvent(newEvent);
                
                String response = "{\"success\": true, \"message\": \"Event added successfully\"}";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
        
        private String extractJsonValue(String json, String key) {
            String pattern = "\"" + key + "\":\"";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf("\"", start);
            return json.substring(start, end);
        }
    }
    
    class CompleteEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
            
            if ("POST".equals(exchange.getRequestMethod())) {
                String requestBody = new String(exchange.getRequestBody().readAllBytes());
                String indexStr = extractJsonValue(requestBody, "index");
                int index = Integer.parseInt(indexStr);
                
                rm.updateEventStatus(index);
                
                String response = "{\"success\": true, \"message\": \"Event completed\"}";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
        
        private String extractJsonValue(String json, String key) {
            String pattern = "\"" + key + "\":";
            int start = json.indexOf(pattern) + pattern.length();
            int end = json.indexOf(",", start);
            if (end == -1) end = json.indexOf("}", start);
            return json.substring(start, end).trim();
        }
    }
    
    class DeleteEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "DELETE");
            
            if ("DELETE".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                int index = Integer.parseInt(query.split("=")[1]);
                
                rm.removeEvent(index);
                
                String response = "{\"success\": true, \"message\": \"Event deleted\"}";
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
    
    class SearchEventHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            
            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                String keyword = java.net.URLDecoder.decode(query.split("=")[1], "UTF-8");
                
                List<Event> results = rm.smartSearch(keyword);
                StringBuilder json = new StringBuilder("[");
                
                for (int i = 0; i < results.size(); i++) {
                    json.append(results.get(i).toJson());
                    if (i < results.size() - 1) json.append(",");
                }
                json.append("]");
                
                String jsonResponse = json.toString();
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, jsonResponse.length());
                exchange.getResponseBody().write(jsonResponse.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
    
    class UndoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST");
            
            if ("POST".equals(exchange.getRequestMethod())) {
                boolean success = rm.undoLastDelete();
                
                String message = success ? "Event restored successfully" : "Nothing to undo";
                String response = String.format("{\"success\": %b, \"message\": \"%s\"}", success, message);
                
                exchange.sendResponseHeaders(200, response.length());
                exchange.getResponseBody().write(response.getBytes());
            }
            exchange.getResponseBody().close();
        }
    }
}

// Your original Executer class with enhanced console menu
public class Executer {
    public static void main(String[] args) throws IOException {
        // Start web server
        new EventWebServer().start();
        
        // Console interface
        Scanner sc = new Scanner(System.in);
        System.out.println("\n" + "=".repeat(50));
        System.out.println("Event Reminder System - Console Interface");
        System.out.println("Web interface running at: http://localhost:8080");
        System.out.println("=".repeat(50));
        System.out.println("Press Enter to access console menu...");
        sc.nextLine();
        
        reminderManager rm = new reminderManager();
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        while (true) {
            System.out.println("\n** Event Reminder Menu **");
            System.out.println("1. Add event");
            System.out.println("2. Display events");
            System.out.println("3. Show completed events");
            System.out.println("4. Remove Event");
            System.out.println("5. Undo last delete");
            System.out.println("6. Search events");
            System.out.println("7. View system stats");
            System.out.println("8. Exit");
            System.out.print("Enter your choice: ");
            
            try {
                int ch = sc.nextInt();
                sc.nextLine();
                
                // Topic 2: Control Flow - Switch statement
                switch (ch) {
                    case 1:
                        System.out.print("Enter Title: ");
                        String t = sc.nextLine();
                        System.out.print("Enter Description: ");
                        String d = sc.nextLine();
                        System.out.print("Enter Date (yyyy-MM-dd): ");
                        String date = sc.nextLine();
                        LocalDate date2 = LocalDate.parse(date, pattern);
                        rm.addEvent(new Event(t, d, date2));
                        break;
                    case 2:
                        rm.viewEvents();
                        break;
                    case 3:
                        rm.showEventAlreadyHappened();
                        break;
                    case 4:
                        rm.viewEvents();
                        System.out.print("Enter index to remove: ");
                        int ind = sc.nextInt();
                        rm.removeEvent(ind);
                        break;
                    case 5:
                        if (rm.undoLastDelete()) {
                            System.out.println("Last deleted event restored!");
                        } else {
                            System.out.println("Nothing to undo.");
                        }
                        break;
                    case 6:
                        System.out.print("Enter search keyword: ");
                        String keyword = sc.nextLine();
                        List<Event> results = rm.smartSearch(keyword);
                        if (results.isEmpty()) {
                            System.out.println("No events found.");
                        } else {
                            System.out.println("Found " + results.size() + " event(s):");
                            for (int i = 0; i < results.size(); i++) {
                                System.out.println((i + 1) + ". " + results.get(i).showTitle());
                            }
                        }
                        break;
                    case 7:
                        rm.printInternalStats();
                        break;
                    case 8:
                        sc.close();
                        System.out.println("Thank you for using Event Reminder System!");
                        System.exit(0);
                        return;
                    default:
                        System.out.println("Invalid choice! Please try again.");
                        break;
                }
            } catch (Exception e) {
                System.err.println("Invalid input: " + e.getMessage());
                sc.nextLine();
            }
        }
    }
}
