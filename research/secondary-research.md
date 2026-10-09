# Secondary research: older adults, phones and communication in Portugal

Compiled on 2026-10-05 for the co-design project: a 3D-printed shell over an Android phone that shows part of the screen with a simplified interface, plus one large push-to-talk button for a few spoken commands (call someone, read messages aloud, reminders).

- Every URL below was accessed on 2026-10-05. Keys in backticks (for example `eurostat2025internet`) refer to entries in `secondary.bib`. None of these keys clash with `references.bib`.
- **How the figures were obtained.** Eurostat values were read from the Eurostat dissemination API on 2026-10-05. They match the data-browser tables linked below. INE, GNR, APAV, Cruz Vermelha and company figures were read on the cited page or PDF. Banco de Portugal pages blocked automated access, so those facts were checked in news reports that quote the Banco de Portugal, and those reports are cited instead.
- **Bib keys.** For Eurostat datasets, the year in the key is the reference year of the data. The `year` field is the year of the dataset version that was read (2026).
- Percentages are rounded to one decimal place, except where a source gives two decimals.

---

## Part 1. Portugal and EU statistics

### 1.1 Population ageing

**F1. Share of the population aged 65+, Portugal vs EU.** Portugal 24.3% vs EU-27 22.0% (the EU value is estimated and provisional). Portugal has the second-highest share in the EU-27, after Italy (24.7%).
Year: 1 January 2025. Source: Eurostat, *Population structure indicators at national level*, dataset `demo_pjanind`, indicator `PC_Y65_MAX` (data updated 2026-09-25). URL: https://ec.europa.eu/eurostat/databrowser/view/demo_pjanind/default/table?lang=en (accessed 2026-10-05). `eurostat2025population`

**F2. People aged 80+ and the old-age dependency ratio.** People aged 80+ are 7.1% of the population in Portugal vs 6.2% in the EU-27. The old-age dependency ratio (people 65+ per 100 people aged 15–64) is 38.6 in Portugal vs 34.5 in the EU-27.
Year: 1 January 2025. Source: Eurostat, `demo_pjanind`, indicators `PC_Y80_MAX` and `OLDDEP1`. URL: https://ec.europa.eu/eurostat/databrowser/view/demo_pjanind/default/table?lang=en (accessed 2026-10-05). `eurostat2025population`

**F3. INE's latest national estimates and the ageing index.**
- On 31 December 2025, Portugal's resident population was 11,424,031.
- The ageing index was 188.8 people aged 65+ per 100 aged 0–14. It was 182.4 in 2024 and 178.3 in 2021.
- INE says the share aged 65+ "manteve-se relativamente estável, em cerca de 23%" (remained relatively stable at about 23%) from 2021 to 2025. The 0–14 group was 12.4% and the 15–64 group 64.3% in 2025, which implies about 23.3% aged 65+ (our subtraction).
- By region, the ageing index is highest in Centro (232.9) and Alentejo (222.0), and lowest in the Azores (138.3).

Year: 2025 (published 22 June 2026). Source: INE, *Estimativas de População Residente – 2025* (press release). URL: https://www.ine.pt/ngt_server/attachfileu.jsp?look_parentBoui=799432499&att_display=n&att_download=y (accessed 2026-10-05). `ine2026estimativas`
*Caveat:* this is INE's first series based entirely on administrative data, and it revised the 2021–2024 figures. Eurostat (F1) and the INE projections (F5) still use the earlier series, which gives 24.3% aged 65+ and an ageing index of 192.4 for 2024. Use Eurostat for EU comparisons and INE for national detail, and don't mix the two series in one comparison.

**F4. Census 2021.** People aged 65+ were 23.4% of residents and people aged 0–14 were 12.9%. The ageing index was 182, up from 128 in 2011.
Year: 2021 (census date 19 April 2021; published 23 November 2022). Source: INE, *Censos 2021 – Divulgação dos Resultados Definitivos*. URL: https://www.ine.pt/ngt_server/attachfileu.jsp?look_parentBoui=585774296&att_display=n&att_download=y (accessed 2026-10-05). `ine2022censos`

**F5. INE projections, central scenario.**
- People aged 65+ go from 2.6 million (2024) to 3.1 million (2100). The number peaks in the early 2050s.
- Their share of the population goes from 24.3% (2024) to 34.5% (2050) and 37.3% (2100).
- The ageing index goes from 192.4 (2024) to 316.3 (2100), rising until about 2060 and then levelling off.
- The old-age dependency ratio goes from 38.6 to 73.4.

Year: projections 2025–2100, base year 2024 (published 30 September 2025). Source: INE, *Projeções da População Residente 2025–2100* (press release, Quadro 1). URL: https://www.ine.pt/ngt_server/attachfileu.jsp?look_parentBoui=753199150&att_display=n&att_download=y (accessed 2026-10-05). `ine2025projecoes`
*Note:* in the INE text, the sentence giving 34.5% says "em 2025", but it sits under the heading "Em 2050…", so 34.5% is the 2050 value.

**F6. Eurostat projections (EUROPOP2023, baseline).** Share of the population aged 65+:
- Portugal: 26.8% (2030), 33.9% (2050), 32.2% (2100).
- EU-27: 23.7% (2030), 29.0% (2050), 32.5% (2100).

Year: 2023-based projections (data updated 2026-05-13). Source: Eurostat, dataset `proj_23ndbi`, indicator `PC_Y65_MAX`, baseline projection (`BSL`). URL: https://ec.europa.eu/eurostat/databrowser/view/proj_23ndbi/default/table?lang=en (accessed 2026-10-05). `eurostat2023projections`

