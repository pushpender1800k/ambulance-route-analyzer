package com.aris.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidents")
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id")
    private Long patientUserId;

    @Column(name = "condition_desc")
    private String condition;

    private Double lat;
    private Double lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentStatus status = IncidentStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_ambulance_id")
    private Ambulance assignedAmbulance;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "dispatched_at")
    private LocalDateTime dispatchedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "arrived_hospital_at")
    private LocalDateTime arrivedHospitalAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "patient_name")
    private String patientName;

    @Column(name = "patient_age")
    private Integer patientAge;

    @Column(name = "patient_gender")
    private String patientGender;

    @Column(name = "patient_phone")
    private String patientPhone;

    @Column(name = "patient_address")
    private String patientAddress;

    @Column(name = "medical_history", columnDefinition = "TEXT")
    private String medicalHistory;

    @Column(name = "current_problem", columnDefinition = "TEXT")
    private String currentProblem;

    @Column(name = "vitals_bp")
    private String vitalsBp;

    @Column(name = "vitals_pulse")
    private Integer vitalsPulse;

    @Column(name = "vitals_temperature")
    private Double vitalsTemperature;

    @Column(name = "vitals_oxygen")
    private Integer vitalsOxygen;

    @Column(name = "vitals_heart_rate")
    private Integer vitalsHeartRate;

    @Column(name = "vitals_respiratory_rate")
    private Integer vitalsRespiratoryRate;

    @Column(name = "vitals_recorded_at")
    private LocalDateTime vitalsRecordedAt;

    @Column(name = "emergency_type")
    private String emergencyType;

    @Column(name = "priority_level")
    private String priorityLevel;

    @Column(name = "assigned_hospital_id")
    private Long assignedHospitalId;

    @Column(name = "assigned_hospital_name")
    private String assignedHospitalName;

    @Column(name = "patient_preferred_hospital_id")
    private Long patientPreferredHospitalId;

    @Column(name = "patient_preferred_hospital_name")
    private String patientPreferredHospitalName;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "dispatcher_notes", columnDefinition = "TEXT")
    private String dispatcherNotes;

    @Column(name = "caller_name")
    private String callerName;

    @Column(name = "caller_phone")
    private String callerPhone;

    public Incident() {}

    public Incident(Long patientUserId, String condition, Double lat, Double lng) {
        this.patientUserId = patientUserId;
        this.condition = condition;
        this.lat = lat;
        this.lng = lng;
        this.status = IncidentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPatientUserId() { return patientUserId; }
    public void setPatientUserId(Long patientUserId) { this.patientUserId = patientUserId; }
    public String getCondition() { return condition; }
    public void setCondition(String condition) { this.condition = condition; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public IncidentStatus getStatus() { return status; }
    public void setStatus(IncidentStatus status) { this.status = status; }
    public Ambulance getAssignedAmbulance() { return assignedAmbulance; }
    public void setAssignedAmbulance(Ambulance assignedAmbulance) { this.assignedAmbulance = assignedAmbulance; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDispatchedAt() { return dispatchedAt; }
    public void setDispatchedAt(LocalDateTime dispatchedAt) { this.dispatchedAt = dispatchedAt; }
    public LocalDateTime getPickedUpAt() { return pickedUpAt; }
    public void setPickedUpAt(LocalDateTime pickedUpAt) { this.pickedUpAt = pickedUpAt; }
    public LocalDateTime getArrivedHospitalAt() { return arrivedHospitalAt; }
    public void setArrivedHospitalAt(LocalDateTime arrivedHospitalAt) { this.arrivedHospitalAt = arrivedHospitalAt; }
    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    
    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }
    public Integer getPatientAge() { return patientAge; }
    public void setPatientAge(Integer patientAge) { this.patientAge = patientAge; }
    public String getPatientGender() { return patientGender; }
    public void setPatientGender(String patientGender) { this.patientGender = patientGender; }
    public String getPatientPhone() { return patientPhone; }
    public void setPatientPhone(String patientPhone) { this.patientPhone = patientPhone; }
    public String getPatientAddress() { return patientAddress; }
    public void setPatientAddress(String patientAddress) { this.patientAddress = patientAddress; }
    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }
    public String getCurrentProblem() { return currentProblem; }
    public void setCurrentProblem(String currentProblem) { this.currentProblem = currentProblem; }
    public String getVitalsBp() { return vitalsBp; }
    public void setVitalsBp(String vitalsBp) { this.vitalsBp = vitalsBp; }
    public Integer getVitalsPulse() { return vitalsPulse; }
    public void setVitalsPulse(Integer vitalsPulse) { this.vitalsPulse = vitalsPulse; }
    public Double getVitalsTemperature() { return vitalsTemperature; }
    public void setVitalsTemperature(Double vitalsTemperature) { this.vitalsTemperature = vitalsTemperature; }
    public Integer getVitalsOxygen() { return vitalsOxygen; }
    public void setVitalsOxygen(Integer vitalsOxygen) { this.vitalsOxygen = vitalsOxygen; }
    public Integer getVitalsHeartRate() { return vitalsHeartRate; }
    public void setVitalsHeartRate(Integer vitalsHeartRate) { this.vitalsHeartRate = vitalsHeartRate; }
    public Integer getVitalsRespiratoryRate() { return vitalsRespiratoryRate; }
    public void setVitalsRespiratoryRate(Integer vitalsRespiratoryRate) { this.vitalsRespiratoryRate = vitalsRespiratoryRate; }
    public LocalDateTime getVitalsRecordedAt() { return vitalsRecordedAt; }
    public void setVitalsRecordedAt(LocalDateTime vitalsRecordedAt) { this.vitalsRecordedAt = vitalsRecordedAt; }
    public String getEmergencyType() { return emergencyType; }
    public void setEmergencyType(String emergencyType) { this.emergencyType = emergencyType; }
    public String getPriorityLevel() { return priorityLevel; }
    public void setPriorityLevel(String priorityLevel) { this.priorityLevel = priorityLevel; }
    public Long getAssignedHospitalId() { return assignedHospitalId; }
    public void setAssignedHospitalId(Long assignedHospitalId) { this.assignedHospitalId = assignedHospitalId; }
    public String getAssignedHospitalName() { return assignedHospitalName; }
    public void setAssignedHospitalName(String assignedHospitalName) { this.assignedHospitalName = assignedHospitalName; }
    public Long getPatientPreferredHospitalId() { return patientPreferredHospitalId; }
    public void setPatientPreferredHospitalId(Long patientPreferredHospitalId) { this.patientPreferredHospitalId = patientPreferredHospitalId; }
    public String getPatientPreferredHospitalName() { return patientPreferredHospitalName; }
    public void setPatientPreferredHospitalName(String patientPreferredHospitalName) { this.patientPreferredHospitalName = patientPreferredHospitalName; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getDispatcherNotes() { return dispatcherNotes; }
    public void setDispatcherNotes(String dispatcherNotes) { this.dispatcherNotes = dispatcherNotes; }
    public String getCallerName() { return callerName; }
    public void setCallerName(String callerName) { this.callerName = callerName; }
    public String getCallerPhone() { return callerPhone; }
    public void setCallerPhone(String callerPhone) { this.callerPhone = callerPhone; }
}
