import { useEffect, useMemo, useState } from 'react';

const API = (import.meta.env.VITE_API_BASE || 'http://localhost:8080').replace(/\/$/, '');
const specialties = ['All specialties', 'Cardiology', 'Dermatology', 'Family Medicine', 'Neurology', 'Orthopedics', 'Pediatrics'];
const accents = ['sage', 'clay', 'sky', 'olive', 'rose', 'slate'];
const initials = (name = '') => name.replace('Dr. ', '').split(' ').map((part) => part[0]).slice(0, 2).join('');
const dateFromToday = (days) => { const date = new Date(); date.setDate(date.getDate() + days); return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`; };

async function request(path, options) {
  const response = await fetch(`${API}/api/v1${path}`, { ...options, headers: { 'Content-Type': 'application/json', ...options?.headers } });
  const payload = response.status === 204 ? null : await response.json().catch(() => null);
  if (!response.ok) throw new Error(payload?.message || payload?.error || 'The scheduling service could not complete that request.');
  return payload;
}

function App() {
  const [doctors, setDoctors] = useState([]);
  const [status, setStatus] = useState('loading');
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [specialty, setSpecialty] = useState('All specialties');
  const [selected, setSelected] = useState(null);
  const [date, setDate] = useState(dateFromToday(1));
  const [slots, setSlots] = useState([]);
  const [chosenSlot, setChosenSlot] = useState('');
  const [loadingSlots, setLoadingSlots] = useState(false);
  const [booking, setBooking] = useState(false);
  const [receipt, setReceipt] = useState(null);
  const [bookingError, setBookingError] = useState('');
  const [lookup, setLookup] = useState('');
  const [lookupResult, setLookupResult] = useState(null);
  const [lookupError, setLookupError] = useState('');
  const [lookupBusy, setLookupBusy] = useState(false);

  async function loadDoctors() {
    setStatus('loading'); setError('');
    try { setDoctors(await request('/doctors')); setStatus('ready'); }
    catch (issue) { setError(issue.message); setStatus('error'); }
  }
  useEffect(() => { loadDoctors(); }, []);
  const visibleDoctors = useMemo(() => doctors.filter((doctor) => (specialty === 'All specialties' || doctor.specialty === specialty) && `${doctor.name} ${doctor.specialty} ${doctor.clinic} ${doctor.location}`.toLowerCase().includes(search.trim().toLowerCase())), [doctors, specialty, search]);

  useEffect(() => {
    if (!selected || receipt) return;
    let ignore = false;
    setLoadingSlots(true); setSlots([]); setChosenSlot(''); setBookingError('');
    request(`/doctors/${selected.id}/slots?date=${date}`).then((items) => { if (!ignore) setSlots(items); }).catch((issue) => { if (!ignore) setBookingError(issue.message); }).finally(() => { if (!ignore) setLoadingSlots(false); });
    return () => { ignore = true; };
  }, [selected, date, receipt]);

  function openBooking(doctor) { setSelected(doctor); setDate(dateFromToday(1)); setReceipt(null); setBookingError(''); }
  function closeDialog() { setSelected(null); setReceipt(null); }
  async function submitBooking(event) {
    event.preventDefault(); if (!selected || !chosenSlot) return;
    setBooking(true); setBookingError('');
    try { setReceipt(await request('/appointments', { method: 'POST', body: JSON.stringify({ doctorId: selected.id, date, time: chosenSlot }) })); }
    catch (issue) { setBookingError(issue.message); }
    finally { setBooking(false); }
  }
  async function checkBooking(event) {
    event.preventDefault(); setLookupBusy(true); setLookupError(''); setLookupResult(null);
    try { setLookupResult(await request(`/appointments/${encodeURIComponent(lookup.trim())}`)); }
    catch (issue) { setLookupError(issue.message); }
    finally { setLookupBusy(false); }
  }
  const money = (amount) => new Intl.NumberFormat(undefined, { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(amount);
  const prettyDate = (value) => new Intl.DateTimeFormat(undefined, { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric', timeZone: 'UTC' }).format(new Date(`${value}T12:00:00Z`));

  return <div className="app-shell">
    <header className="site-header"><a href="#home" className="brand"><span className="brand-symbol">c</span><span>careway<span className="brand-dot">.</span></span></a><nav aria-label="Main navigation"><a href="#doctors">Find a doctor</a><a href="#how-it-works">How it works</a></nav><a className="header-cta" href="#lookup">My booking <span>↗</span></a></header>
    <main id="home">
      <section className="hero"><div className="hero-copy"><p className="eyebrow"><span></span> CONSULTATION, MADE CLEAR</p><h1>Care that meets<br />you <em>where you are.</em></h1><p className="hero-description">Find the right specialist, see a time that works, and book a consultation in a few simple steps.</p><a className="primary-link" href="#doctors">Explore specialists <span>↗</span></a><div className="hero-note"><span>✳</span> Sample scheduling experience · No patient details collected</div></div><div className="hero-art" aria-label="Abstract illustration of a sun above a calm horizon"><div className="art-sun"></div><div className="art-orbit"></div><div className="art-horizon"></div><span className="art-index">CAREWAY / 01</span><span className="art-caption">A little more ease<br />in finding care.</span></div></section>
      <section id="doctors" className="directory"><div className="section-top"><div><p className="eyebrow">01 / MEET YOUR SPECIALIST</p><h2>Thoughtful care,<br /><em>one conversation at a time.</em></h2></div><div className="directory-count"><strong>{status === 'ready' ? String(doctors.length).padStart(2, '0') : '—'}</strong><span>sample specialists</span></div></div>
        <div className="filter-bar"><label className="search-field"><span>⌕</span><input type="search" value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search doctors, clinics or cities" aria-label="Search doctors, clinics or cities" /></label><label className="select-field"><span className="sr-only">Specialty</span><select value={specialty} onChange={(event) => setSpecialty(event.target.value)}>{specialties.map((item) => <option key={item}>{item}</option>)}</select></label></div>
        {status === 'loading' && <div className="service-state" role="status"><span className="spinner"></span>Connecting to the appointment service…</div>}
        {status === 'error' && <div className="service-state error" role="alert"><strong>The appointment service isn’t available.</strong><span>{error}</span><button className="text-action" onClick={loadDoctors}>Try again ↗</button><small>Start the backend from the backend folder to load the sample doctors.</small></div>}
        {status === 'ready' && visibleDoctors.length > 0 && <div className="doctor-grid">{visibleDoctors.map((doctor, index) => <article className="doctor-card" key={doctor.id}><div className={`doctor-art ${accents[index % accents.length]}`}><span className="doctor-initials">{initials(doctor.name)}</span><span className="art-star">✳</span><span className="doctor-no">SPECIALIST / {String(index + 1).padStart(2, '0')}</span></div><div className="doctor-content"><p className="doctor-specialty">{doctor.specialty}</p><h3>{doctor.name}</h3><p className="doctor-clinic">{doctor.clinic} <span>·</span> {doctor.location}</p><div className="doctor-meta"><span>{doctor.experienceYears} years’ experience</span><span>{money(doctor.consultationFee)} <small>/ consult</small></span></div><button className="book-button" onClick={() => openBooking(doctor)}>View appointment times <span>↗</span></button></div></article>)}</div>}
        {status === 'ready' && visibleDoctors.length === 0 && <div className="service-state">No specialists match those filters. Try a different search.</div>}
      </section>
      <section className="how-section" id="how-it-works"><div className="how-intro"><p className="eyebrow">02 / A SIMPLE WAY THROUGH</p><h2>From search<br />to <em>scheduled.</em></h2></div><div className="steps"><article><span>01</span><h3>Find your fit</h3><p>Browse sample specialties and clinic locations, then choose the doctor you’d like to see.</p></article><article><span>02</span><h3>Pick a time</h3><p>Only open appointment slots are shown. Availability is checked again when you book.</p></article><article><span>03</span><h3>Keep your reference</h3><p>Save the booking reference to look up the appointment later in this browser.</p></article></div></section>
      <section className="lookup-section" id="lookup"><div><p className="eyebrow">03 / ALREADY BOOKED?</p><h2>Find your <em>appointment.</em></h2><p>Enter the booking reference you received after scheduling.</p></div><form className="lookup-form" onSubmit={checkBooking}><label className="sr-only" htmlFor="booking-ref">Booking reference</label><input id="booking-ref" required value={lookup} onChange={(event) => setLookup(event.target.value)} placeholder="Paste your booking reference" /><button className="primary-link" disabled={lookupBusy}>{lookupBusy ? 'Looking…' : 'Find booking'} <span>↗</span></button>{lookupError && <p className="inline-error" role="alert">{lookupError}</p>}{lookupResult && <div className="lookup-result" role="status"><strong>{lookupResult.doctor}</strong><span>{prettyDate(lookupResult.date)} · {lookupResult.time}</span><small>Reference: {lookupResult.reference}</small></div>}</form></section>
    </main>
    <footer className="site-footer"><a href="#home" className="brand"><span className="brand-symbol">c</span><span>careway<span className="brand-dot">.</span></span></a><p>A consultation scheduling portfolio demo. Sample doctors only; not a healthcare provider or source of medical advice.</p><a href="#home">Back to top ↑</a></footer>
    {selected && <div className="dialog-backdrop" onMouseDown={(event) => { if (event.target === event.currentTarget) closeDialog(); }}><section className="booking-dialog" role="dialog" aria-modal="true" aria-labelledby="booking-title"><button className="dialog-close" onClick={closeDialog} aria-label="Close booking">×</button>{receipt ? <div className="receipt"><p className="eyebrow">YOUR APPOINTMENT IS RESERVED</p><div className="receipt-mark">✓</div><h2 id="booking-title">A time to look forward to.</h2><p>{receipt.doctor} · {receipt.specialty}</p><p>{prettyDate(receipt.date)} at {receipt.time}</p><div className="reference-box"><span>BOOKING REFERENCE</span><strong>{receipt.reference}</strong><small>Copy this reference to find the appointment later.</small></div><button className="primary-link" onClick={closeDialog}>Done <span>↗</span></button></div> : <form onSubmit={submitBooking}><p className="eyebrow">BOOK A CONSULTATION</p><h2 id="booking-title">{selected.name}</h2><p className="booking-specialty">{selected.specialty} <span>·</span> {selected.clinic}</p><label className="field-label" htmlFor="appointment-date">Choose a date</label><input id="appointment-date" type="date" min={dateFromToday(1)} max={dateFromToday(60)} value={date} onChange={(event) => setDate(event.target.value)} required /><div className="field-label">Available times</div>{loadingSlots ? <div className="slots-loading">Loading appointment times…</div> : <div className="slot-list">{slots.filter((slot) => slot.available).map((slot) => <button type="button" key={slot.time} className={`slot-button ${chosenSlot === slot.time ? 'selected' : ''}`} onClick={() => setChosenSlot(slot.time)}>{slot.time}</button>)}{!slots.some((slot) => slot.available) && <p className="no-slots">No open times on this date. Choose another day.</p>}</div>}{bookingError && <p className="inline-error" role="alert">{bookingError}</p>}<div className="fee-note"><span>Sample consultation fee</span><strong>{money(selected.consultationFee)}</strong></div><button className="primary-link full" type="submit" disabled={!chosenSlot || booking}>{booking ? 'Reserving…' : 'Reserve this time'} <span>↗</span></button><p className="privacy-copy">No name, email, or health information is collected in this demo.</p></form>}</section></div>}
  </div>;
}

export default App;
