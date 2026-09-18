# WHAT I CUT AND WHY

* **Backend Server and Real Payment SDK**: Omitted a remote REST API backend and real payment gateway integration (e.g., Razorpay or Stripe) as per assignment specifications. Kitchen data is bundled as a local JSON asset and subscription purchases are simulated using persistent DataStore Preferences.
* **Complex Customization and Micro-Animations**: Prioritized a production-ready 3-screen Clean Architecture with genuine loading, empty, and error states over decorative animations or multi-item customization drawers within the target scope.
* **External Third-Party Analytics SDKs**: Implemented `LogcatAnalytics` for telemetry and DataStore Preferences for local state persistence without introducing external third-party SDK dependencies.

---

# TASK 1 — Application Scope and Implementation Mapping

* **Kitchen List Screen**: Implemented displaying kitchens near you with name, cuisine, price per tiffin, veg/non-veg indicator, rating, and real loading, empty, error, and success states (`KitchenListScreen.kt`, `KitchenListContent.kt`, `KitchenListViewModel.kt`).
* **Kitchen Detail Screen**: Implemented displaying a single kitchen's profile, full weekly menu, and a subscribe action (`KitchenDetailScreen.kt`, `KitchenDetailContent.kt`, `KitchenDetailViewModel.kt`).
* **Paywall Screen**: Implemented displaying ₹1 charged now and ₹249 charged on a named date 24 hours later with a persistent fake purchase (`PaywallScreen.kt`, `PaywallContent.kt`, `PaywallViewModel.kt`).
* **Third-Launch & Subscribe Paywall Trigger**: Implemented to auto-open paywall on the 3rd launch or Subscribe tap when unpaid, and permanently suppress it once paid (`RecordAppLaunchUseCase.kt`, `SubscriptionDataStore.kt`).
* **State Survival & Process Death**: Implemented to survive screen rotation, process death, and force-stopping/reopening using DataStore Preferences (`SubscriptionDataStore.kt`, `SubscriptionRepositoryImpl.kt`).
* **Analytics Telemetry**: Implemented with a single `Analytics` interface and `LogcatAnalytics` implementation logging 4 key moments (`app_launched`, `kitchen_detail_viewed`, `paywall_shown`, `subscription_purchased`) (`Analytics.kt`).

**Technologies and Libraries Used**:
* Dagger Hilt
* Latest Type-Safe Navigation Compose
* DataStore Preferences
* Jetpack Compose
* Clean Architecture
* MVVM

---

# TASK 2 — Fix What the Agent Wrote

### Identified Issues

1. **Problem 1 (Hardcoded Secret API Key — Financial and Security Risk)**
   * *Impact*: `"sk_live_9f2c41ab"` exposes secret API credentials in client-side code, allowing unauthorized entities to exploit API quotas and generate unexpected financial liabilities.
2. **Problem 2 (Infinite Network and Recomposition Loop — Server Request Flood)**
   * *Impact*: `LaunchedEffect(items)` observes `items` (`mutableListOf<Kitchen>`). Calling `items.addAll()` mutates `items`, which repeatedly re-triggers `LaunchedEffect`, freezing the UI and flooding the server with infinite network requests.
3. **Problem 3 (Blocking Network I/O on Main Thread — ANR Crash Risk)**
   * *Impact*: `URL().readText()` performs synchronous network operations directly on the Main (UI) thread, triggering an Application Not Responding (ANR) crash or `NetworkOnMainThreadException`.
4. **Problem 4 (Unstable Local State and Missing Recomposition Trigger)**
   * *Impact*: `var items = mutableListOf<Kitchen>()` creates an unremembered plain list without Compose state tracking. List mutations fail to trigger UI recomposition, and state is lost on configuration changes.

---

### Corrected Implementation

```kotlin
// --- Corrected ViewModel ---
@HiltViewModel
class KitchenListViewModel @Inject constructor(
    private val getKitchensUseCase: GetKitchensUseCase,
    private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow<KitchenListUIState>(KitchenListUIState.Loading)
    val uiState: StateFlow<KitchenListUIState> = _uiState.asStateFlow()

    init {
        loadKitchens()
    }

    fun loadKitchens() {
        viewModelScope.launch(ioDispatcher) {
            getKitchensUseCase().collect { result ->
                when (result) {
                    is ResultState.Loading -> _uiState.value = KitchenListUIState.Loading
                    is ResultState.Success -> _uiState.value = KitchenListUIState.Success(result.data)
                    is ResultState.Error -> _uiState.value = KitchenListUIState.Error(result.message)
                }
            }
        }
    }
}

// --- Corrected Composable ---
@Composable
fun KitchenListScreen(
    viewModel: KitchenListViewModel = hiltViewModel(),
    onKitchenClick: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is KitchenListUIState.Loading -> CircularProgressIndicator()
        is KitchenListUIState.Success -> {
            LazyColumn {
                items(items = state.kitchens, key = { it.id }) { kitchen ->
                    KitchenRow(kitchen = kitchen, onClick = { onKitchenClick(kitchen.id) })
                }
            }
        }
        is KitchenListUIState.Error -> Text(text = state.message)
    }
}
```

---

# TASK 3 — Release Note

### Release Readiness

Before publishing the application, I would validate the complete critical user journey in a release build rather than relying only on debug builds.

I would manually verify the kitchen list, including loading, empty, and error states, opening a kitchen, viewing its weekly menu, and navigating to the subscription paywall. I would test both ways of reaching the paywall: the third application launch and the Subscribe action.

For the subscription flow, I would verify that the paywall clearly displays the ₹1 initial charge and the ₹249 charge scheduled 24 hours later on the correct date. After completing the simulated purchase, I would verify that the paywall no longer appears. I would repeat this after rotation, process death, force-stopping the application, and reopening it to ensure the persisted state remains correct.

