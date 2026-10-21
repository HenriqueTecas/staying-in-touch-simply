"""Make an illustrative screen alignment image and an offline CAD viewer."""
import base64
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
P = json.loads((ROOT / "parameters.json").read_text())
FONTS = Path("/System/Library/Fonts/Supplemental")


def font(size, bold=False):
    return ImageFont.truetype(str(FONTS / ("Arial Bold.ttf" if bold else "Arial.ttf")), size)


def screen_image():
    d, phone, guard = P["display_reference"], P["phone"], P["keyguard"]
    w, h = d["pixel_width"], d["pixel_height"]
    image = Image.new("RGB", (w, h), "#edf2f4")
    draw = ImageDraw.Draw(image)
    draw.text((85, 70), "9:41", font=font(54, True), fill="#233744")
    draw.text((100, 360), "Good morning", font=font(100, True), fill="#143649")
    draw.text((100, 500), "What would you like to do?", font=font(54), fill="#526978")
    for y, label, detail in [(740, "Call someone", "Contacts and favourites"),
                             (1070, "Messages", "Read and reply"),
                             (1400, "Take a photo", "Open the camera"),
                             (1730, "Photos", "Your favourite moments")]:
        draw.rounded_rectangle((75, y, w - 75, y + 270), radius=46, fill="white")
        draw.text((140, y + 52), label, font=font(76, True), fill="#17394b")
        draw.text((140, y + 157), detail, font=font(46), fill="#617784")
    guard_top = -phone["height"] / 2 + guard["height_from_phone_bottom"]
    top = (d["active_height_assumed"] / 2 - guard_top) / d["active_height_assumed"] * h
    draw.rectangle((0, top, w, h), fill="#172e3e")
    y_mm = -phone["height"] / 2 + guard["hole_row_from_phone_bottom"]
    cy = (d["active_height_assumed"] / 2 - y_mm) / d["active_height_assumed"] * h
    for x, diameter, color, label in zip(guard["hole_x"], guard["hole_diameters"],
                                       ["#27994d", "#2767dc", "#d84442"], ["CALL", "SPEAK", "END"]):
        cx = (x / d["active_width_assumed"] + .5) * w
        radius = diameter / 2 / d["active_width_assumed"] * w
        draw.ellipse((cx - radius - 8, cy - radius - 8, cx + radius + 8, cy + radius + 8), fill=color)
        draw.text((cx, cy + radius * .43), label, font=font(48, True), anchor="mm", fill="white")
        if label == "SPEAK":
            draw.rounded_rectangle((cx - 34, cy - 111, cx + 34, cy - 9), 32, fill="white")
            draw.arc((cx - 63, cy - 85, cx + 63, cy + 34), 0, 180, fill="white", width=12)
            draw.line((cx, cy + 34, cx, cy + 66), fill="white", width=12)
            draw.line((cx - 34, cy + 66, cx + 34, cy + 66), fill="white", width=12)
        else:
            draw.arc((cx - 70, cy - 75, cx + 70, cy + 27), 195, 345, fill="white", width=23)
            draw.rounded_rectangle((cx - 77, cy - 30, cx - 35, cy + 12), 12, fill="white")
            draw.rounded_rectangle((cx + 35, cy - 30, cx + 77, cy + 12), 12, fill="white")
    image.save(ROOT / "preview/screen_alignment_illustration.png")


def viewer():
    data = (ROOT / "preview/mesh.json").read_text()
    template = (ROOT / "source/viewer.html").read_text()
    vendor = (ROOT / "preview/three.min.js").read_text()
    screen = base64.b64encode((ROOT / "preview/screen_alignment_illustration.png").read_bytes()).decode()
    output = template.replace("__THREE_SOURCE__", vendor).replace("__CAD_DATA__", data)
    output = output.replace("__SCREEN_URI__", "data:image/png;base64," + screen)
    (ROOT / "preview/Open_3D_Preview.html").write_text(output)


if __name__ == "__main__":
    screen_image()
    viewer()
