# Backlog And GitHub Issues

Gym Tracker uses `FEATURE_BACKLOG.md` for maintainer planning and GitHub Issues for user-visible tracking.

## Rule

Every backlog item should have a matching GitHub Issue.

This lets users check whether a bug or feature request is already known before they submit another report.

## Categories

- Bugs use the `bug` label.
- Features use the `feature request` and `enhancement` labels.
- General feedback can use `user feedback` when it is not yet a clear bug or feature.

## Backlog Format

Each backlog item should include:

```markdown
- [ ] Feature or bug title
  - GitHub Issue: #123
  - Category: feature / bug
  - Source: user / trainer / testing / bug report
  - Notes: brief description, workflow, or expected behavior
  - Priority: low / medium / high
```

## Workflow

1. Add or identify the backlog item.
2. Search open GitHub Issues for duplicates.
3. Create a new GitHub Issue if one does not already exist.
4. Apply the correct labels.
5. Add the issue number back to `FEATURE_BACKLOG.md`.
6. When the item ships, move it to `Completed` and close the GitHub Issue as completed.