### 1.2 Internet use, ages 65–74

**F7. Internet use in the last 3 months.** Portugal 60.7% vs EU-27 79.0%. Portugal is the fifth-lowest in the EU-27; only Greece (51.9%), Croatia (54.3%), Poland (60.6%) and Bulgaria (60.6%) are lower. Portugal's trend: 53.2% (2023), 61.1% (2024), 60.7% (2025). For comparison, among all people aged 16–74 the figures are 89.5% (Portugal) and 93.8% (EU).
Year: 2025. Source: Eurostat, *Individuals – internet use*, dataset `isoc_ci_ifp_iu`, indicator `I_IU3`, age group `Y65_74`. URL: https://ec.europa.eu/eurostat/databrowser/view/isoc_ci_ifp_iu/default/table?lang=en (accessed 2026-10-05). `eurostat2025internet`. INE gives the same 89.5% for ages 16–74 in its national survey, *Inquérito à Utilização de Tecnologias da Informação e da Comunicação pelas Famílias 2025*: https://www.ine.pt/ngt_server/attachfileu.jsp?look_parentBoui=762433102&att_display=n&att_download=y (accessed 2026-10-05). `ine2025iutic`

**F8. People who have never used the internet, ages 65–74.** Portugal 36.1% vs EU-27 16.4% in 2025. In 2024 the figures were 34.7% vs 18.2%, as also reported by ANACOM.
Year: 2025 (and 2024). Sources:
- Eurostat, `isoc_ci_ifp_iu`, indicator `I_IUX`. URL as in F7 (accessed 2026-10-05). `eurostat2025internet`
- ANACOM, *O Consumidor de Comunicações Eletrónicas 2024* (March 2025): https://www.anacom.pt/streaming/OConsumidor_deComunicacoesEletronicas2024.pdf?contentId=1805701&field=ATTACHED_FILE (accessed 2026-10-05). `anacom2025consumidor`

**F9. Messaging and calls are the main online activities, ages 65–74.**
- Instant messaging (for example WhatsApp): 48.4% of all Portuguese aged 65–74 vs 58.9% in the EU-27. Among those who used the internet in the last 3 months, the figures are 79.8% (Portugal) vs 74.5% (EU-27).
- Telephone or video calls over the internet: 42.8% vs 52.3% of all people that age. Among internet users, 70.5% vs 66.2%.
- In other words, Portugal trails the EU because fewer older people are online. Those who are online message and call more than the EU average.
- INE adds that 93.5% of internet users aged 16–74 exchanged instant messages in 2025.

Year: 2025. Source: Eurostat, *Individuals – internet activities*, dataset `isoc_ci_ac_i`, indicators `I_IUCHAT1` and `I_IUPH1`, units `PC_IND` (all people) and `PC_IND_IU3` (internet users). URL: https://ec.europa.eu/eurostat/databrowser/view/isoc_ci_ac_i/default/table?lang=en (accessed 2026-10-05). `eurostat2025activities`. INE figure from the INE 2025 survey report (URL as in F7, accessed 2026-10-05). `ine2025iutic`

**F10. Use of AI tools.** 5.8% of people aged 65–74 used artificial-intelligence tools in the last 3 months, vs 38.7% of all people aged 16–74.
Year: 2025 (published 21 November 2025). Source: INE, *Inquérito à Utilização de TIC pelas Famílias 2025*, Figura 10. URL as in F7 (accessed 2026-10-05). `ine2025iutic`

### 1.3 Digital skills, ages 65–74

**F11. At least basic overall digital skills.**
- 2025: Portugal 20.65% vs EU-27 33.0%. INE reports the Portuguese value as 20.6%.
- 2023: 18.5% vs 28.2%.
- 2021: 16.6% vs 25.5%.
- In 2025 Portugal is ninth-lowest of the EU-27.
- Across all ages 16–74 the two are close: Portugal 59.2% vs EU 60.4%. The gap is concentrated in older people.

Years: 2021, 2023 and 2025 (the indicator is collected every two years). Source: Eurostat, *Individuals' level of digital skills (from 2021 onwards)*, dataset `isoc_sk_dskl_i21`, indicator `I_DSK2_BAB`. URL: https://ec.europa.eu/eurostat/databrowser/view/isoc_sk_dskl_i21/default/table?lang=en (accessed 2026-10-05). `eurostat2025digitalskills`. INE figure from the INE 2025 survey report, Figura 4 (URL as in F7). `ine2025iutic`

### 1.4 Mobile phone and smartphone use

**F12. Used the internet on a mobile phone or smartphone (last 3 months), ages 65–74.** Portugal 57.7% vs EU-27 70.3% in 2025. In 2023 the figures were 49.4% vs 60.5%. Portugal is eighth-lowest in the EU-27. Among all people aged 16–74 the gap is small: 88.0% vs 89.9%.
Year: 2025 (and 2023). Source: Eurostat, *Individuals – devices used to access the internet*, dataset `isoc_ci_dev_i`, indicator `I_IUG_MP`. URL: https://ec.europa.eu/eurostat/databrowser/view/isoc_ci_dev_i/default/table?lang=en (accessed 2026-10-05). `eurostat2025devices`

**F13. Internet use among all people aged 65+ (Marktest).** About 1.3 million people aged 65+ used the internet in 2024, or 56% of that age group. That is double the 28% of 2019. This survey covers residents of mainland Portugal aged 15+, and it is the only internet-use source found that includes people aged 75+.
Year: 2024 (news release, 1 October 2024). Source: Marktest, *Bareme Internet 2024*, "Utilização de internet por pessoas acima dos 64 anos duplicou desde 2019". URL: https://www.marktest.com/wap/a/n/id~2b2b.aspx (accessed 2026-10-05). `marktest2024bareme`

