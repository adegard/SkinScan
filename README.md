# SkinScan

An Android app to **map moles on the skin and track how each area evolves over time**.

Take a guided set of photos of your body, mark the moles you care about, and SkinScan
assembles a full-body skin map that is **unique per date**. Later, open an older map and
compare the same anatomical area side by side to spot what changed.

> **Disclaimer:** SkinScan is a personal tracking aid, **not a medical device**. It does
> not diagnose anything. Show any changing, bleeding, itching or newly appearing mole to
> a dermatologist.

---

## Features

- **Guided body map** – a drawn human body with a `FRONT` / `BACK` toggle places a tappable
  hotspot on every anatomical region, so you always know exactly where to point the camera.
- **Region-tied capture** – every photo is stored against an explicit region id, keeping the
  shot at a fixed, reproducible position.
- **Real camera capture** – CameraX preview + still capture with runtime permission handling.
- **Tap-to-mark moles** – mark lesions directly on the photo; positions are stored normalised
  (0..1) and are aspect-ratio aware, so they line up on any device.
- **One skin map per date** – each capture session is a timestamped snapshot you can reopen later.
- **Area-by-area comparison** – pick another date and an area; moles are matched across sessions
  by nearest position and flagged **SAME / CHANGED / NEW / GONE** using an on-device
  average-hash (`aHash`) of each mole's local patch.
- **Fully on-device** – photos live in private app storage (Room + internal files). Nothing is
  uploaded anywhere; no cloud service, account or analytics.

## How it works

1. Create a **skin map** (a session) for today.
2. Tap a body region and take a photo.
3. Mark the moles in that photo.
4. Repeat across the body; the map fills in as areas are photographed.
5. After some time, create a new map and use **Compare** to review the same areas.

Change detection is a local perceptual hash comparison:
each marked mole's local crop is reduced to a 64-bit average hash, and the Hamming distance
between the two sessions' crops gives a similarity score. Matched moles below a threshold are
reported as changed.

## Tech stack

| Layer            | Choice                                            |
|------------------|---------------------------------------------------|
| Language         | Kotlin 1.9.24                                     |
| UI               | Jetpack Compose (Material 3), Navigation Compose  |
| Camera           | CameraX 1.3.4                                     |
| Persistence      | Room 2.6.1 + SQLite, internal file storage        |
| Image loading    | Coil                                              |
| Build            | Android Gradle Plugin 8.5.2, Gradle 8.9           |
| Min / target SDK | 24 / 34                                           |

## Project structure

```
app/src/main/java/com/example/skinscan/
├── data
│   ├── db            Room database, DAOs, entities (Session, Photo, Lesion)
│   ├── model         BodyRegion catalogue, comparison models
│   ├── repository    SkinMapRepository – single entry point for the UI
│   └── storage       PhotoStore – private on-device photo files
├── di                AppContainer – tiny manual dependency container
├── ui
│   ├── bodymap       Home: body diagram + session selector
│   ├── capture       Camera capture and mole marking
│   ├── compare       Area-by-area evolution view
│   ├── session(s)    Session list and detail
│   ├── navigation    Routes and NavHost
│   └── theme         Compose theme
└── util              ImageSignature (aHash), ImageSizes, DemoPhotoFactory
```

## Building

Requirements: JDK 17 and the Android SDK (platform 34, build-tools 34+).

```bash
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.
A prebuilt debug APK is also committed under [`release/`](release).

### Install on a device

```bash
adb install -r release/SkinScan-debug-0.1.0.apk
```

## Roadmap

- Standardised capture framing guide (distance/lighting hints per region)
- Segment / auto-detect moles instead of manual marking
- Optional ABCD-feature annotations per mole (asymmetry, border, colour, diameter)
- Export a skin-map PDF report for a dermatologist visit
- Encrypted local storage and biometric lock

---

*This project is a personal tracking tool and does not provide medical advice.*
