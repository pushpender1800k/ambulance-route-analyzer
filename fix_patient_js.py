import re

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Fix confirmSOS() to call submitRequest(true)
html = html.replace('onclick="confirmSOS()"', 'onclick="submitRequest(true)"')

# Fix submitRequest function to read from the new IDs
old_submit = '''async function submitRequest(isSos) {
            const lat = parseFloat(document.getElementById('lat').value);
            const lng = parseFloat(document.getElementById('lng').value);
            const condition = document.getElementById('condition').value;
            const emergencyType = document.getElementById('emergencyType').value;
            const severityLevel = document.getElementById('severityLevel').value;
            const preferredHospital = document.getElementById('preferredHospital').value;'''

new_submit = '''async function submitRequest(isSos) {
            const lat = parseFloat(document.getElementById('latitude')?.value || document.getElementById('lat')?.value) || 28.6139;
            const lng = parseFloat(document.getElementById('longitude')?.value || document.getElementById('lng')?.value) || 77.2090;
            const condition = isSos ? 'SOS TRIGGERED' : (document.getElementById('description')?.value || '');
            const emergencyType = isSos ? 'MEDICAL' : (document.getElementById('emergencyType')?.value || 'MEDICAL');
            const severityLevel = isSos ? 'CRITICAL' : (typeof selectedSeverity !== 'undefined' ? selectedSeverity : 'MODERATE');
            const preferredHospital = null;'''

html = html.replace(old_submit, new_submit)

# Remove the incidentForm default submit event listener from incidents_js
incident_submit = '''document.getElementById('incidentForm').addEventListener('submit', function(e) {
            e.preventDefault();
            const incident = {
                type: document.getElementById('emergencyType').value,
                severity: selectedSeverity,
                lat: parseFloat(document.getElementById('latitude').value),
                lng: parseFloat(document.getElementById('longitude').value),
                location: document.getElementById('locationDesc').value,
                description: document.getElementById('description').value,
                status: 'REPORTED',
                createdAt: new Date().toISOString()
            };

            fetch('/api/incidents', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(incident)
            }).then(res => {
                if (res.ok) {
                    alert('Incident reported successfully!');
                    this.reset();
                    loadRecentIncidents();
                }
            }).catch(err => {
                console.error('Error submitting incident:', err);
                addIncidentToList(incident);
                alert('Incident reported locally.');
                this.reset();
            });
        });'''

html = html.replace(incident_submit, '''document.getElementById('incidentForm').addEventListener('submit', function(e) {
            e.preventDefault();
            submitRequest(false);
        });''')

# Remove duplicate document.getElementById('vLogoutBtn')
logout_snippet = '''document.getElementById('vLogoutBtn').addEventListener('click', () => {
            document.getElementById('logoutBtn').click();
        });'''

# Count how many times it appears and only keep the first one
parts = html.split(logout_snippet)
if len(parts) > 1:
    html = parts[0] + logout_snippet + ''.join(parts[1:])


with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
    f.write(html)
