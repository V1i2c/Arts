# ArtsPath

A private, fully-offline study companion for a **JAC (Jharkhand Academic Council) Class 12 Arts student**.
Four sections: **Dashboard**, **To Do**, **Plan**, and the **Errors book**. No internet permission, no accounts, no analytics — everything lives on the phone.

Built with Kotlin + Jetpack Compose + Room, targeting **Android 16 (API 36)** with edge-to-edge UI, working on any phone from Android 8 (API 26) up.

---

## The four sections

### 1. Dashboard
- Live clock and date, in sync with the device's system time (updates every second; "today" rolls over at midnight in your time zone).
- **Streak system**: consecutive days with at least one task completed or error revised. Streak survives until midnight — if yesterday was active, the streak can still be saved today.
- **Levels** (the light gamification): Fresh Page → Ink Drop (3d) → Quill (7d) → Scribe (14d) → Scholar (30d) → Top of the Class (60d).
- Today's progress ring, overdue count, error revisions today.
- **Last 30 days consistency strip** — intensity = how much was done that day.
- Totals (tasks done, errors logged, revisions, best streak) and a full **history feed** of everything you did, grouped by day.

### 2. To Do
- Tasks are always tied to a subject; group by Today (with an Overdue section), Upcoming (by date), or All (by subject); filter by subject chips.
- Optional deadline date + optional time. If you set a date but no time, the deadline is treated as **11:59 PM** (shown in 12-hour format).
- Edit any task; **delete requires confirmation**. Deleting a task never erases dashboard history — history comes from an append-only activity log, not from live tasks.

### 3. Plan
- Weekly calendar view: swipe between weeks, day strip with today highlighted and dots on days that have entries.
- Entries can be all-day or timed (start + optional end), optionally tied to a subject.
- **Reminders** (none / at start / 10 / 30 / 60 min before) delivered via WorkManager — no exact-alarm permission needed. Notification permission is requested inline the first time you set a reminder.

### 4. Errors book
- Browsed **subject → chapter → error**, exactly mirroring the JAC syllabus.
- Each error: title, short note, **screenshots** (Android Photo Picker — no storage permission), and **voice notes** (in-app AAC recorder + player).
- The in-chapter viewer is a **bounded pager**: swipe goes to the next/previous error *within the chapter* and stops at the ends — it never rolls over. Prev/Next buttons mirror this.
- "Mark as revised" bumps the revision count and feeds the streak/history.
- Chapter selector with search appears when logging an error (subject-wise, as required). Custom chapters can be added for textbooks not covered by the seed data.

## Permissions (first-run onboarding)
| Permission | Why | Runtime |
|---|---|---|
| `POST_NOTIFICATIONS` | Plan reminders | Asked during onboarding, re-askable inline when setting a reminder |
| `RECORD_AUDIO` | Voice notes in Errors book | Asked during onboarding, re-askable at the record button |

Images use the system Photo Picker (no storage permission). All media is stored in app-private storage. **The app declares no `INTERNET` permission** — it physically cannot send data anywhere.

## Syllabus catalog (JAC Class 12 Arts, session 2026-27)
Seeded subjects: History, Political Science, Geography, Economics, Sociology, Psychology, Hindi (Core), English (Core), Sanskrit, Home Science, Philosophy, Anthropology, Music, Urdu, Mathematics (optional).

Chapter lists verified against published JAC/NCERT syllabus sources for every subject except **Philosophy, Anthropology, Music and Urdu** — JAC/JCERT chapter lists for these are not published online in verifiable form, so they are seeded without chapters and you add your textbook's chapters in-app (`SyllabusCatalogTest` pins the verified counts so nothing regresses silently).

## Project layout
```
app/src/main/java/com/artspath/app/
  core/Stats.kt        pure date/streak/format logic (unit-tested, no Android imports)
  data/                Room entities, DAOs, syllabus seed, append-only Actions
  audio/               AAC recorder + shared player
  work/                reminder worker + notifier
  ui/                  theme (paper-planner palette), AppRoot/navigation,
                       onboarding, dashboard, todo, plan, errors
app/src/test/          StatsTest, SyllabusCatalogTest
.github/workflows/android.yml
```

## Building the APK

### CI (recommended)
Every push runs `.github/workflows/android.yml`: JDK 17 → Android SDK → Gradle 9.5.0 → **lint + unit tests + assembleDebug**, then uploads `app-debug.apk` as an artifact named `ArtsPath-debug-APK` (Actions tab → run → Artifacts). Test reports are uploaded when something fails.

### Locally
Android Studio (Otter 3+, which supports AGP 9) or:
```
gradle :app:assembleDebug
```
AGP 9's built-in Kotlin is used (no separate Kotlin plugin). Stack: AGP 9.3.0, KSP 2.3.6, Room 2.7.0, Compose BOM 2024.09.00, minSdk 26, target/compileSdk 36.

## Roadmap ideas
- Signed release APK in CI
- Data export/backup to a user-chosen folder
- NCERT-style chapter completion tracking on the Dashboard
