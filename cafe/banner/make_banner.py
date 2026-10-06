"""Genera el banner 60x80 cm de la app Cafe Aroma.

Salidas (junto a este script):
  - cafe_aroma_banner.svg  -> vector editable (Canva / Illustrator)
  - cafe_aroma_banner.png  -> 300 DPI (7087 x 9449 px) para imprimir
  - preview.jpg            -> vista previa rapida
"""
import math
import os

import qrcode
from PIL import Image, ImageDraw, ImageFont

W_MM, H_MM = 600.0, 800.0
DPI = 300
S = DPI / 25.4
OUT = os.path.dirname(os.path.abspath(__file__))
QR_URL = "https://play.google.com/store/apps/details?id=com.example.cafe"

FONT_R = r"C:\Windows\Fonts\arial.ttf"
FONT_B = r"C:\Windows\Fonts\arialbd.ttf"

BG = "#F8F5F0"
COFFEE = "#3D2C24"
COFFEE2 = "#5A4035"
CINN = "#B47B5A"
TAUPE = "#7F6A5C"
CREAM = "#ECE5E0"
CARD = "#FFFFFF"
BORDER = "#EDE3DA"
ICON_BG = "#F3E7DE"
BEAN2 = "#8B5E3C"
GREEN = "#2D4F1E"


def fmt(v):
    s = f"{v:.2f}".rstrip("0").rstrip(".")
    return s if s else "0"


def rgb(c):
    c = c.lstrip("#")
    return tuple(int(c[i:i + 2], 16) for i in (0, 2, 4))


def lerp_hex(a, b, t):
    ra, ga, ba = rgb(a)
    rb, gb, bb = rgb(b)
    return (int(ra + (rb - ra) * t), int(ga + (gb - ga) * t), int(ba + (bb - ba) * t))


