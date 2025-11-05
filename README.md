<p align="center">
  <strong>An extension for Noxesium, unlocking powerful new server-side controls for an enhanced multiplayer experience.</strong>
</p>

<p align="center">
  <a href="https://modrinth.com/mod/showdium"><img src="https://img.shields.io/modrinth/v/showdium?color=00AF5C&label=Modrinth&style=for-the-badge" alt="Modrinth"></a>
  <a href="https://github.com/Fe4thers/showdium/actions"><img src="https://img.shields.io/github/actions/workflow/status/Fe4thers/showdium/build.yml?branch=main&style=for-the-badge" alt="Build Status"></a>
  <a href="https://github.com/Fe4thers/showdium/blob/main/LICENSE"><img src="https://img.shields.io/github/license/Fe4thers/showdium?style=for-the-badge&color=blue" alt="License"></a>
</p>
About Showdium

Showdium is a Fabric mod designed as a powerful add-on for the Noxesium framework. While Noxesium provides a fantastic foundation for improving the multiplayer experience, Showdium builds upon it by providing server developers with an expanded toolkit to create more controlled, immersive, and unique gameplay mechanics.

This mod is built with server creators in mind, allowing them to change client-side behavior that is normally outside of their control.
Prerequisites

To use Showdium, you will need the following installed on your client:

- Minecraft
- Fabric Loader
- Fabric API
- Noxesium

Installation

- Download the latest version of Showdium from Modrinth, or the GitHub Releases page.
    Place the downloaded .jar file into your mods folder.
    Ensure all prerequisites (Fabric, Fabric API, Noxesium) are also in your mods folder.
    Launch the game. Showdium will be active on any server that utilizes its features.

✨ Features

Showdium introduces a suite of features that can be enabled and controlled by a server.
🧱 World Interaction

- Client-Side NoBlockInteraction
        The server can send a list of blocks that players are forbidden to interact with. Showdium ensures the client provides instant visual feedback by not rendering the interaction (e.g., a trapdoor flipping), even before the server has time to cancel the event.


- Structure Voids with Collision
        When enabled by the server, structure_void blocks gain a full, solid hitbox for players only. All other entities and projectiles can pass through them. This is perfect for creating invisible walls to guide players without interfering with gameplay elements like arrows or mob pathfinding.

🏃 Player Movement & Actions

- Piston-Powered Player Knockback
        This feature grants all blocks pushed by pistons the knockback properties of slime blocks. It's ideal for creating dynamic parkour, unique player transport systems, or contraptions, working best when server-side no-clip is managed.


- Disable Player Attacking
        The server can completely disable a player's ability to attack any entity. This client-side prevention ensure that players do not lose any momentum from sprint-hitting.


- Remove Elytra Hops
        Forces a player's elytra to instantly deactivate the moment they touch the ground. This standardizes elytra behavior across all player latencies, removing the "ping-hop" advantage and creating a more fair and predictable experience in competitive games.


- Force Player Perspective
        Allows the server to lock a player into a specific perspective (First Person, Third Person, etc.). The player cannot change this setting until the server releases the lock.

🎧 Audio & Visual

- Disable Subtitles
        A simple but effective server-side toggle to disable the client from receiving and displaying subtitles, allowing for more controlled audio-visual experiences.


- Customizable Loading Screen
        Showdium replaces the default loading screen with a custom one. Future plans include allowing servers to provide a resource pack to completely customize this screen for truly branded experiences.

⌨️ Advanced Keybind System

Showdium adds a comprehensive, server-driven keybinding system.

- 10 Custom Keybinds: Adds 10 new, unbound keybinds to the options menu. Servers can instruct players to bind them, and their display names can be changed via a resource pack.
    Client-Side & Server-Side Logic: Keybinds can be registered to perform logic entirely on the client (e.g., a launchpad effect) for instant feedback, or simply to notify the server of a key press.
    Customizable Triggers: Configure keybinds to behave differently:
        Spam/Hold: Continuously sends packets while the key is held down.
        Rising/Falling Edge: Sends a distinct packet on key press and another on key release.
    Configurable Delay: A built-in delay can be set to prevent packet spam and manage server load.
    Future development will add an sound when the delay is done

👨‍💻 For Server Developers

Integrating Showdium is a matter of sending custom network packets from your server-side plugin

To see a practical, working implementation of every feature, it's highly recommend examining the TestListener.java class included in this repository. It serves as a comprehensive, hands-on example for activating each piece of Showdium's functionality and is the best starting point for development.

A feature planned for the future is:
LocationPingSystem

smth with api here
