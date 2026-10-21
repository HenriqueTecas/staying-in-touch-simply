"""Build the two-piece passive Pixel keyguard with CadQuery 2.6.1; millimetres."""
import json
from collections import Counter
from pathlib import Path
import cadquery as cq

ROOT = Path(__file__).resolve().parents[1]
P = json.loads((ROOT / "parameters.json").read_text())
PHONE, CASE, GUARD = P["phone"], P["back_case"], P["keyguard"]


def block(width, height, depth, center):
    return cq.Workplane("XY").box(width, height, depth).val().translate(center)


def rounded(width, height, radius, z, depth, xy=(0, 0)):
    return (cq.Workplane("XY").sketch().rect(width, height)
            .vertices().fillet(radius).finalize().extrude(depth).val()
            .translate((xy[0], xy[1], z)))


def cylinder(radius, depth, center):
    return cq.Solid.makeCylinder(radius, depth, cq.Vector(*center))


def dimensions():
    clearance, wall = CASE["side_clearance"], CASE["wall"]
    return {
        "inner_w": PHONE["width"] + 2 * clearance,
        "inner_h": PHONE["height"] + 2 * clearance,
        "outer_w": PHONE["width"] + 2 * (clearance + wall),
        "outer_h": PHONE["height"] + 2 * (clearance + wall),
        "inner_r": PHONE["corner_radius_assumed"] + clearance,
        "outer_r": PHONE["corner_radius_assumed"] + clearance + wall,
    }


D = dimensions()


def back_case():
    shell = rounded(D["outer_w"], D["outer_h"], D["outer_r"],
                    -CASE["back_thickness"], CASE["front_z"] + CASE["back_thickness"])
    # The opening matches the ring tested on the phone. A continuous TPU-style
    # lip is replaced by four independently deflecting PETG retaining fingers.
    cavity = rounded(D["inner_w"], D["inner_h"], D["inner_r"], 0, 16)
    shell = add_phone_retention(shell.cut(cavity))
    camera = rounded(CASE["camera_window_width_assumed"], CASE["camera_window_height_assumed"],
                     CASE["camera_window_radius"], -5, 6, (0, CASE["camera_center_y_assumed"]))
    shell = shell.cut(camera)
    return case_access(shell).clean()


def case_access(shell):
    # Broad provisional windows avoid pretending that port locations are measured.
    # U-shaped access windows have no long bridge ceilings to support/remove.
    bottom = block(CASE["bottom_access_width"], 12, 14,
                   (0, -D["outer_h"] / 2, 8.0))
    y0, y1 = CASE["right_control_window_y_min_assumed"], CASE["right_control_window_y_max_assumed"]
    side = block(10, y1 - y0, 14, (D["outer_w"] / 2, (y0 + y1) / 2, 8.5))
    top_mic = block(20, 12, 14, (0, D["outer_h"] / 2, 8.5))
    earpiece = block(26, 10, 6, (0, PHONE["height"] / 2 + 1, 10))
    shell = shell.cut(bottom).cut(side).cut(top_mic).cut(earpiece)
    for sign in (-1, 1):
        for y in GUARD["clip_y"]:
            depth = GUARD["socket_depth"]
            pocket = block(depth + 2, GUARD["clip_width"] + 2 * GUARD["socket_y_clearance"],
                           GUARD["socket_top_z"] - GUARD["socket_bottom_z"],
                           (sign * (D["outer_w"] / 2 - depth + (depth + 2) / 2), y,
                            (GUARD["socket_top_z"] + GUARD["socket_bottom_z"]) / 2))
            shell = shell.cut(pocket)
    return shell


def xz_prism(points, y, width, sign=1):
    shape = (cq.Workplane("XZ").polyline(points).close().extrude(width).val()
             .translate((0, y + width / 2, 0)))
    return shape if sign == 1 else shape.mirror("YZ")


def phone_retention_lip(y, sign):
    r = CASE["retention"]
    x = D["inner_w"] / 2
    points = [(x + .20, r["lip_underside_z"]),
              (x - r["nose_inward_from_cavity"], r["lip_underside_z"]),
              (x - r["nose_inward_from_cavity"], r["nose_tip_z"]),
              (x, r["top_z"]), (x + .20, r["top_z"])]
    return xz_prism(points, y, r["clip_width"], sign)


