/**
 * Aura Luxury Products Data
 */

const Products = [
    {
        id: 1,
        name: "Obsidian Tech Jacket",
        category: "Outerwear",
        price: 249.00,
        description: "Designed for the urban explorer, the Obsidian Tech Jacket combines high-performance weatherproofing with a sleek, 3D-sculpted silhouette.",
        images: [
            "https://images.unsplash.com/photo-1591047139829-d91aecb6caea?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1544022613-e87ca75a784a?q=80&w=800&auto=format&fit=crop",
            "https://images.unsplash.com/photo-1509948910842-82399d42122e?q=80&w=800&auto=format&fit=crop"
        ],
        tags: ["New Arrival", "Premium"],
        isNew: true
    },
    {
        id: 2,
        name: "Ghost Minimal Tee",
        category: "Essentials",
        price: 65.00,
        description: "A masterclass in minimalism. Crafted from premium organic cotton with a structured fit that retains its shape.",
        images: [
            "https://images.unsplash.com/photo-1521572267360-ee0c2909d518?q=80&w=800&auto=format&fit=crop"
        ],
        tags: ["Essential", "Eco-Friendly"],
        isNew: true
    },
    {
        id: 3,
        name: "Carbon Edge Boots",
        category: "Footwear",
        price: 320.00,
        description: "Engineered for durability and style. The Carbon Edge Boots feature a lightweight reinforced sole and premium leather upper.",
        images: [
            "https://images.unsplash.com/photo-1551028719-00167b16eac5?q=80&w=800&auto=format&fit=crop"
        ],
        tags: ["Leather", "Industrial"],
        isNew: true
    },
    {
        id: 4,
        name: "Raw Selvedge Denim",
        category: "Apparel",
        price: 180.00,
        description: "Japanese selvedge denim that ages uniquely with every wear. A timeless piece for the modern wardrobe.",
        images: [
            "https://images.unsplash.com/photo-1543163521-1bf539c55dd2?q=80&w=800&auto=format&fit=crop"
        ],
        tags: ["Classic", "Crafted"],
        isNew: false
    }
];

// Export for use in other scripts
if (typeof module !== 'undefined' && module.exports) {
    module.exports = Products;
} else {
    window.Products = Products;
}
