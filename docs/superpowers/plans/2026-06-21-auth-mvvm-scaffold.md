# Authentication MVVM Scaffold Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the approved empty Kotlin file structure for email/password authentication using feature-based MVVM.

**Architecture:** Keep authentication inside `feature/auth`, split data access from presentation, and keep application navigation under `core/navigation`. This scaffold contains zero-byte Kotlin files only and therefore introduces no runtime behavior or dependencies.

**Tech Stack:** Kotlin, Android, Jetpack Compose, Gradle

---

### Task 1: Create navigation scaffold

**Files:**
- Create: `app/src/main/java/com/example/project/core/navigation/AppNavHost.kt`
- Create: `app/src/main/java/com/example/project/core/navigation/AppRoute.kt`

- [ ] **Step 1: Create the navigation directory and empty files**

Create both listed files with zero bytes of content.

- [ ] **Step 2: Verify the navigation scaffold**

Run:

```powershell
Get-ChildItem app/src/main/java/com/example/project/core/navigation -File | Select-Object Name,Length
```

Expected: `AppNavHost.kt` and `AppRoute.kt` are listed with length `0`.

### Task 2: Create authentication data scaffold

**Files:**
- Create: `app/src/main/java/com/example/project/feature/auth/data/model/LoginRequest.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/data/model/SignUpRequest.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/data/model/AuthResponse.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/data/remote/AuthApi.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/data/repository/AuthRepository.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/data/repository/AuthRepositoryImpl.kt`

- [ ] **Step 1: Create the data directories and empty files**

Create all six listed files with zero bytes of content.

- [ ] **Step 2: Verify the data scaffold**

Run:

```powershell
Get-ChildItem app/src/main/java/com/example/project/feature/auth/data -Recurse -File | Select-Object FullName,Length
```

Expected: all six data files are listed with length `0`.

### Task 3: Create authentication presentation scaffold

**Files:**
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/login/LoginScreen.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/login/LoginUiState.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/login/LoginViewModel.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/signup/SignUpScreen.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/signup/SignUpUiState.kt`
- Create: `app/src/main/java/com/example/project/feature/auth/presentation/signup/SignUpViewModel.kt`

- [ ] **Step 1: Create the presentation directories and empty files**

Create all six listed files with zero bytes of content.

- [ ] **Step 2: Verify the presentation scaffold**

Run:

```powershell
Get-ChildItem app/src/main/java/com/example/project/feature/auth/presentation -Recurse -File | Select-Object FullName,Length
```

Expected: all six presentation files are listed with length `0`.

### Task 4: Verify the project scaffold

**Files:**
- Verify: `app/src/main/java/com/example/project/core/navigation/`
- Verify: `app/src/main/java/com/example/project/feature/auth/`

- [ ] **Step 1: Verify file count and content size**

Run:

```powershell
$files = Get-ChildItem app/src/main/java/com/example/project/core/navigation,app/src/main/java/com/example/project/feature/auth -Recurse -File
$files.Count
$files | Where-Object Length -ne 0
```

Expected: count is `14` and the second command produces no output.

- [ ] **Step 2: Compile the Android project**

Run:

```powershell
.\gradlew.bat assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 3: Commit the scaffold**

```powershell
git add app/src/main/java/com/example/project/core/navigation app/src/main/java/com/example/project/feature/auth docs/superpowers/plans/2026-06-21-auth-mvvm-scaffold.md
git commit -m "chore: 인증 MVVM 파일 구조 추가"
```