def add_phone_retention(shell):
    r = CASE["retention"]
    x = D["inner_w"] / 2
    for sign in (-1, 1):
        for y in r["clip_y"]:
            for direction in (-1, 1):
                slot_y = y + direction * (r["clip_width"] + r["slot_width"]) / 2
                slot = block(6, r["slot_width"], 15, (sign * x, slot_y, r["root_z"] + 7.5))
                # A round slot end avoids a sharp notch at the spring root.
                end = cq.Solid.makeCylinder(r["slot_width"] / 2, 6,
                                           cq.Vector(sign * x - 3, slot_y, r["root_z"]), cq.Vector(1, 0, 0))
                shell = shell.cut(slot).cut(end)
            points = [(x + CASE["wall"], r["root_z"]),
                      (x + r["clip_thickness"], r["root_z"] + r["root_taper_height"]),
                      (x + r["clip_thickness"], 15), (x + CASE["wall"] + 3, 15),
                      (x + CASE["wall"] + 3, r["root_z"])]
            shell = shell.cut(xz_prism(points, y, r["clip_width"], sign))
            shell = shell.fuse(phone_retention_lip(y, sign))
    return shell.clean()


def hook(sign, y):
    x = D["outer_w"] / 2 + GUARD["clip_side_clearance"]
    points = [(x + .15, GUARD["clip_bottom_z"]),
              (x - GUARD["hook_projection"], GUARD["hook_tip_z"]),
              (x + .15, GUARD["hook_upper_z"])]
    # XZ's positive extrusion points toward -Y; centering keeps both clips identical.
    shape = (cq.Workplane("XZ").polyline(points).close()
             .extrude(GUARD["clip_width"]).val().translate((0, y + GUARD["clip_width"] / 2, 0)))
    return shape if sign == 1 else shape.mirror("YZ")


def keyguard():
    offset, z = GUARD["outer_offset_from_case"], GUARD["plate_bottom_z"]
    thickness = GUARD["plate_thickness"]
    outline = rounded(D["outer_w"] + 2 * offset, D["outer_h"] + 2 * offset,
                      D["outer_r"] + offset, z, thickness)
    top_y = -PHONE["height"] / 2 + GUARD["height_from_phone_bottom"]
    plate = outline.intersect(block(110, 110, 5, (0, top_y - 55, z + 1)))
    for x, diameter in zip(GUARD["hole_x"], GUARD["hole_diameters"]):
        y = -PHONE["height"] / 2 + GUARD["hole_row_from_phone_bottom"]
        plate = plate.cut(cylinder(diameter / 2, 8, (x, y, z - 2)))
        flare = cq.Solid.makeCone(diameter / 2, diameter / 2 + GUARD["hole_flare_radial"],
                                 GUARD["hole_flare_depth"],
                                 cq.Vector(x, y, z + thickness - GUARD["hole_flare_depth"]))
        plate = plate.cut(flare)
    return add_clips(plate).clean()


def add_clips(plate):
    x = D["outer_w"] / 2 + GUARD["clip_side_clearance"] + GUARD["clip_thickness"] / 2
    bottom, top = GUARD["clip_bottom_z"], GUARD["plate_bottom_z"] + .25
    for sign in (-1, 1):
        for y in GUARD["clip_y"]:
            finger = block(GUARD["clip_thickness"], GUARD["clip_width"], top - bottom,
                           (sign * x, y, (top + bottom) / 2))
            plate = plate.fuse(finger).fuse(hook(sign, y))
    # Small internal root rounds remain above/outside the back case rim.
    root_x = D["outer_w"] / 2 + GUARD["clip_side_clearance"]
    roots = (cq.Workplane("XY").newObject([plate]).edges().filter(
        lambda e: e.geomType() == "LINE" and abs(abs(e.Center().x) - root_x) < 1e-5
        and abs(e.Center().z - GUARD["plate_bottom_z"]) < 1e-5
        and abs(e.Length() - GUARD["clip_width"]) < 1e-5))
    assert len(roots.vals()) == 4, "All four panel clip roots must be rounded"
    return roots.fillet(.25).val()


