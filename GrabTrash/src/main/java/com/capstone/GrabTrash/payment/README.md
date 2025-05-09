# Payment Integration Module

This module handles payment information from the EcoTrack mobile application. It receives payment data after successful Xendit payments and stores it in Firestore for administrative access through a web interface.

## Overview

The payment integration system consists of two main components:

1. **Backend (GrabTrash)**: A Spring Boot service that receives and stores payment information
2. **Mobile App (EcoTrack)**: Sends payment details to the backend after successful payments

## Backend Components

### Model
- `Payment.java`: Entity class for storing payment information

### DTOs
- `PaymentRequestDTO.java`: Data Transfer Object for receiving payment information from the mobile app
- `PaymentResponseDTO.java`: Data Transfer Object for sending payment confirmation back to the mobile app

### Controller
- `PaymentController.java`: REST controller with endpoints for processing and retrieving payments

### Service
- `PaymentService.java`: Service class for handling payment business logic and Firestore integration

## Mobile App Components

### API
- `BackendApiService.kt`: Retrofit service interface for communicating with the backend
- `ApiConfig.kt`: Configuration class for API-related constants

### Models
- `PaymentRequest.kt`: Model class for sending payment information to the backend
- `PaymentResponse.kt`: Model class for receiving payment confirmation from the backend

### Integration
- `PaymentCallbackActivity.kt`: Updated to send payment information to the backend after successful payment

## API Endpoints

### Process Payment
- **URL**: `/api/payments`
- **Method**: POST
- **Request Body**:
  ```json
  {
    "orderId": "order_1234567890",
    "customerName": "John Doe",
    "customerEmail": "john.doe@example.com",
    "address": "123 Main St, City",
    "latitude": 14.5995,
    "longitude": 120.9842,
    "amount": 500.0,
    "tax": 50.0,
    "totalAmount": 550.0,
    "paymentMethod": "GCASH",
    "paymentReference": "xnd_123456789",
    "notes": "Pickup scheduled for tomorrow"
  }
  ```

### Get All Payments
- **URL**: `/api/payments`
- **Method**: GET

### Get Payment by ID
- **URL**: `/api/payments/{id}`
- **Method**: GET

### Get Payment by Order ID
- **URL**: `/api/payments/order/{orderId}`
- **Method**: GET

### Get Payments by Customer Email
- **URL**: `/api/payments/customer?email={email}`
- **Method**: GET

## Setup Instructions

### Backend Setup

1. Ensure the GrabTrash backend is running
2. The payment module is automatically integrated with the existing backend

### Mobile App Setup

1. Update the `ApiConfig.kt` file with your backend URL:
   ```kotlin
   const val BASE_URL = "http://your-backend-url:8080/"
   ```

2. Add the Retrofit dependency to your app's build.gradle if not already present:
   ```gradle
   implementation 'com.squareup.retrofit2:retrofit:2.9.0'
   implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
   ```

## Testing

1. Make a test payment in the mobile app
2. Check the logs to verify that the payment information is sent to the backend
3. Use the backend API endpoints to retrieve the payment information

## Customization

You can customize this payment integration according to your specific requirements:

1. Add authentication for the payment API endpoints
2. Implement additional validation for payment requests
3. Add more endpoints for specific reporting needs
4. Set up email notifications for new payments

## Troubleshooting

- If payment information is not being sent to the backend, check the mobile app logs for errors
- Verify that the backend URL is correct in the `ApiConfig.kt` file
- Ensure that the Firestore database is properly configured

## Support

For any questions or issues, please contact your development team.