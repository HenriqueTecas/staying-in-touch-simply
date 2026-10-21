PIXEL 9 PRO XL — REMOVABLE KEYGUARD, REV B, PETG
2026-10-21 | All dimensions in millimetres

WHAT CHANGED AFTER YOUR FIT REPORT
You reported that Perimeter_R11.3_C0.30_PETG_FIT_TEST_ONLY.stl fits your bare
phone without forcing it. This revision uses that same cavity outline:
77.2 x 163.4, with an 11.6 mm modeled inner corner radius (11.3 + 0.30 clearance).
This records the successful gauge profile, not a measurement of the actual
phone's corner radius, thickness, or camera bar.

Both main parts are now intended for PETG. The back case has an open mouth
and FOUR separately flexible phone-retaining tabs, replacing the continuous
lip that previously required TPU flexibility. Slots with rounded ends and a
tapered root isolate the fingers. Nominal lip overlap is 0.40 mm per side.
The removable front has its own four snap fingers with rounded inside roots.
It retains three open holes: 18 / 24 / 18 mm, on 25 mm centres, 20 mm above
the physical phone bottom. The two clip systems occupy different positions.

PRINT ORDER
1. Open 00_PRINT_FIRST_Clips_PETG.3mf. This has three small pieces already laid
   out: one socket and one finger for the removable front, plus one phone-
   retaining tab cut directly from the final back case. Total CAD volume is
   approximately 1.26 cubic centimetres, before slicer additions.
2. Check that the two panel pieces click together and can be released. Check
   that the phone-retaining finger springs back after a small outward bend.
   You can gently compare its straight channel with the phone edge, with the
   little flat flange behind the back of the phone. Keep clear of buttons.
3. Print 01_back_case_PETG.stl and check full-depth fit, retention, camera and
   hardware access before printing the front. A perimeter ring cannot check
   those. All four phone tabs must release outward for removal; avoid levering
   against glass. If fitting needs significant force or stresses a tab, stop
   and report the location so the fit/retaining geometry can be adjusted.
4. Print 02_removable_keyguard_PETG.stl, face down as supplied. Seat its four
   hooks into the case's lower side pockets. To remove it, flex the exposed
   panel fingers outward gently, release one side, then the other.

The parts are also together in Pixel_9_Pro_XL_Rev_B_PETG_Parts.3mf, positioned
separately on a plate. That is a standard model-only 3MF, not a printer preset
or assembled model. Use the STEP assembly or browser viewer for installed
positions. The 3MF files contain no G-code and do not start a print.

SLICER START POINT
PETG, 100% scale, 0.20 mm layers, 0.4 mm nozzle, 3 walls, 20% infill.
Choose your own printer and actual filament profile. STL parts already rest
at Z=0. The back case prints on its flat rear face; the front prints face down.
Try supports off and inspect the lip/clip layers in the slicer: the long port
windows are open at the front, so they have no bridge ceilings. Small clip lip
overhangs may need tuning in your PETG. No slicing or printer settings have
been validated in Bambu Studio during this run. Physical print results govern.

The 3MF plates use a footprint smaller than 200 x 200 mm, with separated
objects on the bed. Bambu Studio may ask whether to load geometry without a
project profile: import as models, keep 100% scale, and use your printer profile.

OTHER FILES
preview/Pixel_9_Pro_XL_Keyguard_Preview.png — views rendered from actual CAD
preview/Open_3D_Preview.html — self-contained offline 3D viewer
step/Pixel_9_Pro_XL_two_part_assembly.step — two installed CAD parts, no phone
stl/04_snap_socket_coupon_PETG.stl — removable-panel socket
stl/05_snap_clip_coupon_PETG.stl — removable-panel finger
stl/06_phone_retaining_tab_coupon_PETG.stl — phone-retaining finger and channel
parameters.json and source/ — editable CadQuery source and packaging tools
verification.json — actual CAD, serialized STL and 3MF checks

WHAT IS VERIFIED
- Your qualitative report that the named perimeter ring fits.
- Five valid single CAD solids and closed manifold STL files on the print bed.
- Main STEP solids re-import correctly with matching volumes.
- No installed overlap of back/front with each other or with the nominal phone.
- Three nominal-diameter access cylinders clear both case parts above glass.
- Sampled straight insertion intersects only the intended retaining lips;
  the other case surfaces clear the nominal phone. This does not simulate
  actual spring bending or establish insertion force.
- Standard 3MF objects are closed meshes, separated, and on the bed.

STILL TO CHECK PHYSICALLY
The 8.5 mm body depth is the published nominal value. A screen protector was
not measured. Phone-retaining lips start 0.6 mm above nominal glass; the panel
underside is 2.1 mm above it and the panel face is 3.5 mm above it. A thicker
protector or print variation can reduce those gaps.

The rear camera window remains a generous provisional 74 x 36 mm opening.
Button, microphone and bottom connector windows are deliberately broad; their
exact positions and clearances are NOT established by the ring test. Clip
force, fatigue, drop protection and camera-lens protection are unvalidated.

The front panel covers the lower 39 mm of the display except for its holes.
Your Android interface needs to place controls underneath those holes. The
coloured controls in the preview are illustrative screen graphics, not physical
button caps or an installed Android app. Normal gesture navigation can be
obstructed with the front fitted; remove the front for unrestricted access.

REBUILD
Use cadquery==2.6.1 with numpy and Pillow (source/requirements.txt).
Run source/build.py, source/check_exports.py, source/presentation.py,
source/render_cpu.py and source/package.py, in that order.

BODY SPECIFICATION SOURCE
https://support.google.com/pixelphone/answer/7158570?hl=en
No factory mechanical CAD or downloaded community SCAD was used in this
revision. The actual tested ring profile and your feedback are its fit basis.
Viewer library: Three.js r160, MIT, preview/THREE-LICENSE.txt.
