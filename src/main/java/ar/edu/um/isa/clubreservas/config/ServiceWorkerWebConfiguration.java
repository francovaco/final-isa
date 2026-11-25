package ar.edu.um.isa.clubreservas.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * El service worker de la PWA (sw.js, generado por Workbox) se sirve sin caché HTTP.
 *
 * <p>El resto de los .js lleva caché larga (tienen el hash en el nombre), pero sw.js tiene siempre
 * el mismo nombre: si quedara cacheado, los navegadores tardarían en enterarse de una versión nueva.
 * Este patrón es más específico que "/*.js", así que Spring lo usa para sw.js.
 */
@Configuration
public class ServiceWorkerWebConfiguration implements WebMvcConfigurer {

    static final String SERVICE_WORKER = "/sw.js";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(SERVICE_WORKER).addResourceLocations("classpath:/static/").setCacheControl(CacheControl.noCache());
    }
}
