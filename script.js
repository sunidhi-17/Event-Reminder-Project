// Global variables and state management
let events = [];
let currentFilter = 'all';
let isLoading = true;

// DOM element references
const elements = {
    navbar: document.getElementById('navbar'),
    navMenu: document.getElementById('nav-menu'),
    navToggle: document.getElementById('nav-toggle'),
    eventsGrid: document.getElementById('eventsGrid'),
    noEvents: document.getElementById('noEvents'),
    eventModal: document.getElementById('eventModal'),
    eventForm: document.getElementById('eventForm'),
    searchInput: document.getElementById('searchInput'),
    searchClear: document.getElementById('searchClear'),
    filterTabs: document.querySelectorAll('.filter-tab'),
    toastContainer: document.getElementById('toastContainer'),
    loadingOverlay: document.getElementById('loadingOverlay'),
    
    // Stats
    totalEvents: document.getElementById('totalEvents'),
    completedEvents: document.getElementById('completedEvents'),
    pendingEvents: document.getElementById('pendingEvents'),
    upcomingEvents: document.getElementById('upcomingEvents')
};

// Initialize application
document.addEventListener('DOMContentLoaded', async () => {
    console.log('DOM loaded, initializing app...');
    initializeApp();
});

async function initializeApp() {
    try {
        console.log('Initializing app...');
        
        // Initialize AOS (Animate On Scroll)
        if (typeof AOS !== 'undefined') {
            AOS.init({
                duration: 800,
                once: true,
                offset: 100
            });
        }

        // Setup event listeners
        setupEventListeners();
        
        // Create particles
        createParticles();
        
        // Load events with timeout
        await loadEventsWithTimeout();
        
        // Hide loading overlay
        hideLoading();
        
        // Show welcome message
        setTimeout(() => {
            showToast('Welcome to EventFlow! 🎉', 'success');
        }, 500);
        
    } catch (error) {
        console.error('Failed to initialize app:', error);
        hideLoading();
        showToast('Application loaded with demo data', 'warning');
        loadDemoData();
    }
}

// FIXED: Loading with timeout and fallback
async function loadEventsWithTimeout() {
    const timeout = new Promise((_, reject) => 
        setTimeout(() => reject(new Error('Loading timeout')), 3000)
    );
    
    try {
        await Promise.race([loadEvents(), timeout]);
    } catch (error) {
        console.log('Loading from server failed, using demo data');
        loadDemoData();
    }
}

function loadDemoData() {
    events = getDemoEvents();
    renderEvents();
    updateStats();
    console.log('Demo data loaded:', events.length, 'events');
}

// FIXED: Enhanced event listeners setup
function setupEventListeners() {
    console.log('Setting up event listeners...');
    
    // Navigation
    if (elements.navToggle) {
        elements.navToggle.addEventListener('click', toggleMobileMenu);
    }
    
    // Navbar scroll effect
    window.addEventListener('scroll', handleNavbarScroll);
    
    // Navigation links
    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            const targetId = link.getAttribute('href').substring(1);
            scrollToSection(targetId);
            
            // Update active nav link
            document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
            link.classList.add('active');
            
            // Close mobile menu
            if (elements.navMenu) elements.navMenu.classList.remove('active');
            if (elements.navToggle) elements.navToggle.classList.remove('active');
        });
    });

    // Form submission
    if (elements.eventForm) {
        elements.eventForm.addEventListener('submit', handleEventSubmit);
    }
    
    // Search functionality
    if (elements.searchInput) {
        elements.searchInput.addEventListener('input', handleSearch);
        elements.searchInput.addEventListener('focus', () => {
            if (elements.searchClear) {
                elements.searchClear.style.display = elements.searchInput.value ? 'block' : 'none';
            }
        });
    }
    
    if (elements.searchClear) {
        elements.searchClear.addEventListener('click', () => {
            elements.searchInput.value = '';
            elements.searchClear.style.display = 'none';
            handleSearch();
            elements.searchInput.focus();
        });
    }

    // Filter tabs
    if (elements.filterTabs) {
        elements.filterTabs.forEach(tab => {
            tab.addEventListener('click', () => handleFilterChange(tab));
        });
    }

    // Modal close handlers
    if (elements.eventModal) {
        elements.eventModal.addEventListener('click', (e) => {
            if (e.target === elements.eventModal) {
                closeEventModal();
            }
        });
    }

    // Prevent modal close when clicking inside modal
    const modalContainer = document.querySelector('.modal-container');
    if (modalContainer) {
        modalContainer.addEventListener('click', (e) => {
            e.stopPropagation();
        });
    }

    // Keyboard shortcuts
    document.addEventListener('keydown', handleKeyboardShortcuts);

    // Enhanced form validation
    setupFormValidation();
    
    console.log('Event listeners setup complete');
}

