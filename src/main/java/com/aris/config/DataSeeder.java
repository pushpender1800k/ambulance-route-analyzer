package com.aris.config;

import com.aris.model.*;
import com.aris.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final HospitalRepository hospitalRepository;
    private final AmbulanceRepository ambulanceRepository;
    private final EventLogRepository eventLogRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      HospitalRepository hospitalRepository,
                      AmbulanceRepository ambulanceRepository,
                      EventLogRepository eventLogRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.hospitalRepository = hospitalRepository;
        this.ambulanceRepository = ambulanceRepository;
        this.eventLogRepository = eventLogRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUsers();
        seedHospitals();
        seedAmbulances();
        seedInitialEvents();
    }

    private void seedUsers() {
        if (userRepository.count() > 0) return;

        userRepository.save(new User("admin", passwordEncoder.encode("admin123"), Role.COMMAND));
        userRepository.save(new User("dispatcher", passwordEncoder.encode("dispatch123"), Role.DISPATCHER));
        userRepository.save(new User("coordinator", passwordEncoder.encode("coord123"), Role.COORDINATOR));
        userRepository.save(new User("supervisor", passwordEncoder.encode("super123"), Role.SUPERVISOR));

        User driver1 = new User("driver1", passwordEncoder.encode("driver123"), Role.DRIVER);
        driver1.setFullName("Ramesh Kumar");
        driver1.setPhone("9876543210");
        userRepository.save(driver1);

        User driver2 = new User("driver2", passwordEncoder.encode("driver123"), Role.DRIVER);
        driver2.setFullName("Suresh Singh");
        driver2.setPhone("9876543211");
        userRepository.save(driver2);

        User driver3 = new User("driver3", passwordEncoder.encode("driver123"), Role.DRIVER);
        driver3.setFullName("Vikram Yadav");
        driver3.setPhone("9876543212");
        userRepository.save(driver3);

        User driver4 = new User("driver4", passwordEncoder.encode("driver123"), Role.DRIVER);
        driver4.setFullName("Ajay Verma");
        driver4.setPhone("9876543213");
        userRepository.save(driver4);

        User patient1 = new User("patient1", passwordEncoder.encode("patient123"), Role.PATIENT);
        patient1.setFullName("John Doe");
        patient1.setPhone("9988776655");
        patient1.setEmail("john.doe@email.com");
        patient1.setAddress("123 Sector 15, Noida");
        patient1.setEmergencyContactName("Jane Doe");
        patient1.setEmergencyContactPhone("9988776656");
        patient1.setMedicalHistory("No known allergies");
        patient1.setBloodType("O+");
        patient1.setDateOfBirth("1985-06-15");
        userRepository.save(patient1);

        User patient2 = new User("patient2", passwordEncoder.encode("patient123"), Role.PATIENT);
        patient2.setFullName("Priya Sharma");
        patient2.setPhone("9988776657");
        patient2.setEmail("priya.sharma@email.com");
        patient2.setAddress("456 MG Road, Gurgaon");
        patient2.setEmergencyContactName("Rahul Sharma");
        patient2.setEmergencyContactPhone("9988776658");
        patient2.setBloodType("A+");
        patient2.setDateOfBirth("1990-03-22");
        userRepository.save(patient2);

        System.out.println("✅ Seeded 4 admin users + 4 drivers + 2 patients");
    }

    private void seedHospitals() {
        if (hospitalRepository.count() > 0) return;

        hospitalRepository.save(new Hospital("AIIMS Delhi", 28.5672, 77.2100, 200, 42, "Trauma,Cardiac,Neuro,Burns"));
        hospitalRepository.save(new Hospital("Safdarjung Hospital", 28.5685, 77.2078, 180, 35, "Trauma,Ortho,General"));
        hospitalRepository.save(new Hospital("Sir Ganga Ram Hospital", 28.6380, 77.1908, 150, 28, "Cardiac,Neuro,Pediatric"));
        hospitalRepository.save(new Hospital("Max Super Speciality Saket", 28.5274, 77.2159, 120, 51, "Cardiac,Oncology,Transplant"));
        hospitalRepository.save(new Hospital("Apollo Hospital", 28.5530, 77.2593, 160, 38, "Trauma,Cardiac,Neuro,Robotic"));
        hospitalRepository.save(new Hospital("Fortis Escorts Heart Institute", 28.5494, 77.2207, 100, 22, "Cardiac,Vascular"));
        hospitalRepository.save(new Hospital("RML Hospital", 28.6252, 77.2021, 170, 45, "General,Trauma,Burns"));

        System.out.println("✅ Seeded 7 Delhi-area hospitals");
    }

    private void seedAmbulances() {
        if (ambulanceRepository.count() > 0) return;

        Ambulance amb1 = new Ambulance("AMB-101", 28.5670, 77.2100);
        amb1.setVehicleNumber("DL-01-AB-1234");
        amb1.setVehicleModel("Mahindra Bolero");
        amb1.setBaseLocation("AIIMS Campus");
        amb1.setEquipmentLevel("Advanced Life Support");
        ambulanceRepository.save(amb1);

        Ambulance amb2 = new Ambulance("AMB-102", 28.6200, 77.2500);
        amb2.setVehicleNumber("DL-01-CD-5678");
        amb2.setVehicleModel("Tata Winger");
        amb2.setBaseLocation("Lajpat Nagar Station");
        amb2.setEquipmentLevel("Basic Life Support");
        ambulanceRepository.save(amb2);

        Ambulance amb3 = new Ambulance("AMB-103", 28.5400, 77.2000);
        amb3.setVehicleNumber("DL-01-EF-9012");
        amb3.setVehicleModel("Force Traveller");
        amb3.setBaseLocation("Nehru Place");
        amb3.setEquipmentLevel("Advanced Life Support");
        ambulanceRepository.save(amb3);

        Ambulance amb4 = new Ambulance("AMB-104", 28.6500, 77.2800);
        amb4.setVehicleNumber("DL-01-GH-3456");
        amb4.setVehicleModel("Maruti Eeco");
        amb4.setBaseLocation("Dwarka Sector 21");
        amb4.setEquipmentLevel("Basic Life Support");
        ambulanceRepository.save(amb4);

        var users = userRepository.findAll();
        for (var user : users) {
            if (user.getRole() == Role.DRIVER && user.getAssignedAmbulanceId() == null) {
                Ambulance amb = null;
                if (user.getUsername().equals("driver1")) {
                    amb = ambulanceRepository.findByUnitCode("AMB-101").orElse(null);
                } else if (user.getUsername().equals("driver2")) {
                    amb = ambulanceRepository.findByUnitCode("AMB-102").orElse(null);
                } else if (user.getUsername().equals("driver3")) {
                    amb = ambulanceRepository.findByUnitCode("AMB-103").orElse(null);
                } else if (user.getUsername().equals("driver4")) {
                    amb = ambulanceRepository.findByUnitCode("AMB-104").orElse(null);
                }
                
                if (amb != null) {
                    amb.setAssignedDriverId(user.getId());
                    amb.setAssignedDriverName(user.getFullName());
                    ambulanceRepository.save(amb);
                    
                    user.setAssignedAmbulanceId(amb.getId());
                    userRepository.save(user);
                }
            }
        }

        System.out.println("✅ Seeded 4 ambulances with assigned drivers");
    }

    private void seedInitialEvents() {
        if (eventLogRepository.count() > 0) return;

        eventLogRepository.save(new EventLog("SYSTEM", "ARIS Core Engine Online. All units initialized.", "INFO"));
        eventLogRepository.save(new EventLog("DISPATCH", "Daily shift started. 5 units marked STANDBY.", "SYSTEM"));

        System.out.println("✅ Seeded initial event logs");
    }
}
