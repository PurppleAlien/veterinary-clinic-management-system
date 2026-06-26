const API_BASE = '/api';

function getToken() { return localStorage.getItem('vet_token'); }
function getUser() { return JSON.parse(localStorage.getItem('vet_user') || '{}'); }
function setUser(u) { localStorage.setItem('vet_user', JSON.stringify(u)); }

let loadingCounter = 0;

function showLoading() {
    loadingCounter++;
    const overlay = document.getElementById('loadingOverlay') || (() => {
        const el = document.createElement('div');
        el.id = 'loadingOverlay';
        el.className = 'loading-overlay';
        el.innerHTML = '<div class="spinner"></div>';
        document.body.appendChild(el);
        return el;
    })();
    overlay.classList.add('active');
}

function hideLoading() {
    loadingCounter = Math.max(0, loadingCounter - 1);
    const overlay = document.getElementById('loadingOverlay');
    if (overlay && loadingCounter === 0) {
        overlay.classList.remove('active');
    }
}

function authHeaders() {
    return {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + getToken()
    };
}

function logout() {
    localStorage.removeItem('vet_token');
    localStorage.removeItem('vet_user');
    window.location.href = '/';
}

async function apiGet(path) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, { headers: authHeaders() });
        if (resp.status === 401) logout();
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ error: resp.statusText }));
            throw new Error(err.error || 'Error de red');
        }
        return resp.json();
    } finally {
        hideLoading();
    }
}

async function apiPost(path, body) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify(body)
        });
        if (resp.status === 401) logout();
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ error: resp.statusText }));
            throw new Error(err.error || 'Error de red');
        }
        return resp.json();
    } finally {
        hideLoading();
    }
}

async function apiPut(path, body) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, {
            method: 'PUT',
            headers: authHeaders(),
            body: JSON.stringify(body)
        });
        if (resp.status === 401) logout();
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ error: resp.statusText }));
            throw new Error(err.error || 'Error de red');
        }
        return resp.json();
    } finally {
        hideLoading();
    }
}

async function apiPatch(path, body) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, {
            method: 'PATCH',
            headers: authHeaders(),
            body: JSON.stringify(body)
        });
        if (resp.status === 401) logout();
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ error: resp.statusText }));
            throw new Error(err.error || 'Error de red');
        }
        return resp.json();
    } finally {
        hideLoading();
    }
}

async function apiDelete(path) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, {
            method: 'DELETE',
            headers: authHeaders()
        });
        if (resp.status === 401) logout();
        if (!resp.ok) {
            const err = await resp.json().catch(() => ({ error: resp.statusText }));
            throw new Error(err.error || 'Error de red');
        }
        try { return resp.json(); } catch { return true; }
    } finally {
        hideLoading();
    }
}

async function apiGetBlob(path) {
    showLoading();
    try {
        const resp = await fetch(API_BASE + path, { headers: authHeaders() });
        if (resp.status === 401) logout();
        if (!resp.ok) throw new Error('Error descargando archivo');
        return resp.blob();
    } finally {
        hideLoading();
    }
}

let searchableClientsCache = [];
let searchablePetsCache = [];

async function loadSearchableClients() {
    try {
        const cs = await apiGet('/medico/clientes');
        searchableClientsCache = cs;
    } catch(e) { searchableClientsCache = []; }
}

async function loadSearchablePets() {
    try {
        const ps = await apiGet('/medico/mascotas');
        searchablePetsCache = ps;
    } catch(e) { searchablePetsCache = []; }
}

function createSearchableInput(inputId, data, labelKey, valueKey) {
    const input = document.getElementById(inputId);
    if (!input) return;
    const container = input.parentElement;
    const listId = inputId + 'List';
    let list = document.getElementById(listId);
    if (!list) {
        list = document.createElement('ul');
        list.id = listId;
        list.className = 'list-group position-absolute w-100 shadow-sm';
        list.style.cssText = 'z-index:1000;max-height:200px;overflow-y:auto;display:none';
        container.style.position = 'relative';
        container.appendChild(list);
    }

    input.addEventListener('input', function() {
        const val = this.value.toLowerCase();
        const filtered = data.filter(d => String(d[labelKey]).toLowerCase().includes(val));
        if (filtered.length > 0 && val.length > 0) {
            list.innerHTML = filtered.slice(0, 10).map(d =>
                `<li class="list-group-item list-group-item-action py-1" style="cursor:pointer;font-size:.85rem"
                    onclick="selectSearchable('${inputId}', ${d[valueKey]}, '${String(d[labelKey]).replace(/'/g, "\\'")}')">
                    ${d[labelKey]} ${d.apellido ? d.apellido : ''}
                </li>`
            ).join('');
            list.style.display = 'block';
        } else {
            list.style.display = 'none';
        }
    });

    input.addEventListener('blur', () => setTimeout(() => { list.style.display = 'none'; }, 200));
}

function selectSearchable(inputId, value, label) {
    document.getElementById(inputId).value = value;
    const list = document.getElementById(inputId + 'List');
    if (list) list.style.display = 'none';
}

function fmt(num) {
    return '$' + Number(num || 0).toFixed(2);
}

function toast(msg, type) {
    const container = document.getElementById('toastContainer') || (() => {
        const c = document.createElement('div');
        c.id = 'toastContainer';
        c.style.cssText = 'position:fixed;top:20px;right:20px;z-index:9999';
        document.body.appendChild(c);
        return c;
    })();
    const t = document.createElement('div');
    t.className = 'toast align-items-center text-white border-0 show';
    t.setAttribute('role', 'alert');
    t.innerHTML = `<div class="d-flex"><div class="toast-body">${msg}</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button></div>`;
    t.style.backgroundColor = type === 'error' ? '#dc3545' : type === 'warning' ? '#ffc107' : '#198754';
    container.appendChild(t);
    setTimeout(() => { t.remove(); }, 4000);
}
