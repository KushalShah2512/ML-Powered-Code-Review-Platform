/**
 * Auth & Form Validation Logic
 */

const Auth = {
    init() {
        const loginForm = document.getElementById('login-form');
        const registerForm = document.getElementById('register-form');

        if (loginForm) {
            loginForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.validateLogin(loginForm);
            });
        }

        if (registerForm) {
            registerForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.validateRegister(registerForm);
            });
        }
    },

    validateLogin(form) {
        const email = form.querySelector('input[type="email"]').value;
        const password = form.querySelector('input[type="password"]').value;

        if (password.length < 6) {
            this.showError(form, 'Password must be at least 6 characters');
            return;
        }

        // Mock Login
        console.log('Logging in...', { email });
        Cart.notify('Welcome back to Aura!');
        setTimeout(() => window.location.href = '../index.html', 1500);
    },

    validateRegister(form) {
        const name = form.querySelector('input[placeholder="John Doe"]').value;
        const email = form.querySelector('input[type="email"]').value;
        const password = form.querySelector('input[type="password"]').value;

        if (name.split(' ').length < 2) {
            this.showError(form, 'Please enter your full name');
            return;
        }

        if (password.length < 8) {
            this.showError(form, 'Security requires 8+ characters for new accounts');
            return;
        }

        // Mock Registration
        console.log('Registering...', { name, email });
        Cart.notify('Registration successful!');
        setTimeout(() => window.location.href = '../index.html', 1500);
    },

    showError(form, msg) {
        let errorEl = form.querySelector('.error-msg');
        if (!errorEl) {
            errorEl = document.createElement('div');
            errorEl.className = 'error-msg';
            errorEl.style = 'color: #ff4444; font-size: 0.8rem; margin-top: 1rem; text-align: center;';
            form.appendChild(errorEl);
        }
        errorEl.innerText = msg;
        errorEl.style.animation = 'shake 0.4s ease-in-out';
        setTimeout(() => errorEl.style.animation = '', 400);
    }
};

// Add shake animation to style
const style = document.createElement('style');
style.innerHTML = `
    @keyframes shake {
        0%, 100% { transform: translateX(0); }
        25% { transform: translateX(-5px); }
        75% { transform: translateX(5px); }
    }
    @keyframes fadeOutDown {
        from { opacity: 1; transform: translateY(0); }
        to { opacity: 0; transform: translateY(20px); }
    }
`;
document.head.appendChild(style);

document.addEventListener('DOMContentLoaded', () => Auth.init());
