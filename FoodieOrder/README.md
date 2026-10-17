# Foodie Order – Android Application (Java)

Food ordering app for the Mobile Application Development assignment.
**Features:** Firebase Authentication | SQLite CRUD | File Handling (TXT/CSV) | Firebase Realtime Database |
Google Location Tracker | Restaurant Ordering | WebView | Sharing

## How to run
1. Unzip and open the `FoodieOrder` folder in **Android Studio** (File > Open).
2. Wait for Gradle sync to finish (internet needed the first time).
3. Run on an emulator (**use a "Google Play" / "Google APIs" system image**) or a real phone.
4. On first launch: **Sign Up** with name / email / password, then you reach the Home screen.

The app runs straight away. A placeholder `app/google-services.json` is included so it builds.
Login / Sign Up also work without Firebase (the account is stored on the phone).
To use **real** Firebase (recommended for marks), do the steps below.

## Connect your own Firebase (5 minutes)
1. Go to https://console.firebase.google.com > Add project.
2. Add an Android app with package name **`com.example.foodieorder`**.
3. Add your SHA-1 (Android Studio: Gradle panel > app > Tasks > android > signingReport, or run `gradlew signingReport`). Needed for Google Sign-In.
4. Download `google-services.json` and **replace** `app/google-services.json`.
5. Authentication > Sign-in method > enable **Email/Password** and **Google**.
6. Build > Realtime Database > Create database > start in **test mode**
   (or set rules: `{ "rules": { ".read": true, ".write": true } }`).
7. Sync Gradle and run again.

## Where each screen is (22 screens)
| # | Screen | File | How to open |
|---|--------|------|-------------|
| 1 | Splash Screen | `SplashActivity` | App start (2 sec) |
| 2 | Login / Sign Up | `LoginActivity` | After splash (Email, Google, Forgot Password) |
| 3 | Sign Up (Email) | `SignUpActivity` | "Don't have an account? Sign Up" |
| 4 | Main Screen (Home) | `MainActivity` | After login – restaurant & food spinners, image slider, menu with ADD |
| 5 | Options Menu (Toolbar) | `MainActivity` + `menu/main_menu.xml`, `menu/drawer_menu.xml` | ☰ icon opens drawer; cart icon and ⋮ overflow menu in toolbar |
| 6 | Popup Menu (More Options) | `menu/popup_menu.xml` | "MORE OPTIONS" button on Home |
| 7 | Edit Location | `EditLocationActivity` | "EDIT" next to location on Home |
| 8 | Restaurant Website (WebView) | `WebsiteActivity` | "VISIT RESTAURANT WEBSITE" button |
| 9 | My Orders | `MyOrdersActivity` | Drawer > My Orders |
| 10 | Order Options (Context Menu) | `menu/context_order_menu.xml` | **Long press** an order in My Orders |
| 11 | SQLite Database (CRUD) | `RestaurantDbActivity`, `DBHelper` | Drawer > Restaurant DB (tap a row to Update/Delete) |
| 12 | File Handling (TXT) | `TxtFileActivity` | Drawer > File Handling > TXT |
| 13 | File Handling (CSV) | `CsvFileActivity` | Drawer > File Handling > CSV |
| 14 | Firebase Authentication | `FirebaseAuthActivity` | Drawer > Firebase Data |
| 15 | Firebase Realtime DB (save) | `FirebaseSyncActivity` | Firebase screen > "SYNC WITH FIREBASE" |
| 16 | View Firebase Data | `FirebaseDataActivity` | Firebase screen > "VIEW FIREBASE DATA" (refresh icon in toolbar) |
| 17 | Google Location Tracker | `LocationTrackerActivity` | Drawer > Location Tracker (Get Current Location + Live switch) |
| 18 | Live Location on Map | `LiveMapActivity` | More Options > View on Map ("SHOW ROUTE" opens Google Maps) |
| 19 | Profile | `ProfileActivity` | Drawer > Profile (Edit Profile) |
| 20 | Share Restaurant | `AppData.share()` | Toolbar ⋮ > Share Restaurant (also "Share Order" in context menu) |
| 21 | Settings | `SettingsActivity` | Drawer > Settings (switches saved, Dark Mode works) |
| 22 | Logout | `AppData.showLogoutDialog()` | Drawer > Logout (confirmation dialog) |

## Notes
- **Orders:** Add items with ADD / Add to Cart, then tap the cart icon > Place Order, or use More Options > Buy Now.
  Orders are stored in SQLite, shown in My Orders, and also uploaded to Firebase when "Sync with Firebase" is ON in Settings.
  "Auto Backup to File" in Settings appends every new order to `orders.txt`.
- **Files** are saved in the app folder: `/storage/emulated/0/Android/data/com.example.foodieorder/files/FoodieOrder/`
  (see them in Android Studio > View > Tool Windows > Device Explorer).
- **Maps** are shown with Google Maps inside a WebView, so no Maps API key is needed.
- **Location on emulator:** open the emulator's ⋯ (Extended controls) > Location, choose a place and press "Set Location",
  then tap "Get Current Location".
- Tools used: Java, XML layouts, AppCompat/Material, SQLiteOpenHelper, SharedPreferences, Firebase Auth,
  Firebase Realtime Database, Google Sign-In, Fused Location Provider, Geocoder, WebView, Intents.
