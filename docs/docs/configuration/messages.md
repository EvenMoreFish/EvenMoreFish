---
title: Messages
---

## The file

This is the file for modifying messages sent to the players from the plugin: if you're running the server locally or
viewing the files on a panel through FTP/SFTP, you can use a text-editor such
as [Notepad++](https://notepad-plus-plus.org/).

## The Wiki

***

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

As of 2.0.12, it is now possible to choose a message type.

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
