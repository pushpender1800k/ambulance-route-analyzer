import re
with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    html = f.read()
with open('temp_script.js', 'r', encoding='utf-8') as f:
    js = f.read()

ids_in_js = set(re.findall(r"getElementById\(['\"](.*?)['\"]\)", js))
for js_id in ids_in_js:
    if f'id="{js_id}"' not in html and f"id='{js_id}'" not in html:
        print(f'MISSING ID: {js_id}')
