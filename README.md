# AI Budgeting App

An Android budgeting application that combines traditional personal finance tools with AI-assisted features to help users manage budgets, track transactions, understand spending patterns, and simplify financial data entry.

Developed as a collaborative university Scrum project at Auckland University of Technology (AUT), the application was built using **Kotlin** and **Jetpack Compose**, with **Firebase** providing authentication and cloud data storage and the **Google Gemini API** supporting AI-powered functionality.

## Project Overview

The AI Budgeting App was designed to make personal budgeting easier by bringing budget planning, transaction tracking, reporting, receipt scanning, and AI assistance into a single mobile application.

Rather than requiring users to manually enter and categorise every transaction, the application supports multiple ways of adding financial data, including manual entry, bank CSV imports, and receipt scanning. AI-assisted functionality is used to help categorise transactions and provide additional support through an in-app chatbot.

The project was developed collaboratively using a Scrum-based workflow, with features implemented and reviewed through GitHub branches and pull requests.

## Key Features

### Budget Management

Users can create and manage:

* Weekly budgets
* Monthly budgets
* Yearly budgets
* Overall spending limits
* Category-specific budgets

Budget information can be edited or removed, while visual summaries help users compare planned spending against their actual expenses.

### Transaction Management

Transactions can be added in several ways:

* Manual transaction entry
* Receipt image capture
* Receipt image upload
* CSV bank statement import
* AI-assisted transaction categorisation

Transactions can also be searched, filtered, categorised, and organised by date.

### Receipt Scanning

Users can capture or upload receipt images when recording a transaction.

Receipt processing helps extract relevant transaction information and reduces the amount of information that needs to be entered manually.

### CSV Import

The application supports importing transaction data from compatible bank CSV files, including formats used by:

* ASB
* ANZ

Imported transactions can then be processed and categorised within the application.

### AI-Assisted Features

The application integrates the **Google Gemini API** to support intelligent features such as:

* AI-assisted transaction categorisation
* An in-app budgeting chatbot
* Contextual assistance when working with financial information

AI functionality is intended to complement the budgeting workflow rather than replace user control over financial data.

### Reports and Spending Insights

Users can review their financial activity through:

* Weekly reports
* Monthly reports
* Yearly reports
* Budget-versus-actual comparisons
* Spending trend visualisations
* Category-based charts

Reports can also be shared through Android's native sharing functionality.

### Authentication and Profiles

Firebase Authentication is used to support account functionality including:

* Registration
* Login and logout
* Password reset
* Password changes
* Profile editing
* Account deletion

User financial data is associated with the authenticated account.

### Notifications and Reminders

The application can notify users about important budgeting activity, including approaching spending thresholds.

Users can:

* Enable or disable reminders
* View previous notifications
* Mark notifications as read
* Delete individual notifications
* Clear notification history

### Shared Access

The **Add User** functionality allows another user to be given shared access, supporting collaborative management of financial information.

## Tech Stack

| Technology                  | Purpose                                |
| --------------------------- | -------------------------------------- |
| **Kotlin**                  | Primary application language           |
| **Jetpack Compose**         | Android user interface                 |
| **Android Studio**          | Development environment                |
| **Firebase Authentication** | User authentication                    |
| **Cloud Firestore**         | Persistent application data            |
| **Google Gemini API**       | AI chatbot and transaction assistance  |
| **MVVM**                    | Application architecture               |
| **StateFlow**               | Reactive UI state management           |
| **OkHttp**                  | API communication                      |
| **SharedPreferences**       | Local application preferences          |
| **Git & GitHub**            | Version control and team collaboration |

## Architecture

The application follows an **MVVM (Model-View-ViewModel)** architecture with unidirectional data flow.

```text
User Interaction
       ↓
Jetpack Compose UI
       ↓
ViewModel
       ↓
Repository
       ↓
Firebase / APIs / Local Data
       ↓
ViewModel State
       ↓
Updated UI
```

### UI Layer

Jetpack Compose screens display application state and send user interactions to the appropriate ViewModel.

### ViewModel Layer

ViewModels manage UI state, validation, and application logic while exposing state to the interface through `StateFlow`.

### Repository Layer

Repositories separate the application's data operations from its presentation logic and provide a consistent interface for accessing stored information.

### Data Sources

The application works with several data sources:

* **Firebase Authentication** for user accounts
* **Cloud Firestore** for budgets, transactions, users, and notifications
* **Google Gemini API** for AI-assisted functionality
* **SharedPreferences** for local settings and reminders
* **Local device storage** for relevant image and file handling

## Getting Started

### Prerequisites

To run the application locally, you will need:

* Android Studio
* Android SDK
* Git
* A Firebase project configured for Android
* A Google Gemini API key
* An Android device or emulator

## Installation

Clone the repository:

```bash
git clone https://github.com/rudrakshb1/AI-Budgeting-App.git
```

Navigate into the project:

```bash
cd AI-Budgeting-App
```

Open the project in **Android Studio** and allow Gradle to complete its initial sync.

## Environment Configuration

The Gemini API key should be stored locally rather than committed to the repository.

Add your configuration to `local.properties`:

```properties
sdk.dir=/path/to/your/android/sdk
GEMINI_API_KEY=your_gemini_api_key
```

Do **not** commit API keys, passwords, or other private credentials to GitHub.

A valid Firebase configuration is also required for Firebase-dependent functionality.

## Running the Application

1. Open the project in Android Studio.
2. Allow Gradle to finish syncing.
3. Configure the required Firebase and Gemini services.
4. Select an Android emulator or physical device.
5. Build and run the application.

Android API 24 or later is recommended.

## Sample Data

The repository includes sample files that can be used when testing features such as:

* Receipt image processing
* ASB transaction CSV imports
* ANZ transaction CSV imports

These files can be transferred to an Android emulator or test device and imported through the relevant application workflow.

## Development Approach

This project was developed as a collaborative **Scrum project**.

Git and GitHub were used throughout development for:

* Feature branches
* Pull requests
* Code reviews
* Integration of independently developed features
* Issue resolution
* Collaborative development

The project provided practical experience working on a shared application where authentication, UI development, persistent data, APIs, AI features, and user workflows needed to operate together as one system.

## Team & Acknowledgements

This application was developed collaboratively by an AUT student development team.

### Contributors

* [@rudrakshb1](https://github.com/rudrakshb1)
* [@jeongjena](https://github.com/jeongjena)
* [@97chopra](https://github.com/97chopra)
* [@v-spnz](https://github.com/v-spnz)

Special thanks to the AUT course staff for their guidance and support throughout the project.

## Project Context

This repository represents a completed university software development project and demonstrates experience with:

* Collaborative Agile development
* Android application development
* Mobile UI development
* Cloud database integration
* Authentication
* API integration
* AI-assisted application features
* Financial data processing
* Application architecture
* Git and GitHub collaboration
* Feature development through pull requests
