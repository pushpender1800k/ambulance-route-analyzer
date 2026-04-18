package com.aris.model;

import jakarta.persistence.*;

@Entity
@Table(name = "ambulances")
public class Ambulance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_code", unique = true, nullable = false)
    private String unitCode;

    private Double lat;
    private Double lng;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AmbulanceStatus status = AmbulanceStatus.STANDBY;

    @OneToOne(mappedBy = "assignedAmbulance", fetch = FetchType.LAZY)
    private Incident currentIncident;

    @Column(name = "assigned_dispatcher_id")
    private Long assignedDispatcherId;

    @Column(name = "assigned_dispatcher_name")
    private String assignedDispatcherName;

    @Column(name = "current_patient_name")
    private String currentPatientName;

    @Column(name = "destination_hospital")
    private String destinationHospital;

    @Column(name = "destination_hospital_id")
    private Long destinationHospitalId;

    @Column(name = "eta_minutes")
    private Integer etaMinutes;

    @Column(name = "destination_lat")
    private Double destinationLat;

    @Column(name = "destination_lng")
    private Double destinationLng;

    @Column(name = "assigned_driver_id")
    private Long assignedDriverId;

    @Column(name = "assigned_driver_name")
    private String assignedDriverName;

    @Column(name = "vehicle_number")
    private String vehicleNumber;

    @Column(name = "vehicle_model")
    private String vehicleModel;

    @Column(name = "base_location")
    private String baseLocation;

    @Column(name = "equipment_level")
    private String equipmentLevel;

    @Column(name = "route_to_patient_json", columnDefinition = "TEXT")
    private String routeToPatientJson;

    @Column(name = "route_to_hospital_json", columnDefinition = "TEXT")
    private String routeToHospitalJson;

    @Column(name = "route_to_patient_progress")
    private Double routeToPatientProgress = 0.0;

    @Column(name = "route_to_hospital_progress")
    private Double routeToHospitalProgress = 0.0;

    public Ambulance() {}

    public Ambulance(String unitCode, Double lat, Double lng) {
        this.unitCode = unitCode;
        this.lat = lat;
        this.lng = lng;
        this.status = AmbulanceStatus.STANDBY;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getUnitCode() { return unitCode; }
    public void setUnitCode(String unitCode) { this.unitCode = unitCode; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public AmbulanceStatus getStatus() { return status; }
    public void setStatus(AmbulanceStatus status) { this.status = status; }
    public Incident getCurrentIncident() { return currentIncident; }
    public void setCurrentIncident(Incident currentIncident) { this.currentIncident = currentIncident; }
    public Long getAssignedDispatcherId() { return assignedDispatcherId; }
    public void setAssignedDispatcherId(Long assignedDispatcherId) { this.assignedDispatcherId = assignedDispatcherId; }
    public String getAssignedDispatcherName() { return assignedDispatcherName; }
    public void setAssignedDispatcherName(String assignedDispatcherName) { this.assignedDispatcherName = assignedDispatcherName; }
    public String getCurrentPatientName() { return currentPatientName; }
    public void setCurrentPatientName(String currentPatientName) { this.currentPatientName = currentPatientName; }
    public String getDestinationHospital() { return destinationHospital; }
    public void setDestinationHospital(String destinationHospital) { this.destinationHospital = destinationHospital; }
    public Long getDestinationHospitalId() { return destinationHospitalId; }
    public void setDestinationHospitalId(Long destinationHospitalId) { this.destinationHospitalId = destinationHospitalId; }
    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }
    public Double getDestinationLat() { return destinationLat; }
    public void setDestinationLat(Double destinationLat) { this.destinationLat = destinationLat; }
    public Double getDestinationLng() { return destinationLng; }
    public void setDestinationLng(Double destinationLng) { this.destinationLng = destinationLng; }
    public Long getAssignedDriverId() { return assignedDriverId; }
    public void setAssignedDriverId(Long assignedDriverId) { this.assignedDriverId = assignedDriverId; }
    public String getAssignedDriverName() { return assignedDriverName; }
    public void setAssignedDriverName(String assignedDriverName) { this.assignedDriverName = assignedDriverName; }
    public String getVehicleNumber() { return vehicleNumber; }
    public void setVehicleNumber(String vehicleNumber) { this.vehicleNumber = vehicleNumber; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getBaseLocation() { return baseLocation; }
    public void setBaseLocation(String baseLocation) { this.baseLocation = baseLocation; }
    public String getEquipmentLevel() { return equipmentLevel; }
    public void setEquipmentLevel(String equipmentLevel) { this.equipmentLevel = equipmentLevel; }
    public String getRouteToPatientJson() { return routeToPatientJson; }
    public void setRouteToPatientJson(String routeToPatientJson) { this.routeToPatientJson = routeToPatientJson; }
    public String getRouteToHospitalJson() { return routeToHospitalJson; }
    public void setRouteToHospitalJson(String routeToHospitalJson) { this.routeToHospitalJson = routeToHospitalJson; }
    public Double getRouteToPatientProgress() { return routeToPatientProgress; }
    public void setRouteToPatientProgress(Double routeToPatientProgress) { this.routeToPatientProgress = routeToPatientProgress; }
    public Double getRouteToHospitalProgress() { return routeToHospitalProgress; }
    public void setRouteToHospitalProgress(Double routeToHospitalProgress) { this.routeToHospitalProgress = routeToHospitalProgress; }
}
