# Introduction #

BatteryProfiler Captures the battery state at specified intervals and displays the raw data at the moment. The program can be changed easily/improved by capturing more information and dealing with data export.


# Details #

BatteryProfiler consists of 6 Java files and 3 xml layouts. Following is the description grouped by type:

> # Activities #
  * Battery.java - The main Activity that lists all the saved captures and allows the users to delete them or create a new capture.

  * Record.java - The Activity that consists of a form for users to enter the capture information before starting a new capture or displays information of a saved capture.

  * Report.java - The Activity that displays all captured data from the database.

> # Services #
  * RecordService.java - It is a background service that gets called by Record.java to capture the battery status into the database.

> # Database #
  * BatteryAdapter.java - This is a row mapper that extends SimpleCursorAdapter class.

  * BatteryDbAdapter.java - This class contains all connection and database support for the BatteryProfiler application.