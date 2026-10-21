"""Orthographic CPU rasterizer for the actual CAD triangles; no graphics driver."""
import json
from pathlib import Path
import numpy as np
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
DATA = json.loads((ROOT / "preview/mesh.json").read_text())
TEXTURE = np.array(Image.open(ROOT / "preview/screen_alignment_illustration.png").convert("RGB"))
W, H, SCALE = 1500, 1900, 8.1


def normalized(vector):
    vector = np.asarray(vector, dtype=float)
    return vector / np.linalg.norm(vector)


def raster_triangle(points, world, color, canvas, depth, texture=False):
    x0, y0 = np.maximum(np.floor(points[:, :2].min(axis=0)).astype(int), (0, 0))
    x1, y1 = np.minimum(np.ceil(points[:, :2].max(axis=0)).astype(int), (W - 1, H - 1))
    if x1 < x0 or y1 < y0:
        return
    xx, yy = np.meshgrid(np.arange(x0, x1 + 1) + .5, np.arange(y0, y1 + 1) + .5)
    a, b, c = points
    den = (b[1] - c[1]) * (a[0] - c[0]) + (c[0] - b[0]) * (a[1] - c[1])
    if abs(den) < 1e-8:
        return
    u = ((b[1] - c[1]) * (xx - c[0]) + (c[0] - b[0]) * (yy - c[1])) / den
    v = ((c[1] - a[1]) * (xx - c[0]) + (a[0] - c[0]) * (yy - c[1])) / den
    t = 1 - u - v
    zz = u * a[2] + v * b[2] + t * c[2]
    region = depth[y0:y1 + 1, x0:x1 + 1]
    mask = (u >= -1e-7) & (v >= -1e-7) & (t >= -1e-7) & (zz > region)
    region[mask] = zz[mask]
    patch = canvas[y0:y1 + 1, x0:x1 + 1]
    if texture:
        wx = u * world[0, 0] + v * world[1, 0] + t * world[2, 0]
        wy = u * world[0, 1] + v * world[1, 1] + t * world[2, 1]
        tx = np.clip(((wx / 70.2 + .5) * (TEXTURE.shape[1] - 1)).astype(int), 0, TEXTURE.shape[1] - 1)
        ty = np.clip(((.5 - wy / 156.3) * (TEXTURE.shape[0] - 1)).astype(int), 0, TEXTURE.shape[0] - 1)
        patch[mask] = TEXTURE[ty[mask], tx[mask]]
    else:
        patch[mask] = color


def render(name, direction, names, exploded=False):
    look = normalized(direction)
    right = normalized(np.cross((0, 1, 0), look))
    up = np.cross(look, right)
    basis = np.array([right, up, look]).T
    only_front = names == ["keyguard"]
    center = np.array((0, -63.8 if only_front else -2, 6.75 if only_front else 14 if exploded else 3))
    scale = 14.5 if only_front else SCALE
    canvas = np.full((H, W, 3), (237, 239, 235), dtype=np.uint8)
    depth = np.full((H, W), -np.inf, dtype=np.float32)
    parts = [dict(part) for part in DATA["parts"] if part["name"] in names]
    if "screen_reference" in names:
        parts = [p for p in parts if p["name"] != "screen_reference"]
        parts.append({"name": "texture", "color": "#ffffff", "vertices":
                      [[-35.1, -78.15, 8.531], [35.1, -78.15, 8.531],
                       [35.1, 78.15, 8.531], [-35.1, 78.15, 8.531]],
                      "triangles": [[0, 1, 2], [0, 2, 3]]})
    for part in parts:
        vertices = np.array(part["vertices"], dtype=float)
        if exploded and part["name"] == "keyguard":
            vertices += (0, -13, 40)
        projected = (vertices - center) @ basis
        projected[:, 0] = projected[:, 0] * scale + W / 2
        projected[:, 1] = -projected[:, 1] * scale + H / 2
        color = np.array([int(part["color"][i:i + 2], 16) for i in (1, 3, 5)])
        for triangle in part["triangles"]:
            world = vertices[triangle]
            normal = normalized(np.cross(world[1] - world[0], world[2] - world[0]))
            if normal @ look <= 0:
                continue
            shade = .65 + .72 * max(0, normal @ normalized((-.45, .4, .85)))
            shade += .20 * max(0, normal @ normalized((.8, -.3, .4)))
            shaded = np.clip(color * shade, 0, 255).astype(np.uint8)
            raster_triangle(projected[triangle], world, shaded, canvas, depth, part["name"] == "texture")
    output = Image.fromarray(canvas).resize((750, 950), Image.Resampling.LANCZOS)
    output.save(ROOT / "preview" / name)
    return output


def main():
    all_names = [p["name"] for p in DATA["parts"]]
    images = [render("assembled.png", (.22, -.24, 1), all_names),
              render("exploded.png", (.47, -.30, 1), all_names, True),
              render("front_keyguard.png", (.32, -.38, 1), ["keyguard"]),
              render("rear_case.png", (-.18, .18, -1), ["back_case"])]
    sheet = Image.new("RGB", (1600, 2240), "#ede feb".replace(" ", ""))
    draw = ImageDraw.Draw(sheet)
    fonts = Path("/System/Library/Fonts/Supplemental")
    title = ImageFont.truetype(str(fonts / "Arial Bold.ttf"), 45)
    label = ImageFont.truetype(str(fonts / "Arial.ttf"), 26)
    draw.text((55, 40), "Pixel 9 Pro XL", font=title, fill="#233744")
    draw.text((55, 103), "PETG back case + removable front / tested ring profile", font=label, fill="#566870")
    captions = ["01 / Front clipped on", "02 / Front removed", "03 / Three open finger holes", "04 / Normal back case"]
    for index, (image, caption) in enumerate(zip(images, captions)):
        x, y = 25 + (index % 2) * 800, 170 + (index // 2) * 1010
        sheet.paste(image, (x, y))
        draw.text((x + 25, y + 938), caption, font=label, fill="#233744")
    draw.text((55, 2200), "Rev B — ring fit reported; clips, depth and hardware clearance need a trial print.",
              font=ImageFont.truetype(str(fonts / "Arial.ttf"), 24), fill="#657278")
    sheet.save(ROOT / "preview/Pixel_9_Pro_XL_Keyguard_Preview.png")
    print("CPU previews generated from actual CAD meshes")


if __name__ == "__main__":
    main()
