/* global window, document */
// Muestra un mensaje de error si la app tarda más de 4 segundos en cargar.
// Está en un archivo aparte (y no inline en index.html) para que la CSP no necesite 'unsafe-inline' en script-src.
window.addEventListener('load', function () {
  setTimeout(function () {
    const errorElm = document.getElementById('jhipster-error');
    if (errorElm?.style) {
      errorElm.style.display = 'block';
    }
  }, 4000);
});
