// Accessibility & Keyboard Navigation Utilities

export function enableAccessibilitySupport() {
  // ESC Key Modal Close Listener
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const openModals = document.querySelectorAll('.modal-overlay:not([style*="display: none"])');
      openModals.forEach(modal => {
        modal.style.display = 'none';
      });
    }
  });

  // Focus Trap Helper for active modals
  document.addEventListener('focusin', (e) => {
    const activeModal = document.querySelector('.modal-overlay:not([style*="display: none"])');
    if (activeModal && !activeModal.contains(e.target)) {
      const focusable = activeModal.querySelectorAll('button, input, select, textarea, [tabindex="0"]');
      if (focusable.length) focusable[0].focus();
    }
  });
}

export function toggleHighContrastMode() {
  document.body.classList.toggle('high-contrast-mode');
  const isHigh = document.body.classList.contains('high-contrast-mode');
  localStorage.setItem('pubexchange_high_contrast', isHigh ? 'true' : 'false');
  return isHigh;
}
