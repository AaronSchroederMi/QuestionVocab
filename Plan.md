# Initial Plan -- Questions to Answer
## 1. What are the functionalities?
### Add a `file` of questions with answers
- [x] Can load json with fitting struktur
- [x] Can load multiple files without issue
- [x] json is loaded with gson into a classic op struktur `(Questions.class, Log.class)`
### Add a `directory` with images inside of it
- [x] can add one directory with images
  - only accepts jpg, jpeg and png
- [x] displays the amount of loaded images
- [x] displays what directory is currently loaded
### Show loaded questions (Numeric)
- [ ] displays the amount of loaded questions
- [ ] displays what files are currently loaded
### Show the most suitable question to user
- [x] chooses a random question from the upper quarter of the `questionList` (sorted by confidence)
### Reset `file` stats
- [ ] clear logs of a file
- [ ] clear logs of a question
- [ ] clear logs of all loaded files
### Show progress on a `file / files`, separate page
...
### Choose one of the options (evaluate: Correct | Incorrect)
- [x] selection buttons are in place
- [x] need to evaluate if answer was correct
- [x] update log in Json accordingly
### Clear loaded `file / files`
- [x] can remove directory from a menu
### Add multiple `question files`
- [x] is implemented and working
### Optional: load random image from `directory` for each question
- [x] a new image from a selectable directory is chosen per question
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