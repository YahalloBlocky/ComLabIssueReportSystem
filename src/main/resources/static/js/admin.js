document.getElementById('hdr').innerHTML = headerHtml('Admin (Laboratory Technician)');
const $ = id => document.getElementById(id);
const gm = $('globalMsg');
let lookups;
function fail(e) { showMsg(gm, 'error', e.message); window.scrollTo(0, 0); }
(async () => {
  if (!await requirePage(['Admin'])) return;
  try {
    lookups = await api('GET', '/api/lookups');
    const opt = (a, first) => `<option value="">${first}</option>` + a.map(x => `<option value="${x.id}">${esc(x.name)}</option>`).join('');
    $('fStatus').innerHTML = opt(lookups.statuses, 'All');
    $('fPriority').innerHTML = opt(lookups.priorities, 'All') + '<option value="0">Not set yet</option>';
    $('fCategory').innerHTML = opt(lookups.categories, 'All');
    loadDash();
  } catch (e) { fail(e); }
})();
document.querySelectorAll('.tab').forEach(t => t.addEventListener('click', () => {
  hideMsg(gm);
  document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === t));
  ['dash', 'reports', 'labs', 'pcs', 'users'].forEach(n => $('tab-' + n).classList.toggle('hidden', n !== t.dataset.tab));
  ({ dash: loadDash, reports: loadReports, labs: loadLabs, pcs: loadPcs, users: loadUsers })[t.dataset.tab]();
}));

// ---------- dashboard
function bars(items) {
  const max = Math.max(1, ...items.map(i => i.count));
  return '<div class="bars">' + items.map(i => `<div class="bar-row"><span class="bar-label">${esc(i.label)}</span>
    <div class="bar" style="width:${Math.round(i.count / max * 60)}%"></div><b>${i.count}</b></div>`).join('') + '</div>';
}
async function loadDash() {
  try {
    const d = await api('GET', '/api/admin/dashboard');
    $('tab-dash').innerHTML = `<div class="stats"><div class="stat"><b>${d.totalReports}</b>Total reports</div><div class="stat"><b>${d.openReports}</b>Open</div>
      <div class="stat"><b>${d.resolvedReports}</b>Resolved</div><div class="stat"><b>${d.unprioritizedReports}</b>Need a priority</div></div>
      <div class="grid2"><div class="card"><h2>By status</h2>${bars(d.byStatus)}</div><div class="card"><h2>By priority</h2>${bars(d.byPriority)}</div>
      <div class="card"><h2>By category</h2>${bars(d.byCategory)}</div><div class="card"><h2>By laboratory</h2>${bars(d.byLaboratory)}</div></div>
      <div class="card table-wrap"><h2>Latest reports</h2><table><thead><tr><th>Reference</th><th>Title</th><th>Status</th><th>Priority</th><th>Submitted</th></tr></thead><tbody>
      ${d.recentReports.map(r => `<tr><td>${esc(r.referenceNo)}</td><td>${esc(r.title)}</td><td>${statusBadge(r.status)}</td><td>${priorityBadge(r.priority)}</td><td>${fmtDate(r.createdAt)}</td></tr>`).join('')}</tbody></table></div>`;
  } catch (e) { fail(e); }
}

