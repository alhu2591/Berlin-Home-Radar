# Engineering Fix Summary

## Root cause
The GitHub Actions build was failing in the `:app:detekt` task because the Detekt configuration declared `StringTemplate` under the `style` rule set:

```yaml
style:
  StringTemplate:
    active: false
```

For Detekt `1.23.6`, `StringTemplate` belongs to the **formatting** rule set, not **style**. The formatting rules are also **not included by default** in the Gradle plugin and must be added explicitly via `detektPlugins(...)`.

## Changes applied
1. Added the Detekt formatting plugin dependency in `app/build.gradle.kts`:

```kotlin
detektPlugins("io.gitlab.arturbosch.detekt:detekt-formatting:1.23.6")
```

2. Moved the rule configuration from `style` to `formatting` in `config/detekt/detekt.yml`:

```yaml
formatting:
  StringTemplate:
    active: false
```

3. Removed the invalid key from the `style` block.

## Expected outcome
- `:app:detekt` should stop failing with:
  - `Property 'style>StringTemplate' is misspelled or does not exist.`
- The configuration becomes aligned with Detekt `1.23.6` semantics.

## Notes
- The `Node.js 20 is deprecated` message in the workflow log is a warning, not the build-breaking error.
- The Android SDK license prompts shown in the log were completed successfully and are not the final failure.
