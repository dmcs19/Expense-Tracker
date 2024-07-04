# Expense Tracker

Expense Tracker is an Android application developed in Kotlin aimed at helping users manage their daily expenses efficiently. It features user authentication and data storage through Firebase, providing a seamless and secure experience. This app also includes a currency exchange feature for added convenience.

## Table of Contents

- [Introduction](#introduction)
- [Objective](#objective)
- [Technologies Used](#technologies-used)
- [Development Process](#development-process)
  - [Login and Registration System](#login-and-registration-system)
  - [Main Page](#main-page)
  - [Expenses](#expenses)
    - [Adding Expenses](#adding-expenses)
    - [Viewing Expenses](#viewing-expenses)
  - [Profile Page](#profile-page)
  - [Settings](#settings)
  - [Plans](#plans)
    - [Categories](#categories)
    - [Currency Exchange](#currency-exchange)
  - [Design - Mockups](#design---mockups)
  - [Animations](#animations)
- [Results and Conclusions](#results-and-conclusions)
- [Future Improvements](#future-improvements)

## Introduction

This report documents the development process, implementation, and results of the Expense Tracker application. The primary goal is to demonstrate the competencies acquired in the Mobile Application Development course and to showcase the practical application of theoretical knowledge in a real-world project. Additionally, we will discuss the technologies used, the challenges faced during development, and the solutions implemented to overcome them. Finally, we will present conclusions about the project's effectiveness and possibilities for future improvements.

## Objective

The objective of this project is to develop a mobile application for Android titled "Expense Tracker." This app is designed to assist users in managing their daily expenses, allowing for more rigorous and efficient control of spending. The app also features a currency exchange functionality related to its main purpose.

## Technologies Used

The development of the application was done in the Android Studio IDE using the Kotlin programming language. To store user data, we used Google's Firebase service. Firebase is a mobile and web development platform focused on providing a complete and easy-to-use backend.

In this application, we used the following Firebase services:
- Authentication
- Firestore Database

## Development Process

### Login and Registration System

User login and registration are processed through Firebase's Authentication service, requiring only an email and password. Using methods from the Firebase.auth library, users can register or log in with a Listener that checks if the process was completed successfully, deciding whether to show an error message or redirect the user to the main page.

### Main Page

The main page is the first page the user sees upon opening the app. It displays the user's main expenses and the buttons corresponding to the main functionalities and purposes of the app. Given its frequent use, animations were implemented to significantly improve user experience.

This page also introduces the use of the Firestore Database, which stores Expenses, Plans, Categories, and the currencies used for Currency Exchange defined by each user. If it is the first time a user logs in, these databases are created with default values.

The main page consists of three areas:
- **AppBar**: Displays the app name and a profile icon that redirects the user to the profile page when pressed.
- **Monthly Expense Area**: Shows the total money spent in the current month and the top three categories where the most money was spent, along with their respective values.
- **Button Panel**: Contains four buttons that redirect the user to different pages: Add Expense, View Expenses, Currency Exchange, and Profile Page.

### Expenses

#### Adding Expenses

To create a new expense, users access the Add Expense page from the main page. Here, users fill in the four required fields (value, category, date, and name) and press the "Add Expense" button. This creates a new expense in the database associated with the user's email and redirects the user back to the main page. Successful creation shows a success message, while errors show an error message.

#### Viewing Expenses

Accessed from the main page, this panel displays a table with all user expenses in chronological order. A loop iterates through all expenses in the database for a given email, storing them in a list and ordering them. The table shows all details of each expense.

### Profile Page

The profile page shows a top bar with a button to return to the main page, a profile photo icon, and the user's email address. The main area contains four buttons: Categories, Settings, About (provides information about the creator), and Logout (logs out the user and redirects to the login page).

### Settings

Similar to the profile page, this page shows the profile photo icon and the user's email. It includes two buttons related to account maintenance: "Change Password" (allows the user to change their password) and "Delete Account" (deletes all account information and expenses from the databases). Both actions require the user to confirm their identity by entering their current password. Additionally, there is a dropdown for selecting a default currency and a "Save" button to apply changes, which triggers a loop to update all expenses from the old to the new currency.

### Plans

There are two types of users in this application: premium users (with a subscription costing 1€ per month, granting access to extra features) and basic users (using the app for free with limited functionalities).

Extra features for premium users include creating new categories for better customization and checking currency exchange rates.

#### Categories

When a user logs in for the first time, a database with default categories ("Entertainment," "Food," "Home," "Transport," "Others") is created. Basic users can only assign their expenses to these five categories, while premium users can create new categories to better adapt the app to their needs.

#### Currency Exchange

This page, available only to premium users, allows checking the value of a specific amount in another currency. This functionality is useful for users traveling to a country with a different currency to better understand prices and compare them to their home country's prices.

### Design - Mockups

Mockups of the application pages are presented below.

![Mockups](image.png)

### Animations

To enhance the user experience, two animations were implemented on the main page:
- **Fade-in**: The monthly expenses section starts with 0 opacity and increases to 1 over 1 second.
- **Button Slide**: The four buttons appear on the screen, sliding up from the bottom over 1 second to sync with the fade-in animation.

## Results and Conclusions

The development of the "Expense Tracker" application yielded very positive results. The application meets all initially defined objectives, enabling efficient daily expense management for users. Tests confirmed that the app functions correctly across different Android devices, ensuring a consistent user experience.

In conclusion, the "Expense Tracker" project proved to be a useful and effective tool for personal expense management. The application not only achieved its goals but also exceeded expectations in terms of functionality and usability. The development process allowed practical application of knowledge acquired during the Mobile Application Development course, including Kotlin programming, Android Studio usage, and Firebase integration.

## Future Improvements

Future enhancements could include implementing advanced expense analysis graphs, adding filters to the expense table, and more based on user feedback.
