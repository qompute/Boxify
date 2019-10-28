# Boxify

## Project Description
This project was created as a part of [Cal Hacks 6.0](https://cal-hacks-6.devpost.com/), which took place over 36 hours on October 25-27, 2019. You can find more complete information about this project, including screenshots, on this project's [Devpost page](https://devpost.com/software/boxify-8gmbwq).

## What it does
Boxify is an Android app that allows you to keep track of important items and where they're stored. Using image recognition technology, Boxify simplifies the process of organizing photos of objects, so you'll know exactly which drawer or shelf you put that one item that you need.

## Inspiration
Both of us relate strongly to the feeling of losing something or spending hours trying to find something that we needed at some point. We've also all been in the awkward situation of trying to fetch a tool in a shared workplace or living space that we were unfamiliar with, leading us to fumble around the shelves trying to find an item. While technology can make many aspects of our life more organized, there isn't much technology available when it comes to organizing physical objects around the house. We wanted to build an app to help people keep track of where they stored their objects by keeping the name of an item, its location, and a photo of the item. This allows users to view their items at a glance and to search for any specific objects. Since manual data entry is tedious, we wanted to use machine learning so that inputting objects is as simple as scanning them in front of your phone's camera.

## How we built it
We wrote this Android app in Java, using [Android Studio](https://developer.android.com/studio). We used the [Google Cloud MLKit for Firebase](https://firebase.google.com/docs/ml-kit), specifically the object detection and image labelling capabilities, to detect and identify items from the phone's camera.

## Challenges we ran into
Since this app is designed to simplify the process of inputting and storing items, we needed to make important design decisions on how to improve the user experience. We've also faced many technical roadblocks; for example, sometimes we'd realize that the API we were using didn't align exactly with what we wanted, so we combined various functions in creative ways to achieve our desired functionality. Finally, we also started this project having no prior hackathon experience nor any experience in mobile development or machine learning, so we had to learn quickly to be able to create this project in a limited amount of time.

## Accomplishments that we're proud of
We are proud of having gone from just an idea to a full app in the course of 36 hours, especially since we started with very little experience in building projects. We have explored many new technologies when building this app. We also coordinated very well as a team, having been able to work on different parts of the app and combine everything together at the end.

## What we learned
Throughout the building of this project, we've learned a lot about Android development, object detection/labeling, UI design, machine learning, Firebase, and cloud computing resources.

## What's next for Boxify
One addition we would make to this project would be to store data on the cloud, allowing the data to be synced across devices and to be shared by multiple people. We would also like to improve the search function, clean up the user interface, add the ability to organize objects further (for example, multiple locations), and improve the image detection and labeling to provide more useful results.
