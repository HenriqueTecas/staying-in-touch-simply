# Related Work: verified references

Verified on 2026-10-05. The BibTeX keys below match `references.bib`.

**How verification was done**
- Every DOI was checked against its Crossref record (`https://api.crossref.org/works/<doi>`) and resolved through `https://doi.org/<doi>`. All 24 DOIs redirect (HTTP 302) to the publisher.
- A script compared each DOI-bearing `.bib` entry with its Crossref record: title, author list and order, volume, issue, pages, page count, year and DOI. All 24 match.
- ACM article numbers come from DBLP, queried through its SPARQL endpoint (`https://sparql.dblp.org`). The dblp.org website and the ACM Digital Library both block automated access (bot check / Cloudflare), so I could not read them directly. Record URLs are given as `https://dblp.org/rec/<key>`.
- The two entries without a DOI were checked on the publisher's page (USENIX) and in library catalogues (Open Library, Internet Archive). The Double Diamond was checked on the Design Council's own site.

| Topic | Verified refs |
|---|---|
| 1. Older adults' difficulties with phones and touchscreens | 5 |
| 2. Older adults and voice assistants | 5 |
| 3. Simplified/senior interfaces, launchers, stigma | 7 |
| 4. Participatory design / co-design and generative methods | 5 |
| 5. Tangible / physical interfaces for older adults | 2 |
| 6. Scams and security | 1 |
| Methodology (not Related Work) | 2 |
| **Total** | **27** (25 Related Work + 2 methodology) |

Two papers by Hugo Nicolau are included: `nicolau2012elderly` (topic 1) and `urbano2022skeuomorphism` (topic 3).

---

## 1. Older adults' difficulties with mobile phones, smartphones and touchscreens

### `kurniawan2008older`
- **Citation:** Sri Kurniawan. 2008. Older people and mobile phones: A multi-method investigation. *International Journal of Human-Computer Studies* (IJHCS) 66, 12, 889–901. https://doi.org/10.1016/j.ijhcs.2008.03.002
- **Summary:** Combined Delphi interviews, focus groups and an online survey to study how people aged 60+ use mobile phones and what an "ageing-friendly" phone should offer. Older people were mostly passive phone users and feared the consequences of using unfamiliar technology. The design features they wanted most were aids that compensate for declining functional abilities.
- **Relevance:** Directly supports two choices: a small set of familiar communication actions, and a design that reduces the fear of "doing something wrong".
- **Verified:** https://api.crossref.org/works/10.1016/j.ijhcs.2008.03.002 · https://doi.org/10.1016/j.ijhcs.2008.03.002
- *Note:* I could not reach the publisher's abstract (ScienceDirect blocks automated access). This summary is based on the Semantic Scholar summary and descriptions of the paper found through search. Check the PDF before quoting specifics.

### `leung2012older`
- **Citation:** Rock Leung, Charlotte Tang, Shathel Haddad, Joanna McGrenere, Peter Graf, and Vilia Ingriany. 2012. How Older Adults Learn to Use Mobile Devices: Survey and Field Investigations. *ACM Transactions on Accessible Computing* (TACCESS) 4, 3, Article 11, 33 pages. https://doi.org/10.1145/2399193.2399195
- **Summary:** A survey of 131 people in three age groups (20–49, 50–64, 65+) and a field study with 6 adults aged 50+ examined how people learn mobile devices. Preference for trial-and-error learning drops with age. Over half of the older participants preferred the instruction manual, yet many had trouble using it. The authors illustrate design implications with a help system called Help Kiosk.
- **Relevance:** Argues against designs that need exploration to learn, and supports a device with a fixed, small set of actions that can be explained once.
- **Verified:** https://api.crossref.org/works/10.1145/2399193.2399195 · https://doi.org/10.1145/2399193.2399195 · Article 11 from DBLP `journals/taccess/LeungTHMGI12` (https://dblp.org/rec/journals/taccess/LeungTHMGI12)

