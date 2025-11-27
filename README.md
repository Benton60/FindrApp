# What is Findr?

Findr is a social media app that serves posts to users with an algorithm based on location.
It was created as a playground to build/practice my knowledge of the Spring Framework,
but quickly evolved into a much more complex project that is now spread across two separate git repositories.
This repo is the backend API.

The back-end is linked here.
https://github.com/Benton60/FindrAPI

## How Findr began
Findr began as the product of boredom during my freshman year of college. I was looking for side projects that would stretch my talents
and decided I wanted a project that stretched across a full tech stack. A social-media platform was the first thing that came to mind.
The project was never meant to become a fully fledged product, instead it was meant to provide a means to experiment with new technologies
in a production-scale environment. Many of my previous projects, i.e. both chess engines and the notes app, only required one development suite.

Chess Engines - Entirely backend development

Notes App - Entirely Front-end Android development

I wanted a project that utilized everything at once, from the database to the UI. I settled on the Spring Framework as the backend due to my
previous experience with java and more specifically the JDBC. I chose an android client as the front end for the same reason. The project didn't begin
as a way to learn new technologies but more to see how the technologies I already used work together to deliver a full-scale product.
As the development process continued, however, I began to find new ideas and technologies that I wanted to implement, and Findr has continued to grow since then.

##APP Specifics
FindrApp is the git repository for the frontend. It is written as a android client. You'll quickly notice while most files are in kotlin, some files are written in java.
This is due to Java and Kotlin being nearly perfectly interoperable, so when I was creating my entity classes it was simpler to directly copy the java files from the API 
rather than try to rewrite them in kotlin. Also I find I am more comfortable tackling new concepts in java so the Retrofit client and LocationConfig classes are written 
in java. 

###API Interactions
The Findr App connects to the backend API using Retrofit. All API credentials should be stored in a 
local.properties file. You can find the specific API endpoints that are available and their function call requirements in the "com/findr/findr/api/ApiService.kt" file. 
Throughout the code base you will find 'retroFitClient' calls, when you see it lowercase it has been built using the APiService interface. And finally the retrofit client 
has several extra functions to help with credential checking you can find those functions in the RetrofitClient.java file next to the ApiService.kt file.

###Fragments
Rather than switch entire activities when switching between the map and home screens. I used fragments. Using fragments allowed me to keep a consistent button ribbon
along the bottom and the profile bar across the top. You can see the list of fragments in the "com/findr/findr/fragments" folder. 
Home Fragment - displays posts and friends
Camera Fragment - allows the user to take pictures and make posts as well as can open the Photo Preview Fragment that sends posts to the API
Map Fragment - this currently only displays the locations of the users friends. It connects to Googles Map API.
Video Fragment - is currently unused i might get rid of it. it was originally supposed to be a video feed but i might change my mind.


