# Embedded optimization mods

Drop **release** Fabric mod jars for Minecraft 1.21.11 into this folder.
The build copies every `*.jar` into `META-INF/jars/` inside the LinkenVisuals
jar and adds a Fabric Jar-in-Jar entry to `fabric.mod.json`. Fabric Loader then
loads them together with the client; no manual mods-folder copy is needed.

Rules:

- Use release jars (already remapped to intermediary), not `-dev`, `-sources`
  or local development builds.
- The mod's license must allow redistribution inside LinkenVisuals.
- Do not add two jars with the same Fabric mod id; the client will refuse to
  start with `Duplicate mod`.
- The included `linkenvisuals-test-optimization-1.0.0.jar` is a metadata-only
  smoke test owned by this project. It is intentionally a no-op.
