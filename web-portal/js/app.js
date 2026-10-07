/**
 * SmartCare Hospital – Patient Booking Portal
 * Main Application Logic
 */

// ─── State ────────────────────────────────────────────────────────────────────
const state = {
    currentStep: 1,
    selectedDeptId: null,
    selectedDoctorId: null,
    selectedDate: null,
    selectedTime: null,
    bookings: JSON.parse(localStorage.getItem('sc_bookings') || '[]'),
    tokenCounter: parseInt(localStorage.getItem('sc_token_counter') || '10'),
    activeFilter: 'all'
};

// ─── DOM Ready ─────────────────────────────────────────────────────────────────
// ─── DOM Ready ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    initNavbar();
    initDateLimits();
    await loadServerData();
    renderDepartments();
    renderDoctors();
    renderDeptSelectorGrid();
    populateDoctorFilters();
    animateStats();
    simulateCrowdIndicator();
});

async function loadServerData() {
    try {
        const [deptRes, docRes] = await Promise.all([
            fetch('/api/departments').catch(() => null),
            fetch('/api/doctors').catch(() => null)
        ]);
        if (deptRes && deptRes.ok) {
            const depts = await deptRes.json();
            if (Array.isArray(depts) && depts.length > 0) {
                depts.forEach(d => {
                    const existing = DEPARTMENTS.find(ed => ed.id === d.id);
                    if (existing) {
                        existing.name = d.name;
                        existing.head = d.head || existing.head;
                    } else {
                        DEPARTMENTS.push({
                            id: d.id,
                            name: d.name,
                            emoji: "🏥",
                            desc: d.description || "",
                            doctorCount: 1,
                            head: d.head || "Specialist",
                            bookingAvail: true
                        });
                    }
                });
            }
        }
        if (docRes && docRes.ok) {
            const docs = await docRes.json();
            if (Array.isArray(docs) && docs.length > 0) {
                docs.forEach(d => {
                    const existing = DOCTORS.find(ed => ed.id === d.id);
                    if (existing) {
                        existing.name = d.name;
                        existing.specialization = d.specialization;
                        existing.fee = d.fee || existing.fee;
                        existing.departmentId = d.departmentId;
                        existing.department = d.departmentName || existing.department;
                    } else {
                        DOCTORS.push({
                            id: d.id,
                            name: d.name,
                            specialization: d.specialization,
                            departmentId: d.departmentId,
                            department: d.departmentName || "General Medicine",
                            qualification: d.qualification || "MBBS",
                            experience: d.experience || 5,
                            fee: d.fee || 500,
                            availableDays: d.availableDays || "Mon-Fri",
                            emoji: "👨‍⚕️",
                            rating: 4.8
                        });
                    }
                });
            }
        }
    } catch (e) {
        console.warn('Backend API unavailable, using offline mock data:', e);
    }
}

// ─── NAVBAR ───────────────────────────────────────────────────────────────────
function initNavbar() {
    const navbar = document.getElementById('navbar');
    const hamburger = document.getElementById('hamburger');
    const navLinks = document.getElementById('nav-links');

    window.addEventListener('scroll', () => {
        navbar.classList.toggle('scrolled', window.scrollY > 40);
        updateActiveNavLink();
    });

    hamburger.addEventListener('click', () => {
        navLinks.classList.toggle('open');
    });

    document.querySelectorAll('.nav-link').forEach(link => {
        link.addEventListener('click', () => navLinks.classList.remove('open'));
    });
}

function updateActiveNavLink() {
    const sections = ['home', 'departments', 'doctors', 'book', 'track'];
    let current = 'home';
    sections.forEach(id => {
        const el = document.getElementById(id);
        if (el && window.scrollY >= el.offsetTop - 100) current = id;
    });
    document.querySelectorAll('.nav-link').forEach(link => {
        link.classList.toggle('active', link.dataset.section === current);
    });
}

