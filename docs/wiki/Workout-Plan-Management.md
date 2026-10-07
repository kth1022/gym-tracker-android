# Workout Plan Management

Knurl supports importing workout plans, tracking scheduled workout days, clearing days to rest days, and loading an existing weekday plan onto another date.

## Import A Workout Plan

Use a workbook that follows the Knurl plan format. If the import fails with `Use a workout plan workbook`, export a blank template from the app and compare the workbook sheet names and column headers.

### Imported Plans Are Not Retroactive

From v1.9, an imported plan takes effect from the day you import it. It never rewrites days you have already been through.

- The plan starts on the day you import it.
- If that day already has a logged workout, the plan starts on the next day the incoming plan schedules a workout, so the workout you already logged is not replaced.
- Dates before that start point keep whatever they already had, including their original planned workout and any logged data.
- After the import, the toast shows the date the plan starts and how many past days were left unchanged.

A workbook can safely cover a date range that starts in the past. Only the part from the start date forward is applied. If a workbook covers nothing but dates that have already passed, the import is refused and nothing changes.

## Logging Sets

A set is a draft until you press **Log set**.

- Typing a weight or reps, or using the +/- steppers, does not complete the set or the workout.
- Press **Log set** to record it. Only then does the day count as Complete.
- Press **Edit** on a logged set to reopen it and change the values, then log it again.
- Reps are required. Weight is optional, so bodyweight work such as push-ups and pull-ups logs without inventing a weight, and shows as `Bodyweight × 20`.

Each exercise card shows the planned prescription, for example `Target: 3 sets × 10-12`, next to the current set counter.

## Exercise Notes And Editing

Open an exercise's menu to add a note for that exercise. The note is separate from workout notes and appears the next time the exercise is performed. The same menu can edit the exercise name and description. After saving, Knurl asks whether to apply the change to matching weekdays from the selected date forward.

## Timer Controls

The Log screen's rest timer dock has one open-ended **Start Rest** control with pause, resume, and reset controls. Tap **Done** when rest is over. The dock flashes at each 30-second marker. Timed exercise rows also use the in-app timer; **Record** saves the held time as the reps value.

Session details show the daily workout elapsed clock after the first logged set or a completed warm-up before any exercise is logged. Only exercise logging advances the clock after it starts; changing warm-up status later or adding weight, sleep, or notes does not move it. Pause and resume exclude an interruption from the elapsed time. Reset clears only the clock and leaves logged data intact.

## Export A Blank Plan Template

Use the app's blank workout plan template export when creating a new plan outside the app. The exported workbook uses the sheet names and headers accepted by the importer.

## Clear A Day To Rest Day

Use this when a planned workout cannot be completed on its scheduled date. The selected date becomes a rest day while the original planned workout remains available as a weekday plan.

## Load A Weekday Plan Onto Another Date

Use this when making up a missed workout on a rest or empty day.

Example:

1. Wednesday cannot be completed.
2. Clear Wednesday to a rest day.
3. Open Friday.
4. Load the Wednesday workout plan onto Friday.

The load plan day selector should show weekdays from the current plan, not every calendar date.
