# Wiki Mirroring

The source copy of the Gym Tracker wiki lives in `docs/wiki/`. This keeps documentation changes versioned with the app repository.

## Mirror Target

GitHub Wiki repository:

```text
https://github.com/kth1022/gym-tracker-android.wiki.git
```

## Mirror Rule

Mirror `docs/wiki/` to the GitHub Wiki when:

- a public release is approved
- a user-facing workflow changes
- troubleshooting or recovery steps change
- the maintainer asks for a wiki refresh

## Manual Mirror Steps

1. Enable the Wiki feature in the GitHub repository settings if it is not already enabled.
2. Clone the wiki repository.
3. Copy the Markdown files from `docs/wiki/` into the wiki repository root.
4. Commit the copied files.
5. Push to the wiki repository.

## Notes

GitHub Wiki pages use file names as page slugs. Keep names stable so links do not break.