// ─── STAT COUNTER ANIMATION ───────────────────────────────────────────────────
function animateStats() {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                const el = entry.target;
                const target = parseInt(el.dataset.target, 10);
                animateCount(el, 0, target, 1600);
                observer.unobserve(el);
            }
        });
    }, { threshold: 0.6 });

    document.querySelectorAll('.stat-number').forEach(el => observer.observe(el));
}

function animateCount(el, start, end, duration) {
    const range = end - start;
    const startTime = performance.now();
    const suffix = el.closest('.stat-item')?.querySelector('.stat-label')?.textContent.includes('%') ? '%' : '+';
    const requestAnim = (timestamp) => {
        const elapsed = timestamp - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const eased = 1 - Math.pow(1 - progress, 3);
        el.textContent = Math.round(start + range * eased).toLocaleString() + (progress < 1 || end < 100 ? '' : suffix);
        if (progress < 1) requestAnimationFrame(requestAnim);
        else el.textContent = end.toLocaleString() + suffix;
    };
    requestAnimationFrame(requestAnim);
}

// ─── DATE LIMITS ──────────────────────────────────────────────────────────────
function initDateLimits() {
    const apptDate = document.getElementById('appt-date');
    if (!apptDate) return;
    const today = new Date();
    const maxDate = new Date(today);
    maxDate.setDate(today.getDate() + 30);
    apptDate.min = today.toISOString().split('T')[0];
    apptDate.max = maxDate.toISOString().split('T')[0];
}

// ─── RENDER DEPARTMENTS ───────────────────────────────────────────────────────
function renderDepartments() {
    const grid = document.getElementById('departments-grid');
    if (!grid) return;
    grid.innerHTML = DEPARTMENTS.map(dept => `
        <a href="#book" class="dept-card" onclick="selectDeptAndScroll(${dept.id})" id="dept-card-${dept.id}">
            <div class="dept-emoji">${dept.emoji}</div>
            <div class="dept-name">${dept.name}</div>
            <div class="dept-count">${dept.doctorCount} Specialist${dept.doctorCount !== 1 ? 's' : ''}</div>
            <div class="dept-desc">${dept.desc}</div>
        </a>
    `).join('');
}

function selectDeptAndScroll(deptId) {
    selectDepartment(deptId);
    goToStep(1);
    setTimeout(() => {
        document.getElementById('book').scrollIntoView({ behavior: 'smooth' });
    }, 100);
}

// ─── RENDER DEPT SELECTOR IN FORM ─────────────────────────────────────────────
function renderDeptSelectorGrid() {
    const grid = document.getElementById('dept-selector-grid');
    if (!grid) return;
    grid.innerHTML = DEPARTMENTS.map(dept => `
        <div class="dept-selector-item" id="ds-${dept.id}" onclick="selectDepartment(${dept.id})">
            <div class="ds-emoji">${dept.emoji}</div>
            <div class="ds-name">${dept.name}</div>
        </div>
    `).join('');
}

function selectDepartment(deptId) {
    state.selectedDeptId = deptId;
    state.selectedDoctorId = null;
    state.selectedDate = null;
    state.selectedTime = null;

    document.querySelectorAll('.dept-selector-item').forEach(el => el.classList.remove('selected'));
    const item = document.getElementById(`ds-${deptId}`);
    if (item) item.classList.add('selected');

    document.getElementById('step1-next').disabled = false;
    renderDoctorSelector();
    updateSummary();
}

// ─── RENDER DOCTORS (section) ─────────────────────────────────────────────────
function populateDoctorFilters() {
    const filterContainer = document.getElementById('doctors-filter');
    if (!filterContainer) return;
    const depts = [...new Set(DOCTORS.map(d => d.department))];
    depts.forEach(dept => {
        const btn = document.createElement('button');
        btn.className = 'filter-btn';
        btn.dataset.filter = dept;
        btn.textContent = dept;
        btn.onclick = () => filterDoctors(dept, btn);
        filterContainer.appendChild(btn);
    });
    renderDoctors('all');
}

function filterDoctors(filter, clickedBtn) {
    state.activeFilter = filter;
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    if (clickedBtn) clickedBtn.classList.add('active');
    renderDoctors(filter);
}

