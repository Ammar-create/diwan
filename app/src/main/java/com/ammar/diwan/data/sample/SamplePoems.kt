package com.ammar.diwan.data.sample

import com.ammar.diwan.data.models.Poem
import com.ammar.diwan.data.models.Verse

object SamplePoems {
    val shikwaStanza1 = Poem(
        id = "shikwa_01",
        titleUrdu = "شکوہ — بند اول",
        titleEnglish = "Shikwa — Stanza 1",
        poetUrdu = "علامہ محمد اقبال",
        poetEnglish = "Allama Muhammad Iqbal",
        verses = listOf(
            Verse(
                urdu = "کیوں زیاں کار بنوں، سُود فراموش رہوں؟",
                roman = "Kyun ziyan-kaar banoon, sood-faramosh rahoon?",
                english = "Why should I act as a loser, forgetful of my own gain?"
            ),
            Verse(
                urdu = "فکرِ فردا نہ کروں، محوِ غمِ دوش رہوں؟",
                roman = "Fikr-e-farda na karoon, mahw-e-gham-e-dosh rahoon?",
                english = "Careless of the morrow, remain absorbed in the grief of yesterday?"
            ),
            Verse(
                urdu = "نالۂ بُلبلِ شوریدہ سُنوں ہمہ تن گوش رہوں؟",
                roman = "Nala-e-bulbul-e-shoreeda sunoon hama-tan gosh rahoon?",
                english = "Listen to the lamentations of the frenzied nightingale and be all ears?"
            ),
            Verse(
                urdu = "ہم نوا میں بھی کوئی گُل ہوں کہ خاموش رہوں؟",
                roman = "Ham-nawa main bhi koi gul hoon keh khamosh rahoon?",
                english = "O companion! Am I a mere flower that I should remain silent?"
            ),
            Verse(
                urdu = "جُرأت آموز مری تابِ سخن ہے مجھ کو",
                roman = "Jur'at aamoz meri taab-e-sukhan hai mujh ko",
                english = "My courage-inspiring poetic power lends me the daring,"
            ),
            Verse(
                urdu = "شکوہ اللہ سے، خاکم بدہن، ہے مجھ کو!",
                roman = "Shikwa Allah se, khaakam badahan, hai mujh ko!",
                english = "To utter a complaint to Allah—may dust be in my mouth!"
            )
        ),
        explanationUrdu = "اقبال اس ابتدائی بند میں اپنی شاعری کی بے باکی کا جواز پیش کرتے ہیں۔ وہ کہتے ہیں کہ جب سب کچھ لٹ رہا ہو تو میں خاموش تماشائی بن کر کیوں بیٹھوں؟ میری قوتِ گویائی مجھے مجبور کر رہی ہے کہ میں بارگاہِ الٰہی میں اپنا شکوہ پیش کروں۔",
        explanationEnglish = "In this opening stanza of Shikwa, Iqbal defends his passionate outcry. He questions why he should remain passive while the Muslim world declines, declaring that his poetic strength compels him to lay his grievances before the Divine.",
        tags = listOf("Shikwa", "Iqbal", "Classical", "Philosophical")
    )

    val ghalibGhazal = Poem(
        id = "ghalib_01",
        titleUrdu = "ہزاروں خواہشیں ایسی",
        titleEnglish = "A Thousand Desires Such",
        poetUrdu = "مرزا اسد اللہ خان غالب",
        poetEnglish = "Mirza Asadullah Khan Ghalib",
        verses = listOf(
            Verse(
                urdu = "ہزاروں خواہشیں ایسی کہ ہر خواہش پہ دم نکلے",
                roman = "Hazaron khwahishein aisi keh har khwahish pe dam nikle",
                english = "Thousands of desires, each so intense that on each breath departs,"
            ),
            Verse(
                urdu = "بہت نکلے مرے ارمان لیکن پھر بھی کم نکلے",
                roman = "Bohat nikle mere armaan lekin phir bhi kam nikle",
                english = "Many of my yearnings came to pass, yet far too few they were."
            ),
            Verse(
                urdu = "ڈرے کیوں میرا قاتل کیا رہے گا اس کی گردن پر",
                roman = "Dare kyun mera qatil kya rahega us ki gardan par",
                english = "Why should my slayer dread, what blood shall rest upon his neck?"
            ),
            Verse(
                urdu = "وہ خوں جو چشمِ تر سے عمر بھر یوں دم بدم نکلے",
                roman = "Woh khoon jo chashm-e-tar se umr bhar yoon dam-ba-dam nikle",
                english = "That blood which from these weeping eyes poured incessantly all my life?"
            ),
            Verse(
                urdu = "ہوئی مدت کہ غالب مر گیا پر یاد آتا ہے",
                roman = "Hui muddat keh Ghalib mar gaya par yaad aata hai",
                english = "An age has passed since Ghalib died, yet still he comes to mind,"
            ),
            Verse(
                urdu = "وہ ہر اک بات پر کہنا کہ یوں ہوتا تو کیا ہوتا!",
                roman = "Woh har ik baat par kehna keh yoon hota to kya hota!",
                english = "His pondering on every turn: 'Had it been thus, what then would be?'"
            )
        ),
        explanationUrdu = "غالب کی سب سے مشہور غزل جس میں انسانی خواہشات کی لا متناہی وسعت اور زندگی کی بے ثباتی کا درد نہایت شوخی اور فلسفیانہ انداز میں بیان ہوا ہے۔",
        explanationEnglish = "Ghalib's celebrated masterpiece exploring the boundless nature of human longing and existential despair with his trademark wit and philosophical gravity.",
        tags = listOf("Ghazal", "Ghalib", "Melancholy", "Desire")
    )

    val all = listOf(shikwaStanza1, ghalibGhazal)
}
