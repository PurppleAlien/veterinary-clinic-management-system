(function () {
  'use strict';

  // ── Theme system ──
  var THEMES = [
    { id: 'light',      label: 'Claro',     swatchClass: 'theme-swatch-light' },
    { id: 'dark',       label: 'Oscuro',    swatchClass: 'theme-swatch-dark' },
    { id: 'terracota',  label: 'Terracota', swatchClass: 'theme-swatch-terracota' },
    { id: 'blue-pro',   label: 'Blue Pro',  swatchClass: 'theme-swatch-bluepro' },
    { id: 'pink',       label: 'Pink',      swatchClass: 'theme-swatch-pink' },
    { id: 'purple',     label: 'Púrpura',   swatchClass: 'theme-swatch-purple' },
    { id: 'olive',      label: 'Oliva',     swatchClass: 'theme-swatch-olive' },
    { id: 'burn',       label: 'Atardecer', swatchClass: 'theme-swatch-burntorange' }
  ];

  var currentTheme = localStorage.getItem('vet-theme') || 'light';

  function applyTheme(themeId) {
    document.documentElement.setAttribute('data-theme', themeId);
    localStorage.setItem('vet-theme', themeId);
    currentTheme = themeId;
    // sync FullCalendar if present
    if (window.calendar && typeof calendar.render === 'function') {
      try { calendar.render(); } catch (_) {}
    }
    updateActiveOption(themeId);
  }

  function updateActiveOption(themeId) {
    var opts = document.querySelectorAll('.theme-option');
    opts.forEach(function (opt) {
      var val = opt.getAttribute('data-theme-value');
      if (val === themeId) {
        opt.classList.add('active');
      } else {
        opt.classList.remove('active');
      }
    });
    // toggle icon on button
    var btn = document.getElementById('themeSwitcherBtn');
    if (btn && (themeId === 'dark' || themeId === 'blue-pro' || themeId === 'burn')) {
      btn.innerHTML = '<span class="ts-icon-open">\u{1F319}</span><span class="ts-icon-close">\u2716</span>';
    } else {
      btn.innerHTML = '<span class="ts-icon-open">\u{1F308}</span><span class="ts-icon-close">\u2716</span>';
    }
  }

  function injectThemePanel() {
    if (document.getElementById('themeSwitcherBtn')) return;

    var btn = document.createElement('button');
    btn.id = 'themeSwitcherBtn';
    btn.setAttribute('aria-label', 'Cambiar tema');
    document.body.appendChild(btn);

    var panel = document.createElement('div');
    panel.id = 'themePanel';
    panel.className = 'theme-panel';
    document.body.appendChild(panel);

    var backdrop = document.createElement('div');
    backdrop.className = 'theme-backdrop';
    document.body.appendChild(backdrop);

    var listHtml = '';
    THEMES.forEach(function (t) {
      listHtml +=
        '<button class="theme-option" data-theme-value="' + t.id + '">' +
          '<span class="theme-swatch ' + t.swatchClass + '"></span>' +
          '<span>' + t.label + '</span>' +
          '<span class="theme-check">\u2714</span>' +
        '</button>';
    });
    panel.innerHTML = listHtml;

    // --- events ---
    btn.addEventListener('click', function (e) {
      e.stopPropagation();
      panel.classList.toggle('open');
      btn.classList.toggle('open');
      backdrop.style.display = panel.classList.contains('open') ? 'block' : 'none';
    });

    backdrop.addEventListener('click', function () {
      panel.classList.remove('open');
      btn.classList.remove('open');
      backdrop.style.display = 'none';
    });

    panel.addEventListener('click', function (e) {
      var opt = e.target.closest('.theme-option');
      if (!opt) return;
      var val = opt.getAttribute('data-theme-value');
      if (val) applyTheme(val);
      panel.classList.remove('open');
      btn.classList.remove('open');
      backdrop.style.display = 'none';
    });

    // init
    applyTheme(currentTheme);
  }

  // ── Init on DOM ready ──
  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', injectThemePanel);
  } else {
    injectThemePanel();
  }

})();