class Svg:
    def __init__(self):
        self.defs = []
        self.body = []
        self.n = 0

    def gradient_v(self, y0, y1, c1, c2):
        self.n += 1
        gid = f"g{self.n}"
        self.defs.append(
            f'<linearGradient id="{gid}" x1="0" y1="{fmt(y0)}" x2="0" y2="{fmt(y1)}" '
            f'gradientUnits="userSpaceOnUse">'
            f'<stop offset="0" stop-color="{c1}"/><stop offset="1" stop-color="{c2}"/>'
            f'</linearGradient>'
        )
        return gid

    def rect(self, x, y, w, h, fill, alpha=None, gid=None, stroke=None, sw=0):
        a = f' fill-opacity="{fmt(alpha)}"' if alpha is not None else ""
        f = f'url(#{gid})' if gid else fill
        s = f' stroke="{stroke}" stroke-width="{fmt(sw)}"' if stroke else ""
        self.body.append(
            f'<rect x="{fmt(x)}" y="{fmt(y)}" width="{fmt(w)}" height="{fmt(h)}" fill="{f}"{a}{s}/>'
        )

    def rrect(self, x, y, w, h, r, fill, corners=(1, 1, 1, 1), alpha=None,
              stroke=None, sw=0):
        tl, tr, br, bl = (r if c else 0 for c in corners)
        x2, y2 = x + w, y + h
        fill = fill if fill else "none"
        d = (
            f"M{fmt(x + tl)},{fmt(y)}"
            f"H{fmt(x2 - tr)}" + (f"A{fmt(tr)},{fmt(tr)} 0 0 1 {fmt(x2)},{fmt(y + tr)}" if tr else "")
            + f"V{fmt(y2 - br)}" + (f"A{fmt(br)},{fmt(br)} 0 0 1 {fmt(x2 - br)},{fmt(y2)}" if br else "")
            + f"H{fmt(x + bl)}" + (f"A{fmt(bl)},{fmt(bl)} 0 0 1 {fmt(x)},{fmt(y2 - bl)}" if bl else "")
            + f"V{fmt(y + tl)}" + (f"A{fmt(tl)},{fmt(tl)} 0 0 1 {fmt(x + tl)},{fmt(y)}" if tl else "")
            + "Z"
        )
        a = f' fill-opacity="{fmt(alpha)}"' if alpha is not None else ""
        s = f' stroke="{stroke}" stroke-width="{fmt(sw)}"' if stroke else ""
        self.body.append(f'<path d="{d}" fill="{fill}"{a}{s}/>')

    def circle(self, cx, cy, r, fill=None, alpha=None, stroke=None, sw=0):
        a = f' fill-opacity="{fmt(alpha)}"' if alpha is not None and fill else ""
        f = f' fill="{fill}"' if fill else ' fill="none"'
        s = f' stroke="{stroke}" stroke-width="{fmt(sw)}"' if stroke else ""
        self.body.append(
            f'<circle cx="{fmt(cx)}" cy="{fmt(cy)}" r="{fmt(r)}"{f}{a}{s}/>'
        )

    def arc(self, cx, cy, r, a0, a1, stroke, sw):
        def pt(a):
            rad = math.radians(a)
            return fmt(cx + r * math.cos(rad)), fmt(cy + r * math.sin(rad))

        x0, y0 = pt(a0)
        x1, y1 = pt(a1)
        large = 1 if (a1 - a0) % 360 > 180 else 0
        self.body.append(
            f'<path d="M{x0},{y0} A{fmt(r)},{fmt(r)} 0 {large} 1 {x1},{y1}" '
            f'fill="none" stroke="{stroke}" stroke-width="{fmt(sw)}" stroke-linecap="round"/>'
        )

    def line(self, x1, y1, x2, y2, stroke, sw):
        self.body.append(
            f'<line x1="{fmt(x1)}" y1="{fmt(y1)}" x2="{fmt(x2)}" y2="{fmt(y2)}" '
            f'stroke="{stroke}" stroke-width="{fmt(sw)}" stroke-linecap="round"/>'
        )

    def poly(self, pts, fill, stroke=None, sw=0):
        d = "M" + " L".join(f"{fmt(px)},{fmt(py)}" for px, py in pts) + "Z"
        s = f' stroke="{stroke}" stroke-width="{fmt(sw)}" stroke-linejoin="round"' if stroke else ""
        self.body.append(f'<path d="{d}" fill="{fill}"{s}/>')

    def path(self, ops, fill=None, stroke=None, sw=0):
        d = []
        for op in ops:
            if op[0] == "M":
                d.append(f"M{fmt(op[1])},{fmt(op[2])}")
            elif op[0] == "L":
                d.append(f"L{fmt(op[1])},{fmt(op[2])}")
            elif op[0] == "C":
                d.append(
                    f"C{fmt(op[1])},{fmt(op[2])} {fmt(op[3])},{fmt(op[4])} "
                    f"{fmt(op[5])},{fmt(op[6])}"
                )
            elif op[0] == "Q":
                d.append(f"Q{fmt(op[1])},{fmt(op[2])} {fmt(op[3])},{fmt(op[4])}")
            elif op[0] == "Z":
                d.append("Z")
        f = f' fill="{fill}"' if fill else ' fill="none"'
        s = f' stroke="{stroke}" stroke-width="{fmt(sw)}" stroke-linecap="round" stroke-linejoin="round"' if stroke else ""
        self.body.append(f'<path d="{"".join(d)}"{f}{s}/>')

    def text(self, x, y, s, size, fill, bold=False, anchor="middle", max_w=None):
        fam = "Arial, Helvetica, sans-serif"
        wgt = ' font-weight="bold"' if bold else ""
        self.body.append(
            f'<text x="{fmt(x)}" y="{fmt(y)}" font-family="{fam}"{wgt} '
            f'font-size="{fmt(size)}" fill="{fill}" text-anchor="'
            f'{"middle" if anchor == "middle" else "start"}" '
            f'dominant-baseline="central">{escape(s)}</text>'
        )

    def save(self, path):
        head = (
            f'<svg xmlns="http://www.w3.org/2000/svg" width="600mm" height="800mm" '
            f'viewBox="0 0 {fmt(W_MM)} {fmt(H_MM)}">'
        )
        defs = f"<defs>{''.join(self.defs)}</defs>" if self.defs else ""
        with open(path, "w", encoding="utf-8") as f:
            f.write(head + defs + "".join(self.body) + "</svg>")


def escape(s):
    return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")


