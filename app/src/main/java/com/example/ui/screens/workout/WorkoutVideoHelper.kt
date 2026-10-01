package com.example.ui.screens.workout

data class SetVideoTutorial(
    val setNumber: Int,
    val title: String,
    val youtubeLink: String,
    val youtubeId: String,
    val focusTip: String
)

data class ExerciseVideoGuide(
    val exerciseName: String,
    val videoUrl: String,
    val youtubeId: String,
    val directYouTubeLink: String,
    val targetMuscles: String,
    val formCues: List<String>,
    val commonMistakes: List<String>,
    val setTutorials: List<SetVideoTutorial>
)

object WorkoutVideoHelper {

    private val guides = listOf(
        ExerciseVideoGuide(
            exerciseName = "Explosive Push-ups",
            videoUrl = "https://www.youtube-nocookie.com/embed/R_bA_y91r0E",
            youtubeId = "R_bA_y91r0E",
            directYouTubeLink = "https://www.youtube.com/watch?v=R_bA_y91r0E",
            targetMuscles = "Pectorals, Anterior Deltoids, Triceps & Core",
            formCues = listOf(
                "Keep hands just outside shoulder-width on the floor.",
                "Maintain a rigid plank line from head to heels; do not let hips sag.",
                "Lower with control until chest is 1 inch off the ground.",
                "Drive upward explosively so hands leave the floor momentarily."
            ),
            commonMistakes = listOf(
                "Flaring elbows out at 90 degrees (tuck elbows to ~45 degrees).",
                "Arching lower back or piking hips upward.",
                "Bouncing off the floor without controlled eccentric deceleration."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Calisthenics Form & Activation",
                    youtubeLink = "https://www.youtube.com/watch?v=R_bA_y91r0E",
                    youtubeId = "R_bA_y91r0E",
                    focusTip = "Focus on controlled 2-second descent and perfect hand placement."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Explosive Power & Velocity",
                    youtubeLink = "https://www.youtube.com/watch?v=naRaVsFn_g4",
                    youtubeId = "naRaVsFn_g4",
                    focusTip = "Drive forcefully through the floor; aim for maximum push height."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Progression & Core Stability",
                    youtubeLink = "https://www.youtube.com/watch?v=P_jM2c-q-7o",
                    youtubeId = "P_jM2c-q-7o",
                    focusTip = "Brace glutes and abs to technical failure without dropping form."
                )
            )
        ),
        ExerciseVideoGuide(
            exerciseName = "Pike Push-ups / Shoulder Press",
            videoUrl = "https://www.youtube-nocookie.com/embed/qvt_9w7iXoc",
            youtubeId = "qvt_9w7iXoc",
            directYouTubeLink = "https://www.youtube.com/watch?v=qvt_9w7iXoc",
            targetMuscles = "Deltoids (Shoulders), Upper Chest, Triceps & Serratus",
            formCues = listOf(
                "Assume a downward-dog or V-shape with hips held high.",
                "Look back towards feet to keep the cervical spine neutral.",
                "Lower head forward in a tripod trajectory between your hands.",
                "Press forcefully through palms back into the apex of the pike."
            ),
            commonMistakes = listOf(
                "Lowering head straight down between hands instead of forward.",
                "Letting knees bend excessively, reducing shoulder load.",
                "Shrugging shoulders into ears at the top of the press."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Tripod Trajectory Prep",
                    youtubeLink = "https://www.youtube.com/watch?v=qvt_9w7iXoc",
                    youtubeId = "qvt_9w7iXoc",
                    focusTip = "Establish strict hand distance and hip elevation before first rep."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Overhead Press Strength",
                    youtubeLink = "https://www.youtube.com/watch?v=B-aVuyhvLHU",
                    youtubeId = "B-aVuyhvLHU",
                    focusTip = "Lock out shoulders at the top of each repetition with full extension."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Volume & Shoulder Endurance",
                    youtubeLink = "https://www.youtube.com/watch?v=qvt_9w7iXoc",
                    youtubeId = "qvt_9w7iXoc",
                    focusTip = "Maintain controlled pace; do not let elbows flare wide."
                )
            )
        ),
        ExerciseVideoGuide(
            exerciseName = "Bench / Chair Tricep Dips",
            videoUrl = "https://www.youtube-nocookie.com/embed/I7k8P_j3dSY",
            youtubeId = "I7k8P_j3dSY",
            directYouTubeLink = "https://www.youtube.com/watch?v=I7k8P_j3dSY",
            targetMuscles = "Triceps Brachii, Front Shoulders & Chest",
            formCues = listOf(
                "Place palms flat on the edge of a sturdy bench or chair.",
                "Keep back close to the bench as you descend.",
                "Lower until elbows form a 90-degree angle.",
                "Lock out triceps forcefully at the top of each repetition."
            ),
            commonMistakes = listOf(
                "Drifting torso far forward away from the bench (strains shoulders).",
                "Descending past 90 degrees with slumped shoulders.",
                "Failing to fully extend arms at the apex."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Depth & Range of Motion",
                    youtubeLink = "https://www.youtube.com/watch?v=I7k8P_j3dSY",
                    youtubeId = "I7k8P_j3dSY",
                    focusTip = "Descend smoothly to 90 degrees with elbows pointing straight back."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Tricep Peak Contraction",
                    youtubeLink = "https://www.youtube.com/watch?v=D-N2uH_I_0I",
                    youtubeId = "D-N2uH_I_0I",
                    focusTip = "Hold the lockout at the top for 1 full second on each rep."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Calisthenic Burnout",
                    youtubeLink = "https://www.youtube.com/watch?v=I7k8P_j3dSY",
                    youtubeId = "I7k8P_j3dSY",
                    focusTip = "Keep heels planted and maintain strict upright posture."
                )
            )
        ),
        ExerciseVideoGuide(
            exerciseName = "Hanging Knee Raises / Plank",
            videoUrl = "https://www.youtube-nocookie.com/embed/hdng3Nm1x_E",
            youtubeId = "hdng3Nm1x_E",
            directYouTubeLink = "https://www.youtube.com/watch?v=hdng3Nm1x_E",
            targetMuscles = "Rectus Abdominis, Hip Flexors & Obliques",
            formCues = listOf(
                "Engage lats and avoid excessive swinging on the bar.",
                "Roll pelvis upward as knees drive towards chest for full contraction.",
                "Lower legs under strict 2-second control; don't use momentum.",
                "If performing plank: squeeze glutes, brace abs, and hold tension."
            ),
            commonMistakes = listOf(
                "Swinging legs like a pendulum rather than flexing the core.",
                "Only lifting legs with hip flexors without posterior pelvic tilt.",
                "Holding breath during abdominal contraction."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Anti-Swing Control",
                    youtubeLink = "https://www.youtube.com/watch?v=hdng3Nm1x_E",
                    youtubeId = "hdng3Nm1x_E",
                    focusTip = "Eliminate all momentum; tuck pelvis under before curling knees."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Full Abdominal Squeeze",
                    youtubeLink = "https://www.youtube.com/watch?v=hdng3Nm1x_E",
                    youtubeId = "hdng3Nm1x_E",
                    focusTip = "Bring knees all the way to chest height and exhale fully."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Hollow Body / Plank Hold",
                    youtubeLink = "https://www.youtube.com/watch?v=ASdvN_XEl_c",
                    youtubeId = "ASdvN_XEl_c",
                    focusTip = "Squeeze glutes and create maximum abdominal stiffness."
                )
            )
        ),
        ExerciseVideoGuide(
            exerciseName = "Pull-ups / Inverted Rows",
            videoUrl = "https://www.youtube-nocookie.com/embed/g1j1DQgM_kQ",
            youtubeId = "g1j1DQgM_kQ",
            directYouTubeLink = "https://www.youtube.com/watch?v=g1j1DQgM_kQ",
            targetMuscles = "Latissimus Dorsi, Biceps, Rhomboids & Rear Delts",
            formCues = listOf(
                "Grip the bar slightly wider than shoulder-width with overhand grip.",
                "Depress and retract scapulae before bending elbows.",
                "Pull until chin clears the bar comfortably.",
                "Lower under control to a full dead-hang stretch."
            ),
            commonMistakes = listOf(
                "Kicking legs (kipping) instead of using strict back power.",
                "Half-reps (not lowering to full extension or not clearing chin).",
                "Rounding shoulders forward at the top of the pull."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Perfect Pull-Up Technique",
                    youtubeLink = "https://www.youtube.com/watch?v=g1j1DQgM_kQ",
                    youtubeId = "g1j1DQgM_kQ",
                    focusTip = "Initiate movement from the back muscles before bending the arms."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Strict Power Sets",
                    youtubeLink = "https://www.youtube.com/watch?v=eGo4IYlbE5g",
                    youtubeId = "eGo4IYlbE5g",
                    focusTip = "Clear chin over bar and hold top pause for 0.5 seconds."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Eccentric Overload",
                    youtubeLink = "https://www.youtube.com/watch?v=g1j1DQgM_kQ",
                    youtubeId = "g1j1DQgM_kQ",
                    focusTip = "Lower down over 3 slow seconds on each final repetition."
                )
            )
        ),
        ExerciseVideoGuide(
            exerciseName = "Bodyweight Squats / Lunges",
            videoUrl = "https://www.youtube-nocookie.com/embed/aclHkVaku9U",
            youtubeId = "aclHkVaku9U",
            directYouTubeLink = "https://www.youtube.com/watch?v=aclHkVaku9U",
            targetMuscles = "Quadriceps, Glutes, Hamstrings & Calves",
            formCues = listOf(
                "Feet shoulder-width apart, toes angled outward 15-30 degrees.",
                "Hinge hips back while driving knees outward over second toe.",
                "Descend until hip crease is below parallel.",
                "Press through mid-foot and heel to stand tall."
            ),
            commonMistakes = listOf(
                "Knees caving inward (valgus collapse).",
                "Heels lifting off the ground during bottom transition.",
                "Rounding lumbar spine at the bottom (butt wink)."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: The Perfect Squat Mechanics",
                    youtubeLink = "https://www.youtube.com/watch?v=aclHkVaku9U",
                    youtubeId = "aclHkVaku9U",
                    focusTip = "Keep heels grounded and descend below parallel with upright chest."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Quad & Glute Drive",
                    youtubeLink = "https://www.youtube.com/watch?v=aclHkVaku9U",
                    youtubeId = "aclHkVaku9U",
                    focusTip = "Drive through floor and squeeze glutes firmly at top lockout."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: High-Rep Volume / Lunge Flow",
                    youtubeLink = "https://www.youtube.com/watch?v=QOVaHwm-Q6U",
                    youtubeId = "QOVaHwm-Q6U",
                    focusTip = "Maintain consistent rhythm and deep knee bend on lunges."
                )
            )
        )
    )

    fun getGuideForExercise(exerciseName: String): ExerciseVideoGuide {
        val lower = exerciseName.lowercase()
        return guides.find {
            (lower.contains("push") && lower.contains("explosive")) ||
            (it.exerciseName.lowercase() in lower) ||
            (lower.contains("pike") && it.exerciseName.contains("Pike")) ||
            (lower.contains("dip") && it.exerciseName.contains("Dip")) ||
            (lower.contains("knee") && it.exerciseName.contains("Knee")) ||
            (lower.contains("plank") && it.exerciseName.contains("Plank")) ||
            (lower.contains("pull") && it.exerciseName.contains("Pull")) ||
            (lower.contains("squat") && it.exerciseName.contains("Squat"))
        } ?: ExerciseVideoGuide(
            exerciseName = exerciseName,
            videoUrl = "https://www.youtube-nocookie.com/embed/R_bA_y91r0E",
            youtubeId = "R_bA_y91r0E",
            directYouTubeLink = "https://www.youtube.com/watch?v=R_bA_y91r0E",
            targetMuscles = "Full Body & Calisthenics Conditioning",
            formCues = listOf(
                "Warm up shoulders and joints with dynamic mobility before sets.",
                "Control the eccentric (lowering) phase for at least 2 seconds.",
                "Breathe out on exertion; maintain a braced core throughout.",
                "Focus on quality range of motion over sheer rep count."
            ),
            commonMistakes = listOf(
                "Rushing repetitions with degraded form.",
                "Skipping proper rest intervals between work sets.",
                "Not tracking sets and volume systematically."
            ),
            setTutorials = listOf(
                SetVideoTutorial(
                    setNumber = 1,
                    title = "Set 1: Calisthenics Masterclass",
                    youtubeLink = "https://www.youtube.com/watch?v=R_bA_y91r0E",
                    youtubeId = "R_bA_y91r0E",
                    focusTip = "Execute every rep with crisp form and controlled tempo."
                ),
                SetVideoTutorial(
                    setNumber = 2,
                    title = "Set 2: Working Set Intensity",
                    youtubeLink = "https://www.youtube.com/watch?v=naRaVsFn_g4",
                    youtubeId = "naRaVsFn_g4",
                    focusTip = "Push with steady pace and clean full range of motion."
                ),
                SetVideoTutorial(
                    setNumber = 3,
                    title = "Set 3: Technical Failure Finish",
                    youtubeLink = "https://www.youtube.com/watch?v=P_jM2c-q-7o",
                    youtubeId = "P_jM2c-q-7o",
                    focusTip = "Maintain brace until the last repetition is completed."
                )
            )
        )
    }

    fun getAllGuides(): List<ExerciseVideoGuide> = guides
}
