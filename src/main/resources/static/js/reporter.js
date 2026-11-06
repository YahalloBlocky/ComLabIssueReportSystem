document.getElementById('hdr').innerHTML = headerHtml('Student / Instructor');
const f = document.getElementById('f'), msg = document.getElementById('msg');
let me;
(async () => {
  me = await requirePage(['Student', 'Instructor']); if (!me) return;
  if (me.role === 'Instructor') document.getElementById('informantBox').classList.remove('hidden');
  try {
    const l = await api('GET', '/api/lookups');
    f.laboratoryId.innerHTML = '<option value="">-- Choose a laboratory --</option>' +
      l.laboratories.map(x => `<option value="${x.laboratoryId}">${esc(x.labName)} (${esc(x.building)} ${esc(x.roomNumber)})</option>`).join('');
    f.categoryId.innerHTML = '<option value="">-- Choose a type --</option>' + l.categories.map(x => `<option value="${x.id}">${esc(x.name)}</option>`).join('');
    resetComputers();
  } catch (e) { showMsg(msg, 'error', e.message); }
})();
function resetComputers() { f.computerId.innerHTML = '<option value="">Whole laboratory / not sure which computer</option>'; }
f.categoryId.addEventListener('change', () => {
  const o = f.categoryId.options[f.categoryId.selectedIndex];
  document.getElementById('otherBox').classList.toggle('hidden', !o || o.text !== 'Other');
});
f.laboratoryId.addEventListener('change', async () => {
  resetComputers(); if (!f.laboratoryId.value) return;
  try {
    const cs = await api('GET', `/api/laboratories/${f.laboratoryId.value}/computers`);
    f.computerId.innerHTML += cs.map(c => `<option value="${c.computerId}">${esc(c.unitLabel)}</option>`).join('');
  } catch (e) { showMsg(msg, 'error', e.message); }
});
f.addEventListener('submit', async e => {
  e.preventDefault(); hideMsg(msg); showFieldErrors(f, null);
  const b = { laboratoryId: +f.laboratoryId.value || null, computerId: +f.computerId.value || null, categoryId: +f.categoryId.value || null,
    title: f.title.value.trim(), description: f.description.value.trim(), informantName: me.role === 'Instructor' ? f.informantName.value.trim() : null,
    otherDescription: f.categoryId.options[f.categoryId.selectedIndex].text === 'Other' ? f.otherDescription.value.trim() : null };
  const errs = {};
  if (!b.laboratoryId) errs.laboratoryId = 'Choose a laboratory.';
  if (!b.categoryId) errs.categoryId = 'Choose a category.';
  if (b.otherDescription !== null && b.otherDescription.length < 3) errs.otherDescription = 'Please describe the type of problem.';
  if (b.title.length < 5) errs.title = 'Use 5 to 150 characters.';
  if (b.description.length < 10) errs.description = 'Use 10 to 2000 characters.';
  if (Object.keys(errs).length) return showFieldErrors(f, errs);
  try {
    const d = await api('POST', '/api/issues', b);
    showMsg(msg, 'ok', `Thank you! Your report was sent. Reference number: ${d.issue.referenceNo}`);
    f.reset(); resetComputers(); document.getElementById('otherBox').classList.add('hidden');
  } catch (err) { showMsg(msg, 'error', err.message); showFieldErrors(f, err.fieldErrors); }
});
document.querySelectorAll('.tab').forEach(t => t.addEventListener('click', () => {
  document.querySelectorAll('.tab').forEach(x => x.classList.toggle('active', x === t));
  document.getElementById('tab-new').classList.toggle('hidden', t.dataset.tab !== 'new');
  document.getElementById('tab-mine').classList.toggle('hidden', t.dataset.tab !== 'mine');
  if (t.dataset.tab === 'mine') loadMine();
}));
async function loadMine() {
  const body = document.getElementById('mineBody'), em = document.getElementById('mineMsg');
  hideMsg(em);
  try {
    const rows = await api('GET', '/api/issues/mine');
    document.getElementById('mineEmpty').classList.toggle('hidden', rows.length > 0);
    body.innerHTML = rows.map(r => `<tr class="click" data-id="${r.reportId}"><td>${esc(r.referenceNo)}</td><td>${esc(r.title)}</td>
      <td>${esc(r.laboratoryName)}${r.computerLabel ? ' / ' + esc(r.computerLabel) : ''}</td><td>${statusBadge(r.status)}</td><td>${priorityBadge(r.priority)}</td><td>${fmtDate(r.createdAt)}</td></tr>`).join('');
    body.querySelectorAll('tr').forEach(tr => tr.addEventListener('click', () => openDetail(tr.dataset.id)));
  } catch (e) { showMsg(em, 'error', e.message); }
}
async function openDetail(id) {
  const dlg = document.getElementById('dlg');
  try {
    const d = await api('GET', '/api/issues/' + id), i = d.issue;
    dlg.innerHTML = detailHtml(d) + '<div style="text-align:right"><button class="btn ghost" id="closeDlg">Close</button></div>';
    dlg.showModal(); document.getElementById('closeDlg').onclick = () => dlg.close();
  } catch (e) { alert(e.message); }
}