class Pil:
    def __init__(self):
        self.img = Image.new("RGB", (round(W_MM * S), round(H_MM * S)), rgb(BG))
        self.d = ImageDraw.Draw(self.img)
        self._fill = rgb(BG)

    def _p(self, *v):
        return [round(x * S) for x in v]

    def _font(self, size, bold):
        path = FONT_B if bold else FONT_R
        return ImageFont.truetype(path, max(1, round(size * S)))

    def _alpha_shape(self, bbox, draw_fn, color, alpha=1.0):
        x0, y0, x1, y1 = (round(v * S) for v in bbox)
        w, h = max(1, x1 - x0), max(1, y1 - y0)
        mask = Image.new("L", (w, h), 0)
        draw_fn(ImageDraw.Draw(mask), x0, y0)
        if alpha < 1.0:
            mask = mask.point(lambda p: round(p * alpha))
        layer = Image.new("RGB", (w, h), rgb(color))
        self.img.paste(layer, (x0, y0), mask)

    def gradient_v(self, y0, y1, c1, c2):
        py0, py1 = round(y0 * S), round(y1 * S)
        for y in range(py0, py1):
            t = (y - py0) / max(1, py1 - py0)
            self.d.line([(0, y), (self.img.width, y)], fill=lerp_hex(c1, c2, t))

    def rect(self, x, y, w, h, fill, alpha=None, gid=None, stroke=None, sw=0):
        if gid is not None:
            return
        color = fill
        if alpha is None:
            self.d.rectangle(self._p(x, y, x + w, y + h), fill=rgb(color))
        else:
            self._alpha_shape((x, y, x + w, y + h),
                              lambda dr, ox, oy: dr.rectangle(
                                  (0, 0, round(w * S), round(h * S)), fill=255),
                              color, alpha)
        if stroke:
            self.d.rectangle(self._p(x, y, x + w, y + h), outline=rgb(stroke),
                             width=max(1, round(sw * S)))

    def rrect(self, x, y, w, h, r, fill, corners=(1, 1, 1, 1), alpha=None,
              stroke=None, sw=0):
        box = self._p(x, y, x + w, y + h)
        rad = round(r * S)
        if alpha is None:
            if fill:
                self.d.rounded_rectangle(box, radius=rad, fill=rgb(fill),
                                         corners=tuple(bool(c) for c in corners))
            if stroke:
                self.d.rounded_rectangle(box, radius=rad, outline=rgb(stroke),
                                         width=max(1, round(sw * S)),
                                         corners=tuple(bool(c) for c in corners))
        else:
            self._alpha_shape((x, y, x + w, y + h),
                              lambda dr, ox, oy: dr.rounded_rectangle(
                                  (0, 0, round(w * S), round(h * S)), radius=rad,
                                  fill=255,
                                  corners=tuple(bool(c) for c in corners)),
                              fill, alpha)

    def circle(self, cx, cy, r, fill=None, alpha=None, stroke=None, sw=0):
        box = self._p(cx - r, cy - r, cx + r, cy + r)
        if fill and alpha is None:
            self.d.ellipse(box, fill=rgb(fill))
        elif fill and alpha is not None:
            self._alpha_shape((cx - r, cy - r, cx + r, cy + r),
                              lambda dr, ox, oy: dr.ellipse(
                                  (0, 0, round(2 * r * S), round(2 * r * S)), fill=255),
                              fill, alpha)
        if stroke:
            self.d.ellipse(box, outline=rgb(stroke), width=max(1, round(sw * S)))

    def arc(self, cx, cy, r, a0, a1, stroke, sw):
        self.d.arc(self._p(cx - r, cy - r, cx + r, cy + r), a0, a1,
                   fill=rgb(stroke), width=max(1, round(sw * S)))

    def line(self, x1, y1, x2, y2, stroke, sw):
        self.d.line(self._p(x1, y1, x2, y2), fill=rgb(stroke),
                    width=max(1, round(sw * S)))

    def poly(self, pts, fill, stroke=None, sw=0):
        p = [tuple(self._p(px, py)) for px, py in pts]
        self.d.polygon(p, fill=rgb(fill))
        if stroke:
            self.d.line(p + [p[0]], fill=rgb(stroke), width=max(1, round(sw * S)))

    def path(self, ops, fill=None, stroke=None, sw=0):
        pts = flatten(ops, steps=28)
        if fill:
            self.poly(pts, fill)
        if stroke:
            for i in range(len(pts) - 1):
                self.line(*pts[i], *pts[i + 1], stroke, sw)

    def text(self, x, y, s, size, fill, bold=False, anchor="middle", max_w=None):
        f = self._font(size, bold)
        if max_w:
            while self.d.textlength(s, font=f) > max_w * S and size > 6:
                size -= 0.5
                f = self._font(size, bold)
        a = "mm" if anchor == "middle" else "lm"
        self.d.text((x * S, y * S), s, font=f, fill=rgb(fill), anchor=a)


