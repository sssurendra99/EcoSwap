# Email Setup Guide for EcoSwap

This guide will help you configure the email notification system for sending order confirmations and receipts to customers.

## Quick Start

### Option 1: Using Gmail (Recommended for Development)

1. **Enable 2-Factor Authentication on your Gmail account**
   - Go to [Google Account Security](https://myaccount.google.com/security)
   - Enable 2-Step Verification

2. **Generate an App Password**
   - Go to [App Passwords](https://myaccount.google.com/apppasswords)
   - Select "Mail" and "Other (Custom name)"
   - Enter "EcoSwap" as the name
   - Click "Generate"
   - Copy the 16-character password

3. **Update `application.properties`**
   ```properties
   spring.mail.username=your-email@gmail.com
   spring.mail.password=xxxx xxxx xxxx xxxx  # Your 16-character app password
   app.email.from=your-email@gmail.com
   app.email.from-name=EcoSwap
   ```

### Option 2: Using Mailtrap (Recommended for Testing)

Mailtrap is a fake SMTP server for testing emails without sending real ones.

1. **Sign up at [Mailtrap](https://mailtrap.io)** (Free plan available)

2. **Get your credentials** from Mailtrap inbox settings

3. **Update `application.properties`**
   ```properties
   spring.mail.host=sandbox.smtp.mailtrap.io
   spring.mail.port=2525
   spring.mail.username=your-mailtrap-username
   spring.mail.password=your-mailtrap-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true

   app.email.from=test@ecoswap.com
   app.email.from-name=EcoSwap Test
   ```

### Option 3: Using Outlook/Hotmail

1. **Update `application.properties`**
   ```properties
   spring.mail.host=smtp-mail.outlook.com
   spring.mail.port=587
   spring.mail.username=your-email@outlook.com
   spring.mail.password=your-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true

   app.email.from=your-email@outlook.com
   app.email.from-name=EcoSwap
   ```

### Option 4: Using SendGrid (Recommended for Production)

SendGrid offers 100 free emails per day.

1. **Sign up at [SendGrid](https://sendgrid.com)**

2. **Create an API Key**
   - Go to Settings → API Keys
   - Create a new API key
   - Copy the key

3. **Update `application.properties`**
   ```properties
   spring.mail.host=smtp.sendgrid.net
   spring.mail.port=587
   spring.mail.username=apikey
   spring.mail.password=your-sendgrid-api-key
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true

   app.email.from=noreply@yourdomain.com
   app.email.from-name=EcoSwap
   ```

## Testing the Email System

1. **Start the application**
   ```bash
   ./mvnw spring-boot:run
   ```

2. **Place a test order**
   - Add items to cart
   - Proceed to checkout
   - Fill in your email address
   - Complete the order

3. **Check your inbox** for the order confirmation email

## Troubleshooting

### Error: "Authentication failed"
- **Gmail**: Make sure you're using an App Password, not your regular password
- **Gmail**: Enable "Less secure app access" (not recommended for production)
- Verify your username and password are correct

### Error: "Connection timeout"
- Check your firewall settings
- Verify the SMTP host and port are correct
- Try using port 465 (SSL) instead of 587 (TLS)

### Emails not being received
- Check your spam/junk folder
- Verify the "from" email address is valid
- Check application logs for error messages

### Gmail specific: "Less secure app access"
If you're not using 2FA and app passwords:
1. Go to https://myaccount.google.com/lesssecureapps
2. Turn ON "Allow less secure apps"
3. ⚠️ **Not recommended for production!**

## Email Templates

The system includes two email templates:

1. **Order Confirmation Email** - Sent when an order is placed
   - Contains order details, items, pricing, and shipping info
   - Beautiful HTML template with EcoSwap branding

2. **Order Status Update Email** - Sent when order status changes
   - Notifies customers of order progress
   - Includes current status and tracking info (if available)

## Production Recommendations

For production deployment:

1. **Use a dedicated email service** like:
   - SendGrid (100 free emails/day)
   - Mailgun (5,000 free emails/month)
   - Amazon SES (very cheap, pay-as-you-go)

2. **Use a custom domain email** (e.g., orders@ecoswap.com)

3. **Implement email queue** for better performance:
   - Send emails asynchronously
   - Retry failed emails
   - Monitor delivery status

4. **Add unsubscribe link** (required for marketing emails)

5. **Monitor email metrics**:
   - Delivery rate
   - Open rate
   - Bounce rate

## Environment Variables (Recommended for Production)

Instead of hardcoding credentials in `application.properties`, use environment variables:

```properties
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
app.email.from=${EMAIL_FROM}
```

Then set these as environment variables:
```bash
export EMAIL_USERNAME=your-email@gmail.com
export EMAIL_PASSWORD=your-app-password
export EMAIL_FROM=your-email@gmail.com
```

## Need Help?

If you encounter issues:
1. Check the application logs
2. Verify your SMTP settings
3. Test with Mailtrap first (it always works)
4. Make sure your email provider allows SMTP access

## Example: Complete Gmail Setup

```properties
# application.properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=ecoswap.noreply@gmail.com
spring.mail.password=abcd efgh ijkl mnop
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.properties.mail.smtp.connectiontimeout=5000
spring.mail.properties.mail.smtp.timeout=5000
spring.mail.properties.mail.smtp.writetimeout=5000

app.email.from=ecoswap.noreply@gmail.com
app.email.from-name=EcoSwap
```

---

**Note**: Always protect your email credentials. Never commit them to version control!