function createParticles() {
    const particlesContainer = document.getElementById('particles');
    if (!particlesContainer) return;
    
    const particleCount = 50;

    for (let i = 0; i < particleCount; i++) {
        const particle = document.createElement('div');
        particle.className = 'particle';
        particle.style.left = Math.random() * 100 + '%';
        particle.style.top = Math.random() * 100 + '%';
        particle.style.animationDelay = Math.random() * 6 + 's';
        particle.style.animationDuration = (3 + Math.random() * 3) + 's';
        particlesContainer.appendChild(particle);
    }
}

async function loadEvents() {
    console.log('Attempting to load events from server...');
    
    try {
        const response = await fetch('/api/events', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
            },
            // Add timeout
            signal: AbortSignal.timeout(5000)
        });
        
        if (response.ok) {
            events = await response.json();
            console.log('Events loaded from server:', events.length);
        } else {
            throw new Error(`Server responded with status: ${response.status}`);
        }
    } catch (error) {
        console.log('Server not available, loading demo data');
        events = getDemoEvents();
    }
    
    renderEvents();
    updateStats();
}

function getDemoEvents() {
    return [
        {
            title: "Project Launch Meeting",
            description: "Final review and launch preparation for the new product release",
            date: "2025-08-25",
            isCompleted: false
        },
        {
            title: "Client Presentation",
            description: "Present quarterly results and future roadmap to key stakeholders",
            date: "2025-09-02",
            isCompleted: false
        },
        {
            title: "Team Building Workshop",
            description: "Interactive workshop to improve team collaboration and communication",
            date: "2025-08-22",
            isCompleted: true
        },
        {
            title: "Product Demo",
            description: "Live demonstration of new features to potential customers",
            date: "2025-09-10",
            isCompleted: false
        },
        {
            title: "Quarterly Review",
            description: "Review team performance and set goals for next quarter",
            date: "2025-08-18",
            isCompleted: true
        }
    ];
}

// FIXED: Enhanced form validation
function setupFormValidation() {
    if (!elements.eventForm) return;
    
    const inputs = elements.eventForm.querySelectorAll('input, textarea');
    
    inputs.forEach(input => {
        input.addEventListener('blur', (e) => validateField(e.target));
        input.addEventListener('input', (e) => clearFieldError(e.target));
        
        if (input.type === 'date') {
            input.addEventListener('change', function() {
                validateField(this);
            });
        }
    });
    
    // Set minimum date for date input
    const dateInput = document.getElementById('eventDate');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
        
        dateInput.addEventListener('click', function() {
            if (this.showPicker) this.showPicker();
        });
        
        dateInput.addEventListener('focus', function() {
            if (this.showPicker) this.showPicker();
        });
    }
}