def flatten(ops, steps=28):
    pts = []
    cur = (0.0, 0.0)
    for op in ops:
        k = op[0]
        if k == "M":
            cur = (op[1], op[2])
            pts.append(cur)
        elif k == "L":
            cur = (op[1], op[2])
            pts.append(cur)
        elif k == "C":
            p0 = cur
            p1, p2, p3 = (op[1], op[2]), (op[3], op[4]), (op[5], op[6])
            for i in range(1, steps + 1):
                t = i / steps
                mt = 1 - t
                x = (mt ** 3 * p0[0] + 3 * mt * mt * t * p1[0]
                     + 3 * mt * t * t * p2[0] + t ** 3 * p3[0])
                y = (mt ** 3 * p0[1] + 3 * mt * mt * t * p1[1]
                     + 3 * mt * t * t * p2[1] + t ** 3 * p3[1])
                pts.append((x, y))
            cur = p3
        elif k == "Q":
            p0 = cur
            p1, p2 = (op[1], op[2]), (op[3], op[4])
            for i in range(1, steps + 1):
                t = i / steps
                mt = 1 - t
                x = mt * mt * p0[0] + 2 * mt * t * p1[0] + t * t * p2[0]
                y = mt * mt * p0[1] + 2 * mt * t * p1[1] + t * t * p2[1]
                pts.append((x, y))
            cur = p2
    return pts


def logo_ops(L):
    """Ops del logo (proporciones de LogoVector.kt) en mm, centrado en 0,0."""
    def P(nx, ny):
        return (nx - 0.5) * L, (ny - 0.5) * L

    def cubic(b, c, d):
        return ("C", *P(*b), *P(*c), *P(*d))

    def quad(b, c):
        return ("Q", *P(*b), *P(*c))

    left = [("M", *P(0.5, 0.2)),
            cubic((0.3, 0.2), (0.25, 0.5), (0.35, 0.75)),
            cubic((0.4, 0.85), (0.5, 0.8), (0.5, 0.8)),
            cubic((0.45, 0.6), (0.55, 0.4), (0.5, 0.2)), ("Z",)]
    right = [("M", *P(0.5, 0.2)),
             cubic((0.7, 0.2), (0.75, 0.5), (0.65, 0.75)),
             cubic((0.6, 0.85), (0.5, 0.8), (0.5, 0.8)),
             cubic((0.45, 0.6), (0.55, 0.4), (0.5, 0.2)), ("Z",)]
    lleaf = [("M", *P(0.5, 0.85)), quad((0.2, 0.8), (0.2, 0.6)),
             quad((0.4, 0.55), (0.5, 0.85)), ("Z",)]
    rleaf = [("M", *P(0.5, 0.85)), quad((0.8, 0.8), (0.8, 0.6)),
             quad((0.6, 0.55), (0.5, 0.85)), ("Z",)]
    return left, right, lleaf, rleaf


def draw_logo(c, cx, cy, L):
    c.circle(cx, cy, L * 0.54, fill=BG)
    c.circle(cx, cy, L * 0.45, stroke=CINN, sw=L * 0.02)
    left, right, lleaf, rleaf = logo_ops(L)

    def shift(ops):
        out = []
        for op in ops:
            if op[0] == "Z":
                out.append(op)
            else:
                vals = list(op[1:])
                vals2 = []
                for i, v in enumerate(vals):
                    vals2.append(v + (cx if i % 2 == 0 else cy))
                out.append((op[0], *vals2))
        return out

    c.path(shift(left), fill=COFFEE)
    c.path(shift(right), fill=BEAN2)
    c.path(shift(lleaf), fill=GREEN)
    c.path(shift(rleaf), fill=GREEN)
    c.path(shift(lleaf), stroke=CINN, sw=L * 0.014)
    c.path(shift(rleaf), stroke=CINN, sw=L * 0.014)


