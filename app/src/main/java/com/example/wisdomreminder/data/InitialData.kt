package com.example.wisdomreminder.data // Assuming this is your package structure

import com.example.wisdomreminder.model.Wisdom
import java.time.LocalDateTime

object InitialData {

    fun getPredefinedWisdom(): List<Wisdom> {
        val wisdomList = mutableListOf<Wisdom>()
        var orderCounter = 0
        val initialDaysToMinus = 10 // Base for date calculation

        // Helper function to add wisdom and manage order/date
        fun addWisdom(text: String, source: String, category: String, isFavorite: Boolean = false, isActive: Boolean = false) {
            wisdomList.add(
                Wisdom(
                    text = text,
                    source = source,
                    category = category,
                    dateCreated = LocalDateTime.now().minusDays((initialDaysToMinus - orderCounter).toLong()),
                    isFavorite = isFavorite,
                    isActive = isActive,
                    orderIndex = orderCounter++
                )
            )
        }

        // BODY
        addWisdom(
            text = "I beseech you therefore, brethren, by the mercies of God, that you present your bodies a living sacrifice, holy, acceptable to God, which is your reasonable service - Rom 12:1",
            source = "TO PRESENT OUR BODY AS LIVING SACRIFICE",
            category = "BODY",
            isFavorite = true
        ) // orderIndex 0

        // MIND
        addWisdom(
            text = "And do not be conformed to this world, but be transformed by the renewing of your mind, that you may prove what is that good and acceptable and perfect will of God. - Rom 12:2",
            source = "TO RENEW OUR MIND",
            category = "MIND"
        ) // orderIndex 1
        addWisdom(
            text = "that you put off, concerning your former conduct, the old man which grows corrupt according to the deceitful lusts, and be renewed in the spirit of your mind, - Eph 4:22",
            source = "TO RENEW OUR MIND",
            category = "MIND"
        ) // orderIndex 2
        addWisdom(
            text = "Rejoice with those who rejoice, and weep with those who weep - Rom 12:15",
            source = "BE OF THE SAME MIND",
            category = "MIND"
        ) // orderIndex 3
        addWisdom(
            text = "Be of the same mind toward one another. Do not set your mind on high things, but associate with the humble. Do not be wise in your own opinion. - Rom 12:16",
            source = "BE OF THE SAME MIND",
            category = "MIND"
        ) // orderIndex 4
        addWisdom(
            text = "Finally, all of you be of one mind, having compassion for one another; love as brothers, be tenderhearted, be courteous; - 1Pe 3:8",
            source = "BE OF THE SAME MIND",
            category = "MIND"
        ) // orderIndex 5
        addWisdom(
            text = "fulfill my joy by being like-minded, having the same love, being of one accord, of one mind. - Phl 2:2",
            source = "BE OF THE SAME MIND",
            category = "MIND"
        ) // orderIndex 6
        addWisdom(
            text = "Let nothing be done through selfish ambition or conceit, but in lowliness of mind let each esteem others better than himself. 4 Let each of you look out not only for his own interests, but also for the interests of others. - Phl 2:3",
            source = "DO THINGS IN THE LOWLINESS OF MIND",
            category = "MIND"
        ) // orderIndex 7
        addWisdom(
            text = "Let this mind be in you which was also in Christ Jesus, 6 who, being in the form of God, did not consider it robbery to be equal with God, 7 but made Himself of no reputation, taking the form of a bondservant, and coming in the likeness of men. 8 And being found in appearance as a man, He humbled Himself and became obedient to the point of death, even the death of the cross. - Phl 2:5",
            source = "PUT ON THE MIND OF CHRIST",
            category = "MIND"
        ) // orderIndex 8
        addWisdom(
            text = "Not that I have already attained, or am already perfected; but I press on, that I may lay hold of that for which Christ Jesus has also laid hold of me.13 Brethren, I do not count myself to have apprehended; but one thing I do, forgetting those things which are behind and reaching forward to those things which are ahead, 14 I press toward the goal for the prize of the upward call of God in Christ Jesus.15 Therefore let us, as many as are mature, have this mind; and if in anything you think otherwise, God will reveal even this to you. - Phl 3:12",
            source = "NOT GIVING UP MENTALITY",
            category = "MIND"
        ) // orderIndex 9
        addWisdom(
            text = "Set your mind on things above, not on things on the earth. - Col 3:2",
            source = "SET OUR MINDS ON THINGS ABOVE",
            category = "MIND"
        ) // orderIndex 10

        // THINK
        addWisdom(
            text = "For I say, through the grace given to me, to everyone who is among you, not to think of himself more highly than he ought to think, but to think soberly, as God has dealt to each one a measure of faith. - Rom 12:3",
            source = "NOT THINK HIGHLY OF YOURSELF",
            category = "THINK"
        ) // orderIndex 11

        // ONE ANOTHER
        addWisdom(
            text = "Be kindly affectionate to one another with brotherly love, in honour giving preference to one another; - Rom 12:10",
            source = "TO BE KINDLY AFFECTIONATE",
            category = "ONE ANOTHER"
        ) // orderIndex 12
        addWisdom(
            text = "And be kind to one another, tender-hearted, forgiving one another, even as God in Christ forgave you. - Eph 4:32",
            source = "TO BE KINDLY AFFECTIONATE",
            category = "ONE ANOTHER"
        ) // orderIndex 13
        addWisdom(
            text = "We then who are strong ought to bear with the scruples of the weak, and not to please ourselves. - Rom 15:1",
            source = "BEAR WITH THE SCRUPLES OF THE WEAK",
            category = "ONE ANOTHER"
        ) // orderIndex 14
        addWisdom(
            text = "Now we exhort you, brethren, warn those who are unruly, comfort the fainthearted, uphold the weak, be patient with all. - 1Th 5:14",
            source = "BEAR WITH THE SCRUPLES OF THE WEAK",
            category = "ONE ANOTHER"
        ) // orderIndex 15
        addWisdom(
            text = "Therefore receive one another, just as Christ also received us, to the glory of God. - Rom 15:7",
            source = "RECEIVE ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 16
        addWisdom(
            text = "Be hospitable to one another without grumbling. - 1Pe 4:9",
            source = "RECEIVE ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 17
        addWisdom(
            text = "distributing to the needs of the saints, given to hospitality - Rom 12:13",
            source = "HELP THE NEEDY BROTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 18
        addWisdom(
            text = "Since you have purified your souls in obeying the truth through the Spirit in sincere love of the brethren, love one another fervently with a pure heart, - 1Pe 1:22",
            source = "LOVE ONE ANOTHER FERVENTLY WITH A PURE HEART",
            category = "ONE ANOTHER"
        ) // orderIndex 19
        addWisdom(
            text = "And above all things have fervent love for one another, for “love will cover a multitude of sins.” - 1Pe 4:8",
            source = "LOVE ONE ANOTHER FERVENTLY WITH A PURE HEART",
            category = "ONE ANOTHER"
        ) // orderIndex 20
        addWisdom(
            text = "As each one has received a gift, minister it to one another, as good stewards of the manifold grace of God - 1Pe 4:10",
            source = "MINISTER YOUR GIFT TO ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 21
        addWisdom(
            text = "speaking to one another in psalms and hymns and spiritual songs, singing and making melody in your heart to the Lord, 20 giving thanks always for all things to God the Father in the name of our Lord Jesus Christ, - Eph 5:19",
            source = "SPEAK WITH ONE ANOTHER IN",
            category = "ONE ANOTHER"
        ) // orderIndex 22
        addWisdom(
            text = "Do not speak evil of one another, brethren. He who speaks evil of a brother and judges his brother, speaks evil of the law and judges the law. But if you judge the law, you are not a doer of the law but a judge. - Jas 4:11",
            source = "SPEAK WITH ONE ANOTHER IN",
            category = "ONE ANOTHER"
        ) // orderIndex 23
        addWisdom(
            text = "submitting to one another in the fear of God. - Eph 5:21",
            source = "SUBMIT TO ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 24
        addWisdom(
            text = "Do not grumble against one another, brethren, lest you be condemned. Behold, the Judge is standing at the door! - Jas 5:9",
            source = "DO NOT GRUMBLE TO ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 25
        addWisdom(
            text = "Confess your trespasses to one another, and pray for one another, that you may be healed. The effective, fervent prayer of a righteous man avails much. - Jas 5:16",
            source = "CONFESS YOUR TRESPASS TO ONE ANOTHER",
            category = "ONE ANOTHER"
        ) // orderIndex 26
        addWisdom(
            text = "But we were gentle among you, just as a nursing mother cherishes her own children. - 1Th 2:7",
            source = "BEHAVE",
            category = "ONE ANOTHER"
        ) // orderIndex 27
        addWisdom(
            text = "You are witnesses, and God also, how devoutly and justly and blamelessly we behaved ourselves among you who believe; - 1Th 2:10",
            source = "BEHAVE",
            category = "ONE ANOTHER"
        ) // orderIndex 28
        addWisdom(
            text = "For you remember, brethren, our labor and toil; for laboring night and day, that we might not be a burden to any of you, we preached to you the gospel of God. - 1Th 2:9",
            source = "BEHAVE",
            category = "ONE ANOTHER"
        ) // orderIndex 29
        addWisdom(
            text = "as you know how we exhorted, and comforted, and charged every one of you, as a father does his own children, - 1Th 2:11",
            source = "BEHAVE",
            category = "ONE ANOTHER"
        ) // orderIndex 30
        addWisdom(
            text = "So then, my beloved brethren, let every man be swift to hear, slow to speak, slow to wrath; 20 for the wrath of man does not produce the righteousness of God. - Jas 1:19",
            source = "BEHAVE",
            category = "ONE ANOTHER"
        ) // orderIndex 31

        // DILIGENCE
        addWisdom(
            text = "not lagging in diligence, fervent in spirit, serving the Lord; - Rom 12:11",
            source = "BE DILIGENT",
            category = "DILIGENCE"
        ) // orderIndex 32
        addWisdom(
            text = "But also for this very reason, giving all diligence, add to your faith virtue, to virtue knowledge, 6 to knowledge self-control, to self-control perseverance, to perseverance godliness, 7 to godliness brotherly kindness, and to brotherly kindness love. - 2Pe 1:5",
            source = "BE DILIGENT",
            category = "DILIGENCE"
        ) // orderIndex 33
        addWisdom(
            text = "Therefore, brethren, be even more diligent to make your call and election sure, for if you do these things you will never stumble; - 2Pe 1:10",
            source = "BE DILIGENT",
            category = "DILIGENCE"
        ) // orderIndex 34

        // REJOICE
        addWisdom(
            text = "Rejoicing in hope, … - Rom 12:12",
            source = "REJOICE IN HOPE",
            category = "REJOICE"
        ) // orderIndex 35
        addWisdom(
            text = "Finally, my brethren, rejoice in the Lord. For me to write the same things to you is not tedious, but for you it is safe. - Phl 3:1",
            source = "REJOICE IN THE LORD",
            category = "REJOICE"
        ) // orderIndex 36
        addWisdom(
            text = "Rejoice in the Lord always. Again I will say, rejoice! - Phl 4:4",
            source = "REJOICE IN THE LORD",
            category = "REJOICE"
        ) // orderIndex 37
        addWisdom(
            text = "Rejoice always, - 1Th 5:16",
            source = "REJOICE ALWAYS",
            category = "REJOICE"
        ) // orderIndex 38

        // PATIENT
        addWisdom(
            text = "…Patient in tribulation… - Rom 12:12", // Note: This is part of Rom 12:12, also used for Rejoice and Prayer
            source = "PATIENT ON THE TRIBULATION",
            category = "PATIENT"
        ) // orderIndex 39
        addWisdom(
            text = "Therefore be patient, brethren, until the coming of the Lord. See how the farmer waits for the precious fruit of the earth, waiting patiently for it until it receives the early and latter rain. 8 You also be patient. Establish your hearts, for the coming of the Lord is at hand. - Jas 5:7",
            source = "BE PATIENT",
            category = "PATIENT"
        ) // orderIndex 40

        // PRAYER
        addWisdom(
            text = "…Continuing steadfastly in prayer; - Rom 12:12", // Note: This is part of Rom 12:12
            source = "BE STEADFAST IN YOUR PRAYERS",
            category = "PRAYER"
        ) // orderIndex 41
        addWisdom(
            text = "But the end of all things is at hand; therefore be serious and watchful in your prayers. - 1Pe 4:7",
            source = "BE SERIOUS AND WATCHFUL IN PRAYERS",
            category = "PRAYER"
        ) // orderIndex 42
        addWisdom(
            text = "Be sober, be vigilant; because your adversary the devil walks about like a roaring lion, seeking whom he may devour. - 1Pe 5:8",
            source = "BE SERIOUS AND WATCHFUL IN PRAYERS", // Grouping with 1Pe 4:7
            category = "PRAYER"
        ) // orderIndex 43
        addWisdom(
            text = "Continue earnestly in prayer, being vigilant in it with thanksgiving; - Col 4:2",
            source = "BE SERIOUS AND WATCHFUL IN PRAYERS", // Grouping
            category = "PRAYER"
        ) // orderIndex 44
        addWisdom(
            text = "Therefore let us not sleep, as others do, but let us watch and be sober. - 1Th 5:6",
            source = "BE SERIOUS AND WATCHFUL IN PRAYERS", // Grouping
            category = "PRAYER"
        ) // orderIndex 45
        addWisdom(
            text = "praying always with all prayer and supplication in the Spirit, being watchful to this end with all perseverance and supplication for all the saints— - Eph 6:18",
            source = "ALWAYS PRAY WITH SUPPLICATION IN THE SPIRIT",
            category = "PRAYER"
        ) // orderIndex 46
        addWisdom(
            text = "pray without ceasing, - 1Th 5:17",
            source = "PRAY WITHOUT CEASING",
            category = "PRAYER"
        ) // orderIndex 47

        // BLESS
        addWisdom(
            text = "Bless those who persecute you; bless and do not curse. - Rom 12:14",
            source = "BLESS THOSE WHO PERSECUTE YOU",
            category = "BLESS"
        ) // orderIndex 48

        // EVIL
        addWisdom(
            text = "Repay no one evil for evil. Have regard for good things in the sight of all men. - Rom 12:17",
            source = "REPAY NO EVIL FOR EVIL",
            category = "EVIL"
        ) // orderIndex 49
        addWisdom(
            text = "not returning evil for evil or reviling for reviling, but on the contrary blessing, knowing that you were called to this, that you may inherit a blessing. - 1Pe 3:9",
            source = "REPAY NO EVIL FOR EVIL",
            category = "EVIL"
        ) // orderIndex 50
        addWisdom(
            text = "Do not be overcome by evil, but overcome evil with good. - Rom 12:21",
            source = "OVERCOME EVIL WITH GOOD",
            category = "EVIL"
        ) // orderIndex 51
        addWisdom(
            text = "Abstain from every form of evil. - 1Th 5:22",
            source = "ABSTAIN FROM EVERY FORM OF EVIL",
            category = "EVIL"
        ) // orderIndex 52

        // PEACE
        addWisdom(
            text = "If it is possible, as much as depends on you, live peaceably with all men. - Rom 12:18",
            source = "LIVE A PEACEABLE LIFE",
            category = "PEACE"
        ) // orderIndex 53
        addWisdom(
            text = "and to esteem them very highly in love for their work’s sake. Be at peace among yourselves. - 1Th 5:13",
            source = "LIVE A PEACEABLE LIFE", // Grouping
            category = "PEACE"
        ) // orderIndex 54
        addWisdom(
            text = "And let the peace of God rule in your hearts, to which also you were called in one body; and be thankful. - Col 3:15",
            source = "LET THE PEACE OF GOD RULE IN YOUR HEART",
            category = "PEACE"
        ) // orderIndex 55
        addWisdom(
            text = "Now may the Lord of peace Himself give you peace always in every way. The Lord be with you all. - 2Th 3:16",
            source = "PEACE FROM THE LORD",
            category = "PEACE"
        ) // orderIndex 56

        // AVENGE
        addWisdom(
            text = "Beloved, do not avenge yourselves, but rather give place to wrath; for it is written, “Vengeance is Mine, I will repay,” says the Lord. - Rom 12:19",
            source = "DO NOT AVENGE OURSELVES",
            category = "AVENGE"
        ) // orderIndex 57
        addWisdom(
            text = "Therefore “If your enemy is hungry, feed him; If he is thirsty, give him a drink; For in so doing you will heap coals of fire on his head.” - Rom 12:20",
            source = "DO NOT AVENGE OURSELVES", // Related to previous
            category = "AVENGE"
        ) // orderIndex 58

        // SUBMISSION
        addWisdom(
            text = "Let every soul be subject to the governing authorities. For there is no authority except from God, and the authorities that exist are appointed by God. - Rom 13:1",
            source = "BE SUBJECT TO THE GOVERNING AUTHORITIES",
            category = "SUBMISSION"
        ) // orderIndex 59
        addWisdom(
            text = "Therefore submit yourselves to every ordinance of man for the Lord’s sake, whether to the king as supreme, - 1Pe 2:13",
            source = "BE SUBJECT TO THE GOVERNING AUTHORITIES", // Grouping
            category = "SUBMISSION"
        ) // orderIndex 60
        addWisdom(
            text = "Servants, be submissive to your masters with all fear, not only to the good and gentle, but also to the harsh. - 1Pe 2:18",
            source = "SERVANTS BE SUBMISSIVE TO YOUR MASTER",
            category = "SUBMISSION"
        ) // orderIndex 61
        addWisdom(
            text = "Wives, likewise, be submissive to your own husbands, that even if some do not obey the word, they, without a word, may be won by the conduct of their wives, 2 when they observe your chaste conduct accompanied by fear. - 1Pe 3:1",
            source = "WIFES BE SUBMISSIVE TO YOUR HUSBANDS",
            category = "SUBMISSION"
        ) // orderIndex 62
        addWisdom(
            text = "Likewise you younger people, submit yourselves to your elders. Yes, all of you be submissive to one another, and be clothed with humility, for “God resists the proud, But gives grace to the humble. - 1Pe 5:5",
            source = "YOUNG PEOPLE SUBMITE YOURSELVES TO ELDERS",
            category = "SUBMISSION"
        ) // orderIndex 63
        // Eph 5:21 is already under "ONE ANOTHER" - "SUBMIT TO ONE ANOTHER". If it needs to be under "SUBMISSION" as well, it would be a duplicate text with a different category/source theme. The data.txt lists it here too.
        addWisdom(
            text = "submitting to one another in the fear of God. - Eph 5:21",
            source = "SUBMIT TO ONE ANOTHER", // Source theme from data.txt
            category = "SUBMISSION" // Current category
        ) // orderIndex 64

        // RENDER TO ALL THEIR DUE
        addWisdom(
            text = "Render therefore to all their due: taxes to whom taxes are due, customs to whom customs, fear to whom fear, honor to whom honor. - Rom 13:7",
            source = "RENDER TO ALL THEIR DUE", // No explicit source theme, using category
            category = "RENDER TO ALL THEIR DUE" // Using the main point as category
        ) // orderIndex 65

        // DEBT
        addWisdom(
            text = "Owe no one anything except to love one another, for he who loves another has fulfilled the law. - Rom 13:8",
            source = "OWNE NO ONE ANYTHING EXCEPT TO LOVE ONE ANOTHER",
            category = "DEBT"
        ) // orderIndex 66

        // PUT OFF
        addWisdom(
            text = "The night is far spent, the day is at hand. Therefore let us cast off the works of darkness, and let us put on the armor of light. - Rom 13:12",
            source = "CAST OFF THE WORKS OF DARKNESS AND PUT ON THE ARMOR OF LIGTH",
            category = "PUT OFF"
        ) // orderIndex 67
        addWisdom(
            text = "Therefore, laying aside all malice, all deceit, hypocrisy, envy, and all evil speaking, - 1Pe 2:1",
            source = "LAY ASIDE ALL MALICE, ALL DECEIT, HYPOCRISY, ENVY AND ALL EVIL SPEAKING",
            category = "PUT OFF"
        ) // orderIndex 68
        addWisdom( // Eph 4:22 was already added under MIND - TO RENEW OUR MIND. It appears here again for PUT OFF - PUT OF THE OLD MAN
            text = "that you put off, concerning your former conduct, the old man which grows corrupt according to the deceitful lusts, - Eph 4:22",
            source = "PUT OF THE OLD MAN",
            category = "PUT OFF"
        ) // orderIndex 69
        addWisdom(
            text = "Do not lie to one another, since you have put off the old man with his deeds, - Col 3:9",
            source = "PUT OF THE OLD MAN",
            category = "PUT OFF"
        ) // orderIndex 70
        addWisdom(
            text = "Let all bitterness, wrath, anger, clamor, and evil speaking be put away from you, with all malice. - Eph 4:31",
            source = "PUT AWAY FROM YOU",
            category = "PUT OFF"
        ) // orderIndex 71
        addWisdom(
            text = "But now you yourselves are to put off all these: anger, wrath, malice, blasphemy, filthy language out of your mouth. - Col 3:8",
            source = "PUT AWAY FROM YOU", // Grouping
            category = "PUT OFF"
        ) // orderIndex 72
        addWisdom(
            text = "But fornication and all uncleanness or covetousness, let it not even be named among you, as is fitting for saints; 4 neither filthiness, nor foolish talking, nor coarse jesting, which are not fitting, but rather giving of thanks. - Eph 5:3",
            source = "LET THIS NOT BE NAMED AMOUNGST YOU",
            category = "PUT OFF"
        ) // orderIndex 73
        addWisdom(
            text = "Therefore put to death your members which are on the earth: fornication, uncleanness, passion, evil desire, and covetousness, which is idolatry. - Col 3:5",
            source = "PUT TO DEATH OUR MEMBERS WHICH ARE ON THE EARTH",
            category = "PUT OFF"
        ) // orderIndex 74
        addWisdom(
            text = "For this is the will of God, your sanctification: that you should abstain from sexual immorality; - 1Th 4:3",
            source = "ABSTAIN FROM SEXUAL IMMORALITY",
            category = "PUT OFF"
        ) // orderIndex 75
        addWisdom(
            text = "Therefore lay aside all filthiness and overflow of wickedness, and receive with meekness the implanted word, which is able to save your souls. - Jas 1:21",
            source = "LAY ASIDE",
            category = "PUT OFF"
        ) // orderIndex 76

        // PUT ON
        addWisdom(
            text = "But put on the Lord Jesus Christ, and make no provision for the flesh, to fulfill its lusts. - Rom 13:14",
            source = "PUT ON THE LORD JESUS CHRIST AND MAKE NO PROVISION FOR THE FLESH",
            category = "PUT ON"
        ) // orderIndex 77
        addWisdom(
            text = "and that you put on the new man which was created according to God, in true righteousness and holiness. - Eph 4:24",
            source = "PUT ON THE NEW MAN",
            category = "PUT ON"
        ) // orderIndex 78
        addWisdom(
            text = "and have put on the new man who is renewed in knowledge according to the image of Him who created him, 11 where there is neither Greek nor Jew, circumcised nor uncircumcised, barbarian, Scythian, slave nor free, but Christ is all and in all. - Col 3:10",
            source = "PUT ON THE NEW MAN",
            category = "PUT ON"
        ) // orderIndex 79
        addWisdom(
            text = "Therefore, as the elect of God, holy and beloved, put on tender mercies, kindness, humility, meekness, longsuffering; 13 bearing with one another, and forgiving one another, if anyone has a complaint against another; even as Christ forgave you, so you also must do. - Col 3:12",
            source = "PUT ON THE NEW MAN", // Grouping by theme
            category = "PUT ON"
        ) // orderIndex 80
        addWisdom(
            text = "Put on the whole armor of God, that you may be able to stand against the wiles of the devil. - Eph 6:11",
            source = "PUT ON THE WHOLE ARMOR OF GOD",
            category = "PUT ON"
        ) // orderIndex 81
        addWisdom(
            text = "But above all these things put on love, which is the bond of perfection. - Col 3:14",
            source = "PUT ON LOVE",
            category = "PUT ON"
        ) // orderIndex 82
        addWisdom(
            text = "But let us who are of the day be sober, putting on the breastplate of faith and love, and as a helmet the hope of salvation. - 1Th 5:8",
            source = "PUT ON THE BREASTPLATE OF FAITH AND LOVE",
            category = "PUT ON"
        ) // orderIndex 83

        // DISPUTES
        addWisdom(
            text = "Receive one who is weak in the faith, but not to disputes over doubtful things. - Rom 14:1",
            source = "DO NOT DISPUTES OVER DOUBTFUL THINGS",
            category = "DISPUTES"
        ) // orderIndex 84

        // NEIGHTBOUR
        addWisdom(
            text = "Let each of us please his neighbor for his good, leading to edification. - Rom 15:2",
            source = "EACH ONE SHOULD PLEASE HIS NEIGHBOUR",
            category = "NEIGHTBOUR"
        ) // orderIndex 85
        addWisdom(
            text = "Therefore, putting away lying, “Let each one of you speak truth with his neighbor,” for we are members of one another. - Eph 4:25",
            source = "SPEAK TRUTH", // Assuming this is the theme
            category = "NEIGHTBOUR"
        ) // orderIndex 86

        // SPEAK
        addWisdom(
            text = "For I will not dare to speak of any of those things which Christ has not accomplished through me, in word and deed, to make the Gentiles obedient— - Rom 15:18",
            source = "DO NOT SPEAK ANYTHING THAT CHRIST IS NOT ACCOMPLISH THROUGH US",
            category = "SPEAK"
        ) // orderIndex 87
        addWisdom(
            text = "but, speaking the truth in love, may grow up in all things into Him who is the head—Christ— - Eph 4:15",
            source = "SPEAK TRUTH IN LOVE",
            category = "SPEAK"
        ) // orderIndex 88
        // Eph 4:25 already listed under NEIGHBOUR - SPEAK TRUTH. Re-adding for SPEAK category.
        addWisdom(
            text = "Therefore, putting away lying, “Let each one of you speak truth with his neighbor,” for we are members of one another. - Eph 4:25",
            source = "PUTTING AWAY LIES AND SPEAK THE TRUTH",
            category = "SPEAK"
        ) // orderIndex 89
        addWisdom(
            text = "Let no corrupt word proceed out of your mouth, but what is good for necessary edification, that it may impart grace to the hearers. - Eph 4:29",
            source = "LET NO CORRUPT WORD PROCEED OUT OF YOUR MOUTH",
            category = "SPEAK"
        ) // orderIndex 90
        // Eph 5:19 already listed under ONE ANOTHER - SPEAK WITH ONE ANOTHER IN. Re-adding for SPEAK category.
        addWisdom(
            text = "speaking to one another in psalms and hymns and spiritual songs, singing and making melody in your heart to the Lord, 20 giving thanks always for all things to God the Father in the name of our Lord Jesus Christ, - Eph 5:19",
            source = "SPEAK WITH ONE ANOTHER IN",
            category = "SPEAK"
        ) // orderIndex 91
        addWisdom(
            text = "Let your speech always be with grace, seasoned with salt, that you may know how you ought to answer each one. - Col 4:6",
            source = "LET YOUR SPEECH ALWAYS BE WITH GRACE",
            category = "SPEAK"
        ) // orderIndex 92
        addWisdom(
            text = "But as we have been approved by God to be entrusted with the gospel, even so we speak, not as pleasing men, but God who tests our hearts. 5 For neither at any time did we use flattering words, as you know, nor a cloak for covetousness—God is witness. - 1Th 2:4",
            source = "DO NOT SPEAK AS MAN PLEASING",
            category = "SPEAK"
        ) // orderIndex 93
        addWisdom(
            text = "So speak and so do as those who will be judged by the law of liberty. - Jas 2:12",
            source = "SPEAK AS YOU WILL BE JUDGE BY THE LAW",
            category = "SPEAK"
        ) // orderIndex 94
        // Jas 4:11 already listed under ONE ANOTHER - SPEAK WITH ONE ANOTHER IN. Re-adding.
        addWisdom(
            text = "Do not speak evil of one another, brethren. He who speaks evil of a brother and judges his brother, speaks evil of the law and judges the law. But if you judge the law, you are not a doer of the law but a judge. - Jas 4:11",
            source = "DO NOT SPEAK EVIL OF ONE ANOTHER",
            category = "SPEAK"
        ) // orderIndex 95

        // TONGUE
        addWisdom(
            text = "If anyone among you thinks he is religious, and does not bridle his tongue but deceives his own heart, this one’s religion is useless. - Jas 1:26",
            source = "BRIDLE THE TONGUE",
            category = "TONGUE"
        ) // orderIndex 96

        // THANKS
        addWisdom( // Note: Ref "20" is from Eph 5:19-20, already used. Assuming this is a focus on verse 20.
            text = "giving thanks always for all things to God the Father in the name of our Lord Jesus Christ, - Eph 5:20",
            source = "GIVING THANKS TO GOD",
            category = "THANKS"
        ) // orderIndex 97
        addWisdom(
            text = "in everything give thanks; for this is the will of God in Christ Jesus for you. - 1Th 5:18",
            source = "IN EVERYTHING GIVE THANKS",
            category = "THANKS"
        ) // orderIndex 98

        // HOPE
        addWisdom(
            text = "Therefore gird up the loins of your mind, be sober, and rest your hope fully upon the grace that is to be brought to you at the revelation of Jesus Christ; - 1Pe 1:13",
            source = "REST OUR HOPE FULLY UPON THE GRACE",
            category = "HOPE"
        ) // orderIndex 99

        // OLD MAN
        addWisdom(
            text = "as obedient children, not conforming yourselves to the former lusts, as in your ignorance; - 1Pe 1:14",
            source = "DO NOT CONFORM YOURSELF TO THE FORMER LUSTS",
            category = "OLD MAN"
        ) // orderIndex 100
        // Eph 4:22 was already added. Adding again for this specific theme/category.
        addWisdom(
            text = "that you put off, concerning your former conduct, the old man which grows corrupt according to the deceitful lusts, - Eph 4:22",
            source = "PUT OFF THE OLD MAN",
            category = "OLD MAN"
        ) // orderIndex 101

        // HOLY
        addWisdom(
            text = "but as He who called you is holy, you also be holy in all your conduct, 16 because it is written, “Be holy, for I am holy.” - 1Pe 1:15",
            source = "WHAT WE SHOULD BE",
            category = "HOLY"
        ) // orderIndex 102
        addWisdom(
            text = "just as He chose us in Him before the foundation of the world, that we should be holy and without blame before Him in love, - Eph 1:4",
            source = "WHAT WE SHOULD BE",
            category = "HOLY"
        ) // orderIndex 103

        // CONDUCT
        addWisdom(
            text = "And if you call on the Father, who without partiality judges according to each one’s work, conduct yourselves throughout the time of your stay here in fear; - 1Pe 1:17",
            source = "CONDUCT OURSELVES THROUGHOUT THE TIME OF OUR STAYING HERE IN FEAR",
            category = "CONDUCT"
        ) // orderIndex 104
        addWisdom(
            text = "having your conduct honorable among the Gentiles, that when they speak against you as evildoers, they may, by your good works which they observe, glorify God in the day of visitation - 1Pe 2:12",
            source = "HAVE OUR CONDUCT HONORABLE",
            category = "CONDUCT"
        ) // orderIndex 105
        // Eph 4:22 already used multiple times. Adding for this specific context.
        addWisdom(
            text = "that you put off, concerning your former conduct, the old man which grows corrupt according to the deceitful lusts, 23 and be renewed in the spirit of your mind, - Eph 4:22", // text includes v23 here
            source = "PUT OF THE CONDUCT OF THE OLD MAN",
            category = "CONDUCT"
        ) // orderIndex 106
        addWisdom(
            text = "Only let your conduct be worthy of the gospel of Christ, so that whether I come and see you or am absent, I may hear of your affairs, that you stand fast in one spirit, with one mind striving together for the faith of the gospel, - Phl 1:27",
            source = "CONDUCT YOURSELF WORTHY OF THE GOSPEL OF CHRIST",
            category = "CONDUCT"
        ) // orderIndex 107

        // WALK
        addWisdom(
            text = "For we are His workmanship, created in Christ Jesus for good works, which God prepared beforehand that we should walk in them. - Eph 2:10",
            source = "WALK IN THE WORK THAT GOD PREPARE FOR US",
            category = "WALK"
        ) // orderIndex 108
        addWisdom(
            text = "I, therefore, the prisoner of the Lord, beseech you to walk worthy of the calling with which you were called, 2 with all lowliness and gentleness, with longsuffering, bearing with one another in love, 3 endeavoring to keep the unity of the Spirit in the bond of peace. - Eph 4:1",
            source = "WALK WORTHY OF THE CALLING",
            category = "WALK"
        ) // orderIndex 109
        addWisdom(
            text = "This I say, therefore, and testify in the Lord, that you should no longer walk as the rest of the Gentiles walk, in the futility of their mind, 18 having their understanding darkened, being alienated from the life of God, because of the ignorance that is in them, because of the blindness of their heart; 19 who, being past feeling, have given themselves over to lewdness, to work all uncleanness with greediness. - Eph 4:17",
            source = "NOT WALK AS GENTILES",
            category = "WALK"
        ) // orderIndex 110
        addWisdom(
            text = "Let us walk properly, as in the day, not in revelry and drunkenness, not in lewdness and lust, not in strife and envy. - Rom 13:13",
            source = "WALK PROPERLY AS IN THE DAY",
            category = "WALK"
        ) // orderIndex 111
        addWisdom(
            text = "And walk in love, as Christ also has loved us and given Himself for us, an offering and a sacrifice to God for a sweet-smelling aroma. - Eph 5:2",
            source = "WALK IN LOVE",
            category = "WALK"
        ) // orderIndex 112
        addWisdom(
            text = "For you were once darkness, but now you are light in the Lord. Walk as children of light 9 (for the fruit of the Spirit is in all goodness, righteousness, and truth),10 finding out what is acceptable to the Lord. - Eph 5:8",
            source = "WALK AS CHILDREN OF THE LIGHT",
            category = "WALK"
        ) // orderIndex 113
        addWisdom(
            text = "See then that you walk circumspectly, not as fools but as wise, 16 redeeming the time, because the days are evil. 17 Therefore do not be unwise, but understand what the will of the Lord is. - Eph 5:15",
            source = "WALK CIRCUMSPECTLY",
            category = "WALK"
        ) // orderIndex 114
        addWisdom(
            text = "that you also aspire to lead a quiet life, to mind your own business, and to work with your own hands, as we commanded you, 12 that you may walk properly toward those who are outside, and that you may lack nothing. - 1Th 4:11",
            source = "WALK CIRCUMSPECTLY", // Grouping
            category = "WALK"
        ) // orderIndex 115
        addWisdom(
            text = "As you therefore have received Christ Jesus the Lord, so walk in Him, 7 rooted and built up in Him and established in the faith, as you have been taught, abounding in it with thanksgiving. - Col 2:6",
            source = "WALK IN CHRIST",
            category = "WALK"
        ) // orderIndex 116
        addWisdom(
            text = "Walk in wisdom toward those who are outside, redeeming the time. - Col 4:5",
            source = "WALKING IN WISDOM",
            category = "WALK"
        ) // orderIndex 117

        // WORD
        addWisdom(
            text = "as newborn babes, desire the pure milk of the word, that you may grow thereby, - 1Pe 2:2",
            source = "DESIRE THE PURE MILK IF THE WORD",
            category = "WORD"
        ) // orderIndex 118
        addWisdom(
            text = "holding fast the word of life, so that I may rejoice in the day of Christ that I have not run in vain or labored in vain. - Phl 2:16",
            source = "HOLD FAST TO IT",
            category = "WORD"
        ) // orderIndex 119
        addWisdom(
            text = "Let the word of Christ dwell in you richly in all wisdom, teaching and admonishing one another in psalms and hymns and spiritual songs, singing with grace in your hearts to the Lord. - Col 3:16",
            source = "LET THE WORD IF CHRIST DWELL IN YOU RICHLY",
            category = "WORD"
        ) // orderIndex 120
        addWisdom(
            text = "For this reason we also thank God without ceasing, because when you received the word of God which you heard from us, you welcomed it not as the word of men, but as it is in truth, the word of God, which also effectively works in you who believe. - 1Th 2:13",
            source = "WORKS EFFECTIVELY IN THOSE WHO BELIEVE",
            category = "WORD"
        ) // orderIndex 121
        addWisdom( // Jas 1:21 was used for PUT OFF - LAY ASIDE. Adding here for WORD.
            text = "Therefore lay aside all filthiness and overflow of wickedness, and receive with meekness the implanted word, which is able to save your souls. - Jas 1:21",
            source = "RECEIVE THE WORD",
            category = "WORD"
        ) // orderIndex 122
        addWisdom(
            text = "But be doers of the word, and not hearers only, deceiving yourselves. 23 For if anyone is a hearer of the word and not a doer, he is like a man observing his natural face in a mirror; 24 for he observes himself, goes away, and immediately forgets what kind of man he was. 25 But he who looks into the perfect law of liberty and continues in it, and is not a forgetful hearer but a doer of the work, this one will be blessed in what he does. - Jas 1:22",
            source = "BE A DOER OF THE WORD",
            category = "WORD"
        ) // orderIndex 123

        // LIVING STONE
        addWisdom(
            text = "Coming to Him as to a living stone, rejected indeed by men, but chosen by God and precious, 5 you also, as living stones, are being built up a spiritual house, a holy priesthood, to offer up spiritual sacrifices acceptable to God through Jesus Christ. - 1Pe 2:4",
            source = "COME TO HIM AS A LIVING STONE",
            category = "LIVING STONE"
        ) // orderIndex 124

        // PRAISES
        addWisdom(
            text = "But you are a chosen generation, a royal priesthood, a holy nation, His own special people, that you may proclaim the praises of Him who called you out of darkness into His marvelous light; - 1Pe 2:9",
            source = "CALL TO PROCLAIM HIS PRAISES",
            category = "PRAISES"
        ) // orderIndex 125

        // LUSTS
        addWisdom(
            text = "Beloved, I beg you as sojourners and pilgrims, abstain from fleshly lusts which war against the soul, - 1Pe 2:11",
            source = "ABSTAIN FROM FLESHLY LUSTS",
            category = "LUSTS"
        ) // orderIndex 126

        // HONOR
        addWisdom(
            text = "Honor all people. Love the brotherhood. Fear God. Honor the king. - 1Pe 2:17",
            source = "HONOR ALL PEOPLE, FEAR GOD",
            category = "HONOR"
        ) // orderIndex 127

        // SANCTIFICATION
        addWisdom(
            text = "But sanctify the Lord God in your hearts, and always be ready to give a defense to everyone who asks you a reason for the hope that is in you, with meekness and fear; - 1Pe 3:15",
            source = "SANCTIFY THE LORD JESUS IN OUR HEART",
            category = "SANCTIFICATION"
        ) // orderIndex 128

        // GIFTS
        addWisdom( // 1Pe 4:10 was already added for ONE ANOTHER - MINISTER YOUR GIFT. Adding for GIFTS.
            text = "As each one has received a gift, minister it to one another, as good stewards of the manifold grace of God - 1Pe 4:10",
            source = "MINISTER YOUR GIFT TO ONE ANOTHER",
            category = "GIFTS"
        ) // orderIndex 129

        // SERVANTS
        addWisdom( // 1Pe 2:18 was already added for SUBMISSION - SERVANTS BE SUBMISSIVE. Adding for SERVANTS.
            text = "Servants, be submissive to your masters with all fear, not only to the good and gentle, but also to the harsh. - 1Pe 2:18",
            source = "SERVANTS BE SUBMISSIVE TO YOUR MASTER",
            category = "SERVANTS"
        ) // orderIndex 130
        addWisdom(
            text = "Bondservants, be obedient to those who are your masters according to the flesh, with fear and trembling, in sincerity of heart, as to Christ; 6 not with eyeservice, as men-pleasers, but as bondservants of Christ, doing the will of God from the heart, 7 with goodwill doing service, as to the Lord, and not to men, 8 knowing that whatever good anyone does, he will receive the same from the Lord, whether he is a slave or free. - Eph 6:5",
            source = "BE OBEDIENT TO YOUR MASTER",
            category = "SERVANTS"
        ) // orderIndex 131
        addWisdom(
            text = "Bondservants, obey in all things your masters according to the flesh, not with eyeservice, as men-pleasers, but in sincerity of heart, fearing God. 23 And whatever you do, do it heartily, as to the Lord and not to men, 24 knowing that from the Lord you will receive the reward of the inheritance; for you serve the Lord Christ. 25 But he who does wrong will be repaid for what he has done, and there is no partiality. - Col 3:22",
            source = "BE OBEDIENT TO YOUR MASTER", // Grouping
            category = "SERVANTS"
        ) // orderIndex 132

        // MASTERS
        addWisdom(
            text = "And you, masters, do the same things to them, giving up threatening, knowing that your own Master also is in heaven, and there is no partiality with Him. - Eph 6:9",
            source = "GIVE UP THREATENING",
            category = "MASTERS"
        ) // orderIndex 133
        addWisdom(
            text = "Masters, give your bondservants what is just and fair, knowing that you also have a Master in heaven. - Col 4:1",
            source = "GIVE YOUR BONDSERVANT WHAT IS JUST AND FAIR",
            category = "MASTERS"
        ) // orderIndex 134

        // HUSBANDS | FATHERS
        addWisdom(
            text = "Husbands, likewise, dwell with them with understanding, giving honor to the wife, as to the weaker vessel, and as being heirs together of the grace of life, that your prayers may not be hindered. - 1Pe 3:7",
            source = "HUSBANDS DWELL WITH THEM WITH UNDERSTANDING",
            category = "HUSBANDS | FATHERS"
        ) // orderIndex 135
        addWisdom(
            text = "Husbands, love your wives, just as Christ also loved the church and gave Himself for her, 26 that He might sanctify and cleanse her with the washing of water by the word, 27 that He might present her to Himself a glorious church, not having spot or wrinkle or any such thing, but that she should be holy and without blemish. 28 So husbands ought to love their own wives as their own bodies; he who loves his wife loves himself. 29 For no one ever hated his own flesh, but nourishes and cherishes it, just as the Lord does the church. 30 For we are members of His body, of His flesh and of His bones. 31 “For this reason a man shall leave his father and mother and be joined to his wife, and the two shall become one flesh.” 32 This is a great mystery, but I speak concerning Christ and the church. 33 Nevertheless let each one of you in particular so love his own wife as himself, and let the wife see that she respects her husband. - Eph 5:25",
            source = "HUSBANDS LOVE YOUR WIFES",
            category = "HUSBANDS | FATHERS"
        ) // orderIndex 136
        addWisdom(
            text = "Husbands, love your wives and do not be bitter toward them. - Col 3:19",
            source = "HUSBANDS LOVE YOUR WIFES", // Grouping
            category = "HUSBANDS | FATHERS"
        ) // orderIndex 137
        addWisdom(
            text = "And you, fathers, do not provoke your children to wrath, but bring them up in the training and admonition of the Lord. - Eph 6:4",
            source = "FATHERS DO NOT PROVOKE YOUR CHILDREN TO WRATH",
            category = "HUSBANDS | FATHERS"
        ) // orderIndex 138
        addWisdom(
            text = "Fathers, do not provoke your children, lest they become discouraged. - Col 3:21",
            source = "FATHERS DO NOT PROVOKE YOUR CHILDREN TO WRATH", // Grouping
            category = "HUSBANDS | FATHERS"
        ) // orderIndex 139

        // WIFES
        addWisdom( // 1Pe 3:1 was used for SUBMISSION - WIFES BE SUBMISSIVE. Adding for WIFES.
            text = "Wives, likewise, be submissive to your own husbands, that even if some do not obey the word, they, without a word, may be won by the conduct of their wives, 2 when they observe your chaste conduct accompanied by fear. - 1Pe 3:1",
            source = "BE SUBMISSIVE TO YOUR HUSBANDS",
            category = "WIFES"
        ) // orderIndex 140
        addWisdom(
            text = "Wives, submit to your own husbands, as to the Lord. - Eph 5:22",
            source = "BE SUBMISSIVE TO YOUR HUSBANDS", // Grouping
            category = "WIFES"
        ) // orderIndex 141
        addWisdom(
            text = "Wives, submit to your own husbands, as is fitting in the Lord. - Col 3:18",
            source = "BE SUBMISSIVE TO YOUR HUSBANDS", // Grouping
            category = "WIFES"
        ) // orderIndex 142

        // ELDERS
        addWisdom(
            text = "Shepherd the flock of God which is among you, serving as overseers, not by compulsion but willingly, not for dishonest gain but eagerly; - 1Pe 5:2",
            source = "ELDERS SHOULD SHEPHERD THE FLOCK OF GOD WILLINGLY AND EAGERLY",
            category = "ELDERS"
        ) // orderIndex 143

        // YOUNG PEOPLE
        addWisdom( // 1Pe 5:5 was used for SUBMISSION - YOUNG PEOPLE SUBMITE. Adding for YOUNG PEOPLE.
            text = "Likewise you younger people, submit yourselves to your elders. Yes, all of you be submissive to one another, and be clothed with humility, for “God resists the proud, But gives grace to the humble. - 1Pe 5:5",
            source = "YOUNG PEOPLE SUBMITE YOURSELVES TO ELDERS",
            category = "YOUNG PEOPLE"
        ) // orderIndex 144
        addWisdom(
            text = "Therefore be imitators of God as dear children. - Eph 5:1",
            source = "IMITATE GOD AS IS DEAR CHILDREN",
            category = "YOUNG PEOPLE"
        ) // orderIndex 145
        addWisdom(
            text = "Children, obey your parents in the Lord, for this is right. - Eph 6:1",
            source = "OBEY YOUR PARENTS",
            category = "YOUNG PEOPLE"
        ) // orderIndex 146
        addWisdom(
            text = "Children, obey your parents in all things, for this is well pleasing to the Lord. - Col 3:20",
            source = "OBEY YOUR PARENTS", // Grouping
            category = "YOUNG PEOPLE"
        ) // orderIndex 147

        // DEVIL
        addWisdom( // 1Pe 5:8 was used for PRAYER - BE SERIOUS. Adding for DEVIL.
            text = "Be sober, be vigilant; because your adversary the devil walks about like a roaring lion, seeking whom he may devour. Resist him, steadfast in the faith, knowing that the same sufferings are experienced by your brotherhood in the world. - 1Pe 5:8-9", // Combined 5:8 and 5:9 as they flow
            source = "RESIST THE DEVIL THAT HE WILL FLEE FROM YOU",
            category = "DEVIL"
        ) // orderIndex 148
        addWisdom(
            text = "“Be angry, and do not sin”: do not let the sun go down on your wrath, 27 nor give place to the devil. - Eph 4:26",
            source = "RESIST THE DEVIL THAT HE WILL FLEE FROM YOU", // Grouping
            category = "DEVIL"
        ) // orderIndex 149
        addWisdom( // Eph 6:11 was used for PUT ON - PUT ON WHOLE ARMOR. Adding for DEVIL.
            text = "Put on the whole armor of God, that you may be able to stand against the wiles of the devil. - Eph 6:11",
            source = "RESIST THE DEVIL THAT HE WILL FLEE FROM YOU", // Grouping
            category = "DEVIL"
        ) // orderIndex 150
        addWisdom(
            text = "Therefore submit to God. Resist the devil and he will flee from you. - Jas 4:7",
            source = "RESIST THE DEVIL THAT HE WILL FLEE FROM YOU", // Grouping
            category = "DEVIL"
        ) // orderIndex 151

        // LIES
        addWisdom( // Eph 4:25 was used multiple times. Adding for LIES.
            text = "Therefore, putting away lying, “Let each one of you speak truth with his neighbor,” for we are members of one another. - Eph 4:25",
            source = "PUTTING AWAY LIES",
            category = "LIES"
        ) // orderIndex 152

        // ANGRY
        addWisdom( // Eph 4:26 was used for DEVIL. Adding for ANGRY.
            text = "“Be angry, and do not sin”: do not let the sun go down on your wrath, - Eph 4:26", // Only v26 here
            source = "BE ANGRY AND DO NOT SIN",
            category = "ANGRY"
        ) // orderIndex 153

        // DO NOT STEAL
        addWisdom(
            text = "Let him who stole steal no longer, but rather let him labor, working with his hands what is good, that he may have something to give him who has need. - Eph 4:28",
            source = "DO NOT STEAL",
            category = "DO NOT STEAL"
        ) // orderIndex 154

        // HOLY SPIRIT
        addWisdom(
            text = "And do not grieve the Holy Spirit of God, by whom you were sealed for the day of redemption. - Eph 4:30",
            source = "DO NOT GRIEVE THE HOLY SPIRIT",
            category = "HOLY SPIRIT"
        ) // orderIndex 155
        addWisdom(
            text = "Do not quench the Spirit. - 1Th 5:19",
            source = "DO NOT QUENCH THE SPIRIT",
            category = "HOLY SPIRIT"
        ) // orderIndex 156
        addWisdom(
            text = "And do not be drunk with wine, in which is dissipation; but be filled with the Spirit, - Eph 5:18",
            source = "BE FIILED WITH THE HOLY SPIRIT",
            category = "HOLY SPIRIT"
        ) // orderIndex 157

        // FELLOWSHIP
        addWisdom(
            text = "And have no fellowship with the unfruitful works of darkness, but rather expose them. - Eph 5:11",
            source = "HAVE NO FELLOSHIP WITH THE UNFRUITIFUL WORKS OF DARKNESS",
            category = "FELLOWSHIP"
        ) // orderIndex 158

        // DRUNK
        addWisdom( // Eph 5:18 was used for HOLY SPIRIT. Adding for DRUNK.
            text = "And do not be drunk with wine, in which is dissipation; but be filled with the Spirit, - Eph 5:18",
            source = "DO NOT BE DRUNK WITH WINE",
            category = "DRUNK"
        ) // orderIndex 159

        // STRENGTH
        addWisdom(
            text = "Finally, my brethren, be strong in the Lord and in the power of His might. - Eph 6:10",
            source = "BE STRONG IN THE LORD",
            category = "STRENGTH"
        ) // orderIndex 160
        addWisdom(
            text = "I can do all things through Christ who strengthens me - Phl 4:13",
            source = "DO ALL THINGS",
            category = "STRENGTH"
        ) // orderIndex 161

        // SALVATION
        addWisdom(
            text = "Therefore, my beloved, as you have always obeyed, not as in my presence only, but now much more in my absence, work out your own salvation with fear and trembling; - Phl 2:12",
            source = "WORK OUT YOUR SALVATION WITH FEAR AND TREMBLING",
            category = "SALVATION"
        ) // orderIndex 162

        // DOING
        addWisdom(
            text = "Do all things without complaining and disputing, 15 that you may become blameless and harmless, children of God without fault in the midst of a crooked and perverse generation, among whom you shine as lights in the world, - Phl 2:14",
            source = "DO ALL THINGS WITHOUT COMPLAIN AND DISUTING",
            category = "DOING"
        ) // orderIndex 163
        addWisdom( // Phl 4:13 was used for STRENGTH. Adding for DOING.
            text = "I can do all things through Christ who strengthens me - Phl 4:13",
            source = "DO ALL THINGS",
            category = "DOING"
        ) // orderIndex 164
        addWisdom(
            text = "But as for you, brethren, do not grow weary in doing good. - 2Th 3:13",
            source = "GROWING WEARY IN DOING GOOD",
            category = "DOING"
        ) // orderIndex 165
        addWisdom(
            text = "Therefore, to him who knows to do good and does not do it, to him it is sin. - Jas 4:17",
            source = "KNOW AND NOT DOING",
            category = "DOING"
        ) // orderIndex 166

        // CONFIDENCE
        addWisdom(
            text = "For we are the circumcision, who worship God in the Spirit, rejoice in Christ Jesus, and have no confidence in the flesh, - Phl 3:3",
            source = "PUT NO CONFIDENCE IN THE FLESH",
            category = "CONFIDENCE"
        ) // orderIndex 167

        // GENTLENESS
        addWisdom(
            text = "Let your gentleness be known to all men. The Lord is at hand. - Phl 4:5",
            source = "LET OUR GENTLENESS BE KNOWN TO ALL",
            category = "GENTLENESS"
        ) // orderIndex 168

        // ANXIETY
        addWisdom(
            text = "Be anxious for nothing, but in everything by prayer and supplication, with thanksgiving, let your requests be made known to God; 7 and the peace of God, which surpasses all understanding, will guard your hearts and minds through Christ Jesus. - Phl 4:6",
            source = "BE ANXIOUS FOR NOTHING",
            category = "ANXIETY"
        ) // orderIndex 169

        // MEDITATION
        addWisdom(
            text = "Finally, brethren, whatever things are true, whatever things are noble, whatever things are just, whatever things are pure, whatever things are lovely, whatever things are of good report, if there is any virtue and if there is anything praiseworthy—meditate on these things. - Phl 4:8",
            source = "MEDITATE ON THESE",
            category = "MEDITATION"
        ) // orderIndex 170

        // CONTENT
        addWisdom(
            text = "Not that I speak in regard to need, for I have learned in whatever state I am, to be content: - Phl 4:11",
            source = "BE CONTENT IN ANY STATE THAT YOU ARE IN",
            category = "CONTENT"
        ) // orderIndex 171

        // SEEK
        addWisdom(
            text = "If then you were raised with Christ, seek those things which are above, where Christ is, sitting at the right hand of God. - Col 3:1",
            source = "SEEK THOSE THINGS WHICH ARE ABOVE",
            category = "SEEK"
        ) // orderIndex 172
        addWisdom(
            text = "Nor did we seek glory from men, either from you or from others, when we might have made demands as apostles of Christ. - 1Th 2:6",
            source = "DON’T SEEK GLORY FROM MAN",
            category = "SEEK"
        ) // orderIndex 173

        // PROPHECIES
        addWisdom(
            text = "Do not despise prophecies. Test all things; hold fast what is good. - 1Th 5:20-21", // Combined verses
            source = "TEST ALL THINGS", // Based on v21
            category = "PROPHECIES"
        ) // orderIndex 174

        // CUT OFF
        addWisdom(
            text = "And if anyone does not obey our word in this epistle, note that person and do not keep company with him, that he may be ashamed. 15 Yet do not count him as an enemy, but admonish him as a brother. - 2Th 3:14",
            source = "THE DISOBIENT BROTHER",
            category = "CUT OFF"
        ) // orderIndex 175

        // PARTIALITY
        addWisdom(
            text = "My brethren, do not hold the faith of our Lord Jesus Christ, the Lord of glory, with partiality. 2 For if there should come into your assembly a man with gold rings, in fine apparel, and there should also come in a poor man in filthy clothes, 3 and you pay attention to the one wearing the fine clothes and say to him, “You sit here in a good place,” and say to the poor man, “You stand there,” or, “Sit here at my footstool,” 4 have you not shown partiality among yourselves, and become judges with evil thoughts? - Jas 2:1",
            source = "DO NOT SHOW PARTIALITY",
            category = "PARTIALITY"
        ) // orderIndex 176

        // WORLD
        addWisdom(
            text = "Adulterers and adulteresses! Do you not know that friendship with the world is enmity with God? Whoever therefore wants to be a friend of the world makes himself an enemy of God. - Jas 4:4",
            source = "FRIENDS WITH THE WORLD IS ENMITY WITH GOD",
            category = "WORLD"
        ) // orderIndex 177

        // GOD
        addWisdom(
            text = "Draw near to God and He will draw near to you. Cleanse your hands, you sinners; and purify your hearts, you double-minded. - Jas 4:8",
            source = "DRAW NEAR TO GOD",
            category = "GOD"
        ) // orderIndex 178

        // HUMBLE
        addWisdom(
            text = "Humble yourselves in the sight of the Lord, and He will lift you up. - Jas 4:10",
            source = "HUMBLE YOURSELVES IN THE SIGHT OF THE LORD",
            category = "HUMBLE"
        ) // orderIndex 179
        addWisdom( // Jas 4:6 was used for SUBMISSION - YOUNG PEOPLE. Adding for HUMBLE
            text = "But He gives more grace. Therefore He says: “God resists the proud, But gives grace to the humble.” - Jas 4:6",
            source = "GOD GIVES GRACE TO THE HUMBLE",
            category = "HUMBLE"
        ) // orderIndex 180

        return wisdomList
    }
}