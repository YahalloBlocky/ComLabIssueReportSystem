const f = document.getElementById('f'), msg = document.getElementById('msg');
f.addEventListener('submit', async e => {
  e.preventDefault(); hideMsg(msg); showFieldErrors(f, null);
  const b = { role: f.role.value, schoolId: f.schoolId.value.trim(), firstName: f.firstName.value.trim(),
    lastName: f.lastName.value.trim(), email: f.email.value.trim(), password: f.password.value };
  const errs = {};
  if (!/^[A-Za-z0-9-]{4,20}$/.test(b.schoolId)) errs.schoolId = 'Use 4 to 20 letters, numbers or dashes.';
  if (!b.firstName) errs.firstName = 'First name is required.';
  if (!b.lastName) errs.lastName = 'Last name is required.';
  if (!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(b.email)) errs.email = 'Enter a valid email address.';
  if (b.password.length < 8) errs.password = 'Use at least 8 characters.';
  if (Object.keys(errs).length) return showFieldErrors(f, errs);
  try {
    const me = await api('POST', '/api/auth/register', b);
    location.href = me.role === 'Admin' ? '/admin.html' : '/reporter.html';
  } catch (err) { showMsg(msg, 'error', err.message); showFieldErrors(f, err.fieldErrors); }
});
