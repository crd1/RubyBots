# RubyBots
This is an engine that enables ruby programs to compete. The concept is similar to the all-time classic CoreWars: you've got a circular battlefield and any number of bots. Winner is last man standing.

## Building
Build the engine: `mvn clean compile assembly:single`

Run the engine in demo mode: `java -jar target\rubybots-0.0.1-SNAPSHOT-jar-with-dependencies.jar`

## Usage
A bot is any valid ruby program. To choose bots simply pass them as arguments to the engine:
`java -jar target\rubybots-0.0.1-SNAPSHOT-jar-with-dependencies.jar bot1.rb bot2.rb bot3.rb`

You can also name a directory:
`java -jar target\rubybots-0.0.1-SNAPSHOT-jar-with-dependencies.jar botDirectory`

This will just add files that match the pattern `*.rb` and will *not* proceed recursively.

## Writing Bots
Bots can use the RubyBot API. At execution time there will be an object `$context`. On this you can read:
+ botNumber // your bot's id
+ round // the current round
+ numberOfBots // the total number of bots competing
+ battlefield // An object representing the battlefield at the time the round started

Also you can call log(message) on this object. This is explicitly encouraged.

On the Battlefield object you can call:
+ getSize() // returns the battlefield's size
+ getMyPosition() // returns your bot's position *at the time the round started*
+ whoIsAtPosition(position) // returns the id of the bot that was at the given position when the round started or nil
+ mine(position) // places a mine at the specified position. If this field is taken no action will be performed. If a bot moves to a mined field it is destroyed.
+ fire(position) // fires as the specified position. If there is a bot on this field it will be destroyed.
+ move() // moves your bot one step forward

## Rules of the game
+ The field is circular. Its size depends on the number of bots.
+ Your bot may perform up to 4 actions each round. An action is one of mine(), fire() or move().
+ Your bot's actions will be performed in the order that they were queued in, yet there will be no guarantees concerning the absolute order in that all the bots' actions will be executed.
+ The battle ends when there is only one bot left. The battle can end without winner if in the last round all bots are killed.
+ Suicide is legal.

## Warning
The bots' code will be executed as-is. This implies that bots are able to delete files on your hard disk, for example.
In a future version there will be a ruleset for the Java Security Manager that can be used. At the moment, enabling Java Security disables bot execution which renders this engine kind of pointless.

# Have fun!