function validateForm() {
    const title = document.getElementById('eventTitle');
    const description = document.getElementById('eventDescription');
    const date = document.getElementById('eventDate');
    
    if (!title || !description || !date) return false;
    
    let isValid = true;
    
    clearFieldError(title);
    clearFieldError(description);
    clearFieldError(date);
    
    if (!title.value.trim()) {
        showFieldError(title, 'Title is required');
        isValid = false;
    } else if (title.value.trim().length < 3) {
        showFieldError(title, 'Title must be at least 3 characters');
        isValid = false;
    }
    
    if (!description.value.trim()) {
        showFieldError(description, 'Description is required');
        isValid = false;
    } else if (description.value.trim().length < 10) {
        showFieldError(description, 'Description must be at least 10 characters');
        isValid = false;
    }
    
    if (!date.value) {
        showFieldError(date, 'Date is required');
        isValid = false;
    } else {
        const selectedDate = new Date(date.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        selectedDate.setHours(0, 0, 0, 0);
        
        if (selectedDate < today) {
            showFieldError(date, 'Date cannot be in the past');
            isValid = false;
        }
    }
    
    return isValid;
}

function validateField(field) {
    clearFieldError(field);
    
    if (field.hasAttribute('required') && !field.value.trim()) {
        showFieldError(field, 'This field is required');
        return false;
    }
    
    if (field.type === 'date' && field.value) {
        const selectedDate = new Date(field.value);
        const today = new Date();
        today.setHours(0, 0, 0, 0);
        selectedDate.setHours(0, 0, 0, 0);
        
        if (selectedDate < today) {
            showFieldError(field, 'Date cannot be in the past');
            return false;
        }
    }
    
    if (field.id === 'eventTitle' && field.value.trim() && field.value.trim().length < 3) {
        showFieldError(field, 'Title must be at least 3 characters');
        return false;
    }
    
    if (field.id === 'eventDescription' && field.value.trim() && field.value.trim().length < 10) {
        showFieldError(field, 'Description must be at least 10 characters');
        return false;
    }
    
    return true;
}

function showFieldError(field, message) {
    field.style.borderColor = 'var(--danger)';
    field.style.background = 'rgba(239, 68, 68, 0.1)';
    
    const wrapper = field.closest('.input-wrapper');
    const feedback = wrapper ? wrapper.nextElementSibling : field.parentNode.querySelector('.form-feedback');
    
    if (feedback && feedback.classList.contains('form-feedback')) {
        feedback.textContent = message;
        feedback.style.display = 'block';
        feedback.style.color = 'var(--danger)';
    }
}

function clearFieldError(field) {
    field.style.borderColor = '';
    field.style.background = '';
    
    const wrapper = field.closest('.input-wrapper');
    const feedback = wrapper ? wrapper.nextElementSibling : field.parentNode.querySelector('.form-feedback');
    
    if (feedback && feedback.classList.contains('form-feedback')) {
        feedback.style.display = 'none';
        feedback.textContent = '';
    }
}

async function handleEventSubmit(e) {
    e.preventDefault();
    
    console.log('Form submitted');
    
    if (!validateForm()) {
        console.log('Form validation failed');
        showToast('Please fix the errors in the form', 'error');
        return;
    }
    
    const title = document.getElementById('eventTitle').value.trim();
    const description = document.getElementById('eventDescription').value.trim();
    const date = document.getElementById('eventDate').value;
    
    const formData = {
        title: title,
        description: description,
        date: date
    };
    
    try {
        const submitButton = elements.eventForm.querySelector('button[type="submit"]');
        const originalHTML = submitButton.innerHTML;
        submitButton.innerHTML = '<span class="btn-content"><i class="fas fa-spinner fa-spin"></i> Saving...</span>';
        submitButton.disabled = true;
        
        // Try API call first
        try {
            const response = await fetch('/api/events/add', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(formData),
                signal: AbortSignal.timeout(5000)
            });
            
            if (response.ok) {
                console.log('Event saved to server');
            } else {
                throw new Error('Server error');
            }
        } catch (apiError) {
            console.log('API not available, saving locally');
        }
        
        // Add to local array regardless
        events.push({ ...formData, isCompleted: false });
        
        // Update UI
        renderEvents();
        updateStats();
        closeEventModal();
        
        // Show success message
        showToast(`Event "${formData.title}" created successfully! 🎉`, 'success');
        
        // Restore button
        submitButton.innerHTML = originalHTML;
        submitButton.disabled = false;
        
    } catch (error) {
        console.error('Error creating event:', error);
        showToast('Error creating event', 'error');
        
        // Restore button
        const submitButton = elements.eventForm.querySelector('button[type="submit"]');
        submitButton.innerHTML = '<span class="btn-content"><span class="btn-icon"><i class="fas fa-save"></i></span><span class="btn-text">Save Event</span></span><div class="btn-glow"></div>';
        submitButton.disabled = false;
    }
}

