// theme.js - Global Theme Manager

// 1. Check local storage immediately when any page loads
if(localStorage.getItem('theme') === 'light') {
    document.body.classList.add('light-mode');
}

// 2. The function that runs when the button is clicked
function toggleTheme() {
    document.body.classList.toggle('light-mode');
    
    if (document.body.classList.contains('light-mode')) {
        localStorage.setItem('theme', 'light');
    } else {
        localStorage.setItem('theme', 'dark');
    }
    updateThemeButtonText();
}

// 3. Update the button text and icon dynamically
function updateThemeButtonText() {
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(btn => {
        if (document.body.classList.contains('light-mode')) {
            btn.innerHTML = '<i class="fas fa-moon"></i> Dark Mode';
        } else {
            btn.innerHTML = '<i class="fas fa-sun"></i> Light Mode';
        }
    });
}

// Run this once on page load to set the correct button text
document.addEventListener('DOMContentLoaded', updateThemeButtonText);// theme.js - Global Theme Manager

// 1. Check local storage immediately when any page loads
if(localStorage.getItem('theme') === 'light') {
    document.body.classList.add('light-mode');
}

// 2. The function that runs when the button is clicked
function toggleTheme() {
    document.body.classList.toggle('light-mode');
    
    if (document.body.classList.contains('light-mode')) {
        localStorage.setItem('theme', 'light');
    } else {
        localStorage.setItem('theme', 'dark');
    }
    updateThemeButtonText();
}

// 3. Update the button text and icon dynamically
function updateThemeButtonText() {
    const btns = document.querySelectorAll('.theme-toggle-btn');
    btns.forEach(btn => {
        if (document.body.classList.contains('light-mode')) {
            btn.innerHTML = '<i class="fas fa-moon"></i> Dark Mode';
        } else {
            btn.innerHTML = '<i class="fas fa-sun"></i> Light Mode';
        }
    });
}

// Run this once on page load to set the correct button text
document.addEventListener('DOMContentLoaded', updateThemeButtonText);