# Gist
A mobile app which works as a message sender/relayer during disasters to facilitate communication in a low/no network scenario.

# Explanation
The app, on first run, collects user's identification data (like name, contact, picture, etc) which will be stored on the cloud.
Then, the app remains pretty dormant in the background. It's only task being determination of the approximate area the user is in.
The app starts it's function when there is a public alert (received directly over the phone or a trigger from the cloud the app is attached to) of any risk (any predicted/impending disaster) or a disaster in the user's area.
This starts the following functions in the app:
1. Gathering GPS location of user at intermittent (but frequent) times, and sending them to cloud till there is a connection.
2. Looking for SOS signals (explained below).
3. Enabling the user to send an SOS (explained below).

An SOS here is a message that the app will send (repeatedly over local networks, once over the internet) which contains the user's critical information like last ID, last location and updatedAt of message.

All the apps will then transmit a detected SOS message to other devices over local network or once over the internet. This makes a relay, till the message reaches the internet.

This enables easier search and rescue operations (the SOS messages relayed over local networks can be accessed by anyone, in the assumption that every capable human is willing to help another in need).
This can also help in case of missing person's case (by knowing the last known location of the user).

*Here, sending message to the internet implies to the deployed cloud server.
