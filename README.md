# Reminder App

This is a simple Android reminder application that allows users to set tasks with notifications. The app lets users input tasks, set reminders for them, and receive notifications. It also supports saving and viewing previously entered tasks.

## Features

- **Set Reminders**: Add a task and set a reminder for a specific time.
- **Notifications**: The app sends notifications to remind the user of their task at the set time and 5 minutes before.
- **Save Tasks**: All tasks with reminders are saved locally using `SharedPreferences`.
- **View Saved Tasks**: Users can view a list of saved tasks anytime.
- **Permissions**: The app requests notification and alarm permissions to work correctly.

## Requirements

- Android 6.0 (API level 23) or higher.
- Permissions to post notifications and schedule alarms.
- `POST_NOTIFICATIONS` permission for Android 13 (API level 33) and above.

## Permissions

The app requests the following permissions:

- **POST_NOTIFICATIONS**: To allow the app to show notifications for reminders.
- **SCHEDULE_EXACT_ALARM** and **USE_EXACT_ALARM**: To set alarms that will trigger notifications at the correct times.

## Screenshots
![image](https://github.com/user-attachments/assets/6f1a5315-fa91-4447-ac9e-3e6eec78557c)
![image](https://github.com/user-attachments/assets/dfbcb39f-e019-466e-9d46-91798849b7d7)



## How It Works

1. **Adding a Task:**
   - Users can input a task and set the reminder time using the `TimePicker`.
   - The task is saved in `SharedPreferences` for persistence.

2. **Setting a Reminder:**
   - Once the user sets a reminder, the app schedules an alarm using `AlarmManager`.
   - Two notifications are created:
     - One at the reminder time.
     - One 5 minutes before the reminder.

## Code Overview

- **MainActivity.java**: The main activity where users can input tasks, set reminders, and view saved tasks.
- **NotificationReceiver.java**: Receives the alarm broadcasts and displays notifications.
- **AndroidManifest.xml**: Configures necessary permissions and app components.
- **activity_main.xml**: The layout file for the main screen where users input tasks and view the time picker.


