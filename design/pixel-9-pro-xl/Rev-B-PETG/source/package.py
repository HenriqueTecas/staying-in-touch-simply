"""Make standard 3MF plates and a checked archive; no printer-specific settings."""
from collections import Counter
from hashlib import sha256
import json
from pathlib import Path
import struct
import xml.etree.ElementTree as ET
from zipfile import ZipFile, ZIP_DEFLATED
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
NS = "http://schemas.microsoft.com/3dmanufacturing/core/2015/02"
ET.register_namespace("", NS)


def stl_mesh(path):
    data = path.read_bytes()
    count = struct.unpack_from("<I", data, 80)[0]
    assert len(data) == 84 + 50 * count
    vertices, faces, lookup = [], [], {}
    for index in range(count):
        row = struct.unpack_from("<12fH", data, 84 + 50 * index)
        face = []
        for offset in (3, 6, 9):
            point = tuple(round(value, 5) for value in row[offset:offset + 3])
            if point not in lookup:
                lookup[point] = len(vertices)
                vertices.append(point)
            face.append(lookup[point])
        faces.append(face)
    return vertices, faces


def check_mesh(vertices, faces):
    edges, volume = Counter(), 0.0
    for a, b, c in faces:
        for edge in ((a, b), (b, c), (c, a)):
            edges[tuple(sorted(edge))] += 1
        volume += np.dot(vertices[a], np.cross(vertices[b], vertices[c])) / 6
    assert all(n == 2 for n in edges.values()) and volume > 0
    assert min(p[2] for p in vertices) == 0
    return round(float(volume), 3)


def plate(filename, placements):
    model = ET.Element(f"{{{NS}}}model", unit="millimeter", attrib={"{http://www.w3.org/XML/1998/namespace}lang": "en-US"})
    ET.SubElement(model, f"{{{NS}}}metadata", name="Title").text = filename.removesuffix(".3mf")
    ET.SubElement(model, f"{{{NS}}}metadata", name="Description").text = "PETG trial parts. Use your printer profile, 100% scale and 0.20 mm layers. See README.txt. No G-code included."
    resources = ET.SubElement(model, f"{{{NS}}}resources")
    build = ET.SubElement(model, f"{{{NS}}}build")
    bounds, volumes = [], {}
    for index, (part, x, y) in enumerate(placements, 1):
        vertices, faces = stl_mesh(ROOT / "stl" / f"{part}.stl")
        volumes[part] = check_mesh(vertices, faces)
        obj = ET.SubElement(resources, f"{{{NS}}}object", id=str(index), type="model", name=part)
        mesh = ET.SubElement(obj, f"{{{NS}}}mesh")
        v_node = ET.SubElement(mesh, f"{{{NS}}}vertices")
        for a, b, c in vertices:
            ET.SubElement(v_node, f"{{{NS}}}vertex", x=f"{a:.5f}", y=f"{b:.5f}", z=f"{c:.5f}")
        t_node = ET.SubElement(mesh, f"{{{NS}}}triangles")
        for a, b, c in faces:
            ET.SubElement(t_node, f"{{{NS}}}triangle", v1=str(a), v2=str(b), v3=str(c))
        ET.SubElement(build, f"{{{NS}}}item", objectid=str(index), transform=f"1 0 0 0 1 0 0 0 1 {x} {y} 0")
        bounds.append((min(p[0] for p in vertices) + x, min(p[1] for p in vertices) + y,
                       max(p[0] for p in vertices) + x, max(p[1] for p in vertices) + y))
    for i, a in enumerate(bounds):
        assert a[0] > 0 and a[1] > 0 and a[2] < 200 and a[3] < 200
        for b in bounds[i + 1:]:
            assert a[2] < b[0] or b[2] < a[0] or a[3] < b[1] or b[3] < a[1]
    types = '<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types"><Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/><Default Extension="model" ContentType="application/vnd.ms-package.3dmanufacturing-3dmodel+xml"/></Types>'
    rels = '<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"><Relationship Target="/3D/3dmodel.model" Id="rel0" Type="http://schemas.microsoft.com/3dmanufacturing/2013/01/3dmodel"/></Relationships>'
    path = ROOT / filename
    with ZipFile(path, "w", ZIP_DEFLATED) as archive:
        archive.writestr("[Content_Types].xml", types)
        archive.writestr("_rels/.rels", rels)
        archive.writestr("3D/3dmodel.model", ET.tostring(model, encoding="utf-8", xml_declaration=True))
    check_3mf(path, len(placements))
    return {"objects": len(placements), "bed_bounds_mm": bounds, "volumes_mm3": volumes,
            "closed_manifolds": True, "bed_placement_checked": True, "printer_settings_included": False}


def check_3mf(path, count):
    with ZipFile(path) as archive:
        assert archive.testzip() is None
        root = ET.fromstring(archive.read("3D/3dmodel.model"))
    objects = root.findall(f"{{{NS}}}resources/{{{NS}}}object")
    assert len(objects) == count and root.attrib["unit"] == "millimeter"
    for obj in objects:
        vertices = [tuple(float(v.attrib[a]) for a in ("x", "y", "z")) for v in obj.findall(f"{{{NS}}}mesh/{{{NS}}}vertices/{{{NS}}}vertex")]
        faces = [tuple(int(v.attrib[a]) for a in ("v1", "v2", "v3")) for v in obj.findall(f"{{{NS}}}mesh/{{{NS}}}triangles/{{{NS}}}triangle")]
        check_mesh(vertices, faces)


def main():
    report = json.loads((ROOT / "verification.json").read_text())
    report["3mf_checks"] = {}
    report["3mf_checks"]["00_PRINT_FIRST_Clips_PETG.3mf"] = plate("00_PRINT_FIRST_Clips_PETG.3mf", [
        ("04_snap_socket_coupon_PETG", 50, 50), ("05_snap_clip_coupon_PETG", 70, 50),
        ("06_phone_retaining_tab_coupon_PETG", 90, 50)])
    report["3mf_checks"]["Pixel_9_Pro_XL_Rev_B_PETG_Parts.3mf"] = plate("Pixel_9_Pro_XL_Rev_B_PETG_Parts.3mf", [
        ("01_back_case_PETG", 55, 100), ("02_removable_keyguard_PETG", 150, 100)])
    (ROOT / "verification.json").write_text(json.dumps(report, indent=2) + "\n")
    files = sorted(p for p in ROOT.rglob("*") if p.is_file() and "__pycache__" not in p.parts and p.name != "SHA256SUMS.txt")
    (ROOT / "SHA256SUMS.txt").write_text("\n".join(f"{sha256(p.read_bytes()).hexdigest()}  {p.relative_to(ROOT)}" for p in files) + "\n")
    path = ROOT.parent / "Pixel_9_Pro_XL_Keyguard_Rev_B_PETG.zip"
    with ZipFile(path, "w", ZIP_DEFLATED) as archive:
        for p in files + [ROOT / "SHA256SUMS.txt"]:
            archive.write(p, str(Path(ROOT.name) / p.relative_to(ROOT)))
    with ZipFile(path) as archive:
        assert archive.testzip() is None
        print(f"Checked archive: {len(archive.namelist())} files, {path.stat().st_size:,} bytes")
    print(path)


if __name__ == "__main__":
    main()
