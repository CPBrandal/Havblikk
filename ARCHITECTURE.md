# App Architecture

# Sketch
![homescreen drawio](https://media.github.uio.no/user/10378/files/90c8c8f0-9e6d-469a-8aff-2fd4f0b5b081)
![mapscreen drawio](https://media.github.uio.no/user/10378/files/a4ecb008-6ca1-4589-8e76-47c34b1de7cd)
![last1 drawio](https://media.github.uio.no/user/10378/files/fc272885-3d18-48b5-8379-0d8dac2257e8)

## Overview
Our app follows the MVVM (Model-View-ViewModel) architecture pattern, utilizing Jetpack Compose for building the UI.

## Components
- **UI Layer:** Contains Jetpack Compose UI components responsible for rendering the user interface.
- **ViewModels:** Contains ViewModel classes that manage UI-related data and communicate with the business logic layer.
- **Repository:** Contains repository classes responsible for abstracting the data sources and providing data to the ViewModel.
- **Data Source:** Includes data sources such as remote data source (API calls) and local data source, see instructionUi and infoUiState.

## Object-Oriented Principles
- **Low Coupling:** We have tried to achieve low coupling in our solution, aiming to reduce the interdependence between components. By minimizing the connections and dependencies between different parts of the system, we make it easier to modify individual components without affecting others significantly. This ensures more flexibility and scalability for future developments.
- **High Cohesion:** We have focused on achieving high cohesion in our solution by dividing the code into various fragments and components, each with its clearly defined
 scope of responsibility. By having this modular structure, we ensure that each fragment and component performs tasks within its specific functional area. To achieve
 this high cohesion, we have implemented a screen-based approach, where we have divided the user interface into separate screens. Each screen focuses on
 a specific part of the application and handles related functionality.
 This makes it easier to understand and maintain the code, as each screen is dedicated to handling specific tasks.

## Design Patterns
- **MVVM:** ViewModels are responsible for managing UI-related data and communicating with the repository to fetch or update data. Views observe changes in ViewModel data using LiveData or State in Jetpack Compose.
- **UDF (Unified Data Flow):** Our app follows a unidirectional data flow where data flows from the repository to the ViewModel, and then to the UI layer, ensuring predictable and consistent state management.

## Maintenance and Further Development
For maintenance and further development of the app, developers should be familiar with Kotlin, Jetpack Compose.
We have chosen API level 24 (Android 8.0) as the minimum API level to support a wide range of devices while still having access to modern APIs and features.
The targeted SDK is 34.