def reference_parts():
    body = rounded(PHONE["width"], PHONE["height"], PHONE["corner_radius_assumed"], 0, PHONE["thickness"])
    screen = rounded(P["display_reference"]["active_width_assumed"],
                     P["display_reference"]["active_height_assumed"], 9, PHONE["thickness"], .025)
    camera = rounded(68, 26, 12.9, -4, 4.05, (0, CASE["camera_center_y_assumed"]))
    return {"phone_reference": (body, "#747c84"), "screen_reference": (screen, "#d9e7ed"),
            "camera_reference": (camera, "#222730")}


def mesh_record(name, shape, color):
    vertices, faces = shape.tessellate(.055, .12)
    return {"name": name, "color": color, "vertices": [v.toTuple() for v in vertices],
            "triangles": faces}


def verify_mesh(shape):
    vertices, faces = shape.tessellate(.035, .10)
    # OCC duplicates vertices between faces; weld by position before checking the skin.
    welded, indices = {}, []
    for vertex in vertices:
        point = tuple(round(value, 5) for value in vertex.toTuple())
        indices.append(welded.setdefault(point, len(welded)))
    edges = Counter()
    for face in faces:
        a, b, c = [indices[index] for index in face]
        for edge in ((a, b), (b, c), (c, a)):
            edges[tuple(sorted(edge))] += 1
    return {"triangles": len(faces), "non_manifold_edges": sum(n != 2 for n in edges.values())}


def check_parts(parts, back, front, phone):
    report = {"status": P["status"], "parts": {}, "interfaces": {}}
    for name, shape in parts.items():
        bounds = shape.BoundingBox()
        record = {"valid": shape.isValid(), "solids": len(shape.Solids()),
                  "volume_mm3": round(shape.Volume(), 3),
                  "bounds_mm": [round(v, 3) for v in (bounds.xlen, bounds.ylen, bounds.zlen)],
                  **verify_mesh(shape)}
        assert record["valid"] and record["solids"] == 1, (name, record)
        assert record["non_manifold_edges"] == 0, (name, record)
        report["parts"][name] = record
    interfaces = report["interfaces"]
    interfaces["back_to_keyguard_overlap_mm3"] = back.intersect(front).Volume()
    interfaces["back_to_nominal_phone_overlap_mm3"] = back.intersect(phone).Volume()
    interfaces["keyguard_to_nominal_phone_overlap_mm3"] = front.intersect(phone).Volume()
    assert all(abs(value) < .00001 for value in interfaces.values()), interfaces
    interfaces["screen_to_keyguard_face_mm"] = GUARD["plate_bottom_z"] + GUARD["plate_thickness"] - PHONE["thickness"]
    interfaces["keyguard_to_back_rim_gap_mm"] = GUARD["plate_bottom_z"] - CASE["front_z"]
    interfaces["nominal_hook_engagement_mm"] = GUARD["hook_projection"] - GUARD["clip_side_clearance"]
    interfaces["screen_holes_clear"] = []
    for x, diameter in zip(GUARD["hole_x"], GUARD["hole_diameters"]):
        y = -PHONE["height"] / 2 + GUARD["hole_row_from_phone_bottom"]
        probe = cylinder(diameter / 2 - .01, 5, (x, y, PHONE["thickness"] + .01))
        overlap = probe.intersect(back).Volume() + probe.intersect(front).Volume()
        assert overlap < .00001, (x, overlap)
        interfaces["screen_holes_clear"].append(True)
    report["fit_evidence"] = P["fit_evidence"]
    report["unverified"] = ["Full-depth fit, camera/bar and access windows", "Both clip systems: force, retention and fatigue",
                            "Screen protector clearance", "Android UI button alignment and system gestures",
                            "Print material shrinkage and support removal", "Lens protection and drop performance"]
    return report