function renderEvents() {
    if (!elements.eventsGrid || !elements.noEvents) return;
    
    const filteredEvents = getFilteredEvents();
    
    if (filteredEvents.length === 0) {
        elements.eventsGrid.style.display = 'none';
        elements.noEvents.style.display = 'block';
        return;
    }
    
    elements.eventsGrid.style.display = 'grid';
    elements.noEvents.style.display = 'none';
    
    elements.eventsGrid.innerHTML = filteredEvents.map((event, index) => {
        const eventDate = new Date(event.date);
        const today = new Date();
        const isOverdue = eventDate < today && !event.isCompleted;
        const statusClass = event.isCompleted ? 'completed' : 
                           isOverdue ? 'overdue' : 'pending';
        
        const formattedDate = formatDate(eventDate);
        const originalIndex = events.findIndex(e => e === event);
        
        return `
            <div class="event-card ${statusClass}" style="animation-delay: ${index * 0.1}s">
                <div class="event-header">
                    <div>
                        <h3 class="event-title">${escapeHtml(event.title)}</h3>
                        <div class="event-date">
                            <i class="fas fa-calendar-alt"></i>
                            ${formattedDate}
                        </div>
                    </div>
                    <div class="event-status">
                        ${getEventStatusIcon(event, isOverdue)}
                    </div>
                </div>
                <p class="event-description">${escapeHtml(event.description)}</p>
                <div class="event-actions">
                    ${!event.isCompleted ? `
                        <button class="btn-icon complete" onclick="completeEvent(${originalIndex})" title="Mark as completed">
                            <span class="btn-bg"></span>
                            <i class="fas fa-check"></i>
                        </button>
                    ` : ''}
                    <button class="btn-icon delete" onclick="deleteEvent(${originalIndex})" title="Delete event">
                        <span class="btn-bg"></span>
                        <i class="fas fa-trash"></i>
                    </button>
                </div>
            </div>
        `;
    }).join('');
}

function getEventStatusIcon(event, isOverdue) {
    if (event.isCompleted) {
        return '<i class="fas fa-check-circle" style="color: var(--success); font-size: 1.5rem;" title="Completed"></i>';
    } else if (isOverdue) {
        return '<i class="fas fa-exclamation-triangle" style="color: var(--danger); font-size: 1.5rem;" title="Overdue"></i>';
    } else {
        return '<i class="fas fa-clock" style="color: var(--warning); font-size: 1.5rem;" title="Pending"></i>';
    }
}

function getFilteredEvents() {
    let filtered = [...events];
    
    // Apply search filter
    if (elements.searchInput) {
        const searchTerm = elements.searchInput.value.toLowerCase().trim();
        if (searchTerm) {
            filtered = filtered.filter(event => 
                event.title.toLowerCase().includes(searchTerm) ||
                event.description.toLowerCase().includes(searchTerm)
            );
        }
    }
    
    // Apply tab filter
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    switch (currentFilter) {
        case 'completed':
            filtered = filtered.filter(event => event.isCompleted);
            break;
        case 'pending':
            filtered = filtered.filter(event => !event.isCompleted);
            break;
        case 'upcoming':
            filtered = filtered.filter(event => {
                const eventDate = new Date(event.date);
                eventDate.setHours(0, 0, 0, 0);
                return eventDate >= today && !event.isCompleted;
            });
            break;
    }
    
    // Sort by date
    filtered.sort((a, b) => new Date(a.date) - new Date(b.date));
    
    return filtered;
}

function updateStats() {
    if (!elements.totalEvents) return;
    
    const total = events.length;
    const completed = events.filter(e => e.isCompleted).length;
    const pending = events.filter(e => !e.isCompleted).length;
    const today = new Date();
    const upcoming = events.filter(e => {
        const eventDate = new Date(e.date);
        return eventDate >= today && !e.isCompleted;
    }).length;
    
    // Update numbers with animation
    animateValue(elements.totalEvents, parseInt(elements.totalEvents.textContent) || 0, total, 1000);
    animateValue(elements.completedEvents, parseInt(elements.completedEvents.textContent) || 0, completed, 1200);
    animateValue(elements.pendingEvents, parseInt(elements.pendingEvents.textContent) || 0, pending, 1400);
    animateValue(elements.upcomingEvents, parseInt(elements.upcomingEvents.textContent) || 0, upcoming, 1600);
}

