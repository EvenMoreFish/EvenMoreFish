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
```yaml
reload: "<white>Successfully reloaded the plugin."
```

You can slightly change the configuration to choose a type, listed in the examples below.

### Examples

Chat:
```yaml
reload:
  type: chat
  message: "<white>This is a chat message."
```

Action Bar:
```yaml
reload:
  type: action_bar
  message: "<white>This is an action bar message."
```

Title and Subtitle:
```yaml
reload:
  type: title
  message: 
    - "<white>This is a title."
    - "<white>This is a subtitle."
```

Title Only:
```yaml
reload:
  type: title
  message: "<white>This is a title."
```

Subtitle Only:
```yaml
reload:
  type: subtitle
  message: "<white>This is a subtitle."
```

