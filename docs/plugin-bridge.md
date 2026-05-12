# CloneBang Paper Plugin Bridge

This document describes the compatibility layer reserved for the future Paper plugin.

The mod does not require the plugin. If no plugin is detected, CloneBang keeps using its local/client-side fallback behavior.

## Protocol

- Protocol version: `1`
- Client to server channel: `clonebang:plugin_hello`
- Server to client channel: `clonebang:plugin_status`

## Client Hello Payload

Sent by the mod after joining a server, only when the server advertises the channel as sendable.

Binary fields:

1. VarInt `protocolVersion`
2. UTF string `modVersion` max 64 chars

## Plugin Status Payload

Sent by the future Paper plugin to announce support and feature flags.

Binary fields:

1. VarInt `protocolVersion`
2. UTF string `pluginVersion` max 64 chars
3. Boolean `safeMode`
4. Boolean `optimizedCloneCommands`
5. Boolean `structureStorageApi`

## Intended Use

The Paper plugin should stay an optimization and safety API, not a full replacement for the mod.

Planned responsibilities:

- validate clone/placement requests server-side
- expose safer server storage hooks
- reduce crash risk from unsupported server behavior
- provide feature flags so the mod can choose safer flows

The mod settings screen already displays whether the server API plugin is connected, missing, waiting, or incompatible.
