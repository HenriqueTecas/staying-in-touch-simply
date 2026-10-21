"""Inspect the serialized binary STL files, independently of the CAD solids."""
from collections import Counter
import json
from pathlib import Path
import struct
import numpy as np

ROOT = Path(__file__).resolve().parents[1]
report = json.loads((ROOT / "verification.json").read_text())
report["serialized_stl_checks"] = {}
for path in sorted((ROOT / "stl").glob("*.stl")):
    data = path.read_bytes()
    count = struct.unpack_from("<I", data, 80)[0]
    assert len(data) == 84 + count * 50, path.name
    edges, vertices, volume = Counter(), [], 0.0
    for index in range(count):
        record = struct.unpack_from("<12fH", data, 84 + index * 50)
        points = [tuple(round(value, 5) for value in record[offset:offset + 3]) for offset in (3, 6, 9)]
        vertices.extend(points)
        a, b, c = np.array(points)
        volume += np.dot(a, np.cross(b, c)) / 6
        for edge in ((points[0], points[1]), (points[1], points[2]), (points[2], points[0])):
            edges[tuple(sorted(edge))] += 1
    mesh = np.array(vertices)
    bad_edges = sum(value != 2 for value in edges.values())
    relative_error = abs(volume - report["parts"][path.stem]["volume_mm3"]) / volume
    assert bad_edges == 0 and volume > 0 and relative_error < .002, path.name
    assert abs(mesh[:, 2].min()) < .00002, (path.name, mesh[:, 2].min())
    result = {"triangles": count, "non_manifold_edges": bad_edges,
              "positive_signed_volume_mm3": round(volume, 3),
              "volume_error_vs_CAD_percent": round(relative_error * 100, 5),
              "on_print_bed_z0": True}
    report["serialized_stl_checks"][path.name] = result
    print(path.name, result)
(ROOT / "verification.json").write_text(json.dumps(report, indent=2) + "\n")
