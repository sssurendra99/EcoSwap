/**
 * Analytics Page JavaScript
 * Handles chart rendering and data visualization
 */

document.addEventListener('DOMContentLoaded', function() {
    // Initialize all charts
    initSalesTrendChart();
    initCategoryChart();
    initOrderStatusChart();
});

/**
 * Initialize Sales Trend Chart (Line Chart)
 */
function initSalesTrendChart() {
    const canvas = document.getElementById('salesTrendChart');
    if (!canvas) return;

    const chartData = document.getElementById('chartData');
    if (!chartData) return;

    const salesTrendData = chartData.getAttribute('data-sales-trend');
    if (!salesTrendData) return;

    // Parse the data (format: "{Jan 2025=1234.50, Feb 2025=2345.67, ...}")
    const data = parseTrendData(salesTrendData);

    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {
            labels: data.labels,
            datasets: [{
                label: 'Revenue ($)',
                data: data.values,
                backgroundColor: 'rgba(17, 153, 142, 0.1)',
                borderColor: '#11998e',
                borderWidth: 3,
                fill: true,
                tension: 0.4,
                pointRadius: 5,
                pointHoverRadius: 7,
                pointBackgroundColor: '#11998e',
                pointBorderColor: '#ffffff',
                pointBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: true,
                    position: 'top'
                },
                tooltip: {
                    backgroundColor: 'rgba(31, 41, 55, 0.95)',
                    padding: 12,
                    titleFont: {
                        size: 14,
                        weight: 'bold'
                    },
                    bodyFont: {
                        size: 13
                    },
                    callbacks: {
                        label: function(context) {
                            return 'Revenue: $' + context.parsed.y.toFixed(2);
                        }
                    }
                }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: function(value) {
                            return '$' + value.toFixed(0);
                        }
                    },
                    grid: {
                        color: 'rgba(0, 0, 0, 0.05)'
                    }
                },
                x: {
                    grid: {
                        display: false
                    }
                }
            }
        }
    });
}

/**
 * Initialize Category Revenue Chart (Doughnut Chart)
 */
