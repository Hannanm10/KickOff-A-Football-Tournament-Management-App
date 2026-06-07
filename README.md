# ⚽ KickOff – Professional Football Tournament Manager

KickOff is a modern, cloud-native Android application built with Kotlin and Firebase. It is designed to streamline the organization of football tournaments, providing real-time synchronization, automated scheduling, and advanced statistical insights.

---

## 📱 Key Features

### 🏆 Multi-Format Tournament Engine
*   **League Format:** Standard round-robin where the champion is determined by the final standings.
*   **Group + Knockout:** Dynamic group splitting followed by automated Semi-Final and Final generation.
*   **Automatic Fixture Generator:** Generates a complete round-robin schedule with a single click.

### 📊 Real-time Leaderboards & Analytics
*   **Dynamic Standings:** Instantly calculated points, GD, and ranking based on completed matches.
*   **Advanced Analytics:** Tracks winning streaks, unbeaten runs, best defense/offense, and match records.
*   **Visual Charts:** Integrated **MPAndroidChart** for points distribution, goal analysis, and tournament progress.

### 🏠 Home Dashboard
*   **Overview Cards:** Quick view of your tournaments, live events, total teams, and matches.
*   **Recent Activity:** Fast access to the 5 most recently created tournaments.
*   **Personalized Experience:** Greeting headers and role-based navigation.

### 🔐 Security & Integrity
*   **Firebase Authentication:** Secure email-based login and signup.
*   **Role-Based Access:** Only tournament organizers can add teams, edit scores, or generate fixtures.
*   **Cascading Deletes:** Atomic operations ensure that deleting a tournament removes all related teams and matches.
*   **ID-Based Architecture:** Uses unique Firebase IDs instead of names to ensure absolute data integrity.

---

## 🛠️ Tech Stack

*   **Language:** Kotlin
*   **Backend:** Firebase Authentication & Realtime Database
*   **Architecture:** Repository Pattern (Separation of Concerns)
*   **UI:** Material Design 3 Components + ConstraintLayout
*   **Visualization:** MPAndroidChart Library
*   **Data Flow:** Asynchronous Real-time Listeners (No manual refresh needed)

---

## 📂 Project Structure

```
com.example.kickoff
│
├── activities/     # Screen logic (Home, Dashboard, Analytics, Completion, etc.)
├── repositories/   # Centralized Firebase data operations
├── models/         # Data classes (User, Tournament, Team, Match, etc.)
├── adapters/       # Real-time RecyclerView adapters
└── utils/          # Logic helpers
```

---

## 🚀 Getting Started

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/your-username/kickoff.git
    ```
2.  **Firebase Setup:**
    *   Add your `google-services.json` to the `app/` directory.
    *   Enable **Email/Password** Auth in Firebase Console.
    *   Enable **Realtime Database** and apply rules from `firebase-rules.json`.
3.  **Run:** Build and deploy to an Android device (API 24+).

---

## 🎯 Completion System
Once all fixtures are completed, the app automatically:
1.  Determines the Champion and Runner-up.
2.  Locks the tournament data to prevent unauthorized changes.
3.  Generates a **Tournament Summary** featuring award winners and statistical insights.

---

## 👤 Author
**Hannan Mushtaq**

---

## 📜 License
This project is for academic purposes.
