package com.ejemplo.facturacion.services;

import com.ejemplo.facturacion.valueobjects.Articulo;
import com.ejemplo.facturacion.valueobjects.Factura;
import com.ejemplo.facturacion.valueobjects.Orden;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FacturaServiceTest {

    @InjectMocks
    private FacturaService facturaService;

    @Mock
    private FacturaService autoReferencia;

    private Orden ordenEjemplo;

    @BeforeEach
    void setUp() throws Exception {
        Articulo articulo = new Articulo();
        articulo.setPrecioUnitario(new BigDecimal("100.00"));
        articulo.setCantidad(2);

        ordenEjemplo = new Orden();
        ordenEjemplo.setId(1L);
        ordenEjemplo.setUsuario("developer");
        ordenEjemplo.setArticulos(List.of(articulo));

        Field field = FacturaService.class.getDeclaredField("autoReferencia");
        field.setAccessible(true);
        field.set(facturaService, autoReferencia);
    }

    // --- iniciarFacturaAsincrona ---

    @Test
    void iniciarFacturaAsincrona_debeRetornarIdFactura() throws InterruptedException {
        doNothing().when(autoReferencia).crearFacturaAsincrona(anyString(), any(Orden.class));

        String idFactura = facturaService.iniciarFacturaAsincrona(ordenEjemplo);

        assertNotNull(idFactura);
        assertFalse(idFactura.isBlank());
    }

    @Test
    void iniciarFacturaAsincrona_debeRegistrarIdEnMapaComoVacio() throws InterruptedException {
        doNothing().when(autoReferencia).crearFacturaAsincrona(anyString(), any(Orden.class));

        String idFactura = facturaService.iniciarFacturaAsincrona(ordenEjemplo);

        assertTrue(facturaService.getFacturas().containsKey(idFactura));
        assertFalse(facturaService.getFacturas().get(idFactura).isPresent());
    }

    // --- obtenerFacturaAsincrona ---

    @Test
    void obtenerFacturaAsincrona_debeRetornar404_cuandoIdNoExiste() {
        ResponseEntity<Factura> respuesta = facturaService.obtenerFacturaAsincrona("no-existe");

        assertEquals(HttpStatus.NOT_FOUND, respuesta.getStatusCode());
    }

    @Test
    void obtenerFacturaAsincrona_debeRetornar204_cuandoFacturaEnProceso() {
        Map<String, Optional<Factura>> facturas = new HashMap<>();
        facturas.put("en-proceso", Optional.empty());
        facturaService.setFacturas(facturas);

        ResponseEntity<Factura> respuesta = facturaService.obtenerFacturaAsincrona("en-proceso");

        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());
    }

    @Test
    void obtenerFacturaAsincrona_debeRetornar200_conFactura_cuandoYaEstaLista() {
        Factura factura = new Factura();
        factura.setId("lista");
        factura.setSubtotal(new BigDecimal("200.00"));
        factura.setIva(new BigDecimal("32.00"));
        factura.setTotal(new BigDecimal("232.00"));

        Map<String, Optional<Factura>> facturas = new HashMap<>();
        facturas.put("lista", Optional.of(factura));
        facturaService.setFacturas(facturas);

        ResponseEntity<Factura> respuesta = facturaService.obtenerFacturaAsincrona("lista");

        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("lista", respuesta.getBody().getId());
    }

    // --- crearFacturaAsincrona ---

    @Test
    void crearFacturaAsincrona_debeAlmacenarFacturaCompletaEnMapa() throws InterruptedException {
        Map<String, Optional<Factura>> facturas = new HashMap<>();
        facturas.put("id-test", Optional.empty());
        facturaService.setFacturas(facturas);

        facturaService.crearFacturaAsincrona("id-test", ordenEjemplo);

        assertTrue(facturas.get("id-test").isPresent());
        Factura resultado = facturas.get("id-test").get();
        assertEquals(new BigDecimal("200.00"), resultado.getSubtotal());
        assertEquals(new BigDecimal("32.00"), resultado.getIva());
        assertEquals(new BigDecimal("232.00"), resultado.getTotal());
    }
}