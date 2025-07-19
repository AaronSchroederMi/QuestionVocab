# Initial Plan -- Questions to Answer
## 1. What are the functionalities?
### Add a `file` of questions with answers
### Add a `directory` with images inside of it
### Show loaded questions (Numeric)
### Show the most suitable question to user
### Reset `file` stats
### Show progress on a `file / files`, separate page
### Choose one of the options (evaluate: Correct | Incorrect)
### Clear loaded `file / files`
### Add multiple `question files`
### Optional: load random image from `directory` for each question
## 2. What is the form of the File that is read in?
**Geplant ist eine JSON die folgendes Enthält:** 
- Die Frage
- Antwortmöglichkeiten (A-D)
- Richtige Antwort
- Liste von Log je Frage (Timestamp richtig/falsch)

Bsp.:
```JSON
[
  {
    "id": 1,
    "question": "Was ist die Hauptstadt von Deutschland?",
    "answers": {
      "A": "Berlin",
      "B": "München",
      "C": "Hamburg",
      "D": "Köln"
    },
    "correctAnswer": "A",
    "logs": [
      {"timestamp": "2025-07-19T10:23:00", "correct": true},
      {"timestamp": "2025-07-19T10:23:00", "correct": false}
    ]
  },
  ...
]
```
## 3. How should stats be shown?
### Asked vs. Unasked ratio (Pie-Chart)
#### Correctly vs. Incorrectly ratio (Pie-Chart)
### Overall eval on Progress (Percentage)
### Progress over Time (Bar-Chart and Trendlinie)
### Time Spend on File (Duration)
### List questions, with confidence value, progress
## 4. Technology to use?
### JavaFX SDK
### jpackage
### Install4j