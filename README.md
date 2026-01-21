# Habesha Pool  
An Android application designed to calculate the scores of up to four player in a game of Straight Pool. Straight Pool is a game in which players pocket balls sequentially, earning points for each successful shot while competing to obtain the highest score possible. Habesha Pool provides a clean, intuitive interface for managing this style of play, allowing users to track scores, fouls, scratches, and full game history with minimal manual input.

---

## Overview
Habesha Pool is designed to simplify scorekeeping for Straight Pool by automating calculations and maintaining a complete record of gameplay. Users can select the number of players, assign names, track every scoring event, and view a detailed history for each player. The interface is optimized for clarity and ease of use, reducing the need for manual scorekeeping and eliminating common tracking errors.

---

## Features
- Supports 2, 3, or 4 players per game.
- Customizable player names via text fields.
- Real-time tracking of:
  - Scores  
  - Fouls  
  - Scratches  
  - Game history  
- Interactive "Balls Still in Play" section showing balls 3 through 15 as HD image buttons.
- Automatic score updates based on selected ball and action.
- Undo functionality to revert the most recent action.
- End-of-game alert displaying the winner and final scores.
- Options to start a new game or restart the current game.

---

## How the App Works

### Player Setup
1. Select the number of players (2, 3, or 4).
2. Enter each player's name in the provided text fields.
3. Begin the game with all balls (3–15) displayed in the "Balls Still in Play" section.

### Gameplay Interaction
Each player box contains:
- The player's name.
- A text-based game history log.
- Four action buttons:
  - **Score**
  - **Foul**
  - **Scratch**
  - **Undo**

#### Scoring
- Tap **Score**, then tap the scored ball from the "Balls Still in Play" section.
- The app will:
  - Add the ball's value to the player's score.
  - Append the event to the player's game history.
  - Remove the ball from the "Balls Still in Play" section.
  - Display the ball under the player's action buttons as a visual record.

#### Fouls
- Tap **Foul**, then tap the fouled ball from the "Balls Still in Play" section.
- The app will:
  - Deduct the appropriate value based on Straight Pool rules.
  - Update the player's score and history.
  - Remove the ball from play and record it under the player's section.

#### Scratches
- Tap **Scratch** to automatically deduct the value of the current ball in play.
- This is a convenience shortcut that avoids tapping both Foul and the ball.

#### Undo
- Tap **Undo** to revert the most recent scoring, foul, or scratch action.
- The app restores the previous score, history entry, and ball placement.

---

## Game Management

### Start a New Game
- Clears all scores, histories, and ball placements.
- Prompts the user to select the number of players again.
- Allows new names to be entered for the next game.

### Restart Current Game
- Resets scores, histories, and ball placements.
- Retains:
  - Player names
  - Number of players
- Useful when the same group plays multiple rounds.

---

## Maintainability Guide

### Code Structure
- The project is built in Android Studio using:
  - Java for backend logic
  - XML for UI layout
- Core components include:
  - Activity classes for UI and interaction handling
  - Data models for players, scores, and game state
  - Utility classes for score calculations and ball management

### Recommended Practices
- Keep UI logic separated from scoring logic to maintain clarity.
- Use consistent naming conventions for player objects, ball identifiers, and history entries.
- Centralize score calculation rules to simplify updates or rule adjustments.
- Maintain a single source of truth for game state to avoid inconsistencies.

### Extensibility
The app can be extended by:
- Adding support for additional pool game types.
- Introducing persistent storage for saving past games.
- Implementing animations or enhanced visual feedback.
- Adding settings for custom rule variations.

---

## Usability Guide

### Interface Principles
- All primary actions (score, foul, scratch, undo) are placed directly under each player's name for quick access.
- Balls are displayed in sequential order with HD images to reduce misidentification.
- Alerts and prompts are used sparingly to avoid interrupting gameplay flow.
- Buttons are sized for easy tapping during live games.

### User Flow
1. Select number of players.
2. Enter names.
3. Begin scoring using the action buttons and ball images.
4. Monitor game history under each player's section.
5. Use Undo for corrections.
6. End-of-game alert displays results.
7. Start a new game or restart the current one as needed.

### Error Prevention
- Undo functionality allows quick correction of mistakes.
- Visual ball tracking prevents duplicate scoring.
- Clear separation of scoring, fouling, and scratching actions reduces confusion.