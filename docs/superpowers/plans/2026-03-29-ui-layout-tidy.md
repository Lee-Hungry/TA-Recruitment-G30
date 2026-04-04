# UI Layout Tidy Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the visible Sprint 1 Swing pages more orderly without changing system behavior.

**Architecture:** Keep all logic and data flow unchanged, and confine the work to UI classes plus shared theme helpers. Improve consistency by centralizing control sizing and replacing overly stretched layout containers with more stable Swing panel structures.

**Tech Stack:** Java 21, Swing, Maven, JUnit 5

---

### Task 1: Add shared spacing and control helpers

**Files:**
- Modify: `src/main/java/com/group30/tarecruitment/ui/UiTheme.java`

- [ ] **Step 1: Add reusable size and spacing helpers**
- [ ] **Step 2: Expose helpers for uniform text field and text area sizing**
- [ ] **Step 3: Keep existing colors/fonts unchanged unless needed for alignment support**

### Task 2: Tighten login and registration forms

**Files:**
- Modify: `src/main/java/com/group30/tarecruitment/ui/LoginFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/TaRegistrationPanel.java`

- [ ] **Step 1: Center the login card and prevent awkward full-height stretching**
- [ ] **Step 2: Normalize input stack spacing and button widths**
- [ ] **Step 3: Make the registration dialog use matching field rhythm and footer alignment**

### Task 3: Tidy TA and MO dashboard content layout

**Files:**
- Modify: `src/main/java/com/group30/tarecruitment/ui/TaDashboardFrame.java`
- Modify: `src/main/java/com/group30/tarecruitment/ui/MoDashboardFrame.java`

- [ ] **Step 1: Normalize top bar spacing and content padding**
- [ ] **Step 2: Replace stretched form grids with more stable grouped sections where needed**
- [ ] **Step 3: Align list/table/detail areas to a consistent top edge and card spacing**

### Task 4: Tidy admin dashboard layout and verify

**Files:**
- Modify: `src/main/java/com/group30/tarecruitment/ui/AdminDashboardFrame.java`

- [ ] **Step 1: Align top bar and main card spacing with the other dashboards**
- [ ] **Step 2: Balance the table pane and message pane widths/heights**
- [ ] **Step 3: Run `mvn -q test`**
- [ ] **Step 4: Run `mvn -q -DskipTests package`**
