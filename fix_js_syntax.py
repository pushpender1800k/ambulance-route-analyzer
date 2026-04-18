import re

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    patient_html = f.read()

with open('final_test.js', 'r', encoding='utf-8') as f:
    js = f.read()

parts = js.split('let map;')
if len(parts) > 1:
    # Only replace occurrences after the first one
    js = parts[0] + 'let map;' + 'let incMap;'.join(parts[1:])

js = js.replace("map = L.map('incidentMap')", "incMap = L.map('incidentMap')")
js = js.replace("addTo(map)", "addTo(incMap)")
js = js.replace("map.on(", "incMap.on(")
js = js.replace("map.removeLayer", "incMap.removeLayer")

matches = list(re.finditer(r'<script>(.*?)</script>', patient_html, re.DOTALL))
longest_match = max(matches, key=lambda m: len(m.group(1)))
new_html = patient_html[:longest_match.start(1)] + js + patient_html[longest_match.end(1):]

with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
    f.write(new_html)
