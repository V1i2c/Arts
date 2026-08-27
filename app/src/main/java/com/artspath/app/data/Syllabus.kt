package com.artspath.app.data

/**
 * JAC (Jharkhand Academic Council) Class 12 Arts subject & chapter catalog,
 * academic session 2026-27. JAC prescribes NCERT/JCERT textbooks.
 *
 * Chapter lists verified against multiple syllabus sources for:
 * History, Political Science, Geography, Economics, Sociology, Psychology,
 * English (Core), Hindi (Core), Sanskrit, Home Science, Mathematics (optional).
 *
 * For Urdu, Philosophy, Anthropology and Music the JAC/JCERT chapter lists are
 * not published online in verifiable form — these subjects are seeded WITHOUT
 * chapters and the student adds chapters from their textbook in the Errors book.
 */
object SyllabusCatalog {

    data class SeedChapter(val name: String, val part: String? = null)

    data class SeedSubject(
        val name: String,
        val colorKey: String,
        val sortOrder: Int,
        val chapters: List<SeedChapter> = emptyList(),
        /** Shown under the subject name in the picker. */
        val note: String? = null
    )

    val subjects: List<SeedSubject> = listOf(
        SeedSubject(
            "History", "terracotta", 1,
            listOf(
                SeedChapter("Bricks, Beads and Bones — The Harappan Civilisation", "Themes in Indian History"),
                SeedChapter("Kings, Farmers and Towns — Early States and Economies", "Themes in Indian History"),
                SeedChapter("Kinship, Caste and Class — Early Societies", "Themes in Indian History"),
                SeedChapter("Thinkers, Beliefs and Buildings — Cultural Developments", "Themes in Indian History"),
                SeedChapter("Through the Eyes of Travellers — Perceptions of Society", "Themes in Indian History"),
                SeedChapter("Bhakti-Sufi Traditions — Changes in Religious Beliefs and Devotional Texts", "Themes in Indian History"),
                SeedChapter("An Imperial Capital: Vijayanagara", "Themes in Indian History"),
                SeedChapter("Peasants, Zamindars and the State — Agrarian Society and the Mughal Empire", "Themes in Indian History"),
                SeedChapter("Colonialism and the Countryside — Exploring Official Archives", "Themes in Indian History"),
                SeedChapter("Rebels and the Raj — The Revolt of 1857 and its Representations", "Themes in Indian History"),
                SeedChapter("Mahatma Gandhi and the Nationalist Movement — Civil Disobedience and Beyond", "Themes in Indian History"),
                SeedChapter("Framing the Constitution — The Beginning of a New Era", "Themes in Indian History")
            )
        ),
        SeedSubject(
            "Political Science", "indigo", 2,
            listOf(
                SeedChapter("The End of Bipolarity", "Contemporary World Politics"),
                SeedChapter("Contemporary Centres of Power", "Contemporary World Politics"),
                SeedChapter("Contemporary South Asia", "Contemporary World Politics"),
                SeedChapter("International Organisations", "Contemporary World Politics"),
                SeedChapter("Security in the Contemporary World", "Contemporary World Politics"),
                SeedChapter("Environment and Natural Resources", "Contemporary World Politics"),
                SeedChapter("Globalisation", "Contemporary World Politics"),
                SeedChapter("Challenges of Nation Building", "Politics in India since Independence"),
                SeedChapter("Era of One-Party Dominance", "Politics in India since Independence"),
                SeedChapter("Politics of Planned Development", "Politics in India since Independence"),
                SeedChapter("India's External Relations", "Politics in India since Independence"),
                SeedChapter("Challenges to and Restoration of the Congress System", "Politics in India since Independence"),
                SeedChapter("The Crisis of Democratic Order", "Politics in India since Independence"),
                SeedChapter("Regional Aspirations", "Politics in India since Independence"),
                SeedChapter("Recent Developments in Indian Politics", "Politics in India since Independence")
            )
        ),
        SeedSubject(
            "Geography", "moss", 3,
            listOf(
                SeedChapter("Human Geography — Nature and Scope", "Fundamentals of Human Geography"),
                SeedChapter("The World Population — Distribution, Density and Growth", "Fundamentals of Human Geography"),
                SeedChapter("Population Composition", "Fundamentals of Human Geography"),
                SeedChapter("Human Development", "Fundamentals of Human Geography"),
                SeedChapter("Primary Activities", "Fundamentals of Human Geography"),
                SeedChapter("Secondary Activities", "Fundamentals of Human Geography"),
                SeedChapter("Tertiary, Quaternary and Quinary Activities", "Fundamentals of Human Geography"),
                SeedChapter("Transport and Communication", "Fundamentals of Human Geography"),
                SeedChapter("International Trade", "Fundamentals of Human Geography"),
                SeedChapter("Human Settlements", "Fundamentals of Human Geography"),
                SeedChapter("Population — Distribution, Density, Growth and Composition", "India: People and Economy"),
                SeedChapter("Migration — Types, Causes and Consequences", "India: People and Economy"),
                SeedChapter("Human Development in India", "India: People and Economy"),
                SeedChapter("Human Settlements in India", "India: People and Economy"),
                SeedChapter("Land Resources and Agriculture", "India: People and Economy"),
                SeedChapter("Water Resources", "India: People and Economy"),
                SeedChapter("Mineral and Energy Resources", "India: People and Economy"),
                SeedChapter("Manufacturing Industries", "India: People and Economy"),
                SeedChapter("Planning and Sustainable Development in Indian Context", "India: People and Economy"),
                SeedChapter("Transport and Communication in India", "India: People and Economy"),
                SeedChapter("International Trade in India", "India: People and Economy"),
                SeedChapter("Geographical Perspective on Selected Issues and Problems", "India: People and Economy")
            )
        ),
        SeedSubject(
            "Economics", "ochre", 4,
            listOf(
                SeedChapter("Introduction to Macroeconomics", "Introductory Macroeconomics"),
                SeedChapter("National Income Accounting", "Introductory Macroeconomics"),
                SeedChapter("Money and Banking", "Introductory Macroeconomics"),
                SeedChapter("Determination of Income and Employment", "Introductory Macroeconomics"),
                SeedChapter("Government Budget and the Economy", "Introductory Macroeconomics"),
                SeedChapter("Open Economy Macroeconomics", "Introductory Macroeconomics"),
                SeedChapter("Indian Economy on the Eve of Independence", "Indian Economic Development"),
                SeedChapter("Indian Economy 1950–1990", "Indian Economic Development"),
                SeedChapter("Liberalisation, Privatisation and Globalisation — An Appraisal", "Indian Economic Development"),
                SeedChapter("Poverty", "Indian Economic Development"),
                SeedChapter("Human Capital Formation in India", "Indian Economic Development"),
                SeedChapter("Rural Development", "Indian Economic Development"),
                SeedChapter("Employment — Growth, Informalisation and Other Issues", "Indian Economic Development"),
                SeedChapter("Infrastructure", "Indian Economic Development"),
                SeedChapter("Environment and Sustainable Development", "Indian Economic Development"),
                SeedChapter("Comparative Development Experiences of India and its Neighbours", "Indian Economic Development")
            )
        ),
        SeedSubject(
            "Sociology", "plum", 5,
            listOf(
                SeedChapter("Introducing Indian Society", "Indian Society"),
                SeedChapter("The Demographic Structure of the Indian Society", "Indian Society"),
                SeedChapter("Social Institutions — Continuity and Change", "Indian Society"),
                SeedChapter("The Market as a Social Institution", "Indian Society"),
                SeedChapter("Patterns of Social Inequality and Exclusion", "Indian Society"),
                SeedChapter("Challenges of Cultural Diversity", "Indian Society"),
                SeedChapter("Structural Change", "Social Change and Development in India"),
                SeedChapter("Cultural Change", "Social Change and Development in India"),
                SeedChapter("The Constitution and Social Change", "Social Change and Development in India"),
                SeedChapter("Change and Development in Rural Society", "Social Change and Development in India"),
                SeedChapter("Change and Development in Industrial Society", "Social Change and Development in India"),
                SeedChapter("Globalisation and Social Change", "Social Change and Development in India"),
                SeedChapter("Mass Media and Communications", "Social Change and Development in India"),
                SeedChapter("Social Movements", "Social Change and Development in India")
            )
        ),
        SeedSubject(
            "Psychology", "teal", 6,
            listOf(
                SeedChapter("Variations in Psychological Attributes"),
                SeedChapter("Self and Personality"),
                SeedChapter("Meeting Life Challenges"),
                SeedChapter("Psychological Disorders"),
                SeedChapter("Therapeutic Approaches"),
                SeedChapter("Attitude and Social Cognition"),
                SeedChapter("Social Influence and Group Processes"),
                SeedChapter("Psychology and Life"),
                SeedChapter("Developing Psychological Skills")
            )
        ),
        SeedSubject(
            "Hindi (Core)", "copper", 7,
            listOf(
                SeedChapter("आत्म-परिचय, एक गीत", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("पतंग", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("कविता के बहाने, बात सीधी थी पर", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("कैमरे में बंद अपाहिज", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("सहर्ष स्वीकारा है", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("उषा", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("बादल राग", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("कवितावली (उत्तर कांड से), लक्ष्मण-मूच्छ और राम का विलाप", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("रुबाइयाँ, ग़ज़ल", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("छोटा मेरा खेत, बगुलों के पंख", "आरोह भाग-2 — काव्य खंड"),
                SeedChapter("भक्तिन", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("बाज़ार दर्शन", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("काले मेघा पानी दे", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("पहलवान की ढोलक", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("चार्ली चैप्लिन यानी हम सब", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("नमक", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("शिरीष के फूल", "आरोह भाग-2 — गद्य खंड"),
                SeedChapter("सिल्वर वैडिंग", "वितान भाग-2"),
                SeedChapter("जूझ", "वितान भाग-2"),
                SeedChapter("अतीत में दबे पाँव", "वितान भाग-2"),
                SeedChapter("डायरी के पन्ने", "वितान भाग-2")
            )
        ),
        SeedSubject(
            "English (Core)", "sand", 8,
            listOf(
                SeedChapter("The Last Lesson", "Flamingo — Prose"),
                SeedChapter("Lost Spring", "Flamingo — Prose"),
                SeedChapter("Deep Water", "Flamingo — Prose"),
                SeedChapter("The Rattrap", "Flamingo — Prose"),
                SeedChapter("Indigo", "Flamingo — Prose"),
                SeedChapter("Poets and Pancakes", "Flamingo — Prose"),
                SeedChapter("The Interview", "Flamingo — Prose"),
                SeedChapter("Going Places", "Flamingo — Prose"),
                SeedChapter("My Mother at Sixty-six", "Flamingo — Poetry"),
                SeedChapter("Keeping Quiet", "Flamingo — Poetry"),
                SeedChapter("A Thing of Beauty", "Flamingo — Poetry"),
                SeedChapter("A Roadside Stand", "Flamingo — Poetry"),
                SeedChapter("Aunt Jennifer's Tigers", "Flamingo — Poetry"),
                SeedChapter("The Third Level", "Vistas — Supplementary Reader"),
                SeedChapter("The Tiger King", "Vistas — Supplementary Reader"),
                SeedChapter("Journey to the End of the Earth", "Vistas — Supplementary Reader"),
                SeedChapter("The Enemy", "Vistas — Supplementary Reader"),
                SeedChapter("On the Face of It", "Vistas — Supplementary Reader"),
                SeedChapter("Memories of Childhood", "Vistas — Supplementary Reader")
            )
        ),
        SeedSubject(
            "Sanskrit", "wine", 9,
            listOf(
                SeedChapter("विद्ययाऽमृतमश्नुते", "शाश्वती द्वितीयो भागः"),
                SeedChapter("रघुकौतुकसंवादः", "शाश्वती द्वितीयो भागः"),
                SeedChapter("बालकौतुकम्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("कर्मगौरवम्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("शुकनासोपदेशः", "शाश्वती द्वितीयो भागः"),
                SeedChapter("सूक्तिसुधा", "शाश्वती द्वितीयो भागः"),
                SeedChapter("विक्रमस्योदार्यम्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("भूविभागाः", "शाश्वती द्वितीयो भागः"),
                SeedChapter("कार्यं वा साधयेयं, देहं वा पातयेयम्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("दीनबन्धुः श्रीनिवासः", "शाश्वती द्वितीयो भागः"),
                SeedChapter("उद्भिज्जपरिषद्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("किन्तोः कुटिलता", "शाश्वती द्वितीयो भागः"),
                SeedChapter("योगस्य वैशिष्ट्यम्", "शाश्वती द्वितीयो भागः"),
                SeedChapter("कथं शब्दानुशासनं कर्तव्यम्", "शाश्वती द्वितीयो भागः")
            )
        ),
        SeedSubject(
            "Home Science", "olive", 10,
            listOf(
                SeedChapter("Work, Livelihood and Career", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Clinical Nutrition and Dietetics", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Public Nutrition and Health", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Catering and Food Services Management", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Food Processing and Technology", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Food Quality and Food Safety", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Early Childhood Care and Education", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Guidance and Counselling", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Special Education and Support Services", "Human Ecology and Family Sciences — Part I"),
                SeedChapter("Management of Support Services, Institutions and Programmes for Children, Youth and Elderly", "Human Ecology and Family Sciences — Part I")
            )
        ),
        SeedSubject(
            "Philosophy", "forest", 11,
            emptyList(),
            "Add chapters from your JCERT textbook"
        ),
        SeedSubject(
            "Anthropology", "slate", 12,
            emptyList(),
            "Add chapters from your JCERT textbook"
        ),
        SeedSubject(
            "Music", "berry", 13,
            emptyList(),
            "Add chapters from your JCERT textbook"
        ),
        SeedSubject(
            "Urdu", "rose", 14,
            emptyList(),
            "Add chapters from Gulistan-e-Adab"
        ),
        SeedSubject(
            "Mathematics (optional)", "steel", 15,
            listOf(
                SeedChapter("Relations and Functions", "Part I"),
                SeedChapter("Inverse Trigonometric Functions", "Part I"),
                SeedChapter("Matrices", "Part I"),
                SeedChapter("Determinants", "Part I"),
                SeedChapter("Continuity and Differentiability", "Part I"),
                SeedChapter("Application of Derivatives", "Part I"),
                SeedChapter("Integrals", "Part II"),
                SeedChapter("Application of Integrals", "Part II"),
                SeedChapter("Differential Equations", "Part II"),
                SeedChapter("Vector Algebra", "Part II"),
                SeedChapter("Three Dimensional Geometry", "Part II"),
                SeedChapter("Linear Programming", "Part II"),
                SeedChapter("Probability", "Part II")
            )
        )
    )

    /** All distinct part labels used by a seed subject, in order. */
    fun partsOf(subject: SeedSubject): List<String> =
        subject.chapters.mapNotNull { it.part }.distinct()
}
