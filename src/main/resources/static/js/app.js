let calendarInstance = null;
let currentClienteId = null;
let currentMascotaId = null;
let reportChart = null;

const ESTADOS_CITA = ['PROGRAMADA','CONFIRMADA','EN_CURSO','COMPLETADA','CANCELADA','NO_ASISTIO'];
const COLORES_ESTADO = {PROGRAMADA:'#0d6efd',CONFIRMADA:'#198754',EN_CURSO:'#ffc107',COMPLETADA:'#6f42c1',CANCELADA:'#dc3545',NO_ASISTIO:'#6c757d'};
const ESPECIES = ['CANINO','FELINO','EQUINO','BOVINO','AVIAR','EXOTICO','OTRO'];
const GENEROS = ['MACHO','HEMBRA'];

function showSection(name) {
    document.querySelectorAll('.section-content').forEach(s => s.classList.add('d-none'));
    const sec = document.getElementById('sec-' + name);
    if (sec) sec.classList.remove('d-none');
    if (name === 'dashboard') loadDashboard();
    if (name === 'agenda') initCalendar();
    if (name === 'clientes') { loadClientes(); loadSearchableClients(); }
    if (name === 'mascotas') { loadMascotas(); loadSearchablePets(); loadSearchableClients(); }
    if (name === 'servicios') loadServicios();
    if (name === 'medicamentos') loadMedicamentos();
    if (name === 'facturacion') { loadFacturas(); loadSearchableClients(); }
    if (name === 'historial') { if(currentMascotaId) loadHistorial(currentMascotaId); }
    if (name === 'vacunas') loadVacunas();
    if (name === 'hospitalizaciones') { loadHospitalizaciones(); loadSearchablePets(); }
    if (name === 'planes') loadPlanes();
    if (name === 'consentimientos') { loadConsentimientos(); loadSearchableClients(); }
    if (name === 'reportes') initReportes();
    if (name === 'configuracion') loadPerfil();
    if (name === 'admin') loadAdmin();
    if (name === 'auditoria') loadAuditoria();
}

async function loadDashboard() {
    try {
        const d = await apiGet('/medico/dashboard');
        document.getElementById('statClientes').textContent = d.totalClientes || 0;
        document.getElementById('statMascotas').textContent = d.totalMascotas || 0;
        document.getElementById('statVets').textContent = d.totalVeterinarios || 0;
        document.getElementById('statCitasHoy').textContent = d.citasHoy || 0;
        document.getElementById('statPendientes').textContent = d.citasPendientes || 0;
        document.getElementById('statHospitalizados').textContent = d.hospitalizados || 0;
        document.getElementById('statStockBajo').textContent = d.stockBajo || 0;
    } catch(e) { toast(e.message, 'error'); }
}

let calendarInitialized = false;
function initCalendar() {
    if (!calendarInitialized) {
        calendarInitialized = true;
        const calEl = document.getElementById('calendar');
        if (!calEl) return;
        calendarInstance = new FullCalendar.Calendar(calEl, {
            initialView: 'timeGridWeek',
            headerToolbar: { left:'prev,next today', center:'title', right:'dayGridMonth,timeGridWeek,timeGridDay' },
            locale: 'es',
            slotMin: '07:00',
            slotMax: '21:00',
            allDaySlot: false,
            height: 'auto',
            events: function(info, successCallback) {
                apiGet('/medico/citas').then(citas => {
                    successCallback(citas.map(c => ({
                        id: c.id,
                        title: c.mascotaNombre + ' - ' + c.motivo,
                        start: c.fechaHoraInicio,
                        end: c.fechaHoraFin,
                        backgroundColor: COLORES_ESTADO[c.estado] || '#0d6efd',
                        extendedProps: c
                    })));
                }).catch(() => successCallback([]));
            },
            eventClick: function(info) {
                fillEditCitaModal(info.event.extendedProps);
                new bootstrap.Modal('#modalCita').show();
            },
            selectable: true,
            select: function(info) {
                document.getElementById('citaId').value = '';
                document.getElementById('citaMascota').value = '';
                document.getElementById('citaMotivo').value = '';
                document.getElementById('citaNotas').value = '';
                document.getElementById('citaInicio').value = info.startStr.substring(0,16);
                document.getElementById('citaFin').value = info.endStr.substring(0,16);
                document.getElementById('citaEstado').value = 'PROGRAMADA';
                document.getElementById('citaCancelMotivo').style.display = 'none';
                clearValidation('citaForm');
                new bootstrap.Modal('#modalCita').show();
            },
            eventDrop: function(info) {
                const cita = info.event.extendedProps;
                apiPut('/medico/citas/' + info.event.id, {
                    fechaHoraInicio: info.event.start.toISOString(),
                    fechaHoraFin: info.event.end.toISOString()
                }).catch(() => { info.revert(); toast('Error al mover cita', 'error'); });
            }
        });
        calendarInstance.render();
    } else {
        calendarInstance.refetchEvents();
    }
}

