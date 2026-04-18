import re

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    html = f.read()

# The JS in patient.html currently has TWO 'function initMap()' definitions!
# The first one initializes 'patientMap' and uses 'map'.
# The second one initializes 'incidentMap' and uses 'map'.
# Let's split the html to find the two definitions and rename them.

# Let's find all occurrences of "function initMap()"
parts = html.split('function initMap() {')
if len(parts) == 3:
    # parts[0] is everything before the first initMap
    # parts[1] is the first initMap (patientMap)
    # parts[2] is the second initMap (incidentMap)
    
    # We rename the first one to initPatientMap
    part1 = 'function initPatientMap() {' + parts[1]
    
    # We rename the second one to initIncidentMap
    part2 = 'function initIncidentMap() {' + parts[2]
    
    html = parts[0] + part1 + part2
else:
    print("WARNING: Could not find exactly 2 initMap functions")

# Now update the calls:
# Where it says `initMap();` when tab === 'track', it should be `initPatientMap();`
# Wait, let's just replace all `initMap()` calls that are standalone with nothing? 
# Actually, the tab switching logic:
tab_switch = '''if (btn.dataset.tab === 'track') {
                    initMap();
                    checkActiveIncident();
                }'''
new_tab_switch = '''if (btn.dataset.tab === 'track') {
                    if (typeof initPatientMap === 'function') initPatientMap();
                    checkActiveIncident();
                } else if (btn.dataset.tab === 'request') {
                    if (typeof initIncidentMap === 'function') {
                        setTimeout(() => { initIncidentMap(); }, 100);
                    }
                }'''
html = html.replace(tab_switch, new_tab_switch)

# There's also an `initMap();` call inside setTimeout in submitRequest()
html = html.replace("setTimeout(() => {\n                        initMap();", "setTimeout(() => {\n                        if(typeof initPatientMap === 'function') initPatientMap();")
html = html.replace("setTimeout(() => { initMap();", "setTimeout(() => { if(typeof initPatientMap === 'function') initPatientMap();")

# Also, on page load, if requestTab is active, we should call initIncidentMap.
# We can add it at the very bottom of the script.
init_on_load = '''
        setTimeout(() => {
            if(document.getElementById('requestTab') && document.getElementById('requestTab').classList.contains('active')) {
                if(typeof initIncidentMap === 'function') initIncidentMap();
            }
        }, 500);
'''
html = html.replace('</body>', init_on_load + '\n</body>')

# In the second initMap (now initIncidentMap), it uses the global `map` variable which is overwritten!
# Let's rename the global variable for incident map to `incMap`.
# But `patientMap` also uses `map`.
# Let's do a careful replacement in part2 ONLY.
# We need to find the `let map;` or similar, but let's just do it directly.
html = html.replace("map = L.map('incidentMap')", "incMap = L.map('incidentMap')")
html = html.replace("map.on('click', function(e) {", "incMap.on('click', function(e) {")
html = html.replace("L.marker([lat, lng]).addTo(map)", "L.marker([lat, lng]).addTo(incMap)")
# Add `let incMap = null;` at the top of the second block or just globally
html = html.replace("let map = null;", "let map = null;\n        let incMap = null;")

# To ensure we catch `map.removeLayer(marker)` in the incidentMap block
# In incidents.html it's `if (marker) map.removeLayer(marker);`
html = html.replace("map.removeLayer(marker);", "if(incMap) incMap.removeLayer(marker); else if(map) map.removeLayer(marker);")
html = html.replace("marker.addTo(map);", "if(incMap) marker.addTo(incMap); else marker.addTo(map);")

# Also wait, does hospitals.js use a map? No, hospitals just has a grid.
# Does analytics.js use a map? No.

with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
    f.write(html)

print("Map fixes applied.")
