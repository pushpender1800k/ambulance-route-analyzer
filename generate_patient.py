import re

with open('src/main/resources/templates/patient.html', 'r', encoding='utf-8') as f:
    patient_html = f.read()

js_match = re.search(r'<script>(.*?)</script>\s*</body>', patient_html, re.DOTALL)
if js_match:
    patient_js = js_match.group(1)
else:
    patient_js = ''

new_html = f'''<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>RESQ — Patient Portal</title>
    <link rel="preconnect" href="https://fonts.googleapis.com">
    <link href="https://fonts.googleapis.com/css2?family=Orbitron:wght@400;500;600;700;800;900&family=Rajdhani:wght@300;400;500;600;700&family=Share+Tech+Mono&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
    <script src="https://cdnjs.cloudflare.com/ajax/libs/sockjs-client/1.5.1/sockjs.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/stomp.js/2.3.3/stomp.min.js"></script>
    <link rel="stylesheet" href="/css/dashboard.css">
    <style>
        .tab-content {{ display: none; }}
        .tab-content.active {{ display: block; height: 100%; }}
        .leaflet-popup-tip {{ background: rgba(10,15,20,0.95); }}
        .popup-title {{ font-family: 'Orbitron', sans-serif; font-size: 14px; color: #00f0ff; margin-bottom: 5px; }}
        .popup-info {{ font-size: 12px; color: #888; }}
        .popup-beds {{ color: #00ff88; font-weight: 600; }}
        .connection-status {{ position: fixed; bottom: 20px; right: 20px; padding: 8px 15px; border-radius: 20px; font-size: 12px; font-weight: 600; z-index: 10000; }}
        .connection-status.connected {{ background: rgba(0,255,136,0.2); color: #00ff88; border: 1px solid rgba(0,255,136,0.5); }}
        .connection-status.disconnected {{ background: rgba(255,68,68,0.2); color: #ff4444; border: 1px solid rgba(255,68,68,0.5); }}
        @keyframes pulse {{ 0%, 100% {{ transform: scale(1); opacity: 1; }} 50% {{ transform: scale(1.1); opacity: 0.8; }} }}
        .ambulance-marker-icon {{ font-size: 30px; text-shadow: 0 0 10px rgba(0,240,255,0.5); }}
        .hidden {{ display: none !important; }}
        
        /* Map Panel from Command Center */
        .dashboard-content {{ height: calc(100vh - 80px); padding: 20px; }}
        .map-section {{ width: 100%; height: 100%; border: 1px solid rgba(0,240,255,0.2); border-radius: 12px; position: relative; overflow: hidden; }}
        #patientMap {{ width: 100%; height: 100%; }}
        
        .status-timeline {{ position: absolute; bottom: 20px; left: 50%; transform: translateX(-50%); background: rgba(10,15,20,0.95); border: 1px solid rgba(0,240,255,0.3); border-radius: 12px; padding: 20px; display: flex; gap: 40px; z-index: 1000; backdrop-filter: blur(10px); }}
        .timeline-step {{ display: flex; flex-direction: column; align-items: center; position: relative; z-index: 1; opacity: 0.5; transition: all 0.3s; }}
        .timeline-step.active {{ opacity: 1; }}
        .timeline-step.active .step-icon {{ background: rgba(0,240,255,0.2); color: #00f0ff; border-color: #00f0ff; box-shadow: 0 0 15px rgba(0,240,255,0.5); }}
        .timeline-step.completed .step-icon {{ background: rgba(0,255,136,0.2); color: #00ff88; border-color: #00ff88; }}
        .step-icon {{ width: 40px; height: 40px; border-radius: 50%; background: rgba(0,0,0,0.5); border: 2px solid #444; display: flex; align-items: center; justify-content: center; font-size: 16px; margin-bottom: 8px; transition: all 0.3s; }}
        .step-label {{ font-family: 'Orbitron', sans-serif; font-size: 11px; color: #fff; text-transform: uppercase; letter-spacing: 1px; }}
        
        .incident-details-card {{ position: absolute; top: 20px; right: 20px; background: rgba(10,15,20,0.95); border: 1px solid rgba(0,240,255,0.3); border-radius: 12px; padding: 20px; width: 300px; z-index: 1000; backdrop-filter: blur(10px); }}
        .detail-row {{ display: flex; justify-content: space-between; margin-bottom: 15px; padding-bottom: 15px; border-bottom: 1px solid rgba(255,255,255,0.1); }}
        .detail-row:last-child {{ border-bottom: none; margin-bottom: 0; padding-bottom: 0; }}
        .detail-label {{ font-family: 'Rajdhani', sans-serif; font-size: 12px; color: #888; text-transform: uppercase; }}
        .detail-value {{ font-family: 'Orbitron', sans-serif; font-size: 14px; color: #00f0ff; text-align: right; }}
    </style>
</head>
<body>
    <div class="app-wrapper">
        <nav class="vertical-nav">
            <div class="vnav-brand"><div class="vbrand-icon">R</div></div>
            <div class="vnav-items">
                <a href="#" class="vnav-btn" data-tab="track" title="Command Map"><span class="vnav-icon">🗺️</span></a>
                <a href="#" class="vnav-btn" data-tab="hospitalsTab" title="Hospitals"><span class="vnav-icon">🏥</span></a>
                <a href="#" class="vnav-btn active" data-tab="request" title="Incidents"><span class="vnav-icon">🔴</span></a>
                <a href="#" class="vnav-btn" data-tab="profile" title="Analytics"><span class="vnav-icon">📊</span></a>
            </div>
            <div class="vnav-bottom">
                <button class="vnav-btn logout-vbtn" id="vLogoutBtn" title="Logout"><span class="vnav-icon">⏻</span></button>
            </div>
        </nav>

        <div class="main-content">
            <nav class="top-nav">
                <div class="nav-left">
                    <div class="nav-brand">
                        <div class="brand-icon">R</div>
                        <span class="brand-name">RESQ</span>
                    </div>
                    <div class="nav-divider"></div>
                    <span class="nav-subtitle">PATIENT PORTAL</span>
                </div>
                <div class="nav-center">
                    <div class="nav-stat">
                        <span class="ns-value" id="activeIncidentsTop">0</span>
                        <span class="ns-label">ACTIVE SOS</span>
                    </div>
                </div>
                <div class="nav-right">
                    <div class="nav-clock" id="navClock">00:00:00</div>
                    <div class="nav-user">
                        <span class="user-role">PATIENT</span>
                        <span class="user-name" id="userName">patient</span>
                    </div>
                    <div class="nav-avatar">P</div>
                    <button class="logout-btn" id="logoutBtn">⏻ LOGOUT</button>
                </div>
            </nav>

            <div id="connectionStatus" class="connection-status disconnected">⚡ WebSocket: Connecting...</div>

            <div id="trackTab" class="tab-content">
                <div class="dashboard-content">
                    <div class="map-section" id="trackingPanel">
                        <div id="patientMap"></div>
                        <div class="incident-details-card">
                            <div class="detail-row">
                                <div class="detail-label">Ambulance</div>
                                <div class="detail-value" id="ambCode">--</div>
                            </div>
                            <div class="detail-row">
                                <div class="detail-label">Status</div>
                                <div class="detail-value" id="ambDriver" style="color: #00ff88;">Waiting for dispatch...</div>
                            </div>
                            <div class="detail-row">
                                <div class="detail-label">ETA</div>
                                <div class="detail-value" style="color: #ffaa00;"><span id="etaValue">--</span> min</div>
                            </div>
                        </div>
                        <div class="status-timeline">
                            <div class="timeline-step" id="step-requested">
                                <div class="step-icon">🚨</div>
                                <div class="step-label">Requested</div>
                            </div>
                            <div class="timeline-step" id="step-dispatched">
                                <div class="step-icon">📡</div>
                                <div class="step-label">Dispatched</div>
                            </div>
                            <div class="timeline-step" id="step-en-route">
                                <div class="step-icon">🚑</div>
                                <div class="step-label">En Route</div>
                            </div>
                            <div class="timeline-step" id="step-arrived">
                                <div class="step-icon">📍</div>
                                <div class="step-label">Arrived</div>
                            </div>
                            <div class="timeline-step" id="step-transit">
                                <div class="step-icon">🏥</div>
                                <div class="step-label">To Hospital</div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <!-- WILL INJECT INCIDENTS / HOSPITALS / ANALYTICS HTML HERE -->
            INJECT_TABS

        </div>
    </div>
    
    <script>
        {patient_js}

        // Add clock logic
        function updateNavClock() {{
            const now = new Date();
            const clockEl = document.getElementById('navClock');
            if(clockEl) clockEl.textContent = now.toLocaleTimeString('en-US', {{hour12: false}});
        }}
        setInterval(updateNavClock, 1000);
        updateNavClock();

        // Fix tab navigation
        document.querySelectorAll('.vnav-btn[data-tab]').forEach(btn => {{
            btn.addEventListener('click', (e) => {{
                e.preventDefault();
                document.querySelectorAll('.vnav-btn[data-tab]').forEach(b => b.classList.remove('active'));
                document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
                
                btn.classList.add('active');
                document.getElementById(btn.dataset.tab + 'Tab').classList.add('active');

                if (btn.dataset.tab === 'track') {{
                    initMap();
                    checkActiveIncident();
                }}
            }});
        }});
        
        document.getElementById('vLogoutBtn').addEventListener('click', () => {{
            document.getElementById('logoutBtn').click();
        }});
    </script>
</body>
</html>
'''

