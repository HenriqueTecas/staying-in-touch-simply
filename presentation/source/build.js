// Builds presentation/midterm-presentation.pptx (mid-term talk, about 5 minutes, 10 slides).
// Run from this folder: npm install, then node build.js. SKILL below points to the pptx skill on the machine that built it.
const path = require("path");
const pptxgen = require("pptxgenjs");
const React = require("react");
const ReactDOMServer = require("react-dom/server");
const sharp = require("sharp");
const fa = require("react-icons/fa");

const SKILL = "/Users/henrique/.claude/skills/synced/05ebc6af-f24f-4069-942a-8fa02cb223ea_b3331445-443d-4335-8781-82e4b00bde48/pptx";
const { applyTheme } = require(path.join(SKILL, "scripts/apply_theme.js"));
const OUT = path.resolve(__dirname, "../midterm-presentation.pptx");

const THEME = {
  name: "Staying in Touch",
  headFontFace: "Cambria",
  bodyFontFace: "Calibri",
  colors: {
    dk1: "1F1A24", // body text
    lt1: "FFFFFF",
    dk2: "2B2440", // dark slides
    lt2: "F2F0F6", // cards
    accent1: "E4572E", // the "button" coral, for shapes
    accent2: "17766B", // teal labels; the "talk" button
    accent3: "B23A1C", // dark coral: placeholder text; the "end" button
    accent4: "F2A541", // amber: accents on dark slides
    accent5: "6B6478", // muted text
    accent6: "DCD7E6", // quiet lines
    hlink: "17766B",
    folHlink: "6B6478",
  },
};

// Secondary-research figures for slide 4 (verified, see .context/research/secondary-research.md).
const STATS = require("./stats.js");

async function iconData(Icon, color = "#FFFFFF") {
  const svg = ReactDOMServer.renderToStaticMarkup(React.createElement(Icon, { color, size: 256 }));
  const png = await sharp(Buffer.from(svg)).resize(256, 256).png().toBuffer();
  return "image/png;base64," + png.toString("base64");
}

