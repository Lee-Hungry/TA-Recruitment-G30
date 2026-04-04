# UI Layout Tidy Design

## Goal

Make the currently visible Sprint 1 Swing pages look more orderly by tightening spacing, aligning headers and action areas, and making forms and content cards feel visually consistent.

## Scope

This change only affects presentation in the existing Swing UI. It does not change authentication, CSV persistence, routing, or story scope.

Pages in scope:

- `LoginFrame`
- `TaRegistrationPanel`
- `TaDashboardFrame`
- `MoDashboardFrame`
- `AdminDashboardFrame`
- shared UI styling in `UiTheme`

## Design

### Shared layout rhythm

- Add a small set of reusable layout helpers in `UiTheme` for consistent field heights, card gaps, and page section spacing.
- Keep the existing white-card-on-light-background visual direction.
- Use fixed vertical spacing between labels, controls, section headers, and footer buttons.

### Login and registration

- Center the login card within the right pane instead of letting it stretch awkwardly.
- Keep labels and controls in one clean vertical stack with uniform control height.
- Increase breathing room between title, subtitle, inputs, and call-to-action buttons.
- Make the registration window use the same spacing rules and button alignment.

### TA dashboard

- Regularize the top bar height and the content gap below it.
- Make the profile editor feel like a true two-column form by keeping label/control alignment stable and text areas at matched heights.
- Tighten the jobs split view so the list and detail panel align at the top and the detail panel has clearer spacing between title, metadata, and content blocks.

### MO dashboard

- Align the job posting form into a consistent two-column composition rather than a stretched grid.
- Keep input rows evenly sized and move the publish action into a stable footer area.
- Make the postings table card align visually with the dashboard metrics card and top bar spacing.

### Admin dashboard

- Make the search/top bar spacing consistent with the other dashboards.
- Balance the main table panel and side message panel so they read as one organized section.

## Risks and mitigations

- Swing layouts can shift unexpectedly when nested panels mix layout managers.
  Mitigation: use fewer stretched `GridLayout` blocks for forms and replace them with more controlled boxed or bordered sections where necessary.

- Pure layout work is difficult to test automatically.
  Mitigation: keep behavior untouched and verify by successful compile and existing automated tests.