// ---------- reports
$('fGo').addEventListener('click', loadReports);
async function loadLabSelect(sel, first) {
  const labs = await api('GET', '/api/admin/laboratories');
  sel.innerHTML = (first ? `<option value="">${first}</option>` : '') + labs.map(l => `<option value="${l.laboratoryId}">${esc(l.labName)}${l.active ? '' : ' (inactive)'}</option>`).join('');
  return labs;
}
async function loadReports() {
  try {
    if ($('fLab').options.length === 0) await loadLabSelect($('fLab'), 'All');
    const p = new URLSearchParams();
    [['statusId', 'fStatus'], ['priorityId', 'fPriority'], ['categoryId', 'fCategory'], ['laboratoryId', 'fLab'], ['q', 'fq']]
      .forEach(([k, id]) => { if ($(id).value.trim() !== '') p.set(k, $(id).value.trim()); });
    const rows = await api('GET', '/api/admin/issues?' + p);
    $('repEmpty').classList.toggle('hidden', rows.length > 0);
    $('repBody').innerHTML = rows.map(r => `<tr class="click" data-id="${r.reportId}"><td>${esc(r.referenceNo)}</td><td>${esc(r.title)}</td>
      <td>${esc(r.reporterName)}<br><span class="muted">${esc(r.reporterRole)}</span></td><td>${esc(r.laboratoryName)}${r.computerLabel ? ' / ' + esc(r.computerLabel) : ''}</td>
      <td>${esc(r.category)}</td><td>${statusBadge(r.status)}</td><td>${priorityBadge(r.priority)}</td><td>${fmtDate(r.createdAt)}</td></tr>`).join('');
    $('repBody').querySelectorAll('tr').forEach(tr => tr.addEventListener('click', () => openManage(tr.dataset.id)));
  } catch (e) { fail(e); }
}
async function openManage(id) {
  const dlg = $('dlg');
  try {
    const d = await api('GET', '/api/issues/' + id), i = d.issue, resolved = i.status === 'Resolved';
    let form = '<div class="alert ok">This report is Resolved and can no longer be changed.</div>';
    if (!resolved) {
      const st = lookups.statuses.filter(s => s.id >= i.statusId).map(s => `<option value="${s.id}" ${s.id === i.statusId ? 'selected' : ''}>${esc(s.name)}</option>`).join('');
      const pr = '<option value="">Not set yet</option>' + lookups.priorities.map(p => `<option value="${p.id}" ${p.id === i.priorityId ? 'selected' : ''}>${esc(p.name)}</option>`).join('');
      form = `<h3>Manage this report</h3><form id="mf" novalidate><div class="row"><div><label>Status (can only move forward)</label><select name="statusId">${st}</select></div>
        <div><label>Priority</label><select name="priorityId">${pr}</select></div></div>
        <label>Remarks (optional)</label><input name="remarks" maxlength="255">
        <div id="actBox" class="hidden"><label>Action taken (required to resolve)</label><textarea name="actionTaken" maxlength="2000"></textarea></div>
        <div class="field-error" data-err="actionTaken"></div><div id="mm" class="alert error hidden"></div><button class="btn">Save changes</button></form>`;
    }
    dlg.innerHTML = detailHtml(d) + form + '<div style="text-align:right;margin-top:10px"><button class="btn ghost" id="closeDlg">Close</button></div>';
    dlg.showModal(); $('closeDlg').onclick = () => dlg.close();
    const mf = $('mf'); if (!mf) return;
    const resolvedId = lookups.statuses.find(s => s.name === 'Resolved').id;
    mf.statusId.addEventListener('change', () => $('actBox').classList.toggle('hidden', +mf.statusId.value !== resolvedId));
    mf.addEventListener('submit', async e => {
      e.preventDefault(); hideMsg($('mm'));
      const body = { statusId: +mf.statusId.value, priorityId: mf.priorityId.value === '' ? null : +mf.priorityId.value,
        remarks: mf.remarks.value.trim(), actionTaken: mf.actionTaken ? mf.actionTaken.value.trim() : '' };
      if (body.statusId === resolvedId && !body.actionTaken) return showMsg($('mm'), 'error', 'Describe the action taken before resolving.');
      try { await api('PATCH', '/api/admin/issues/' + id, body); dlg.close(); loadReports(); }
      catch (err) { showMsg($('mm'), 'error', err.message); }
    });
  } catch (e) { fail(e); }
}

// ---------- laboratories
const labForm = $('labForm');
function clearLab() { labForm.reset(); labForm.id.value = ''; $('labFormTitle').textContent = 'Add a laboratory'; showFieldErrors(labForm, null); hideMsg($('labMsg')); }
$('labCancel').onclick = clearLab;
async function loadLabs() {
  try {
    const labs = await api('GET', '/api/admin/laboratories');
    $('labBody').innerHTML = labs.map(l => `<tr><td>${esc(l.labName)}</td><td>${esc(l.building)}</td><td>${esc(l.roomNumber)}</td><td>${l.computerCount}</td><td>${l.reportCount}</td>
      <td>${l.active ? 'Yes' : 'No'}</td><td class="actions"><button class="btn small ghost" data-a="edit" data-id="${l.laboratoryId}">Edit</button>
      <button class="btn small gold" data-a="toggle" data-id="${l.laboratoryId}" data-active="${l.active}">${l.active ? 'Make inactive' : 'Make active'}</button>
      <button class="btn small danger" data-a="del" data-id="${l.laboratoryId}">Delete</button></td></tr>`).join('');
    $('labBody').querySelectorAll('button').forEach(b => b.addEventListener('click', async () => {
      hideMsg(gm); const id = b.dataset.id;
      try {
        if (b.dataset.a === 'edit') { const l = labs.find(x => x.laboratoryId == id); labForm.id.value = id; labForm.labName.value = l.labName; labForm.building.value = l.building; labForm.roomNumber.value = l.roomNumber; $('labFormTitle').textContent = 'Edit laboratory'; window.scrollTo(0, 0); }
        else if (b.dataset.a === 'toggle') { await api('PATCH', `/api/admin/laboratories/${id}/active`, { active: b.dataset.active !== 'true' }); loadLabs(); }
        else if (confirm('Delete this laboratory and its computers? This cannot be undone.')) { await api('DELETE', '/api/admin/laboratories/' + id); loadLabs(); }
      } catch (e) { fail(e); }
    }));
  } catch (e) { fail(e); }
}
labForm.addEventListener('submit', async e => {
  e.preventDefault(); hideMsg($('labMsg')); showFieldErrors(labForm, null);
  const b = { labName: labForm.labName.value.trim(), building: labForm.building.value.trim(), roomNumber: labForm.roomNumber.value.trim() };
  const errs = {}; if (!b.labName) errs.labName = 'Laboratory name is required.'; if (!b.building) errs.building = 'Building is required.'; if (!b.roomNumber) errs.roomNumber = 'Room number is required.';
  if (Object.keys(errs).length) return showFieldErrors(labForm, errs);
  try {
    const id = labForm.id.value;
    await (id ? api('PUT', '/api/admin/laboratories/' + id, b) : api('POST', '/api/admin/laboratories', b));
    clearLab(); loadLabs();
  } catch (err) { showMsg($('labMsg'), 'error', err.message); showFieldErrors(labForm, err.fieldErrors); }
});

