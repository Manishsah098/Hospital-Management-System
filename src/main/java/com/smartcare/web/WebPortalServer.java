package com.smartcare.web;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.smartcare.dao.AppointmentDAO;
import com.smartcare.dao.DepartmentDAO;
import com.smartcare.dao.DoctorDAO;
import com.smartcare.dao.PatientDAO;
import com.smartcare.dao.impl.AppointmentDAOImpl;
import com.smartcare.dao.impl.DepartmentDAOImpl;
import com.smartcare.dao.impl.DoctorDAOImpl;
import com.smartcare.dao.impl.PatientDAOImpl;
import com.smartcare.enums.AppointmentStatus;
import com.smartcare.enums.Gender;
import com.smartcare.model.Appointment;
import com.smartcare.model.Department;
import com.smartcare.model.Doctor;
import com.smartcare.model.Patient;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Embedded HTTP server for SmartCare Patient Web Booking Portal.
 * Serves the static web-portal files and exposes REST API endpoints
 * that read/write directly to the same MySQL database as the HMS app.
 *
 * Endpoints:
 *   GET  /api/departments           → list all departments
 *   GET  /api/doctors               → list all doctors (optional ?deptId=N filter)
 *   POST /api/book                  → create appointment, returns token
 *   GET  /api/track?token=SC-...    → find appointment by token (stored in notes)
 *   GET  /api/track?phone=9xxxxxxx  → find appointments by patient phone
 *   GET  /*                         → serve static files from web-portal/
 */
public class WebPortalServer {

    private static final Logger LOGGER = Logger.getLogger(WebPortalServer.class.getName());
    private static final int PORT = 8080;
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .create();

    private HttpServer server;
    private final String webPortalDir;

    public WebPortalServer(String webPortalDir) {
        this.webPortalDir = webPortalDir;
    }

    public void start() {
        try {
            server = HttpServer.create(new InetSocketAddress(PORT), 0);
            server.setExecutor(Executors.newFixedThreadPool(4));

            // API routes
            server.createContext("/api/departments", this::handleDepartments);
            server.createContext("/api/doctors",     this::handleDoctors);
            server.createContext("/api/book",        this::handleBook);
            server.createContext("/api/track",       this::handleTrack);

            // Static file fallback — must be last
            server.createContext("/", this::handleStatic);

            server.start();
            LOGGER.info("SmartCare Patient Portal running at: http://localhost:" + PORT);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to start Patient Portal web server on port " + PORT, e);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(2);
            LOGGER.info("SmartCare Patient Portal web server stopped.");
        }
    }

