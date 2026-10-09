import { HojaVida, SoftwareLicencia, textoVidaUtil, etiquetaSoftware } from '../models/equipment.model';
import { etiqueta } from '../pipes/etiqueta.pipe';
import { LOGO_BASE64 } from './hoja-vida-logo';

/**
 * Genera y descarga la Hoja de Vida del equipo en PDF (formato de calidad PA-GT-FT-001).
 * pdfmake y sus fuentes se cargan bajo demanda (chunk separado) solo al llamar esta funcion.
 */
export async function descargarHojaVidaPdf(hv: HojaVida): Promise<void> {
  const mod: any = await import('pdfmake/build/pdfmake');
  const pdfMake: any = mod.default ?? mod;
  const fontsMod: any = await import('pdfmake/build/vfs_fonts');
  const fonts: any = fontsMod.default ?? fontsMod;
  const vfs = fonts?.pdfMake?.vfs ?? fonts?.vfs ?? fonts;
  if (typeof pdfMake.addVirtualFileSystem === 'function') pdfMake.addVirtualFileSystem(vfs);
  else pdfMake.vfs = vfs;

  const doc = construirDefinicion(hv);
  const base = limpiarNombre(hv.equipment?.inventoryNumber || hv.equipment?.name || 'EQUIPO');
  const fileName = `HV_${base}_${aaaammdd(new Date())}.pdf`;
  const res = pdfMake.createPdf(doc).download(fileName);
  if (res && typeof res.then === 'function') await res;
}

// ---------- utilidades de formato ----------

const GRIS = '#D9D9D9';
const GRIS_CLARO = '#F2F2F2';
const BORDE = '#7F7F7F';

function pad(n: number): string { return n < 10 ? '0' + n : String(n); }

function aaaammdd(d: Date): string { return `${d.getFullYear()}${pad(d.getMonth() + 1)}${pad(d.getDate())}`; }

function limpiarNombre(v: string): string {
  return String(v).normalize('NFD').replace(/[̀-ͯ]/g, '').replace(/[^A-Za-z0-9-]+/g, '_').replace(/^_+|_+$/g, '') || 'EQUIPO';
}

/** Fecha en dd/mm/aaaa. Acepta 'AAAA-MM-DD', ISO con hora, Date o arreglo [a, m, d]. */
function fecha(v: any): string {
  if (v === null || v === undefined || v === '') return '-';
  if (Array.isArray(v) && v.length >= 3) return `${pad(+v[2])}/${pad(+v[1])}/${v[0]}`;
  const s = String(v);
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) { const [a, m, d] = s.split('-'); return `${d}/${m}/${a}`; }
  const dt = new Date(s);
  if (!isNaN(dt.getTime())) return `${pad(dt.getDate())}/${pad(dt.getMonth() + 1)}/${dt.getFullYear()}`;
  return s;
}

