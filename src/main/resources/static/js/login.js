const f = document.getElementById('f'), msg = document.getElementById('msg');
// Already logged in? go straight to the right page.
api('GET', '/api/auth/me').then(go).catch(() => {});
function go(me) { location.href = me.role === 'Admin' ? '/admin.html' : '/reporter.html'; }
f.addEventListener('submit', async e => {
  e.preventDefault(); hideMsg(msg); showFieldErrors(f, null);
  const schoolId = f.schoolId.value.trim(), password = f.password.value;
  const errs = {};
  if (!schoolId) errs.schoolId = 'Enter your ID number.';
  if (!password) errs.password = 'Enter your password.';
  if (Object.keys(errs).length) return showFieldErrors(f, errs);
  try { go(await api('POST', '/api/auth/login', { schoolId, password })); }
  catch (err) { showMsg(msg, 'error', err.message); showFieldErrors(f, err.fieldErrors); }
});