function renderDoctors(filter) {
    const grid = document.getElementById('doctors-grid');
    if (!grid) return;
    const f = filter || state.activeFilter;
    const filtered = f === 'all' ? DOCTORS : DOCTORS.filter(d => d.department === f);

    grid.innerHTML = filtered.map(doc => `
        <div class="doctor-card" id="doc-card-${doc.id}">
            <div class="doctor-avatar" style="background:${doc.avatarBg};">
                <span style="font-size:72px;">${doc.emoji}</span>
            </div>
            <div class="doctor-body">
                <div class="doctor-name">${doc.name}</div>
                <div class="doctor-spec">${doc.specialization}</div>
                <div class="doctor-dept">🏥 ${doc.department}</div>
                <div class="doctor-meta">
                    <span class="doctor-meta-item">🎓 ${doc.qualification.split(',')[0]}</span>
                    <span class="doctor-meta-item">⭐ ${doc.rating}/5</span>
                    <span class="doctor-meta-item">🕐 ${doc.experience} yrs</span>
                </div>
                <div class="doctor-fee">₹${doc.fee.toLocaleString()} <span>/ consultation</span></div>
                <div class="doctor-meta-item" style="margin-bottom:14px;font-size:12px;color:var(--teal-400);">
                    📅 Available: ${doc.availableDays}
                </div>
                <button class="btn-book-doctor" onclick="selectDoctorAndScroll(${doc.id})">
                    Book Appointment
                </button>
            </div>
        </div>
    `).join('');
}

function selectDoctorAndScroll(docId) {
    const doc = DOCTORS.find(d => d.id === docId);
    if (!doc) return;
    state.selectedDeptId = doc.departmentId;
    selectDoctorInForm(docId);
    goToStep(2);
    setTimeout(() => {
        document.getElementById('book').scrollIntoView({ behavior: 'smooth' });
    }, 100);
}

// ─── RENDER DOCTOR SELECTOR IN FORM ──────────────────────────────────────────
function renderDoctorSelector() {
    const grid = document.getElementById('doctor-selector-grid');
    if (!grid) return;
    const docs = state.selectedDeptId
        ? DOCTORS.filter(d => d.departmentId === state.selectedDeptId)
        : DOCTORS;

    if (docs.length === 0) {
        grid.innerHTML = `<div style="color:var(--gray-400);padding:20px;text-align:center;">No doctors available for this department.</div>`;
        return;
    }

    grid.innerHTML = docs.map(doc => `
        <div class="doctor-selector-item" id="dsi-${doc.id}" onclick="selectDoctorInForm(${doc.id})">
            <div class="dsi-avatar">${doc.emoji}</div>
            <div class="dsi-name">${doc.name}</div>
            <div class="dsi-spec">${doc.specialization}</div>
            <div class="dsi-fee">₹${doc.fee.toLocaleString()} <small>/ visit</small></div>
            <div style="font-size:11px;color:var(--gray-400);margin-top:6px;">⭐ ${doc.rating} • ${doc.experience}y exp</div>
        </div>
    `).join('');
}

function selectDoctorInForm(docId) {
    state.selectedDoctorId = docId;
    state.selectedDate = null;
    state.selectedTime = null;

    document.querySelectorAll('.doctor-selector-item').forEach(el => el.classList.remove('selected'));
    const item = document.getElementById(`dsi-${docId}`);
    if (item) item.classList.add('selected');

    document.getElementById('step2-next').disabled = false;
    updateSummary();
}

