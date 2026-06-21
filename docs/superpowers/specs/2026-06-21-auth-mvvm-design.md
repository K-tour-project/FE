# Authentication MVVM Design

## Scope

Implement the initial project structure for email/password login and sign-up. This phase creates empty Kotlin files only; dependencies, package declarations, UI, networking, validation, and authentication logic are deferred.

## Architecture

Use feature-based MVVM with a Repository boundary. The UI sends user actions to a ViewModel, the ViewModel calls the repository, and observable UI state drives Compose rendering.

```text
Screen -> ViewModel -> Repository -> AuthApi
Screen <- UiState  <- ViewModel  <- response
```

The backend response is already shaped for the frontend, so this feature does not introduce Domain models or UseCases. Add those layers later only if business rules or cross-repository orchestration justify them.

## File Structure

```text
app/src/main/java/com/example/project/
|-- core/navigation/
|   |-- AppNavHost.kt
|   `-- AppRoute.kt
`-- feature/auth/
    |-- data/
    |   |-- model/
    |   |   |-- LoginRequest.kt
    |   |   |-- SignUpRequest.kt
    |   |   `-- AuthResponse.kt
    |   |-- remote/AuthApi.kt
    |   `-- repository/
    |       |-- AuthRepository.kt
    |       `-- AuthRepositoryImpl.kt
    `-- presentation/
        |-- login/
        |   |-- LoginScreen.kt
        |   |-- LoginUiState.kt
        |   `-- LoginViewModel.kt
        `-- signup/
            |-- SignUpScreen.kt
            |-- SignUpUiState.kt
            `-- SignUpViewModel.kt
```

## Responsibilities

- Screens render state and forward user actions.
- ViewModels own input, loading, success, and failure state.
- UI state files define immutable screen state.
- `AuthRepository` is the data-access boundary; its implementation delegates to `AuthApi`.
- Request and response models match the backend contract.
- Navigation files define routes and connect authentication screens.

## Error Handling and Testing

When implementation begins, ViewModels will convert repository failures into UI state without exposing transport exceptions to Composables. Unit tests will target ViewModels and repository behavior; Compose tests will verify rendering and user interactions. No tests are required for this empty-file scaffolding phase.
