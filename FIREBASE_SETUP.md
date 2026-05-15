# Namma Homestay Firebase Setup

## Enabled Services

Use these Firebase services for this version:

- Authentication: Email/Password provider
- Cloud Firestore: app data and user roles

Do not enable Firebase Storage for this project. The app keeps selected images as local device URI strings so the demo avoids paid Storage usage.

## Console Checklist

1. Open Firebase Console for this app.
2. Go to Authentication > Sign-in method.
3. Enable Email/Password.
4. Go to Firestore Database.
5. Create the database in test mode for development, or use the rules below for a safer classroom demo.
6. Confirm `app/google-services.json` exists in this Android project.
7. Run the app and create accounts from the sign-up screen.

## Firestore Collections Used

- `users`: Firebase Auth profile and role
- `profiles`: host homestay profile
- `menu`: daily menu items
- `inquiries`: traveler inquiry messages
- `guides`: local guide spots
- `bookings`: upcoming booking cards
- `calendar`: availability and prices

## Suggested Demo Rules

These rules require sign-in and let authenticated users read/write demo data. Tighten them later if this becomes a real public app.

```text
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```

## Run Notes

- Sign up as Host to enter the host dashboard.
- Sign up as Traveler to see the traveler search and detail flow.
- If login fails, confirm Email/Password auth is enabled and the app package in Firebase matches `com.example.namma_homestay`.