def icon(kind, c, cx, cy, s=12.0, col=COFFEE, w=2.2):
    """Icono simple centrado en (cx,cy), caja ~2s mm."""
    if kind == "cup":
        c.rrect(cx - s * 0.7, cy - s * 0.5, s * 1.15, s * 1.15, s * 0.22,
                fill=None, stroke=col, sw=w)
        c.arc(cx + s * 0.55, cy, s * 0.35, -70, 70, col, w)
        c.line(cx - s * 0.7, cy + s * 0.85, cx + s * 0.5, cy + s * 0.85, col, w)
        c.line(cx - s * 0.3, cy - s * 0.8, cx - s * 0.3, cy - s * 0.55, col, w)
        c.line(cx + s * 0.05, cy - s * 0.9, cx + s * 0.05, cy - s * 0.55, col, w)
    elif kind == "bag":
        c.rrect(cx - s * 0.65, cy - s * 0.35, s * 1.3, s * 1.2, s * 0.18,
                fill=None, stroke=col, sw=w)
        c.arc(cx, cy - s * 0.35, s * 0.33, 180, 360, col, w)
    elif kind == "pin":
        c.circle(cx, cy - s * 0.2, s * 0.5, stroke=col, sw=w)
        c.circle(cx, cy - s * 0.2, s * 0.16, fill=col)
        c.poly([(cx - s * 0.36, cy + s * 0.18), (cx + s * 0.36, cy + s * 0.18),
                (cx, cy + s * 0.85)], fill=col)
    elif kind == "promo":
        c.circle(cx - s * 0.4, cy - s * 0.4, s * 0.3, stroke=col, sw=w)
        c.circle(cx + s * 0.4, cy + s * 0.4, s * 0.3, stroke=col, sw=w)
        c.line(cx - s * 0.62, cy + s * 0.65, cx + s * 0.62, cy - s * 0.65, col, w)
    elif kind == "star":
        pts = []
        for i in range(10):
            r = s * (0.75 if i % 2 == 0 else 0.32)
            a = math.radians(-90 + i * 36)
            pts.append((cx + r * math.cos(a), cy + r * math.sin(a)))
        c.poly(pts, fill=col)
    elif kind == "card":
        c.rrect(cx - s * 0.75, cy - s * 0.5, s * 1.5, s * 1.0, s * 0.15,
                fill=None, stroke=col, sw=w)
        c.line(cx - s * 0.75, cy - s * 0.12, cx + s * 0.75, cy - s * 0.12, col, w)
        c.line(cx - s * 0.5, cy + s * 0.28, cx - s * 0.1, cy + s * 0.28, col, w)


FEATURES = [
    ("cup", "Menú digital", "Café, postres y panadería"),
    ("bag", "Pedidos en línea", "Estado en vivo de tu orden"),
    ("pin", "Delivery rápido", "Llega calientito a tu puerta"),
    ("promo", "Promociones", "Cupón CAFE20 con -20%"),
    ("star", "Recompensas", "Acumula puntos en cada compra"),
    ("card", "Pagos fáciles", "Tarjeta, efectivo o wallet"),
]


def make_qr():
    qr = qrcode.QRCode(error_correction=qrcode.constants.ERROR_CORRECT_M,
                       box_size=1, border=2)
    qr.add_data(QR_URL)
    qr.make(fit=True)
    return qr.get_matrix()


def draw_qr(c, matrix, x, y, size, fg=COFFEE):
    n = len(matrix)
    m = size / n
    for row in range(n):
        for col in range(n):
            if matrix[row][col]:
                c.rect(x + col * m, y + row * m, m, m, fg)