function initCategoryChart() {
    const canvas = document.getElementById('categoryChart');
    if (!canvas) return;

    const chartData = document.getElementById('chartData');
    if (!chartData) return;

    const categoryData = chartData.getAttribute('data-category-revenue');
    if (!categoryData) return;

    // Parse the data
    const data = parseMapData(categoryData);

    // Generate colors for each category
    const colors = generateColors(data.labels.length);

    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'doughnut',
        data: {
            labels: data.labels,
            datasets: [{
                data: data.values,
                backgroundColor: colors.background,
                borderColor: colors.border,
                borderWidth: 2,
                hoverOffset: 10
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        padding: 15,
                        font: {
                            size: 12
                        },
                        generateLabels: function(chart) {
                            const data = chart.data;
                            if (data.labels.length && data.datasets.length) {
                                return data.labels.map((label, i) => {
                                    const value = data.datasets[0].data[i];
                                    return {
                                        text: label + ': $' + value.toFixed(2),
                                        fillStyle: data.datasets[0].backgroundColor[i],
                                        hidden: false,
                                        index: i
                                    };
                                });
                            }
                            return [];
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(31, 41, 55, 0.95)',
                    padding: 12,
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return label + ': $' + value.toFixed(2) + ' (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Initialize Order Status Chart (Pie Chart)
 */
function initOrderStatusChart() {
    const canvas = document.getElementById('orderStatusChart');
    if (!canvas) return;

    const chartData = document.getElementById('chartData');
    if (!chartData) return;

    const statusData = chartData.getAttribute('data-order-status');
    if (!statusData) return;

    // Parse the data
    const data = parseMapData(statusData);

    // Define colors for each status
    const statusColors = {
        'Delivered': { bg: 'rgba(5, 150, 105, 0.8)', border: '#059669' },
        'Shipped': { bg: 'rgba(59, 130, 246, 0.8)', border: '#3b82f6' },
        'Confirmed': { bg: 'rgba(139, 92, 246, 0.8)', border: '#8b5cf6' },
        'Pending': { bg: 'rgba(245, 158, 11, 0.8)', border: '#f59e0b' },
        'Cancelled': { bg: 'rgba(239, 68, 68, 0.8)', border: '#ef4444' }
    };

    const backgroundColors = data.labels.map(label =>
        statusColors[label] ? statusColors[label].bg : 'rgba(156, 163, 175, 0.8)'
    );
    const borderColors = data.labels.map(label =>
        statusColors[label] ? statusColors[label].border : '#9ca3af'
    );

    const ctx = canvas.getContext('2d');
    new Chart(ctx, {
        type: 'pie',
        data: {
            labels: data.labels,
            datasets: [{
                data: data.values,
                backgroundColor: backgroundColors,
                borderColor: borderColors,
                borderWidth: 2,
                hoverOffset: 8
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'right',
                    labels: {
                        padding: 15,
                        font: {
                            size: 12
                        },
                        generateLabels: function(chart) {
                            const data = chart.data;
                            if (data.labels.length && data.datasets.length) {
                                return data.labels.map((label, i) => {
                                    const value = data.datasets[0].data[i];
                                    return {
                                        text: label + ': ' + value,
                                        fillStyle: data.datasets[0].backgroundColor[i],
                                        hidden: false,
                                        index: i
                                    };
                                });
                            }
                            return [];
                        }
                    }
                },
                tooltip: {
                    backgroundColor: 'rgba(31, 41, 55, 0.95)',
                    padding: 12,
                    callbacks: {
                        label: function(context) {
                            const label = context.label || '';
                            const value = context.parsed || 0;
                            const total = context.dataset.data.reduce((a, b) => a + b, 0);
                            const percentage = ((value / total) * 100).toFixed(1);
                            return label + ': ' + value + ' orders (' + percentage + '%)';
                        }
                    }
                }
            }
        }
    });
}

/**
 * Parse trend data from string format
 * Input: "{Jan 2025=1234.50, Feb 2025=2345.67}"
 * Output: { labels: ['Jan 2025', 'Feb 2025'], values: [1234.50, 2345.67] }
 */
function parseTrendData(dataString) {
    const labels = [];
    const values = [];

    // Remove curly braces and split by comma
    const cleaned = dataString.replace(/[{}]/g, '').trim();
    if (!cleaned) return { labels, values };

    const pairs = cleaned.split(',');

    pairs.forEach(pair => {
        const [key, value] = pair.split('=').map(s => s.trim());
        if (key && value) {
            labels.push(key);
            values.push(parseFloat(value) || 0);
        }
    });

    return { labels, values };
}

/**
 * Parse map data from string format
 * Input: "{Category1=100.50, Category2=200.75}"
 * Output: { labels: ['Category1', 'Category2'], values: [100.50, 200.75] }
 */
function parseMapData(dataString) {
    const labels = [];
    const values = [];

    // Remove curly braces and split by comma
    const cleaned = dataString.replace(/[{}]/g, '').trim();
    if (!cleaned) return { labels, values };

    const pairs = cleaned.split(',');

    pairs.forEach(pair => {
        const [key, value] = pair.split('=').map(s => s.trim());
        if (key && value) {
            labels.push(key);
            // Parse as integer for counts, float for revenue
            const numValue = value.includes('.') ? parseFloat(value) : parseInt(value);
            values.push(isNaN(numValue) ? 0 : numValue);
        }
    });

    return { labels, values };
}

/**
 * Generate colors for charts
 */
function generateColors(count) {
    const baseColors = [
        { bg: 'rgba(17, 153, 142, 0.8)', border: '#11998e' },
        { bg: 'rgba(59, 130, 246, 0.8)', border: '#3b82f6' },
        { bg: 'rgba(245, 158, 11, 0.8)', border: '#f59e0b' },
        { bg: 'rgba(139, 92, 246, 0.8)', border: '#8b5cf6' },
        { bg: 'rgba(236, 72, 153, 0.8)', border: '#ec4899' },
        { bg: 'rgba(16, 185, 129, 0.8)', border: '#10b981' },
        { bg: 'rgba(239, 68, 68, 0.8)', border: '#ef4444' },
        { bg: 'rgba(99, 102, 241, 0.8)', border: '#6366f1' }
    ];

    const background = [];
    const border = [];

    for (let i = 0; i < count; i++) {
        const color = baseColors[i % baseColors.length];
        background.push(color.bg);
        border.push(color.border);
    }

    return { background, border };
}
