This entry describe how to root Nexus One with Android build number ERD79.

## Introduction ##

In order for us to implement our application, we had to root the nexus one to read and write device input files.


## Step 1: Unlocking bootloader ##
  * Connect the Nexus One to your computer which has Android SDK tool downloaded.
  * Reboot the Nexus One into fastboot (Power off and then hold down trackball while booting).
  * Type **fastboot devices** in command line to check device is recognized.
  * Type **fastboot oem unlock** to unlock the bootloader.
  * Use volume keys to navigate to yes and press the power button to confirm.
  * You should see unlocked after you reboot the phone.
  * ![http://automater-isa673.googlecode.com/files/unlocked.jpg](http://automater-isa673.googlecode.com/files/unlocked.jpg)

## Step 2: Rooting ##
  * Download and extract superboot for ERD79 at http://coblitz.codeen.org/www.romraid.com/paul/nexusone/1.1-ERD79-nexusone-superboot.zip.  If you have different build version, then check Paul's superboot forum for other versions.
  * Go to superboot extracted location using your command prompt (i.e c:\tmp\1.1-nexusone-superboot)
  * connect the phone to your computer and boot the phone into bootloader by holding down trackball while booting.
  * On windows, load the superboot by **install-superboot-windows.bat**.
  * For linux --> install-superboot-linux.sh & for Mac -->install-superboot-mac.sh
  * You should have unlocked rooted Nexus One now.

## Restoring ##
  * If you happen to brick the phone or the phone stopped working, you can restore the original android images from [here](http://android.modaco.com/content/google-nexus-one-nexusone-modaco-com/300414/10-may-erd79-epf21b-stock-rom-for-nexus-one-images-zip-online-kitchen-optional-root-insecure-himem/)
  * The command to flash would be --> ./fastboot-windows.exe 

&lt;partition&gt;

 

<image\_file>


  * fastboot-windows.exe : executable that comes with superboot.
  * 

&lt;partition&gt;

 : {boot, recover, system, userdata,...}
  * 

<image\_file>

 : {boot.img, recover.img, system.img, userdata.img, ... }


# References #
  * http://androidandme.com/2010/01/hacks/video-how-to-unlock-and-root-a-nexus-one/
  * http://android.modaco.com/content/google-nexus-one-nexusone-modaco-com/298782/10-may-erd79-epf21b-superboot-rooting-the-nexus-one/