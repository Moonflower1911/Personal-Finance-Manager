# 💰 Personal Finance Manager 

An Android mobile application to track expenses, manage monthly budgets, analyze finances, and get personalized recommendations.

## ✨ Features 

### 📊 Core Features
- 💵 Income & expense tracking by category
- 📈 Data visualization (pie charts & bar graphs)   
- 📅 Monthly and 6-month analysis
- 🔍 Transaction search and filtering

### 🤖 Smart Features
- 🧠 Budget limit recommendations (TensorFlow Lite)
- 🔔 Spending alerts and notifications
- 📤 PDF export functionality

### 🔒 Security
- 🔐 Firebase Authentication (Email + Google)
- 💾 Local data storage with Room DB

## 🛠️ Technologies 

| Category           | Technologies                          |
|--------------------|---------------------------------------|
| Language           | ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white) |
| Architecture       | MVVM with Repository pattern          |
| Database           | ![RoomDB](https://img.shields.io/badge/Room-4285F4?style=flat&logo=google-cloud&logoColor=white) |
| Authentication     | ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black) |
| Machine Learning   | ![TensorFlow Lite](https://img.shields.io/badge/TensorFlow_Lite-FF6F00?style=flat&logo=tensorflow&logoColor=white) |

## 🚀 Installation & Setup

### Prerequisites 
- Android Studio Flamingo or later
- Android SDK 33+
- Java JDK 17

### Setup Instructions 

1. **Clone the repository**
   ```bash
   git clone https://github.com/votre-repo/finance-manager.git
   cd finance-manager
2. **Firebase Configuration:**
* Create project in Firebase Console
*  Android app and download google-services.json
* Enable authentication (Email/Password + Google)
3. **Run the app:**
* Open in Android Studio
* Build the project
* Run on emulator or physical device

## 📂 Project Structure

```text
finance-manager/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/personal_finance/
│   │   │   ├── activities/
│   │   │   ├── adapters/
│   │   │   ├── models/
│   │   │   ├── viewmodels/
│   │   │   ├── repositories/
│   │   │   └── utils/
│   │   ├── res/
│   │   └── assets/
│   ├── build.gradle
└── build.gradle