# Now extract the content of incidents.html
with open('src/main/resources/templates/incidents.html', 'r', encoding='utf-8') as f:
    incidents_html = f.read()
    incidents_style = re.search(r'<style>(.*?)</style>', incidents_html, re.DOTALL).group(1)
    incidents_content = re.search(r'<div class="incidents-container">(.*?)</div>\s*</div>\s*</div>\s*<div class="sos-modal"', incidents_html, re.DOTALL).group(1)
    incidents_modal = re.search(r'(<div class="sos-modal".*?</div>\s*</div>)', incidents_html, re.DOTALL).group(1)

with open('src/main/resources/templates/hospitals.html', 'r', encoding='utf-8') as f:
    hospitals_html = f.read()
    hospitals_style = re.search(r'<style>(.*?)</style>', hospitals_html, re.DOTALL).group(1)
    hospitals_content = re.search(r'<div class="hospitals-container">(.*?)</div>\s*</div>\s*</div>\s*<!-- Hospital Detail Modal -->', hospitals_html, re.DOTALL).group(1)
    hospitals_modal = re.search(r'(<div class="hospital-modal".*?</div>\s*</div>)', hospitals_html, re.DOTALL).group(1)

with open('src/main/resources/templates/analytics.html', 'r', encoding='utf-8') as f:
    analytics_html = f.read()
    analytics_style = re.search(r'<style>(.*?)</style>', analytics_html, re.DOTALL).group(1)
    analytics_content = re.search(r'<div class="analytics-container">(.*?)</div>\s*</div>\s*</div>', analytics_html, re.DOTALL).group(1)

