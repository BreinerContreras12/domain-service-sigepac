"""
Generador del PDF oficial de Sustentación y Contexto para Semanas 9 y 10
Utiliza fpdf2 con fuente Arial de Windows (soporte UTF-8 completo: tildes, viñetas, guiones).
"""
import os
from fpdf import FPDF
from fpdf.enums import XPos, YPos

class SustentacionPDF(FPDF):
    def __init__(self, **kwargs):
        super().__init__(**kwargs)
        # Cargar fuentes Unicode de Windows
        self.add_font("ArialUni", style="", fname=r"C:\Windows\Fonts\arial.ttf")
        self.add_font("ArialUni", style="B", fname=r"C:\Windows\Fonts\arialbd.ttf")
        self.add_font("ArialUni", style="I", fname=r"C:\Windows\Fonts\ariali.ttf")
        self.add_font("ArialUni", style="BI", fname=r"C:\Windows\Fonts\arialbi.ttf")

    def header(self):
        if self.page_no() > 1:
            self.set_font("ArialUni", "I", 8)
            self.set_text_color(120, 120, 130)
            self.cell(0, 8, "SIGEPAC · Guía de Sustentación Semanas 9 y 10 · Ingeniería del Software III (Grupo 4)", border=0, align="L")
            self.ln(10)

    def footer(self):
        self.set_y(-12)
        self.set_font("ArialUni", "I", 8)
        self.set_text_color(130, 130, 140)
        self.cell(0, 8, f"Página {self.page_no()}/{{nb}}", border=0, align="C")

    def chapter_title(self, text, part=False):
        if part:
            self.ln(3)
            self.set_fill_color(26, 54, 93) # Deep Navy
            self.set_text_color(255, 255, 255)
            self.set_font("ArialUni", "B", 10.5)
            self.cell(0, 8, f"  {text.upper()}", fill=True, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
            self.set_text_color(40, 40, 40)
            self.ln(3)
        else:
            self.set_text_color(37, 99, 235) # Bright Blue
            self.set_font("ArialUni", "B", 10.5)
            self.cell(0, 6, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
            self.set_text_color(40, 40, 40)
            self.ln(1)

    def sub_heading(self, text):
        self.set_text_color(30, 41, 59)
        self.set_font("ArialUni", "B", 9.5)
        self.cell(0, 5, text, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        self.set_text_color(51, 65, 85)
        self.ln(1)

    def body_text(self, text):
        self.set_font("ArialUni", "", 9)
        self.set_text_color(51, 65, 85)
        self.multi_cell(0, 4.5, text)
        self.ln(1.5)

    def bullet_item(self, title, desc):
        self.set_font("ArialUni", "B", 9)
        self.set_text_color(30, 41, 59)
        w_title = self.get_string_width(f"• {title} ") + 1
        self.cell(w_title, 4.5, f"• {title} ")
        self.set_font("ArialUni", "", 9)
        self.set_text_color(51, 65, 85)
        self.multi_cell(0, 4.5, desc)
        self.ln(1)

    def callout_box(self, title, text, bg_color=(240, 244, 248), border_color=(37, 99, 235)):
        self.ln(1)
        self.set_fill_color(*bg_color)
        self.set_draw_color(*border_color)
        self.set_line_width(0.3)
        
        x = self.get_x()
        y = self.get_y()
        w = self.epw
        
        # Estimate text height
        self.set_font("ArialUni", "", 8.5)
        lines = len(text.split('\n')) + 1
        h = max(18, lines * 5 + 6)
        
        self.rect(x, y, w, h, style="DF")
        self.set_fill_color(*border_color)
        self.rect(x, y, 3, h, style="F") # Accent bar
        
        self.set_xy(x + 6, y + 2)
        if title:
            self.set_font("ArialUni", "B", 9)
            self.set_text_color(*border_color)
            self.cell(0, 4.5, title, new_x=XPos.LMARGIN, new_y=YPos.NEXT)
            self.set_x(x + 6)
            
        self.set_font("ArialUni", "", 8.5)
        self.set_text_color(40, 40, 50)
        self.multi_cell(w - 10, 4.2, text)
        self.set_xy(x, y + h + 3)

def build_pdf():
    pdf = SustentacionPDF(orientation="P", unit="mm", format="Letter")
    pdf.set_margins(16, 14, 16)
    pdf.set_auto_page_break(auto=True, margin=15)
    pdf.alias_nb_pages()
    
    pdf.add_page()
    
    # Header Banner
    pdf.set_fill_color(26, 54, 93)
    pdf.rect(16, 12, pdf.epw, 22, style="F")
    
    pdf.set_xy(19, 14)
    pdf.set_font("ArialUni", "B", 13)
    pdf.set_text_color(255, 255, 255)
    pdf.cell(0, 6, "GUÍA COMPLETA DE SUSTENTACIÓN Y CONTEXTO", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    
    pdf.set_xy(19, 21)
    pdf.set_font("ArialUni", "", 9)
    pdf.set_text_color(220, 230, 245)
    pdf.cell(0, 5, "Semanas 9 y 10 · Ingeniería del Software III · Proyecto: SIGEPAC (Grupo 4)", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    pdf.set_xy(19, 27)
    pdf.set_font("ArialUni", "I", 7.5)
    pdf.cell(0, 4, "Universidad de Pamplona — Documento de apoyo rápido para defensa y examen", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
    
    pdf.set_y(38)
    
    # =========================================================================
    # PARTE 1
    # =========================================================================
    pdf.chapter_title("PARTE 1: LO QUE TIENES QUE SUSTENTARLE AL PROFESOR", part=True)
    pdf.body_text("Resumen coherente, claro y no técnico con los resultados de cada entrega:")
    
    # Semana 9
    pdf.chapter_title("ENTREGA SEMANA 9: Taller 9 y Comparativo")
    pdf.sub_heading("¿De qué se trataba?")
    pdf.body_text("Demostrar con el código real de nuestro proyecto qué le pasa al sistema cuando el cliente pide cambios, y cómo cambia el costo si trabajamos en Cascada, en RUP o con nuestra Arquitectura Hexagonal.")
    
    pdf.sub_heading("¿Qué hicimos?")
    pdf.body_text("Simulamos 3 cambios reales sobre nuestro servicio de la plaza cívica (domain-service-sigepac):")
    pdf.bullet_item("Cambio 1 (Regla de negocio):", "Dar un 10% de descuento si el inquilino paga temprano (pronto pago).")
    pdf.bullet_item("Cambio 2 (Infraestructura):", "Cambiar la base de datos de pruebas (H2) a una base de datos real (PostgreSQL/MySQL).")
    pdf.bullet_item("Cambio 3 (Alcance):", "Clasificar los locales por categoría (comida, artesanías, etc.) y registrar qué administrador hizo el trámite.")
    
    pdf.sub_heading("¿Cuáles fueron los resultados que obtuvimos? (Mostrar al profesor):")
    pdf.bullet_item("En el Cambio 1 (Descuento):", "Se modificaron 9 archivos, pero todos quedaron concentrados en el dominio y la aplicación. La base de datos y la web no se enteraron. Dolió un poco en las pruebas unitarias porque tocó recalcular valores esperados a mano.")
    pdf.bullet_item("En el Cambio 2 (Base de datos):", "CERO archivos de código Java cambiaron. Solo tocamos 2 archivos de configuración (pom.xml y application-prod.yml). Demostró que la Arquitectura Hexagonal funcionó perfecto: al negocio no le importa qué motor de base de datos existe.")
    pdf.bullet_item("En el Cambio 3 (Categorías):", "Se tocaron 10 archivos de arriba a abajo, pero de forma ordenada gracias a que cada caso de uso está separado e independiente.")
    
    pdf.callout_box("[!] La Conclusión que le dices al profesor en Semana 9:",
                    '"Profesor, la conclusión del taller es que la metodología decide cuándo nos enteramos del cambio y cuánto papeleo cuesta (en Cascada es una pesadilla reabrir fases; en RUP se mete en la siguiente iteración). Pero la arquitectura es la que decide cuántos archivos tocamos realmente en el código".')

    # Semana 10
    pdf.chapter_title("ENTREGA SEMANA 10: Deuda Técnica, Spikes y Sprints")
    pdf.sub_heading("¿De qué se trataba?")
    pdf.body_text("Dejar de ver el proyecto como un código perfecto y ser autocríticos: identificar qué cosas dejamos pendientes o hechas 'de afán' para poder entregar (deuda técnica), investigar dudas antes de programar (spikes) y organizar la solución en 2 iteraciones en Trello.")
    
    pdf.sub_heading("¿Cuáles fueron los resultados que obtuvimos?:")
    pdf.bullet_item("1. Seis Deudas Técnicas reales:", "La principal: la BD se crea sola con Hibernate (ddl-auto: update), lo cual es riesgoso en producción si se borran datos. Otras: falta de paginación en historial extenso y falta de Swagger para el frontend.")
    pdf.bullet_item("2. Dos Spikes formulados:", "Spike 1 (2 horas): Averiguar si sirve más Flyway o Liquibase para migraciones. Spike 2 (3 horas): Investigar cómo bloquear registros para evitar que dos cajeros cobren la misma deuda al mismo tiempo.")
    pdf.bullet_item("3. Plan de 2 Sprints en Trello:", "Sprint 1 (Base técnica firme): Ítems urgentes (A-01 a A-03) en 'Iteración actual' (pruebas en verde y Flyway). Sprint 2 (Calidad): Tareas futuras (A-04 a A-06) en 'Backlog' (paginación, Swagger y seguridad).")
    pdf.bullet_item("4. Definición de Hecho (DoD):", "Seis reglas que debe cumplir toda tarea para darse por terminada (pruebas pasando, revisión de pares y README actualizado).")

    pdf.callout_box("[>] Resumen en una sola frase por si te pide ser breve:",
                    "• Semana 9: Demostramos que la arquitectura hexagonal aisló el negocio de la BD (0 clases Java modificadas).\n• Semana 10: Registramos 6 deudas reales, 2 spikes con timebox y organizamos 2 sprints en Trello.",
                    bg_color=(254, 243, 199), border_color=(217, 119, 6))

    # =========================================================================
    # PARTE 2
    # =========================================================================
    pdf.add_page()
    pdf.chapter_title("PARTE 2: QUÉ HICIMOS CON LAS GUÍAS, QUÉ SE ALTERÓ Y CONTEXTO", part=True)
    
    pdf.sub_heading("1. Las guías y materiales que se usaron")
    pdf.bullet_item("Guía de estudio oficial (Sem. 9 y 10):", "Teoría de Cascada (Royce), RUP (Kruchten), Scrum, Cuadrante de Fowler y rúbricas.")
    pdf.bullet_item("Plantilla del Taller 9 ('El cambio de alcance'):", "Hoja oficial con las 3 fichas para los cambios.")
    pdf.bullet_item("Ejemplo de la Ferretería:", "Matriz modelo comparativa Hexagonal vs Capas y Cascada vs RUP.")
    
    pdf.sub_heading("2. ¿Qué fue lo que se hizo exactamente?")
    pdf.bullet_item("Taller_9_Ficha_Cambio_Alcance.docx:", "Diligenciado con las clases reales de SIGEPAC para los 3 cambios (Pronto pago, BD PostgreSQL, Categoría de locales).")
    pdf.bullet_item("Semana_9_Comparativo_Clasico_Actual.docx:", "Informe formal de 4 páginas con mapeo a las 4 fases de RUP, comparación de cambios y autocrítica del Grupo 4.")
    pdf.bullet_item("Semana_10_Backlog_Arquitectonico.docx:", "6 deudas técnicas en cuadrante de Fowler, 2 Spikes con timebox, Backlog (escalas 1-10 y 10-100), plan de 2 Sprints y DoD.")
    pdf.bullet_item("Sincronización en Trello (SIGEPAC · XP):", "Tarjetas A-01 a A-03 y SP-01/SP-02 en 'Iteración actual' (Sprint 1), y A-04 a A-06 en 'Historias (backlog)' (Sprint 2).")

    pdf.sub_heading("3. ¿Qué se alteró en tu proyecto?")
    pdf.bullet_item("Cero código Java modificado:", "No se alteró ninguna clase en src/main/ ni src/test/. Las 70 pruebas unitarias siguen en verde.")
    pdf.bullet_item("Cero bases de datos tocadas:", "No se modificó ni borró ningún dato existente.")
    pdf.bullet_item("Solo archivos nuevos de documentación:", "Todo se generó de forma limpia en la carpeta docs/entregas/.")

    pdf.sub_heading("4. ¿Qué debes presentarle al profesor exactamente?")
    pdf.body_text("• Para Semana 9: El Word Taller_9_Ficha_Cambio_Alcance.docx y el informe formal Semana_9_Comparativo_Clasico_Actual.docx.\n• Para Semana 10: El Word Semana_10_Backlog_Arquitectonico.docx y 2 capturas de Trello (vista global del tablero y detalle del Spike SP-01 con su checklist).")

    pdf.sub_heading("5. 'Machete': Preguntas típicas de sustentación")
    
    preguntas = [
        ("¿Por qué el cambio de BD (H2 a PostgreSQL) no costó nada en su código?",
         "Porque gracias a la Arquitectura Hexagonal y la Inversión de Dependencias (DIP), el dominio no sabe qué BD existe. Solo se tocaron 2 archivos de configuración (application-prod.yml y pom.xml); ninguna clase de negocio cambió."),
        ("¿Qué es una tarjeta A-00 y una SP-00 en su Trello?",
         "Las HU son historias de usuario. Las 'A' son Ítems Arquitectónicos (tareas técnicas de soporte/migración). Las 'SP' son Spikes, investigaciones técnicas acotadas en tiempo (Timebox) para despejar dudas antes de codificar."),
        ("¿Qué es el Cuadrante de Fowler y denme un ejemplo de su proyecto?",
         "Clasifica la deuda técnica en Deliberada/Inadvertida y Prudente/Imprudente. Ejemplo: ddl-auto: update fue Deliberada-Prudente (permitió modelar rápido el prototipo sabiendo que luego se pagaría con Flyway en el Sprint 1)."),
        ("¿Cuál es la conclusión central entre Metodología y Arquitectura?",
         "La arquitectura decide cuántos archivos tocamos; la metodología decide cuándo descubrimos el cambio y cuánto retrabajo burocrático cuesta.")
    ]
    
    for p, r in preguntas:
        pdf.set_font("ArialUni", "B", 8.5)
        pdf.set_text_color(26, 54, 93)
        pdf.cell(0, 4.5, f"Q: {p}", new_x=XPos.LMARGIN, new_y=YPos.NEXT)
        pdf.set_font("ArialUni", "", 8.5)
        pdf.set_text_color(51, 65, 85)
        pdf.multi_cell(0, 4.2, f"R: {r}")
        pdf.ln(1.5)

    output_dir = os.path.dirname(os.path.abspath(__file__))
    output_pdf = os.path.join(output_dir, "RESUMEN_SUSTENTACION.pdf")
    pdf.output(output_pdf)
    print(f"PDF generado exitosamente en: {output_pdf}")

if __name__ == '__main__':
    build_pdf()
