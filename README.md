# Fly Limit 30 — Fabric 1.21.1

Limits upward movement of every `MobEntity` to 30 blocks above its local ground/takeoff height.

## Behavior
- 30-block vertical limit from the height where the mob was grounded.
- Terrain height is local: mountains/hills get their own limit.
- Walking/climbing upward is allowed; landing establishes a new reference height.
- The mod does not create invisible blocks or a physical ceiling.
- It does not force mobs downward. At the ceiling it only cancels upward velocity, so normal mob AI can decide when to descend.
- Teleportation is not intercepted. A mob can be teleported above the limit.
- Applies to vanilla mobs and modded mobs that extend `MobEntity`.
- Java 21, Minecraft 1.21.1, Fabric.

## Build
Install a recent Gradle 8.x distribution and run:

    gradlew build

The jar will be in `build/libs/`.