    // ─── GET /api/departments ────────────────────────────────────────────────
    private void handleDepartments(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            DepartmentDAO dao = new DepartmentDAOImpl();
            List<Department> depts = dao.findAll();
            // Build a simplified JSON array
            List<Map<String, Object>> result = new ArrayList<>();
            for (Department d : depts) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id",          d.getDepartmentId());
                map.put("name",        d.getName());
                map.put("description", d.getDescription());
                map.put("head",        d.getHeadDoctorName());
                result.add(map);
            }
            sendJson(ex, 200, GSON.toJson(result));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching departments for web portal: " + e.getMessage());
            sendJson(ex, 500, "{\"error\":\"Failed to load departments\"}");
        }
    }

    // ─── GET /api/doctors[?deptId=N] ─────────────────────────────────────────
    private void handleDoctors(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            DoctorDAO dao = new DoctorDAOImpl();
            List<Doctor> doctors = dao.findAll();

            // Optional filter by department
            String query = ex.getRequestURI().getQuery();
            if (query != null && query.startsWith("deptId=")) {
                int deptId = Integer.parseInt(query.split("=")[1]);
                doctors.removeIf(d -> d.getDepartmentId() == null || d.getDepartmentId() != deptId);
            }

            // Only available doctors
            doctors.removeIf(d -> !d.isAvailable());

            List<Map<String, Object>> result = new ArrayList<>();
            for (Doctor d : doctors) {
                Map<String, Object> map = new LinkedHashMap<>();
                map.put("id",             d.getDoctorId());
                map.put("name",           d.getFullName());
                map.put("specialization", d.getSpecialization());
                map.put("departmentId",   d.getDepartmentId());
                map.put("departmentName", d.getDepartmentName());
                map.put("qualification",  d.getQualification());
                map.put("experience",     d.getExperienceYears());
                map.put("fee",            d.getConsultationFee());
                map.put("availableDays",  d.getAvailableDays());
                result.add(map);
            }
            sendJson(ex, 200, GSON.toJson(result));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error fetching doctors for web portal: " + e.getMessage());
            sendJson(ex, 500, "{\"error\":\"Failed to load doctors\"}");
        }
    }

    // ─── POST /api/book ───────────────────────────────────────────────────────
    private void handleBook(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("POST")) {
            sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            // Parse JSON body
            String body = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            // Safely parse JSON fields with fallbacks
            String  patientName = getString(json, "patientName", "");
            String  phone       = getString(json, "phone", "");
            String  email       = getString(json, "email", "");
            String  dobStr      = getString(json, "dob", "1995-01-01");
            String  genderStr   = getString(json, "gender", "MALE");
            String  reason      = getString(json, "reason", "General OPD Consultation");
            int     doctorId    = getInt(json, "doctorId", 1);
            String  dateStr     = getString(json, "date", LocalDate.now().toString());
            String  timeStr     = getString(json, "time", "10:00 AM");
            String  patientCode = getString(json, "patientCode", "");

            if (patientName.isEmpty() || phone.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"Patient name and phone number are required.\"}");
                return;
            }

            // Resolve or create patient
            PatientDAO patientDAO = new PatientDAOImpl();
            Patient patient = null;

            // 1. Try existing patient code first
            if (!patientCode.isEmpty()) {
                patient = patientDAO.findByCode(patientCode).orElse(null);
            }
            // 2. Try phone lookup
            if (patient == null) {
                patient = patientDAO.findByPhone(phone).orElse(null);
            }
            // 3. Register as new patient
            if (patient == null) {
                patient = new Patient();
                patient.setPatientCode(patientDAO.generateNextPatientCode());
                patient.setFullName(patientName);
                patient.setPhone(phone);
                patient.setEmail(email.isEmpty() ? null : email);
                LocalDate dob = LocalDate.of(1995, 1, 1);
                if (dobStr != null && !dobStr.isBlank()) {
                    try { dob = LocalDate.parse(dobStr); } catch (Exception ignored) {}
                }
                patient.setDateOfBirth(dob);
                Gender gender = Gender.MALE;
                if (genderStr != null && !genderStr.isBlank()) {
                    try { gender = Gender.valueOf(genderStr.toUpperCase()); } catch (Exception ignored) {}
                }
                patient.setGender(gender);
                patient.setAddress("Registered via Online Portal");
                boolean created = patientDAO.create(patient);
                if (!created) {
                    sendJson(ex, 500, "{\"error\":\"Failed to register patient. Please try again.\"}");
                    return;
                }
                // Re-fetch to get generated ID and code
                patient = patientDAO.findByPhone(phone).orElse(patient);
            }

            // Parse appointment date/time
            LocalDate apptDate = LocalDate.now();
            if (dateStr != null && !dateStr.isBlank()) {
                try { apptDate = LocalDate.parse(dateStr); } catch (Exception ignored) {}
            }
            LocalTime apptTime = parseTime(timeStr);

            // Check doctor conflict
            AppointmentDAO apptDAO = new AppointmentDAOImpl();
            boolean conflict = apptDAO.hasDoctorConflict(doctorId, apptDate, apptTime, null);
            if (conflict) {
                sendJson(ex, 409, "{\"error\":\"This time slot is already booked. Please choose another slot.\"}");
                return;
            }

            // Generate online token — stored in notes field
            String year  = String.valueOf(apptDate.getYear());
            String token = "SC-" + year + "-" + String.format("%04d", System.currentTimeMillis() % 9999 + 1);

            // Create appointment
            Appointment appt = new Appointment();
            appt.setPatientId(patient.getPatientId());
            appt.setDoctorId(doctorId);
            appt.setAppointmentDate(apptDate);
            appt.setAppointmentTime(apptTime);
            appt.setStatus(AppointmentStatus.SCHEDULED);
            appt.setReasonForVisit(reason);
            appt.setNotes("ONLINE_BOOKING | Token: " + token
                    + " | Source: Patient Web Portal"
                    + " | Phone: " + phone);

            boolean saved = apptDAO.create(appt);
            if (!saved) {
                sendJson(ex, 500, "{\"error\":\"Failed to save appointment. Please try again.\"}");
                return;
            }

            // Build success response
            DoctorDAO doctorDAO = new DoctorDAOImpl();
            Doctor doctor = doctorDAO.findById(doctorId).orElse(null);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success",       true);
            resp.put("token",         token);
            resp.put("appointmentId", appt.getAppointmentId());
            resp.put("queueNumber",   appt.getAppointmentId());
            resp.put("patientName",   patientName);
            resp.put("patientCode",   patient.getPatientCode());
            resp.put("phone",         phone);
            resp.put("doctorName",    doctor != null ? doctor.getFullName() : "Doctor #" + doctorId);
            resp.put("specialization",doctor != null ? doctor.getSpecialization() : "");
            resp.put("department",    doctor != null ? doctor.getDepartmentName() : "");
            resp.put("fee",           doctor != null ? doctor.getConsultationFee() : 0);
            resp.put("date",          dateStr);
            resp.put("time",          timeStr);
            resp.put("reason",        reason);
            resp.put("status",        "SCHEDULED");
            resp.put("bookedAt",      java.time.LocalDateTime.now().toString());

            LOGGER.info("Online appointment booked: " + token + " | " + patientName + " with Doctor #" + doctorId);
            sendJson(ex, 200, GSON.toJson(resp));

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error booking online appointment: " + e.getMessage(), e);
            sendJson(ex, 500, "{\"error\":\"" + e.getMessage().replace("\"","'") + "\"}");
        }
    }

    // ─── GET /api/track?token=SC-...  OR  ?phone=9xxxxxxx ────────────────────
    private void handleTrack(HttpExchange ex) throws IOException {
        if (!ex.getRequestMethod().equalsIgnoreCase("GET")) {
            sendResponse(ex, 405, "{\"error\":\"Method not allowed\"}");
            return;
        }
        try {
            String query = ex.getRequestURI().getQuery();
            if (query == null || query.isEmpty()) {
                sendJson(ex, 400, "{\"error\":\"Provide token or phone query parameter\"}");
                return;
            }

            Map<String, String> params = parseQuery(query);
            AppointmentDAO apptDAO = new AppointmentDAOImpl();
            List<Appointment> matches = new ArrayList<>();

            if (params.containsKey("token")) {
                String token = params.get("token").toUpperCase();
                // Search in all appointments whose notes contain the token
                List<Appointment> all = apptDAO.findAll();
                for (Appointment a : all) {
                    if (a.getNotes() != null && a.getNotes().contains(token)) {
                        matches.add(a);
                        break;
                    }
                }
            } else if (params.containsKey("phone")) {
                String phone = params.get("phone");
                PatientDAO patientDAO = new PatientDAOImpl();
                Patient patient = patientDAO.findByPhone(phone).orElse(null);
                if (patient != null) {
                    matches = apptDAO.findByPatientId(patient.getPatientId());
                }
            }

            if (matches.isEmpty()) {
                sendJson(ex, 404, "{\"found\":false,\"message\":\"No appointment found.\"}");
                return;
            }

            List<Map<String, Object>> result = new ArrayList<>();
            for (Appointment a : matches) {
                // Extract token from notes
                String storedToken = "";
                if (a.getNotes() != null && a.getNotes().contains("Token:")) {
                    String[] parts = a.getNotes().split("Token:");
                    if (parts.length > 1) storedToken = parts[1].split("\\|")[0].trim();
                }
                if (storedToken.isEmpty()) {
                    storedToken = "SC-" + (a.getAppointmentDate() != null ? a.getAppointmentDate().getYear() : "2024")
                            + "-" + String.format("%04d", a.getAppointmentId());
                }

                Map<String, Object> map = new LinkedHashMap<>();
                map.put("token",          storedToken);
                map.put("appointmentId",  a.getAppointmentId());
                map.put("queueNumber",    a.getAppointmentId());
                map.put("patientName",    a.getPatientName());
                map.put("patientCode",    a.getPatientCode());
                map.put("doctorName",     a.getDoctorName());
                map.put("specialization", a.getDoctorSpecialization());
                map.put("date",           a.getAppointmentDate() != null ? a.getAppointmentDate().toString() : "");
                map.put("time",           a.getAppointmentTime() != null
                                            ? a.getAppointmentTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "");
                map.put("reason",         a.getReasonForVisit());
                map.put("status",         a.getStatus() != null ? a.getStatus().name() : "SCHEDULED");
                map.put("notes",          a.getNotes());
                result.add(map);
            }
            sendJson(ex, 200, GSON.toJson(result));

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error tracking appointment: " + e.getMessage());
            sendJson(ex, 500, "{\"error\":\"Failed to look up appointment.\"}");
        }
    }

    // ─── Static file server ───────────────────────────────────────────────────
    private void handleStatic(HttpExchange ex) throws IOException {
        String uriPath = ex.getRequestURI().getPath();
        if (uriPath.equals("/") || uriPath.isEmpty()) uriPath = "/index.html";

        // Security: prevent directory traversal
        if (uriPath.contains("..")) {
            sendResponse(ex, 403, "Forbidden");
            return;
        }

        Path rootDir = Paths.get(webPortalDir).toAbsolutePath().normalize();
        Path filePath = rootDir.resolve(uriPath.startsWith("/") ? uriPath.substring(1) : uriPath).normalize();
        if (!filePath.startsWith(rootDir)) {
            sendResponse(ex, 403, "Forbidden");
            return;
        }

        if (!Files.exists(filePath) || Files.isDirectory(filePath)) {
            sendResponse(ex, 404, "Not Found: " + uriPath);
            return;
        }

        String contentType = getContentType(filePath.toString());
        byte[] bytes = Files.readAllBytes(filePath);

        ex.getResponseHeaders().set("Content-Type", contentType);
        addCorsHeaders(ex);
        ex.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────
    private void sendJson(HttpExchange ex, int status, String json) throws IOException {
        addCorsHeaders(ex);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void sendResponse(HttpExchange ex, int status, String body) throws IOException {
        addCorsHeaders(ex);
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        ex.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
    }

    private void addCorsHeaders(HttpExchange ex) {
        ex.getResponseHeaders().set("Access-Control-Allow-Origin",  "*");
        ex.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        ex.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
    }

    private String getContentType(String path) {
        if (path.endsWith(".html")) return "text/html; charset=UTF-8";
        if (path.endsWith(".css"))  return "text/css; charset=UTF-8";
        if (path.endsWith(".js"))   return "application/javascript; charset=UTF-8";
        if (path.endsWith(".json")) return "application/json";
        if (path.endsWith(".png"))  return "image/png";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".svg"))  return "image/svg+xml";
        if (path.endsWith(".ico"))  return "image/x-icon";
        return "application/octet-stream";
    }

    private Map<String, String> parseQuery(String query) {
        Map<String, String> map = new LinkedHashMap<>();
        if (query == null) return map;
        for (String pair : query.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                try {
                    map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                            URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
                } catch (Exception ignored) {}
            }
        }
        return map;
    }

    /** Parses "09:00 AM", "2:30 PM", or "HH:mm" into LocalTime */
    private LocalTime parseTime(String timeStr) {
        timeStr = timeStr.trim().toUpperCase();
        try {
            if (timeStr.contains("AM") || timeStr.contains("PM")) {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
                return LocalTime.parse(timeStr, fmt);
            }
            return LocalTime.parse(timeStr);
        } catch (Exception e) {
            return LocalTime.of(9, 0);
        }
    }

    private String getString(JsonObject json, String key, String defaultValue) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            try {
                String val = json.get(key).getAsString().trim();
                return val.isEmpty() ? defaultValue : val;
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    private int getInt(JsonObject json, String key, int defaultValue) {
        if (json != null && json.has(key) && !json.get(key).isJsonNull()) {
            try {
                return json.get(key).getAsInt();
            } catch (Exception ignored) {}
        }
        return defaultValue;
    }

    public static void main(String[] args) {
        String webDir = "web-portal";
        if (args.length > 0) {
            webDir = args[0];
        } else {
            java.io.File dir = new java.io.File("web-portal");
            if (!dir.exists()) {
                dir = new java.io.File("..", "web-portal");
            }
            webDir = dir.getAbsolutePath();
        }
        WebPortalServer server = new WebPortalServer(webDir);
        server.start();
        System.out.println("=================================================");
        System.out.println("  SmartCare Patient Portal Server is RUNNING!   ");
        System.out.println("  URL: http://localhost:8080                     ");
        System.out.println("  Press Ctrl+C to stop.                          ");
        System.out.println("=================================================");
    }
}
