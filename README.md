# **Habesha Pool**

An Android application designed to calculate and manage scores for up to four players in a game of Straight Pool. Straight Pool is a cue‑sport where players pocket balls sequentially, earning points for each successful shot while competing for the highest score. Habesha Pool provides a clean, intuitive interface for tracking scores, fouls, scratches, shot order, early win/loss indicators, and full game history with minimal manual input.

---

## **Overview**

Habesha Pool simplifies scorekeeping for Straight Pool by automating calculations, detecting early winners/losers, maintaining complete game history, and intelligently managing player order between games. The interface is optimized for clarity and speed, ensuring players can focus on the game rather than bookkeeping.

The app now supports **Amharic**, includes **unit tests**, features a **unified theme** that displays consistently regardless of system dark mode, and uses **edge‑to‑edge UI** for a modern look.

---

## **Features**

### **Core Gameplay**
- Supports **2, 3, or 4 players**
- Customizable player names
- Real‑time tracking of:
  - Scores
  - Fouls
  - Scratches
  - Game history
  - Balls remaining in play
- HD ball images for balls **3 through 15**
- Automatic score updates and ball removal
- Undo functionality for the most recent action

---

#### **Early Win/Loss Detection**
The app analyzes score differentials and remaining balls to determine when a winner or loser is mathematically guaranteed. Early indicators update live, helping players know the game outcome ahead of time.

#### **Shot Order by Rank (Auto‑Reordering Players)**
After each game, players are automatically rearranged based on their final ranking. No more manually swapping names or reordering players between rounds.

#### **Aggregate Score Calculator (2‑Player Mode)**
When playing with two players, the app provides a combined aggregate score to eliminate duplicate calculations and simplify long‑form match tracking.

#### **Amharic Language Support**
The entire app can now be used in **Amharic**, making it accessible to a wider audience.

#### **Unit Tests Added**
Core scoring logic, foul handling, scratch behavior, and undo operations are now covered by unit tests for improved reliability and maintainability.

#### **Unified Theme + Edge‑to‑Edge UI**
The app now uses a custom unified theme that displays consistently regardless of the user’s system theme. Edge‑to‑edge layout provides a modern, immersive appearance.

#### **Improved Foul Logic**
If **ball 3** (the break ball) is still on the table, the **Foul** button behaves exactly like **Scratch**, automatically deducting 4 points. This reduces unnecessary taps and speeds up gameplay.

---

## **How the App Works**

### **Player Setup**
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

#### Scratches
- Tap **Scratch** to automatically deduct the value of the current ball in play.
- This is a convenience shortcut that avoids tapping both Foul and the ball.

#### Undo
- Tap **Undo** to revert the most recent scoring, foul, or scratch action.
- The app restores the previous score, history entry, and ball placement.

## **Game Management**

### **Early Win/Loss Detection**
The app continuously evaluates:
- Remaining balls
- Score differentials
- Maximum possible comeback

If a player cannot catch up—or if a player is guaranteed to win—the app displays early indicators.

---

### **Shot Order by Rank**
At the end of each game:
- Players are automatically reordered based on final ranking.
- The next game starts with the correct shooting order.

---

### **Start a New Game**
- Clears all data and prompts for player count and names.

---

### **Restart Current Game**
Resets scores and balls but keeps:
- Player names
- Player order
- Number of players

---

## **Maintainability Guide**

### Code Structure
- The project is built in Android Studio using:
  - Java for backend logic
  - XML for UI layout

- Core components include:
  - Activity classes for UI and interaction handling
  - Data models for players, scores, and game state
  - Utility classes for score calculations and ball management
  - Unit tests

---

### **Recommended Practices**
- Keep UI and scoring logic separated.
- Use consistent naming conventions.
- Centralize rule logic for easy updates.
- Maintain a single source of truth for game state.

---

### **Extensibility**
Future enhancements may include:
- Additional pool game types
- Persistent storage for past games
- Animations and enhanced visuals
- Custom rule variations

---

## **Usability Guide**

### **Interface Principles**
- Primary actions placed directly under each player’s name
- HD ball images reduce misidentification
- Minimal interruptions during gameplay
- Buttons sized for fast, accurate tapping

---

### **User Flow**
1. Select number of players.
2. Enter names.
3. Begin scoring using the action buttons and ball images.
4. Monitor game history under each player's section.
5. Use Undo for corrections.
6. End-of-game alert displays results.
7. Start a new game or restart the current one as needed.

---

### **Error Prevention**
- Undo functionality allows quick correction of mistakes.
- Visual ball tracking prevents duplicate scoring.
- Clear separation of scoring, fouling, and scratching actions reduces confusion.

---
