package com.aris.model;

public enum IncidentStatus {
    PENDING,        // patient requested, waiting for dispatch
    UNASSIGNED,     // no ambulance yet
    ASSIGNED,       // ambulance dispatched, en route to patient
    EN_ROUTE_PATIENT, // ambulance going to pick up patient
    PICKED_UP,      // patient picked up, going to hospital
    EN_ROUTE_HOSPITAL, // transporting patient to hospital
    ARRIVED_HOSPITAL,  // arrived at hospital
    RESOLVED,       // completed
    CANCELLED
}