function animateValue(element, start, end, duration) {
    if (!element) return;
    
    const startTime = performance.now();
    
    const animate = (currentTime) => {
        const elapsed = currentTime - startTime;
        const progress = Math.min(elapsed / duration, 1);
        
        const easeOutCubic = 1 - Math.pow(1 - progress, 3);
        const current = Math.round(start + (end - start) * easeOutCubic);
        
        element.textContent = current;
        
        if (progress < 1) {
            requestAnimationFrame(animate);
        }
    };
    
    requestAnimationFrame(animate);
}

async function completeEvent(index) {
    if (index === -1 || index >= events.length) return;
    
    try {
        // Try API call
        try {
            const response = await fetch('/api/events/complete', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ index: index + 1 }),
                signal: AbortSignal.timeout(5000)
            });
        } catch (apiError) {
            console.log('API not available for complete action');
        }
        
        // Update locally
        events[index].isCompleted = true;
        renderEvents();
        updateStats();
        showToast(`"${events[index].title}" marked as completed! ✅`, 'success');
        
    } catch (error) {
        console.error('Error completing event:', error);
        showToast('Error completing event', 'error');
    }
}

async function deleteEvent(index) {
    if (index === -1 || index >= events.length) return;
    
    const event = events[index];
    const confirmDelete = confirm(`Are you sure you want to delete "${event.title}"? This action cannot be undone.`);
    
    if (!confirmDelete) return;
    
    try {
        // Try API call
        try {
            const response = await fetch(`/api/events/delete?index=${index + 1}`, {
                method: 'DELETE',
                signal: AbortSignal.timeout(5000)
            });
        } catch (apiError) {
            console.log('API not available for delete action');
        }
        
        // Update locally
        const deletedEvent = events.splice(index, 1)[0];
        renderEvents();
        updateStats();
        showToast(`"${deletedEvent.title}" deleted successfully`, 'info');
        
    } catch (error) {
        console.error('Error deleting event:', error);
        showToast('Error deleting event', 'error');
    }
}

async function undoLastAction() {
    try {
        const response = await fetch('/api/events/undo', { 
            method: 'POST',
            signal: AbortSignal.timeout(5000)
        });
        
        if (response.ok) {
            const result = await response.json();
            if (result.success) {
                await loadEvents();
                showToast('Last action undone successfully! ↶', 'success');
            } else {
                showToast('Nothing to undo', 'info');
            }
        } else {
            throw new Error('Undo failed');
        }
    } catch (error) {
        showToast('Undo feature temporarily unavailable', 'error');
    }
}

function handleSearch() {
    renderEvents();
    if (elements.searchClear && elements.searchInput) {
        const searchTerm = elements.searchInput.value.trim();
        elements.searchClear.style.display = searchTerm ? 'block' : 'none';
    }
}

function handleFilterChange(clickedTab) {
    if (!elements.filterTabs) return;
    
    elements.filterTabs.forEach(tab => tab.classList.remove('active'));
    clickedTab.classList.add('active');
    
    currentFilter = clickedTab.dataset.filter;
    renderEvents();
    
    const filterMessages = {
        'all': 'Showing all events',
        'pending': 'Showing pending events',
        'completed': 'Showing completed events',
        'upcoming': 'Showing upcoming events'
    };
    
    showToast(filterMessages[currentFilter], 'info');
}

function handleKeyboardShortcuts(e) {
    if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return;
    
    switch (e.key.toLowerCase()) {
        case 'escape':
            if (elements.eventModal && elements.eventModal.classList.contains('active')) {
                closeEventModal();
            }
            break;
        case 'n':
            if (e.ctrlKey || e.metaKey) {
                e.preventDefault();
                openEventModal();
            }
            break;
        case 'f':
            if (e.ctrlKey || e.metaKey) {
                e.preventDefault();
                if (elements.searchInput) elements.searchInput.focus();
            }
            break;
        case '/':
            e.preventDefault();
            if (elements.searchInput) elements.searchInput.focus();
            break;
    }
}

