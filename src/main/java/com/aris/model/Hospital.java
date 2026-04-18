package com.aris.model;

import jakarta.persistence.*;

@Entity
@Table(name = "hospitals")
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double lat;
    private Double lng;

    @Column(name = "total_beds")
    private Integer totalBeds;

    @Column(name = "available_beds")
    private Integer availableBeds;

    private String specialties;

    @Column(nullable = false)
    private String status = "OPEN";

    @Column(name = "workload_percentage")
    private Integer workloadPercentage;

    @Column(name = "emergency_capacity")
    private Integer emergencyCapacity;

    @Column(name = "current_patients")
    private Integer currentPatients;

    @Column(name = "wait_time_minutes")
    private Integer waitTimeMinutes;

    @Column(name = "contact_phone")
    private String contactPhone;

    @Column(name = "address")
    private String address;

    public Hospital() {}

    public Hospital(String name, Double lat, Double lng, Integer totalBeds, Integer availableBeds, String specialties) {
        this.name = name;
        this.lat = lat;
        this.lng = lng;
        this.totalBeds = totalBeds;
        this.availableBeds = availableBeds;
        this.specialties = specialties;
        this.status = "OPEN";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }
    public Integer getTotalBeds() { return totalBeds; }
    public void setTotalBeds(Integer totalBeds) { this.totalBeds = totalBeds; }
    public Integer getAvailableBeds() { return availableBeds; }
    public void setAvailableBeds(Integer availableBeds) { this.availableBeds = availableBeds; }
    public String getSpecialties() { return specialties; }
    public void setSpecialties(String specialties) { this.specialties = specialties; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Integer getWorkloadPercentage() { return workloadPercentage; }
    public void setWorkloadPercentage(Integer workloadPercentage) { this.workloadPercentage = workloadPercentage; }
    public Integer getEmergencyCapacity() { return emergencyCapacity; }
    public void setEmergencyCapacity(Integer emergencyCapacity) { this.emergencyCapacity = emergencyCapacity; }
    public Integer getCurrentPatients() { return currentPatients; }
    public void setCurrentPatients(Integer currentPatients) { this.currentPatients = currentPatients; }
    public Integer getWaitTimeMinutes() { return waitTimeMinutes; }
    public void setWaitTimeMinutes(Integer waitTimeMinutes) { this.waitTimeMinutes = waitTimeMinutes; }
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}