### `mitzner2010older`
- **Citation:** Tracy L. Mitzner, Julie B. Boron, Cara Bailey Fausset, Anne E. Adams, Neil Charness, Sara J. Czaja, Katinka Dijkstra, Arthur D. Fisk, Wendy A. Rogers, and Joseph Sharit. 2010. Older adults talk technology: Technology usage and attitudes. *Computers in Human Behavior* (CHB) 26, 6, 1710–1721. https://doi.org/10.1016/j.chb.2010.06.020
- **Summary:** In focus groups, 113 older adults discussed technology at home, at work and in healthcare. Likes outnumbered dislikes, contradicting the stereotype that older adults fear or reject technology. Dislikes centred on inconvenience, unhelpful features, and security and reliability concerns.
- **Relevance:** Treats the co-designer as a capable user who weighs benefits against hassle, which supports cutting unhelpful features rather than "dumbing down".
- **Verified:** https://api.crossref.org/works/10.1016/j.chb.2010.06.020 · https://doi.org/10.1016/j.chb.2010.06.020

### `czaja2006factors`
- **Citation:** Sara J. Czaja, Neil Charness, Arthur D. Fisk, Christopher Hertzog, Sankaran N. Nair, Wendy A. Rogers, and Joseph Sharit. 2006. Factors predicting the use of technology: Findings from the Center for Research and Education on Aging and Technology Enhancement (CREATE). *Psychology and Aging* (Psychol. Aging) 21, 2, 333–352. https://doi.org/10.1037/0882-7974.21.2.333
- **Summary:** A survey of 1,204 adults aged 18–91 found that older adults used technology, computers and the Web less than younger adults. Computer anxiety, fluid intelligence and crystallised intelligence predicted technology use. The link between age and adoption was mediated by cognitive abilities, computer self-efficacy and computer anxiety.
- **Relevance:** Shows that anxiety and self-efficacy, not age itself, drive adoption, which motivates a low-anxiety design (one button, few actions).
- **Verified:** https://api.crossref.org/works/10.1037/0882-7974.21.2.333 · https://doi.org/10.1037/0882-7974.21.2.333

