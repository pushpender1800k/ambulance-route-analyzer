import re

def get_script(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        html = f.read()
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

# We need to make sure we don't redefine function initMap()
incidents_js = incidents_js.replace('function initMap() {', 'function initIncidentMap() {')

combined_js = incidents_js + '\n' + hospitals_js + '\n' + analytics_js

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    patient_html = f.read()

# Find the largest script block in patient.html and append to it
matches = list(re.finditer(r'<script>(.*?)</script>', patient_html, re.DOTALL))
if matches:
    # the longest script block is our main logic
    longest_match = max(matches, key=lambda m: len(m.group(1)))
    old_script = longest_match.group(1)
    
    if 'incidentMap' not in old_script:
        new_script = old_script + '\n// INJECTED COMPONENTS\n' + combined_js
        new_html = patient_html[:longest_match.start(1)] + new_script + patient_html[longest_match.end(1):]
        with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
            f.write(new_html)
        print("Successfully injected missing JS!")
    else:
        print("JS already seems injected.")
else:
    print("Could not find script blocks in patient.html")
