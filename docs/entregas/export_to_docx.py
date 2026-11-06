"""
Script para compilar los documentos Markdown de las entregas de Semana 9 y 10
a formato Microsoft Word (.docx) con estilo corporativo y universitario limpio.
"""
import os
import re
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT, WD_ALIGN_VERTICAL
from docx.oxml import OxmlElement, parse_xml
from docx.oxml.ns import nsdecls, qn

def set_cell_background(cell, fill_hex):
    """Establece color de fondo de una celda."""
    tcPr = cell._tc.get_or_add_tcPr()
    shd = parse_xml(f'<w:shd {nsdecls("w")} w:fill="{fill_hex}"/>')
    tcPr.append(shd)

def set_cell_margins(cell, top=100, bottom=100, left=150, right=150):
    """Establece padding interno en una celda."""
    tcPr = cell._tc.get_or_add_tcPr()
    tcMar = parse_xml(f'<w:tcMar {nsdecls("w")}><w:top w:w="{top}" w:type="dxa"/><w:bottom w:w="{bottom}" w:type="dxa"/><w:left w:w="{left}" w:type="dxa"/><w:right w:w="{right}" w:type="dxa"/></w:tcMar>')
    tcPr.append(tcMar)

def add_styled_paragraph(doc, text, style='Normal', space_after=6, space_before=0, bold=False, italic=False, font_size=11, color_rgb=(40, 40, 40)):
    p = doc.add_paragraph()
    p.paragraph_format.space_after = Pt(space_after)
    p.paragraph_format.space_before = Pt(space_before)
    p.paragraph_format.line_spacing = 1.15
    
    # Process bold markers within text
    parts = re.split(r'(\*\*.*?\*\*)', text)
    for part in parts:
        if part.startswith('**') and part.endswith('**'):
            run = p.add_run(part[2:-2])
            run.bold = True
        elif part.startswith('*') and part.endswith('*') and len(part) > 2:
            run = p.add_run(part[1:-1])
            run.italic = True
        elif part.startswith('`') and part.endswith('`') and len(part) > 2:
            run = p.add_run(part[1:-1])
            run.font.name = 'Consolas'
            run.font.size = Pt(font_size - 1)
            run.font.color.rgb = RGBColor(160, 40, 40)
        else:
            # check inline backticks
            subparts = re.split(r'(`.*?`)', part)
            for sub in subparts:
                if sub.startswith('`') and sub.endswith('`'):
                    run = p.add_run(sub[1:-1])
                    run.font.name = 'Consolas'
                    run.font.size = Pt(font_size - 1)
                    run.font.color.rgb = RGBColor(160, 40, 40)
                else:
                    run = p.add_run(sub)
                    run.font.name = 'Calibri'
                    run.font.size = Pt(font_size)
                    run.font.color.rgb = RGBColor(*color_rgb)
                    if bold:
                        run.bold = True
                    if italic:
                        run.italic = True
    return p

