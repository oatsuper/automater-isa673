# Introduction #

We have changed Automater to AutoDroid to not get in conflict with the name and icon from Apple Automater.

AutoDroid is a Java based Android application that only works with a rooted Google Nexus One at the moment.


# Details #

AutoDroid consists of 8 Java files and 4 xml layouts. Following is the description grouped by type:

> # Activities #
  * AutoDroid.java - The main Activity that lists all the saved captures and allows the users to delete them or create a new capture.

  * Record.java - The Activity that consists of a form for users to enter the capture information before starting a new capture or displays information of a saved capture.

  * Replay.java - The Activity that consists of a form for the parameters for the ReplayService.

> # Services #
  * PlaybackService.java - It is a background service that gets called by Replay.java to inject the captured events from the database back into the device.

  * RecordService.java - It is a background service that gets called by Record.java to capture the raw input events into the database.

> # Database #
  * AutoDroidAdapter.java - This is a row mapper that extends SimpleCursorAdapter class.

  * AutoDroidDbAdapter.java - This class contains all connection and database support for the AutoDroid application.

> # Misc #
  * Constants.java - Users can change the touchpad device event and buttons device event to allow for compatibility with other Android devices.