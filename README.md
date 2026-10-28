# Catherine's JAMB Nursing Prep 🩺 — Native Android App

Catherine's 139-day JAMB UTME study companion as a **native Android app** built with **Kotlin + Jetpack Compose** (Material 3).

> Package: `com.catherine.nursingprep` · Target SDK 34 (Android 14) · Min SDK 24 (Android 7.0)

## ✨ Features

| Tab | What it does |
|---|---|
| 🏠 **Dashboard** | Live per-second countdown to **March 20, 2027** · time-of-day greeting ("Good evening, Catherine! 🩺✨") · daily quote · syllabus mastery ring · past-question quick add (+10/+25/+50) · night-focus stats & streak · tonight's rotation focus |
| 🗺️ **Roadmap** | The full **20-week / 139-day** master plan in 5 phases — all **161 official JAMB syllabus topics** (Use of English, Biology, Chemistry, Physics) with interactive checkboxes, subject filters and the current week auto-opened |
| 🌙 **Routine** | The 10:00 PM – 12:30 AM night routine: 15-min Warm-Up → 75-min Core Reading → 45-min Past-Question Drill → 15-min Error Log, with a **session timer** (per-block modes + classic Pomodoro), completion chime, routine check-offs and recent-session history |
| 📓 **Errors** | Digital mistakes notebook — subject, question, your answer vs the correct answer, revision note · pending/mastered views · subject filters · search |

Progress (checkboxes, past-question count, sessions, routine blocks, error log) is saved on-device via **SharedPreferences** (Gson-serialized) — no account, no internet, no permissions.

## 🚀 Build the APK — GitHub Actions (no Android Studio needed)

1. Create a new **GitHub repository** (e.g. `cathcare-android`).
2. Upload this project's files to the repo (drag-and-drop the folder contents on github.com, or `git push` — see below).
3. Open the **Actions** tab → the included **Build APK** workflow (`.github/workflows/build-apk.yml`) runs automatically on push.
4. When it finishes, click the run → **Artifacts → CathCare-debug-apk** → download the APK.
5. Copy the APK to Catherine's phone → tap to install (allow "install unknown apps" if asked).

The workflow pins **JDK 17 + Gradle 8.4 + AGP 8.2.2 + Kotlin 1.9.22**, so it compiles reproducibly in the cloud.

## 🛠 Build locally

### Android Studio (recommended)
1. **File → Open** and select this folder (it will be recognized as an Android Gradle project).
2. Let Studio sync (it may offer to generate the Gradle wrapper — accept).
3. **Run ▶** on a device/emulator, or **Build → Build APK(s)**.

### Command line
Requires JDK 17 and Gradle 8.4:
```bash
gradle assembleDebug          # APK at app/build/outputs/apk/debug/app-debug.apk
gradle wrapper --gradle-version 8.4   # optional: generate the wrapper for ./gradlew
```

## 📁 Project structure

```
CathCareJambPrep/
├── .github/workflows/build-apk.yml      # one-click cloud APK build
├── settings.gradle.kts · build.gradle.kts · gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
└── app/
    ├── build.gradle.kts                 # SDK 34 / minSdk 24 / Compose
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                          # theme, colors, launcher icon
        └── java/com/catherine/nursingprep/
            ├── MainActivity.kt           # tabs + scaffold + snackbars
            ├── data/
            │   ├── Models.kt             # models + stats engine
            │   ├── CurriculumData.kt     # ⭐ the full 20-week syllabus
            │   └── StudyPreferences.kt   # SharedPreferences + Gson
            ├── viewmodel/StudyViewModel.kt  # state, timer, persistence
            └── ui/
                ├── theme/                # navy/sage/coral palette
                ├── components/           # cards, rings, pills, bars
                └── tabs/                 # Dashboard · Roadmap · Routine · Errors
```

## 📝 Notes

- **Exam date / plan start** live in `CurriculumData.kt` (`EXAM_MILLIS`, `planStart`) — everything (countdown, current week, phase) derives from them.
- To edit the curriculum, change `CurriculumData.WEEKS` — topic keys are `w{week}-{subject}-{index}`, so old checkboxes keep working when you append topics.
- Works fully offline; uninstalling clears all data.
- This is a debug build (no signing config). For Play Store, add a release signing config in `app/build.gradle.kts`.

---

Made with 💚 for Catherine — future nurse. 🩺