def build(c):
    # Fondo
    c.rect(0, 0, W_MM, H_MM, BG)

    # Hero
    c.rect(0, 0, W_MM, 310, COFFEE)
    gid = c.gradient_v(0, 310, COFFEE, COFFEE2)
    c.rect(0, 0, W_MM, 310, COFFEE, gid=gid)
    c.circle(535, 20, 100, fill=CINN, alpha=0.20)
    c.circle(35, 230, 80, fill=CINN, alpha=0.14)

    # Pastilla superior
    pill_w, pill_h = 330, 30
    c.rrect((W_MM - pill_w) / 2, 26, pill_w, pill_h, pill_h / 2,
            fill="#FFFFFF", alpha=0.12, stroke=CINN, sw=0.8)
    c.text(W_MM / 2, 41, "PEDIDOS  ·  DELIVERY  ·  RECOMPENSAS", 12,
           CINN, bold=True, max_w=pill_w - 24)

    # Logo + titulo
    draw_logo(c, W_MM / 2, 132, 108)
    c.text(W_MM / 2, 237, "CAFÉ AROMA", 58, CREAM, bold=True, max_w=540)
    c.text(W_MM / 2, 280, "¿Qué se te antoja hoy?", 20, "#D9C7BA", max_w=540)

    # Titulo de seccion
    c.text(W_MM / 2, 341, "TODO TU CAFÉ EN UNA APP", 21, COFFEE, bold=True, max_w=540)
    c.rect(W_MM / 2 - 30, 356, 60, 3, CINN)

    # Tarjetas de funciones
    x0, colw, gap, ch = 40.0, 250.0, 20.0, 58.0
    rows = [372.0, 442.0, 512.0]
    for i, (ic, title, sub) in enumerate(FEATURES):
        cx = x0 + (i % 2) * (colw + gap)
        cy = rows[i // 2]
        c.rrect(cx, cy, colw, ch, 12, fill=CARD, stroke=BORDER, sw=1)
        c.circle(cx + 40, cy + ch / 2, 20, fill=ICON_BG)
        icon(ic, c, cx + 40, cy + ch / 2, s=11, col=COFFEE, w=2.2)
        c.text(cx + 72, cy + 20, title, 17, COFFEE, bold=True,
               anchor="start", max_w=colw - 82)
        c.text(cx + 72, cy + 41, sub, 11.5, TAUPE, anchor="start",
               max_w=colw - 82)

    # Bloque QR
    qx, qy, qw, qh = 40.0, 580.0, 520.0, 150.0
    c.rrect(qx, qy, qw, qh, 18, fill=CARD, stroke=BORDER, sw=1)
    qr_size = 120.0
    draw_qr(c, QR, qx + 15, qy + 15, qr_size)
    tx = qx + 155
    c.text(tx, qy + 40, "Escanea y pide tu café", 25, COFFEE, bold=True,
           anchor="start", max_w=qw - 170)
    c.text(tx, qy + 70, "Descarga la app y pide en minutos.", 13.5, TAUPE,
           anchor="start", max_w=qw - 170)
    c.text(tx, qy + 90, "Menú · Delivery · Promociones", 13.5, TAUPE,
           anchor="start", max_w=qw - 170)
    bw, bh = 215, 32
    c.rrect(tx, qy + 105, bw, bh, bh / 2, fill=CINN)
    c.text(tx + bw / 2, qy + 105 + bh / 2, "-20% en tu primer pedido", 13.5,
           "#FFFFFF", bold=True, max_w=bw - 16)

    # Footer
    c.rect(0, 745, W_MM, H_MM - 745, COFFEE)
    c.text(W_MM / 2, 767, "Av. Esencia #123   ·   +1 234 567 890", 15, CREAM,
           bold=True, max_w=540)
    c.text(W_MM / 2, 789, "Café Aroma · Pide desde tu móvil", 11.5, CINN,
           max_w=540)


QR = make_qr()

svg = Svg()
build(svg)
svg.save(os.path.join(OUT, "cafe_aroma_banner.svg"))

pil = Pil()
build(pil)
png = os.path.join(OUT, "cafe_aroma_banner.png")
pil.img.save(png, dpi=(DPI, DPI))

prev = pil.img.copy()
prev.thumbnail((1000, 1400))
prev.save(os.path.join(OUT, "preview.jpg"), quality=90)

print("OK", png)
print("px:", pil.img.size, "qr modules:", len(QR))
