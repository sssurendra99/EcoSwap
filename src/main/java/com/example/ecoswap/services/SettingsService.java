package com.example.ecoswap.services;

import com.example.ecoswap.model.Settings;
import com.example.ecoswap.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for managing site-wide settings
 */
@Service
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    /**
     * Get the current settings (creates default if none exist)
     */
    @Transactional
    public Settings getSettings() {
        return settingsRepository.findFirstByOrderByIdAsc()
                .orElseGet(() -> {
                    Settings defaultSettings = new Settings();
                    return settingsRepository.save(defaultSettings);
                });
    }

    /**
     * Update settings
     */
    @Transactional
    public Settings updateSettings(Settings settings) {
        Settings existingSettings = getSettings();

        // Update general settings
        existingSettings.setShopName(settings.getShopName());
        existingSettings.setShopTagline(settings.getShopTagline());
        existingSettings.setShopDescription(settings.getShopDescription());
        existingSettings.setContactEmail(settings.getContactEmail());
        existingSettings.setSupportPhone(settings.getSupportPhone());
        existingSettings.setAddress(settings.getAddress());

        // Update currency settings
        existingSettings.setCurrencyCode(settings.getCurrencyCode());
        existingSettings.setCurrencySymbol(settings.getCurrencySymbol());
        existingSettings.setCurrencyPosition(settings.getCurrencyPosition());
        existingSettings.setDecimalPlaces(settings.getDecimalPlaces());

        // Update product settings
        existingSettings.setProductsPerPage(settings.getProductsPerPage());
        existingSettings.setAllowReviews(settings.getAllowReviews());
        existingSettings.setAutoApproveProducts(settings.getAutoApproveProducts());
        existingSettings.setLowStockThreshold(settings.getLowStockThreshold());

        // Update order settings
        existingSettings.setOrderPrefix(settings.getOrderPrefix());
        existingSettings.setMinimumOrderAmount(settings.getMinimumOrderAmount());
        existingSettings.setTaxRate(settings.getTaxRate());
        existingSettings.setShippingFee(settings.getShippingFee());
        existingSettings.setFreeShippingThreshold(settings.getFreeShippingThreshold());

        // Update email settings
        existingSettings.setEmailNotificationsEnabled(settings.getEmailNotificationsEnabled());
        existingSettings.setAdminNotificationEmail(settings.getAdminNotificationEmail());
        existingSettings.setSendOrderConfirmation(settings.getSendOrderConfirmation());
        existingSettings.setSendShippingNotification(settings.getSendShippingNotification());

        // Update appearance settings
        existingSettings.setPrimaryColor(settings.getPrimaryColor());
        existingSettings.setSecondaryColor(settings.getSecondaryColor());
        existingSettings.setLogoUrl(settings.getLogoUrl());
        existingSettings.setFaviconUrl(settings.getFaviconUrl());

        // Update social media
        existingSettings.setFacebookUrl(settings.getFacebookUrl());
        existingSettings.setTwitterUrl(settings.getTwitterUrl());
        existingSettings.setInstagramUrl(settings.getInstagramUrl());
        existingSettings.setLinkedinUrl(settings.getLinkedinUrl());

        // Update SEO settings
        existingSettings.setMetaTitle(settings.getMetaTitle());
        existingSettings.setMetaDescription(settings.getMetaDescription());
        existingSettings.setMetaKeywords(settings.getMetaKeywords());

        // Update maintenance
        existingSettings.setMaintenanceMode(settings.getMaintenanceMode());
        existingSettings.setMaintenanceMessage(settings.getMaintenanceMessage());

        return settingsRepository.save(existingSettings);
    }

    /**
     * Format price according to settings
     */
    public String formatPrice(Double amount) {
        Settings settings = getSettings();
        return settings.formatPrice(amount);
    }

    /**
     * Get shop name
     */
    public String getShopName() {
        return getSettings().getShopName();
    }

    /**
     * Get currency symbol
     */
    public String getCurrencySymbol() {
        return getSettings().getCurrencySymbol();
    }

    /**
     * Check if maintenance mode is enabled
     */
    public boolean isMaintenanceMode() {
        return getSettings().getMaintenanceMode();
    }
}