I would test on multiple Android versions and form factors, including a recent Android device and a lower-end/older device close to the minimum supported SDK. I would also test different screen sizes and orientations.

I would additionally test fresh installation, application restart, force-stop/reopen, configuration changes, empty local data, malformed JSON, and failure scenarios. Since the application is intended to behave correctly without relying on a backend, I would also verify its behavior when network connectivity is unavailable.

For a real production release, I would use a staged rollout instead of immediately releasing to 100% of users. This gives us an opportunity to observe crashes, application behavior, and important product metrics before expanding the rollout.

### Production Monitoring

For a production application, I would add observability around the critical user journey.

I would use Firebase Analytics to track important events such as:
- Kitchen list viewed
- Kitchen details opened
- Paywall displayed
- Subscribe button clicked
- Purchase initiated
- Purchase completed
- Purchase failed
- Subscription state restored

This would allow me to identify abnormal drops in the subscription funnel and understand where users are encountering problems.

I would use Firebase Crashlytics for crash and non-fatal error monitoring. Critical failures around persistence, navigation, JSON parsing, or subscription state would be reported with enough context to identify the affected application version and device configuration.

For a real subscription implementation, I would use RevenueCat to manage and monitor entitlements, purchase state, restoration, renewals, and subscription-related failures. I would compare subscription events with application-side analytics so that discrepancies between the application state and the actual entitlement state can be detected.

### Most Likely Production Failure

The area I consider most likely to cause a serious production issue is subscription/payment state becoming inconsistent with the application's persisted state.

For example, a user could successfully complete a purchase but still see the paywall after restarting the application. Another possible issue would be the application incorrectly treating a user as subscribed when their entitlement has expired or is otherwise unavailable.

I would detect this without waiting for users to report it by monitoring the subscription funnel through Firebase Analytics and validating entitlement and purchase information through RevenueCat in a real implementation.

I would also monitor Crashlytics for crashes and non-fatal errors around subscription state restoration and application startup. A significant increase in purchase failures, entitlement mismatches, or paywall displays after successful purchases would be treated as a release signal requiring investigation.

### If It Breaks at 11 PM

If a critical issue appeared shortly after release, I would first determine whether it was introduced by the latest version by comparing the affected events, crashes, and subscription behavior against the previous release.

I would use Crashlytics to identify the affected application version and devices, Firebase Analytics to understand how widespread the issue is, and RevenueCat to verify whether the problem is related to actual subscription/entitlement state.

If the issue is confirmed to be caused by the latest release, I would immediately stop or pause the staged rollout so that the problematic version is not distributed to additional users.

If the previous version is known to be stable, I would roll back to the previous release where possible. I would then reproduce the issue locally using the same state and environment, identify the root cause, and add a regression test covering the failure.

For the subsequent fix, I would release through a staged rollout again and closely monitor Crashlytics, Analytics, and subscription metrics before increasing the rollout percentage.

### Next Three Things I Would Build

1. **Real Subscription Infrastructure**: The current assignment uses a simulated purchase. For production, I would replace this with a real Google Play subscription flow and use RevenueCat for subscription management. This would include purchase verification, entitlement management, restoration, renewal/expiration handling, and keeping the application state synchronized with the user's actual subscription status.
2. **Backend-Driven Kitchen and Delivery System**: The current application uses bundled local JSON data. For production, I would introduce a backend for kitchens, weekly menus, pricing, availability, and delivery slots. I would also add appropriate local caching so that previously loaded information remains available when connectivity is temporarily unavailable.
3. **Automated Testing and Production Observability**: I would expand automated coverage around the most critical flows, particularly subscription state, persistence, process death, navigation, and the kitchen browsing journey. I would also expand Firebase Analytics and Crashlytics instrumentation and establish monitoring for important production signals such as crash-free users, purchase failures, entitlement problems, and unexpected drops in conversion through the subscription funnel.

### Production Hardening

As an additional production-hardening step, I would enable R8/ProGuard for release builds with carefully maintained rules for libraries that rely on serialization, reflection, dependency injection, or other runtime behavior.

This would provide code shrinking and optimization, reduce the final application size, and make reverse engineering of the released application more difficult.

I would test the minified release build thoroughly because incorrect R8 rules can remove classes or members required at runtime. I would specifically verify JSON serialization, dependency injection, navigation, and other reflection-based functionality after obfuscation.

However, I would not rely on ProGuard/R8 to protect secrets. API keys, private credentials, and other sensitive server-side secrets should never be embedded in the Android application because anything shipped in the APK can potentially be extracted. Those secrets should remain on trusted backend infrastructure.

---

# TASK 4 — How You Used AI

### 1. Tools and Share of Code
* **Tool Used**: Gemini (integrated directly within Android Studio IDE).
* **Share of Code**: Gemini AI generated approximately **75%** of boilerplate infrastructure, DTO models, local JSON assets, and Compose screen layouts. The remaining **25%** of architecture design, state-management rules, DataStore flow contracts, Hilt module bindings, and unit tests were directed and written by hand.

---

### 2. Single Best Prompt

