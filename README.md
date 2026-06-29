# Bariatric Nepal — Android App

Patient-facing mobile app for the Bariatric Nepal WordPress plugin.
Pulls live data from your WordPress site via the built-in REST API.

---

## How to get your APK (zero coding needed)

### Step 1 — Put the project on GitHub
1. Go to **github.com → New repository** → name it `bariatric-nepal-app` → **Create**.
2. Unzip the project folder you downloaded.
3. Drag-and-drop all the files into the GitHub web interface (or use GitHub Desktop).
4. Click **Commit changes**.

### Step 2 — GitHub builds the APK for you automatically
- As soon as you push code, the **Build Debug APK** workflow runs (check the **Actions** tab).
- It takes about 5–8 minutes on first run (downloading Gradle and Android SDK).
- When it finishes you'll see a green ✅ tick.

### Step 3 — Download your APK
1. Click on the green ✅ in the **Actions** tab.
2. Scroll down to **Artifacts**.
3. Click **bariatric-nepal-debug** → download the zip → inside is `app-debug.apk`.

### Step 4 — Install on Android
- Email the `.apk` to yourself, open on your Android phone.
- If "Install from unknown sources" is blocked, go to **Settings → Apps → Special access → Install unknown apps** and allow your browser or file manager.

---

## First-time setup in the app
1. Open the app → enter your clinic's website address (e.g. `bariatricnepal.com`).
2. Tap **Continue** — the app verifies it can reach your server.
3. Log in with your phone number/email and the password your doctor set.
4. On first login you'll be asked to change your password.

---

## Features
| Screen | What it does |
|---|---|
| **Weight** | Log daily weight, see BMI, track loss vs target, progress chart |
| **Blood** | Submit blood test results, see doctor's reviewed notes |
| **Diet** | Date-wise accordion showing all assigned diet charts |
| **Meds** | Date-wise accordion showing all prescribed medications |
| **Profile** | Update name/phone/email, change profile photo, change password, logout |
| **Notifications** | All doctor messages, tappable to mark read individually or all at once |

---

## Requirements
- Your WordPress site must have the **Bariatric Nepal plugin v1.1.0+** active.
- The site needs to be reachable from the internet (not localhost).
- Android 8.0 (API 26) or newer.

---

## API Endpoints used
All under `https://yoursite.com/wp-json/bariatric-nepal/v1/`

```
POST   /login                    Patient authentication
POST   /logout
POST   /change-password
GET    /profile                  Patient profile + current stats
POST   /profile/picture          Upload new profile photo (multipart)
GET    /weight                   Weight log history
POST   /weight                   Log a new weight entry
GET    /blood-reports            All blood reports
POST   /blood-reports            Submit a new blood report
GET    /complaints               All complaints
POST   /complaints               Submit a new complaint
GET    /diet-charts              All assigned diet charts
GET    /medications              All prescribed medications
GET    /notifications            All notifications (unread count for badge)
POST   /notifications/read       Mark all notifications as read
POST   /notifications/{id}/read  Mark a single notification as read
```

Auth: Bearer token in `X-BN-Token` header, returned by `/login`.