def convert_md_to_docx(md_path, docx_path, main_title=""):
    with open(md_path, 'r', encoding='utf-8') as f:
        lines = f.readlines()
        
    doc = Document()
    
    # Page setup: Letter, 2.5cm margins (approx 1 inch)
    for section in doc.sections:
        section.top_margin = Inches(1.0)
        section.bottom_margin = Inches(1.0)
        section.left_margin = Inches(1.0)
        section.right_margin = Inches(1.0)
        section.page_width = Inches(8.5)
        section.page_height = Inches(11.0)
        
    i = 0
    in_code_block = False
    code_lines = []
    
    while i < len(lines):
        line = lines[i].rstrip('\r\n')
        
        # Check code block
        if line.startswith('```'):
            if in_code_block:
                in_code_block = False
                # emit code block table or box
                tbl = doc.add_table(rows=1, cols=1)
                tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
                cell = tbl.cell(0, 0)
                cell.width = Inches(6.5)
                set_cell_background(cell, "F4F5F7")
                set_cell_margins(cell, 120, 120, 180, 180)
                cp = cell.paragraphs[0]
                cp.paragraph_format.space_before = Pt(4)
                cp.paragraph_format.space_after = Pt(4)
                code_text = '\n'.join(code_lines)
                c_run = cp.add_run(code_text)
                c_run.font.name = 'Consolas'
                c_run.font.size = Pt(9)
                c_run.font.color.rgb = RGBColor(50, 50, 60)
                doc.add_paragraph().paragraph_format.space_after = Pt(4)
                code_lines = []
            else:
                in_code_block = True
                code_lines = []
            i += 1
            continue
            
        if in_code_block:
            code_lines.append(line)
            i += 1
            continue
            
        # Empty line
        if not line.strip():
            i += 1
            continue
            
        # Title (# )
        if line.startswith('# '):
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(14)
            p.paragraph_format.space_after = Pt(4)
            run = p.add_run(line[2:].strip())
            run.font.name = 'Calibri'
            run.font.size = Pt(20)
            run.bold = True
            run.font.color.rgb = RGBColor(26, 54, 93) # Deep Blue
            i += 1
            continue
            
        # Subtitle (## )
        if line.startswith('## '):
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(12)
            p.paragraph_format.space_after = Pt(4)
            run = p.add_run(line[3:].strip())
            run.font.name = 'Calibri'
            run.font.size = Pt(15)
            run.bold = True
            run.font.color.rgb = RGBColor(37, 99, 235) # Bright/Modern Blue
            i += 1
            continue
            
        # H3 (### )
        if line.startswith('### '):
            p = doc.add_paragraph()
            p.paragraph_format.space_before = Pt(10)
            p.paragraph_format.space_after = Pt(3)
            run = p.add_run(line[4:].strip())
            run.font.name = 'Calibri'
            run.font.size = Pt(12.5)
            run.bold = True
            run.font.color.rgb = RGBColor(45, 55, 72)
            i += 1
            continue

        # Blockquote (> )
        if line.startswith('> '):
            bq_text = line[2:].strip()
            tbl = doc.add_table(rows=1, cols=1)
            tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
            cell = tbl.cell(0, 0)
            cell.width = Inches(6.5)
            set_cell_background(cell, "EFF6FF")
            set_cell_margins(cell, 100, 100, 150, 150)
            p = cell.paragraphs[0]
            p.paragraph_format.space_after = Pt(2)
            run = p.add_run(bq_text.replace('*', ''))
            run.font.name = 'Calibri'
            run.font.size = Pt(10)
            run.italic = True
            run.font.color.rgb = RGBColor(30, 64, 175)
            i += 1
            continue
            
        # Horizontal rule (---)
        if line.strip() in ['---', '***', '___']:
            i += 1
            continue
            
        # Markdown table
        if line.startswith('|') and line.endswith('|'):
            table_lines = []
            while i < len(lines) and lines[i].strip().startswith('|') and lines[i].strip().endswith('|'):
                table_lines.append(lines[i].strip())
                i += 1
                
            # Parse table
            raw_rows = []
            for tl in table_lines:
                # ignore separator rows (|---|---|)
                if re.match(r'^\|(\s*:?-+:?\s*\|)+$', tl):
                    continue
                cells = [c.strip() for c in tl.strip('|').split('|')]
                raw_rows.append(cells)
                
            if raw_rows:
                num_cols = max(len(r) for r in raw_rows)
                tbl = doc.add_table(rows=len(raw_rows), cols=num_cols)
                tbl.alignment = WD_TABLE_ALIGNMENT.CENTER
                tbl.autofit = True
                
                for r_idx, r_data in enumerate(raw_rows):
                    row = tbl.rows[r_idx]
                    is_header = (r_idx == 0)
                    for c_idx in range(num_cols):
                        cell = row.cells[c_idx]
                        text = r_data[c_idx] if c_idx < len(r_data) else ""
                        text = text.replace('<br>', '\n')
                        
                        set_cell_margins(cell, 90, 90, 120, 120)
                        cell.vertical_alignment = WD_ALIGN_VERTICAL.CENTER
                        
                        p = cell.paragraphs[0]
                        p.paragraph_format.space_before = Pt(2)
                        p.paragraph_format.space_after = Pt(2)
                        p.paragraph_format.line_spacing = 1.05
                        
                        if is_header:
                            set_cell_background(cell, "1E3A8A") # Navy Blue
                            # parse bold or simple
                            clean_text = text.replace('**', '')
                            run = p.add_run(clean_text)
                            run.font.name = 'Calibri'
                            run.font.size = Pt(9.5)
                            run.bold = True
                            run.font.color.rgb = RGBColor(255, 255, 255)
                        else:
                            if r_idx % 2 == 1:
                                set_cell_background(cell, "FFFFFF")
                            else:
                                set_cell_background(cell, "F8FAFC")
                            
                            # Add text with formatting
                            sub_lines = text.split('\n')
                            for s_idx, sl in enumerate(sub_lines):
                                if s_idx > 0:
                                    p = cell.add_paragraph()
                                    p.paragraph_format.space_before = Pt(1)
                                    p.paragraph_format.space_after = Pt(1)
                                    p.paragraph_format.line_spacing = 1.05
                                
                                parts = re.split(r'(\*\*.*?\*\*)', sl)
                                for pt in parts:
                                    if pt.startswith('**') and pt.endswith('**'):
                                        run = p.add_run(pt[2:-2])
                                        run.font.name = 'Calibri'
                                        run.font.size = Pt(9)
                                        run.bold = True
                                        run.font.color.rgb = RGBColor(30, 41, 59)
                                    else:
                                        run = p.add_run(pt)
                                        run.font.name = 'Calibri'
                                        run.font.size = Pt(9)
                                        run.font.color.rgb = RGBColor(51, 65, 85)
                                        
                doc.add_paragraph().paragraph_format.space_after = Pt(4)
            continue
            
        # Bullet list (- or *)
        if line.strip().startswith(('-', '*', '•')):
            item_text = line.strip()[1:].strip()
            p = doc.add_paragraph(style='List Bullet')
            p.paragraph_format.space_before = Pt(1)
            p.paragraph_format.space_after = Pt(2)
            p.paragraph_format.line_spacing = 1.15
            
            parts = re.split(r'(\*\*.*?\*\*)', item_text)
            for part in parts:
                if part.startswith('**') and part.endswith('**'):
                    run = p.add_run(part[2:-2])
                    run.font.name = 'Calibri'
                    run.font.size = Pt(10.5)
                    run.bold = True
                    run.font.color.rgb = RGBColor(30, 41, 59)
                elif part.startswith('*') and part.endswith('*') and len(part) > 2:
                    run = p.add_run(part[1:-1])
                    run.font.name = 'Calibri'
                    run.font.size = Pt(10.5)
                    run.italic = True
                else:
                    subparts = re.split(r'(`.*?`)', part)
                    for sub in subparts:
                        if sub.startswith('`') and sub.endswith('`'):
                            run = p.add_run(sub[1:-1])
                            run.font.name = 'Consolas'
                            run.font.size = Pt(9.5)
                            run.font.color.rgb = RGBColor(180, 40, 40)
                        else:
                            run = p.add_run(sub)
                            run.font.name = 'Calibri'
                            run.font.size = Pt(10.5)
                            run.font.color.rgb = RGBColor(51, 65, 85)
            i += 1
            continue
            
        # Normal paragraph
        add_styled_paragraph(doc, line, font_size=10.5, space_after=4)
        i += 1
        
    doc.save(docx_path)
    print(f"Exportado exitosamente: {docx_path}")

if __name__ == '__main__':
    base_dir = os.path.dirname(os.path.abspath(__file__))
    files = [
        ("Taller_9_Ficha_Cambio_Alcance.md", "Taller_9_Ficha_Cambio_Alcance.docx", "Taller 9 - El cambio de alcance"),
        ("Semana_9_Comparativo_Clasico_Actual.md", "Semana_9_Comparativo_Clasico_Actual.docx", "Comparativo Clásico vs Actual"),
        ("Semana_10_Backlog_Arquitectonico.md", "Semana_10_Backlog_Arquitectonico.docx", "Backlog Arquitectónico Semanas 9 y 10")
    ]
    for md, docx, title in files:
        md_file = os.path.join(base_dir, md)
        docx_file = os.path.join(base_dir, docx)
        if os.path.exists(md_file):
            convert_md_to_docx(md_file, docx_file, title)
        else:
            print(f"No existe: {md_file}")
