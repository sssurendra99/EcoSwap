/**
 * FAQ Page JavaScript
 * Handles FAQ accordion functionality
 */

function toggleFAQ(element) {
    const answer = element.nextElementSibling;
    const allQuestions = document.querySelectorAll('.faq-question');
    const allAnswers = document.querySelectorAll('.faq-answer');

    // Close all other FAQs
    allQuestions.forEach(q => {
        if (q !== element) {
            q.classList.remove('active');
        }
    });
    allAnswers.forEach(a => {
        if (a !== answer) {
            a.classList.remove('active');
        }
    });

    // Toggle current FAQ
    element.classList.toggle('active');
    answer.classList.toggle('active');
}

// Initialize FAQ page
document.addEventListener('DOMContentLoaded', function() {
    console.log('FAQ page loaded');
});