// ─── TIME SLOTS ───────────────────────────────────────────────────────────────
function loadTimeSlots() {
    const dateEl = document.getElementById('appt-date');
    const grid = document.getElementById('time-slots-grid');
    const date = dateEl.value;
    state.selectedDate = date;
    state.selectedTime = null;
    document.getElementById('step3-next').disabled = true;

    if (!date) { grid.innerHTML = '<div class="timeslot-placeholder">Please select a date first</div>'; return; }

    // Simulate some slots being booked (random based on date + doctor)
    const bookedSlotIndexes = getSimulatedBookedSlots(date, state.selectedDoctorId);

    grid.innerHTML = TIME_SLOTS.map((slot, idx) => {
        const isBooked = bookedSlotIndexes.includes(idx);
        return `<button class="timeslot-btn ${isBooked ? 'booked' : ''}"
            onclick="${isBooked ? '' : `selectTimeSlot('${slot.time}', this)`}"
            ${isBooked ? 'disabled title="Already booked"' : ''}>
            ${slot.label}
        </button>`;
    }).join('');

    updateSummary();
}

function getSimulatedBookedSlots(date, doctorId) {
    const seed = (date.split('-').join('') + (doctorId || 1)).toString();
    const booked = [];
    for (let i = 0; i < seed.length; i++) {
        const idx = (parseInt(seed[i], 16) * 3 + i) % TIME_SLOTS.length;
        if (!booked.includes(idx)) booked.push(idx);
    }
    return booked.slice(0, 4); // simulate 4 booked slots
}

function selectTimeSlot(time, btn) {
    document.querySelectorAll('.timeslot-btn').forEach(b => b.classList.remove('selected'));
    btn.classList.add('selected');
    state.selectedTime = time;
    document.getElementById('step3-next').disabled = false;
    updateSummary();
}