```text
You are an expert Senior Android Engineer, Kotlin Engineer, Jetpack Compose Engineer, Clean Architecture specialist, Room/Offline-First engineer, Dagger Hilt engineer, and production Android code reviewer.

You are going to implement TASK 1 of the attached Propel Spark Android Developer Intern assignment.

I have my own established Android architecture and coding style.

YOUR JOB IS NOT TO INVENT A NEW ARCHITECTURE.

You must implement the assignment using MY existing architecture, patterns, naming conventions, state-management approach, dependency-injection approach, navigation approach, and coding style.

I want the final code to look like it was written by me consistently across my projects.

============================================================

1. SOURCE OF TRUTH
   ============================================================

You have two important references:

SOURCE A:
The Propel Spark Android Developer Intern Assignment PDF.

SOURCE B:
My personal Android architecture/code-style document.

Read BOTH completely before touching the codebase.

The assignment is the functional source of truth.

My architecture document is the architectural and coding-style source of truth.

When there is a conflict:

1. Assignment requirements determine WHAT the application must do.
2. My architecture determines HOW the application should be structured.
3. Do not violate an explicit assignment requirement in order to follow an architectural preference.
4. Do not add unnecessary architecture merely because it is theoretically possible.

The assignment explicitly states that Task 1 is approximately 150 minutes and that deliberate scope cutting is expected. Therefore:

BUILD A COMPLETE, CLEAN, PRODUCTION-QUALITY VERSION OF THE REQUIRED SCOPE.

Do NOT turn this into a giant enterprise application.

Do NOT add unnecessary features.

Do NOT add unnecessary libraries.

Do NOT add a backend.

Do NOT add real payments.

Do NOT add unnecessary networking.

Do NOT implement features that are not required.

The goal is:

Simple enough to finish within the intended assignment scope.

Structured enough to demonstrate professional Android engineering.

============================================================
2. ASSIGNMENT REQUIREMENTS — MUST IMPLEMENT
===========================================

Task 1 requires:

* Single-module Android application
* Kotlin
* Jetpack Compose
* Minimum SDK 24
* No backend
* Local JSON asset written by us
* Approximately 12 kitchens
* Three primary screens:

  1. Kitchen List
  2. Kitchen Detail
  3. Paywall

The List screen must show:

* kitchen name
* cuisine
* price per tiffin
* veg/non-veg
* rating

The List screen must have REAL:

* loading state
* empty state
* error state

Do NOT implement a fake spinner that never resolves.

The Detail screen must show:

* one kitchen
* its weekly menu
* Subscribe button

The Paywall must:

* open on the third launch
* open whenever Subscribe is tapped
* clearly state that ₹1 is charged now
* clearly state that ₹249 will be charged on a named date 24 hours later
* use a fake purchase
* persist the purchase state
* never show the paywall again once the user is paid

The application must survive:

* configuration change / rotation
* process death
* force-stop and reopening

The state must remain correct after reopening.

There must also be:

analytics.kt

with:

* one analytics interface
* one Logcat implementation
* calls at exactly four meaningful moments that YOU select

The four moments must be intentional and documented.

Do not add Firebase Analytics.

Do not add unnecessary analytics SDKs.

Use Logcat as required.

============================================================
3. FIRST: INSPECT THE EXISTING PROJECT
======================================

Before implementing anything:

READ THE ENTIRE EXISTING ANDROID PROJECT.

Do not immediately start writing files.

Inspect:

* Gradle files
* settings
* version catalog if present
* Application class
* MainActivity
* existing Compose setup
* theme
* navigation
* package structure
* dependencies
* existing utilities
* existing architecture
* existing tests
* resources
* manifest

Determine what already exists.

Do NOT recreate infrastructure that already exists.

If the repository already contains architecture code, extend it consistently.

If the repository is empty/starter-level, create the required architecture according to my architecture document.

============================================================
4. MY ARCHITECTURE MUST BE FOLLOWED
===================================

My preferred architecture is:

Clean Architecture

Presentation
↓
Domain
↓
Data

with:

presentation
domain
data
DI
navigation
core

Use a feature-oriented structure where appropriate.

My preferred package organization is approximately:

com.example.app/

core/
di/

domain/
state/
ResultState.kt
repository/ <feature>/
usecase/ <feature>/

data/
remote/ <feature>/
local/
entity/
dao/
database/
repo/ <feature>/

di/
modules/ <feature>/

presentation/
uiStates/ <feature>/
viewmodels/ <feature>/
screens/ <feature>/
screen/
components/

navigation/
routes/
navHost/

However, this assignment has only a small local data source.

DO NOT create meaningless layers or folders merely to make the project look larger.

Use the same architectural principles while keeping the implementation appropriate for the assignment.

============================================================
5. DATA ARCHITECTURE
====================

The assignment says the kitchen data should be bundled as a local JSON asset.

Therefore, implement the kitchen data source locally.

The architecture should conceptually be:

JSON asset
↓
Local Data Source
↓
Repository Implementation
↓
Repository Interface
↓
UseCase
↓
ViewModel
↓
UI State
↓
Compose UI

Do NOT access the JSON asset directly from a Composable.

Do NOT access Android Context directly from the ViewModel.

Do NOT put JSON parsing inside a Composable.

Do NOT put business logic inside UI components.

Do NOT let the screen directly call a repository.

The ViewModel should communicate through UseCases.

============================================================
6. RESULTSTATE — FOLLOW MY PATTERN
==================================

Use my ResultState pattern:

sealed class ResultState<out T> {
object Loading : ResultState<Nothing>()
data class Success<T>(val data: T) : ResultState<T>()
data class Error(val message: String) : ResultState<Nothing>()
}

Use this for asynchronous/data operations according to my established pattern.

Do not create multiple competing generic result wrappers.

Do not introduce Result, Either, Resource, UiResult, NetworkResult, etc. unless the existing codebase already uses one.

Use my ResultState consistently.

============================================================
7. USE CASES
============

Every meaningful business operation must have its own UseCase.

Follow my pattern:

class SomeUseCase @Inject constructor(
private val repository: SomeRepository
) {
suspend operator fun invoke(...): Flow<ResultState<...>> {
return repository.someOperation(...)
}
}

For this application, think in terms of actual business actions rather than blindly creating files.

Likely operations include:

* Load kitchens
* Get kitchen details
* Check launch/paywall state
* Check purchase state
* Initiate fake purchase
* Reset/clear state only if actually required

Only create UseCases that represent meaningful application actions.

Do not create useless UseCases such as:

GetContextUseCase

GetJsonParserUseCase

GetStringUseCase

or one-file wrappers around trivial UI operations.

============================================================
8. REPOSITORY LAYER
===================

Create repository interfaces in the domain layer.

Example concept:

KitchenRepository

The repository implementation belongs in:

data/repo/<feature>/

The repository should hide the actual data source from the domain layer.

Example:

JSON/local source
↓
KitchenRepoImpl
↓
KitchenRepository
↓
GetKitchensUseCase

The domain layer must NOT depend on Android framework classes unnecessarily.

The repository is responsible for coordinating the data source and mapping errors into ResultState.

Follow my established repository pattern:

* flow { }
* Loading
* try/catch
* Success
* Error
* appropriate logging

However, do not blindly copy the example code if a simpler implementation is more correct.

============================================================
9. LOCAL JSON DATA
==================

Create a realistic local JSON asset containing approximately 12 kitchens.

The data must be sufficient to demonstrate the UI.

Each kitchen should contain the information actually required by the application.

At minimum:

* ID
* name
* cuisine
* price per tiffin
* veg/non-veg
* rating
* weekly menu

The weekly menu should be structured cleanly rather than being one giant string if the UI needs individual days/meals.

Use realistic but fictional data.

Do not use a real company's data.

Do not add backend/API calls.

The JSON should be loaded asynchronously so the UI can demonstrate a genuine loading state.

============================================================
10. DOMAIN MODELS VS DATA MODELS
================================

Do not automatically use JSON DTOs directly everywhere.

Separate data representation from domain representation where it provides meaningful architectural value.

For example:

data/local/.../KitchenDto

and:

domain/.../Kitchen

if appropriate.

Explain and implement mapping cleanly.

Do not over-engineer a dozen mapper files for trivial structures.

Use judgment.

The final architecture should demonstrate that you understand separation of concerns.

============================================================
11. PAYWALL / PURCHASE PERSISTENCE
==================================

This is one of the most important requirements.

The fake purchase MUST persist.

Do not keep purchase state only in:

* remember
* rememberSaveable
* ViewModel memory
* Compose state
* singleton memory

Those do not satisfy the requirement.

The purchase state must survive:

* rotation
* process death
* force-stop
* application restart

Use persistent local storage.

For this assignment, use the simplest appropriate persistent mechanism consistent with my architecture.

If DataStore is already part of the project architecture, prefer DataStore for simple preferences such as:

* hasPurchased
* launchCount

Do NOT introduce Room merely for a boolean preference unless there is a strong architectural reason.

Room should be used for relational application data where appropriate, not simply because it is available.

============================================================
12. THIRD-LAUNCH PAYWALL LOGIC
==============================

Implement the third-launch requirement correctly.

You must carefully define what "launch" means.

The launch counter should persist.

Example conceptual flow:

Application starts
↓
Read launch count
↓
Increment appropriately
↓
Persist new count
↓
Check purchase state
↓
If already paid:
do not show paywall

If not paid and launch count reaches 3:
show paywall

The paywall must also open whenever Subscribe is tapped.

IMPORTANT:

Once the fake purchase succeeds:

hasPurchased = true

After that:

* third launch must NOT show paywall
* subscribe action must NOT show paywall again
* reopening app must preserve paid state

Avoid race conditions between incrementing launch count and reading purchase state.

============================================================
13. PAYWALL DATE
================

The paywall must state:

₹1 is charged now.

₹249 will be charged on a named date 24 hours later.

Do not hard-code a misleading arbitrary date.

Calculate the actual date/time based on the purchase moment.

The UI should clearly communicate the date.

Use an appropriate Android date/time API.

If exact time is shown, format it clearly.

The wording should be understandable to a normal user.

Example concept:

₹1 charged today.
₹249 will be charged on [date] after 24 hours.

Use the actual calculated date.

The purchase timestamp should be persisted if it is needed to reproduce the subscription information.

Do not pretend an actual payment occurred.

It is a fake purchase as explicitly required by the assignment.

============================================================
14. UI STATE ARCHITECTURE
=========================

I specifically want to be able to look at the code and clearly understand:

WHAT THE VIEWMODEL IS DOING

and

WHAT STATE THE UI IS IN.

Do not hide state management inside random Compose code.

Use dedicated sealed UI states following my architecture.

For example, conceptually:

sealed class KitchenListUIState {
object Idle : ...
object Loading : ...
data class Success(...) : ...
data class Error(...) : ...
}

Adapt the exact state structure to the real feature.

For the list screen, the UI state must explicitly represent:

* Idle if appropriate
* Loading
* Success
* Error
* Empty

Do NOT make Empty simply an unexplained side effect inside Success.

It should be obvious from reading the state handling how an empty result is represented.

You may use:

Success(emptyList())

if that is the cleanest implementation, but the UI must explicitly handle the empty case.

If a separate Empty state makes the architecture clearer, use it.

Use judgment based on my established pattern.

============================================================
15. VIEWMODEL ARCHITECTURE
==========================

Every major screen/feature should have an appropriate @HiltViewModel.

Follow my established pattern:

@HiltViewModel

class FeatureViewModel @Inject constructor(
private val useCase: SomeUseCase,
private val ioDispatcher: CoroutineDispatcher
) : ViewModel()

Use:

private val _state = MutableStateFlow<...>(...)
val state = _state.asStateFlow()

Use:

viewModelScope.launch(ioDispatcher)

for IO-bound work according to my architecture.

Collect ResultState from UseCases and map it into the appropriate dedicated UIState.

Conceptually:

ResultState.Loading
↓
UIState.Loading

ResultState.Success
↓
UIState.Success

ResultState.Error
↓
UIState.Error

The ViewModel owns screen state.

The Composable observes it.

The Composable does not contain business logic.

============================================================
16. UI STATE MUST BE EASY TO TRACE
==================================

I want the architecture to be readable enough that a reviewer can follow:

User taps button
↓
Screen callback
↓
ViewModel function
↓
UseCase
↓
Repository
↓
Data source
↓
ResultState
↓
ViewModel maps ResultState
↓
StateFlow changes
↓
Compose recomposes
↓
UI changes

Make this flow obvious from the code.

Avoid hidden magic.

Avoid excessive abstraction.

============================================================
17. SCREEN + CONTENT SEPARATION
===============================

Follow my preferred Compose structure.

Separate:

FeatureScreen.kt

from:

FeatureContent.kt

The Screen container is responsible for:

* obtaining Hilt ViewModel
* collecting StateFlow
* lifecycle-aware state collection
* navigation callbacks
* triggering initial actions
* coordinating screen-level effects

The Content composable is responsible for:

* displaying UI
* receiving UI state
* receiving callbacks
* rendering loading/error/empty/success
* being previewable

Use:

collectAsStateWithLifecycle()

for collecting state.

Do not use plain collectAsState() when lifecycle-aware collection is appropriate.

============================================================
18. LAUNCHEDEFFECT
==================

Use LaunchedEffect carefully.

For initial data loading, use an appropriate stable key such as:

LaunchedEffect(Unit)

ONLY when an initial one-time action is actually required.

Do not accidentally cause repeated API/data loads because of unstable keys.

Do not put business logic inside LaunchedEffect.

LaunchedEffect should trigger a ViewModel action.

Example concept:

LaunchedEffect(Unit) {
viewModel.loadKitchens()
}

============================================================
19. TYPE-SAFE NAVIGATION
========================

Use my preferred KotlinX Serialization type-safe navigation.

Use:

@Serializable object

for routes without arguments.

Use:

@Serializable data class

for routes with arguments.

Use:

composable<Route>

and:

backStackEntry.toRoute<Route>()

where appropriate.

The application should have routes for:

* Kitchen List
* Kitchen Detail
* Paywall

Kitchen detail should receive the kitchen identifier through the type-safe route.

Do NOT pass entire Kitchen objects through navigation if an ID is sufficient.

Preferred:

KitchenDetailRoute(kitchenId = id)

Then the detail ViewModel/use case retrieves the required kitchen.

Keep navigation independent from business logic.

============================================================
20. NAVIGATION RESPONSIBILITIES
===============================

The NavHost should coordinate navigation.

Screen composables should expose callbacks such as:

onKitchenClicked

onSubscribeClicked

onBackClicked

The screen should not contain hard-coded navigation architecture scattered throughout UI components.

Prefer:

Screen
↓
callback
↓
NavHost
↓
NavController

Keep route definitions centralized.

============================================================
21. KITCHEN LIST SCREEN
=======================

Implement the List screen professionally.

Each kitchen should display:

* name
* cuisine
* price per tiffin
* veg/non-veg
* rating

The screen must clearly show:

Loading

Empty

Error

Success

states.

The success state should use a LazyColumn.

Each kitchen should be represented with a reusable component such as:

KitchenCard

or:

KitchenRow

Place reusable components in:

presentation/screens/<feature>/components/

Do not put the entire UI into one 500-line Composable.

However, do not split every Text() into its own file.

Use meaningful component boundaries.

============================================================
22. KITCHEN DETAIL SCREEN
=========================

The detail screen should:

* receive kitchen ID through type-safe navigation
* load the kitchen
* display kitchen information
* display weekly menu
* expose Subscribe action

Handle:

* loading
* success
* missing kitchen
* error

Do not assume the kitchen always exists.

The UI should gracefully handle an invalid/missing kitchen ID.

============================================================
23. PAYWALL SCREEN
==================

Create a clean paywall.

It must clearly communicate:

* ₹1 is charged now
* ₹249 will be charged 24 hours later
* exact/named future date
* fake purchase action

The purchase button must:

1. trigger ViewModel action
2. execute appropriate UseCase
3. persist purchase
4. update UI state
5. navigate/close paywall appropriately

Do not let the Composable directly write DataStore.

Do not let the Composable directly modify persistent state.

============================================================
24. ANALYTICS
=============

Create:

analytics.kt

with one interface.

Example concept:

interface Analytics {
fun logEvent(...)
}

Then provide a Logcat implementation.

Do NOT create multiple analytics abstractions.

Do NOT integrate Firebase.

Choose exactly FOUR meaningful moments.

They should demonstrate that you understand product analytics.

Potential candidates:

* application launch
* kitchen viewed
* paywall shown
* purchase completed

But do not blindly copy these.

Choose the four that are most meaningful for THIS application.

Document why you selected them.

Analytics should be called from the appropriate architectural layer.

Do not put analytics calls randomly into UI rendering.

Do not fire analytics repeatedly due to recomposition.

This is critical.

A Composable may recompose many times.

Therefore analytics events must NOT accidentally fire multiple times because of recomposition.

============================================================
25. HILT
========

Use Dagger Hilt according to my architecture.

Use:

@Module

@InstallIn(SingletonComponent::class)

and appropriate:

@Provides

@Singleton

bindings.

Inject:

* repositories
* data sources where appropriate
* use cases where appropriate
* dispatchers
* persistence dependencies
* analytics

Use constructor injection whenever possible.

Do not create dependencies manually inside ViewModels or Composables.

Avoid Service Locator patterns.

============================================================
26. DISPATCHERS
===============

Follow my preference for injected CoroutineDispatcher.

Provide IO dispatcher through Hilt.

Use it for IO-bound operations.

This makes ViewModels testable.

Do not hard-code Dispatchers.IO throughout business logic if the architecture can inject it cleanly.

============================================================
27. COROUTINE / FLOW RULES
==========================

Follow structured concurrency.

Do not create:

GlobalScope.launch

or unmanaged coroutines.

Use:

viewModelScope

for ViewModel work.

Use Flow where asynchronous stream/result handling is appropriate.

Do not create Flow unnecessarily for purely synchronous operations.

Do not block the main thread.

JSON file reading must not block UI rendering.

Persistent state operations must be asynchronous where appropriate.

============================================================
28. ROOM — IMPORTANT
====================

DO NOT add Room simply because my general architecture supports Room.

The assignment explicitly requires bundled local JSON for kitchen data.

For this task:

JSON is the required kitchen data source.

DataStore or another simple local persistence mechanism can be used for:

* purchase state
* launch count
* purchase timestamp if needed

If you determine that Room is genuinely useful for another requirement, explain why before introducing it.

Do not over-engineer the assignment.

The reviewer should see that you know when to use Room and when NOT to use it.

============================================================
29. ERROR HANDLING
==================

Do not swallow exceptions.

Do not write:

catch (e: Exception) {
// ignore
}

Every meaningful failure should become a proper ResultState.Error.

Provide useful user-facing messages.

Log technical details separately.

Do not expose raw stack traces to the user.

Do not expose sensitive information in logs.

============================================================
30. EMPTY STATE
===============

The assignment specifically evaluates whether the app has a real empty state.

Therefore implement it intentionally.

For example:

If the JSON data source returns an empty list:

Success(emptyList())

must lead to:

Empty UI

rather than:

blank screen

or:

infinite spinner.

The architecture should make this behavior obvious.

============================================================
31. PREVIEWS
============

Every meaningful Content composable should have @Preview functions.

At minimum provide previews for:

* loading
* success
* empty
* error

where applicable.

Also provide realistic preview data.

Use:

showSystemUi = true

showBackground = true

according to my architecture style.

Do not require ViewModels inside previews.

Do not require Hilt inside previews.

The Content composable should remain previewable using injected state/callbacks.

============================================================
32. UI QUALITY
==============

The UI should be simple, modern, clean, and production-presentable.

Do not spend most of the assignment time creating elaborate animations.

Prioritize:

* hierarchy
* spacing
* readable typography
* clear CTA
* accessibility
* loading feedback
* error recovery
* empty state
* consistent cards
* proper touch targets
* clear navigation
* clean paywall

Use Material 3 where appropriate.

Do not create an unnecessarily complicated design system for a three-screen assignment.

============================================================
33. ACCESSIBILITY
=================

Use meaningful:

* content descriptions where required
* readable text
* adequate touch targets
* semantic labels where useful

Do not add decorative content descriptions.

Buttons must clearly communicate their action.

============================================================
34. STATE SURVIVAL
==================

Carefully reason about state categories.

There are three different kinds of state:

1. UI state
2. screen/process state
3. persistent application state

Examples:

UI state:
Loading / Success / Error

Temporary screen state:
selected UI element

Persistent state:
purchase status
launch count
purchase timestamp

Do not confuse these.

ViewModel state handles screen state.

Persistent storage handles application state that must survive process death.

============================================================
35. PROCESS-DEATH REQUIREMENT
=============================

The application must still be correct after:

1. User launches app
2. App loads data
3. User purchases
4. App is force-stopped
5. App is opened again

The application must remember the purchase.

Similarly, launch count must be persisted so that the third-launch behavior works across application restarts.

Do not rely on in-memory variables.

============================================================
36. CODE QUALITY
================

Write production-quality Kotlin.

Follow:

* Kotlin idioms
* immutability
* meaningful names
* small focused functions
* single responsibility
* proper visibility
* no unnecessary mutable state
* no magic numbers where constants are appropriate
* no duplicated business logic
* no dead code
* no commented-out code
* no unnecessary abstraction

Use private visibility where implementation details do not need exposure.

============================================================
37. KDOC AND DOCUMENTATION
==========================

I care about documentation quality.

Add KDoc to meaningful public APIs and important architectural elements.

KDoc should explain:

* purpose
* responsibility
* important parameters
* return value when non-obvious
* important business rules
* architectural decisions
* persistence behavior
* analytics behavior
* complex logic

Do NOT add meaningless comments to every trivial line.

For example, do NOT write:

// Increment i
i++

That is noise.

Instead document WHY something exists when the reason is not obvious.

Especially document:

* repository interfaces
* use cases
* ViewModels
* UI state classes
* analytics abstraction
* persistence abstraction
* important domain models
* non-obvious business logic
* third-launch behavior
* purchase persistence
* date calculation
* architectural boundaries

Comments should explain WHY, not restate WHAT obvious code already says.

============================================================
38. TESTABILITY
===============

Structure the code so that important logic can be unit tested.

Especially make testable:

* third-launch logic
* purchase persistence logic
* paywall visibility logic
* repository behavior
* ViewModel state transitions
* error handling
* date calculation

Do not create an enormous test suite for the assignment.

At minimum identify the highest-value tests.

If time allows, implement focused unit tests for critical logic.

============================================================
39. DO NOT HIDE BUSINESS LOGIC
==============================

I want to be able to inspect the ViewModel and immediately understand:

* what action happened
* what UseCase was called
* what state changed
* why the UI changed

Likewise, I want to inspect a UseCase and understand:

* what business action it performs
* what repository it uses

And inspect the Repository and understand:

* where the data comes from
* how errors are handled

Avoid clever abstractions that make simple behavior difficult to trace.

============================================================
40. FILE-BY-FILE RESPONSIBILITY
===============================

After implementation, verify that every created file has a clear reason to exist.

The project should be understandable approximately as:

Presentation
↓
UI
↓
ViewModel
↓
UseCase
↓
Repository
↓
Data Source
↓
Local JSON / DataStore

Explain this through the code structure.

Do not create files merely because Clean Architecture tutorials usually contain them.

============================================================
41. SUGGESTED FEATURE ORGANIZATION
==================================

Use my architecture style.

A reasonable structure may look like:

presentation/
uiStates/
kitchen/
paywall/

```
viewmodels/
    kitchen/
    paywall/

