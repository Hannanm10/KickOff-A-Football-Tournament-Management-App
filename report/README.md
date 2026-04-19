# ⚽ KickOff – A Football Tournament Manager

KickOff is a Kotlin-based Android application designed to simplify football tournament management. It allows users to create tournaments, manage teams, record match results, and view real-time leaderboards — all in a clean, modern interface.

---

## 📱 Features

### 🏆 Tournament Management

* Create, edit, and delete tournaments
* Each tournament acts as a separate container

### 👥 Team Management

* Add teams to tournaments
* Upload custom team logos from device gallery

### ⚽ Match Management

* Record match scores and dates
* Prevents invalid matches (same team vs same team)
* Dynamic team selection per tournament

### 📊 Leaderboard System

* Real-time standings calculation
* Ranking logic:

    * Points (Win = 3, Draw = 1, Loss = 0)
    * Goal Difference
    * Goals Scored

### 🔐 Role-Based Access

* **Organizer:** Full control (CRUD operations)
* **Viewer:** Read-only access

### 🔑 Authentication System

* Login & Signup
* Auto-login for returning users
* Secure logout with session clearing

---

## 🧠 Key Highlights

* 🔄 **Dynamic Leaderboard** (no redundant storage)
* 🔗 **Cascading Data Integrity**

    * Renaming teams updates matches automatically
    * Deleting tournaments removes related data
* 💾 **JSON-Based Storage**

    * Simulates relational database using SharedPreferences


---

## 🏗️ Tech Stack

* **Language:** Kotlin
* **Platform:** Android SDK
* **UI:** XML + Material Design Components
* **Storage:** SharedPreferences + JSON
* **Architecture:** Modular (Models, Utils, Activities, Adapters)

---

## 📂 Project Structure

```
com.example.kickoff
│
├── models/        # Data classes (User, Team, Match, etc.)
├── utils/         # Storage, JSON, session, image handling
├── activities/    # All app screens
├── adapters/      # RecyclerView adapters
└── res/           # Layouts, colors, strings
```

---

## 🚀 How to Run

1. Clone the repository:

```
git clone https://github.com/your-username/kickoff.git
```

2. Open in **Android Studio**

3. Build & Run on:

* Emulator OR
* Physical Android device

---

## 📈 Future Improvements

* Firebase / Cloud database integration
* Real-time multi-user support
* Push notifications
* Advanced analytics and stats

---

## 🎯 Learning Outcomes

This project demonstrates:

* Android UI development
* Data persistence without database
* Role-based access control
* Dynamic data computation
* Clean modular architecture

---

## 👤 Author

**Hannan Mushtaq**

---

## 📜 License

This project is for academic purposes.