// Modal functions
function openEventModal() {
    if (!elements.eventModal) return;
    
    elements.eventModal.classList.add('active');
    document.body.style.overflow = 'hidden';
    
    setTimeout(() => {
        const titleInput = document.getElementById('eventTitle');
        if (titleInput) titleInput.focus();
    }, 100);
    
    const dateInput = document.getElementById('eventDate');
    if (dateInput) {
        const today = new Date().toISOString().split('T')[0];
        dateInput.min = today;
    }
}

function closeEventModal() {
    if (!elements.eventModal || !elements.eventForm) return;
    
    elements.eventModal.classList.remove('active');
    document.body.style.overflow = '';
    elements.eventForm.reset();
    
    const inputs = elements.eventForm.querySelectorAll('input, textarea');
    inputs.forEach(field => {
        clearFieldError(field);
    });
}

// Navigation functions
function scrollToSection(sectionId) {
    const section = document.getElementById(sectionId);
    if (section && elements.navbar) {
        const navHeight = elements.navbar.offsetHeight;
        const sectionTop = section.offsetTop - navHeight;
        
        window.scrollTo({
            top: sectionTop,
            behavior: 'smooth'
        });
    }
}

function toggleMobileMenu() {
    if (elements.navMenu && elements.navToggle) {
        elements.navMenu.classList.toggle('active');
        elements.navToggle.classList.toggle('active');
    }
}

function handleNavbarScroll() {
    if (elements.navbar) {
        if (window.scrollY > 100) {
            elements.navbar.classList.add('scrolled');
        } else {
            elements.navbar.classList.remove('scrolled');
        }
    }
}

function toggleFilters() {
    const filterTabs = document.getElementById('filterTabs');
    if (filterTabs) {
        filterTabs.style.display = filterTabs.style.display === 'none' ? 'flex' : 'none';
    }
}

function playDemo() {
    showToast('Demo feature coming soon! 🎬', 'info');
}

// FIXED: Loading functions
function hideLoading() {
    console.log('Hiding loading overlay...');
    
    if (elements.loadingOverlay) {
        elements.loadingOverlay.classList.add('hidden');
        
        setTimeout(() => {
            elements.loadingOverlay.style.display = 'none';
            isLoading = false;
            console.log('Loading overlay hidden');
        }, 500);
    } else {
        console.log('Loading overlay element not found');
    }
}

// Toast notification system
function showToast(message, type = 'success', duration = 4000) {
    if (!elements.toastContainer) return;
    
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    
    const icon = getToastIcon(type);
    toast.innerHTML = `
        <div class="toast-icon">${icon}</div>
        <div class="toast-message">${message}</div>
    `;
    
    elements.toastContainer.appendChild(toast);
    
    setTimeout(() => {
        if (toast.parentNode) {
            toast.remove();
        }
    }, duration);
}

function getToastIcon(type) {
    const icons = {
        success: '<i class="fas fa-check-circle"></i>',
        error: '<i class="fas fa-exclamation-circle"></i>',
        warning: '<i class="fas fa-exclamation-triangle"></i>',
        info: '<i class="fas fa-info-circle"></i>'
    };
    return icons[type] || icons.info;
}

// Utility functions
function formatDate(date) {
    const options = { 
        weekday: 'short',
        year: 'numeric', 
        month: 'short', 
        day: 'numeric'
    };
    return date.toLocaleDateString('en-US', options);
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// Performance optimizations
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

// Optimized search with debouncing
if (elements.searchInput) {
    elements.searchInput.addEventListener('input', debounce(handleSearch, 300));
}

// Force hide loading after 2 seconds as fallback
setTimeout(() => {
    if (isLoading) {
        console.log('Force hiding loading overlay after timeout');
        hideLoading();
    }
}, 2000);

console.log('Script loaded successfully');
