// Shared helpers used by every page.
async function api(method, url, body) {
  const opts = { method, headers: {}, credentials: 'same-origin' };
  if (body !== undefined) { opts.headers['Content-Type'] = 'application/json'; opts.body = JSON.stringify(body); }
  let res;
  try { res = await fetch(url, opts); }
  catch (e) { throw { status: 0, message: 'Cannot reach the server. Is the application running?' }; }
  if (res.status === 204) return null;
  let data = null;
  try { data = await res.json(); } catch (e) { /* no body */ }
  if (!res.ok) {
    if (res.status === 401 && !location.pathname.endsWith('index.html') && location.pathname !== '/') {
      location.href = '/index.html';
    }
    throw { status: res.status, message: (data && data.message) || 'Something went wrong.', fieldErrors: data && data.fieldErrors };
  }
  return data;
}
// Escape text before putting it in innerHTML (stops script injection).
function esc(s) {
  return String(s == null ? '' : s).replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
}
function fmtDate(s) {
  if (!s) return '';
  const d = new Date(s);
  return isNaN(d) ? esc(s) : d.toLocaleString('en-PH', { dateStyle: 'medium', timeStyle: 'short' });
}
function statusBadge(name) { return `<span class="badge s-${esc(String(name).replace(/ /g, '-'))}">${esc(name)}</span>`; }
function priorityBadge(name) {
  return name ? `<span class="badge p-${esc(name)}">${esc(name)}</span>` : '<span class="badge p-none">Not set</span>';
}
function showMsg(el, type, text) { el.className = 'alert ' + type; el.textContent = text; el.classList.remove('hidden'); }
function hideMsg(el) { el.classList.add('hidden'); }
// Show server field errors under the matching inputs (data-err="fieldName").
function showFieldErrors(form, errors) {
  form.querySelectorAll('[data-err]').forEach(e => e.textContent = '');
  if (!errors) return;
  Object.entries(errors).forEach(([k, v]) => { const e = form.querySelector(`[data-err="${k}"]`); if (e) e.textContent = v; });
}
async function requirePage(roles) {
  try {
    const me = await api('GET', '/api/auth/me');
    if (!roles.includes(me.role)) { location.href = '/index.html'; return null; }
    document.getElementById('who').textContent = `${me.fullName} (${me.role})`;
    return me;
  } catch (e) { location.href = '/index.html'; return null; }
}
async function logout() { try { await api('POST', '/api/auth/logout'); } catch (e) {} location.href = '/index.html'; }
function headerHtml(sub) {
  return `<img class="logo" src="/assets/cvsu-logo.png" alt="Cavite State University logo">
  <div><div class="title">Computer Laboratory Issue Reporting System</div><div class="sub">${esc(sub)}</div></div>
  <div class="who"><span id="who"></span><button class="header-btn" onclick="logout()">Log out</button></div>`;
}

// Report detail (used by reporter and admin pages)
function detailHtml(d) {
  const i = d.issue;
  return `<h2>${esc(i.referenceNo)} - ${esc(i.title)}</h2>
  <dl class="meta"><dt>Status</dt><dd>${statusBadge(i.status)}</dd><dt>Priority</dt><dd>${priorityBadge(i.priority)}</dd>
  <dt>Category</dt><dd>${esc(i.category)}</dd><dt>Laboratory</dt><dd>${esc(i.laboratoryName)} (Room ${esc(i.roomNumber)})</dd>
  <dt>Computer</dt><dd>${i.computerLabel ? esc(i.computerLabel) : 'Whole laboratory'}</dd>
  <dt>Reported by</dt><dd>${esc(i.reporterName)} (${esc(i.reporterRole)}, ${esc(i.reporterSchoolId)})</dd>
  ${i.informantName ? `<dt>Informant</dt><dd>${esc(i.informantName)}</dd>` : ''}
  <dt>Submitted</dt><dd>${fmtDate(i.createdAt)}</dd><dt>Description</dt><dd>${esc(i.description)}</dd></dl>
  <h3>Status history</h3><ul class="timeline">${d.history.map(h => `<li><b>${h.oldStatus ? esc(h.oldStatus) + ' &rarr; ' : ''}${esc(h.newStatus)}</b> - ${fmtDate(h.changedAt)}<br>
  <span class="muted">by ${esc(h.changedByName)} (${esc(h.changedByRole)})${h.remarks ? ' - ' + esc(h.remarks) : ''}</span></li>`).join('')}</ul>
  ${d.resolutions.length ? '<h3>Resolution</h3>' + d.resolutions.map(r => `<p>${esc(r.actionTaken)}<br><span class="muted">by ${esc(r.resolvedByName)}, ${fmtDate(r.resolvedAt)}</span></p>`).join('') : ''}`;
}

// Show / hide password buttons (<button class="pw-toggle" data-pw="inputId">)
document.addEventListener('click', e => {
  const b = e.target.closest && e.target.closest('.pw-toggle'); if (!b) return;
  const input = document.getElementById(b.dataset.pw), show = input.type === 'password';
  input.type = show ? 'text' : 'password';
  b.textContent = show ? 'Hide' : 'Show';
  b.setAttribute('aria-pressed', String(show));
  b.setAttribute('aria-label', show ? 'Hide password' : 'Show password');
});
