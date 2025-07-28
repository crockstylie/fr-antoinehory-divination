# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [2.2] - 2025-XX-XX <!-- Mettez la date de release ici -->

### Added
- **Localization:** Added translations for key UI strings in multiple languages:
    - French (fr)
    - Japanese (ja)
    - Arabic (ar)
    - Spanish (es)
    - Italian (it)
    - German (de)
    - Dutch (nl)
    - Russian (ru)
    - Romanian (ro)
    - Greek (el)
    - Portuguese (pt)
    - Korean (ko)
    - Chinese (Simplified) (zh-CN)
    - Hindi (hi)
    - Indonesian (in)
- **Interaction System:**
    - Defined a clear `InteractionEvent` interface and specific event objects (`ShakeEvent`, `TapEvent`, `BlowEvent`) for better event handling.
    - Implemented dedicated ViewModels for each interaction type: `ShakeDetectViewModel`, `TapDetectViewModel`, (and presumably `BlowDetectViewModel`).
    - Introduced an `InteractionDetectViewModel` to orchestrate various interaction methods (shake, tap, blow) based on user preferences.
- **Settings Screen:**
    - Improved logic for requesting microphone permission on the Settings screen.
    - Added rationale and permanently denied dialogs for microphone permission.

### Changed
- **Interaction System Refactoring:**
    - Refactored interaction detection logic into a more modular and maintainable structure with separate detector ViewModels and an orchestrator ViewModel.
    - Standardized event emission from specific detectors to the orchestrator.
- **Code Structure:**
    - Ensured `InteractionPreferences` data class is correctly located in the `data` package and imported appropriately across UI and ViewModel layers.
- **String Resources:**
    - Updated English base strings for clarity and consistency.

### Fixed
- **ViewModel Logic:**
    - Resolved issues with `InteractionEvent` and specific event object (e.g., `TapEvent`, `ShakeEvent`) resolution across different ViewModel files by ensuring proper imports and single definitions.
    - Corrected import paths for `InteractionPreferences` in `SettingsScreen.kt` and other relevant files.
    - Addressed potential cooldown conflicts and ensured specific detectors reset their state correctly via `completeInteractionProcessing()` callbacks from the orchestrator.
- **Shake Detection:**
    - Refined shake detection algorithm in `ShakeDetectViewModel` for more reliable event triggering.

## [2.1] - 2025-07-24

### Added
- `README.md` file for the project. (7c0fc59)

### Changed
- Improved Dice Roll functionality and visual colors. (575d023)
- Improvements to Rock Paper Scissors (Shifoumi). (adbbd30)

## [2.0] - 2025-07-24

### Changed
- General application improvements. (ae60a09)
- Started replacing hardcoded text with string resources. (ae60a09)
- Visuals added for the Coin Flip feature. (ae60a09)
- Added basic functionalities for other sub-applications. (aaa284e)

### Fixed
- Addressed feedback from the Play Console. (4ff206d)

## [1.2] - 2025-07-24

### Changed
- Significant UI improvements. (4f894e6)
- UI improvements and replacement of the old icon with a new one. (6b4314d)

## [1.1] - 2025-07-21

### Added
- New features including a home page with a menu. (27b60a0)
- Menu links to 4 activities including the Magic 8 Ball. (27b60a0)
- Information button added. (27b60a0)

### Changed
- Basic interface improvements (full screen and title bar removal). (79bf8b2)

## [1.0] - 2025-07-07

### Added
- Start of the Divination app development. (b387b24)

### Changed
- `.gitignore` improvements. (489bcbd)
- Git repository cleanup. (114aeb6)
