# 💰 Personal Finance Manager - Gestionnaire de Finances Personnelles

![App Screenshot](https://i.imgur.com/sample-image.png)

An Android mobile application to track expenses, manage monthly budgets, analyze finances, and get personalized recommendations.

Une application mobile Android pour suivre ses dépenses, gérer son budget mensuel, analyser ses finances et obtenir des recommandations personnalisées.

## ✨ Features / Fonctionnalités

### 📊 Core Features
- 💵 Income & expense tracking by category  
  *Suivi des revenus et dépenses par catégorie*
- 📈 Data visualization (pie charts & bar graphs)  
  *Visualisation via diagrammes et histogrammes*
- 📅 Monthly and 6-month analysis  
  *Analyse mensuelle et sur 6 mois*
- 🔍 Transaction search and filtering  
  *Recherche et filtrage des transactions*

### 🤖 Smart Features
- 🧠 Budget limit recommendations (TensorFlow Lite)  
  *Recommandations de limites budgétaires (ML)*
- 🔔 Spending alerts and notifications  
  *Alertes de dépenses*
- 📤 PDF export functionality  
  *Export PDF des données*

### 🔒 Security
- 🔐 Firebase Authentication (Email + Google)  
  *Authentification sécurisée*
- 💾 Local data storage with Room DB  
  *Stockage local des données*

## 🛠️ Technologies / Technologies utilisées

| Category           | Technologies                          |
|--------------------|---------------------------------------|
| Language           | ![Java](https://img.shields.io/badge/Java-ED8B00?style=flat&logo=java&logoColor=white) |
| Architecture       | MVVM with Repository pattern          |
| Database           | ![RoomDB](https://img.shields.io/badge/Room-4285F4?style=flat&logo=google-cloud&logoColor=white) |
| Authentication     | ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=black) |
| Data Visualization | ![MPAndroidChart](https://img.shields.io/badge/MPAndroidChart-6DB33F?style=flat) |
| Machine Learning   | ![TensorFlow Lite](https://img.shields.io/badge/TensorFlow_Lite-FF6F00?style=flat&logo=tensorflow&logoColor=white) |

## 🚀 Installation & Setup

### Prerequisites / Prérequis
- Android Studio Flamingo or later
- Android SDK 33+
- Java JDK 17

### Setup Instructions / Configuration

* **Clone the repository**
   ```bash
   git clone https://github.com/votre-repo/finance-manager.git
   cd finance-manager
* **Firebase Configuration:**
Create project in Firebase Console
Add Android app and download google-services.json
Enable authentication (Email/Password + Google)
* **Run the app:**
Open in Android Studio
Build the project
Run on emulator or physical device

📂 Project Structure
finance-manager/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/personal_finance/
│   │   │   │   ├── activities/       # All Activities
│   │   │   │   ├── adapters/         # RecyclerView Adapters  
│   │   │   │   ├── models/           # Data Models
│   │   │   │   ├── viewmodels/       # ViewModels
│   │   │   │   ├── repositories/     # Data Repositories
│   │   │   │   └── utils/            # Utility classes
│   │   │   ├── res/                  # Resources
│   │   │   └── assets/               # ML Models
│   ├── build.gradle                  # App-level config
└── build.gradle                      # Project-level config