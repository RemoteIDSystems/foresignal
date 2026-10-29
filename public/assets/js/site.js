(() => {
  'use strict';
  const navToggle = document.querySelector('.nav-toggle');
  const nav = document.querySelector('.nav-links');
  const toast = document.querySelector('.toast');
  let toastTimer;
  const notify = (message) => {
    if (!toast) return;
    toast.textContent = message;
    toast.classList.add('visible');
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => toast.classList.remove('visible'), 2300);
  };
  if (navToggle && nav) {
    navToggle.addEventListener('click', () => {
      const opened = nav.classList.toggle('is-open');
      navToggle.setAttribute('aria-expanded', String(opened));
      navToggle.setAttribute('aria-label', opened ? 'Close menu' : 'Open menu');
    });
    nav.querySelectorAll('a').forEach(link => link.addEventListener('click', () => {
      nav.classList.remove('is-open');
      navToggle.setAttribute('aria-expanded', 'false');
    }));
    document.addEventListener('keydown', e => {
      if (e.key === 'Escape' && nav.classList.contains('is-open')) {
        nav.classList.remove('is-open'); navToggle.setAttribute('aria-expanded','false');navToggle.focus();
      }
    });
  }
  document.querySelectorAll('[data-year]').forEach(el => el.textContent = new Date().getFullYear());
  document.querySelectorAll('[data-repo-link]').forEach(link => {
    const key = link.getAttribute('data-repo-link');
    const href = window.FORESIGNAL_SITE?.repositories?.[key];
    if (href && /^https:\/\//i.test(href)) {
      link.href = href;
      link.target = '_blank';
      link.rel = 'noopener noreferrer';
      link.hidden = false;
    }
  });
  async function copyText(value) {
    try {
      await navigator.clipboard.writeText(value);
      notify('Copied to clipboard');
    } catch (error) {
      // Clipboard API can be unavailable on file:// or insecure origins. Use a fallback.
      try {
        const field = document.createElement('textarea');
        field.value = value;
        field.style.cssText='position:fixed;left:-9999px;top:-9999px;';
        document.body.appendChild(field);
        field.select();
        const success = document.execCommand('copy');
        field.remove();
        notify(success ? 'Copied to clipboard' : 'Select the command to copy it');
      } catch (_) { notify('Clipboard unavailable in this browser'); }
    }
  }
  document.querySelectorAll('[data-copy]').forEach(button => {
    button.addEventListener('click', () => copyText(button.getAttribute('data-copy')));
  });
  const filters = [...document.querySelectorAll('[data-filter]')];
  if (filters.length) {
    const galleryCards = [...document.querySelectorAll('.gallery-card')];
    filters.forEach(button => button.addEventListener('click', () => {
      const selected = button.getAttribute('data-filter');
      filters.forEach(item => {
        const active = item === button;
        item.classList.toggle('active', active);
        item.setAttribute('aria-pressed', String(active));
      });
      galleryCards.forEach(card => {
        card.hidden = selected !== 'all' && card.getAttribute('data-category') !== selected;
      });
    }));
  }
  const dialog = document.querySelector('.lightbox');
  const lightboxImage = dialog?.querySelector('.lightbox-image');
  const lightboxTitle = dialog?.querySelector('#lightbox-title');
  const lightboxCaption = dialog?.querySelector('.lightbox-caption');
  const closeBtn = dialog?.querySelector('.lightbox-close');
  let previousFocus = null;
  const openLightbox = (element) => {
    if (!dialog) return;
    const image = element.matches('img') ? element : element.querySelector('img');
    if (!image) return;
    previousFocus = document.activeElement;
    lightboxImage.src = image.src;
    lightboxImage.alt = image.alt;
    lightboxTitle.textContent = element.getAttribute('data-lightbox-title') || 'Foresignal design';
    lightboxCaption.textContent = element.getAttribute('data-lightbox-caption') || '';
    dialog.classList.add('is-open');
    dialog.setAttribute('aria-hidden', 'false');
    document.body.style.overflow='hidden';
    closeBtn.focus();
  };
  const closeLightbox = () => {
    if (!dialog?.classList.contains('is-open')) return;
    dialog.classList.remove('is-open');
    dialog.setAttribute('aria-hidden', 'true');
    document.body.style.overflow='';
    lightboxImage.removeAttribute('src');
    previousFocus?.focus?.();
  };
  document.querySelectorAll('[data-lightbox-title]').forEach(el => {
    el.addEventListener('click', () => openLightbox(el));
    if (el.tagName.toLowerCase() !== 'button') {
      el.addEventListener('keydown', e => {
        if (e.key === 'Enter' || e.key === ' ') {e.preventDefault();openLightbox(el);}
      });
    }
  });
  closeBtn?.addEventListener('click', closeLightbox);
  dialog?.addEventListener('click', e => { if (e.target === dialog) closeLightbox(); });
  document.addEventListener('keydown', e => {
    if (!dialog?.classList.contains('is-open')) return;
    if (e.key === 'Escape') {e.preventDefault();closeLightbox();}
    if (e.key === 'Tab') {e.preventDefault();closeBtn.focus();}
  });
})();