**F14. Smartphone as the device used to go online (Marktest, older data).** In 2022, 41% of mainland residents aged 65+ were online. Among internet users aged 65+, 35% used a smartphone to go online, vs 98% of users aged 25–44.
Year: 2022 (news release, 23 May 2023). Source: Marktest, *Bareme Internet 2022*, "Acesso de idosos portugueses à Internet duplica em cinco anos". URL: https://www.marktest.com/wap/a/n/id~29bc.aspx (accessed 2026-10-05). `marktest2023bareme`

**F15. Mobile phone service.** 97.4% of people aged 16–74 used mobile telephone service (an INE 2023 figure quoted by ANACOM). ANACOM publishes no breakdown by age for this figure.
Year: 2023 (in a report published March 2025). Source: ANACOM, *O Consumidor de Comunicações Eletrónicas 2024*, footnotes 3 and 5. URL as in F8 (accessed 2026-10-05). `anacom2025consumidor`

### 1.5 Living alone and isolation

**F16. People aged 65+ living alone (Census 2021).**
- 517,146 one-person households were made up of someone aged 65+, up from 406,942 in 2011 (+27%).
- That is 50.3% of all one-person households and 12.5% of all private households (10.1% in 2011).
- These households are concentrated in parishes in the interior north and centre of the mainland and in the north of Madeira.

Year: 2021. Source: INE (C. Ferreira, G. Nunes, M. Aurindo, R. Santos), *Estruturas familiares em Portugal: dinâmicas territoriais recentes* (INE presentation, PDF dated 18 September 2024). URL: https://www.ine.pt/ngt_server/attachfileu.jsp?look_parentBoui=686728612&att_display=n&att_download=y (accessed 2026-10-05). `ine2024estruturas`

**F17. The oldest old.** 37.8% of people aged 80+ lived in households without a family nucleus. The press headline was "Quase 40% dos idosos com mais de 80 anos viviam sozinhos em 2021" (almost 40% of over-80s lived alone). The category also includes, for example, siblings living together, so it is not strictly "living alone".
Year: 2021. Source: an INE study ("Famílias nas fases mais avançadas da vida"), as reported by Observador on 18 September 2024. URL: https://observador.pt/2024/09/18/quase-40-dos-idosos-com-mais-de-80-anos-viviam-sozinhos-em-2021/ (accessed 2026-10-05). `observador2024idosos`

**F18. Share of people aged 65+ living alone, Portugal vs EU (EU-SILC survey).** Portugal 25.7% (women 33.5%, men 15.6%) vs EU-27 32.2% (women 40.5%, men 21.4%).
Year: 2025. Source: Eurostat, *Distribution of population aged 65 and over by type of household*, dataset `ilc_lvps30`, category `A1_GE65` (data updated 2026-09-17). URL: https://ec.europa.eu/eurostat/databrowser/view/ilc_lvps30/default/table?lang=en (accessed 2026-10-05). `eurostat2025livingalone`

**F19. Pordata.** 552,000 people aged 65+ lived alone in 2022, about one in four.
Year: 2022 (press release, July 2023). Source: PORDATA / Fundação Francisco Manuel dos Santos, *Pordata analisa situação da população com 65 e mais anos em Portugal*. URL: https://ffms.pt/sites/default/files/2023-07/2023%2007%2003%20PR_Dia%20Mundial%20da%20Popula%C3%A7%C3%A3o%20VF.pdf (accessed 2026-10-05). `pordata2023seniores`

**F20. GNR "Censos Sénior" operation.**
- In 2025 the GNR flagged 43,074 older people "que vivem sozinhos e/ou isolados, ou em situação de vulnerabilidade" (who live alone and/or isolated, or are vulnerable) because of their physical, psychological or other condition. The figure for 2024 was 42,873.
- The districts with the most were Guarda (5,852), Vila Real (5,167) and Bragança (4,191). Lisbon district had 1,054 and Porto 854; the PSP polices the main urban centres.
- The 2025 operation ran from 1 October to 16 November 2025 and reached 35,143 older people through 328 group sessions and 2,338 door-to-door actions.
- The 2026 operation (1 October–15 November 2026) lists among its aims "capacitar os adultos seniores para a utilização segura da internet" (helping older adults use the internet safely).

Years: 2024, 2025 and 2026. Source: GNR press releases:
- 18 November 2025: https://www.gnr.pt/comunicado.aspx?linha=9391 `gnr2025balanco`
- 1 October 2025: https://www.gnr.pt/comunicado.aspx?linha=9371 `gnr2025censos`
- 1 October 2026: https://www.gnr.pt/comunicado.aspx?linha=9548 `gnr2026censos`

All accessed 2026-10-05.

**F21. Risk of poverty (relevant to affordability).** The at-risk-of-poverty rate for people aged 65+ (below 60% of median equivalised income) is 17.8% in Portugal vs 16.3% in the EU-27.
Year: 2025. Source: Eurostat, dataset `ilc_li02` (EU-SILC), age group `Y_GE65` (data updated 2026-10-05). URL: https://ec.europa.eu/eurostat/databrowser/view/ilc_li02/default/table?lang=en (accessed 2026-10-05). `eurostat2025poverty`

### 1.6 Phone and online fraud targeting older people