// ─── STEP NAVIGATION ──────────────────────────────────────────────────────────
function goToStep(step) {
    if (step === 2 && !state.selectedDeptId) { showToast('Please select a department first', 'error'); return; }
    if (step === 3 && !state.selectedDoctorId) { showToast('Please select a doctor first', 'error'); return; }
    if (step === 4 && (!state.selectedDate || !state.selectedTime)) { showToast('Please select a date and time slot', 'error'); return; }

    state.currentStep = step;

    document.querySelectorAll('.form-step').forEach(el => el.classList.remove('active'));
    document.getElementById(`step-${step}`).classList.add('active');

    // Update progress indicators
    document.querySelectorAll('.progress-step').forEach(el => {
        const s = parseInt(el.dataset.step);
        el.classList.toggle('active', s === step);
        el.classList.toggle('done', s < step);
    });
    document.querySelectorAll('.progress-line').forEach((line, idx) => {
        line.classList.toggle('done', idx < step - 1);
    });

    // Populate doctor selector when entering step 2
    if (step === 2) renderDoctorSelector();

    updateSummary();

    // Scroll form card into view
    document.getElementById('booking-form-card').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

// ─── STEP 4 VALIDATION ────────────────────────────────────────────────────────
function validateStep4() {
    const confirmBtn = document.getElementById('step4-confirm');
    if (confirmBtn) {
        confirmBtn.disabled = false;
    }
}

// ─── CONFIRM BOOKING ──────────────────────────────────────────────────────────
async function confirmBooking() {
    const doctor = DOCTORS.find(d => d.id == state.selectedDoctorId) || DOCTORS[0];
    const dept = DEPARTMENTS.find(d => d.id == state.selectedDeptId) || DEPARTMENTS[0];
    if (!doctor || !dept) {
        showToast('⚠️ Please select a department and doctor first.', 'error');
        goToStep(1);
        return;
    }

    const nameEl = document.getElementById('patient-name');
    const phoneEl = document.getElementById('patient-phone');
    const emailEl = document.getElementById('patient-email');
    const dobEl = document.getElementById('patient-dob');
    const genderEl = document.getElementById('patient-gender');
    const reasonEl = document.getElementById('patient-reason');
    const codeEl = document.getElementById('patient-code');
    const termsEl = document.getElementById('terms-checkbox');

    const patientName = nameEl ? nameEl.value.trim() : '';
    let rawPhone = phoneEl ? phoneEl.value.trim() : '';
    let phone = rawPhone.replace(/\D/g, '');
    if (phone.length === 12 && phone.startsWith('91')) {
        phone = phone.substring(2);
    }
    const email = emailEl ? emailEl.value.trim() : '';
    let dob = dobEl && dobEl.value ? dobEl.value : '1995-01-01';
    let gender = genderEl && genderEl.value ? genderEl.value : 'MALE';
    let reason = reasonEl && reasonEl.value.trim() ? reasonEl.value.trim() : 'General Health Checkup & OPD Consultation';
    const patientCode = codeEl ? codeEl.value.trim() : '';
    const terms = termsEl ? termsEl.checked : true;

    if (!patientName || patientName.length < 2) {
        showToast('⚠️ Please enter the patient full name.', 'warning');
        if (nameEl) nameEl.focus();
        return;
    }
    if (!phone || phone.length < 10) {
        showToast('⚠️ Please enter a valid 10-digit mobile number.', 'warning');
        if (phoneEl) phoneEl.focus();
        return;
    }
    if (!terms) {
        showToast('⚠️ Please accept the terms to confirm your booking.', 'warning');
        if (termsEl) termsEl.focus();
        return;
    }

    if (!state.selectedDate) {
        state.selectedDate = new Date().toISOString().split('T')[0];
    }
    if (!state.selectedTime) {
        state.selectedTime = '10:00 AM';
    }

    const confirmBtn = document.getElementById('step4-confirm');
    const origText = confirmBtn ? confirmBtn.innerHTML : '';
    if (confirmBtn) {
        confirmBtn.disabled = true;
        confirmBtn.innerHTML = '<span>⏳</span> Processing Ticket & Booking...';
    }

    state.tokenCounter += 1;
    let token = `SC-${new Date().getFullYear()}-${String(state.tokenCounter).padStart(4, '0')}`;
    let queueNumber = state.tokenCounter;
    let appointmentId = state.tokenCounter;

    // Send to backend API so it directly saves to MySQL
    try {
        const payload = {
            patientName,
            phone,
            email: email || undefined,
            dob,
            gender,
            reason,
            doctorId: doctor.id,
            date: state.selectedDate,
            time: state.selectedTime,
            patientCode: patientCode || undefined
        };
        const res = await fetch('/api/book', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        if (res.ok) {
            const data = await res.json();
            if (data.token) token = data.token;
            if (data.queueNumber) queueNumber = data.queueNumber;
            if (data.appointmentId) appointmentId = data.appointmentId;
        } else {
            const err = await res.json().catch(() => ({}));
            if (err.error) {
                showToast(`⚠️ Server note: ${err.error}`, 'warning');
            }
        }
    } catch (err) {
        console.warn('API call failed, proceeding with local ticket generation:', err);
    } finally {
        if (confirmBtn) {
            confirmBtn.disabled = false;
            confirmBtn.innerHTML = origText;
        }
    }

    const booking = {
        token,
        queueNumber,
        appointmentId,
        patientName,
        phone,
        email,
        dob,
        gender,
        reason,
        patientCode: patientCode || null,
        doctorId: doctor.id,
        doctorName: doctor.name,
        specialization: doctor.specialization,
        department: dept.name,
        date: state.selectedDate,
        time: state.selectedTime,
        fee: doctor.fee,
        status: "SCHEDULED",
        bookedAt: new Date().toISOString()
    };

    // Save locally
    state.bookings.push(booking);
    localStorage.setItem('sc_bookings', JSON.stringify(state.bookings));
    localStorage.setItem('sc_token_counter', state.tokenCounter);

    // Render confirmation in step 5
    renderConfirmationTicket(booking);
    goToStepFive();

    showToast(`🎫 Booking confirmed! Token: ${token}`, 'success');
}

function goToStepFive() {
    state.currentStep = 5;
    document.querySelectorAll('.form-step').forEach(el => el.classList.remove('active'));
    document.getElementById('step-5').classList.add('active');
    document.querySelectorAll('.progress-step').forEach(el => el.classList.add('done'));
    document.getElementById('booking-form-card').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function renderConfirmationTicket(booking) {
    const dateFormatted = formatDate(booking.date);
    const step5 = document.getElementById('step-5');
    const barcode = generateBarcode();

    step5.innerHTML = `
        <div class="ticket-container">
            <div class="ticket-success-icon">✅</div>
            <div class="ticket-success-title">Appointment Confirmed!</div>
            <div class="ticket-success-sub">Your digital ticket has been generated. Present this token at the hospital reception.</div>

            <div class="ticket-card" id="printable-ticket">
                <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:16px;">
                    <div>
                        <div style="font-size:11px;color:var(--gray-400);font-weight:600;text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Your Token</div>
                        <div class="ticket-token">${booking.token}</div>
                    </div>
                    <div style="text-align:right;">
                        <div style="font-size:11px;color:var(--gray-400);font-weight:600;text-transform:uppercase;letter-spacing:1px;margin-bottom:4px;">Queue No.</div>
                        <div style="font-family:var(--font-display);font-size:36px;font-weight:900;color:var(--teal-400);">#${booking.queueNumber}</div>
                    </div>
                </div>

                <div class="ticket-details">
                    <div class="ticket-detail-item">
                        <div class="td-key">Patient</div>
                        <div class="td-val">${booking.patientName}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Phone</div>
                        <div class="td-val">${booking.phone}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Doctor</div>
                        <div class="td-val">${booking.doctorName}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Specialization</div>
                        <div class="td-val">${booking.specialization}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Department</div>
                        <div class="td-val">${booking.department}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Consultation Fee</div>
                        <div class="td-val text-teal">₹${booking.fee.toLocaleString()}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Date</div>
                        <div class="td-val">${dateFormatted}</div>
                    </div>
                    <div class="ticket-detail-item">
                        <div class="td-key">Time Slot</div>
                        <div class="td-val">${booking.time}</div>
                    </div>
                </div>

                <div class="ticket-detail-item" style="margin-bottom:16px;">
                    <div class="td-key">Reason</div>
                    <div class="td-val">${booking.reason}</div>
                </div>

                <div style="background:rgba(255,255,255,.04);border-radius:10px;padding:14px;display:flex;align-items:center;justify-content:space-between;gap:12px;">
                    <div>
                        <div style="font-size:10px;color:var(--gray-400);text-transform:uppercase;letter-spacing:.8px;font-weight:600;margin-bottom:4px;">Booked At</div>
                        <div style="font-size:13px;color:var(--white);font-weight:600;">${formatDateTime(booking.bookedAt)}</div>
                    </div>
                    <div class="ticket-barcode">${barcode}</div>
                </div>

                <div class="ticket-footer-note">
                    ⚠️ Please arrive 15 minutes before your scheduled time.<br>
                    Carry a valid ID and your Patient Code (if applicable) to the reception.
                </div>
            </div>

            <div style="display:flex;gap:12px;justify-content:center;flex-wrap:wrap;margin-bottom:12px;">
                <button class="btn-print" onclick="printTicket()">🖨️ Print Ticket</button>
                <button class="btn-new-booking" onclick="startNewBooking()">+ Book Another Appointment</button>
            </div>
        </div>
    `;
}

function generateBarcode() {
    const heights = [40,28,40,32,48,28,40,36,44,28,40,32,48,36,40,28,44,40,28,40,32];
    return heights.map(h =>
        `<div class="barcode-line" style="height:${h}px;opacity:${0.5 + Math.random()*0.5};"></div>`
    ).join('');
}

// ─── NEW BOOKING ──────────────────────────────────────────────────────────────
function startNewBooking() {
    state.selectedDeptId = null;
    state.selectedDoctorId = null;
    state.selectedDate = null;
    state.selectedTime = null;
    state.currentStep = 1;

    document.getElementById('patient-name').value = '';
    document.getElementById('patient-phone').value = '';
    document.getElementById('patient-email').value = '';
    document.getElementById('patient-dob').value = '';
    document.getElementById('patient-gender').value = '';
    document.getElementById('patient-reason').value = '';
    document.getElementById('patient-code').value = '';
    document.getElementById('terms-checkbox').checked = false;
    document.getElementById('step4-confirm').disabled = true;

    renderDeptSelectorGrid();
    goToStep(1);
    updateSummary();
    document.getElementById('book').scrollIntoView({ behavior: 'smooth' });
}

// ─── BOOKING SUMMARY SIDEBAR ──────────────────────────────────────────────────
function updateSummary() {
    const container = document.getElementById('summary-items');
    const dept = DEPARTMENTS.find(d => d.id === state.selectedDeptId);
    const doc = DOCTORS.find(d => d.id === state.selectedDoctorId);

    if (!dept) {
        container.innerHTML = `
            <div class="summary-placeholder">
                <div class="placeholder-icon">📋</div>
                <p>Your booking details will appear here as you complete each step.</p>
            </div>`;
        return;
    }

    let rows = [];
    if (dept) rows.push({ key: 'Department', val: `${dept.emoji} ${dept.name}` });
    if (doc) rows.push({ key: 'Doctor', val: doc.name });
    if (doc) rows.push({ key: 'Specialization', val: doc.specialization });
    if (doc) rows.push({ key: 'Consultation Fee', val: `₹${doc.fee.toLocaleString()}` });
    if (state.selectedDate) rows.push({ key: 'Date', val: formatDate(state.selectedDate) });
    if (state.selectedTime) rows.push({ key: 'Time', val: state.selectedTime });

    container.innerHTML = rows.map(r => `
        <div class="summary-row">
            <span class="summary-key">${r.key}</span>
            <span class="summary-val">${r.val}</span>
        </div>
    `).join('');
}

// ─── CROWD INDICATOR ──────────────────────────────────────────────────────────
function simulateCrowdIndicator() {
    const hour = new Date().getHours();
    let pct, label, color;
    if (hour >= 9 && hour < 11) { pct = 85; label = '🔴 High — Peak OPD Hours'; color = '#ef4444'; }
    else if (hour >= 11 && hour < 13) { pct = 72; label = '🟠 Moderate — Busy Period'; color = '#f59e0b'; }
    else if (hour >= 13 && hour < 14) { pct = 35; label = '🟡 Low — Lunch Break'; color = '#eab308'; }
    else if (hour >= 14 && hour < 17) { pct = 60; label = '🟠 Moderate — Afternoon OPD'; color = '#f59e0b'; }
    else if (hour >= 17 && hour < 19) { pct = 45; label = '🟢 Moderate — Evening OPD'; color = '#10b981'; }
    else { pct = 10; label = '🟢 Low — Off-Peak Hours'; color = '#10b981'; }

    setTimeout(() => {
        const bar = document.getElementById('crowd-bar');
        const status = document.getElementById('crowd-status');
        if (bar) { bar.style.width = pct + '%'; bar.style.background = color; }
        if (status) { status.textContent = label; status.style.color = color; }
    }, 800);
}

// ─── TRACK APPOINTMENT ────────────────────────────────────────────────────────
function switchTrackTab(type) {
    document.getElementById('track-token-group').classList.toggle('active', type === 'token');
    document.getElementById('track-phone-group').classList.toggle('active', type === 'phone');
    document.getElementById('tab-token').classList.toggle('active', type === 'token');
    document.getElementById('tab-phone').classList.toggle('active', type === 'phone');
    document.getElementById('track-result').innerHTML = '';
}

async function trackAppointment(type) {
    const resultEl = document.getElementById('track-result');
    const query = type === 'token'
        ? document.getElementById('token-input').value.trim().toUpperCase()
        : document.getElementById('phone-input').value.trim();

    if (!query) { showToast('Please enter a search value', 'error'); return; }

    resultEl.innerHTML = '<div style="text-align:center;padding:24px;color:var(--gray-400);font-size:14px;">🔍 Searching appointments...</div>';

    // 1. Try querying live backend server
    try {
        const param = type === 'token' ? `token=${encodeURIComponent(query)}` : `phone=${encodeURIComponent(query)}`;
        const res = await fetch(`/api/track?${param}`);
        if (res.ok) {
            const data = await res.json();
            if (Array.isArray(data) && data.length > 0) {
                resultEl.innerHTML = data.map(b => renderTrackResult(b)).join('');
                return;
            }
        }
    } catch (err) {
        console.warn('API track request failed, searching local data:', err);
    }

    // 2. Fallback to local bookings + demo bookings
    const allBookings = [...BOOKED_APPOINTMENTS, ...state.bookings];
    let found = null;

    if (type === 'token') found = allBookings.find(b => b.token && b.token.toUpperCase() === query);
    else found = allBookings.filter(b => b.phone === query);

    if (type === 'phone' && Array.isArray(found) && found.length > 0) {
        resultEl.innerHTML = found.map(b => renderTrackResult(b)).join('');
    } else if (type === 'token' && found) {
        resultEl.innerHTML = renderTrackResult(found);
    } else {
        resultEl.innerHTML = `
            <div class="track-not-found">
                <div class="track-not-found-title">❌ No Appointment Found</div>
                <p style="font-size:13px;color:var(--gray-400);margin-top:8px;">
                    No appointment found for the provided ${type === 'token' ? 'token' : 'phone number'}. 
                    Please check and try again, or <a href="#book" style="color:var(--teal-400);">book a new appointment</a>.
                </p>
            </div>`;
    }
}

function renderTrackResult(b) {
    const statusClass = {
        'SCHEDULED': 'status-scheduled',
        'CONFIRMED': 'status-confirmed',
        'COMPLETED': 'status-completed',
        'CANCELLED': 'status-cancelled'
    }[b.status] || 'status-scheduled';

    return `
        <div class="track-found">
            <div class="track-found-title">✅ Appointment Found</div>
            <div class="track-detail-grid">
                <div class="track-detail-item"><div class="tdi-key">Token</div><div class="tdi-val" style="color:var(--teal-400);font-size:15px;font-weight:800;">${b.token}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Status</div><div class="tdi-val"><span class="status-badge ${statusClass}">${b.status}</span></div></div>
                <div class="track-detail-item"><div class="tdi-key">Patient</div><div class="tdi-val">${b.patientName}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Doctor</div><div class="tdi-val">${b.doctorName}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Department</div><div class="tdi-val">${b.department}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Date</div><div class="tdi-val">${formatDate(b.date)}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Time</div><div class="tdi-val">${b.time}</div></div>
                <div class="track-detail-item"><div class="tdi-key">Reason</div><div class="tdi-val">${b.reason}</div></div>
            </div>
            ${b.queueNumber ? `<div style="margin-top:14px;text-align:center;padding:12px;background:rgba(14,203,214,.08);border-radius:10px;border:1px solid rgba(14,203,214,.2);">
                <div style="font-size:11px;color:var(--gray-400);font-weight:600;text-transform:uppercase;letter-spacing:1px;">Queue Number</div>
                <div style="font-family:var(--font-display);font-size:32px;font-weight:900;color:var(--teal-400);">#${b.queueNumber}</div>
            </div>` : ''}
        </div>`;
}

// ─── MODAL ────────────────────────────────────────────────────────────────────
function closeTicketModal() {
    document.getElementById('ticket-modal-overlay').classList.remove('open');
}

// ─── PRINT ────────────────────────────────────────────────────────────────────
function printTicket() {
    window.print();
}

// ─── TOAST ────────────────────────────────────────────────────────────────────
let toastTimeout;
function showToast(message, type = '') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    clearTimeout(toastTimeout);
    toastTimeout = setTimeout(() => {
        toast.classList.remove('show');
    }, 3400);
}

// ─── HELPERS ──────────────────────────────────────────────────────────────────
function formatDate(dateStr) {
    if (!dateStr) return '—';
    const d = new Date(dateStr + 'T00:00:00');
    return d.toLocaleDateString('en-IN', { weekday: 'short', day: '2-digit', month: 'long', year: 'numeric' });
}

function formatDateTime(isoStr) {
    if (!isoStr) return '—';
    const d = new Date(isoStr);
    return d.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' });
}