screens/
    kitchen/
        screen/
        components/

    paywall/
        screen/
        components/
```

domain/
state/
ResultState.kt

```
repository/
    kitchen/
    subscription/

usecase/
    kitchen/
    subscription/
```

data/
local/
...

```
repo/
    kitchen/
    subscription/
```

di/
modules/
kitchen/
subscription/
dispatchers/

navigation/
routes/
navHost/

analytics/
analytics.kt

But adapt this to the actual starter project and my architecture document.

Do not duplicate infrastructure.

============================================================
42. DATA FLOW DOCUMENTATION
===========================

For every major feature, ensure the implementation makes this trace possible.

KITCHEN LIST:

KitchenListScreen
↓
KitchenListViewModel
↓
GetKitchensUseCase
↓
KitchenRepository
↓
Local JSON Data Source
↓
Kitchen data
↓
ResultState
↓
KitchenListUIState
↓
Compose

KITCHEN DETAIL:

KitchenDetailScreen
↓
KitchenDetailViewModel
↓
GetKitchenByIdUseCase
↓
KitchenRepository
↓
Local JSON
↓
Kitchen
↓
UI State
↓
Compose

PURCHASE:

PaywallScreen
↓
PaywallViewModel
↓
PurchaseUseCase
↓
SubscriptionRepository
↓
Persistent local storage
↓
Purchase saved
↓
Success
↓
UI / Navigation

These are conceptual examples.

Implement the actual flow correctly.

============================================================
43. IMPORTANT — DO NOT CHEAT THE REQUIREMENTS
=============================================

Do not implement fake persistence like:

var isPaid = false

Do not implement launch count like:

var launches = 0

Do not rely on ViewModel memory.

Do not use rememberSaveable for persistent purchase state.

Do not use a hard-coded third-launch flag.

Do not show paywall every time Subscribe is tapped after payment.

Do not use a fake loading spinner that remains forever.

Do not directly access URL/network.

Do not add an API key.

Do not create a backend.

Do not use a payment SDK.

Do not use Firebase Analytics.

Do not use external services for kitchen data.

============================================================
44. ANALYTICS — EXACTLY FOUR MOMENTS
====================================

Choose exactly four analytics moments.

For each one, document:

Event name

Where it is called

Why it matters

How duplicate events are prevented

Example format:

Event:
kitchen_viewed

Trigger:
User opens a kitchen detail screen

Layer:
appropriate application layer

Reason:
Measures which kitchens users inspect

Again, choose events based on the actual implementation.

Do not call analytics directly during every recomposition.

============================================================
45. README / CUT LIST
=====================

The assignment explicitly asks for a short "what I cut and why" at the top of the README because the task is intentionally larger than four hours.

Create/update README accordingly.

At the very top include:

WHAT I CUT AND WHY

Keep it short and honest.

Only list things that were genuinely out of scope.

Do not claim that required functionality was cut.

Good examples could be things such as:

* real payment SDK
* backend
* advanced filtering
* animations
* extensive automated UI testing

ONLY mention items that you actually did not implement.

Do not pretend something was cut if you implemented it.

============================================================
46. IMPLEMENTATION ORDER
========================

Work in this order:

STEP 1
Read assignment completely.

STEP 2
Read my architecture document completely.

STEP 3
Inspect entire Android repository.

STEP 4
Understand existing Gradle/project setup.

STEP 5
Create/confirm package architecture.

STEP 6
Create domain models.

STEP 7
Create local JSON data.

STEP 8
Create local data source.

STEP 9
Create repository interface.

STEP 10
Create repository implementation.

STEP 11
Create ResultState handling.

STEP 12
Create UseCases.

STEP 13
Create persistent subscription/launch state mechanism.

STEP 14
Create Hilt modules.

STEP 15
Create UI state classes.

STEP 16
Create ViewModels.

STEP 17
Create type-safe routes.

STEP 18
Create NavHost.

STEP 19
Create List Screen + Content + components.

STEP 20
Create Detail Screen + Content + components.

STEP 21
Create Paywall Screen + Content + components.

STEP 22
Implement third-launch logic.

STEP 23
Implement persistent fake purchase.

STEP 24
Implement analytics interface + Logcat implementation.

STEP 25
Add exactly four analytics events.

STEP 26
Add previews.

STEP 27
Add focused tests where practical.

STEP 28
Build and fix compilation errors.

STEP 29
Run through the complete user flow.

STEP 30
Verify process-death/persistence behavior.

STEP 31
Review architecture.

STEP 32
Review code quality.

STEP 33
Review README and cut list.

============================================================
47. MANUAL ACCEPTANCE TEST
==========================

Before declaring the task complete, manually reason through this exact flow:

TEST 1:

Fresh install.

Launch application.

Expected:

Kitchen list loads from local JSON.

No network required.

TEST 2:

Kitchen list loading state appears appropriately.

TEST 3:

Kitchen list success state displays kitchens.

TEST 4:

Tap kitchen.

Expected:

Navigate using type-safe navigation.

Detail screen loads correct kitchen.

TEST 5:

Detail screen displays weekly menu.

TEST 6:

Tap Subscribe.

Expected:

Paywall opens.

TEST 7:

Paywall displays:

₹1 now

₹249 on a named date 24 hours later

TEST 8:

Complete fake purchase.

Expected:

Purchase is persisted.

Paywall closes/navigates appropriately.

TEST 9:

Tap Subscribe again.

Expected:

Paywall must NOT reappear if the user is already paid.

TEST 10:

Force-stop application.

Reopen application.

Expected:

Paid state remains.

TEST 11:

Verify third-launch behavior with a fresh/unpaid state.

Paywall appears on the third launch.

TEST 12:

Verify that after payment, the third-launch paywall does not return.

TEST 13:

Verify rotation.

Expected:

UI remains correct.

No accidental duplicate loading/purchase events.

TEST 14:

Verify analytics events.

Exactly four meaningful analytics moments should exist.

No analytics event should fire repeatedly due to Compose recomposition.

============================================================
48. FINAL ARCHITECTURE REVIEW
=============================

After implementation, review the entire codebase as a Senior Android Engineer.

Check:

ARCHITECTURE

* Is Clean Architecture actually followed?
* Is dependency direction correct?
* Is presentation independent from data implementation?
* Are repositories behind interfaces?
* Are UseCases meaningful?

VIEWMODEL

* Is ViewModel responsible for screen state?
* Is StateFlow exposed read-only?
* Is MutableStateFlow private?
* Is dispatcher injected?
* Is viewModelScope used correctly?
* Are ResultState values mapped correctly?

UI

* Is Screen separated from Content?
* Is state collected with collectAsStateWithLifecycle?
* Are callbacks used correctly?
* Is navigation outside reusable UI?
* Are previews available?
* Are loading/error/empty/success states visible?

NAVIGATION

* Are routes @Serializable?
* Are route arguments type-safe?
* Is toRoute() used?
* Is only required data passed?

DATA

* Is JSON loading separated from UI?
* Is repository responsible for data access?
* Are errors handled?
* Is there no unnecessary networking?

PERSISTENCE

* Does purchase survive process death?
* Does launch count survive process death?
* Does force-stop preserve state?
* Is persistent state separated from transient UI state?

PAYWALL

* Is ₹1 clearly stated?
* Is ₹249 clearly stated?
* Is the future date calculated correctly?
* Does paywall disappear after purchase?

ANALYTICS

* Exactly four moments?
* Meaningful?
* No recomposition duplicates?
* Interface + Logcat implementation?
* No Firebase?

CODE QUALITY

* No dead code
* No unnecessary abstractions
* No magic strings where inappropriate
* No duplicated logic
* No unnecessary comments
* Meaningful KDocs
* Clean naming
* Small focused functions
* Testable architecture

============================================================
49. VERY IMPORTANT — DO NOT OVER-ENGINEER
=========================================

This is an intern take-home assignment.

The assignment says the task is deliberately larger than the available time and that cutting scope deliberately is part of the evaluation.

Therefore:

Do NOT add:

* multi-module architecture
* unnecessary networking
* Retrofit
* Ktor
* Room solely for demonstration
* Firebase
* real payment SDK
* complex dependency abstractions
* unnecessary design-system modules
* unnecessary repository layers
* unnecessary mappers
* complex state machines
* unnecessary event buses
* unnecessary coordinators
* unnecessary factories
* unnecessary generic abstractions

Use my architecture principles, but apply engineering judgment.

A small application can still have excellent architecture.

============================================================
50. FINAL DELIVERABLE
=====================

When finished, the repository should contain a working Android application implementing Task 1.

It must:

* compile
* install
* run
* work without backend
* load local JSON
* display 12 kitchens
* navigate between three screens
* display weekly menu
* show real loading/empty/error states
* show paywall on third launch
* show paywall on Subscribe
* clearly state ₹1 now
* clearly state ₹249 on named date 24 hours later
* fake purchase
* persist purchase
* persist launch count
* survive rotation
* survive process death
* survive force-stop/reopen
* contain analytics.kt
* contain one analytics interface
* contain Logcat implementation
* call analytics at exactly four meaningful moments
* use my Clean Architecture
* use my ResultState
* use my UseCase pattern
* use my ViewModel/StateFlow pattern
* use my Hilt pattern
* use my Screen/Content Compose pattern
* use my type-safe navigation pattern
* contain meaningful KDoc
* contain previews
* remain understandable and maintainable

============================================================
51. FINAL RESPONSE TO ME
========================

After implementation, do NOT simply say:

"Done."

Give me a concise engineering report containing:

1. Files created
2. Files modified
3. Final architecture
4. Complete data flow
5. ViewModel responsibilities
6. UI state explanation for each screen
7. Persistence mechanism
8. Third-launch implementation
9. Paywall logic
10. Four analytics events and why they were selected
11. Navigation flow
12. Tests performed
13. Any known limitations
14. What was intentionally cut
15. Any architectural decisions that differ from my original architecture document and WHY

For every important architectural decision, explain it in practical terms.

============================================================
FINAL INSTRUCTION
=================

DO NOT CODE BEFORE UNDERSTANDING.

READ → ANALYZE → PLAN → IMPLEMENT → TEST → REVIEW.

I want code that demonstrates that I understand Android architecture, not code that merely satisfies the visible UI requirements.

The reviewer should be able to inspect the project and clearly understand:

UI state
↓
ViewModel
↓
UseCase
↓
Repository
↓
Data Source
↓
Persistence

and:

User action
↓
ViewModel
↓
business operation
↓
state change
↓
Compose recomposition

The architecture must be visible in the code.

Do not hide complexity.

Do not add unnecessary complexity.

Follow my established Android architecture consistently.

Implement ONLY Task 1.
```

---

### 3. One Thing the Agent Got Wrong in Your Actual Work
* **Agent Error**: During initial build configuration, the agent attempted to import extended Material icons (`Icons.AutoMirrored.Filled.ArrowBack`) which caused a compilation failure because `material-icons-extended` was not declared in the project's version catalog.
* **Detection**: Caught immediately during Gradle build execution (`gradle_build`).
* **Resolution**: Replaced the missing extended icon with clean custom typography ("←") to eliminate unnecessary binary dependency overhead and keep the APK lightweight.

---

### 4. One Thing Written by Hand
* **Hand-Written Component**: The generic `ResultState<out T>` sealed class, `LogcatAnalytics` telemetry implementation, and `RecordAppLaunchUseCaseTest` unit test logic were written by hand directly.
* **Reasoning**: Specifying the exact generic sealed structure and unit test assertion flow in an AI prompt takes more round-trip time than directly writing the concise Kotlin definitions.