async function loadClientes() {
    try {
        const cs = await apiGet('/medico/clientes');
        const tbody = document.getElementById('tablaClientes');
        tbody.innerHTML = cs.map(c => `<tr>
            <td>${c.id}</td><td>${escapeHtml(c.nombre)}</td><td>${escapeHtml(c.apellido)}</td>
            <td>${c.email || '-'}</td><td>${c.telefono || '-'}</td>
            <td>${c.mascotas ? c.mascotas.length : 0}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="viewCliente(${c.id})"><i class="bi bi-eye"></i></button>
                <button class="btn btn-sm btn-outline-warning" onclick="editCliente(${c.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteCliente(${c.id})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function viewCliente(id) {
    try {
        const c = await apiGet('/medico/clientes/' + id);
        document.getElementById('viewClienteNombre').textContent = c.nombre + ' ' + c.apellido;
        document.getElementById('viewClienteEmail').textContent = c.email || '-';
        document.getElementById('viewClienteTel').textContent = c.telefono || '-';
        document.getElementById('viewClienteDir').textContent = c.direccion || '-';
        const petList = document.getElementById('viewClienteMascotas');
        petList.innerHTML = (c.mascotas || []).map(m => `<li class="list-group-item d-flex justify-content-between align-items-center">
            ${escapeHtml(m.nombre)} <span class="badge bg-primary rounded-pill">${m.especie}</span>
            <button class="btn btn-sm btn-outline-info" onclick="showMascotaDetail(${m.id})"><i class="bi bi-eye"></i></button>
        </li>`).join('');
        new bootstrap.Modal('#modalViewCliente').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function editCliente(id) {
    try {
        const c = await apiGet('/medico/clientes/' + id);
        document.getElementById('clienteId').value = c.id;
        document.getElementById('cliNombre').value = c.nombre || '';
        document.getElementById('cliApellido').value = c.apellido || '';
        document.getElementById('cliEmail').value = c.email || '';
        document.getElementById('cliTel').value = c.telefono || '';
        document.getElementById('cliDir').value = c.direccion || '';
        document.getElementById('cliRfc').value = c.identificacionFiscal || '';
        clearValidation('clienteForm');
        new bootstrap.Modal('#modalCliente').show();
    } catch(e) { toast(e.message, 'error'); }
}

function validateField(id) {
    const el = document.getElementById(id);
    if (!el) return true;
    if (el.hasAttribute('required') && !el.value.trim()) {
        el.classList.add('is-invalid');
        return false;
    }
    if (el.type === 'email' && el.value && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(el.value)) {
        el.classList.add('is-invalid');
        return false;
    }
    el.classList.remove('is-invalid');
    return true;
}

function clearValidation(formId) {
    document.querySelectorAll('#' + formId + ' .is-invalid').forEach(el => el.classList.remove('is-invalid'));
}

function validateForm(formId) {
    const form = document.getElementById(formId);
    if (!form) return true;
    const fields = form.querySelectorAll('[required], [type="email"]');
    let valid = true;
    fields.forEach(f => {
        if (!validateField(f.id)) valid = false;
    });
    return valid;
}

async function saveCliente() {
    if (!validateForm('clienteForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('clienteId').value;
    const body = {
        nombre: document.getElementById('cliNombre').value,
        apellido: document.getElementById('cliApellido').value,
        email: document.getElementById('cliEmail').value,
        telefono: document.getElementById('cliTel').value,
        direccion: document.getElementById('cliDir').value,
        identificacionFiscal: document.getElementById('cliRfc').value
    };
    try {
        if (id) await apiPut('/medico/clientes/' + id, body);
        else await apiPost('/medico/clientes', body);
        bootstrap.Modal.getInstance('#modalCliente').hide();
        toast('Cliente guardado');
        loadClientes();
    } catch(e) { toast(e.message, 'error'); }
}

async function deleteCliente(id) {
    if (!confirm('¿Eliminar cliente?')) return;
    try { await apiDelete('/medico/clientes/' + id); toast('Cliente eliminado'); loadClientes(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadMascotas() {
    try {
        const ms = await apiGet('/medico/mascotas');
        document.getElementById('tablaMascotas').innerHTML = ms.map(m => `<tr>
            <td>${m.id}</td><td>${escapeHtml(m.nombre)}</td><td>${m.especie}</td>
            <td>${m.raza || '-'}</td><td>${m.genero || '-'}</td>
            <td>${m.cliente ? escapeHtml(m.cliente.nombre+' '+m.cliente.apellido) : '-'}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="showMascotaDetail(${m.id})"><i class="bi bi-eye"></i></button>
                <button class="btn btn-sm btn-outline-warning" onclick="editMascota(${m.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteMascota(${m.id})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function showMascotaDetail(id) {
    try {
        const m = await apiGet('/medico/mascotas/' + id);
        currentMascotaId = id;
        document.getElementById('detMascotaNombre').textContent = m.nombre;
        document.getElementById('detMascotaEspecie').textContent = m.especie;
        document.getElementById('detMascotaRaza').textContent = m.raza || '-';
        document.getElementById('detMascotaColor').textContent = m.color || '-';
        document.getElementById('detMascotaGenero').textContent = m.genero || '-';
        document.getElementById('detMascotaPeso').textContent = m.peso ? m.peso + ' kg' : '-';
        document.getElementById('detMascotaCliente').textContent = m.clienteNombre || '-';
        document.getElementById('detMascotaAlergias').textContent = m.alergias || 'Ninguna';
        document.getElementById('detMascotaCondiciones').textContent = m.condicionesMedicas || 'Ninguna';

        const citas = await apiGet('/medico/citas/mascota/' + id);
        document.getElementById('detMascotaCitas').innerHTML = citas.slice(0,5).map(c =>
            `<li class="list-group-item">${new Date(c.fechaHoraInicio).toLocaleDateString()} - ${escapeHtml(c.motivo)} <span class="badge bg-${c.estado==='COMPLETADA'?'success':c.estado==='CANCELADA'?'danger':'primary'}">${c.estado}</span></li>`
        ).join('') || '<li class="list-group-item">Sin citas</li>';

        new bootstrap.Modal('#modalMascotaDetail').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function editMascota(id) {
    try {
        const m = await apiGet('/medico/mascotas/' + id);
        document.getElementById('mascotaId').value = m.id;
        document.getElementById('petNombre').value = m.nombre;
        document.getElementById('petEspecie').value = m.especie;
        document.getElementById('petRaza').value = m.raza || '';
        document.getElementById('petColor').value = m.color || '';
        document.getElementById('petGenero').value = m.genero || '';
        document.getElementById('petPeso').value = m.peso || '';
        document.getElementById('petAlergias').value = m.alergias || '';
        document.getElementById('petCondiciones').value = m.condicionesMedicas || '';
        document.getElementById('petClienteId').value = m.clienteId || '';
        clearValidation('mascotaForm');
        new bootstrap.Modal('#modalMascota').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function saveMascota() {
    if (!validateForm('mascotaForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('mascotaId').value;
    const body = {
        nombre: document.getElementById('petNombre').value,
        especie: document.getElementById('petEspecie').value,
        raza: document.getElementById('petRaza').value,
        color: document.getElementById('petColor').value,
        genero: document.getElementById('petGenero').value,
        peso: parseFloat(document.getElementById('petPeso').value) || null,
        alergias: document.getElementById('petAlergias').value,
        condicionesMedicas: document.getElementById('petCondiciones').value,
        cliente: { id: parseInt(document.getElementById('petClienteId').value) || null }
    };
    try {
        if (id) await apiPut('/medico/mascotas/' + id, body);
        else await apiPost('/medico/mascotas', body);
        bootstrap.Modal.getInstance('#modalMascota').hide();
        toast('Mascota guardada');
        loadMascotas();
    } catch(e) { toast(e.message, 'error'); }
}

async function deleteMascota(id) {
    if (!confirm('¿Eliminar mascota?')) return;
    try { await apiDelete('/medico/mascotas/' + id); toast('Mascota eliminada'); loadMascotas(); }
    catch(e) { toast(e.message, 'error'); }
}

function fillEditCitaModal(c) {
    document.getElementById('citaId').value = c.id || '';
    document.getElementById('citaMascota').value = c.mascotaId || '';
    document.getElementById('citaMotivo').value = c.motivo || '';
    document.getElementById('citaNotas').value = c.notas || '';
    document.getElementById('citaInicio').value = c.fechaHoraInicio ? c.fechaHoraInicio.substring(0,16) : '';
    document.getElementById('citaFin').value = c.fechaHoraFin ? c.fechaHoraFin.substring(0,16) : '';
    document.getElementById('citaEstado').value = c.estado || 'PROGRAMADA';
    document.getElementById('citaCancelMotivoInput').value = c.motivoCancelacion || '';
    document.getElementById('citaCancelMotivo').style.display = (c.estado === 'CANCELADA') ? 'block' : 'none';
    clearValidation('citaForm');
}

async function saveCita() {
    if (!validateForm('citaForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('citaId').value;
    const estado = document.getElementById('citaEstado').value;
    const body = {
        mascotaId: parseInt(document.getElementById('citaMascota').value),
        motivo: document.getElementById('citaMotivo').value,
        notas: document.getElementById('citaNotas').value,
        fechaHoraInicio: document.getElementById('citaInicio').value,
        fechaHoraFin: document.getElementById('citaFin').value,
        estado: estado,
        motivoCancelacion: estado === 'CANCELADA' ? document.getElementById('citaCancelMotivoInput').value : null
    };
    try {
        if (id) await apiPut('/medico/citas/' + id, body);
        else await apiPost('/medico/citas', body);
        bootstrap.Modal.getInstance('#modalCita').hide();
        toast('Cita guardada');
        if (calendarInstance) calendarInstance.refetchEvents();
    } catch(e) { toast(e.message, 'error'); }
}

async function loadServicios() {
    try {
        const ss = await apiGet('/medico/servicios');
        document.getElementById('tablaServicios').innerHTML = ss.map(s => `<tr>
            <td>${s.id}</td><td>${escapeHtml(s.nombre)}</td><td>${s.descripcion || '-'}</td>
            <td>${fmt(s.precioBase)}</td><td>${s.codigoInterno || '-'}</td>
            <td><span class="badge bg-${s.activo?'success':'secondary'}">${s.activo?'Activo':'Inactivo'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-warning" onclick="editServicio(${s.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="toggleServicio(${s.id})"><i class="bi bi-power"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function editServicio(id) {
    try {
        const ss = await apiGet('/medico/servicios');
        const s = ss.find(x => x.id === id);
        document.getElementById('servId').value = s.id;
        document.getElementById('servNombre').value = s.nombre;
        document.getElementById('servDesc').value = s.descripcion || '';
        document.getElementById('servPrecio').value = s.precioBase;
        document.getElementById('servCodigo').value = s.codigoInterno || '';
        clearValidation('servicioForm');
        new bootstrap.Modal('#modalServicio').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function saveServicio() {
    if (!validateForm('servicioForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('servId').value;
    const body = {
        nombre: document.getElementById('servNombre').value,
        descripcion: document.getElementById('servDesc').value,
        precioBase: parseFloat(document.getElementById('servPrecio').value),
        codigoInterno: document.getElementById('servCodigo').value
    };
    try {
        if (id) await apiPut('/medico/servicios/' + id, body);
        else await apiPost('/medico/servicios', body);
        bootstrap.Modal.getInstance('#modalServicio').hide();
        toast('Servicio guardado');
        loadServicios();
    } catch(e) { toast(e.message, 'error'); }
}

async function toggleServicio(id) {
    try { await apiDelete('/medico/servicios/' + id); toast('Servicio desactivado/activado'); loadServicios(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadMedicamentos() {
    try {
        const ms = await apiGet('/medico/medicamentos');
        const bajos = await apiGet('/medico/medicamentos/stock-bajo');
        const bajosIds = new Set(bajos.map(b => b.id));
        document.getElementById('tablaMedicamentos').innerHTML = ms.map(m => `<tr class="${bajosIds.has(m.id)?'table-warning':''}">
            <td>${m.id}</td><td>${escapeHtml(m.nombre)}</td><td>${m.descripcion || '-'}</td>
            <td>${m.unidad}</td>
            <td><span class="badge bg-${m.stockActual <= m.stockMinimo ? 'danger' : 'success'}">${m.stockActual}</span></td>
            <td>${m.stockMinimo}</td><td>${fmt(m.precioUnitario)}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="ajustarStock(${m.id})"><i class="bi bi-box-seam"></i></button>
                <button class="btn btn-sm btn-outline-warning" onclick="editMedicamento(${m.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="toggleMedicamento(${m.id})"><i class="bi bi-power"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function editMedicamento(id) {
    try {
        const ms = await apiGet('/medico/medicamentos');
        const m = ms.find(x => x.id === id);
        document.getElementById('medId').value = m.id;
        document.getElementById('medNombre').value = m.nombre;
        document.getElementById('medDesc').value = m.descripcion || '';
        document.getElementById('medUnidad').value = m.unidad;
        document.getElementById('medPrecio').value = m.precioUnitario;
        document.getElementById('medStockMin').value = m.stockMinimo;
        clearValidation('medicamentoForm');
        new bootstrap.Modal('#modalMedicamento').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function saveMedicamento() {
    if (!validateForm('medicamentoForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('medId').value;
    const body = {
        nombre: document.getElementById('medNombre').value,
        descripcion: document.getElementById('medDesc').value,
        unidad: document.getElementById('medUnidad').value,
        precioUnitario: parseFloat(document.getElementById('medPrecio').value),
        stockMinimo: parseInt(document.getElementById('medStockMin').value)
    };
    try {
        if (id) await apiPut('/medico/medicamentos/' + id, body);
        else await apiPost('/medico/medicamentos', body);
        bootstrap.Modal.getInstance('#modalMedicamento').hide();
        toast('Medicamento guardado');
        loadMedicamentos();
    } catch(e) { toast(e.message, 'error'); }
}

async function toggleMedicamento(id) {
    try { await apiDelete('/medico/medicamentos/' + id); loadMedicamentos(); }
    catch(e) { toast(e.message, 'error'); }
}

async function ajustarStock(id) {
    document.getElementById('stockMedId').value = id;
    clearValidation('stockForm');
    new bootstrap.Modal('#modalStock').show();
}

async function saveAjusteStock() {
    if (!validateForm('stockForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('stockMedId').value;
    const body = {
        cantidad: parseInt(document.getElementById('stockCantidad').value),
        tipo: document.getElementById('stockTipo').value,
        motivo: document.getElementById('stockMotivo').value
    };
    try {
        await apiPost('/medico/medicamentos/' + id + '/ajustar-stock', body);
        bootstrap.Modal.getInstance('#modalStock').hide();
        toast('Stock ajustado');
        loadMedicamentos();
    } catch(e) { toast(e.message, 'error'); }
}

async function loadFacturas() {
    try {
        const fs = await apiGet('/medico/facturas');
        document.getElementById('tablaFacturas').innerHTML = fs.map(f => `<tr>
            <td>${f.numeroFactura}</td>
            <td>${escapeHtml(f.clienteNombre)}</td>
            <td>${new Date(f.fechaEmision).toLocaleDateString()}</td>
            <td>${fmt(f.total)}</td>
            <td>${fmt(f.totalPagado)}</td>
            <td>${fmt(f.saldoPendiente)}</td>
            <td><span class="badge bg-${f.estado==='PAGADA'?'success':f.estado==='ANULADA'?'danger':'warning'}">${f.estado}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="viewFactura(${f.id})"><i class="bi bi-eye"></i></button>
                <button class="btn btn-sm btn-outline-success" onclick="addPago(${f.id})"><i class="bi bi-cash"></i></button>
                ${f.estado!=='PAGADA'&&f.estado!=='ANULADA'?`<button class="btn btn-sm btn-outline-primary" onclick="pagarOnline(${f.id})"><i class="bi bi-credit-card"></i></button>`:''}
                <a class="btn btn-sm btn-outline-secondary" href="/api/medico/facturas/${f.id}/pdf" target="_blank"><i class="bi bi-file-pdf"></i></a>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

let facturaServicios = [];
let facturaMedicamentos = [];
let facturaDetalles = [];

async function openNewFactura() {
    document.getElementById('facturaId').value = '';
    document.getElementById('facCliente').value = '';
    document.getElementById('facObs').value = '';
    facturaDetalles = [];
    renderFacturaDetalles();
    try {
        facturaServicios = await apiGet('/medico/servicios');
        facturaMedicamentos = await apiGet('/medico/medicamentos');
    } catch(e) {}
    clearValidation('facturaForm');
    new bootstrap.Modal('#modalFactura').show();
}

function addFacturaDetalle() {
    const tipo = document.getElementById('facTipoItem').value;
    const itemId = parseInt(document.getElementById('facItemId').value);
    const cant = parseInt(document.getElementById('facCantidad').value) || 1;

    if (tipo === 'SERVICIO') {
        const s = facturaServicios.find(x => x.id === itemId);
        if (!s) { toast('Seleccione un servicio válido', 'warning'); return; }
        facturaDetalles.push({ tipoItem: tipo, servicioId: s.id, descripcionLinea: s.nombre, cantidad: cant, precioUnitario: s.precioBase, descuentoLinea: 0 });
    } else {
        const m = facturaMedicamentos.find(x => x.id === itemId);
        if (!m) { toast('Seleccione un medicamento válido', 'warning'); return; }
        facturaDetalles.push({ tipoItem: tipo, medicamentoId: m.id, descripcionLinea: m.nombre, cantidad: cant, precioUnitario: m.precioUnitario, descuentoLinea: 0 });
    }
    renderFacturaDetalles();
}

function renderFacturaDetalles() {
    const tbody = document.getElementById('facturaDetallesBody');
    let total = 0;
    tbody.innerHTML = facturaDetalles.map((d,i) => {
        const sub = (d.precioUnitario * d.cantidad) - (d.descuentoLinea || 0);
        total += sub;
        return `<tr>
            <td>${escapeHtml(d.descripcionLinea)}</td><td>${d.cantidad}</td><td>${fmt(d.precioUnitario)}</td>
            <td>${fmt(d.descuentoLinea||0)}</td><td>${fmt(sub)}</td>
            <td><button class="btn btn-sm btn-danger" onclick="removeFacturaDetalle(${i})">&times;</button></td>
        </tr>`;
    }).join('');
    document.getElementById('facturaTotal').textContent = fmt(total);
}

function removeFacturaDetalle(i) { facturaDetalles.splice(i,1); renderFacturaDetalles(); }

async function saveFactura() {
    const clienteId = parseInt(document.getElementById('facCliente').value);
    if (!clienteId || facturaDetalles.length === 0) { toast('Complete los datos', 'warning'); return; }
    try {
        await apiPost('/medico/facturas', { clienteId, detalles: facturaDetalles, observaciones: document.getElementById('facObs').value });
        bootstrap.Modal.getInstance('#modalFactura').hide();
        toast('Factura creada');
        loadFacturas();
    } catch(e) { toast(e.message, 'error'); }
}

async function viewFactura(id) {
    try {
        const f = await apiGet('/medico/facturas/' + id);
        let html = `<div class="table-responsive"><table class="table table-sm"><thead><tr><th>Descripción</th><th>Cant</th><th>P.Unit</th><th>Desc</th><th>Subtotal</th></tr></thead><tbody>`;
        f.detalles.forEach(d => { html += `<tr><td>${escapeHtml(d.descripcionLinea)}</td><td>${d.cantidad}</td><td>${fmt(d.precioUnitario)}</td><td>${fmt(d.descuentoLinea)}</td><td>${fmt(d.subtotal)}</td></tr>`; });
        html += `</tbody></table></div>`;
        html += `<p><strong>Subtotal:</strong> ${fmt(f.subtotal)} | <strong>Total:</strong> ${fmt(f.total)} | <strong>Pagado:</strong> ${fmt(f.totalPagado)} | <strong>Saldo:</strong> ${fmt(f.saldoPendiente)}</p>`;
        if (f.pagos && f.pagos.length) {
            html += `<h6>Pagos</h6><ul class="list-group">`;
            f.pagos.forEach(p => { html += `<li class="list-group-item">${fmt(p.monto)} - ${p.metodo} - ${new Date(p.fechaPago).toLocaleString()}</li>`; });
            html += `</ul>`;
        }
        document.getElementById('facturaViewContent').innerHTML = html;
        document.getElementById('facturaPdfLink').href = '/api/medico/facturas/' + id + '/pdf';
        window._currentFacturaId = id;
        new bootstrap.Modal('#modalViewFactura').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function addPago(id) {
    document.getElementById('pagoFacturaId').value = id;
    document.getElementById('pagoMonto').value = '';
    document.getElementById('pagoMetodo').value = 'EFECTIVO';
    document.getElementById('pagoRef').value = '';
    clearValidation('pagoForm');
    new bootstrap.Modal('#modalPago').show();
}

async function savePago() {
    if (!validateForm('pagoForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('pagoFacturaId').value;
    const body = { monto: parseFloat(document.getElementById('pagoMonto').value), metodo: document.getElementById('pagoMetodo').value, referencia: document.getElementById('pagoRef').value };
    try {
        await apiPost('/medico/facturas/' + id + '/pago', body);
        bootstrap.Modal.getInstance('#modalPago').hide();
        toast('Pago registrado');
        loadFacturas();
    } catch(e) { toast(e.message, 'error'); }
}

async function pagarOnline(facturaId) {
    try {
        const res = await apiPost('/pagos/stripe/create-payment-intent', { facturaId });
        if (res.mode === 'simulado') {
            if (confirm('Modo simulado: ¿Confirmar pago de $' + res.total.toFixed(2) + ' para factura ' + res.numeroFactura + '?')) {
                await apiPost('/medico/facturas/' + facturaId + '/pago', {
                    monto: res.saldoPendiente || res.total,
                    metodo: 'TARJETA_CREDITO',
                    referencia: 'SIMULADO-' + Date.now()
                });
                toast('Pago exitoso (simulado)');
                loadFacturas();
            }
        } else if (res.mode === 'stripe') {
            toast('Redirigiendo a pasarela de pago...', 'info');
            const { loadStripe } = await import('https://js.stripe.com/v3/');
            const stripe = await loadStripe(res.publishableKey || 'pk_test_placeholder');
            const { error } = await stripe.confirmCardPayment(res.clientSecret);
            if (error) { toast('Error de pago: ' + error.message, 'error'); }
            else { toast('Pago exitoso'); loadFacturas(); }
        }
    } catch(e) { toast(e.message, 'error'); }
}

/// ----- Public Booking -----

let publicVets = [];
let publicSlots = [];

async function loadPublicVets() {
    try {
        publicVets = await apiGet('/public/veterinarios');
        const sel = document.getElementById('pubVet');
        sel.innerHTML = '<option value="">Seleccione...</option>' + publicVets.map(v =>
            `<option value="${v.id}">${v.nombre} (${v.especialidad || 'General'})</option>`
        ).join('');
    } catch(e) { console.error(e); }
}

async function loadPublicSlots() {
    const vetId = document.getElementById('pubVet').value;
    const date = document.getElementById('pubDate').value;
    if (!vetId || !date) return;
    try {
        publicSlots = await apiGet('/public/slots?vetId=' + vetId + '&date=' + date);
        const container = document.getElementById('pubSlots');
        const avail = publicSlots.filter(s => s.disponible);
        if (avail.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No hay horarios disponibles para esta fecha.</div>';
        } else {
            container.innerHTML = avail.map(s =>
                `<button class="btn btn-outline-primary btn-sm m-1 slot-btn" data-hora="${s.hora}" onclick="selectSlot(this)">${s.hora}</button>`
            ).join('');
        }
    } catch(e) { toast(e.message, 'error'); }
}

function selectSlot(btn) {
    document.querySelectorAll('.slot-btn').forEach(b => b.classList.remove('btn-primary', 'active'));
    btn.classList.add('btn-primary', 'active');
    document.getElementById('pubHora').value = btn.dataset.hora;
}

async function savePublicCita() {
    const vetId = document.getElementById('pubVet').value;
    const date = document.getElementById('pubDate').value;
    const hora = document.getElementById('pubHora').value;
    const mascotaId = document.getElementById('pubMascotaId').value;
    const email = document.getElementById('pubEmail').value;
    const motivo = document.getElementById('pubMotivo').value;

    if (!vetId || !date || !hora || !mascotaId) {
        toast('Complete todos los campos requeridos', 'warning'); return;
    }

    try {
        const res = await fetch('/api/public/citas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ veterinarioId: vetId, fecha: date, hora, mascotaId, clienteEmail: email, motivo })
        });
        if (!res.ok) { const err = await res.json(); throw new Error(err.error || 'Error al agendar'); }
        toast('Cita agendada exitosamente. Te enviaremos un recordatorio.');
        document.getElementById('pubVet').value = '';
        document.getElementById('pubDate').value = '';
        document.getElementById('pubHora').value = '';
        document.getElementById('pubMascotaId').value = '';
        document.getElementById('pubEmail').value = '';
        document.getElementById('pubMotivo').value = '';
        document.getElementById('pubSlots').innerHTML = '';
        document.getElementById('pubResult').classList.remove('d-none');
    } catch(e) { toast(e.message, 'error'); }
}

async function loadHistorial(mascotaId) {
    try {
        const hs = await apiGet('/medico/historial/mascota/' + mascotaId);
        document.getElementById('tablaHistorial').innerHTML = hs.map(h => `<tr>
            <td>${new Date(h.fechaConsulta).toLocaleDateString()}</td>
            <td>${escapeHtml(h.motivo)}</td>
            <td>${h.diagnostico || '-'}</td>
            <td>${h.medicacionIndicada || '-'}</td>
            <td>
                <button class="btn btn-sm btn-outline-warning" onclick="editHistorial(${h.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteHistorial(${h.id})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function saveHistorial() {
    if (!validateForm('historialForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const body = {
        mascota: { id: currentMascotaId },
        motivo: document.getElementById('histMotivo').value,
        diagnostico: document.getElementById('histDiagnostico').value,
        procedimientoRealizado: document.getElementById('histProcedimiento').value,
        medicacionIndicada: document.getElementById('histMedicacion').value,
        observaciones: document.getElementById('histObs').value
    };
    try {
        await apiPost('/medico/historial', body);
        bootstrap.Modal.getInstance('#modalHistorial').hide();
        toast('Historial registrado');
        loadHistorial(currentMascotaId);
    } catch(e) { toast(e.message, 'error'); }
}

async function deleteHistorial(id) {
    if (!confirm('¿Eliminar registro?')) return;
    try { await apiDelete('/medico/historial/' + id); loadHistorial(currentMascotaId); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadVacunas() {
    try {
        if (!currentMascotaId) return;
        const vs = await apiGet('/medico/vacunas/mascota/' + currentMascotaId);
        document.getElementById('tablaVacunas').innerHTML = vs.map(v => `<tr>
            <td>${escapeHtml(v.nombre)}</td><td>${v.fechaAplicacion}</td>
            <td>${v.fechaProximaDosis || '-'}</td><td>${v.fabricante || '-'}</td>
            <td><button class="btn btn-sm btn-outline-danger" onclick="deleteVacuna(${v.id})"><i class="bi bi-trash"></i></button></td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function saveVacuna() {
    if (!validateForm('vacunaForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const body = {
        mascota: { id: currentMascotaId },
        nombre: document.getElementById('vacNombre').value,
        fechaAplicacion: document.getElementById('vacFecha').value,
        fechaProximaDosis: document.getElementById('vacProxima').value || null,
        numeroLote: document.getElementById('vacLote').value,
        fabricante: document.getElementById('vacFab').value,
        observaciones: document.getElementById('vacObs').value
    };
    try {
        await apiPost('/medico/vacunas', body);
        bootstrap.Modal.getInstance('#modalVacuna').hide();
        toast('Vacuna registrada');
        loadVacunas();
    } catch(e) { toast(e.message, 'error'); }
}

async function deleteVacuna(id) {
    if (!confirm('¿Eliminar vacuna?')) return;
    try { await apiDelete('/medico/vacunas/' + id); loadVacunas(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadHospitalizaciones() {
    try {
        const hs = await apiGet('/medico/hospitalizaciones');
        document.getElementById('tablaHospitalizaciones').innerHTML = hs.map(h => `<tr>
            <td>${h.id}</td><td>${h.mascota ? escapeHtml(h.mascota.nombre) : '-'}</td>
            <td>${new Date(h.checkIn).toLocaleDateString()}</td>
            <td>${h.motivo || '-'}</td><td>${h.jaula || '-'}</td>
            <td><span class="badge bg-${h.estado==='HOSPITALIZADO'?'warning':'success'}">${h.estado}</span></td>
            <td>
                ${h.estado==='HOSPITALIZADO'?`<button class="btn btn-sm btn-outline-success" onclick="darAlta(${h.id})"><i class="bi bi-check-lg"></i></button>`:''}
                <button class="btn btn-sm btn-outline-danger" onclick="deleteHospitalizacion(${h.id})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function saveHospitalizacion() {
    if (!validateForm('hospForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const body = {
        mascota: { id: parseInt(document.getElementById('hospMascotaId').value) },
        motivo: document.getElementById('hospMotivo').value,
        jaula: document.getElementById('hospJaula').value,
        notas: document.getElementById('hospNotas').value
    };
    try {
        await apiPost('/medico/hospitalizaciones', body);
        bootstrap.Modal.getInstance('#modalHospitalizacion').hide();
        toast('Hospitalización registrada');
        loadHospitalizaciones();
    } catch(e) { toast(e.message, 'error'); }
}

async function darAlta(id) {
    if (!confirm('¿Dar de alta?')) return;
    try { await apiPut('/medico/hospitalizaciones/' + id + '/alta', {}); toast('Alta registrada'); loadHospitalizaciones(); }
    catch(e) { toast(e.message, 'error'); }
}

async function deleteHospitalizacion(id) {
    if (!confirm('¿Eliminar?')) return;
    try { await apiDelete('/medico/hospitalizaciones/' + id); loadHospitalizaciones(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadPlanes() {
    try {
        if (!currentMascotaId) return;
        const ps = await apiGet('/medico/planes/mascota/' + currentMascotaId);
        document.getElementById('tablaPlanes').innerHTML = ps.map(p => `<tr>
            <td>${p.id}</td><td>${escapeHtml(p.titulo)}</td>
            <td><span class="badge bg-${p.estado==='ACTIVO'?'primary':p.estado==='COMPLETADO'?'success':'secondary'}">${p.estado}</span></td>
            <td>${p.costoEstimado ? fmt(p.costoEstimado) : '-'}</td>
            <td>${p.pasos ? p.pasos.filter(x=>x.estado==='COMPLETADO').length+'/'+p.pasos.length : 0}</td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="viewPlan(${p.id})"><i class="bi bi-eye"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="deletePlan(${p.id})"><i class="bi bi-trash"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function savePlan() {
    if (!validateForm('planForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const body = {
        mascota: { id: currentMascotaId },
        titulo: document.getElementById('planTitulo').value,
        descripcion: document.getElementById('planDesc').value,
        costoEstimado: parseFloat(document.getElementById('planCosto').value) || null
    };
    try {
        await apiPost('/medico/planes', body);
        bootstrap.Modal.getInstance('#modalPlan').hide();
        toast('Plan creado');
        loadPlanes();
    } catch(e) { toast(e.message, 'error'); }
}

async function viewPlan(id) {
    try {
        const p = await apiGet('/medico/planes/' + id);
        let html = `<h5>${escapeHtml(p.titulo)}</h5><p>${p.descripcion || ''}</p>`;
        if (p.pasos && p.pasos.length) {
            html += `<ul class="list-group">`;
            p.pasos.forEach(ps => {
                html += `<li class="list-group-item d-flex justify-content-between align-items-center">
                    ${ps.orden}. ${escapeHtml(ps.descripcion)}
                    <span class="badge bg-${ps.estado==='COMPLETADO'?'success':ps.estado==='EN_PROCESO'?'warning':'secondary'}">${ps.estado}</span>
                </li>`;
            });
            html += `</ul>`;
        }
        document.getElementById('planViewContent').innerHTML = html;
        new bootstrap.Modal('#modalViewPlan').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function deletePlan(id) {
    if (!confirm('¿Eliminar plan?')) return;
    try { await apiDelete('/medico/planes/' + id); loadPlanes(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadConsentimientos() {
    try {
        const cs = await apiGet('/medico/consentimientos');
        document.getElementById('tablaConsentimientos').innerHTML = cs.map(c => `<tr>
            <td>${c.id}</td><td>${escapeHtml(c.titulo)}</td><td>${c.tipoProcedimiento}</td>
            <td>${c.cliente ? escapeHtml(c.cliente.nombre+' '+c.cliente.apellido) : '-'}</td>
            <td><span class="badge bg-${c.firmado?'success':'warning'}">${c.firmado?'Firmado':'Pendiente'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-info" onclick="viewConsentimiento(${c.id})"><i class="bi bi-eye"></i></button>
                <a class="btn btn-sm btn-outline-secondary" href="/api/medico/consentimientos/${c.id}/pdf" target="_blank"><i class="bi bi-file-pdf"></i></a>
                ${!c.firmado ? `<button class="btn btn-sm btn-outline-success" onclick="firmarConsentimiento(${c.id})"><i class="bi bi-pen"></i></button>` : ''}
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function saveConsentimiento() {
    if (!validateForm('consForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const body = {
        cliente: { id: parseInt(document.getElementById('consClienteId').value) },
        titulo: document.getElementById('consTitulo').value,
        tipoProcedimiento: document.getElementById('consTipo').value,
        contenido: document.getElementById('consContenido').value
    };
    try {
        await apiPost('/medico/consentimientos', body);
        bootstrap.Modal.getInstance('#modalConsentimiento').hide();
        toast('Consentimiento creado');
        loadConsentimientos();
    } catch(e) { toast(e.message, 'error'); }
}

async function firmarConsentimiento(id) {
    const nombre = prompt('Nombre del firmante:');
    if (!nombre) return;
    try { await apiPatch('/medico/consentimientos/' + id + '/firmar', { nombreFirmante: nombre }); toast('Firmado'); loadConsentimientos(); }
    catch(e) { toast(e.message, 'error'); }
}

async function viewConsentimiento(id) {
    try {
        const c = await apiGet('/medico/consentimientos/' + id);
        document.getElementById('viewConsTitulo').textContent = c.titulo;
        document.getElementById('viewConsTipo').textContent = c.tipoProcedimiento;
        document.getElementById('viewConsContenido').textContent = c.contenido;
        document.getElementById('viewConsEstado').textContent = c.firmado ? 'Firmado por ' + c.nombreFirmante + ' el ' + new Date(c.fechaFirma).toLocaleDateString() : 'Pendiente de firma';
        new bootstrap.Modal('#modalViewConsentimiento').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function initReportes() {
    if (reportChart) { reportChart.destroy(); reportChart = null; }
    try {
        const r = await apiGet('/medico/reportes/ingresos');
        document.getElementById('reporteTotal').textContent = fmt(r.totalIngresos);
        const ctx = document.getElementById('reportChart');
        if (ctx) {
            reportChart = new Chart(ctx, {
                type: 'bar',
                data: { labels: ['Ingresos'], datasets: [{ label: 'Total', data: [r.totalIngresos], backgroundColor: '#0d6efd' }] }
            });
        }
    } catch(e) { toast(e.message, 'error'); }
}

async function downloadExcel() {
    try {
        const blob = await apiGetBlob('/medico/reportes/ingresos/excel');
        const url = URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url; a.download = 'ingresos.xlsx'; a.click();
        URL.revokeObjectURL(url);
    } catch(e) { toast(e.message, 'error'); }
}

async function loadPerfil() {
    try {
        const user = getUser();
        document.getElementById('perfNombre').value = user.nombre || '';
    } catch(e) {}
}

async function updatePerfil() {
    const body = {
        nombre: document.getElementById('perfNombre').value,
        apellido: document.getElementById('perfApellido').value,
        telefono: document.getElementById('perfTel').value
    };
    try {
        await apiPut('/medico/perfil', body);
        toast('Perfil actualizado');
    } catch(e) { toast(e.message, 'error'); }
}

async function changePassword() {
    const body = {
        passwordActual: document.getElementById('passActual').value,
        passwordNueva: document.getElementById('passNueva').value
    };
    try {
        await apiPost('/medico/cambiar-password', body);
        toast('Contraseña cambiada');
        document.getElementById('passActual').value = '';
        document.getElementById('passNueva').value = '';
    } catch(e) { toast(e.message, 'error'); }
}

async function loadAdmin() {
    try {
        const vs = await apiGet('/medico/veterinarios');
        const user = getUser();
        if (user.rol !== 'ADMIN') { document.getElementById('adminPanel').innerHTML = '<div class="alert alert-danger">Acceso restringido a Administradores</div>'; return; }
        document.getElementById('tablaVeterinarios').innerHTML = vs.map(v => `<tr>
            <td>${v.id}</td><td>${escapeHtml(v.nombre)} ${escapeHtml(v.apellido)}</td>
            <td>${v.email}</td><td>${v.especialidad || '-'}</td>
            <td><span class="badge bg-${v.rol==='ADMIN'?'danger':v.rol==='VETERINARIO'?'primary':'secondary'}">${v.rol}</span></td>
            <td><span class="badge bg-${v.activo?'success':'secondary'}">${v.activo?'Activo':'Inactivo'}</span></td>
            <td>
                <button class="btn btn-sm btn-outline-warning" onclick="editVeterinario(${v.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="toggleVet(${v.id})"><i class="bi bi-power"></i></button>
            </td>
        </tr>`).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function editVeterinario(id) {
    try {
        const vs = await apiGet('/medico/veterinarios');
        const v = vs.find(x => x.id === id);
        document.getElementById('vetId').value = v.id;
        document.getElementById('vetNombre').value = v.nombre;
        document.getElementById('vetApellido').value = v.apellido;
        document.getElementById('vetEmail').value = v.email;
        document.getElementById('vetEspecialidad').value = v.especialidad || '';
        document.getElementById('vetCedula').value = v.cedulaProfesional || '';
        document.getElementById('vetRol').value = v.rol;
        document.getElementById('vetPass').value = '';
        document.getElementById('vetInicio').value = v.horarioInicio || '';
        document.getElementById('vetFin').value = v.horarioFin || '';
        document.getElementById('vetDuracion').value = v.duracionTurnoMinutos || 30;
        clearValidation('vetForm');
        new bootstrap.Modal('#modalVeterinario').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function saveVeterinario() {
    if (!validateForm('vetForm')) { toast('Complete los campos requeridos', 'warning'); return; }
    const id = document.getElementById('vetId').value;
    const body = {
        nombre: document.getElementById('vetNombre').value,
        apellido: document.getElementById('vetApellido').value,
        email: document.getElementById('vetEmail').value,
        especialidad: document.getElementById('vetEspecialidad').value,
        cedulaProfesional: document.getElementById('vetCedula').value,
        rol: document.getElementById('vetRol').value,
        passwordHash: document.getElementById('vetPass').value,
        horarioInicio: document.getElementById('vetInicio').value || null,
        horarioFin: document.getElementById('vetFin').value || null,
        duracionTurnoMinutos: parseInt(document.getElementById('vetDuracion').value) || 30
    };
    try {
        if (id) await apiPut('/medico/veterinarios/' + id, body);
        else await apiPost('/medico/veterinarios', body);
        bootstrap.Modal.getInstance('#modalVeterinario').hide();
        toast('Veterinario guardado');
        loadAdmin();
    } catch(e) { toast(e.message, 'error'); }
}

async function toggleVet(id) {
    if (!confirm('¿Desactivar/activar veterinario?')) return;
    try { await apiDelete('/medico/veterinarios/' + id); loadAdmin(); }
    catch(e) { toast(e.message, 'error'); }
}

async function loadAuditoria() {
    try {
        const logs = await apiGet('/admin/auditoria');
        document.getElementById('tablaAuditoria').innerHTML = logs.map(l => `<tr>
            <td>${l.id}</td><td>${l.accion}</td><td>${l.entidad}</td>
            <td>${l.detalle || '-'}</td>
            <td>${l.fechaAccion ? new Date(l.fechaAccion).toLocaleString() : '-'}</td>
        </tr>`).join('');
    } catch(e) {
        document.getElementById('tablaAuditoria').innerHTML = '<tr><td colspan="5">Error al cargar auditoría</td></tr>';
    }
}

function escapeHtml(str) {
    if (!str) return '';
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
}

function openNewCita() {
    document.getElementById('citaId').value = '';
    document.getElementById('citaMascota').value = '';
    document.getElementById('citaMotivo').value = '';
    document.getElementById('citaNotas').value = '';
    document.getElementById('citaInicio').value = '';
    document.getElementById('citaFin').value = '';
    document.getElementById('citaEstado').value = 'PROGRAMADA';
    document.getElementById('citaCancelMotivoInput').value = '';
    document.getElementById('citaCancelMotivo').style.display = 'none';
    clearValidation('citaForm');
    new bootstrap.Modal('#modalCita').show();
}

function openNewCliente() {
    document.getElementById('clienteId').value = '';
    ['cliNombre','cliApellido','cliEmail','cliTel','cliDir','cliRfc'].forEach(id => document.getElementById(id).value = '');
    clearValidation('clienteForm');
    new bootstrap.Modal('#modalCliente').show();
}

function openNewMascota() {
    document.getElementById('mascotaId').value = '';
    ['petNombre','petRaza','petColor','petPeso','petAlergias','petCondiciones','petClienteId'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('petEspecie').value = 'CANINO';
    document.getElementById('petGenero').value = 'MACHO';
    clearValidation('mascotaForm');
    new bootstrap.Modal('#modalMascota').show();
}

function openNewServicio() {
    document.getElementById('servId').value = '';
    ['servNombre','servDesc','servCodigo'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('servPrecio').value = '';
    clearValidation('servicioForm');
    new bootstrap.Modal('#modalServicio').show();
}

function openNewMedicamento() {
    document.getElementById('medId').value = '';
    ['medNombre','medDesc'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('medUnidad').value = 'tableta';
    document.getElementById('medPrecio').value = '';
    document.getElementById('medStockMin').value = 5;
    clearValidation('medicamentoForm');
    new bootstrap.Modal('#modalMedicamento').show();
}

function openNewHistorial() {
    if (!currentMascotaId) { toast('Seleccione una mascota primero', 'warning'); return; }
    document.getElementById('histMotivo').value = '';
    document.getElementById('histDiagnostico').value = '';
    document.getElementById('histProcedimiento').value = '';
    document.getElementById('histMedicacion').value = '';
    document.getElementById('histObs').value = '';
    clearValidation('historialForm');
    new bootstrap.Modal('#modalHistorial').show();
}

function openNewVacuna() {
    if (!currentMascotaId) { toast('Seleccione una mascota primero', 'warning'); return; }
    document.getElementById('vacNombre').value = '';
    document.getElementById('vacFecha').value = new Date().toISOString().substring(0,10);
    document.getElementById('vacProxima').value = '';
    document.getElementById('vacLote').value = '';
    document.getElementById('vacFab').value = '';
    document.getElementById('vacObs').value = '';
    clearValidation('vacunaForm');
    new bootstrap.Modal('#modalVacuna').show();
}

function openNewHospitalizacion() {
    document.getElementById('hospMascotaId').value = '';
    document.getElementById('hospMotivo').value = '';
    document.getElementById('hospJaula').value = '';
    document.getElementById('hospNotas').value = '';
    clearValidation('hospForm');
    new bootstrap.Modal('#modalHospitalizacion').show();
}

function openNewPlan() {
    if (!currentMascotaId) { toast('Seleccione una mascota primero', 'warning'); return; }
    document.getElementById('planTitulo').value = '';
    document.getElementById('planDesc').value = '';
    document.getElementById('planCosto').value = '';
    clearValidation('planForm');
    new bootstrap.Modal('#modalPlan').show();
}

function openNewConsentimiento() {
    document.getElementById('consClienteId').value = '';
    document.getElementById('consTitulo').value = '';
    document.getElementById('consTipo').value = '';
    document.getElementById('consContenido').value = '';
    clearValidation('consForm');
    new bootstrap.Modal('#modalConsentimiento').show();
}

function openNewVeterinario() {
    document.getElementById('vetId').value = '';
    ['vetNombre','vetApellido','vetEmail','vetEspecialidad','vetCedula','vetPass','vetInicio','vetFin'].forEach(id => document.getElementById(id).value = '');
    document.getElementById('vetRol').value = 'VETERINARIO';
    document.getElementById('vetDuracion').value = 30;
    clearValidation('vetForm');
    new bootstrap.Modal('#modalVeterinario').show();
}

const CANINE_TEETH = {
    upperRight: [
        { num: 110, label: 'M2' }, { num: 109, label: 'M1' },
        { num: 108, label: 'P4' }, { num: 107, label: 'P3' }, { num: 106, label: 'P2' }, { num: 105, label: 'P1' },
        { num: 104, label: 'C' },
        { num: 103, label: 'I3' }, { num: 102, label: 'I2' }, { num: 101, label: 'I1' }
    ],
    upperLeft: [
        { num: 201, label: 'I1' }, { num: 202, label: 'I2' }, { num: 203, label: 'I3' },
        { num: 204, label: 'C' },
        { num: 205, label: 'P1' }, { num: 206, label: 'P2' }, { num: 207, label: 'P3' }, { num: 208, label: 'P4' },
        { num: 209, label: 'M1' }, { num: 210, label: 'M2' }
    ],
    lowerLeft: [
        { num: 311, label: 'M3' }, { num: 310, label: 'M2' }, { num: 309, label: 'M1' },
        { num: 308, label: 'P4' }, { num: 307, label: 'P3' }, { num: 306, label: 'P2' }, { num: 305, label: 'P1' },
        { num: 304, label: 'C' },
        { num: 303, label: 'I3' }, { num: 302, label: 'I2' }, { num: 301, label: 'I1' }
    ],
    lowerRight: [
        { num: 401, label: 'I1' }, { num: 402, label: 'I2' }, { num: 403, label: 'I3' },
        { num: 404, label: 'C' },
        { num: 405, label: 'P1' }, { num: 406, label: 'P2' }, { num: 407, label: 'P3' }, { num: 408, label: 'P4' },
        { num: 409, label: 'M1' }, { num: 410, label: 'M2' }, { num: 411, label: 'M3' }
    ]
};

const TOOTH_COLORS = {
    SANO: '#4caf50', CARIES: '#ff5722', FRACTURA: '#f44336', AUSENTE: '#9e9e9e',
    RETENIDO: '#ff9800', PERIODONTAL: '#e91e63', TARTARO: '#795548', OBTURADO: '#2196f3', ENDODONCIA: '#9c27b0'
};

const TOOTH_LABELS = {
    SANO: 'Sano', CARIES: 'Caries', FRACTURA: 'Fracturado', AUSENTE: 'Ausente',
    RETENIDO: 'Retenido', PERIODONTAL: 'Periodontal', TARTARO: 'Tártaro', OBTURADO: 'Obturado', ENDODONCIA: 'Endodoncia'
};

let odontoSelectedEstado = 'SANO';
let odontoCurrentData = {};

function renderOdontogramaChart(containerId, toothData, readOnly) {
    const container = document.getElementById(containerId);
    if (!container) return;
    const data = toothData || {};
    let html = '<div class="odonto-arch">';
    html += '<div class="odonto-label-jaw">Superior (Maxilar)</div>';
    html += '<div class="odonto-row">';
    CANINE_TEETH.upperRight.forEach(t => html += toothCell(t, data, readOnly));
    html += '<div class="odonto-divider"></div>';
    CANINE_TEETH.upperLeft.forEach(t => html += toothCell(t, data, readOnly));
    html += '</div>';
    html += '<div class="odonto-divider-jaw"><div class="odonto-midline">●</div></div>';
    html += '<div class="odonto-row">';
    CANINE_TEETH.lowerLeft.forEach(t => html += toothCell(t, data, readOnly));
    html += '<div class="odonto-divider"></div>';
    CANINE_TEETH.lowerRight.forEach(t => html += toothCell(t, data, readOnly));
    html += '</div>';
    html += '<div class="odonto-label-jaw">Inferior (Mandíbula)</div>';
    html += '</div>';
    container.innerHTML = html;
}

function toothCell(t, data, readOnly) {
    const key = t.num;
    const estado = data[key] || 'SANO';
    const color = TOOTH_COLORS[estado] || '#4caf50';
    const cls = readOnly ? 'odonto-tooth odonto-tooth-ro' : 'odonto-tooth';
    const onclick = readOnly ? '' : ` onclick="odontoClickTooth(${t.num})"`;
    return `<div class="${cls}" style="background-color:${color}" data-tooth="${t.num}" data-estado="${estado}"${onclick}>
        <span class="odonto-tooth-label">${t.label}</span>
        <span class="odonto-tooth-num">${t.num}</span>
    </div>`;
}

function odontoClickTooth(num) {
    const el = document.querySelector(`.odontograma-chart [data-tooth="${num}"]`);
    if (!el) return;
    const estado = odontoSelectedEstado;
    const color = TOOTH_COLORS[estado] || '#4caf50';
    el.style.backgroundColor = color;
    el.dataset.estado = estado;
    odontoCurrentData[num] = estado;
}

function collectToothData() {
    const data = {};
    document.querySelectorAll('#odontoChart .odonto-tooth').forEach(el => {
        const num = parseInt(el.dataset.tooth);
        const estado = el.dataset.estado || 'SANO';
        data[num] = estado;
    });
    return data;
}

function buildToothDetails(data) {
    const detalles = [];
    const lookup = {};
    Object.keys(CANINE_TEETH).forEach(quad => {
        CANINE_TEETH[quad].forEach(t => { lookup[t.num] = { cuadrante: quad, diente: t.label }; });
    });
    Object.keys(data).forEach(numStr => {
        const num = parseInt(numStr);
        const estado = data[num];
        const info = lookup[num] || { cuadrante: 'unknown', diente: '?' };
        detalles.push({ toothNumber: num, cuadrante: info.cuadrante, diente: info.diente, estado: estado, observacion: '', colorHex: TOOTH_COLORS[estado] || '#4caf50' });
    });
    return detalles;
}

async function openOdontograma(mascotaId, mascotaNombre) {
    currentMascotaId = mascotaId;
    document.getElementById('odontoMascotaId').value = mascotaId;
    document.getElementById('odontoListMascotaNombre').textContent = mascotaNombre;
    const listBody = document.getElementById('odontoListBody');
    try {
        const list = await apiGet('/medico/mascotas/' + mascotaId + '/odontogramas');
        if (!list || list.length === 0) {
            listBody.innerHTML = '<div class="alert alert-info">No hay odontogramas. Cree uno nuevo.</div>';
        } else {
            listBody.innerHTML = list.map(o => `
                <div class="d-flex justify-content-between align-items-center border-bottom py-2">
                    <div>
                        <strong>${new Date(o.fecha).toLocaleDateString()}</strong>
                        <small class="text-muted ms-2">${o.veterinarioNombre || ''}</small>
                        ${o.notas ? '<br><small>' + escapeHtml(o.notas.substring(0, 60)) + '</small>' : ''}
                    </div>
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-info" onclick="viewOdontograma(${o.id})"><i class="bi bi-eye"></i></button>
                        <button class="btn btn-outline-warning" onclick="editOdontograma(${o.id})"><i class="bi bi-pencil"></i></button>
                    </div>
                </div>
            `).join('');
        }
        new bootstrap.Modal('#modalOdontogramaList').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function viewOdontograma(id) {
    try {
        const o = await apiGet('/medico/odontogramas/' + id);
        document.querySelector('#modalOdontogramaView .modal-content').dataset.odontoId = id;
        document.getElementById('odontoViewFecha').textContent = new Date(o.fecha).toLocaleDateString();
        document.getElementById('odontoViewVet').textContent = o.veterinarioNombre || '-';
        document.getElementById('odontoViewNotas').textContent = o.notas || '-';
        document.getElementById('odontoViewMascotaNombre').textContent = document.getElementById('odontoListMascotaNombre').textContent;
        const data = {};
        (o.detalles || []).forEach(d => { data[d.toothNumber] = d.estado; });
        renderOdontogramaChart('odontoViewChart', data, true);
        bootstrap.Modal.getInstance('#modalOdontogramaList').hide();
        new bootstrap.Modal('#modalOdontogramaView').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function editOdontograma(id) {
    try {
        const o = await apiGet('/medico/odontogramas/' + id);
        document.getElementById('odontoId').value = o.id;
        document.getElementById('odontoMascotaId').value = o.mascotaId;
        document.getElementById('odontoFecha').value = o.fecha;
        document.getElementById('odontoNotas').value = o.notas || '';
        document.getElementById('odontoMascotaNombre').textContent = document.getElementById('odontoListMascotaNombre').textContent;
        const data = {};
        (o.detalles || []).forEach(d => { data[d.toothNumber] = d.estado; });
        odontoCurrentData = data;
        renderOdontogramaChart('odontoChart', data, false);
        document.getElementById('odontoDeleteBtn').style.display = '';
        odontoSelectedEstado = 'SANO';
        updateOdontoToolbar('SANO');
        try { bootstrap.Modal.getInstance('#modalOdontogramaList').hide(); } catch(e) {}
        try { bootstrap.Modal.getInstance('#modalOdontogramaView').hide(); } catch(e) {}
        new bootstrap.Modal('#modalOdontograma').show();
    } catch(e) { toast(e.message, 'error'); }
}

async function editOdontogramaFromView() {
    const oId = document.querySelector('#modalOdontogramaView .modal-content').dataset.odontoId;
    if (oId) { bootstrap.Modal.getInstance('#modalOdontogramaView').hide(); editOdontograma(parseInt(oId)); }
}

function newOdontograma() {
    const mascotaId = parseInt(document.getElementById('odontoMascotaId').value);
    const name = document.getElementById('odontoListMascotaNombre').textContent;
    document.getElementById('odontoId').value = '';
    document.getElementById('odontoMascotaId').value = mascotaId;
    document.getElementById('odontoFecha').value = new Date().toISOString().substring(0, 10);
    document.getElementById('odontoNotas').value = '';
    document.getElementById('odontoMascotaNombre').textContent = name;
    odontoCurrentData = {};
    renderOdontogramaChart('odontoChart', {}, false);
    document.getElementById('odontoDeleteBtn').style.display = 'none';
    odontoSelectedEstado = 'SANO';
    updateOdontoToolbar('SANO');
    bootstrap.Modal.getInstance('#modalOdontogramaList').hide();
    new bootstrap.Modal('#modalOdontograma').show();
}

async function saveOdontograma() {
    const mascotaId = parseInt(document.getElementById('odontoMascotaId').value);
    const odontoId = document.getElementById('odontoId').value;
    const data = collectToothData();
    const body = {
        fecha: document.getElementById('odontoFecha').value,
        notas: document.getElementById('odontoNotas').value,
        detalles: buildToothDetails(data)
    };
    try {
        if (odontoId) {
            await apiPut('/medico/odontogramas/' + parseInt(odontoId), body);
        } else {
            await apiPost('/medico/mascotas/' + mascotaId + '/odontogramas', body);
        }
        bootstrap.Modal.getInstance('#modalOdontograma').hide();
        toast('Odontograma guardado');
    } catch(e) { toast(e.message, 'error'); }
}

async function deleteCurrentOdontograma() {
    const odontoId = document.getElementById('odontoId').value;
    if (!odontoId || !confirm('¿Eliminar este odontograma?')) return;
    try {
        await apiDelete('/medico/odontogramas/' + parseInt(odontoId));
        bootstrap.Modal.getInstance('#modalOdontograma').hide();
        toast('Odontograma eliminado');
    } catch(e) { toast(e.message, 'error'); }
}

function showOdontogramHelp() {
    const content = document.getElementById('odontoHelpContent');
    content.innerHTML = Object.keys(TOOTH_COLORS).map(k =>
        `<div class="d-flex align-items-center mb-1"><span style="display:inline-block;width:20px;height:20px;border-radius:4px;background:${TOOTH_COLORS[k]};margin-right:8px"></span><strong>${TOOTH_LABELS[k]}</strong></div>`
    ).join('');
    new bootstrap.Modal('#modalOdontogramaHelp').show();
}

function updateOdontoToolbar(estado) {
    document.querySelectorAll('#odontoToolbar .btn').forEach(b => {
        b.classList.toggle('active', b.dataset.estado === estado);
    });
}

document.addEventListener('click', function(e) {
    const btn = e.target.closest('#odontoToolbar .btn');
    if (btn) {
        odontoSelectedEstado = btn.dataset.estado;
        updateOdontoToolbar(odontoSelectedEstado);
    }
});

function showClientSection(name) {
    document.querySelectorAll('.section-content').forEach(s => s.classList.add('d-none'));
    const sec = document.getElementById('sec-cliente-' + name);
    if (sec) sec.classList.remove('d-none');
    if (name === 'inicio') loadClienteDashboard();
    if (name === 'mascotas') loadClienteMascotas();
    if (name === 'citas') loadClienteCitas();
    if (name === 'facturas') loadClienteFacturas();
    if (name === 'perfil') loadClientePerfil();
}

async function loadClienteDashboard() {
    try {
        const p = await apiGet('/portal/perfil');
        document.getElementById('cliWelcomeName').innerHTML = `<h5>Bienvenido, ${escapeHtml(p.nombre || '')} ${escapeHtml(p.apellido || '')}</h5>`;
        document.getElementById('cliStatMascotas').textContent = (p.mascotas || []).length;

        const citas = await apiGet('/portal/citas');
        const prox = (citas || []).filter(c => c.estado === 'PROGRAMADA' || c.estado === 'CONFIRMADA');
        document.getElementById('cliStatCitas').textContent = prox.length;

        const facturas = await apiGet('/portal/facturas');
        const pend = (facturas || []).reduce((s, f) => s + (f.saldoPendiente || 0), 0);
        document.getElementById('cliStatPendiente').textContent = fmt(pend);

        let totalRegistros = 0;
        if (p.mascotas) {
            for (const m of p.mascotas) {
                try {
                    const h = await apiGet('/portal/historial/' + m.id);
                    totalRegistros += (h || []).length;
                } catch(e) {}
            }
        }
        document.getElementById('cliStatHistorial').textContent = totalRegistros;
    } catch(e) { toast(e.message, 'error'); }
}

async function loadClienteMascotas() {
    try {
        const p = await apiGet('/portal/perfil');
        const container = document.getElementById('clienteMascotasList');
        const mascotas = p.mascotas || [];
        if (mascotas.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No tienes mascotas registradas.</div>';
            return;
        }
        container.innerHTML = mascotas.map(m => `
            <div class="col-md-4">
                <div class="card h-100">
                    <div class="card-body">
                        <h5 class="card-title">${escapeHtml(m.nombre)}</h5>
                        <p class="card-text small">
                            <strong>Especie:</strong> ${m.especie || '-'}<br>
                            <strong>Raza:</strong> ${escapeHtml(m.raza || '-')}<br>
                            <strong>Género:</strong> ${m.genero || '-'}<br>
                            <strong>Peso:</strong> ${m.peso || '-'} kg
                        </p>
                        <button class="btn btn-sm btn-outline-info" onclick="viewClienteMascota(${m.id})">Ver Historial</button>
                    </div>
                </div>
            </div>
        `).join('');
    } catch(e) { toast(e.message, 'error'); }
}

let clienteMascotaViewId = null;

async function viewClienteMascota(id) {
    clienteMascotaViewId = id;
    try {
        const h = await apiGet('/portal/historial/' + id);
        const p = await apiGet('/portal/perfil');
        const mascota = (p.mascotas || []).find(m => m.id === id);
        const modal = document.getElementById('modalClienteHistorial');
        document.getElementById('cliHistMascotaNombre').textContent = mascota ? mascota.nombre : 'Mascota #' + id;

        const container = document.getElementById('cliHistorialList');
        if (!h || h.length === 0) {
            container.innerHTML = '<div class="alert alert-info">No hay registros médicos.</div>';
        } else {
            container.innerHTML = h.slice(0, 10).map(r => `
                <div class="card mb-2">
                    <div class="card-body py-2">
                        <small class="text-muted">${new Date(r.fechaConsulta).toLocaleDateString()}</small>
                        <p class="mb-1"><strong>${escapeHtml(r.motivo || '')}</strong></p>
                        <p class="mb-0 small">${escapeHtml(r.diagnostico || '')}</p>
                    </div>
                </div>
            `).join('');
        }
        new bootstrap.Modal(modal).show();
    } catch(e) { toast(e.message, 'error'); }
}

async function loadClienteCitas() {
    try {
        const citas = await apiGet('/portal/citas');
        const tbody = document.getElementById('clienteCitasTable');
        if (!citas || citas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center">No tienes citas registradas.</td></tr>';
            return;
        }
        tbody.innerHTML = citas.map(c => `
            <tr>
                <td>${new Date(c.fechaHoraInicio).toLocaleString()}</td>
                <td>${escapeHtml(c.mascotaNombre || '-')}</td>
                <td>${escapeHtml(c.veterinarioNombre || '-')}</td>
                <td>${escapeHtml(c.motivo || '-')}</td>
                <td><span class="badge bg-${c.estado==='PROGRAMADA'||c.estado==='CONFIRMADA'?'success':c.estado==='CANCELADA'||c.estado==='NO_ASISTIO'?'danger':'warning'}">${c.estado}</span></td>
            </tr>
        `).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function loadClienteFacturas() {
    try {
        const facturas = await apiGet('/portal/facturas');
        const tbody = document.getElementById('clienteFacturasTable');
        if (!facturas || facturas.length === 0) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center">No tienes facturas.</td></tr>';
            return;
        }
        tbody.innerHTML = facturas.map(f => `
            <tr>
                <td>${escapeHtml(f.numeroFactura || '')}</td>
                <td>${new Date(f.fechaEmision).toLocaleDateString()}</td>
                <td>${fmt(f.total)}</td>
                <td>${fmt(f.totalPagado)}</td>
                <td><span class="badge bg-${f.estado==='PAGADA'?'success':f.estado==='ANULADA'?'danger':'warning'}">${f.estado}</span></td>
                <td><a class="btn btn-sm btn-outline-secondary" href="/api/portal/facturas/${f.id}/pdf" target="_blank"><i class="bi bi-file-pdf"></i></a></td>
            </tr>
        `).join('');
    } catch(e) { toast(e.message, 'error'); }
}

async function loadClientePerfil() {
    try {
        const p = await apiGet('/portal/perfil');
        document.getElementById('cliPerfNombre').value = (p.nombre || '') + ' ' + (p.apellido || '');
        document.getElementById('cliPerfEmail').value = p.email || '';
        document.getElementById('cliPerfTelefono').value = p.telefono || '';
        document.getElementById('cliPerfDireccion').value = p.direccion || '';
        document.getElementById('cliPerfMsg').innerHTML = '';
    } catch(e) { toast(e.message, 'error'); }
}

async function updateClientePerfil() {
    try {
        await apiPut('/portal/perfil', {
            telefono: document.getElementById('cliPerfTelefono').value,
            direccion: document.getElementById('cliPerfDireccion').value
        });
        document.getElementById('cliPerfMsg').innerHTML = '<div class="alert alert-success py-1">Perfil actualizado</div>';
    } catch(e) {
        document.getElementById('cliPerfMsg').innerHTML = '<div class="alert alert-danger py-1">' + e.message + '</div>';
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const token = getToken();
    if (!token) {
        document.getElementById('loginSection').classList.remove('d-none');
        document.getElementById('appSection').classList.add('d-none');
        return;
    }
    document.getElementById('loginSection').classList.add('d-none');
    document.getElementById('appSection').classList.remove('d-none');
    const user = getUser();
    document.getElementById('userName').textContent = user.nombre || 'Usuario';
    document.getElementById('userRol').textContent = user.rol || '';

    if (user.rol === 'CLIENTE') {
        document.getElementById('staffNav').style.display = 'none';
        document.getElementById('clientNav').style.display = '';
        showClientSection('inicio');
    } else {
        document.getElementById('staffNav').style.display = '';
        document.getElementById('clientNav').style.display = 'none';
        if (user.rol !== 'ADMIN') {
            const adminNav = document.getElementById('navAdmin');
            if (adminNav) adminNav.style.display = 'none';
            const auditNav = document.getElementById('navAuditoria');
            if (auditNav) auditNav.style.display = 'none';
        }
        showSection('dashboard');
    }
});