async function main() {
  const pres = new pptxgen();
  pres.layout = "LAYOUT_WIDE"; // 13.333 x 7.5 in
  pres.theme = { headFontFace: THEME.headFontFace, bodyFontFace: THEME.bodyFontFace };
  pres.title = "Staying in touch, simply — mid-term presentation";
  pres.author = "Henrique Rodrigues, João Rocha";
  const C = pres.SchemeColor;
  const S = pres.shapes;

  const icons = {
    phone: await iconData(fa.FaPhoneAlt),
    home: await iconData(fa.FaHome),
    mobile: await iconData(fa.FaMobileAlt),
    people: await iconData(fa.FaUserFriends),
    stories: await iconData(fa.FaComments),
    tour: await iconData(fa.FaHandPointer),
    map: await iconData(fa.FaProjectDiagram),
    check: await iconData(fa.FaCheck),
    cross: await iconData(fa.FaTimes),
  };

  const FOOTER = "Staying in touch, simply · Mid-term · Week of 5 October 2026";

  // ---------- Layouts ----------
  pres.defineSlideMaster({
    title: "TITLE_DARK",
    background: { color: C.text2 },
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.8, y: 1.75, w: 7.4, h: 1.5, fontSize: 48, bold: true, align: "left", color: C.background1, valign: "bottom", margin: 0 }, text: "" } },
      { placeholder: { options: { name: "body", type: "body", x: 0.8, y: 3.35, w: 7.4, h: 0.9, fontSize: 22, color: C.background2, valign: "top", margin: 0 }, text: "" } },
    ],
  });
  pres.defineSlideMaster({
    title: "CONTENT",
    background: { color: C.background1 },
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.4, w: 12.1, h: 0.8, fontSize: 32, bold: true, align: "left", color: C.text1, valign: "middle", margin: 0 }, text: "" } },
      { text: { text: FOOTER, options: { x: 0.6, y: 6.95, w: 8.0, h: 0.3, fontSize: 11, color: C.accent5, margin: 0 } } },
    ],
    slideNumber: { x: 12.2, y: 6.95, w: 0.5, h: 0.3, fontSize: 11, color: C.accent5, align: "right" },
  });
  pres.defineSlideMaster({
    title: "CLOSING_DARK",
    background: { color: C.text2 },
    objects: [
      { placeholder: { options: { name: "title", type: "title", x: 0.6, y: 0.4, w: 12.1, h: 0.8, fontSize: 32, bold: true, align: "left", color: C.background1, valign: "middle", margin: 0 }, text: "" } },
    ],
    slideNumber: { x: 12.2, y: 6.95, w: 0.5, h: 0.3, fontSize: 11, color: C.background2, align: "right" },
  });

  // ---------- Helpers ----------
  // Fresh objects every time: pptxgenjs mutates option objects in place.
  const phLight = () => ({ italic: true, color: C.accent3 }); // placeholder text on light slides

  function iconCircle(slide, data, x, y, d, name, color) {
    const fillColor = color || C.accent1;
    slide.addShape(S.OVAL, { x, y, w: d, h: d, fill: { color: fillColor }, line: { color: fillColor, width: 0 }, objectName: name + " circle" });
    slide.addImage({ data, x: x + d * 0.26, y: y + d * 0.26, w: d * 0.48, h: d * 0.48, objectName: name + " icon", altText: name });
  }

  function numberCircle(slide, n, x, y, d, name) {
    slide.addShape(S.OVAL, { x, y, w: d, h: d, fill: { color: C.accent1 }, line: { color: C.accent1, width: 0 }, objectName: name + " circle" });
    slide.addText(String(n), { x, y, w: d, h: d, fontSize: 14, bold: true, color: C.background1, align: "center", valign: "middle", margin: 0, isTextBox: true, objectName: name + " number" });
  }

  function placeholderBox(slide, x, y, w, h, text, name) {
    slide.addShape(S.ROUNDED_RECTANGLE, { x, y, w, h, rectRadius: 0.12, fill: { color: C.background1 }, line: { color: C.accent1, width: 1.5, dashType: "dash" }, objectName: name });
    slide.addText(text, { x: x + 0.25, y: y + 0.2, w: w - 0.5, h: h - 0.4, fontSize: 15, ...phLight(), align: "center", valign: "middle", margin: 0, isTextBox: true, objectName: name + " text" });
  }

  function card(slide, x, y, w, h, name) {
    slide.addShape(S.ROUNDED_RECTANGLE, { x, y, w, h, rectRadius: 0.12, fill: { color: C.background2 }, line: { color: C.background2, width: 0 }, objectName: name });
  }

  function meta(slide, rest, name) {
    slide.addText([
      { text: "[Date]", options: phLight() },
      { text: rest, options: { color: C.accent5 } },
    ], { x: 0.6, y: 1.2, w: 12.1, h: 0.35, fontSize: 15, margin: 0, isTextBox: true, objectName: name });
  }

  // ---------- 1. Title ----------
  pres.addSection({ title: "Opening" });
  {
    const s = pres.addSlide({ masterName: "TITLE_DARK", sectionTitle: "Opening" });
    s.addText("Staying in touch, simply", { placeholder: "title" });
    s.addText("Co-designing a way for an older adult to use his smartphone", { placeholder: "body" });
    s.addText("Mid-term presentation · Co-Designing Inclusive Technologies · Week of 5 October 2026", {
      x: 0.8, y: 4.55, w: 7.4, h: 0.4, fontSize: 15, color: C.accent6, margin: 0, isTextBox: true, objectName: "Meta",
    });
    s.addText("Henrique Rodrigues · João Rocha", { x: 0.8, y: 5.05, w: 7.4, h: 0.4, fontSize: 16, color: C.background1, margin: 0, isTextBox: true, objectName: "Team" });
    // The motif: one big round button.
    s.addShape(S.OVAL, { x: 8.75, y: 1.75, w: 4.0, h: 4.0, fill: { color: C.accent1 }, line: { color: C.accent1, width: 0 }, objectName: "Big button" });
    s.addShape(S.OVAL, { x: 9.1, y: 2.1, w: 3.3, h: 3.3, fill: { type: "none" }, line: { color: C.background1, width: 2.5, transparency: 55 }, objectName: "Button ring" });
    s.addImage({ data: icons.phone, x: 10.0, y: 3.0, w: 1.5, h: 1.5, objectName: "Phone icon", altText: "Phone handset" });
    s.addNotes("Henrique (0:15). Hi, I'm Henrique, and this is João. Our project is Staying in touch, simply: we're co-designing, with an older adult, a way for him to use the smartphone his family gave him. [Click]");
  }

  // ---------- 2. Where we are ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Opening" });
    s.addText("Where we are: two sessions done, one to go", { placeholder: "title" });
    const dy = 1.65, dh = 2.6, dw = 5.0, x1 = 1.65, x2 = x1 + dw;
    s.addShape(S.DIAMOND, { x: x1, y: dy, w: dw, h: dh, fill: { color: C.background2 }, line: { color: C.accent1, width: 2 }, objectName: "Diamond 1" });
    s.addShape(S.DIAMOND, { x: x2, y: dy, w: dw, h: dh, fill: { color: C.background2 }, line: { color: C.accent1, width: 2 }, objectName: "Diamond 2" });
    s.addShape(S.LINE, { x: x1 + dw / 2, y: dy, w: 0, h: dh, line: { color: C.accent5, width: 1, dashType: "dash" }, objectName: "Divider 1" });
    s.addShape(S.LINE, { x: x2 + dw / 2, y: dy, w: 0, h: dh, line: { color: C.accent5, width: 1, dashType: "dash" }, objectName: "Divider 2" });
    [["Discover", x1 + dw * 0.3], ["Define", x1 + dw * 0.7], ["Develop", x2 + dw * 0.3], ["Deliver", x2 + dw * 0.7]].forEach(([txt, cx]) => {
      s.addText(txt, { x: cx - 0.9, y: dy + dh / 2 - 0.25, w: 1.8, h: 0.5, fontSize: 18, bold: true, color: C.text1, align: "center", valign: "middle", margin: 0, isTextBox: true, objectName: txt + " label" });
    });
    const sessions = [
      ["Session 1 · done", "Problem framed and signed", x1 + dw / 2, x1 + 0.3, x1 + dw - 0.3, true],
      ["Session 2 · done", "Concept chosen by Manuel", x2 + dw * 0.25, x2 + 0.15, x2 + dw / 2 - 0.1, true],
      ["Session 3 · by 16 Oct", "Test the prototype at home", x2 + dw * 0.75, x2 + dw / 2 + 0.1, x2 + dw - 0.15, false],
    ];
    sessions.forEach(([name, desc, cx, l, r, done]) => {
      s.addShape(S.LINE, { x: l, y: 4.75, w: r - l, h: 0, line: { color: C.accent6, width: 2 }, objectName: name + " span" });
      if (done) {
        iconCircle(s, icons.check, cx - 0.25, 4.5, 0.5, name);
      } else {
        s.addShape(S.OVAL, { x: cx - 0.25, y: 4.5, w: 0.5, h: 0.5, fill: { color: C.background1 }, line: { color: C.accent1, width: 2 }, objectName: name + " circle" });
      }
      s.addText(name, { x: cx - 1.3, y: 5.15, w: 2.6, h: 0.4, fontSize: 17, bold: true, color: C.text1, align: "center", margin: 0, isTextBox: true, objectName: name + " name" });
      s.addText(desc, { x: cx - 1.3, y: 5.55, w: 2.6, h: 0.4, fontSize: 14, color: C.accent5, align: "center", margin: 0, isTextBox: true, objectName: name + " desc" });
    });
    s.addNotes("Henrique (0:20). We follow the Double Diamond. In session one we explored and defined the problem with him; in session two he developed the ideas with us. Next we build the prototype, and session three tests it with him by October 16th. [Click]");
  }

  // ---------- 3. Co-designer and problem ----------
  pres.addSection({ title: "Problem framing" });
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Problem framing" });
    s.addText("Manuel has a smartphone, but cannot work it", { placeholder: "title" });
    s.addText("Manuel, 80", { x: 0.6, y: 1.45, w: 5.6, h: 0.6, fontSize: 28, bold: true, fontFace: THEME.headFontFace, color: C.text1, margin: 0, isTextBox: true, objectName: "Name" });
    const facts = [
      [icons.home, "Home", "Lives alone in Lisbon; plays bisca at the café every day"],
      [icons.mobile, "Phone", "His daughter bought him a smartphone in July; he does not know how to work it, so it sits in a drawer"],
      [icons.people, "People", "His family shares everything in a WhatsApp group: news, and photos of his newborn great-grandson"],
    ];
    facts.forEach(([ic, name, text], i) => {
      const y = 2.3 + i * 0.85;
      iconCircle(s, ic, 0.6, y + 0.1, 0.5, name);
      s.addText(text, { x: 1.3, y, w: 4.9, h: 0.72, fontSize: 16, color: C.text1, valign: "middle", margin: 0, isTextBox: true, objectName: name + " fact" });
    });
    card(s, 6.7, 1.5, 6.0, 3.55, "Problem statement card");
    s.addText("THE PROBLEM STATEMENT HE SIGNED", { x: 7.0, y: 1.72, w: 5.4, h: 0.3, fontSize: 12, bold: true, color: C.accent2, charSpacing: 1, margin: 0, isTextBox: true, objectName: "Problem label" });
    s.addText("Manuel needs a way to see the new photos of his great-grandson, to hear the family group and to reply with a short sentence, without learning the smartphone. He needs the sender’s name first, because a message that says “olá pai” may be a scam.", { x: 7.0, y: 2.1, w: 5.4, h: 2.75, fontSize: 20, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: "Problem statement" });
    s.addText("“They already got it. They did not get me a way to work it.”", { x: 0.6, y: 5.15, w: 12.1, h: 0.85, fontSize: 28, italic: true, fontFace: THEME.headFontFace, color: C.accent3, valign: "middle", margin: 0, isTextBox: true, objectName: "Pull quote" });
    s.addText("Manuel, session 1", { x: 0.6, y: 6.05, w: 6.0, h: 0.35, fontSize: 14, color: C.accent5, margin: 0, isTextBox: true, objectName: "Quote attribution" });
    s.addNotes("Henrique (0:40). This is Manuel. He's eighty and lives alone in Lisbon. In July his daughter bought him a smartphone so he could join the family WhatsApp group, where the photos of his newborn great-grandson are. He doesn't know how to work it: his taps are too short, gestures do the opposite of what he expects, and the keyboard opens by accident. So it sits uncharged in a drawer. In his words: “They already got it. They did not get me a way to work it.” He corrected our problem statement twice before signing it. [Hand over: “João.” Click]");
  }

  // ---------- 4. Why it matters ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Problem framing" });
    s.addText(STATS.title, { placeholder: "title" });
    STATS.callouts.forEach((c, i) => {
      const x = 0.6 + i * 4.15;
      s.addText(c.value, { x, y: 1.45, w: 3.85, h: 1.0, fontSize: 54, bold: true, fontFace: THEME.headFontFace, color: C.accent3, margin: 0, isTextBox: true, objectName: "Stat " + (i + 1) });
      s.addText(c.label, { x, y: 2.5, w: 3.85, h: 0.95, fontSize: 16, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: "Stat label " + (i + 1) });
      s.addText(c.source, { x, y: 3.5, w: 3.85, h: 0.3, fontSize: 11, color: C.accent5, margin: 0, isTextBox: true, objectName: "Stat source " + (i + 1) });
    });
    s.addText("What already exists", { x: 0.6, y: 4.2, w: 12.1, h: 0.4, fontSize: 18, bold: true, color: C.text1, margin: 0, isTextBox: true, objectName: "Exists label" });
    STATS.products.forEach((p, i) => {
      const x = 0.6 + i * 4.15;
      card(s, x, 4.75, 3.85, 1.2, "Product " + (i + 1));
      s.addText([
        { text: p.kind, options: { bold: true, breakLine: true } },
        { text: p.example, options: { color: C.accent5 } },
      ], { x: x + 0.25, y: 4.85, w: 3.35, h: 1.0, fontSize: 15, color: C.text1, valign: "middle", margin: 0, isTextBox: true, objectName: "Product text " + (i + 1) });
    });
    s.addText(STATS.gap, { x: 0.6, y: 6.12, w: 12.1, h: 0.6, fontSize: 17, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: "Gap" });
    s.addNotes(STATS.notes);
  }

  // ---------- 5. Session 1 · methods ----------
  pres.addSection({ title: "Sessions" });
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Sessions" });
    s.addText("Session 1 · Exploring the problem", { placeholder: "title" });
    meta(s, " · his kitchen · about 70 minutes · Discover and Define", "Session 1 meta");
    const methods = [
      [icons.stories, "Directed storytelling", "The missed blood test, the Sunday lunch, the “olá pai” scam", "Materials: interview guide, recorder"],
      [icons.tour, "Phone tour", "On the Samsung: taps too short, a pinch that shrank the text, the keyboard by accident", "Materials: both his phones"],
      [icons.map, "Communication map", "He placed every dot: calls with known names green, “the group” red", "Materials: A3 sheet, green and red dots"],
    ];
    methods.forEach(([ic, name, desc, mat], i) => {
      const x = 0.6 + i * 4.15, y = 1.75;
      card(s, x, y, 3.85, 2.25, name + " card");
      iconCircle(s, ic, x + 0.25, y + 0.25, 0.6, name);
      s.addText(name, { x: x + 1.0, y: y + 0.3, w: 2.7, h: 0.5, fontSize: 18, bold: true, color: C.text1, valign: "middle", margin: 0, isTextBox: true, objectName: name + " name" });
      s.addText(desc, { x: x + 0.25, y: y + 0.98, w: 3.35, h: 0.78, fontSize: 15, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: name + " description" });
      s.addText(mat, { x: x + 0.25, y: y + 1.8, w: 3.35, h: 0.33, fontSize: 12, color: C.accent5, margin: 0, isTextBox: true, objectName: name + " materials" });
    });
    s.addText("How we adapted it to him", { x: 0.6, y: 4.3, w: 7.6, h: 0.4, fontSize: 18, bold: true, color: C.text1, margin: 0, isTextBox: true, objectName: "Adapted label" });
    s.addText([
      { text: "He hosted a game of bisca before any question", options: { bullet: true, breakLine: true } },
      { text: "We read the consent aloud, in plain Portuguese", options: { bullet: true, breakLine: true } },
      { text: "One of us asked at a time; we said every step aloud when we touched his phone", options: { bullet: true, breakLine: true } },
      { text: "He could refuse categories on the map, and did", options: { bullet: true } },
    ], { x: 0.6, y: 4.8, w: 7.8, h: 1.85, fontSize: 16, color: C.text1, paraSpaceAfter: 6, valign: "top", margin: 0, isTextBox: true, objectName: "Adaptations" });
    placeholderBox(s, 8.9, 4.3, 3.85, 2.3, "Insert photo: his communication map, initialled by him", "Map photo placeholder");
    s.addNotes("João (0:30). Session one took about seventy minutes in his kitchen. We used directed storytelling, a tour of both his phones, and a communication map. We adapted to him: before any question he made us play bisca, and he beat us. We read the consent form aloud in plain Portuguese, only one of us asked at a time, and he placed every dot on the map himself. [Click]");
  }

  // ---------- 6. Session 1 · in his words ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Sessions" });
    s.addText("Session 1 · What we learned, in his words", { placeholder: "title" });
    const quotes = [
      ["The smartphone confuses him", "“I broke the letters. Put them back. Do not laugh.”"],
      ["But he wants what is inside it", "“[My sister] could refuse. I cannot refuse, because the refusal includes the baby.”"],
      ["The news is in the group", "“She wrote Pai in a conversation with twelve people, and she thought that was telling me.”"],
      ["The name first", "“I still want to know the name before I see the child.”"],
    ];
    quotes.forEach(([label, q], i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 0.6 + col * 6.2, y = 1.5 + row * 2.6;
      card(s, x, y, 5.9, 2.35, label + " card");
      s.addText(label.toUpperCase(), { x: x + 0.35, y: y + 0.28, w: 5.2, h: 0.3, fontSize: 12, bold: true, color: C.accent2, charSpacing: 1, margin: 0, isTextBox: true, objectName: label + " label" });
      s.addText(q, { x: x + 0.35, y: y + 0.7, w: 5.2, h: 1.45, fontSize: 24, italic: true, fontFace: THEME.headFontFace, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: label + " quote" });
    });
    s.addNotes("João (0:35). Four things we learned, in his words. The smartphone confuses him: when a pinch shrank the text, he said “I broke the letters.” But he won't refuse it like his sister did, “because the refusal includes the baby.” The family's news lives in the group: he went to a blood test at the wrong hour because the change was only written there. And after an “olá pai” scam text, he wants the sender's name before anything else. [Hand over: “Henrique.” Click]");
  }

  // ---------- 7. Session 2 · card sort ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Sessions" });
    s.addText("Session 2 · The smartphone features he wants", { placeholder: "title" });
    meta(s, " · his kitchen · about 70 minutes · Develop · he sorted 16 cards and wrote 2 of his own", "Session 2 meta");
    const columns = [
      ["PRECISO · NEED", C.accent2, [
        ["The name before the photo*", true],
        ["Only people my daughter saved*", true],
        ["See family photos", false],
        ["Hear messages read aloud", false],
        ["Know if a message is a scam", false],
        ["Call someone, if as easy as the Nokia", false],
        ["Answer calls, same condition", false],
        ["Send a message by speaking, short phrases only", false],
      ]],
      ["GOSTAVA · WOULD LIKE", C.accent3, [
        ["Appointment reminders", false],
        ["Emergency help, only to call his daughter", false],
        ["Video call, only the baby, his face hidden", false],
        ["Time and date", false],
      ]],
      ["NÃO PRECISO · DON’T NEED", C.accent5, [
        ["Medication reminders", false],
        ["Torch", false],
        ["Radio: his late wife’s station stays in the kitchen", false],
        ["Take photos", false],
        ["Weather", false],
        ["Magnifier", false],
      ]],
    ];
    columns.forEach(([label, color, items], i) => {
      const x = 0.6 + i * 4.15, y = 1.75;
      card(s, x, y, 3.85, 4.85, label + " card");
      s.addText(label, { x: x + 0.25, y: y + 0.22, w: 3.35, h: 0.35, fontSize: 14, bold: true, color, charSpacing: 1, margin: 0, isTextBox: true, objectName: label + " label" });
      s.addText(items.map(([t, his], j) => ({
        text: t,
        options: { bullet: true, bold: his, color: his ? C.accent3 : C.text1, breakLine: j < items.length - 1 },
      })), { x: x + 0.25, y: y + 0.72, w: 3.35, h: 3.55, fontSize: 15, paraSpaceAfter: 5, valign: "top", margin: 0, isTextBox: true, objectName: label + " items" });
    });
    s.addText("* cards he wrote himself", { x: 0.85, y: 6.15, w: 3.35, h: 0.3, fontSize: 12, italic: true, color: C.accent3, margin: 0, isTextBox: true, objectName: "Own cards note" });
    s.addNotes("Henrique (0:30). In session two, he decided what it should be. We gave him sixteen cards, each one a smartphone feature. He kept the ones about his family: today's photo, messages read aloud, knowing a scam, calls, and short spoken replies. He also wrote two cards of his own: the name before the photo, and only people his daughter saved. He threw out the torch, the weather and the radio. As he put it: “Do not get confused and build me a nicer Nokia.” [Click]");
  }

  // ---------- 8. Session 2 · the form ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Sessions" });
    s.addText("Session 2 · The form he chose", { placeholder: "title" });
    s.addText("He held three empty 3D-printed mock-ups to his ear, flat on the table and in his pocket", { x: 0.6, y: 1.2, w: 12.1, h: 0.35, fontSize: 15, color: C.accent5, margin: 0, isTextBox: true, objectName: "Form meta" });
    const cols = [
      ["Slim bumper", false, "“My finger is disloyal.”", "All the icons stayed there to confuse him."],
      ["Full cover", true, "“This. I see one thing.”", "His own smartphone inside, one thing at a time. Green to talk, red to end. Dark and plain, “like the radio”."],
      ["Separate box", false, "“A third phone.”", "He already struggles with two."],
    ];
    cols.forEach(([name, chosen, quote, why], i) => {
      const x = 0.6 + i * 4.15, cx = x + 1.925;
      if (chosen) {
        s.addShape(S.ROUNDED_RECTANGLE, { x, y: 1.7, w: 3.85, h: 4.95, rectRadius: 0.12, fill: { color: C.background2 }, line: { color: C.accent1, width: 2 }, objectName: name + " highlight" });
      }
      // Simple drawings of the three mock-ups.
      if (i === 0) {
        s.addShape(S.ROUNDED_RECTANGLE, { x: cx - 0.62, y: 1.9, w: 1.24, h: 2.2, rectRadius: 0.15, fill: { color: C.background1 }, line: { color: C.text1, width: 1.5 }, objectName: "Bumper body" });
        for (let r = 0; r < 4; r++) {
          for (let c = 0; c < 3; c++) {
            s.addShape(S.ROUNDED_RECTANGLE, { x: cx - 0.45 + c * 0.32, y: 2.1 + r * 0.32, w: 0.22, h: 0.22, rectRadius: 0.04, fill: { color: C.accent6 }, line: { color: C.accent6, width: 0 }, objectName: "Bumper icon" });
          }
        }
        s.addShape(S.OVAL, { x: cx - 0.15, y: 3.62, w: 0.3, h: 0.3, fill: { color: C.accent1 }, line: { color: C.accent1, width: 0 }, objectName: "Bumper button" });
      } else if (i === 1) {
        s.addShape(S.ROUNDED_RECTANGLE, { x: cx - 0.62, y: 1.9, w: 1.24, h: 2.2, rectRadius: 0.15, fill: { color: C.text2 }, line: { color: C.text2, width: 0 }, objectName: "Cover body" });
        s.addShape(S.RECTANGLE, { x: cx - 0.47, y: 2.08, w: 0.94, h: 1.05, fill: { color: C.background2 }, line: { color: C.background2, width: 0 }, objectName: "Cover window" });
        s.addShape(S.OVAL, { x: cx + 0.27, y: 2.92, w: 0.15, h: 0.15, fill: { color: C.accent3 }, line: { color: C.background1, width: 0.75 }, objectName: "Cover red button" });
        s.addShape(S.OVAL, { x: cx - 0.2, y: 3.38, w: 0.4, h: 0.4, fill: { color: C.accent2 }, line: { color: C.background1, width: 0.75 }, objectName: "Cover green button" });
      } else {
        s.addShape(S.ROUNDED_RECTANGLE, { x: cx - 0.7, y: 2.45, w: 1.4, h: 1.2, rectRadius: 0.15, fill: { color: C.background1 }, line: { color: C.text1, width: 1.5 }, objectName: "Box body" });
        s.addShape(S.OVAL, { x: cx - 0.2, y: 2.85, w: 0.4, h: 0.4, fill: { color: C.accent1 }, line: { color: C.accent1, width: 0 }, objectName: "Box button" });
      }
      iconCircle(s, chosen ? icons.check : icons.cross, x + 0.3, 4.3, 0.42, name + (chosen ? " chosen" : " rejected"), chosen ? C.accent2 : C.accent5);
      s.addText((chosen ? "Chosen · " : "Rejected · ") + name, { x: x + 0.85, y: 4.3, w: 2.85, h: 0.42, fontSize: 17, bold: true, color: C.text1, valign: "middle", margin: 0, isTextBox: true, objectName: name + " verdict" });
      s.addText(quote, { x: x + 0.3, y: 4.9, w: 3.3, h: 0.45, fontSize: 17, italic: true, fontFace: THEME.headFontFace, color: C.text1, margin: 0, isTextBox: true, objectName: name + " quote" });
      s.addText(why, { x: x + 0.3, y: 5.4, w: 3.3, h: 1.1, fontSize: 14, color: C.accent5, valign: "top", margin: 0, isTextBox: true, objectName: name + " reason" });
    });
    s.addNotes("Henrique (0:35). Then he tried three empty 3D-printed shapes: at his ear, flat on the table, in his pocket. (Hold up the full cover.) He rejected the bumper because all the icons were still there to confuse him: “My finger is disloyal.” He rejected the box as “a third phone.” He chose the full cover. It keeps his own smartphone, with everything it can do, but shows one thing at a time: “This. I see one thing.” A green button to talk, where his thumb rests, and a red one to end. [Hand over: “João played the phone's voice. João?” Click]");
  }

  // ---------- 9. Session 2 · the voice ----------
  {
    const s = pres.addSlide({ masterName: "CONTENT", sectionTitle: "Sessions" });
    s.addText("Session 2 · How it should talk", { placeholder: "title" });
    s.addText("João voiced the phone while Manuel pressed a cardboard button; then Manuel told the story we drew", { x: 0.6, y: 1.2, w: 12.1, h: 0.35, fontSize: 15, color: C.accent5, margin: 0, isTextBox: true, objectName: "Voice meta" });
    s.addText("Rules from the role-play", { x: 0.6, y: 1.8, w: 6.2, h: 0.4, fontSize: 18, bold: true, color: C.text1, margin: 0, isTextBox: true, objectName: "Rules label" });
    const rules = [
      "No greeting: he knows he pressed the button",
      "The sender’s name first, then one sentence, then silence while he looks",
      "Unknown sender: no photo, no link, offer to call his daughter",
      "One voice message at a time",
      "His reply is repeated back before it is sent",
      "After two misunderstandings, it does nothing",
    ];
    s.addText(rules.map((t, j) => ({ text: t, options: { bullet: true, breakLine: j < rules.length - 1 } })),
      { x: 0.6, y: 2.3, w: 6.2, h: 3.6, fontSize: 16, color: C.text1, paraSpaceAfter: 7, valign: "top", margin: 0, isTextBox: true, objectName: "Rules" });
    s.addText("His storyboard", { x: 7.2, y: 1.8, w: 5.5, h: 0.4, fontSize: 18, bold: true, color: C.text1, margin: 0, isTextBox: true, objectName: "Storyboard label" });
    const panels = [
      "His granddaughter sends today’s photo; the phone says her name",
      "He presses green: “Quero ver.”",
      "The name again, one sentence, and the photo stays",
      "“Diz-lhe: ele é lindo.” It repeats; he says “É.”",
    ];
    panels.forEach((t, i) => {
      const col = i % 2, row = Math.floor(i / 2);
      const x = 7.2 + col * 2.8, y = 2.3 + row * 1.75;
      card(s, x, y, 2.65, 1.6, "Panel " + (i + 1));
      numberCircle(s, i + 1, x + 0.15, y + 0.15, 0.4, "Panel " + (i + 1));
      s.addText(t, { x: x + 0.15, y: y + 0.65, w: 2.35, h: 0.85, fontSize: 14, color: C.text1, valign: "top", margin: 0, isTextBox: true, objectName: "Panel " + (i + 1) + " text" });
    });
    s.addText("“Rude and safe. I prefer that to cheerful and wrong.”", { x: 0.6, y: 6.15, w: 12.1, h: 0.5, fontSize: 20, italic: true, fontFace: THEME.headFontFace, color: C.accent3, margin: 0, isTextBox: true, objectName: "Voice quote" });
    s.addNotes("João (0:30). In a role-play I was the phone's voice while he pressed a cardboard button. He corrected me straight away: no greeting. Say the name first, then one sentence, then stay quiet while he looks. Unknown senders never get a photo. And after two misunderstandings I should do nothing: “Rude and safe. I prefer that to cheerful and wrong.” His storyboard is the morning his great-grandson's photo arrives, and he answers: ele é lindo, he's beautiful. [Hand over: “Henrique.” Click]");
  }

  // ---------- 10. Lessons and next steps ----------
  pres.addSection({ title: "Next steps" });
  {
    const s = pres.addSlide({ masterName: "CLOSING_DARK", sectionTitle: "Next steps" });
    s.addText("Lessons and next steps", { placeholder: "title" });
    s.addText("Lessons so far", { x: 0.6, y: 1.55, w: 6.0, h: 0.45, fontSize: 20, bold: true, color: C.accent4, margin: 0, isTextBox: true, objectName: "Lessons label" });
    s.addText([
      { text: "Letting him host a game of bisca first put him in charge", options: { bullet: true, breakLine: true } },
      { text: "He needed things to refuse: decoy cards and blank cards made his choices clear", options: { bullet: true, breakLine: true } },
      { text: "We were wrong about one button: he needs red to end, or calls aren’t safe", options: { bullet: true } },
    ], { x: 0.6, y: 2.1, w: 6.0, h: 2.25, fontSize: 18, color: C.background1, paraSpaceAfter: 10, valign: "top", margin: 0, isTextBox: true, objectName: "Lessons" });
    s.addText([
      { text: "What he wants: ", options: { color: C.accent4, bold: true } },
      { text: "not a simpler phone, but his smartphone’s family features in a form he can use.", options: { color: C.background1 } },
    ], { x: 0.6, y: 4.45, w: 6.0, h: 0.8, fontSize: 16, valign: "top", margin: 0, isTextBox: true, objectName: "What he wants" });
    s.addText([
      { text: "His condition: ", options: { color: C.accent4, bold: true } },
      { text: "the Nokia stays until calling his daughter on the new phone is as easy as its green button.", options: { color: C.background1 } },
    ], { x: 0.6, y: 5.3, w: 6.0, h: 0.8, fontSize: 16, valign: "top", margin: 0, isTextBox: true, objectName: "Condition" });
    const steps = [
      ["Sessions 1 and 2", "Done · problem signed, concept chosen"],
      ["Prototype", "12–15 Oct · cover, buttons and voice"],
      ["Session 3", "By 16 Oct · test his storyboard at home"],
      ["Final presentation", "Week of 19 Oct · he may come and speak"],
    ];
    s.addShape(S.LINE, { x: 7.75, y: 1.85, w: 0, h: 3.6, line: { color: C.accent5, width: 2 }, objectName: "Timeline line" });
    steps.forEach(([name, when], i) => {
      const y = 1.65 + i * 1.2;
      s.addShape(S.OVAL, { x: 7.55, y, w: 0.4, h: 0.4, fill: { color: i === 0 ? C.accent1 : C.text2 }, line: { color: C.accent1, width: 2 }, objectName: name + " marker" });
      s.addText(name, { x: 8.25, y: y - 0.08, w: 4.45, h: 0.4, fontSize: 18, bold: true, color: C.background1, margin: 0, isTextBox: true, objectName: name + " name" });
      s.addText(when, { x: 8.25, y: y + 0.33, w: 4.45, h: 0.4, fontSize: 14, color: C.accent6, margin: 0, isTextBox: true, objectName: name + " when" });
    });
    s.addText("Thank you. We would love your feedback.", { x: 0.6, y: 6.2, w: 12.1, h: 0.5, fontSize: 20, bold: true, color: C.background1, margin: 0, isTextBox: true, objectName: "Thanks" });
    s.addNotes("Henrique (0:35). Three lessons. Letting him host a game first put him in charge. He needed things to refuse, so decoy and blank cards made his choices clear. And we were wrong about one button: without a red one to end, calls aren't safe for him. What he wants isn't a simpler phone, but his smartphone's family features in a form he can use. Next we build it, test it with him by the sixteenth, and he may join our final presentation. Thank you. [Pause, then invite questions]");
  }

  await pres.writeFile({ fileName: OUT });
  await applyTheme(OUT, THEME);
  console.log("Wrote " + OUT);
}

main().catch((e) => { console.error(e); process.exit(1); });
