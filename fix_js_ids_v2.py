import re

with open('temp_script.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Fix userAvatar
js = js.replace("document.getElementById('userAvatar').textContent", "if(document.getElementById('userAvatar')) document.getElementById('userAvatar').textContent")

# 2. Fix gpsBtn -> autoLocationBtn
js = js.replace("document.getElementById('gpsBtn').addEventListener", "if(document.getElementById('autoLocationBtn')) document.getElementById('autoLocationBtn').addEventListener")
js = js.replace("'gpsBtn'", "'autoLocationBtn'")

# 3. Fix defaultBtn -> manualLocationBtn
js = js.replace("document.getElementById('defaultBtn').addEventListener", "if(document.getElementById('manualLocationBtn')) document.getElementById('manualLocationBtn').addEventListener")
js = js.replace("'defaultBtn'", "'manualLocationBtn'")

# 4. Fix requestForm -> incidentForm
js = js.replace("document.getElementById('requestForm').addEventListener", "if(document.getElementById('incidentForm')) document.getElementById('incidentForm').addEventListener")
js = js.replace("document.getElementById('requestForm').reset()", "if(document.getElementById('incidentForm')) document.getElementById('incidentForm').reset()")
js = js.replace("'requestForm'", "'incidentForm'")

# 5. Fix sosBtn -> mainSosBtn
js = js.replace("document.getElementById('sosBtn').addEventListener", "if(document.getElementById('mainSosBtn')) document.getElementById('mainSosBtn').addEventListener")
js = js.replace("'sosBtn'", "'mainSosBtn'")
js = js.replace("'requestBtn'", "'mainSosBtn'")

# 6. Fix lat, lng -> latitude, longitude
js = js.replace("document.getElementById('lat').value", "document.getElementById('latitude').value")
js = js.replace("document.getElementById('lng').value", "document.getElementById('longitude').value")
js = js.replace("'lat'", "'latitude'")
js = js.replace("'lng'", "'longitude'")

# 7. Fix locationInput -> locationDesc
js = js.replace("document.getElementById('locationInput').value", "document.getElementById('locationDesc').value")
js = js.replace("'locationInput'", "'locationDesc'")

# 8. Fix condition -> description
js = js.replace("document.getElementById('condition').value", "document.getElementById('description').value")
js = js.replace("'condition'", "'description'")

# 9. Fix preferredHospital
js = js.replace("const select = document.getElementById('preferredHospital');", "const select = document.getElementById('preferredHospital');\n            if(!select) return;")

# 10. Fix mapHospitals
js = js.replace("document.getElementById('mapHospitals').textContent", "if(document.getElementById('mapHospitals')) document.getElementById('mapHospitals').textContent")

# 11. Fix mapIncidents
js = js.replace("document.getElementById('mapIncidents').textContent", "if(document.getElementById('mapIncidents')) document.getElementById('mapIncidents').textContent")

# 12. Fix noRequestOverlay
js = js.replace("document.getElementById('noRequestOverlay').classList", "if(document.getElementById('noRequestOverlay')) document.getElementById('noRequestOverlay').classList")

# 13. Fix trackingPanel
js = js.replace("document.getElementById('trackingPanel').classList", "if(document.getElementById('trackingPanel')) document.getElementById('trackingPanel').classList")

# 14. Fix hospitalListContainer
js = js.replace("const listContainer = document.getElementById('hospitalListContainer');", "const listContainer = document.getElementById('hospitalListContainer');")

# 15. Fix emergencyType assignments
js = js.replace("document.getElementById('emergencyType').value = 'Medical Emergency';", "if(document.getElementById('emergencyType')) document.getElementById('emergencyType').value = 'MEDICAL';")

# 16. Fix severityLevel assignments safely
js = js.replace("document.getElementById('severityLevel').value = 'CRITICAL';", "if(document.getElementById('severityLevel')) document.getElementById('severityLevel').value = 'CRITICAL';")

# Also, there's `triggerSOS()` in incidents_js that might be clashing with our click listener on mainSosBtn.
# Let's override triggerSOS() to just submitRequest(true)
trigger_sos = '''function triggerSOS() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        document.getElementById('latitude').value = position.coords.latitude;
                        document.getElementById('longitude').value = position.coords.longitude;
                        document.querySelector('.sos-modal').classList.add('active');
                    },
                    (err) => {
                        alert("Please enable location services for SOS");
                        document.querySelector('.sos-modal').classList.add('active');
                    }
                );
            } else {
                document.querySelector('.sos-modal').classList.add('active');
            }
        }'''

new_trigger_sos = '''function triggerSOS() {
            if (navigator.geolocation) {
                navigator.geolocation.getCurrentPosition(
                    (position) => {
                        if(document.getElementById('latitude')) document.getElementById('latitude').value = position.coords.latitude;
                        if(document.getElementById('longitude')) document.getElementById('longitude').value = position.coords.longitude;
                        submitRequest(true);
                    },
                    (error) => {
                        useDefaultLocation();
                        submitRequest(true);
                    }
                );
            } else {
                useDefaultLocation();
                submitRequest(true);
            }
        }'''
js = js.replace(trigger_sos, new_trigger_sos)

# Write the patched script
with open('temp_script_fixed.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Fixes applied successfully to temp_script_fixed.js")
