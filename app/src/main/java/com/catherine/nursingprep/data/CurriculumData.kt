package com.catherine.nursingprep.data

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt

/* ────────────────────────────────────────────────────────────────
   The 20-week master curriculum — 100% official JAMB syllabus
   Use of English · Biology · Chemistry · Physics
   Plan start: November 2, 2026 → Exam: March 20, 2027 (139 days)
   ──────────────────────────────────────────────────────────────── */

object CurriculumData {

    const val TOTAL_DAYS = 139
    const val TOTAL_WEEKS = 20

    /* ── Key dates ─────────────────────────────────────────────── */

    /** JAMB UTME exam morning — March 20, 2027, 8:00 AM local time */
    val EXAM_MILLIS: Long by lazy {
        Calendar.getInstance().apply {
            clear()
            set(2027, 2, 20, 8, 0, 0)
        }.timeInMillis
    }

    /** Day 1 of the plan — November 2, 2026 */
    private val planStart: Calendar by lazy {
        Calendar.getInstance().apply {
            clear()
            set(2026, 10, 2)
        }
    }

    /* ── Plan math ─────────────────────────────────────────────── */

    fun planDayFor(now: Calendar): Int {
        val today = (now.clone() as Calendar).apply { toMidnight() }
        val diffDays =
            ((today.timeInMillis - planStart.timeInMillis).toDouble() / 86_400_000.0).roundToInt()
        return diffDays + 1
    }

    fun weekForDay(day: Int): Int = when {
        day < 1 -> 1
        day > TOTAL_DAYS -> TOTAL_WEEKS
        else -> minOf(TOTAL_WEEKS, (day + 6) / 7)
    }

    fun weekDayRange(week: Int): Pair<Int, Int> =
        if (week >= TOTAL_WEEKS) Pair(134, TOTAL_DAYS)
        else Pair((week - 1) * 7 + 1, week * 7)

    fun phaseForWeek(week: Int): Int = (week + 3) / 4

