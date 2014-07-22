Change Log
===============================================================================

Version 4.1.0 *(2012-05-17)*
----------------------------

 * Fix: Altered technique used for menu event dispatching through the fragment
   manager for greater control.
 * Fix: Do not dispatch menu creation event if the activity has been destroyed.
 * Fix: Correct potential `NullPointerException` when expanding an action item.
 * Fix: Correct potential `NullPointerException` when the hardware menu key was
   pressed in an activity that is forcing the overflow menu.
 * Fix: Do not set a listener on the native action bar tab wrapper unless a
   compatibility listener has been set.
 * Fix: Ensure the compatibility animation framework is always available on
   views even if they were previously detached from the view hierarchy.


Version 4.0.2 *(2012-04-15)*
----------------------------

 * Upgrade to r7 support library.
 * Fix: Do not trigger menu creation after `onCreate` if activity is finishing.
 * Fix: Prevent overflow from displaying if there are no overflow action items.
 * Fix: Long-pressing menu key no longer triggers overflow.
 * Fix: Use proper tab state-list drawable to mimic ICS.
 * Fix: Ensure dispatching menu creation and preparation to fragments can
   properly return `false` when appropriate to avoid rendering artifacts.
 * Fix: Properly save and fetch action mode tag on ICS.
 * Fix: Add missing density-specific resources for certain asssets and remove
   unused assets.


Version 4.0.1 *(2012-03-25)*
----------------------------

 * Add `ShareActionProvider` widget for use as action items.
 * Re-add 'Styled' sample to provide a more comprehensive theming example.
 * Fix: Do not dispatch options item selection to fragments if the activity
   handles the callback.
 * Fix: Prevent menu key from opening the overflow menu when an action mode is
   currently displayed.
 * Fix: Ensure fragment transaction instance is not `null` on initial tab
   selection callback.
 * Fix: Displaying an action mode while using stacked tab navigation no longer
   throws an exception.
 * Fix: Using expandable action item callbacks no longer results in a possible
   exception on older devices.


Version 4.0.0 *(2012-03-07)*
----------------------------

Complete rewrite of the library to backport the Android 4.0 action bar.

 * The minimum supported version of Android is now 2.1 (API 7).
 * New base activities are provided (e.g., `SherlockActivity` and
   `SherlockFragmentActivity`) which extend from the native activities.
 * The support library sources are no longer included in the library. You must
   include `android-support-v4.jar` in your project separately.
 * Theming now mirrors that of the native action bar through the use of multiple
   styles rather than through `ab`- and `am`-prefixed attributes in the theme.
 * The action bar can be statically attached to an activity view without the
   requirement of using one of the provided base activities.


Version 3.5.1 *(2012-01-03)*
----------------------------

 * Fix: `NullPointerException` in `FragmentManager` can no longer occur when an
   attempt is being made to save to a `Bundle` that has not yet been created.
 * Fix: Pre-3.0 action item submenu dialogs now properly dismiss themselves when
   an item of theirs is selected.


Version 3.5.0 *(2011-12-18)*
----------------------------

 *  Library now uses the `r6` version of the compatibility library for its base.
    Ice Cream Sandwich-specific implementations are currently disabled, however,
    but will be added in a future version of the library.

    `MenuCompat`, `MenuItemCompat`, and `ActivityCompat` have be added back in
    to ease transition to this library but all their methods and the classes
    themselves have been deprecated.
 *  Rewritten menu and action item support from Ice Cream Sandwich.

    * Removed the need for the custom `Window.FEATURE_ACTION_ITEM_TEXT` flag.
      You should now use the `showAsAction` attribute and/or the
      `setShowAsAction(int)` method on each `MenuItem` to control whether or
      not t