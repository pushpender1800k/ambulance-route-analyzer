import re

def get_script(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        html = f.read()
    match = re.search(r'<script>(.*?)</script>\s*</body>', html, re.DOTALL)
    if match:
        return match.group(1)
    
    # fallback
    scripts = re.findall(r'<script>(.*?)</script>', html, re.DOTALL)
    if scripts:
        return scripts[-1]
    return ""

incidents_js = get_script('src/main/resources/templates/incidents.html')
hospitals_js = get_script('src/main/resources/templates/hospitals.html')
analytics_js = get_script('src/main/resources/templates/analytics.html')

# Clean them
incidents_js = incidents_js.replace('initMap();', '')
incidents_js = incidents_js.replace('detectLocation();', '')
incidents_js = incidents_js.replace('loadRecentIncidents();', '')

hospitals_js = hospitals_js.replace('loadHospitals();', '')

analytics_js = analytics_js.replace('loadSampleLogs();', '')
analytics_js = analytics_js.replace('updateStats();', '')

combined_js = incidents_js + '\n' + hospitals_js + '\n' + analytics_js

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    patient_html = f.read()

# We need to inject this into patient.html's script block.
# Since we just completely replaced patient_html's script block with temp_script_fixed.js in the last step,
# the script block is at the very end of patient.html
match = re.search(r'<script>(.*?)</script>\s*</body>', patient_html, re.DOTALL)
if match:
    old_script = match.group(1)
    # We shouldn't duplicate combined_js if we run this multiple times, so check
    if 'incidentMap' not in old_script:
        new_script = old_script + '\n// INJECTED COMPONENTS\n' + combined_js
        new_html = patient_html[:match.start(1)] + new_script + patient_html[match.end(1):]
        with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
            f.write(new_html)
        print("Successfully injected missing JS!")
    else:
        print("JS already seems injected.")
else:
    print("Could not find script block in patient.html")