**F22. GNR, 2026.** In the GNR's area, from January to August 2026, there were 2,720 fraud crimes ("burlas") against older people, 4,545 thefts and 417 robberies. One person was arrested for fraud. The release names fraud and the "conto do vigário" (confidence trick) among the main threats.
Year: 2026. Source: GNR, *Operação "Censos Sénior 2026"*, 1 October 2026. URL: https://www.gnr.pt/comunicado.aspx?linha=9548 (accessed 2026-10-05). `gnr2026censos`

**F23. GNR, 2025.** From 1 January to 15 September 2025 there were 1,942 fraud occurrences with older victims, 4,964 thefts and 327 robberies, and three arrests for fraud. The period differs from F22, so the two are not directly comparable.
Year: 2025. Source: GNR, *Operação "Censos Sénior 2025"*, 1 October 2025. URL: https://www.gnr.pt/comunicado.aspx?linha=9371 (accessed 2026-10-05). `gnr2025censos`

**F24. PSP (urban areas).** The PSP recorded 54,275 crimes against older people from 2023 to 2025: 18,432 in 2023, 17,613 in 2024 and 18,230 in 2025. The average victim was 73. Men were mostly victims of property crimes and women of crimes against the person. In the PSP's 2026 summer operation ("A Solidariedade Não Tem Idade", 27 July–25 September), officers made 2,240 contacts and flagged 461 older people, 294 of them at risk.
Year: 2023–2025 and 2026. Source: PSP figures reported by Lusa in RTP Notícias, 1 October 2026. URL: https://www.rtp.pt/noticias/pais/gnr-inicia-operacao-censos-senior-2026-psp-sinalizou-quase-300-idosos-em-risco-nos-centros-urbanos_n1768827 (accessed 2026-10-05). `rtp2026psp`. Observador gives the same totals: https://observador.pt/2026/10/01/psp-regista-mais-de-54-000-crimes-contra-idosos-nos-ultimos-tres-anos/ `observador2026psp`. The press breakdown by type of crime was internally inconsistent, so it is not used here.

**F25. APAV (victim support).** APAV supported 8,540 older people from 2021 to 2025, an increase of 26.5% over the period, covering 15,804 crimes and other forms of violence. Fraud was 2% of cases; domestic violence was 78.9%. These figures only cover people who asked for support, so they do not measure how common fraud is.
Year: 2021–2025 (published 15 June 2026). Source: APAV, *Estatísticas APAV – Pessoas Idosas Vítimas de Crime e de Violência 2021–2025*. URL: https://apav.pt/estatisticas-apav-pessoas-idosas-vitimas-de-crime-e-violencia-2021-2025/ (accessed 2026-10-05). `apav2026idosos`

**F26. The "Olá pai, olá mãe" WhatsApp scam.**
- The PSP said older people "continuam a ser as principais vítimas" (remain the main victims), especially in dense urban areas. Scammers pose as a son or daughter by text message and don't answer voice calls. The PSP advises calling the child's known number before transferring any money. 31 people had been arrested in 2023 at the time of the report.
- The Banco de Portugal says its payee-confirmation service, launched in 2024, "terá permitido reduzir, de forma substancial" these frauds (it has probably reduced them substantially).

Years: 2023 and 2024. Sources:
- TSF, 22 November 2023: https://www.tsf.pt/sociedade/artigo/ola-pai-ola-mae-detidas-31-pessoas-este-ano-os-conselhos-da-psp-para-evitar-burla/17380469 (accessed 2026-10-05). `tsf2023olapai`
- Banco de Portugal, *Relatório dos Sistemas de Pagamentos 2024* (May 2025), "Nota de abertura": https://www.bportugal.pt/sites/default/files/documents/2025-05/rsp2024.pdf (accessed 2026-10-05). `bdp2025rsp`

**F27. MB WAY scams.** The Banco de Portugal warned (October 2025) about two common tricks on second-hand marketplaces:
- A fake buyer persuades a seller who doesn't have MB WAY to sign up at an ATM using the scammer's phone number. This gives the scammer access to the seller's account.
- The scammer sends a "Pedir dinheiro" (request money) instead of "Enviar dinheiro" (send money), and a distracted victim pays.

Year: 2025. Source: Banco de Portugal alert as reported by SAPO, 29 October 2025. URL: https://sapo.pt/artigo/burlas-com-mb-way-continuam-a-crescer-nas-plataformas-de-compra-e-venda-online-69033e09e4360362687b8753 (accessed 2026-10-05). `sapo2025mbway`. The Banco de Portugal's own page blocked automated access.

**F28. Scale of payment fraud (not broken down by age).**
- In the first half of 2025, losses from card and credit-transfer fraud were €5.3 million, down 40.4% on a year earlier.
- There were 66 fraudulent card transactions and 10 fraudulent credit transfers per million. The Banco de Portugal publishes only half-year data.
- In the first half of 2024, losses were €8.9 million.

Sources:
- Banco de Portugal, *Relatório dos Sistemas de Pagamentos 2025*, as reported by RTP on 21 May 2026: https://www.rtp.pt/noticias/economia/perdas-em-fraudes-com-pagamentos-recuam-para-53-me-no-1o-semestre-de-2025-diz-bdp_n1742645 (accessed 2026-10-05). `rtp2026bdp`
- First-half 2024 figure: *Relatório dos Sistemas de Pagamentos 2024* (URL as in F26). `bdp2025rsp`

---

## Part 2. Existing products and approaches

Prices were checked on 2026-10-05. US products are priced in USD, and several were on sale that day.

