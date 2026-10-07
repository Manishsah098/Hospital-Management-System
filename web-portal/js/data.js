/**
 * SmartCare Hospital – Patient Portal Data
 * Mirrors the SmartCare database seed data: departments, doctors, time slots
 */

const DEPARTMENTS = [
    {
        id: 1,
        name: "Cardiology",
        emoji: "❤️",
        desc: "Heart, blood vessels & cardiovascular health",
        doctorCount: 4,
        head: "Dr. Rajesh Sharma",
        bookingAvail: true
    },
    {
        id: 2,
        name: "Neurology",
        emoji: "🧠",
        desc: "Brain, spinal cord & nervous system disorders",
        doctorCount: 3,
        head: "Dr. Priya Patel",
        bookingAvail: true
    },
    {
        id: 3,
        name: "Orthopedics",
        emoji: "🦴",
        desc: "Bones, joints, ligaments, tendons & muscles",
        doctorCount: 3,
        head: "Dr. Amit Roy",
        bookingAvail: true
    },
    {
        id: 4,
        name: "General Medicine",
        emoji: "🩺",
        desc: "Primary healthcare, diagnostics & internal medicine",
        doctorCount: 5,
        head: "Dr. Sunita Rao",
        bookingAvail: true
    },
    {
        id: 5,
        name: "Pediatrics",
        emoji: "👶",
        desc: "Medical care for infants, children & adolescents",
        doctorCount: 3,
        head: "Dr. Neha Gupta",
        bookingAvail: true
    },
    {
        id: 6,
        name: "Dermatology",
        emoji: "🧴",
        desc: "Skin, hair & nail conditions",
        doctorCount: 2,
        head: "Dr. Kavita Iyer",
        bookingAvail: true
    },
    {
        id: 7,
        name: "Ophthalmology",
        emoji: "👁️",
        desc: "Eye care, vision disorders & surgery",
        doctorCount: 2,
        head: "Dr. Arjun Nair",
        bookingAvail: true
    },
    {
        id: 8,
        name: "Gynecology",
        emoji: "🌸",
        desc: "Women's reproductive health & obstetrics",
        doctorCount: 3,
        head: "Dr. Meera Pillai",
        bookingAvail: true
    },
    {
        id: 9,
        name: "ENT",
        emoji: "👂",
        desc: "Ear, nose & throat disorders",
        doctorCount: 2,
        head: "Dr. Vivek Shah",
        bookingAvail: true
    },
    {
        id: 10,
        name: "Radiology",
        emoji: "🔬",
        desc: "Diagnostic imaging: X-ray, MRI, CT scan",
        doctorCount: 2,
        head: "Dr. Pooja Verma",
        bookingAvail: true
    }
];

