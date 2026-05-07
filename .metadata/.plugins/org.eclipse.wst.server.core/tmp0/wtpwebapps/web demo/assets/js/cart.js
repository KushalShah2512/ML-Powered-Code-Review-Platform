/**
 * Cart Logic System
 */

const Cart = {
    items: JSON.parse(localStorage.getItem('aura_cart')) || [],

    save() {
        localStorage.setItem('aura_cart', JSON.stringify(this.items));
        this.updateUI();
    },

    addItem(product) {
        const existing = this.items.find(item => item.id === product.id && item.size === product.size);
        if (existing) {
            existing.quantity += product.quantity;
        } else {
            this.items.push(product);
        }
        this.save();
        this.notify('Added to bag');
    },

    removeItem(id, size) {
        this.items = this.items.filter(item => !(item.id === id && item.size === size));
        this.save();
    },

    updateQuantity(id, size, delta) {
        const item = this.items.find(i => i.id === id && i.size === size);
        if (item) {
            item.quantity += delta;
            if (item.quantity <= 0) this.removeItem(id, size);
            else this.save();
        }
    },

    getSubtotal() {
        return this.items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
    },

    updateUI() {
        // Update cart count in nav
        const count = this.items.reduce((sum, item) => sum + item.quantity, 0);
        const cartBadge = document.querySelector('.fa-bag-shopping');
        if (cartBadge) {
            cartBadge.setAttribute('data-count', count);
            // Simple visual indicator if badge element doesn't exist
            let badgeEl = document.getElementById('cart-badge');
            if (count > 0) {
                if (!badgeEl) {
                    badgeEl = document.createElement('span');
                    badgeEl.id = 'cart-badge';
                    badgeEl.style = 'position:absolute; top:-5px; right:-5px; background:var(--accent); color:black; font-size:10px; padding:2px 5px; border-radius:10px; font-weight:bold;';
                    document.querySelector('.fa-bag-shopping').parentElement.style.position = 'relative';
                    document.querySelector('.fa-bag-shopping').parentElement.appendChild(badgeEl);
                }
                badgeEl.innerText = count;
            } else if (badgeEl) {
                badgeEl.remove();
            }
        }

        // Specific logic for cart.html
        const cartList = document.querySelector('.cart-items');
        if (cartList && window.location.pathname.includes('cart.html')) {
            this.renderCartPage();
        }
    },

    renderCartPage() {
        const cartList = document.querySelector('.cart-items');
        if (!cartList) return;

        // Keep the header
        const header = cartList.querySelector('h1');
        cartList.innerHTML = '';
        cartList.appendChild(header);

        if (this.items.length === 0) {
            cartList.innerHTML += '<p style="margin-top: 2rem; color: var(--text-muted);">Your bag is empty.</p>';
            return;
        }

        this.items.forEach(item => {
            const itemEl = document.createElement('div');
            itemEl.className = 'cart-item fade-in';
            itemEl.innerHTML = `
                <div class="cart-item-image">
                    <img src="${item.image}" alt="${item.name}">
                </div>
                <div class="cart-item-info">
                    <p class="product-category">${item.category}</p>
                    <h3>${item.name}</h3>
                    <p>Size: ${item.size}</p>
                    <div class="cart-qty-ctrl">
                        <button class="qty-btn" onclick="Cart.updateQuantity(${item.id}, '${item.size}', -1)">-</button>
                        <span>${item.quantity}</span>
                        <button class="qty-btn" onclick="Cart.updateQuantity(${item.id}, '${item.size}', 1)">+</button>
                    </div>
                </div>
                <div class="cart-item-price">
                    <p style="font-size: 1.2rem; font-weight: 600;">$${(item.price * item.quantity).toFixed(2)}</p>
                    <button class="btn-icon" onclick="Cart.removeItem(${item.id}, '${item.size}')" style="margin-top: 1rem; color: #ff4444;"><i class="fa-solid fa-trash-can"></i></button>
                </div>
            `;
            cartList.appendChild(itemEl);
        });

        // Update Summary
        const subtotal = this.getSubtotal();
        const tax = subtotal * 0.08;
        const total = subtotal + tax;

        document.querySelector('.summary-row span:last-child').innerText = `$${subtotal.toFixed(2)}`;
        document.querySelectorAll('.summary-row span:last-child')[2].innerText = `$${tax.toFixed(2)}`;
        document.querySelector('.total-row span:last-child').innerText = `$${total.toFixed(2)}`;
    },

    notify(msg) {
        const toast = document.createElement('div');
        toast.style = 'position:fixed; bottom:20px; right:20px; background:var(--accent); color:black; padding:10px 20px; border-radius:4px; font-weight:600; z-index:2000; animation: fadeInUp 0.3s forwards;';
        toast.innerText = msg;
        document.body.appendChild(toast);
        setTimeout(() => {
            toast.style.animation = 'fadeOutDown 0.3s forwards';
            setTimeout(() => toast.remove(), 300);
        }, 2000);
    }
};

// Global Exposure for onclick handlers
window.Cart = Cart;

// Init
document.addEventListener('DOMContentLoaded', () => Cart.updateUI());