    fun dateForPlanDay(day: Int): Calendar =
        (planStart.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, day - 1) }

    fun formatShort(c: Calendar): String = SimpleDateFormat("MMM d", Locale.US).format(c.time)

    fun dateRangeLabel(week: Int): String {
        val (a, b) = weekDayRange(week)
        return "${formatShort(dateForPlanDay(a))} – ${formatShort(dateForPlanDay(b))}"
    }

    fun examDateLabel(): String = "Friday, March 20, 2027 · 8:00 AM"

    fun fullDateLabel(c: Calendar): String =
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.US).format(c.time)

    fun greeting(hour: Int): String = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else -> "Good evening"
    }

    /* ── Nightly window: 10:00 PM – 12:30 AM ───────────────────── */

    fun routineLive(minutesOfDay: Int): Boolean =
        minutesOfDay >= 22 * 60 || minutesOfDay < 30

    /* ── Daily motivation ──────────────────────────────────────── */

    fun quoteFor(now: Calendar): String = QUOTES[now.get(Calendar.DAY_OF_YEAR) % QUOTES.size]
    fun tipFor(now: Calendar): String = NURSE_TIPS[now.get(Calendar.DAY_OF_YEAR) % NURSE_TIPS.size]

    fun todayRotation(now: Calendar): RotationDay {
        val dow = now.get(Calendar.DAY_OF_WEEK) - 1 // 0 = Sunday
        return ROTATION.first { it.dow == dow }
    }

    /* ── The 5 phases ──────────────────────────────────────────── */

    val PHASES = listOf(
        PhaseMeta(
            1, "Foundations", "Days 1–28", 1, 4,
            "Lock in the building blocks — cells, moles, units and lexis — across all four subjects.",
        ),
        PhaseMeta(
            2, "Core Systems & Physiology", "Days 29–56", 5, 8,
            "Plant and animal systems, nutrition, transport, bonding and forces in equilibrium.",
        ),
        PhaseMeta(
            3, "Advanced Concepts & Energy", "Days 57–84", 9, 12,
            "Respiration to electrolysis — energy changes, machines, motion and reproduction.",
        ),
        PhaseMeta(
            4, "Systems, Waves & Organic Chem", "Days 85–112", 13, 16,
            "Coordination, germination, heat, waves, non-metals and the human senses.",
        ),
        PhaseMeta(
            5, "Final Exam Mastery & Mocks", "Days 113–139", 17, 20,
            "Genetics, ecology, organics, electromagnetism, modern physics and full UTME mocks.",
        ),
    )

    /* ── The 20-week master roadmap ────────────────────────────── */

    val WEEKS = listOf(
        WeekData(1, 1, mapOf(
            Subject.ENGLISH to listOf(
                "Lexis I — synonyms & antonyms in context",
                "Ordinary, figurative & idiomatic usage",
            ),
            Subject.BIOLOGY to listOf(
                "Cell structure & functions of organelles",
                "Plant vs animal cells",
                "Levels of organization (cell → organism)",
            ),
            Subject.CHEMISTRY to listOf(
                "Elements, compounds & mixtures",
                "Separation & purification techniques",
            ),
            Subject.PHYSICS to listOf(
                "Fundamental & derived units",
                "Measuring instruments & dimensions",
            ),
        )),
        WeekData(2, 1, mapOf(
            Subject.ENGLISH to listOf(
                "Idioms & Standard British English usage",
                "Correct vs incorrect spellings",
            ),
            Subject.BIOLOGY to listOf(
                "Monera, Protista & Fungi",
                "Plant evolution: Thallophyta → Bryophyta",
            ),
            Subject.CHEMISTRY to listOf(
                "Laws of chemical combination",
                "Mole concept & stoichiometry",
            ),
            Subject.PHYSICS to listOf(
                "Scalars, vectors & resolution",
                "Relative velocity & position",
            ),
        )),
        WeekData(3, 1, mapOf(
            Subject.ENGLISH to listOf(
                "Comprehension: main ideas & inference",
                "Grammatical functions of words & phrases",
            ),
            Subject.BIOLOGY to listOf(
                "Spermatophyta: gymnosperms & angiosperms",
                "Invertebrates: coelenterates → molluscs",
            ),
            Subject.CHEMISTRY to listOf(
                "Kinetic theory of matter",
                "Gas laws: Boyle, Charles & PV = nRT",
            ),
            Subject.PHYSICS to listOf(
                "Linear motion & motion graphs",
                "Equations of uniformly accelerated motion",
            ),
        )),
        WeekData(4, 1, mapOf(
            Subject.ENGLISH to listOf(
                "Summary: synthesis of ideas from passages",
                "Coherence & logical reasoning",
            ),
            Subject.BIOLOGY to listOf(
                "Vertebrates: pisces → mammalia",
                "Structural, functional & behavioural adaptations",
            ),
            Subject.CHEMISTRY to listOf(
                "Atomic structure & the four quantum numbers",
                "Electron configuration & isotopy",
            ),
            Subject.PHYSICS to listOf(
                "Newton's laws of motion",
                "Impulse, momentum & conservation",
            ),
        )),
        WeekData(5, 2, mapOf(
            Subject.ENGLISH to listOf(
                "Grammar I — concord & agreement",
                "Tense, aspect & number",
            ),
            Subject.BIOLOGY to listOf(
                "Internal structure: root, stem & leaf",
                "Mammalian internal organs",
            ),
            Subject.CHEMISTRY to listOf(
                "Periodic table & periodicity of properties",
                "Families of elements",
            ),
            Subject.PHYSICS to listOf(
                "Projectile motion & applications",
                "Types & causes of motion",
            ),
        )),
        WeekData(6, 2, mapOf(
            Subject.ENGLISH to listOf(
                "Figures of speech & figurative expressions",
                "Writers' mood, attitude & opinion",
            ),
            Subject.BIOLOGY to listOf(
                "Photosynthesis: light & dark reactions",
                "Mineral requirements (macro & micro)",
            ),
            Subject.CHEMISTRY to listOf(
                "Chemical bonding: electrovalent & covalent",
                "Shapes of simple molecules",
            ),
            Subject.PHYSICS to listOf(
                "Circular motion & centripetal force",
                "Simple Harmonic Motion (S.H.M.)",
            ),
        )),
        WeekData(7, 2, mapOf(
            Subject.ENGLISH to listOf(
                "Cloze passages: vocabulary in context",
                "Approved reading text practice",
            ),
            Subject.BIOLOGY to listOf(
                "Animal nutrition & food tests",
                "Mammalian tooth, dental formulae & alimentary canal",
            ),
            Subject.CHEMISTRY to listOf(
                "Nuclear chemistry & radioactivity",
                "Half-life calculations",
            ),
            Subject.PHYSICS to listOf(
                "Universal gravitation & gravitational fields",
                "Escape velocity, parking orbit & weightlessness",
            ),
        )),
        WeekData(8, 2, mapOf(
            Subject.ENGLISH to listOf(
                "Lexis II — antonyms/synonyms drills",
                "Question tags & mechanics",
            ),
            Subject.BIOLOGY to listOf(
                "Plant transport: xylem & phloem",
                "Mammalian circulation, blood & lymph",
            ),
            Subject.CHEMISTRY to listOf(
                "Solubility curves & calculations",
                "True vs false solutions (suspensions & colloids)",
            ),
            Subject.PHYSICS to listOf(
                "Equilibrium of coplanar forces",
                "Moments, couples & centre of gravity",
            ),
        )),
        WeekData(9, 3, mapOf(
            Subject.ENGLISH to listOf(
                "Oral I — monothongs, diphthongs & triphthongs",
                "Vowel distinctions",
            ),
            Subject.BIOLOGY to listOf(
                "Respiration: aerobic & anaerobic",
                "Glycolysis, Krebs cycle & ATP",
            ),
            Subject.CHEMISTRY to listOf(
                "Environmental pollution: air, water & soil",
                "Pollutants, effects & control",
            ),
            Subject.PHYSICS to listOf(
                "Friction: static & dynamic",
                "Viscosity, terminal velocity & Stoke's law",
            ),
        )),
        WeekData(10, 3, mapOf(
            Subject.ENGLISH to listOf(
                "Oral II — consonants & clusters",
                "Rhymes & homophones",
            ),
            Subject.BIOLOGY to listOf(
                "Excretion & kidney osmoregulation",
                "Excretory products of plants",
            ),
            Subject.CHEMISTRY to listOf(
                "Acids, bases & salts",
                "pH & pOH calculations, titrations",
            ),
            Subject.PHYSICS to listOf(
                "Work, energy & power",
                "Energy & society, solar energy",
            ),
        )),
        WeekData(11, 3, mapOf(
            Subject.ENGLISH to listOf(
                "Oral III — word stress (mono & polysyllabic)",
                "Emphatic stress in connected speech",
            ),
            Subject.BIOLOGY to listOf(
                "Tropic, tactic & nastic movements",
                "Skeleton: types, joints & functions",
            ),
            Subject.CHEMISTRY to listOf(
                "Salt hydrolysis",
                "Redox reactions & balancing equations",
            ),
            Subject.PHYSICS to listOf(
                "Simple machines: MA, VR & efficiency",
                "Energy conversion devices",
            ),
        )),
        WeekData(12, 3, mapOf(
            Subject.ENGLISH to listOf(
                "Structure I — clause & sentence patterns",
                "Mood & word classes",
            ),
            Subject.BIOLOGY to listOf(
                "Asexual & sexual reproduction in plants",
                "Pollination, fertilization & fruits",
            ),
            Subject.CHEMISTRY to listOf(
                "Faraday's laws of electrolysis",
                "Electrochemical cells & corrosion",
            ),
            Subject.PHYSICS to listOf(
                "Elasticity: Hooke's law & Young's modulus",
                "Liquid pressure & Pascal's principle",
            ),
        )),
        WeekData(13, 4, mapOf(
            Subject.ENGLISH to listOf(
                "Structure II — degrees of comparison",
                "Punctuation & capitalization",
            ),
            Subject.BIOLOGY to listOf(
                "Mammalian reproductive system",
                "Fertilization & development to birth",
            ),
            Subject.CHEMISTRY to listOf(
                "Thermochemistry: ΔH & entropy",
                "Spontaneity: ΔG° = ΔH° − TΔS°",
            ),
            Subject.PHYSICS to listOf(
                "Archimedes' principle & law of floatation",
                "Density, upthrust & hydrometers",
            ),
        )),
        WeekData(14, 4, mapOf(
            Subject.ENGLISH to listOf(
                "Structure III — concord drills",
                "Common errors in English",
            ),
            Subject.BIOLOGY to listOf(
                "Seed germination: epigeal vs hypogeal",
                "Conditions necessary for germination",
            ),
            Subject.CHEMISTRY to listOf(
                "Rates of chemical reactions",
                "Activation energy & collision theory",
            ),
            Subject.PHYSICS to listOf(
                "Temperature scales & thermometers",
                "Specific heat capacity: Q = mcΔT",
            ),
        )),
        WeekData(15, 4, mapOf(
            Subject.ENGLISH to listOf(
                "Approved reading text — comprehension drill",
                "Synthesis of ideas practice",
            ),
            Subject.BIOLOGY to listOf(
                "Sense organs: eye & ear defects and corrections",
                "Endocrine glands & phytohormones",
            ),
            Subject.CHEMISTRY to listOf(
                "Chemical equilibria & Le Chatelier's principle",
                "Industrial applications of equilibrium",
            ),
            Subject.PHYSICS to listOf(
                "Latent heat & change of state",
                "Thermal expansivity (linear, volume, area)",
            ),
        )),
        WeekData(16, 4, mapOf(
            Subject.ENGLISH to listOf(
                "Mixed comprehension & summary drills",
                "Timed passage practice",
            ),
            Subject.BIOLOGY to listOf(
                "Homeostasis: temperature & water balance",
                "Genetics: chromosomes & heredity",
            ),
            Subject.CHEMISTRY to listOf(
                "Non-metals & their compounds (H, halogens, O, S, N, C)",
                "Water hardness & treatment",
            ),
            Subject.PHYSICS to listOf(
                "Vapours, S.V.P. & humidity",
                "Heat transfer: conduction, convection & radiation",
            ),
        )),
        WeekData(17, 5, mapOf(
            Subject.ENGLISH to listOf(
                "Timed lexis & structure drills",
                "Sentence interpretation",
            ),
            Subject.BIOLOGY to listOf(
                "Variation in population (continuous & discontinuous)",
                "Sex-linked traits & probability in genetics",
            ),
            Subject.CHEMISTRY to listOf(
                "Metal extraction methods",
                "Alloys: constituents & uses",
            ),
            Subject.PHYSICS to listOf(
                "Wave equations: v = fλ",
                "Doppler effect & superposition",
            ),
        )),
        WeekData(18, 5, mapOf(
            Subject.ENGLISH to listOf(
                "Oral forms — mixed revision drill",
                "Pronunciation of individual words",
            ),
            Subject.BIOLOGY to listOf(
                "Ecology: habitats, biomes & niches",
                "Nutrient cycles: carbon, water & nitrogen",
            ),
            Subject.CHEMISTRY to listOf(
                "Organic I — alkanes, alkenes & alkynes",
                "Alkanols, alkanoic acids & saponification",
            ),
            Subject.PHYSICS to listOf(
                "Sound waves & characteristics",
                "Harmonics, resonance & air columns",
            ),
        )),
        WeekData(19, 5, mapOf(
            Subject.ENGLISH to listOf(
                "Full English paper — 60 timed questions",
                "Mock debrief & error review",
            ),
            Subject.BIOLOGY to listOf(
                "Diseases, drug abuse & pollution control",
                "Organic evolution: Lamarck & Darwin",
            ),
            Subject.CHEMISTRY to listOf(
                "Organic II — carbohydrates & proteins",
                "Polymers, soaps & detergents",
            ),
            Subject.PHYSICS to listOf(
                "Optical instruments & the human eye",
                "Current electricity & circuits",
            ),
        )),
        WeekData(20, 5, mapOf(
            Subject.ENGLISH to listOf(
                "Final review & full mock debrief",
                "Final error-log sweep",
            ),
            Subject.BIOLOGY to listOf(
                "Final syllabus review",
                "Full UTME mock: Biology",
            ),
            Subject.CHEMISTRY to listOf(
                "Final review: redox & electrolysis focus",
                "Full UTME mock: Chemistry",
            ),
            Subject.PHYSICS to listOf(
                "Electromagnetism",
                "Modern physics: photoelectric effect & radioactivity",
            ),
        )),
    )

    /* ── Nightly study blocks (10:00 PM – 12:30 AM) ────────────── */

    val NIGHT_BLOCKS = listOf(
        NightBlock(
            "warmup", "10:00 – 10:15 PM", "Warm-Up & Error Check", 15,
            "Skim yesterday's error log, flash through last night's summary and set tonight's single goal.",
        ),
        NightBlock(
            "core", "10:15 – 11:30 PM", "Core Topic Reading", 75,
            "Deep-read tonight's rotation subject from the roadmap. Build notes, diagrams and formulas as you go.",
        ),
        NightBlock(
            "drill", "11:30 – 12:15 AM", "Targeted Past Questions Drill", 45,
            "Hammer past questions on tonight's topics only. Mark honestly — every miss goes straight to the Error Log.",
        ),
        NightBlock(
            "log", "12:15 – 12:30 AM", "Error Log Entry", 15,
            "Write each miss into the Error Log: the question, the correct answer, and why you fell for the trap.",
        ),
    )

    /* ── Session timer modes ───────────────────────────────────── */

    val TIMER_MODES = listOf(
        TimerMode("warmup", "Warm-Up", 15, "warmup"),
        TimerMode("core", "Core Reading", 75, "core"),
        TimerMode("drill", "Past Q Drill", 45, "drill"),
        TimerMode("log", "Log Entry", 15, "log"),
        TimerMode("pomodoro", "Classic Pomodoro", 25),
        TimerMode("break", "Short Break", 5),
    )

    val DEFAULT_TIMER: TimerMode = TIMER_MODES[1]

    /* ── Weekly subject rotation (Mon → Sun) ───────────────────── */

    val ROTATION = listOf(
        RotationDay(
            1, "Monday", "Biology Night", "Theory & System Diagrams",
            listOf(Subject.BIOLOGY),
            listOf(
                "Core 75m — read theory & draw labelled system diagrams",
                "Drill 45m — Biology past questions on this week's topics",
                "Log — sketch one system from memory before bed",
            ),
        ),
        RotationDay(
            2, "Tuesday", "Chemistry Night", "Theory & Chemical Equations",
            listOf(Subject.CHEMISTRY),
            listOf(
                "Core 75m — theory + balance 10 key equations",
                "Drill 45m — Chemistry past questions & mole drills",
                "Log — write the 5 equations you must not forget",
            ),
        ),
        RotationDay(
            3, "Wednesday", "Physics Night", "Concepts & Formulas",
            listOf(Subject.PHYSICS),
            listOf(
                "Core 75m — concepts, derivations & formula sheet",
                "Drill 45m — Physics calculation past questions",
                "Log — recite tonight's formulas from memory",
            ),
        ),
        RotationDay(
            4, "Thursday", "Use of English Night", "Grammar & Oral Rules",
            listOf(Subject.ENGLISH),
            listOf(
                "Core 75m — grammar rules + one comprehension passage",
                "Drill 45m — lexis, structure & oral forms questions",
                "Log — list tricky idioms & stress patterns",
            ),
        ),
        RotationDay(
            5, "Friday", "Calculation Night", "Chem & Phys Problem-Solving",
            listOf(Subject.CHEMISTRY, Subject.PHYSICS),
            listOf(
                "Core 75m — split: mole/titration maths then mechanics maths",
                "Drill 45m — timed mixed calculation questions",
                "Log — note every formula slip and unit error",
            ),
        ),
        RotationDay(
            6, "Saturday", "Biology & English", "Ecology/Genetics & Comprehension Passage",
            listOf(Subject.BIOLOGY, Subject.ENGLISH),
            listOf(
                "Core 75m — Ecology/Genetics reading + summary writing",
                "Drill 45m — Bio past Qs + one full comprehension set",
                "Log — 3 new ecology terms & 3 new vocab words",
            ),
        ),
        RotationDay(
            0, "Sunday", "Full UTME Mock", "Timed 4-Subject Simulation",
            listOf(Subject.ENGLISH, Subject.BIOLOGY, Subject.CHEMISTRY, Subject.PHYSICS),
            listOf(
                "Simulate — full UTME: English 60 + 3 × 40 questions, timed",
                "Mark honestly — score each subject separately",
                "Log — every miss into the Error Log for next week",
            ),
        ),
    )

    /* ── Daily motivation ──────────────────────────────────────── */

    val QUOTES = listOf(
        "Every page you read tonight is a patient you'll help tomorrow. 🩺",
        "Consistency beats intensity — show up, night after night. 🌙",
        "You don't have to be perfect. You just have to keep going. ✨",
        "The white coat is waiting — every past question takes you closer. 🧑‍⚕️",
        "Small wins compound: 2.5 focused hours a day becomes an admission letter.",
        "Nurses are built on discipline. Tonight's session is part of your training. 🏥",
        "Trust the 139-day plan. Future Catherine will thank present Catherine. 💚",
        "Dream big, start small, act now — your JAMB score is counting on tonight. 🌟",
        "It always seems impossible until it is done. One topic at a time. 📚",
        "Read, drill, log, sleep. Repeat. That's how a 280+ is made. 🎯",
        "You're not just studying for an exam — you're preparing to save lives. 🫀",
        "Discipline is choosing between what you want now and what you want most. ✨",
        "One subject, one night, one step closer to Nursing School. 🩺",
        "Rest is part of the plan too — a fresh brain retains more. Sleep well. 🌙",
    )

    val NURSE_TIPS = listOf(
        "Keep your phone on Do Not Disturb — the drill block flies by when you're locked in.",
        "Water before, water after: your brain is 75% water, nurse-to-be. 💧",
        "Read aloud the diagrams you draw — labelling out loud doubles retention.",
        "End every session by predicting tomorrow's hardest question.",
        "Don't chase a new topic during the drill block — drill what you just read.",
        "Sleep by 12:45 AM sharp. Memory consolidation happens while you sleep. 😴",
        "On mock Sundays, sit at a proper desk — train your exam posture too.",
    )
}