const copFmt = new Intl.NumberFormat('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 0 });
function cop(v: any): string { return v === null || v === undefined || v === '' || isNaN(Number(v)) ? '-' : copFmt.format(Number(v)); }

function txt(v: any): string { return v === null || v === undefined || String(v).trim() === '' ? '-' : String(v); }

function ubicacion(b?: string, o?: string): string {
  const parts = [b, o].filter(x => !!x && String(x).trim());
  return parts.length ? parts.join(' - ') : '-';
}

// ---------- bloques del documento ----------

const layoutTabla = {
  hLineWidth: () => 0.5, vLineWidth: () => 0.5,
  hLineColor: () => BORDE, vLineColor: () => BORDE,
  paddingLeft: () => 4, paddingRight: () => 4, paddingTop: () => 3, paddingBottom: () => 3
};

/** Marcador de seccion; unirSecciones() lo convierte en la primera fila de la tabla siguiente. */
function seccion(n: number, titulo: string): any {
  return { _seccion: `${n}. ${titulo}` };
}

/**
 * Inserta el titulo de cada seccion como fila de encabezado de la tabla que le sigue,
 * para que el titulo nunca quede solo al final de una pagina (y se repita si la tabla continua).
 */
function unirSecciones(items: any[]): any[] {
  const out: any[] = [];
  for (let i = 0; i < items.length; i++) {
    const it = items[i];
    if (!it?._seccion) { out.push(it); continue; }
    const next = items[i + 1];
    const titulo = (cols: number) => [{ text: it._seccion, bold: true, fillColor: GRIS, fontSize: 9, colSpan: cols }, ...Array.from({ length: cols - 1 }, () => ({}))];
    if (next?.table?.widths && next.table.body) {
      const cols = next.table.widths.length;
      next.table.body = [titulo(cols), ...next.table.body];
      next.table.headerRows = (next.table.headerRows || 0) + 1;
      next.table.keepWithHeaderRows = 1;
      next.margin = [0, 10, 0, 0];
      out.push(next);
      i++;
    } else {
      out.push({ table: { widths: ['*'], body: [titulo(1)] }, layout: layoutTabla, margin: [0, 10, 0, 0] });
    }
  }
  return out;
}

/** Tabla de 4 columnas etiqueta / valor. Un par impar al final ocupa toda la fila. */
function tablaPares(pares: [string, any][]): any {
  const body: any[] = [];
  const lab = (t: string) => ({ text: t, bold: true, fillColor: GRIS_CLARO });
  for (let i = 0; i < pares.length; i += 2) {
    const a = pares[i], b = pares[i + 1];
    if (b) body.push([lab(a[0]), valor(a[1]), lab(b[0]), valor(b[1])]);
    else body.push([lab(a[0]), { ...valor(a[1]), colSpan: 3 }, {}, {}]);
  }
  return { table: { widths: ['20%', '30%', '20%', '30%'], body }, layout: layoutTabla, fontSize: 8 };
}

function valor(v: any): any {
  if (v && typeof v === 'object' && !Array.isArray(v)) return v;
  return { text: txt(v) };
}

/** Tabla con encabezado gris. Si no hay filas muestra el mensaje vacio. */
function tablaLista(headers: string[], widths: any[], rows: any[][], vacio: string): any {
  const body: any[] = [headers.map(h => ({ text: h, bold: true, fillColor: GRIS_CLARO }))];
  if (rows.length) rows.forEach(r => body.push(r.map(c => (c && typeof c === 'object') ? c : { text: txt(c) })));
  else body.push([{ text: vacio, italics: true, color: '#595959', colSpan: headers.length }, ...headers.slice(1).map(() => ({}))]);
  return { table: { headerRows: 1, widths, body }, layout: layoutTabla, fontSize: 8 };
}

function encabezado(generatedAt: string): any {
  const logo = LOGO_BASE64
    ? { image: LOGO_BASE64, fit: [80, 45], alignment: 'center', rowSpan: 3 }
    : { text: '', rowSpan: 3 };
  const titulo = { text: 'HOJA DE VIDA EQUIPOS TI', bold: true, fontSize: 13, alignment: 'center', rowSpan: 3, margin: [0, 14, 0, 0] };
  return {
    margin: [40, 24, 40, 0],
    table: {
      widths: [95, '*', 130],
      heights: [16, 16, 16],
      body: [
        [logo, titulo, { text: 'Codigo: PA-GT-FT-001', fontSize: 8 }],
        [{}, {}, { text: 'Version: 1', fontSize: 8 }],
        [{}, {}, { text: 'Fecha: ' + fecha(generatedAt || new Date().toISOString()), fontSize: 8 }]
      ]
    },
    layout: layoutTabla
  };
}

function construirDefinicion(hv: HojaVida): any {
  const e: any = hv.equipment || {};
  const hw: any = e.hardware || {};
  const ri: any = e.rentalInfo || {};
  const rentado = (e.ownershipType || '').toUpperCase() === 'RENTED';
  const docs = hv.documents || [];

  const content: any[] = [];

  // 1. Identificacion
  content.push(seccion(1, 'IDENTIFICACION DEL EQUIPO'));
  content.push(tablaPares([
    ['Fecha de registro', fecha(e.createdAt ?? (hv as any).registeredAt ?? e.registeredAt ?? e.registrationDate)],
    ['N. de activo fijo (N inventario)', e.inventoryNumber],
    ['Nombre', e.name], ['Marca', e.brand],
    ['Modelo', e.model], ['Numero de serie', e.serialNumber],
    ['Vida util estimada', textoVidaUtil(e.usefulLife)], ['Tipo de equipo', e.category],
    ['Estado', etiqueta(e.status, 'estado')], ['Criticidad', e.criticality]
  ]));

  // 2. Ubicacion y responsable
  content.push(seccion(2, 'UBICACION Y RESPONSABLE'));
  content.push(tablaPares([
    ['Sede', e.location?.building], ['Area / Servicio', e.location?.office],
    ['Centro de costo', e.costCenter], ['Usuario responsable', e.assignedTo],
    ['Cargo', e.responsiblePosition], ['Documento de identidad', e.responsibleDocument],
    ['Celular', e.responsiblePhone], ['Correo electronico', e.responsibleEmail]
  ]));

  // 3. Adquisicion
  content.push(seccion(3, 'INFORMACION DE ADQUISICION'));
  const docsTxt = {
    stack: [
      { text: 'Ver documentos adjuntos' + (docs.length ? ':' : ' (sin documentos cargados)') },
      ...(docs.length ? [{ ul: docs.map(d => `${txt(d.fileName)}${d.documentType ? ' (' + d.documentType + ')' : ''} - ${fecha(d.uploadedAt)}${d.uploadedBy ? ' - ' + d.uploadedBy : ''}`), margin: [0, 2, 0, 0] }] : [])
    ]
  };
  const adq: [string, any][] = [
    ['Forma de adquisicion', rentado ? 'Alquilado' : 'Propio'],
    ['Proveedor / Arrendador', rentado ? ri.rentalCompany : (e.supplier ?? e.provider)],
    ['Contrato', ri.contractNumber]
  ];
  if (rentado) {
    adq.push(['Valor mensual del alquiler', cop(ri.monthlyValue)]);
    adq.push(['Vigencia del alquiler', (ri.startDate || ri.endDate) ? `${fecha(ri.startDate)} a ${fecha(ri.endDate)}` : '-']);
  } else {
    adq.push(['Fecha de compra', fecha(e.purchaseDate)]);
    adq.push(['Valor de compra', cop(e.purchaseValue)]);
  }
  content.push(tablaPares(adq));
  content.push({ table: { widths: ['20%', '80%'], body: [[{ text: 'Factura y garantia', bold: true, fillColor: GRIS_CLARO }, docsTxt]] }, layout: layoutTabla, fontSize: 8 });

  // 4. Especificaciones
  const ram = [hw.ramSizeGb ? hw.ramSizeGb + ' GB' : '', hw.ramType || ''].filter(Boolean).join(' ');
  const disco = [hw.diskSizeGb ? hw.diskSizeGb + ' GB' : '', hw.diskType || ''].filter(Boolean).join(' ');
  const salud = [hw.diskHealthPercent != null ? 'Salud ' + hw.diskHealthPercent + '%' : '', hw.diskTemperatureCelsius != null ? 'Temp. ' + hw.diskTemperatureCelsius + ' C' : ''].filter(Boolean).join(' / ');
  const so = [e.operatingSystem ? etiqueta(e.operatingSystem, 'so') : '', e.osVersion || ''].filter(Boolean).join(' - ');
  content.push(seccion(4, 'ESPECIFICACIONES TECNICAS'));
  content.push(tablaPares([
    ['Procesador', hw.processor], ['RAM (GB) y tipo', ram],
    ['Almacenamiento (GB) y tipo', disco], ['Salud / temperatura del disco', salud],
    ['Sistema operativo y version', so]
  ]));

  // 5. Red
  content.push(seccion(5, 'CONFIGURACION DE RED'));
  content.push(tablaPares([
    ['Hostname', e.name], ['Direccion IP', e.ipAddress],
    ['Asignacion (DHCP/Fija)', e.ipAssignment === 'FIJA' ? 'Fija' : e.ipAssignment], ['MAC Ethernet', e.macAddress],
    ['MAC Wi-Fi', e.macAddress2]
  ]));

  // 6. Software (para diligenciar a mano)
  content.push(seccion(6, 'SOFTWARE Y LICENCIAMIENTO'));
  const blanco = () => [1, 2, 3, 4, 5].map(() => ({ text: ' ', margin: [0, 4, 0, 4] }));
  let swList: SoftwareLicencia[] = Array.isArray(hv.software) ? hv.software : [];
  if (!Array.isArray(hv.software) && (e.osEdition || e.operatingSystem || e.osVersion || e.osLicenseType)) {
    swList = [{
      software: e.osEdition || (e.operatingSystem ? etiqueta(e.operatingSystem, 'so') : ''),
      version: e.osVersion || '', licenseType: e.osLicenseType || '', licenseKey: '', expiration: ''
    }];
  }
  const swRows: any[] = swList.map(sw => [
    txt(etiquetaSoftware(sw.software)), txt(sw.version), txt(etiquetaSoftware(sw.licenseType)), txt(sw.licenseKey), fecha(sw.expiration)
  ].map(t => ({ text: t, margin: [0, 4, 0, 4] })));
  while (swRows.length < 3) swRows.push(blanco());
  content.push({
    table: {
      headerRows: 1, widths: ['26%', '14%', '20%', '24%', '16%'],
      body: [
        ['Software', 'Version', 'Tipo de licencia', 'N licencia / Clave', 'Vencimiento'].map(h => ({ text: h, bold: true, fillColor: GRIS_CLARO })),
        ...swRows
      ]
    },
    layout: layoutTabla, fontSize: 8
  });

  // 7. Perifericos y monitores
  content.push(seccion(7, 'PERIFERICOS Y ACCESORIOS ASOCIADOS'));
  content.push(tablaLista(['Tipo', 'Marca'], ['50%', '50%'], (hv.peripherals || []).map(p => [p.type, p.brand]), 'Sin perifericos registrados'));
  content.push({ text: 'Monitores asociados', bold: true, fontSize: 8, margin: [0, 6, 0, 2] });
  content.push(tablaLista(['Nombre', 'Marca / Modelo', 'Serial', 'N inventario', 'Propio / Alquilado'], ['24%', '22%', '18%', '14%', '22%'],
    (hv.monitors || []).map(m => [
      m.name, [m.brand, m.model].filter(Boolean).join(' / '), m.serialNumber, m.inventoryNumber,
      (m.ownershipType || '').toUpperCase() === 'RENTED'
        ? 'Alquilado' + (m.rentalCompany ? ' - ' + m.rentalCompany : '') + (m.monthlyValue != null ? ' - ' + cop(m.monthlyValue) + '/mes' : '')
        : 'Propio'
    ]), 'Sin monitores asociados'));

  // 8. Mantenimientos
  content.push(seccion(8, 'HISTORIAL DE MANTENIMIENTO'));
  content.push(tablaLista(['Fecha', 'Tipo', 'Descripcion de la actividad / Repuestos', 'Realizado por'], ['13%', '15%', '52%', '20%'],
    (hv.maintenances || []).map(m => {
      const parts = (m.parts || []).map(p => `${txt(p.partName)}${p.partSerialNumber ? ' (S/N ' + p.partSerialNumber + ')' : ''}${p.reason ? ' - ' + p.reason : ''}`);
      const stack: any[] = [];
      if (m.reason) stack.push({ text: [{ text: 'Motivo: ', bold: true }, m.reason] });
      stack.push({ text: txt(m.description) });
      if (parts.length) stack.push({ text: [{ text: 'Repuestos: ', bold: true }, parts.join('; ')] });
      const por = [m.technicianName, m.signedBy && m.signedBy !== m.technicianName ? 'Firma: ' + m.signedBy : ''].filter(Boolean).join('\n');
      return [fecha(m.performedDate), etiqueta(m.maintenanceType || '', 'mantenimiento') || '-', { stack }, por || '-'];
    }), 'Sin mantenimientos registrados'));
  content.push({ text: 'Tipo tal como se registro en THOTH.', italics: true, fontSize: 7, color: '#595959', margin: [0, 2, 0, 0] });

  // 9. Traslados
  content.push(seccion(9, 'TRASLADOS Y ASIGNACIONES'));
  content.push(tablaLista(['Fecha', 'Ubicacion anterior', 'Ubicacion nueva', 'Motivo', 'Realizado por'], ['13%', '22%', '22%', '28%', '15%'],
    (hv.transfers || []).map(t => [fecha(t.date), ubicacion(t.fromBuilding, t.fromOffice), ubicacion(t.toBuilding, t.toOffice), t.reason, t.performedBy]),
    'Sin traslados registrados'));

  // 10. Baja
  content.push(seccion(10, 'BAJA DEL EQUIPO'));
  content.push(hv.baja
    ? tablaPares([['Fecha de baja', fecha(hv.baja.date)], ['Motivo', hv.baja.reason]])
    : { table: { widths: ['*'], body: [[{ text: 'No aplica' }]] }, layout: layoutTabla, fontSize: 8 });

  // 11. Observaciones y firmas
  content.push(seccion(11, 'OBSERVACIONES'));
  content.push({ table: { widths: ['*'], heights: [70], body: [[{ text: '' }]] }, layout: layoutTabla });
  const firma = (titulo: string) => ({
    stack: [
      { text: titulo, bold: true, alignment: 'center', margin: [0, 0, 0, 10] },
      { text: 'Nombre: ________________________________', margin: [0, 6, 0, 0] },
      { text: 'Firma:   ________________________________', margin: [0, 14, 0, 0] },
      { text: 'Fecha:   ________________________________', margin: [0, 14, 0, 4] }
    ]
  });
  content.push({
    unbreakable: true, margin: [0, 14, 0, 0], fontSize: 8,
    table: { widths: ['50%', '50%'], body: [[firma('Elaborado por (TI)'), firma('Responsable del equipo')]] },
    layout: layoutTabla
  });

  return {
    pageSize: 'LETTER',
    pageMargins: [40, 100, 40, 45],
    info: { title: 'Hoja de vida ' + (e.name || ''), author: 'THOTH C.O.R.E.' },
    defaultStyle: { fontSize: 9 },
    header: () => encabezado(hv.generatedAt),
    footer: (currentPage: number, pageCount: number) => ({
      text: `Generado por THOTH C.O.R.E. - pagina ${currentPage} de ${pageCount}`,
      alignment: 'center', fontSize: 7, color: '#595959', margin: [40, 15, 40, 0]
    }),
    content: unirSecciones(content)
  };
}
