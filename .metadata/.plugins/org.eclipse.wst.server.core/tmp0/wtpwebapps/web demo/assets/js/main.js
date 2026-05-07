/**
 * 3D Tilt Effect System
 * Handles perspective rotation based on mouse position.
 */

class TiltEffect {
    constructor(elements, options = {}) {
        this.elements = typeof elements === 'string' ? document.querySelectorAll(elements) : elements;
        this.settings = {
            max: options.max || 15, // Max rotation degree
            perspective: options.perspective || 1000,
            scale: options.scale || 1.05,
            speed: options.speed || 400,
            easing: options.easing || "cubic-bezier(.03,.98,.52,.99)",
            ...options
        };
        
        this.init();
    }

    init() {
        this.elements.forEach(el => {
            el.style.transition = `transform ${this.settings.speed}ms ${this.settings.easing}`;
            el.style.transformStyle = "preserve-3d";
            
            el.addEventListener('mousemove', (e) => this.onMouseMove(e, el));
            el.addEventListener('mouseleave', (e) => this.onMouseLeave(e, el));
            el.addEventListener('mouseenter', (e) => this.onMouseEnter(e, el));
        });
    }

    onMouseEnter(e, el) {
        el.style.transition = `transform ${this.settings.speed}ms ${this.settings.easing}`;
    }

    onMouseMove(e, el) {
        const rect = el.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;
        
        const centerX = rect.width / 2;
        const centerY = rect.height / 2;
        
        const percentX = (x - centerX) / centerX;
        const percentY = (y - centerY) / centerY;
        
        const rotateX = (this.settings.max * percentY).toFixed(2);
        const rotateY = (-this.settings.max * percentX).toFixed(2);
        
        el.style.transform = `perspective(${this.settings.perspective}px) rotateX(${rotateX}deg) rotateY(${rotateY}deg) scale3d(${this.settings.scale}, ${this.settings.scale}, ${this.settings.scale})`;
    }

    onMouseLeave(e, el) {
        el.style.transform = `perspective(${this.settings.perspective}px) rotateX(0deg) rotateY(0deg) scale3d(1, 1, 1)`;
    }
}

// Initialize on load
document.addEventListener('DOMContentLoaded', () => {
    new TiltEffect('.tilt-card', { max: 15, scale: 1.05 });
    
    // Navbar scroll effect
    const nav = document.querySelector('nav');
    window.addEventListener('scroll', () => {
        if (window.scrollY > 50) {
            nav.classList.add('scrolled');
        } else {
            nav.classList.remove('scrolled');
        }
    });

    // Reveal animations
    const observerOptions = {
        threshold: 0.1
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('fade-in');
            }
        });
    }, observerOptions);

    document.querySelectorAll('.animate-on-scroll').forEach(el => observer.observe(el));
});