# Inject styles into the head
new_style = f'''
        {incidents_style}
        {hospitals_style}
        {analytics_style}
'''
new_html = new_html.replace('</style>', new_style + '</style>')

# Merge the script tags from hospitals and analytics and incidents so buttons work
with open('src/main/resources/templates/hospitals.html', 'r', encoding='utf-8') as f:
    hospitals_js = re.search(r'<script>(.*?)</script>\s*</body>', f.read(), re.DOTALL).group(1)
    
with open('src/main/resources/templates/analytics.html', 'r', encoding='utf-8') as f:
    analytics_js = re.search(r'<script>(.*?)</script>\s*</body>', f.read(), re.DOTALL).group(1)

with open('src/main/resources/templates/incidents.html', 'r', encoding='utf-8') as f:
    incidents_js = re.search(r'<script>(.*?)</script>\s*</body>', f.read(), re.DOTALL).group(1)

# we need to remove the initialization calls from those scripts because patient.html will load them lazily or they will collide
incidents_js = incidents_js.replace('initMap();', '')
incidents_js = incidents_js.replace('detectLocation();', '')
incidents_js = incidents_js.replace('loadRecentIncidents();', '')

hospitals_js = hospitals_js.replace('loadHospitals();', '')
analytics_js = analytics_js.replace('loadSampleLogs();', '')
analytics_js = analytics_js.replace('updateStats();', '')

# Replace patient_js with patient_js + components
new_html = new_html.replace('{patient_js}', patient_js + '\n' + incidents_js + '\n' + hospitals_js + '\n' + analytics_js)


# Inject contents
tabs = f'''
            <div id="requestTab" class="tab-content active">
                <div class="incidents-container">
                    {incidents_content}
                </div>
            </div>
            
            <div id="hospitalsTabTab" class="tab-content">
                <div class="hospitals-container">
                    {hospitals_content}
                </div>
            </div>
            
            <div id="profileTab" class="tab-content">
                <div class="analytics-container">
                    {analytics_content}
                </div>
            </div>
            
            {incidents_modal}
            
            {hospitals_modal}
'''
new_html = new_html.replace('INJECT_TABS', tabs)

with open('src/main/resources/templates/patient.html', 'w', encoding='utf-8') as f:
    f.write(new_html)