const DOCTORS = [
    {
        id: 1,
        name: "Dr. Rajesh Sharma",
        specialization: "Cardiologist",
        departmentId: 1,
        department: "Cardiology",
        qualification: "MBBS, MD, DM (Cardiology)",
        experience: 12,
        fee: 800,
        availableDays: "Mon, Tue, Wed, Thu, Fri",
        emoji: "👨‍⚕️",
        avatarBg: "#1a3a4a",
        rating: 4.8,
        licenseNumber: "MED-IND-2015-8841"
    },
    {
        id: 2,
        name: "Dr. Priya Patel",
        specialization: "Neurologist",
        departmentId: 2,
        department: "Neurology",
        qualification: "MBBS, MD, DM (Neurology)",
        experience: 9,
        fee: 900,
        availableDays: "Mon, Wed, Thu, Fri",
        emoji: "👩‍⚕️",
        avatarBg: "#2a1a4a",
        rating: 4.9,
        licenseNumber: "MED-IND-2018-9421"
    },
    {
        id: 3,
        name: "Dr. Amit Roy",
        specialization: "Orthopedic Surgeon",
        departmentId: 3,
        department: "Orthopedics",
        qualification: "MBBS, MS (Orthopedics)",
        experience: 15,
        fee: 750,
        availableDays: "Tue, Wed, Thu, Sat",
        emoji: "👨‍⚕️",
        avatarBg: "#1a4a2a",
        rating: 4.7,
        licenseNumber: "MED-IND-2010-5511"
    },
    {
        id: 4,
        name: "Dr. Sunita Rao",
        specialization: "General Physician",
        departmentId: 4,
        department: "General Medicine",
        qualification: "MBBS, MD (General Medicine)",
        experience: 18,
        fee: 500,
        availableDays: "Mon, Tue, Wed, Thu, Fri, Sat",
        emoji: "👩‍⚕️",
        avatarBg: "#3a2a1a",
        rating: 4.8,
        licenseNumber: "MED-IND-2007-3341"
    },
    {
        id: 5,
        name: "Dr. Neha Gupta",
        specialization: "Pediatrician",
        departmentId: 5,
        department: "Pediatrics",
        qualification: "MBBS, MD (Pediatrics)",
        experience: 8,
        fee: 600,
        availableDays: "Mon, Tue, Thu, Fri",
        emoji: "👩‍⚕️",
        avatarBg: "#1a2a3a",
        rating: 4.9,
        licenseNumber: "MED-IND-2019-7721"
    },
    {
        id: 6,
        name: "Dr. Vivek Shah",
        specialization: "ENT Specialist",
        departmentId: 9,
        department: "ENT",
        qualification: "MBBS, MS (ENT)",
        experience: 11,
        fee: 650,
        availableDays: "Mon, Wed, Fri",
        emoji: "👨‍⚕️",
        avatarBg: "#2a1a2a",
        rating: 4.6,
        licenseNumber: "MED-IND-2014-6631"
    },
    {
        id: 7,
        name: "Dr. Kavita Iyer",
        specialization: "Dermatologist",
        departmentId: 6,
        department: "Dermatology",
        qualification: "MBBS, MD (Dermatology)",
        experience: 7,
        fee: 700,
        availableDays: "Tue, Thu, Sat",
        emoji: "👩‍⚕️",
        avatarBg: "#1a3a2a",
        rating: 4.7,
        licenseNumber: "MED-IND-2020-8811"
    },
    {
        id: 8,
        name: "Dr. Arjun Nair",
        specialization: "Ophthalmologist",
        departmentId: 7,
        department: "Ophthalmology",
        qualification: "MBBS, MS (Ophthalmology)",
        experience: 10,
        fee: 680,
        availableDays: "Mon, Tue, Wed, Sat",
        emoji: "👨‍⚕️",
        avatarBg: "#3a1a1a",
        rating: 4.8,
        licenseNumber: "MED-IND-2016-4421"
    }
];

const TIME_SLOTS = [
    { time: "09:00 AM", label: "09:00 AM" },
    { time: "09:30 AM", label: "09:30 AM" },
    { time: "10:00 AM", label: "10:00 AM" },
    { time: "10:30 AM", label: "10:30 AM" },
    { time: "11:00 AM", label: "11:00 AM" },
    { time: "11:30 AM", label: "11:30 AM" },
    { time: "12:00 PM", label: "12:00 PM" },
    { time: "02:00 PM", label: "02:00 PM" },
    { time: "02:30 PM", label: "02:30 PM" },
    { time: "03:00 PM", label: "03:00 PM" },
    { time: "03:30 PM", label: "03:30 PM" },
    { time: "04:00 PM", label: "04:00 PM" },
    { time: "04:30 PM", label: "04:30 PM" },
    { time: "05:00 PM", label: "05:00 PM" },
    { time: "05:30 PM", label: "05:30 PM" },
    { time: "06:00 PM", label: "06:00 PM" }
];

// Simulated booked appointments (would come from DB in real integration)
const BOOKED_APPOINTMENTS = [
    {
        token: "SC-2024-0001",
        patientName: "Rahul Mehta",
        phone: "9811223344",
        doctorId: 1,
        doctorName: "Dr. Rajesh Sharma",
        department: "Cardiology",
        date: "2024-10-15",
        time: "10:00 AM",
        reason: "Chest pain & palpitations",
        status: "CONFIRMED"
    },
    {
        token: "SC-2024-0002",
        patientName: "Aarav Gupta",
        phone: "9822334455",
        doctorId: 2,
        doctorName: "Dr. Priya Patel",
        department: "Neurology",
        date: "2024-10-14",
        time: "11:00 AM",
        reason: "Recurring headaches",
        status: "SCHEDULED"
    },
    {
        token: "SC-2024-0003",
        patientName: "Kavita Iyer",
        phone: "9833445566",
        doctorId: 4,
        doctorName: "Dr. Sunita Rao",
        department: "General Medicine",
        date: "2024-10-13",
        time: "09:30 AM",
        reason: "Fever & body ache",
        status: "COMPLETED"
    }
];