| # | Product (year) | What it is | How it simplifies the phone | Input | Price (checked 2026-10-05) | Main limitations for an older adult in Portugal |
|---|---|---|---|---|---|---|
| 1 | **Doro Aurora A10/A20/A30** (May 2025); **Doro Leva** feature phones | Android smartphones for seniors; the A20 is a flip with a keypad. Leva phones are bar or flip feature phones. | "Doro Easy Interface" with fewer, clearer choices; physical call and home keys; **Secure Button** that alerts chosen contacts and shares location; clearer audio and hearing-aid compatibility; remote help from family through TeamViewer; flags suspicious callers | Touch plus physical keys; Secure Button on the back; keypad (A20, Leva) | Aurora £220–£290 (UK store) and €260–€335 at launch. Portuguese retailer gomibo.pt: Aurora A30 €276–€305, Leva €78–€171 | The Secure Button only works if contacts (and the assistance service) are set up beforehand; underneath it is still a full Android phone with apps, updates and notifications. |
| 2 | **Lively Jitterbug Smart5 / Flip3** (2026; Lively was formerly GreatCall) | US phones for seniors, sold with Lively's own mobile service | Simple list-based menu, big text, voice typing, Google Assistant, an **Urgent Response** button that reaches 24/7 agents, "Ask Lively" operators | Touch and voice (Smart5); keypad (Flip) | Smart5 $119.99 (on sale $89.99); Flip3 $79.99 (on sale $59.99); plans from $19.99/month (Smart) or $14.99/month (Flip2) | US only; the phone "cannot be used with other carriers"; Urgent Response needs a higher plan; monthly subscription. |
| 3 | **Apple Assistive Access** (announced May 2023; iOS 17, September 2023; extended 2025) | A built-in alternative iPhone interface, designed for people with cognitive disabilities | Calls (Phone and FaceTime merged), Messages (emoji-only keyboard, video messages), Camera, Photos, Music; layout as a grid or rows; set up by a "trusted supporter"; leaving it needs a triple-click and passcode; Siri can be allowed | Touch; Siri optional | Free (needs an iPhone) | A carer has to set it up. Other apps, such as WhatsApp, appear in their normal design with a big Back button unless the developer adopts Apple's 2025 API. Requires an iPhone. |
| 4 | **Samsung Easy mode** (One UI); **Google Pixel Simple View** (December 2024) | Built-in display modes on Android | Larger icons, text and spacing. Samsung adds a longer touch-and-hold delay, a high-contrast keyboard and contacts on the home screen. Pixel adds larger text, higher touch sensitivity and 3-button navigation. | Touch | Free (built in) | Changes presentation only: the same apps, notifications and settings remain. Device-specific: Samsung Galaxy S24–S26, Z and A series; Pixel 6 and later. |
| 5 | **BIG Launcher** (since 2011, Czech developer) | A replacement Android home screen, plus BIG Phone and BIG SMS apps | Six layouts of very large buttons, down to a single button; hides other apps; SOS button sends GPS location by SMS; a carer can set it up remotely; settings locked with a PIN; available in 70 languages, including Portuguese | Touch | One-time in-app purchase (price varies by country; not verified) | Other apps such as WhatsApp keep their normal interface. The SOS is an SMS to contacts, not a monitored service. Needs a carer to set it up. |
| 6 | **ElliQ** (Intuition Robotics; sold since March 2022; current model ElliQ 3) | A tabletop "companion robot" with a tablet | Starts conversations on its own, gives reminders, makes video calls, plays games; family app; caregiver alerts | Voice and touchscreen | $249 one-time fee plus $39–$59/month | US only, with no language other than English advertised. It stays in one room and is not a phone. Ongoing subscription. |
| 7 | **Amazon Echo (Alexa)**; **Google Nest** | Smart speakers and displays | Hands-free voice for calls, reminders and music. Amazon's caregiver service Alexa Together (2021) is no longer available and was replaced by Alexa Emergency Assist in the US. | Voice (plus touch on displays) | Echo Dot International €59.99 at its Portuguese launch (2021). Alexa+ costs €22.99/month for non-Prime users in Spain; it had no Portuguese launch date as of April 2026. | No European Portuguese. In Portugal Alexa must be set to English (US), and Alexa skills support only Brazilian Portuguese. Google Nest speakers support Portuguese only for Brazil, and Nest displays support no Portuguese at all. They stay in one room. |
| 8 | **Clicks keyboard case** (iPhone 2024; Android February 2025; iPhone 17 models 2025) | A phone case with a physical QWERTY keyboard that **plugs into the phone's USB-C or Lightning port** | Physical keys and shortcut keys; frees the screen from the on-screen keyboard | Physical keyboard | US$159 list price (US$139 on sale); ships to Portugal | **Charging passes through:** the case has its own port and no battery. Wireless charging works on some models, but on Razr phones it stops while something is plugged into USB-C. Fits only specific models: iPhone 14 Pro–17, Pixel 9/9 Pro, Razr 2024/2025. Wired headphones are not supported. The small keys are made for fast typing (our assessment). |
| 9 | **Light Phone III** (March 2025); **Light Flip** (announced July 2026) | Minimalist phones built around a few "tools" instead of apps | A black-and-white screen with a short list of tools: calls, messages, alarm, timer, calendar, voice notes, directions, music, podcasts, camera | Touch and buttons (III); keypad and directional pad (Flip) | Light Phone III: $799 at launch ($599 pre-order), now $899 on Light's shop. Light Flip: $349 on Light's shop, shipping April 2027. | No third-party apps, so no WhatsApp. Expensive. Designed for "digital minimalism", not for older users. |
| 10 | **Cruz Vermelha Portuguesa Teleassistência**; municipal schemes (for example Porto's "Estamos Juntos") | A 24/7 alarm and telecare service | One button connects to the Cruz Vermelha (CVP) Attention Centre, which can send medical help or alert family and neighbours. Regular follow-up calls (every two weeks in base plans, weekly in "+" plans). The mobile plan uses a portable device with GPS. | One emergency button | CVP: €24–€34/month (€19–€29 for CVP members), price table in force from 1 June 2026; equipment is rented. Porto: free for eligible residents aged 65+. | Only for emergencies and companionship: no messaging or contact list. The user has to carry the device and press the button. Monthly fee. |

### Notes and sources by product

**1. Doro.**
- The UK store lists three Aurora smartphones: A10 (£220, "full Android", WhatsApp supported), A20 (£250, flip with touchscreen and backlit keypad) and A30 (£290, 6.1-inch screen). It also lists Leva feature phones from £75 to £149.99; the Leva L20 and L30 are flip phones with a charging cradle.
- The A30 page describes the Secure Button as sending "an alert and your location with one press". It adds that the location-sharing alert works "with the supported assistance service set up" and that "you'll need to arrange this with your trusted contacts before using it".
- At launch on 20 May 2025, Doro described "three physical buttons for easy calling and a dedicated home key" and quoted its own survey: 51% of seniors worry about pressing the wrong thing, and 49% struggle with navigating apps.
- Doro phones are sold in Portugal through online retailers such as gomibo.pt.

Sources:
- Doro UK smartphones: https://uk.doro.com/en-gb/collections/smartphone `doro2026smartphones`
- Doro UK feature phones: https://uk.doro.com/en-gb/collections/feature-phone `doro2026featurephones`
- Doro Aurora A30 page: https://uk.doro.com/en-gb/products/doro-aurora-a30 `doro2026aurora`
- Launch coverage (TechBuzz Ireland): https://techbuzzireland.com/2025/05/20/doro-unveils-game-changing-aurora-smartphone-series-empowering-seniors-and-their-loved-ones/ `techbuzz2025doro`
- gomibo.pt: https://www.gomibo.pt/telefone/doro `gomibo2026doro`

**2. Lively Jitterbug.**
- Smart5 page (year of release 2026): "The simple list-based menu makes the phone easy to navigate". Features include voice typing, Google Assistant, "Ask Lively – 24/7 Operators" and a dedicated Urgent Response button that requires upgrading to the Urgent Response service. The phone "cannot be used with other carriers".
- Flip2 page: plans from $14.99 to $44.99 per month, plus a $35 activation fee and a $3.23 monthly admin fee. It has a read-out feature that speaks menu options aloud. The Flip2 is now sold out and replaced by the Flip3.

Sources:
- Phones: https://shop.lively.com/collections/phones `lively2026phones`
- Smart5: https://shop.lively.com/products/jitterbug-smart5-smartphone-for-seniors `lively2026smart5`
- Flip2: https://shop.lively.com/products/jitterbug-flip2-flip-phone `lively2026flip2`

**3. Apple Assistive Access.**
- Announced on 16 May 2023 as an interface that "distills apps and experiences to their essential features in order to lighten cognitive load". It shipped in iOS 17 on 18 September 2023.
- In May 2025 Apple added a simplified Apple TV app and an Assistive Access API so developers can build tailored versions of their apps.
- Settings include "Siri: Allow the use of Siri in Assistive Access".
- Apps that are not designed for Assistive Access open "in its original design" with "a large Back button at the bottom".

Sources:
- Announcement: https://www.apple.com/newsroom/2023/05/apple-previews-live-speech-personal-voice-and-more-new-accessibility-features/ `apple2023assistiveaccess`
- iOS 17 release: https://www.apple.com/ie/newsroom/2023/09/ios-17-is-available-today/ `apple2023ios17`
- 2025 update: https://www.apple.com/newsroom/2025/05/apple-unveils-powerful-accessibility-features-coming-later-this-year/ `apple2025accessibility`
- Support guide, setup: https://support.apple.com/guide/assistive-access-iphone/set-up-assistive-access-devcd5016d31/ios `apple2026aasetup`
- Support guide, settings: https://support.apple.com/guide/assistive-access-iphone/change-settings-dev073880b40/ios `apple2026aasettings`
- Support guide, apps: https://support.apple.com/guide/assistive-access-iphone/add-remove-and-customize-apps-devd7d3e3b512/ios `apple2026aaapps`

**4. Android simplified modes.**
- Samsung's support page (last updated 2026-06-24): Easy Mode "simplifies your Samsung Galaxy device interface by enlarging icons, fonts, and spacing". It is listed for Galaxy S24, S25, S26, Z Fold, Z Flip and A series phones.
- How-To Geek (2021) describes the longer touch-and-hold delay and the high-contrast keyboard.
- Google's Pixel Simple View (December 2024 Pixel Drop) "increases your phone's font size and touch sensitivity". It works on Pixel 6 and later.

Sources:
- Samsung: https://www.samsung.com/ae/support/mobile-devices/using-easy-mode-on-my-samsung-phone/ `samsung2026easymode`
- How-To Geek: https://www.howtogeek.com/736539/psa-samsung-galaxy-phones-have-easy-mode-for-better-accessibility/ `howtogeek2021easymode`
- Google blog: https://blog.google/products/pixel/pixel-drop-december-2024/ `google2024pixeldrop`
- Pixel Help: https://support.google.com/pixelphone/answer/15683160?hl=en `google2026simpleview`

**5. BIG Launcher.** The official site describes it as "a fast and simple Android home screen for seniors and people with vision problems" and says: "Full versions are one-time purchases — no subscriptions. Pricing varies by country". It lists 70 languages, including Português and Português BR. Source: https://biglauncher.com/home/en/ `biglauncher2026`

**6. ElliQ.**
- Launched commercially in the US on 15 March 2022 for older adults living alone, at $249.99 to enrol plus $29.99/month on an annual plan.
- Current pricing on the shop page: a "Lease initiation fee $249 / one time" plus membership at $59/month (monthly), $49/month (annual, $588) or $39/month (24 months, $936).
- The FAQ says "ElliQ is not a medical device".

Sources:
- Launch press release: https://www.prnewswire.com/news-releases/intuition-robotics-launches-elliq-the-award-winning-care-companion-robot-for-commercial-sale-301502682.html `intuition2022elliq`
- Pricing: https://elliq.com/products/membership `elliq2026membership`
- Home page: https://elliq.com/ `elliq2026home`

**7. Alexa and Google Nest.**
- Amazon launched Alexa Together in December 2021 at $19.99/month, with Urgent Response, fall detection through partner devices, an activity feed and remote setup by caregivers. Amazon's article now notes that the service is no longer available and has been replaced by Alexa Emergency Assist ("Alexa, call for help"; up to 25 emergency contacts).
- The languages supported for Alexa skills include Portuguese (BR) but not Portuguese from Portugal.
- When Echo devices launched in Portugal (July 2021), users had to set the Alexa app to English (US) because Portuguese (Portugal) was not available. In April 2026 Alexa+ still had no launch date for Portugal.
- Google's language table lists Portuguese only for Brazil on Nest speakers, and no Portuguese at all on Nest Hub displays.

Sources:
- Alexa Together: https://www.aboutamazon.com/news/devices/alexa-together-launches-to-help-customers-remotely-care-for-loved-ones `amazon2021alexatogether`
- Emergency Assist: https://www.aboutamazon.com/news/devices/alexa-emergency-assist `amazon2026emergencyassist`
- Alexa skill languages: https://developer.amazon.com/en-US/docs/alexa/custom-skills/develop-skills-in-multiple-languages.html `amazon2026alexalanguages`
- Pplware: https://pplware.sapo.pt/internet/echo-e-echo-dot-da-alexa-disponiveis-para-clientes-de-portugal/amp/ `pplware2021alexa`
- Techbit: https://techbit.pt/alexa-chega-espanha-para-breve-portugal/ `techbit2026alexaplus`
- Google Nest languages: https://support.google.com/googlehome/answer/7550584?hl=en `google2026nestlanguages`

**8. Clicks keyboard case.**
- Pass-through charging is confirmed. The FAQ says "You can still charge your phone with Clicks as normal via the Lightning or USB-C port at the bottom of the keyboard" and "Clicks does not contain a battery". The Pixel and Razr pages say "Clicks supports passthrough charging with USB-C".
- Data pass-through depends on the model: USB-C charging and data on iPhone 16 and 17, charging only on iPhone 15, Lightning charging and data on iPhone 14 Pro.
- Wireless charging: the Pixel case has a magnetic array. On the Razr, "Motorola Razr disables wireless charging whenever something is plugged in via USB-C".
- Supported phones listed today: iPhone 14 Pro/Pro Max, 15, 16 and 17 series (Clicks says it is still evaluating iPhone Air); Pixel 9/9 Pro (not XL); Razr 2024 and 2025.
- A Galaxy S25 model was announced in February 2025 but is not in Clicks' current shop or FAQ.
- Clicks also sells a separate **Power Keyboard**. It connects by Bluetooth, attaches with MagSafe/Qi2 magnets, and has its own 2,300 mAh battery that can top up the phone wirelessly (5 W Qi). This is a design without a port connection, but it needs charging itself.

Sources:
- FAQ: https://www.clicks.tech/pages/support `clicks2026support`
- iPhone 17 case: https://www.clicks.tech/products/clicks-keyboard-for-iphone-17 `clicks2026iphone17`
- Pixel case: https://www.clicks.tech/products/clicks-for-google-pixel `clicks2026pixel`
- Razr case: https://www.clicks.tech/products/clicks-keyboard-for-motorola-razr-2025 `clicks2026razr`
- Power Keyboard: https://www.clicks.tech/powerkeyboard `clicks2026powerkeyboard`
- TechCrunch, Android launch: https://techcrunch.com/2025/02/25/clicks-brings-its-blackberry-style-keyboard-case-to-android-users `techcrunch2025clicks`

**9. Light Phone.**
- Engadget (27 March 2025) on the Light Phone III: "There are no third-party apps. There's no access to social media, the internet or even email."
- Light's shop lists the Light Phone III at $899 and the Light Flip at $349.
- The Light Flip uses "a 12-button keypad" and was not expected to ship until April 2027 (Engadget, 21 July 2026).

Sources:
- Light Phone III shop page: https://www.thelightphone.com/shop/products/light-phone-iii `light2026phoneiii`
- Light Flip shop page: https://www.thelightphone.com/shop/products/light-flip `light2026flip`
- Engadget, Light Phone III: https://www.engadget.com/mobile/smartphones/the-minimalist-light-phone-iii-is-officially-available-for-purchase-150056138.html `engadget2025lightphone`
- Engadget, Light Flip: https://www.engadget.com/2219683/the-light-flip-is-a-minimalist-flip-phone-that-leaves-out-all-those-distracting-apps/ `engadget2026lightflip`

**10. Teleassistance in Portugal.**
- The Cruz Vermelha (CVP) service gives "contacto imediato com o nosso Centro de Atenção 24 horas por dia" through a device for use "em casa ou no exterior". It covers the whole country, including the islands.
- It has four plans: Segurança and +Segurança (home equipment), and Independência and +Independência (a portable device with an emergency button and GPS).
- Base plans include follow-up calls every two weeks, calls to help users get used to the equipment, and birthday calls. The "+" plans add weekly calls and home medical visits.
- The terms say the equipment is rented and the service is billed monthly.
- Porto's "Estamos Juntos" gives free teleassistance (a water-resistant device usable "dentro e fora de casa", inside and outside the home) to residents aged 65+ who are economically vulnerable and at risk of isolation. It is co-funded by the EU programme NORTE 2030.

Sources:
- CVP service page: https://www.cruzvermelha.pt/servicos/teleassistencia/ `cvp2026teleassistencia`
- CVP terms and conditions: https://www.cruzvermelha.pt/files/uploads/2024/10/F.COT_.001-01_Termos_e_Condicoes-1.pdf `cvp2024termos`
- Porto "Estamos Juntos": https://acaosocial.cm-porto.pt/pessoas-idosas/programa-estamos-juntos `porto2026estamosjuntos`

---

## Design implications

1. **Make the shell and a trusted helper carry the complexity, not the user.** In Portugal, 36.1% of people aged 65–74 have never used the internet (F8), only 20.65% have basic digital skills (F11), and 5.8% have used AI tools (F10). People aged 75+ are not measured at all (F7–F12). This supports a few fixed actions behind one physical button, with contacts and reminders set up by family. Existing products take the same approach: Apple's "trusted supporter" with a passcode to exit, BIG Launcher's remote setup with PIN-locked settings, and Doro's remote help through TeamViewer. The shell should physically hide everything else on the screen, so nothing can be changed or exited by accident.

2. **Build "call" and "read messages" around the channel the family already uses, probably WhatsApp.** Once online, Portuguese people aged 65–74 message (79.8%) and call over the internet (70.5%) more than their EU peers (F9). The products that cut complexity most either drop third-party apps altogether (Light Phone: no WhatsApp) or show them in their normal interface (Apple Assistive Access). A voice layer over the family's real messaging channel would fill that gap. Check with the co-designer which apps her family actually uses.

3. **Treat European Portuguese speech as a top risk and prototype it early.** Mainstream voice assistants don't support it: Alexa in Portugal runs in English (US) and its skills support only Brazilian Portuguese, Google Nest speakers support only Brazilian Portuguese, and ElliQ is sold only in the US and advertises only English (Part 2, rows 6–7). Use Wizard-of-Oz first. Keep commands short, using the names in the contact list. Always confirm out loud before acting ("Ligar à Maria?") and let a second press cancel. Keep large on-screen contact tiles as a fallback.

4. **Design fraud out of the device.**
   - The threat is real: the GNR alone recorded 2,720 frauds against older people from January to August 2026 (F22). The "Olá pai, olá mãe" scam impersonates children over WhatsApp and avoids voice calls (F26), and MB WAY scams trick people into payment actions (F27).
   - The push-to-talk device should never start payments, MB WAY actions or links.
   - When reading a message aloud, it should say who sent it and warn when the number is new or unknown.
   - "Call them back on their saved number" should be one press away, which turns the PSP's advice into a single action.
   - The contact list should be curated by family.

5. **Work alongside existing safety services rather than replacing them, and avoid subscriptions.**
   - Many older people live alone: 517,146 aged 65+ in 2021 (F16), 25.7% of people 65+ (33.5% of women) in 2025 (F18), and 43,074 flagged as isolated or vulnerable by the GNR (F20).
   - Telecare already exists: Cruz Vermelha's 24/7 service at €24–€34/month with regular follow-up calls, and free municipal schemes (row 10).
   - The button should not be presented as an emergency alarm, to avoid a false sense of safety. "Call my daughter" and reminders are the right scope.
   - With 17.8% of people 65+ at risk of poverty (F21), and subscription services ending (Alexa Together, row 7) or costing $39–$59/month (ElliQ), reusing an existing Android phone with a one-off printed shell is a strong option.

6. **Design the shell around charging and one specific phone model.**
   - Clicks shows that a case plugged into the phone's port can pass charging through (its keyboard has no battery). It also shows the pitfalls: wireless charging can stop while USB-C is in use (Razr), and every phone model needs its own fit (row 8).
   - If the push-to-talk button plugs into USB-C, keep a charging path open, either a pass-through port or a cradle like Doro's Leva phones. If the button uses Bluetooth instead, it needs its own battery and charging routine (like the Clicks Power Keyboard).
   - Make the button large, tactile and distinct. Doro's Secure Button, Jitterbug's Urgent Response button and telecare alarm buttons show that older users already recognise this pattern.

---

## Gaps and caveats

- **No official technology data for people aged 75+ in Portugal.** INE's survey covers ages 16–74, and Eurostat has no 2025 data for Portugal aged 75+. Marktest (F13) is the only source found that covers all people 65+, and it is a commercial survey of mainland Portugal.
- **Series break in INE population figures.** INE's June 2026 revision changes the population shares (F3), while Eurostat (F1) and the INE projections (F5) still use the earlier series.
- **No official national statistic on phone or online fraud by victim age.** GNR and PSP figures each cover only their own area and include all types of fraud. APAV only counts people who asked for support.
- **No official figure for smartphone ownership by age.** Neither ANACOM nor INE publishes one; Eurostat and Marktest measure using a phone to go online. No Obercom report giving smartphone figures for people 65+ was found.
- **Banco de Portugal pages blocked automated access.** Those figures were checked in reports from RTP and SAPO that quote the Banco de Portugal.
- **Prices change often.** Several products were on sale on 2026-10-05.
