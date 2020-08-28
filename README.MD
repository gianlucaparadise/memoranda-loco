# Memoranda Loco App

## Overview

Memoranda Loco will remember you to:
* open your contact tracing app once you leave home
* turn off the bluetooth once you get home

## Try It

**N.B.** This is intended to be a POC.

### Android

Memoranda Loco for Android can be installed from the [Play Store](https://play.google.com/store/apps/details?id=com.gianlucaparadise.memorandaloco).

## Tech notes

I made this project to test the capabilities of the following technologies:
* Geofencing
* Android ViewModel
* Android LiveData
* DataBinding
* Room
* Dagger Hilt
* Coroutines

## FAQ

### You're stealing my location! 😭

No, I'm not. Mostly because I personally don't care about it, but also because Memoranda Loco doesn't have Internet Permissions. Therefore, there is no way for the app to share any information with the world.