### `nicolau2012elderly` (Hugo Nicolau)
- **Citation:** Hugo Nicolau and Joaquim Jorge. 2012. Elderly text-entry performance on touchscreens. In *Proceedings of the 14th International ACM SIGACCESS Conference on Computers and Accessibility* (ASSETS '12), Boulder, CO, USA. ACM, 127–134. https://doi.org/10.1145/2384916.2384939
- **Summary:** 15 older adults typed on a phone and a tablet touchscreen. Omissions were the most common error, mostly cognitive in origin, followed by substitutions and insertions. Tablets compensated for about 9% of errors, and measured hand tremor correlated strongly with text-entry errors.
- **Relevance:** Evidence from Técnico itself that touchscreen typing is error-prone for older adults, which motivates replacing typing with a physical button and speech.
- **Verified:** https://api.crossref.org/works/10.1145/2384916.2384939 · https://doi.org/10.1145/2384916.2384939 · DBLP `conf/assets/NicolauJ12`

---

## 2. Older adults and voice assistants

### `pradhan2020use`
- **Citation:** Alisha Pradhan, Amanda Lazar, and Leah Findlater. 2020. Use of Intelligent Voice Assistants by Older Adults with Low Technology Use. *ACM Transactions on Computer-Human Interaction* (TOCHI) 27, 4, Article 31, 27 pages. https://doi.org/10.1145/3373759
- **Summary:** Amazon Echo Dots were deployed for 3 weeks in the homes of 7 older adults who rarely use technology. Use for finding online information, much of it health-related, stayed steady, while other uses such as music declined. Memory features (timers, reminders) were first seen as useful but were rarely used because participants doubted their reliability.
- **Relevance:** Voice can reach people who avoid smartphones, but reminders, one of our core actions, will only be used if they are trusted to be reliable.
- **Verified:** https://api.crossref.org/works/10.1145/3373759 · https://doi.org/10.1145/3373759 · Article 31 from DBLP `journals/tochi/PradhanLF20`

### `trajkova2020alexa`
- **Citation:** Milka Trajkova and Aqueasha Martin-Hammond. 2020. "Alexa is a Toy": Exploring Older Adults' Reasons for Using, Limiting, and Abandoning Echo. In *Proceedings of the 2020 CHI Conference on Human Factors in Computing Systems* (CHI '20), Honolulu, HI, USA. ACM, 13 pages. https://doi.org/10.1145/3313831.3376760
- **Summary:** 8 focus groups with 38 older adults in a Life Plan Community; 36 had owned an Echo for at least a year. Most had become non-users because they struggled to find valuable uses, held beliefs about ability and voice-assistant use, or found it hard to use in shared spaces. Participants still saw potential for future support.
- **Relevance:** Shows that open-ended assistants get abandoned, which supports a narrowly scoped assistant built around tasks the co-designer values.
- **Verified:** https://api.crossref.org/works/10.1145/3313831.3376760 · https://doi.org/10.1145/3313831.3376760 · author copy: https://hdl.handle.net/1805/27530
- *Note:* I could not confirm the ACM article number (ACM DL blocked; DBLP doesn't record it), so `articleno` is left out. Add it from the ACM DL page if you want it.

### `kim2021exploring`
- **Citation:** Sunyoung Kim and Abhishek Choudhury. 2021. Exploring older adults' perception and use of smart speaker-based voice assistants: A longitudinal study. *Computers in Human Behavior* (CHB) 124, Article 106914. https://doi.org/10.1016/j.chb.2021.106914
- **Summary:** A smart speaker was deployed in the homes of 12 older adults for 16 weeks, with interviews every other week and usage logs. The perceived benefits shifted over time: first simplicity and convenience, then not worrying about making mistakes and a sense of digital companionship. The challenges also shifted, from unfamiliarity at first to coping with speech-recognition errors.
- **Relevance:** Voice can reduce fear of mistakes over time, but recognition errors must be handled gracefully. This matters for how our on-device model confirms and recovers from misunderstandings.
- **Verified:** https://api.crossref.org/works/10.1016/j.chb.2021.106914 · https://doi.org/10.1016/j.chb.2021.106914
- *Note:* The publisher abstract was not reachable. This summary combines search-engine descriptions of the abstract with how later papers describe the study (e.g., arXiv:2403.02421). Check the PDF before quoting numbers.

### `kowalski2019older`
- **Citation:** Jarosław Kowalski, Anna Jaskulska, Kinga Skorupska, Katarzyna Abramczuk, Cezary Biele, Wiesław Kopeć, and Krzysztof Marasek. 2019. Older Adults and Voice Interaction: A Pilot Study with Google Home. In *Extended Abstracts of the 2019 CHI Conference on Human Factors in Computing Systems* (CHI EA '19), Glasgow, Scotland, UK. ACM, 6 pages. https://doi.org/10.1145/3290607.3312973
- **Summary:** An exploratory study in which older adults used Google Home in a smart-home setting during two workshops. It reports possible benefits of and barriers to voice assistants for older adults, and suggests improvements to the voice-interaction model.
- **Relevance:** Early evidence on older adults' first contact with voice interfaces. It is a short pilot, so cite it alongside the larger studies.
- **Verified:** https://api.crossref.org/works/10.1145/3290607.3312973 · https://doi.org/10.1145/3290607.3312973 · preprint: https://arxiv.org/abs/1903.07195 (confirms ISBN 978-1-4503-5971-9)
- *Note:* ACM article number not confirmable, so it is left out (same reason as above).

### `pradhan2018accessibility`
- **Citation:** Alisha Pradhan, Kanika Mehta, and Leah Findlater. 2018. "Accessibility Came by Accident": Use of Voice-Controlled Intelligent Personal Assistants by People with Disabilities. In *Proceedings of the 2018 CHI Conference on Human Factors in Computing Systems* (CHI '18), Montreal QC, Canada. ACM, Article 459, 13 pages. https://doi.org/10.1145/3173574.3174033
- **Summary:** A content analysis of 346 Amazon Echo reviews that mention users with disabilities, plus interviews with 16 voice-assistant users with visual impairments. People with many kinds of disability use the Echo, including unexpected uses such as speech therapy and support for caregivers. Discoverability of features and richer voice applications remain problems.
- **Relevance:** Supports voice as an accessibility channel for impairments common in later life. Our fixed, visible set of actions addresses the discoverability problem it identifies. Note that it is about disability, not ageing specifically.
- **Verified:** https://api.crossref.org/works/10.1145/3173574.3174033 · https://doi.org/10.1145/3173574.3174033 · Article 459 from DBLP `conf/chi/PradhanMF18`

---

## 3. Simplified or senior-oriented phone interfaces, launchers, accessibility modes, and stigma

### `petrovcic2018smart` (added to fill gap)
- **Citation:** Andraž Petrovčič, Ajda Rogelj, and Vesna Dolničar. 2018. Smart but not adapted enough: Heuristic evaluation of smartphone launchers with an adapted interface and assistive technologies for older adults. *Computers in Human Behavior* (CHB) 79, 123–136. https://doi.org/10.1016/j.chb.2017.10.021
- **Summary:** A heuristic evaluation of commercial smartphone "senior launchers" that offer simplified interfaces and assistive features. Launchers mostly included only basic features and a single assistive technology (an SOS service). Usability problems with content and perception limited users' ability to recover from errors and to access information visually, aurally and through touch. Simply reducing the number of features did not guarantee usability.
- **Relevance:** The closest prior work to our simplified screen. It shows that hiding apps is not enough: error recovery and feedback through several senses also matter.
- **Verified:** https://api.crossref.org/works/10.1016/j.chb.2017.10.021 · https://doi.org/10.1016/j.chb.2017.10.021
- *Note:* The abstract was read through search-engine excerpts and the Semantic Scholar summary because ScienceDirect was blocked.

### `diazbossini2014accessibility`
- **Citation:** José-Manuel Díaz-Bossini and Lourdes Moreno. 2014. Accessibility to Mobile Interfaces for Older People. *Procedia Computer Science* (Procedia Comput. Sci.) 27, 57–66. https://doi.org/10.1016/j.procs.2014.02.008
- **Summary:** Compiles an accessibility checklist for mobile interfaces for older people from the literature, standards and best practice. The checklist is then applied to three Android apps that replace the default interface with a more accessible one.
- **Relevance:** Gives concrete guidelines for auditing the part of the screen our shell leaves visible, plus an early look at launcher-type apps.
- **Verified:** https://api.crossref.org/works/10.1016/j.procs.2014.02.008 · https://doi.org/10.1016/j.procs.2014.02.008

### `urbano2022skeuomorphism` (Hugo Nicolau)
- **Citation:** Inês Cunha Vaz Pereira Urbano, João Pedro Vieira Guerreiro, and Hugo Miguel Aleixo Albuquerque Nicolau. 2022. From skeuomorphism to flat design: age-related differences in performance and aesthetic perceptions. *Behaviour & Information Technology* (BIT) 41, 3, 452–467. https://doi.org/10.1080/0144929X.2020.1814867
- **Summary:** Compared three visual styles (skeuomorphic, "skeuominimalist" and flat) with older (65–77) and younger (20–40) adults. For older adults, flat design was slower or less accurate in visual search, identifying clickable objects and navigating across pages. Skeuomorphic design narrowed the gap between age groups. Older adults preferred skeuomorphic interfaces for their perceived usability, beauty and trustworthiness.
- **Relevance:** Supports making controls on our simplified screen look clearly pressable. It also gives a rationale for a physical button, which is skeuomorphism taken to its extreme.
- **Verified:** https://api.crossref.org/works/10.1080/0144929X.2020.1814867 · https://doi.org/10.1080/0144929X.2020.1814867 · DBLP `journals/behaviourIT/UrbanoGN22`
- *Note:* Published online in 2020; the volume/issue is 2022, which is the year used. Author names are given in full as published.

### `shinohara2011shadow`
- **Citation:** Kristen Shinohara and Jacob O. Wobbrock. 2011. In the shadow of misperception: assistive technology use and social interactions. In *Proceedings of the SIGCHI Conference on Human Factors in Computing Systems* (CHI '11), Vancouver, BC, Canada. ACM, 705–714. https://doi.org/10.1145/1978942.1979044
- **Summary:** Interviews with 20 people examined how social and professional settings affect assistive-technology use. Devices sometimes marked their users as disabled, and functional access usually outweighed self-consciousness. Two misperceptions were common: that devices eliminate disability, and that users are helpless without them. The authors propose "design for social acceptability".
- **Relevance:** The shell and button will be seen in public, so they should not read as an "assistive device".
- **Verified:** https://api.crossref.org/works/10.1145/1978942.1979044 · https://doi.org/10.1145/1978942.1979044 · DBLP `conf/chi/ShinoharaW11`

### `pullin2009design`
- **Citation:** Graham Pullin. 2009. *Design Meets Disability*. MIT Press, Cambridge, MA. ISBN 978-0-262-16255-5.
- **Summary:** Argues that products for disabled people should draw on mainstream design culture. The model is eyeglasses, which went from medical necessity to fashion accessory. The book works through tensions such as "fashion meets discretion" and "simple meets universal", and argues that disability can also inspire mainstream design.
- **Relevance:** Supports treating the 3D-printed shell as a designed object the co-designer can be proud of, not a medical-looking aid. The "simple meets universal" argument also backs a reduced feature set.
- **Verified:** No DOI exists for the book. Open Library edition (MIT Press, Cambridge, MA, 2009, 341 pp., ISBN 9780262162555, LCCN 2008031030): https://openlibrary.org/books/OL16991860M · Internet Archive catalogue record (same ISBN/LCCN): https://archive.org/details/designmeetsdisab0000pull · a review registered with Crossref that cites the same edition: https://doi.org/10.1108/17549451211285807

### `neven2010obviously` (added to fill gap)
- **Citation:** Louis Neven. 2010. 'But obviously not for me': robots, laboratories and the defiant identity of elder test users. *Sociology of Health & Illness* (Sociol. Health Illn.) 32, 2, 335–347. https://doi.org/10.1111/j.1467-9566.2009.01218.x
- **Summary:** Studied older test users of a prototype health robot and the robot's designers. Test users drew on stereotypes of old people (lonely, in need of care) and presented themselves as healthy and independent, so the robot was "not for me". The designers' image of older users did not change, and the author argues that ignoring these views risks non-use.
- **Relevance:** A "phone for old people" may be rejected by the very people it targets, so co-design should surface how the co-designer wants to be seen.
- **Verified:** https://api.crossref.org/works/10.1111/j.1467-9566.2009.01218.x · https://doi.org/10.1111/j.1467-9566.2009.01218.x

### `vines2015age`
- **Citation:** John Vines, Gary Pritchard, Peter Wright, Patrick Olivier, and Katie Brittain. 2015. An Age-Old Problem: Examining the Discourses of Ageing in HCI and Strategies for Future Research. *ACM Transactions on Computer-Human Interaction* (TOCHI) 22, 1, Article 2, 27 pages. https://doi.org/10.1145/2696867
- **Summary:** A discourse analysis of 644 SIGCHI papers spanning 30 years. It shows that HCI usually frames ageing as a "problem" for technology to manage: health and care costs, isolation, and declining abilities. These framings mirror stereotypes from gerontology, and the paper proposes alternative strategies for future research.
- **Relevance:** Frames our stance: designing *with* a capable older adult around communication they value, rather than designing for decline. Fits topic 3 (stigma) or the introduction.
- **Verified:** https://api.crossref.org/works/10.1145/2696867 · https://doi.org/10.1145/2696867 · Article 2 from DBLP `journals/tochi/VinesPWOB15`

---

## 4. Participatory design and co-design with older adults; generative methods

### `lindsay2012engaging`
- **Citation:** Stephen Lindsay, Daniel Jackson, Guy Schofield, and Patrick Olivier. 2012. Engaging older people using participatory design. In *Proceedings of the SIGCHI Conference on Human Factors in Computing Systems* (CHI '12), Austin, TX, USA. ACM, 1199–1208. https://doi.org/10.1145/2207676.2208570
- **Summary:** Argues that designers are poorly equipped to design for older people and proposes participatory design as a remedy. It names four issues such approaches must address and describes an approach to engaging older people early in design. It reflects on applying the approach across several projects, including with partners less committed to participation.
- **Relevance:** Practical guidance for running engaging sessions with a single older co-designer.
- **Verified:** https://api.crossref.org/works/10.1145/2207676.2208570 · https://doi.org/10.1145/2207676.2208570 · DBLP `conf/chi/LindsayJSO12`

### `rogers2014never`
- **Citation:** Yvonne Rogers, Jeni Paay, Margot Brereton, Kate L. Vaisutis, Gary Marsden, and Frank Vetere. 2014. Never too old: engaging retired people inventing the future with MaKey MaKey. In *Proceedings of the SIGCHI Conference on Human Factors in Computing Systems* (CHI '14), Toronto, ON, Canada. ACM, 3913–3922. https://doi.org/10.1145/2556288.2557184
- **Summary:** In workshops, retirees from their early 60s to late 80s used the MaKey MaKey toolkit to invent future technologies. They mastered the toolkit, collaborated intensely, and talked at length about their own relationship with technology. The paper reframes ageing in terms of wisdom, creativity and invention rather than assistance.
- **Relevance:** Supports hands-on making in our sessions, such as having the co-designer handle and modify button and shell mock-ups.
- **Verified:** https://api.crossref.org/works/10.1145/2556288.2557184 · https://doi.org/10.1145/2556288.2557184 · DBLP `conf/chi/RogersPBVMV14`

### `sanders2008cocreation`
- **Citation:** Elizabeth B.-N. Sanders and Pieter Jan Stappers. 2008. Co-creation and the new landscapes of design. *CoDesign* 4, 1, 5–18. https://doi.org/10.1080/15710880701875068
- **Summary:** Traces the shift in design research from user-centred design to co-designing, in which the person "formerly known as the user" becomes a co-creator. It contrasts the US and European traditions and discusses what the shift means for the roles and education of designers and researchers.
- **Relevance:** Theoretical grounding for treating our older adult as a co-designer rather than a test subject.
- **Verified:** https://api.crossref.org/works/10.1080/15710880701875068 · https://doi.org/10.1080/15710880701875068

### `gaver1999cultural`
- **Citation:** Bill Gaver, Tony Dunne, and Elena Pacenti. 1999. Design: Cultural probes. *interactions* 6, 1, 21–29. https://doi.org/10.1145/291224.291235
- **Summary:** Introduces cultural probes: packs of maps, postcards, cameras, photo albums and media diaries given to older people in three European communities (Oslo, Amsterdam, and near Pisa) for an EU-funded project. The probes returned fragmentary, inspirational material rather than "scientific" answers.
- **Relevance:** A low-effort way to learn about the co-designer's daily communication between sessions, for example with a diary of calls and messages.
- **Verified:** https://api.crossref.org/works/10.1145/291224.291235 · https://doi.org/10.1145/291224.291235 · DBLP `journals/interactions/GaverDP99`
- *Note:* This magazine article has no abstract. The summary is based on descriptions of the article found through search.

### `andersen2019magic`
- **Citation:** Kristina Andersen and Ron Wakkary. 2019. The Magic Machine Workshops: Making Personal Design Knowledge. In *Proceedings of the 2019 CHI Conference on Human Factors in Computing Systems* (CHI '19), Glasgow, Scotland, UK. ACM, Article 112, 13 pages. https://doi.org/10.1145/3290605.3300342
- **Summary:** Proposes qualities for short, intense, workshop-like sessions in which participants make open-ended "magic machines" to imagine new things and expose the personal desires behind their ideas. The emphasis is on making work that is *about* technology rather than *of* technology.
- **Relevance:** A generative method for finding out what the co-designer wishes a phone could do, without anchoring on today's smartphones.
- **Verified:** https://api.crossref.org/works/10.1145/3290605.3300342 · https://doi.org/10.1145/3290605.3300342 · Article 112 from DBLP `conf/chi/AndersenW19`

---

## 5. Tangible or physical interfaces for older adults

### `brereton2015messaging` (added to fill gap)
- **Citation:** Margot Brereton, Alessandro Soro, Kate Vaisutis, and Paul Roe. 2015. The Messaging Kettle: Prototyping Connection over a Distance between Adult Children and Older Parents. In *Proceedings of the 33rd Annual ACM Conference on Human Factors in Computing Systems* (CHI '15), Seoul, Republic of Korea. ACM, 713–716. https://doi.org/10.1145/2702123.2702462
- **Summary:** Presents a connected kettle that supports keeping in touch with a remote older relative during the routine of boiling water. It was shown at morning-tea gatherings of people in their 50s to late 70s and placed in the homes of two people in their 80s on another continent. Augmenting a habitual object was well received, but the design needed simple, varied and asymmetric ways to communicate, and introducing it into daily routines still took effort.
- **Relevance:** A precedent for putting communication into a familiar physical object with minimal interaction, as our shell and single button do.
- **Verified:** https://api.crossref.org/works/10.1145/2702123.2702462 · https://doi.org/10.1145/2702123.2702462 · DBLP `conf/chi/BreretonSVR15`

### `bong2018tangible` (added to fill gap)
- **Citation:** Way Kiat Bong, Weiqin Chen, and Astrid Bergland. 2018. Tangible User Interface for Social Interactions for the Elderly: A Review of Literature. *Advances in Human-Computer Interaction* (Adv. Hum.-Comput. Interact.) 2018, Article 7249378, 15 pages. https://doi.org/10.1155/2018/7249378
- **Summary:** A systematic review of tangible user interfaces (TUIs) designed to support older people's social interaction. It found very little published work, despite TUIs having existed for 20 years. It recommends involving older people from design through evaluation and studying the effects on social interaction and health.
- **Relevance:** Places our physical button and shell within TUI research for older adults' communication, and supports the co-design approach.
- **Verified:** https://api.crossref.org/works/10.1155/2018/7249378 · https://doi.org/10.1155/2018/7249378 · DBLP `journals/ahci/BongCB18`

---

## 6. Older adults, scams and security (optional topic)

### `frik2019privacy` (added to fill gap)
- **Citation:** Alisa Frik, Leysan Nurgalieva, Julia Bernd, Joyce Lee, Florian Schaub, and Serge Egelman. 2019. Privacy and Security Threat Models and Mitigation Strategies of Older Adults. In *Fifteenth Symposium on Usable Privacy and Security* (SOUPS 2019), Santa Clara, CA. USENIX Association, 21–40.
- **Summary:** Semi-structured interviews with 46 adults aged 65+ about privacy and security in emerging technologies. Participants were aware of fraud, scams and identity theft. Limited technical knowledge and age-related declines increased their vulnerability, as did health, living situation and finances. Participants often managed risk by limiting or avoiding technology use.
- **Relevance:** A device that places calls and reads messages aloud must not expose the co-designer to scam calls or messages. Restricting actions to known contacts is therefore a protective design choice.
- **Verified:** USENIX publisher page (no DOI; USENIX does not assign them): https://www.usenix.org/conference/soups2019/presentation/frik · DBLP `conf/soups/FrikNBLSE19` (https://dblp.org/rec/conf/soups/FrikNBLSE19)

---

## Methodology (not part of Related Work)

### `braun2006using`
- **Citation:** Virginia Braun and Victoria Clarke. 2006. Using thematic analysis in psychology. *Qualitative Research in Psychology* (QRP) 3, 2, 77–101. https://doi.org/10.1191/1478088706qp063oa
- **Summary:** Defines thematic analysis as a flexible, accessible method for qualitative analysis and places it among other qualitative methods and epistemological positions. Gives step-by-step guidelines for doing it rigorously, along with common pitfalls.
- **Relevance:** The analysis method for session transcripts and notes.
- **Verified:** https://api.crossref.org/works/10.1191/1478088706qp063oa · https://doi.org/10.1191/1478088706qp063oa

### `designcouncil2004double`
- **Citation:** Design Council. 2004. The Double Diamond: A universally accepted depiction of the design process. https://www.designcouncil.org.uk/resources/the-double-diamond/ (accessed October 5, 2026).
- **Summary:** The Design Council's model of the design process: four phases (Discover, Define, Develop, Deliver) arranged as two diamonds of divergent and convergent thinking. The current page publishes it under a CC BY 4.0 licence.
- **Relevance:** Structures the process section (discover with the co-designer, define the problem, develop prototypes, deliver).
- **Verified:** https://www.designcouncil.org.uk/resources/the-double-diamond/ (HTTP 200; the older `/our-resources/the-double-diamond/` URL redirects here) · history page: https://www.designcouncil.org.uk/resources/the-double-diamond/history-of-the-double-diamond/
- *Note:* The page itself is undated. The year 2004 comes from the Design Council's history page ("In 2004, Design Council started to share the Double Diamond at conferences and in presentations"). If your course prefers the access year for web pages, change `year`.

---

## If you need to cut down to 20 Related Work references

25 are verified. The five lowest-priority, in order:
1. `pradhan2018accessibility` (about disability rather than ageing)
2. `kowalski2019older` (short pilot study)
3. `czaja2006factors` (general technology adoption; `mitzner2010older` covers attitudes)
4. `diazbossini2014accessibility` (overlaps with `petrovcic2018smart`)
5. Either `pullin2009design` or `shinohara2011shadow`, depending on whether your stigma argument is about aesthetics (keep Pullin) or social perception (keep Shinohara)

---

## Dropped candidates and changes

**Dropped:** none. All 21 candidates you supplied (19 Related Work + Braun & Clarke + Double Diamond) exist and were verified.

**Corrections and clarifications to the metadata you supplied:**
- Leung et al. 2012: TACCESS 4(3), **Article 11**. Crossref spells "Mcgrenere"; the correct spelling is "McGrenere" (as on the paper and in DBLP).
- Vines et al. 2015: TOCHI 22(1), **Article 2**.
- Pradhan, Lazar, Findlater 2020: TOCHI 27(4), **Article 31**.
- Pradhan, Mehta, Findlater 2018: CHI '18, **Article 459**.
- Andersen & Wakkary 2019: CHI '19, **Article 112**.
- Trajkova & Martin-Hammond 2020 and Kowalski et al. 2019: ACM article numbers could not be confirmed, so they are omitted (page counts 13 and 6 are included).
- Kowalski et al. 2019: "et al." is 7 authors: Jarosław Kowalski, Anna Jaskulska, Kinga Skorupska, Katarzyna Abramczuk, Cezary Biele, Wiesław Kopeć, Krzysztof Marasek. Venue: CHI EA '19.
- Mitzner et al. 2010 has 10 authors and Czaja et al. 2006 has 7 (full lists in the `.bib`).
- Kim & Choudhury 2021: CHB 124 has no page range; the paper is **Article 106914**.
- Rogers et al. 2014: the fourth author appears as "Kate L. Vaisutis" in this paper.
- Gaver et al. 1999: published byline is "Bill Gaver, Tony Dunne, Elena Pacenti" (DBLP indexes them as William W. Gaver and Anthony Dunne); cited as published.
- Pullin 2009: the book has no DOI; it is cited with its ISBN.
- Double Diamond: the official URL has moved to `/resources/the-double-diamond/`.

**Screened but not included (Hugo Nicolau).** I went through all 139 of his works listed in OpenAlex (author ID A5025652256, ORCID 0000-0002-8176-7638). Most are about blind users, children or rehabilitation. Besides the two included papers:
- Ana Cristina Pires, Camila Hergatacorzian, Ana O. Henriques, Reem Talhouk, Anna R. L. Carter, Clara Rosa Cardoso, Hugo Nicolau. 2025. Bridging the Digital Divide: Relational Autonomy, Care and Trust in Community Service Desks. C&T '25, 250–264. https://doi.org/10.1145/3742800.3742840. Studies Lisbon's *Balcão do Bairro* service desks. "Elderly individuals with limited technology exposure" are named as one of the main user groups, but the paper is about digital public services, and only two residents were interviewed (one aged 25, one of unknown age). It could support one sentence on Portuguese older adults relying on trusted intermediaries; it is not core.
- Hugo Nicolau, Tiago Guerreiro, Joaquim Jorge, Daniel Gonçalves. 2014. Mobile touchscreen user interfaces: bridging the gap between motor-impaired and able-bodied users. UAIS 13(3), 303–313. https://doi.org/10.1007/s10209-013-0320-5. About motor impairment, not ageing.
- Kyle Montague, Hugo Nicolau, Vicki L. Hanson. 2014. Motor-impaired touchscreen interactions in the wild. ASSETS '14, 123–130. https://doi.org/10.1145/2661334.2661362. About tremor and motor impairment, not older adults specifically.
- His 2026 work on intermediaries in digital public services (ECSCW 2026; NordiCHI 2026 workshop) is not about older adults specifically.

**Verified alternatives, not in the `.bib` (DOIs checked on Crossref and doi.org):**
- Andraž Petrovčič, Sakari Taipale, Ajda Rogelj, Vesna Dolničar. 2018. Design of Mobile Phones for Older Adults: An Empirical Analysis of Design Guidelines and Checklists for Feature Phones and Smartphones. *International Journal of Human–Computer Interaction* 34, 3, 251–264. https://doi.org/10.1080/10447318.2017.1345142. A good swap for `diazbossini2014accessibility`.
- Jeannette Durick, Toni Robertson, Margot Brereton, Frank Vetere, Bjorn Nansen. 2013. Dispelling ageing myths in technology design. OzCHI '13, 467–476. https://doi.org/10.1145/2541016.2541040 (topic 3).
- Yvonne Rogers and Gary Marsden. 2013. Does he take sugar? Moving beyond the rhetoric of compassion. *interactions* 20, 4, 48–57. https://doi.org/10.1145/2486227.2486238 (topic 3).
- Way Kiat Bong, Astrid Bergland, Weiqin Chen. 2019. Technology Acceptance and Quality of Life among Older People Using a TUI Application. *IJERPH* 16, 23, 4706. https://doi.org/10.3390/ijerph16234706 (topic 5).
- Allison Woodruff and Paul M. Aoki. 2003. How push-to-talk makes talk less pushy. GROUP '03, 170–179. https://doi.org/10.1145/958160.958187. Not about older adults, but the standard HCI reference for push-to-talk if you want to justify the button.
- James Nicholson, Lynne Coventry, Pamela Briggs. 2019. "If It's Important It Will Be A Headline": Cybersecurity Information Seeking in Older Adults. CHI '19. https://doi.org/10.1145/3290605.3300579 (alternative for topic 6).