// ---------- computers
const pcForm = $('pcForm');
function clearPc() { pcForm.reset(); pcForm.id.value = ''; showFieldErrors(pcForm, null); hideMsg($('pcMsg')); }
$('pcCancel').onclick = clearPc;
$('pcLab').addEventListener('change', () => { clearPc(); loadPcs(); });
async function loadPcs() {
  try {
    if ($('pcLab').options.length === 0) await loadLabSelect($('pcLab'));
    if (!$('pcLab').value) return;
    const pcs = await api('GET', `/api/admin/laboratories/${$('pcLab').value}/computers`);
    $('pcBody').innerHTML = pcs.map(c => `<tr><td>${esc(c.unitLabel)}</td><td>${esc(c.assetTag || '')}</td><td>${c.reportCount}</td><td>${c.active ? 'Yes' : 'No'}</td>
      <td class="actions"><button class="btn small ghost" data-a="edit" data-id="${c.computerId}">Edit</button>
      <button class="btn small gold" data-a="toggle" data-id="${c.computerId}" data-active="${c.active}">${c.active ? 'Make inactive' : 'Make active'}</button>
      <button class="btn small danger" data-a="del" data-id="${c.computerId}">Delete</button></td></tr>`).join('');
    $('pcBody').querySelectorAll('button').forEach(b => b.addEventListener('click', async () => {
      hideMsg(gm); const id = b.dataset.id;
      try {
        if (b.dataset.a === 'edit') { const c = pcs.find(x => x.computerId == id); pcForm.id.value = id; pcForm.unitLabel.value = c.unitLabel; pcForm.assetTag.value = c.assetTag || ''; }
        else if (b.dataset.a === 'toggle') { await api('PATCH', `/api/admin/computers/${id}/active`, { active: b.dataset.active !== 'true' }); loadPcs(); }
        else if (confirm('Delete this computer?')) { await api('DELETE', '/api/admin/computers/' + id); loadPcs(); }
      } catch (e) { fail(e); }
    }));
  } catch (e) { fail(e); }
}
pcForm.addEventListener('submit', async e => {
  e.preventDefault(); hideMsg($('pcMsg')); showFieldErrors(pcForm, null);
  const b = { laboratoryId: +$('pcLab').value, unitLabel: pcForm.unitLabel.value.trim(), assetTag: pcForm.assetTag.value.trim() };
  if (!b.unitLabel) return showFieldErrors(pcForm, { unitLabel: 'Unit label is required.' });
  try {
    const id = pcForm.id.value;
    await (id ? api('PUT', '/api/admin/computers/' + id, b) : api('POST', '/api/admin/computers', b));
    clearPc(); loadPcs();
  } catch (err) { showMsg($('pcMsg'), 'error', err.message); showFieldErrors(pcForm, err.fieldErrors); }
});

// ---------- users
async function loadUsers() {
  try {
    const u = await api('GET', '/api/admin/users');
    $('userBody').innerHTML = u.map(x => `<tr><td>${esc(x.schoolId)}</td><td>${esc(x.firstName)} ${esc(x.lastName)}</td><td>${esc(x.email)}</td><td>${esc(x.role)}</td><td>${x.reportCount}</td>
      <td>${x.role === 'Admin' ? '' : `<button class="btn small danger" data-id="${x.userId}" data-name="${esc(x.firstName)} ${esc(x.lastName)}" data-n="${x.reportCount}">Delete</button>`}</td></tr>`).join('');
    $('userBody').querySelectorAll('button').forEach(b => b.addEventListener('click', async () => {
      hideMsg(gm);
      const n = +b.dataset.n;
      if (!confirm(`Delete the account of ${b.dataset.name}?` + (n ? `\n\nThis also permanently deletes their ${n} report(s) and the history of those reports.` : '') + '\n\nThis cannot be undone.')) return;
      try { await api('DELETE', '/api/admin/users/' + b.dataset.id); loadUsers(); } catch (e) { fail(e); }
    }));
  } catch (e) { fail(e); }
}
