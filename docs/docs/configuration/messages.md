---
title: Messages
---

## Tags

It's possible to add "tags" to the messages, these cause the message to be formatted differently or to act differently.

There are only two tags: `-s` and `[noPrefix]`.

#### -s

This variable simply causes the message to not be sent. You might want to do this if you don't want players to see when
someone overtakes them in the competition leaderboard.

#### [noPrefix]

This stops the [EvenMoreFish] prefix (or your server's equivalent) from displaying before the message. For lists like /emf
help, you have to add one for each line of the list.

***

## Message Types

A message type can be specified to choose where the message is sent to. Only one type can be added per message.

While a normal chat message can be configured like this:
```
reload: "<white>Successfully reloaded the plugin."
```

You can slightly change the configuration to choose a type like this:
```
reload:
  type: subtitle
  message: "<white>Successfully reloaded the plugin."
```
The list of currently supported types are:
- chat
- action_bar
- title
- subtitle
