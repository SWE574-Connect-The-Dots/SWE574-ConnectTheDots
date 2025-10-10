# Connect-The-Dots Mobile Application

This repository contains the source code for the Connect-The-Dots mobile application, built with modern Android development practices.

## Table of Contents
- [Architecture](#architecture)
- [Key Design Patterns](#key-design-patterns)
- [Development Guidelines](#development-guidelines)
- [Build and Run Instructions](#build-and-run-instructions)

## Architecture

The application is built upon the principles of **Clean Architecture**. This approach separates concerns into distinct layers, making the codebase more modular, scalable, testable, and easier to maintain. The architecture is divided into three primary layers:

1.  **`:data` Layer:** This layer is responsible for all data operations. It contains the repositories, which handle the logic for fetching data from sources like the network (via Retrofit) or a local database. It is completely independent of the UI.
2.  **`:di` (Dependency Injection) Layer:** This layer, using **Hilt**, is responsible for providing dependencies throughout the application. It decouples the creation of objects from their usage, making the code cleaner and more testable. The `NetworkModule`, for example, provides the singleton instance of Retrofit.
3.  **`:presentation` Layer:** This is the UI layer of the application. It contains all the Android Framework components, such as Activities, Composables (UI), and ViewModels. This layer follows the **Model-View-ViewModel (MVVM)** pattern.
    *   **View (Composables):** The UI is built entirely with Jetpack Compose. The screens are responsible for observing state changes from the ViewModel and emitting user events.
    *   **ViewModel:** The ViewModel acts as a bridge between the View and the data layer. It holds and manages the UI-related state, handles user events, and calls the appropriate repositories to perform business logic. It never holds a direct reference to the View.

## Key Design Patterns

To ensure a robust and scalable codebase, we follow several key design patterns:

### 1. State Management with `ViewState`

For asynchronous operations, we use a generic `ViewState` sealed interface to represent the state of the UI (`Loading`, `Success`, `Error`).

- **Lifecycle-Aware Collection:** UI state is exposed from the ViewModel using a `StateFlow` and collected in the composable screens using the `collectAsStateWithLifecycle()` function. This ensures that the UI only observes state changes when it's on the screen (`STARTED` lifecycle state), preventing wasted resources and potential crashes.

- **The `StatefulContent` Composable:** To avoid repeating `when` statements in every screen to handle these states, a reusable composable was created at `presentation/ui/utils/StatefulContent.kt`. This component accepts a `ViewState` object and uses slots to render the appropriate UI for each state. This keeps the screen-level code cleaner and focused on the success state.

  **Usage Example:**
  ```kotlin
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  StatefulContent(
      state = uiState,
      loadingContent = { CircularProgressIndicator() },
      errorContent = { message -> Text(text = message, color = Color.Red) },
      successContent = { data ->
          // Your main screen content goes here
          Text(text = data)
      }
  )
  ```

### 2. One-Time Events for Navigation

To handle events that should only be consumed once (like navigation, showing a Toast, etc.), we use a specific pattern to maintain the unidirectional data flow.
- **ViewModel:** The ViewModel exposes a `Channel` for events. When a specific action occurs (e.g., a successful login), the ViewModel sends an event to the `Channel`. Using a `Channel` ensures **guaranteed delivery** of the event, meaning the event will be buffered until the UI is ready to consume it.
- **View:** The screen collects events from this `Channel` using a reusable, lifecycle-aware composable function (`CollectAsEffect.kt`). This function ensures that events are only processed when the UI is visible and prevents stale events from being processed on configuration changes or when returning to the app.

### 3. Navigation with Nested Graphs

The app's navigation is built using Jetpack Compose Navigation and is structured into nested graphs for a clean separation of flows:
- **`RootNavGraph`:** The entry point of the app's navigation. It acts as a conditional navigator, deciding whether to show the authentication flow or the main application flow based on the user's session state.
- **`AuthNavGraph`:** Handles the pre-login flow, including the Login and Register screens.
- **`MainNavGraph`:** Handles the post-login flow, including the main features of the app accessible via the bottom navigation bar.

## Development Guidelines

### Adding a New Screen

1.  **Create the Composable:** Create a new Kotlin file for your screen in the appropriate sub-package under `presentation/ui/screens/`.
2.  **Create the ViewModel:** Create a corresponding ViewModel in `presentation/ui/viewmodel/`. Annotate it with `@HiltViewModel`.
3.  **Define the Route:** Add a new route for your screen in the `presentation/navigation/Screen.kt` sealed class. If it takes arguments, define them there as well.
4.  **Add to NavGraph:** Add the new composable to the appropriate navigation graph (`AuthNavGraph.kt` or `MainNavGraph.kt`).
5.  **Inject ViewModel:** In your new screen's composable, get an instance of your ViewModel using `hiltViewModel()`.

### Adding Localized Strings

1.  **Add to Default:** Add your new string resource to `app/src/main/res/values/strings.xml`.
2.  **Add to Other Languages:** Add the translated string to the language-specific file (e.g., `app/src/main/res/values-tr/strings.xml` for Turkish).
3.  **Use in Compose:** Use the `stringResource(id = R.string.your_string_id)` function in your composable to display the text.

## Build and Run Instructions

### Prerequisites

- Android Studio (latest stable version recommended)
- JDK 11 or higher

### Steps

1.  **Clone the Repository:**
    ```bash
    git clone https://github.com/your-username/SWE574-ConnectTheDots.git
    ```

2.  **Open in Android Studio:**
    - Launch Android Studio.
    - Select "Open an Existing Project".
    - Navigate to the cloned repository folder on your local machine and select it.
    - Android Studio will automatically sync the Gradle project. This may take a few minutes.

3.  **Run the Application:**
    - **Select a Device:** In the top toolbar of Android Studio, select an available device from the dropdown menu. You can either use a connected physical device (with developer options enabled) or create a new virtual device (Emulator).
    - **Run:** Click the green "Run 'app'" button (the triangle icon) in the toolbar, or use the shortcut `Shift + F10` (or `Ctrl + R` on Mac).
    - The Gradle build will start, and upon completion, the app will be installed and launched on your selected device/emulator.

---
This documentation should serve as a guide for anyone working on the project. Let me know if you'd like any adjustments!
