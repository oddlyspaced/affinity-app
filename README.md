# Affinity - App

Affinity is meant to be an easy to deploy and use platform for any person/organisation wanting to publicize their offerings in their area.

Note: affinity is a 2 part project, this repo contains the app codebase. You can checkout the [server](https://github.com/oddlyspaced/affinity-server) part of the project here.

## About
Affinity is meant to be used by organisations or individuals that offer some form of service and want to publicize their offerings to people in their area, but while doing so do not want to involve a congolomerate service platform as their basis. Affinity provides a solution by providing a fully self sufficient deployable platform that anyone can deploy on their server and then use it as a basis to share and deploy apps to the people.

## Setting up
Step 1: Clone the server repo to the server where the project is going to be deployed

    git clone https://github.com/oddlyspaced/affinity-app.git
   
Step 2: Open the `affinity-app` project in Android Studio.

Step 3: Open `AffinityConfiguration` class in the common module folder and edit the class parameters according to your requirements. The main parameter to edit here is the `API_URL_LINK` and `API_URL_PORT`.

Step 4: From top of Android Studio, choose the module that you want to compile for. The availaible options include:
- provider
- manager
- customer

Step 5: Compile debug or signed releases apk according to your usecase.

## Requirements
- Android Studio

## Technologies Used
- Kotlin
- Coroutines
- Retrofit
- OpenStreetMap

## Contributors
- Hardik Srivastava ([@oddlyspaced](https://github.com/oddlyspaced))
