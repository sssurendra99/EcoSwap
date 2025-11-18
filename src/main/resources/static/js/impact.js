// Impact Tracker JavaScript

document.addEventListener('DOMContentLoaded', function() {
    // Animate counter numbers on page load
    animateCounters();

    // Animate progress bars
    animateProgressBars();

    // Animate SVG progress circles
    animateProgressCircles();

    // Add confetti effect for unlocked achievements
    celebrateAchievements();
});

/**
 * Animate counter numbers from 0 to their final value
 */
function animateCounters() {
    const counters = document.querySelectorAll('.stat-value, .impact-value .value, .hero-value span');

    counters.forEach(counter => {
        const text = counter.textContent.trim();
        // Extract number from text (handle decimals and commas)
        const numberMatch = text.match(/[\d,]+\.?\d*/);

        if (numberMatch) {
            const target = parseFloat(numberMatch[0].replace(/,/g, ''));

            // Skip if not a valid number
            if (isNaN(target)) return;

            const duration = 2000; // 2 seconds
            const steps = 60;
            const increment = target / steps;
            let current = 0;
            let step = 0;

            const timer = setInterval(() => {
                step++;
                current += increment;

                if (step >= steps) {
                    current = target;
                    clearInterval(timer);
                }

                // Format number with appropriate decimal places
                let formatted;
                if (target >= 100) {
                    formatted = Math.round(current).toLocaleString();
                } else if (target >= 10) {
                    formatted = current.toFixed(1);
                } else {
                    formatted = current.toFixed(2);
                }

                // Replace only the number part, keep any suffix
                counter.textContent = text.replace(/[\d,]+\.?\d*/, formatted);
            }, duration / steps);
        }
    });
}

/**
 * Animate progress bars
 */
function animateProgressBars() {
    const progressBars = document.querySelectorAll('.bar-fill, .progress-fill');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const bar = entry.target;
                const width = bar.style.width || '0%';

                // Reset width
                bar.style.width = '0%';
                bar.style.transition = 'none';

                // Trigger reflow
                void bar.offsetWidth;

                // Animate to target width
                bar.style.transition = 'width 1.5s ease-out';
                bar.style.width = width;

                observer.unobserve(bar);
            }
        });
    }, { threshold: 0.5 });

    progressBars.forEach(bar => {
        observer.observe(bar);
    });
}

/**
 * Animate SVG progress circles
 */
function animateProgressCircles() {
    const circles = document.querySelectorAll('.progress-circle circle:nth-child(2)');

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const circle = entry.target;
                const dashArray = circle.getAttribute('stroke-dasharray');

                if (dashArray) {
                    // Reset
                    circle.style.strokeDasharray = '0 282.7';
                    circle.style.transition = 'none';

                    // Trigger reflow
                    void circle.offsetWidth;

                    // Animate
                    circle.style.transition = 'stroke-dasharray 1.5s ease-out';
                    circle.style.strokeDasharray = dashArray;

                    observer.unobserve(circle);
                }
            }
        });
    }, { threshold: 0.5 });

    circles.forEach(circle => {
        observer.observe(circle);
    });
}

/**
 * Add celebration effect for unlocked achievements
 */
function celebrateAchievements() {
    const unlockedAchievements = document.querySelectorAll('.achievement-card.unlocked');

    unlockedAchievements.forEach((achievement, index) => {
        setTimeout(() => {
            achievement.style.animation = 'achievementPop 0.6s ease-out';
        }, index * 200);
    });
}

/**
 * Download impact report (placeholder)
 */
function downloadReport() {
    // Get all impact data from the page
    const impactData = {
        totalCo2: document.querySelector('[data-co2]')?.dataset.co2 || '0',
        totalPlastic: document.querySelector('[data-plastic]')?.dataset.plastic || '0',
        timestamp: new Date().toISOString()
    };

    // Create a simple text report
    const report = `
EcoSwap Impact Report
Generated: ${new Date().toLocaleDateString()}

Environmental Impact Summary:
- CO₂ Emissions Saved: ${impactData.totalCo2} kg
- Plastic Waste Prevented: ${impactData.totalPlastic} g

Thank you for making a difference!
    `.trim();

    // Create download link
    const blob = new Blob([report], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `ecoswap-impact-${new Date().toISOString().split('T')[0]}.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
}

/**
 * Share impact on social media (placeholder)
 */
function shareImpact() {
    const co2Saved = document.querySelector('.impact-value .value')?.textContent || '0';
    const text = `I've saved ${co2Saved} kg of CO₂ emissions by shopping sustainably with EcoSwap! 🌍♻️ Join me in making a difference!`;

    // Check if Web Share API is available
    if (navigator.share) {
        navigator.share({
            title: 'My Environmental Impact',
            text: text,
            url: window.location.href
        }).catch(err => console.log('Error sharing:', err));
    } else {
        // Fallback: copy to clipboard
        navigator.clipboard.writeText(text).then(() => {
            alert('Impact message copied to clipboard! Share it on your favorite platform.');
        });
    }
}

// Add event listeners for CTA buttons if they exist
document.addEventListener('DOMContentLoaded', function() {
    const downloadBtn = document.querySelector('.btn-primary');
    const shareBtn = document.querySelector('.btn-secondary');

    if (downloadBtn && downloadBtn.textContent.includes('Download')) {
        downloadBtn.addEventListener('click', downloadReport);
    }

    if (shareBtn && shareBtn.textContent.includes('Share')) {
        shareBtn.addEventListener('click', shareImpact);
    }
});

// Add CSS animation keyframes dynamically
const style = document.createElement('style');
style.textContent = `
    @keyframes achievementPop {
        0% {
            transform: scale(0.9);
            opacity: 0;
        }
        50% {
            transform: scale(1.05);
        }
        100% {
            transform: scale(1);
            opacity: 1;
        }
    }

    @keyframes fadeInUp {
        from {
            opacity: 0;
            transform: translateY(20px);
        }
        to {
            opacity: 1;
            transform: translateY(0);
        }
    }

    .impact-card,
    .stat-box,
    .chart-container,
    .achievement-card,
    .tip-card {
        animation: fadeInUp 0.6s ease-out backwards;
    }

    .impact-card:nth-child(1) { animation-delay: 0.1s; }
    .impact-card:nth-child(2) { animation-delay: 0.2s; }
    .impact-card:nth-child(3) { animation-delay: 0.3s; }
    .impact-card:nth-child(4) { animation-delay: 0.4s; }
`;
document.head.appendChild(style);
