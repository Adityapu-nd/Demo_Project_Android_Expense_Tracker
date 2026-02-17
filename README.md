# Expense Tracker Android App

A modern Android expense tracking application built with MVVM architecture, Jetpack Compose, Room Database, and Material Design 3.

## Features
- Add, modify, and delete expenses
- Categorize expenses
- Analytics dashboard with graphs and category breakdown
- Sort expenses by price, date, and category
- Customizable categories with unique colors
- FTUX (First Time User Experience) onboarding
- Hide system navigation and status bars for immersive experience

## Architecture
- **MVVM**: Separation of UI, business logic, and data
- **Jetpack Compose**: Declarative UI
- **Room**: Local database for storing expenses and categories
- **ViewModel**: State management and business logic

## Getting Started
1. Clone the repository:
   ```bash
   git clone https://github.com/yourusername/ExpenseTrackerAndroid.git
   ```
2. Open in Android Studio
3. Build and run on an emulator or device

## Project Structure
- `app/src/main/java/com/example/expense_tracker_android/model/` — Data models and DAOs
- `app/src/main/java/com/example/expense_tracker_android/viewmodel/` — ViewModels
- `app/src/main/java/com/example/expense_tracker_android/ui/screen/` — Composable screens
- `app/src/main/java/com/example/expense_tracker_android/util/` — Utility classes

## Testing
- Unit tests in `app/src/test/java/`
- Instrumented tests in `app/src/androidTest/java/`

## License
MIT License

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## Contact
For questions or support, open an issue or contact the maintainer.