def check_phone_retention(back, report):
    r = CASE["retention"]
    x = D["inner_w"] / 2 - r["nose_inward_from_cavity"]
    bearing_overlap = PHONE["width"] / 2 - x
    assert 0 < bearing_overlap < .6
    assert r["lip_underside_z"] - PHONE["thickness"] >= .5
    # Rigidly translating the phone through the mouth intentionally intersects
    # only the spring lips. The report distinguishes this from installed fit.
    lips = cq.Compound.makeCompound([phone_retention_lip(y, sign)
                                   for sign in (-1, 1) for y in r["clip_y"]])
    proxy = reference_parts()["phone_reference"][0]
    samples = []
    for lift in (0, .3, .6, 1, 2, 5, 9, 12):
        body = proxy.translate((0, 0, lift))
        overlap = body.intersect(back).Volume()
        outside_lips = body.intersect(back.cut(lips)).Volume()
        assert outside_lips < .00001, (lift, outside_lips)
        samples.append({"phone_lift_mm": lift, "lip_intersection_mm3": round(overlap, 6),
                        "nonlip_intersection_mm3": round(outside_lips, 6)})
    report["phone_retention"] = {"tabs": 4, "nominal_overlap_per_side_mm": bearing_overlap,
        "glass_to_lip_underside_mm": r["lip_underside_z"] - PHONE["thickness"],
        "nominal_required_tab_deflection_mm": bearing_overlap,
        "mouth_test": "Undeformed lips intentionally interfere during insertion; no other solid intersects sampled insertion poses",
        "insertion_samples": samples, "force_and_strength_validated": False}


def print_orientation(shape, front=False):
    result = shape.rotate((0, 0, 0), (0, 1, 0), 180) if front else shape
    # OCC's cached bounding box includes a meshing margin. Use actual exported
    # triangle coordinates so every STL rests precisely on the print bed.
    vertices, _ = result.tessellate(.035, .10)
    points = [vertex.toTuple() for vertex in vertices]
    low = [min(point[axis] for point in points) for axis in range(3)]
    high = [max(point[axis] for point in points) for axis in range(3)]
    return result.translate((-(low[0] + high[0]) / 2,
                             -(low[1] + high[1]) / 2, -low[2]))


def main():
    back, front = back_case(), keyguard()
    coupon_box = block(12, 14, 25, (39, GUARD["clip_y"][1], 5))
    phone_clip_box = block(13, 18, 25, (39, CASE["retention"]["clip_y"][0], 5))
    parts = {"01_back_case_PETG": back, "02_removable_keyguard_PETG": front,
             "04_snap_socket_coupon_PETG": back.intersect(coupon_box).clean(),
             "05_snap_clip_coupon_PETG": front.intersect(coupon_box).clean()}
    parts["06_phone_retaining_tab_coupon_PETG"] = back.intersect(phone_clip_box).clean()
    refs = reference_parts()
    report = check_parts(parts, back, front, refs["phone_reference"][0])
    check_phone_retention(back, report)
    assembly = cq.Assembly(name="Pixel_9_Pro_XL_keyguard_Rev_B_PETG")
    for name, shape in list(parts.items())[:2]:
        cq.exporters.export(shape, str(ROOT / "step" / f"{name}.step"))
        assembly.add(shape, name=name, color=cq.Color(.12, .14, .16) if name.startswith("01") else cq.Color(.2, .23, .27))
    assembly.save(str(ROOT / "step/Pixel_9_Pro_XL_two_part_assembly.step"))
    for name, shape in parts.items():
        printable = print_orientation(shape, front=name.startswith(("02", "05")))
        path = ROOT / "stl" / f"{name}.stl"
        cq.exporters.export(printable, str(path), tolerance=.035, angularTolerance=.10)
        imported = cq.importers.importStep(str(ROOT / "step" / f"{name}.step")) if name.startswith(("01", "02")) else None
        if imported:
            assert imported.val().isValid()
            assert abs(imported.val().Volume() - shape.Volume()) < .001
    records = [mesh_record("back_case", back, "#22272d"), mesh_record("keyguard", front, "#343c45")]
    records += [mesh_record(name, shape, color) for name, (shape, color) in refs.items()]
    (ROOT / "preview/mesh.json").write_text(json.dumps({"parameters": P, "parts": records}, separators=(",", ":")))
    (ROOT / "verification.json").write_text(json.dumps(report, indent=2) + "\n")
    print(json.dumps(report, indent=2))


if __name__ == "__main__":
    main()
