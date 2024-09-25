package org.olafneumann.palette.colorful

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cbrt
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.PI

// taken over from
// https://github.com/mojzesh/swift-colorful/blob/master/Sources/Colorful/colors.swift
// https://github.com/mojzesh/swift-colorful/blob/master/Sources/Colorful/hsluv.swift

data class WhiteReference(var wref: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)) {
    constructor(X: Double, Y: Double, Z: Double) : this(doubleArrayOf(X, Y, Z))
}

private fun sq(v: Double): Double {
    return v * v
}

private fun cub(v: Double): Double {
    return v * v * v
}

// clamp01 clamps from 0 to 1.
private fun clamp01(v: Double): Double {
    return max(0.0, min(v, 1.0))
}

// This is the tolerance used when comparing colors using AlmostEqualRgb.
private const val Delta: Double = 1.0 / 255.0

// This is the default reference white point.
val D65: WhiteReference = WhiteReference(X = 0.95047, Y = 1.00000, Z = 1.08883)

// And another one.
val D50: WhiteReference = WhiteReference(X = 0.96422, Y = 1.00000, Z = 0.82521)

var hSLuvD65: WhiteReference = WhiteReference(X = 0.95045592705167, Y = 1.0, Z = 1.089057750759878)

data class Color(var R: Double = 0.0, var G: Double = 0.0, var B: Double = 0.0) : Comparable<Color> {

    fun RGBA(): RGBA {
        val r = (R * 65535.0 + 0.5).toInt()
        val g = (G * 65535.0 + 0.5).toInt()
        val b = (B * 65535.0 + 0.5).toInt()
        val a: Int = 0xFFFF
        return RGBA(r, g, b, a)
    }

    // Might come in handy sometimes to reduce boilerplate code.
    fun RGB255(): RGB255 {
        val r = ((R * 255.0) + 0.5).toInt()
        val g = ((G * 255.0) + 0.5).toInt()
        val b = ((B * 255.0) + 0.5).toInt()
        return RGB255(r, g, b)
    }

    // Used to simplify HSLuv testing.
    fun Values(): RGB {
        return RGB(R, G, B)
    }

    // Checks whether the color exists in RGB space, i.e. all values are in [0..1]
    fun IsValid(): Boolean {
        return R in 0.0..1.0 && G in 0.0..1.0 && B in 0.0..1.0
    }

    // Returns Clamps the color into valid range, clamping each value to [0..1]
    // If the color is valid already, this is a no-op.
    fun Clamped(): Color {
        return Color(R = clamp01(R), G = clamp01(G), B = clamp01(B))
    }

    // DistanceRgb computes the distance between two colors in RGB space.
    // This is not a good measure! Rather do it in Lab space.
    fun DistanceRgb(c2: Color): Double {
        return sqrt(sq(R - c2.R) + sq(G - c2.G) + sq(B - c2.B))
    }

    // DistanceLinearRgb computes the distance between two colors in linear RGB
    // space. This is not useful for measuring how humans perceive color, but
    // might be useful for other things, like dithering.
    fun DistanceLinearRgb(c2: Color): Double {
        val (r1, g1, b1) = LinearRgb()
        val (r2, g2, b2) = c2.LinearRgb()
        return sqrt(sq(r1 - r2) + sq(g1 - g2) + sq(b1 - b2))
    }


    // DistanceLinearRGB is deprecated in favour of DistanceLinearRgb.
    // They do the exact same thing.
    fun DistanceLinearRGB(c2: Color): Double {
        return DistanceLinearRgb(c2)
    }

    // DistanceRiemersma is a color distance algorithm developed by Thiadmer Riemersma.
    // It uses RGB coordinates, but he claims it has similar results to CIELUV.
    // This makes it both fast and accurate.
    //
    // Sources:
    //
    //     https://www.compuphase.com/cmetric.htm
    //     https://github.com/lucasb-eyer/go-colorful/issues/52
    fun DistanceRiemersma(c2: Color): Double {
        val rAvg: Double = (this.R + c2.R) / 2.0
        // Deltas
        val dR: Double = this.R - c2.R
        val dG: Double = this.G - c2.G
        val dB: Double = this.B - c2.B

        return sqrt((2 + rAvg) * dR * dR + 4 * dG * dG + (2 + (1 - rAvg)) * dB * dB)
    }

    // Check for equality between colors within the tolerance Delta (1/255).
    fun AlmostEqualRgb(c2: Color): Boolean {
        return abs(this.R - c2.R) +
                abs(this.G - c2.G) +
                abs(this.B - c2.B) < (3.0 * Delta)
    }

    // You don't really want to use this, do you? Go for BlendLab, BlendLuv or BlendHcl.
    public fun BlendRgb(c2: Color, t: Double): Color {
        return Color(R = this.R + t * (c2.R - this.R), G = this.G + t * (c2.G - this.G), B = this.B + t * (c2.B - this.B))
    }


    /// HSV ///
    ///////////
    // From http://en.wikipedia.org/wiki/HSL_and_HSV
    // Note that h is in [0..360] and s,v in [0..1]

    // Hsv returns the Hue [0..360], Saturation and Value [0..1] of the color.
    public fun Hsv(): HSV {
        val min = min(min(this.R, this.G), this.B)
        val v = max(max(this.R, this.G), this.B)
        val C = v - min

        var s = 0.0
        if (v != 0.0) {
            s = C / v
        }

        var h = 0.0 // We use 0 instead of undefined as in wp.
        if (min != v) {
            if (v == this.R) {
                h = ((this.G - this.B) / C).mod(6.0)
            }
            if (v == this.G) {
                h = (this.B - this.R) / C + 2.0
            }
            if (v == this.B) {
                h = (this.R - this.G) / C + 4.0
            }
            h *= 60.0
            if (h < 0.0) {
                h += 360.0
            }
        }
        return HSV(h, s, v)
    }


    // You don't really want to use this, do you? Go for BlendLab, BlendLuv or BlendHcl.
    public fun BlendHsv(c2: Color, t: Double): Color {
        var (h1, s1, v1) = this.Hsv()
        var (h2, s2, v2) = c2.Hsv()

        // https://github.com/lucasb-eyer/go-colorful/pull/60
        if (s1 == 0.0 && s2 != 0.0) {
            h1 = h2
        } else if (s2 == 0.0 && s1 != 0.0) {
            h2 = h1
        }

        // We know that h are both in [0..360]
        return Hsv(H = interp_angle(a0 = h1, a1 = h2, t = t), S = s1 + t * (s2 - s1), V = v1 + t * (v2 - v1))
    }

    /// HSL ///
    ///////////

    // Hsl returns the Hue [0..360], Saturation [0..1], and Luminance (lightness) [0..1] of the color.
    public fun Hsl(): HSL {
        val min: Double = min(min(this.R, this.G), this.B)
        val max: Double = max(max(this.R, this.G), this.B)

        val l: Double = (max + min) / 2

        var s: Double
        var h: Double

        if (min == max) {
            s = 0.0
            h = 0.0
        } else {
            if (l < 0.5) {
                s = (max - min) / (max + min)
            } else {
                s = (max - min) / (2.0 - max - min)
            }

            if (max == this.R) {
                h = (this.G - this.B) / (max - min)
            } else if (max == this.G) {
                h = 2.0 + (this.B - this.R) / (max - min)
            } else {
                h = 4.0 + (this.R - this.G) / (max - min)
            }

            h *= 60

            if (h < 0) {
                h += 360
            }
        }

        return HSL(h, s, l)
    }


    /// Hex ///
    ///////////

    // Hex returns the hex "html" representation of the color, as in #ff0080.
    public fun Hex(): String = "#${R.asHexByte()}${G.asHexByte()}${B.asHexByte()}"

    // Add 0.5 for rounding
    private fun Double.asRoundedByte(): Int = (this * 255.0 + 0.5).toInt()
    @OptIn(ExperimentalStdlibApi::class)
    private fun Int.asHex(): String = this.toByte().toHexString(format = HexFormat.UpperCase).padStart(length = 2, padChar = '0')
    private fun Double.asHexByte(): String = asRoundedByte().asHex()

    // LinearRgb converts the color into the linear RGB space (see http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/).
    public fun LinearRgb(): RGB {
        val r = linearize(this.R)
        val g = linearize(this.G)
        val b = linearize(this.B)
        return RGB(r, g, b)
    }


    // FastLinearRgb is much faster than and almost as accurate as LinearRgb.
    // BUT it is important to NOTE that they only produce good results for valid colors r,g,b in [0,1].
    public fun FastLinearRgb(): RGB {
        val r = linearize_fast(this.R)
        val g = linearize_fast(this.G)
        val b = linearize_fast(this.B)
        return RGB(r, g, b)
    }

    // BlendLinearRgb blends two colors in the Linear RGB color-space.
    // Unlike BlendRgb, this will not produce dark color around the center.
    // t == 0 results in c1, t == 1 results in c2
    public fun BlendLinearRgb(c2: Color, t: Double): Color {
        val (r1, g1, b1) = this.LinearRgb()
        val (r2, g2, b2) = c2.LinearRgb()
        return LinearRgb(
            r = r1 + t * (r2 - r1),
            g = g1 + t * (g2 - g1),
            b = b1 + t * (b2 - b1)
        )
    }

    /// XYZ ///
    ///////////
    // http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/

    public fun Xyz(): XYZ {
        val (r, g, b) = this.LinearRgb()
        return LinearRgbToXyz(r = r, g = g, b = b)
    }

    // Converts the given color to CIE xyY space using D65 as reference white.
    // (Note that the reference white is only used for black input.)
    // x, y and Y are in [0..1]
    public fun Xyy(): XYYout {
        val (x, y, z) = this.Xyz()
        val (X, Y, Yout) = XyzToXyy(X = x, Y = y, Z = z)
        return XYYout(X, Y, Yout)
    }

    // Converts the given color to CIE xyY space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // (Note that the reference white is only used for black input.)
    // x, y and Y are in [0..1]
    public fun XyyWhiteRef(wref: WhiteReference): XYYout {
        val (X, Y2, Z) = this.Xyz()
        val (x, y, Yout) = XyzToXyyWhiteRef(X = X, Y = Y2, Z = Z, wref = wref)
        return XYYout(x, y, Yout)
    }

    // Converts the given color to CIE L*u*v* space using D65 as reference white.
    // L* is in [0..1] and both u* and v* are in about [-1..1]
    public fun Luv(): LUV {
        val (x, y, z) = this.Xyz()
        val (l, u, v) = XyzToLuv(x = x, y = y, z = z)
        return LUV(l, u, v)
    }

    // Converts the given color to CIE L*u*v* space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // L* is in [0..1] and both u* and v* are in about [-1..1]
    public fun LuvWhiteRef(wref: WhiteReference): LUV {
        val (x, y, z) = this.Xyz()
        val (l, u, v) = XyzToLuvWhiteRef(x = x, y = y, z = z, wref = wref)
        return LUV(l, u, v)
    }

    // Converts the given color to CIE L*a*b* space using D65 as reference white.
    public fun Lab(): LAB {
        val (x, y, z) = this.Xyz()
        return XyzToLab(x = x, y = y, z = z)
    }

    // Converts the given color to CIE L*a*b* space, taking into account
    // a given reference white. (i.e. the monitor's white)
    public fun LabWhiteRef(wref: WhiteReference): LAB {
        val (x, y, z) = this.Xyz()
        return XyzToLabWhiteRef(x= x, y = y, z = z, wref = wref)
    }

    // DistanceLab is a good measure of visual similarity between two colors!
    // A result of 0 would mean identical colors, while a result of 1 or higher
    // means the colors differ a lot.
    public fun DistanceLab(c2: Color): Double {
        val (l1, a1, b1) = Lab()
        val (l2, a2, b2) = c2.Lab()
        return sqrt(sq(l1 - l2) + sq(a1 - a2) + sq(b1 - b2))
    }

    // DistanceCIE76 is the same as DistanceLab.
    public fun DistanceCIE76(c2: Color): Double {
        return this.DistanceLab(c2)
    }

    // Uses the CIE94 formula to calculate color distance. More accurate than
    // DistanceLab, but also more work.
    public fun DistanceCIE94(cr: Color): Double {
        var (l1, a1, b1) = this.Lab()
        var (l2, a2, b2) = cr.Lab()

        // NOTE: Since all those formulas expect L,a,b values 100x larger than we
        //       have them in this library, we either need to adjust all constants
        //       in the formula, or convert the ranges of L,a,b before, and then
        //       scale the distances down again. The latter is less error-prone.
        l1 = l1 * 100.0
        a1 = a1 * 100.0
        b1 = b1 * 100.0
        l2 = l2 * 100.0
        a2 = a2 * 100.0
        b2 = b2 * 100.0

        val kl = 1.0 // 2.0 for textiles
        val kc = 1.0
        val kh = 1.0
        val k1 = 0.045 // 0.048 for textiles
        val k2 = 0.015 // 0.014 for textiles.

        val deltaL = l1 - l2
        val c1 = sqrt(sq(a1) + sq(b1))
        val c2 = sqrt(sq(a2) + sq(b2))
        val deltaCab = c1 - c2

        // Not taking Sqrt here for stability, and it's unnecessary.
        val deltaHab2 = sq(a1 - a2) + sq(b1 - b2) - sq(deltaCab)
        val sl = 1.0
        val sc = 1.0 + k1 * c1
        val sh = 1.0 + k2 * c1

        val vL2 = sq(deltaL / (kl * sl))
        val vC2 = sq(deltaCab / (kc * sc))
        val vH2 = deltaHab2 / sq(kh * sh)

        return sqrt(vL2 + vC2 + vH2) * 0.01 // See above.
    }

    // DistanceCIEDE2000 uses the Delta E 2000 formula to calculate color
    // distance. It is more expensive but more accurate than both DistanceLab
    // and DistanceCIE94.
    public fun DistanceCIEDE2000(cr: Color): Double {
        return this.DistanceCIEDE2000klch(cr = cr, kl = 1.0, kc = 1.0, kh = 1.0)
    }

    // DistanceCIEDE2000klch uses the Delta E 2000 formula with custom values
    // for the weighting factors kL, kC, and kH.
    public fun DistanceCIEDE2000klch(cr: Color, kl: Double, kc: Double, kh: Double): Double {
        var (l1, a1, b1) = this.Lab()
        var (l2, a2, b2) = cr.Lab()

        // As with CIE94, we scale up the ranges of L,a,b beforehand and scale
        // them down again afterwards.
        l1 = l1 * 100.0
        a1 = a1 * 100.0
        b1 = b1 * 100.0
        l2 = l2 * 100.0
        a2 = a2 * 100.0
        b2 = b2 * 100.0

        val cab1 = sqrt(sq(a1) + sq(b1))
        val cab2 = sqrt(sq(a2) + sq(b2))
        val cabmean = (cab1 + cab2) / 2

        val g = 0.5 * (1 - sqrt(cabmean.pow(7) / (cabmean.pow(7) + 25.0.pow(7))))
        val ap1 = (1 + g) * a1
        val ap2 = (1 + g) * a2
        val cp1 = sqrt(sq(ap1) + sq(b1))
        val cp2 = sqrt(sq(ap2) + sq(b2))

        var hp1 = 0.0
        if (b1 != ap1 || ap1 != 0.0) {
            hp1 = atan2(b1, ap1)
            if (hp1 < 0) {
                hp1 += PI * 2
            }
            hp1 *= 180 / PI
        }
        var hp2 = 0.0
        if (b2 != ap2 || ap2 != 0.0) {
            hp2 = atan2(b2, ap2)
            if (hp2 < 0) {
                hp2 += PI * 2
            }
            hp2 *= 180 / PI
        }

        val deltaLp = l2 - l1
        val deltaCp = cp2 - cp1
        var dhp = 0.0
        val cpProduct = cp1 * cp2
                if (cpProduct != 0.0) {
                    dhp = hp2 - hp1
                    if (dhp > 180) {
                        dhp -= 360
                    } else if (dhp < -180) {
                        dhp += 360
                    }
                }
        val deltaHp = 2 * sqrt(cpProduct) * sin(dhp / 2 * PI / 180)

        val lpmean = (l1 + l2) / 2
        val cpmean = (cp1 + cp2) / 2
        var hpmean = hp1 + hp2
        if (cpProduct != 0.0) {
            hpmean /= 2
            if (abs(hp1 - hp2) > 180) {
                if (hp1 + hp2 < 360) {
                    hpmean += 180
                } else {
                    hpmean -= 180
                }
            }
        }

        val t = 1 - 0.17 * cos((hpmean - 30) * PI / 180) + 0.24 * cos(2 * hpmean * PI / 180) + 0.32 * cos((3 * hpmean + 6) * PI / 180) - 0.2 * cos((4 * hpmean - 63) * PI / 180)
        val deltaTheta = 30 * exp(-sq((hpmean - 275) / 25))
        val rc = 2 * sqrt(cpmean.pow(7) / (cpmean.pow(7) + 25.0.pow(7)))
        val sl = 1 + sqrt(0.015 * sq(lpmean - 50)) / (20 + sq(lpmean - 50))
        val sc = 1 + 0.045 * cpmean
        val sh = 1 + 0.015 * cpmean * t
        val rt = -sin(2 * deltaTheta * PI / 180) * rc

        return sqrt(sq(deltaLp / (kl * sl)) + sq(deltaCp / (kc * sc)) + sq(deltaHp / (kh * sh)) + rt * (deltaCp / (kc * sc)) * (deltaHp / (kh * sh))) * 0.01
    }

    // BlendLab blends two colors in the L*a*b* color-space, which should result in a smoother blend.
    // t == 0 results in c1, t == 1 results in c2
    public fun BlendLab(c2: Color, t: Double): Color {
        val (l1, a1, b1) = this.Lab()
        val (l2, a2, b2) = c2.Lab()
        return Lab(l = l1 + t * (l2 - l1),
            a = a1 + t * (a2 - a1),
            b = b1 + t * (b2 - b1))
    }

    // DistanceLuv is a good measure of visual similarity between two colors!
    // A result of 0 would mean identical colors, while a result of 1 or higher
    // means the colors differ a lot.
    public fun DistanceLuv(c2: Color): Double {
        val (l1, u1, v1) = this.Luv()
        val (l2, u2, v2) = c2.Luv()
        return sqrt(sq(l1 - l2) + sq(u1 - u2) + sq(v1 - v2))
    }

    // BlendLuv blends two colors in the CIE-L*u*v* color-space, which should result in a smoother blend.
    // t == 0 results in c1, t == 1 results in c2
    public fun BlendLuv(c2: Color, t: Double): Color {
        val (l1, u1, v1) = this.Luv()
        val (l2, u2, v2) = c2.Luv()
        return Luv(l = l1 + t * (l2 - l1),
            u = u1 + t * (u2 - u1),
            v = v1 + t * (v2 - v1))
    }

    /// HCL ///
    ///////////
    // HCL is nothing else than L*a*b* in cylindrical coordinates!
    // (this was wrong on English wikipedia, I fixed it, let's hope the fix stays.)
    // But it is widely popular since it is a "correct HSV"
    // http://www.hunterlab.com/appnotes/an09_96a.pdf

    // Converts the given color to HCL space using D65 as reference white.
    // H values are in [0..360], C and L values are in [0..1] although C can overshoot 1.0
    public fun Hcl(): HCL {
        return this.HclWhiteRef(wref = D65)
    }

    // Converts the given color to HCL space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // H values are in [0..360], C and L values are in [0..1]
    public fun HclWhiteRef(wref: WhiteReference): HCL {
        val (L, a, b) = this.LabWhiteRef(wref = wref)
        return LabToHcl(L = L, a = a, b = b)
    }

    // BlendHcl blends two colors in the CIE-L*C*h° color-space, which should result in a smoother blend.
    // t == 0 results in c1, t == 1 results in c2
    public fun BlendHcl(c2: Color, t: Double): Color {
        var (h1, c1, l1) = Hcl()
        var (h2, c2, l2) = c2.Hcl()

        // https://github.com/lucasb-eyer/go-colorful/pull/60
        if (c1 <= 0.00015 && c2 >= 0.00015) {
            h1 = h2
        } else if (c2 <= 0.00015 && c1 >= 0.00015) {
            h2 = h1
        }

        // We know that h are both in [0..360]
        return Hcl(h = interp_angle(a0 = h1, a1 = h2, t = t), c = c1 + t * (c2 - c1), l = l1 + t * (l2 - l1)).Clamped()
    }

    // LuvLch

    // Converts the given color to LuvLCh space using D65 as reference white.
    // h values are in [0..360], C and L values are in [0..1] although C can overshoot 1.0
    public fun LuvLCh(): LCH {
        return LuvLChWhiteRef(wref = D65)
    }

    // Converts the given color to LuvLCh space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // h values are in [0..360], c and l values are in [0..1]
    public fun LuvLChWhiteRef(wref: WhiteReference): LCH {
        val (l, u, v) = LuvWhiteRef(wref = wref)
        return LuvToLuvLCh(L = l, u = u, v = v)
    }

    // Generates a color by using data given in LuvLCh space using D65 as reference white.
    // h values are in [0..360], C and L values are in [0..1]
    // WARNING: many combinations of `l`, `c`, and `h` values do not have corresponding
    // valid RGB values, check the FAQ in the README if (you're unsure.
    public fun LuvLCh(l: Double, c: Double, h: Double): Color {
        return LuvLChWhiteRef(l = l, c = c, h = h, wref = D65)
    }

    // BlendLuvLCh blends two colors in the cylindrical CIELUV color space.
    // t == 0 results in c1, t == 1 results in c2
    public fun BlendLuvLCh(c2: Color, t: Double): Color {
        val (l1, c1, h1) = LuvLCh()
        val (l2, c2, h2) = c2.LuvLCh()

        // We know that h are both in [0..360]
        return LuvLCh(l = l1 + t * (l2 - l1), c = c1 + t * (c2 - c1), h = interp_angle(a0 = h1, a1 = h2, t = t))
    }



    // HSLuv returns the Hue, Saturation and Luminance of the color in the HSLuv
    // color space. Hue in [0..360], a Saturation [0..1], and a Luminance
    // (lightness) in [0..1].
    public fun HSLuv(): HSL {
        // sRGB -> Linear RGB -> CIEXYZ -> CIELUV -> LuvLCh -> HSLuv
        var s: Double
        var lch/*(l, c, h)*/ = LuvLChWhiteRef(wref = hSLuvD65)
        return LuvLChToHSLuv(l = lch.l, c = lch.c, h = lch.h)
    }

    // HPLuv returns the Hue, Saturation and Luminance of the color in the HSLuv
    // color space. Hue in [0..360], a Saturation [0..1], and a Luminance
    // (lightness) in [0..1].
    //
    // Note that HPLuv can only represent pastel colors, and so the Saturation
    // value could be much larger than 1 for colors it can't represent.
    public fun HPLuv(): HSL {
        var s: Double
        var lch/*(l, c, h)*/ = LuvLChWhiteRef(wref = hSLuvD65)
        return LuvLChToHPLuv(l = lch.l, c = lch.c, h = lch.h)
    }

    // DistanceHPLuv calculates Euclidean distance in the HPLuv colorspace. No idea
    // how useful this is.

    // The Hue value is divided by 100 before the calculation, so that H, S, and L
    // have the same relative ranges.
    public fun DistanceHPLuv(c2: Color): Double {
        val (h1, s1, l1) = HPLuv()
        val (h2, s2, l2) = c2.HPLuv()
        return sqrt(sq((h1 - h2) / 100.0) + sq(s1 - s2) + sq(l1 - l2))
    }


    override fun toString(): String {
        return "R: $R, G: $G B: $B"
    }



    override fun compareTo(other: Color): Int {
        return when {
            this.R != other.R -> this.R.compareTo(other.R)
            this.G != other.G -> this.G.compareTo(other.G)
            else -> this.B.compareTo(other.B)
        }
    }

    companion object {
        // Utility used by Hxx color-spaces for interpolating between two angles in [0,360].
        private fun interp_angle(a0: Double, a1: Double, t: Double): Double {
            // Based on the answer here: http://stackoverflow.com/a/14498790/2366315
            // With potential proof that it works here: http://math.stackexchange.com/a/2144499
            val delta = ((a1 - a0).mod(360.0) + 540).mod(360.0) - 180.0
            return (a0 + t * delta + 360.0).mod(360.0)
        }


        // Hsv creates a new Color given a Hue in [0..360], a Saturation and a Value in [0..1]
        public fun Hsv(H: Double, S: Double, V: Double): Color {
            val Hp = H / 60.0
            val C = V * S
            val X = C * (1.0 - abs(Hp.mod(2.0) - 1.0))

            val m = V - C
            var r = 0.0
            var g = 0.0
            var b = 0.0

            if (Hp >= 0.0 && Hp < 1.0) {
                r = C
                g = X
            } else if (Hp >= 1.0 && Hp < 2.0) {
                r = X
                g = C
            } else if (Hp >= 2.0 && Hp < 3.0) {
                g = C
                b = X
            } else if (Hp >= 3.0 && Hp < 4.0) {
                g = X
                b = C
            } else if (Hp >= 4.0 && Hp < 5.0) {
                r = X
                b = C
            } else if (Hp >= 5.0 && Hp < 6.0) {
                r = C
                b = X
            }

            return Color(R = m + r, G = m + g, B = m + b)
        }

        // Hsl creates a new Color given a Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1]
        public fun Hsl(h: Double, s: Double, l: Double): Color {
            if (s == 0.0) {
                return Color(R = l, G = l, B = l)
            }

            var r: Double
            var g: Double
            var b: Double
            var t1: Double
            var t2: Double
            var tr: Double
            var tg: Double
            var tb: Double

            if (l < 0.5) {
                t1 = l * (1.0 + s)
            } else {
                t1 = l + s - l * s
            }

            t2 = 2 * l - t1
            val h = h / 360
            tr = h + 1.0 / 3.0
            tg = h
            tb = h - 1.0 / 3.0

            if (tr < 0) {
                tr += 1
            }
            if (tr > 1) {
                tr -= 1
            }
            if (tg < 0) {
                tg += 1
            }
            if (tg > 1) {
                tg -= 1
            }
            if (tb < 0) {
                tb += 1
            }
            if (tb > 1) {
                tb -= 1
            }

            // Red
            if (6 * tr < 1) {
                r = t2 + (t1 - t2) * 6 * tr
            } else if (2 * tr < 1) {
                r = t1
            } else if (3 * tr < 2) {
                r = t2 + (t1 - t2) * (2.0 / 3.0 - tr) * 6
            } else {
                r = t2
            }

            // Green
            if (6 * tg < 1) {
                g = t2 + (t1 - t2) * 6 * tg
            } else if (2 * tg < 1) {
                g = t1
            } else if (3 * tg < 2) {
                g = t2 + (t1 - t2) * (2.0 / 3.0 - tg) * 6
            } else {
                g = t2
            }

            // Blue
            if (6 * tb < 1) {
                b = t2 + (t1 - t2) * 6 * tb
            } else if (2 * tb < 1) {
                b = t1
            } else if (3 * tb < 2) {
                b = t2 + (t1 - t2) * (2.0 / 3.0 - tb) * 6
            } else {
                b = t2
            }

            return Color(R = r, G = g, B = b)
        }

        // Hex parses a "html" hex color-string, either in the 3 "#f0c" or 6 "#ff1034" digits form.
        public fun Hex(colorString: String): Color? {
            val scol = if (colorString.startsWith("#")) colorString.substring(1) else colorString

            if (scol.length == 3) {
                val r = scol.substring(0, 1).toInt(16) / 255.0
                val g = scol.substring(1, 2).toInt(16) / 255.0
                val b = scol.substring(2, 3).toInt(16) / 255.0
                return Color(R = r, G = g, B = b)
            } else if (scol.length == 6) {
                val r = scol.substring(0, 2).toInt(16) / 255.0
                val g = scol.substring(2, 4).toInt(16) / 255.0
                val b = scol.substring(4, 6).toInt(16) / 255.0
                return Color(R = r, G = g, B = b)
            }

            return null
        }

        /// Linear ///
        //////////////
        // http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/
        // http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html

        public fun linearize(v: Double): Double {
            if (v <= 0.04045) {
                return v / 12.92
            }
            return ((v + 0.055) / 1.055).pow(2.4)
        }

        // A much faster and still quite precise linearization using a 6th-order Taylor approximation.
        // See the accompanying Jupyter notebook for derivation of the constants.
        public fun linearize_fast(v: Double): Double {
            val v1 = v - 0.5
            val v2 = v1 * v1
            val v3 = v2 * v1
            val v4 = v2 * v2
            // v5 := v3*v2
            return -0.248750514614486 + 0.925583310193438 * v + 1.16740237321695 * v2 + 0.280457026598666 * v3 - 0.0757991963780179 * v4 // + 0.0437040411548932*v5
        }

        public fun delinearize(v: Double): Double {
            if (v <= 0.0031308) {
                return 12.92 * v
            }
            return 1.055 * v.pow(1.0 / 2.4) - 0.055
        }

        // LinearRgb creates an sRGB color out of the given linear RGB color (see http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/).
        public fun LinearRgb(r: Double, g: Double, b: Double): Color {
            return Color(R = delinearize(r), G = delinearize(g), B = delinearize(b))
        }

        public fun delinearize_fast(v: Double): Double {
            var v1: Double
            var v2: Double
            var v3: Double
            var v4: Double
            var v5: Double
            // This funtion (fractional root) is much harder to linearize, so we need to split.
            if (v > 0.2) {
                v1 = v - 0.6
                v2 = v1 * v1
                v3 = v2 * v1
                v4 = v2 * v2
                v5 = v3 * v2
                return 0.442430344268235 + 0.592178981271708 * v - 0.287864782562636 * v2 + 0.253214392068985 * v3 - 0.272557158129811 * v4 + 0.325554383321718 * v5
            } else if (v > 0.03) {
                v1 = v - 0.115
                v2 = v1 * v1
                v3 = v2 * v1
                v4 = v2 * v2
                v5 = v3 * v2
                return 0.194915592891669 + 1.55227076330229 * v - 3.93691860257828 * v2 + 18.0679839248761 * v3 - 101.468750302746 * v4 + 632.341487393927 * v5
            } else {
                v1 = v - 0.015
                v2 = v1 * v1
                v3 = v2 * v1
                v4 = v2 * v2
                v5 = v3 * v2
                // You can clearly see from the involved constants that the low-end is highly nonlinear.
                return 0.0519565234928877 + 5.09316778537561 * v - 99.0338180489702 * v2 + 3484.52322764895 * v3 - 150_028.083412663 * v4 + 7_168_008.42971613 * v5
            }
        }

        // FastLinearRgb is much faster than and almost as accurate as LinearRgb.
        // BUT it is important to NOTE that they only produce good results for valid inputs r,g,b in [0,1].
        public fun FastLinearRgb(r: Double, g: Double, b: Double): Color {
            return Color(R = delinearize_fast(r), G = delinearize_fast(g), B = delinearize_fast(b))
        }

        // XyzToLinearRgb converts from CIE XYZ-space to Linear RGB space.
        public fun XyzToLinearRgb(x: Double, y: Double, z: Double): RGB {
            val r = 3.2409699419045214 * x - 1.5373831775700935 * y - 0.49861076029300328 * z
            val g = -0.96924363628087983 * x + 1.8759675015077207 * y + 0.041555057407175613 * z
            val b = 0.055630079696993609 * x - 0.20397695888897657 * y + 1.0569715142428786 * z
            return RGB(r, g, b)
        }

        public fun LinearRgbToXyz(r: Double, g: Double, b: Double): XYZ {
            val x = 0.41239079926595948 * r + 0.35758433938387796 * g + 0.18048078840183429 * b
            val y = 0.21263900587151036 * r + 0.71516867876775593 * g + 0.072192315360733715 * b
            val z = 0.019330818715591851 * r + 0.11919477979462599 * g + 0.95053215224966058 * b
            return XYZ(x, y, z)
        }

        public fun Xyz(x: Double, y: Double, z: Double): Color {
            val (r, g, b) = XyzToLinearRgb(x = x, y = y, z = z)
            return LinearRgb(r = r, g = g, b = b)
        }

        /// xyY ///
        ///////////
        // http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html

        // Well, the name is bad, since it's xyY but Golang needs me to start with a
        // capital letter to make the method public.
        public fun XyzToXyy(X: Double, Y: Double, Z: Double): XYYout {
            return XyzToXyyWhiteRef(X = X, Y = Y, Z = Z, wref = D65)
        }

        public fun XyzToXyyWhiteRef(X: Double, Y: Double, Z: Double, wref: WhiteReference): XYYout {
            var x: Double
            var y: Double
            val Yout = Y
            val N = X + Y + Z
            if (abs(N) < 1e-14) {
                // When we have black, Bruce Lindbloom recommends to use
                // the reference white's chromacity for x and y.
                x = wref.wref[0] / (wref.wref[0] + wref.wref[1] + wref.wref[2])
                y = wref.wref[1] / (wref.wref[0] + wref.wref[1] + wref.wref[2])
            } else {
                x = X / N
                y = Y / N
            }
            return XYYout(x, y, Yout)
        }

        public fun XyyToXyz(x: Double, y: Double, Y: Double): XYoutZ {
            val X: Double
            var Z: Double
            val Yout = Y

            if (y > -1e-14 && y < 1e-14) {
                X = 0.0
                Z = 0.0
            } else {
                X = Y / y * x
                Z = Y / y * (1.0 - x - y)
            }

            return XYoutZ(X, Yout, Z)
        }


        /// L*a*b* ///
        //////////////
        // http://en.wikipedia.org/wiki/Lab_color_space#CIELAB-CIEXYZ_conversions
        // For L*a*b*, we need to L*a*b*<->XYZ->RGB and the first one is device dependent.

        public fun lab_f(t: Double): Double {
            if (t > 6.0 / 29.0 * 6.0 / 29.0 * 6.0 / 29.0) {
                return cbrt(t)
            }
            return t / 3.0 * 29.0 / 6.0 * 29.0 / 6.0 + 4.0 / 29.0
        }

        public fun XyzToLab(x: Double, y: Double, z: Double): LAB {
            // Use D65 white as reference point by default.
            // http://www.fredmiranda.com/forum/topic/1035332
            // http://en.wikipedia.org/wiki/Standard_illuminant
            return XyzToLabWhiteRef(x = x, y = y, z = z, wref = D65)
        }

        public fun XyzToLabWhiteRef(x: Double, y: Double, z: Double, wref: WhiteReference): LAB {
            val fy = lab_f(y / wref.wref[1])
            val l = 1.16 * fy - 0.16
            val a = 5.0 * (lab_f(x / wref.wref[0]) - fy)
            val b = 2.0 * (fy - lab_f(z / wref.wref[2]))
            return LAB(l, a, b)
        }

        public fun lab_finv(t: Double): Double {
            if (t > 6.0 / 29.0) {
                return t * t * t
            }
            return 3.0 * 6.0 / 29.0 * 6.0 / 29.0 * (t - 4.0 / 29.0)
        }

        public fun LabToXyz(l: Double, a: Double, b: Double): XYZ {
            // D65 white (see above).
            return LabToXyzWhiteRef(l = l, a = a, b = b, wref = D65)
        }

        public fun LabToXyzWhiteRef(l: Double, a: Double, b: Double, wref: WhiteReference): XYZ {
            val l2 = (l + 0.16) / 1.16
            val x = wref.wref[0] * lab_finv(l2 + a / 5.0)
            val y = wref.wref[1] * lab_finv(l2)
            val z = wref.wref[2] * lab_finv(l2 - b / 2.0)
            return XYZ(x, y, z)
        }

        // Generates a color by using data given in CIE xyY space.
        // x, y and Y are in [0..1]
        public fun Xyy(x: Double, y: Double, Y: Double): Color {
            val (X, Yout, Z) = XyyToXyz(x = x, y = y, Y = Y)
            return Xyz(x = X, y = Yout, z = Z)
        }

        // Generates a color by using data given in CIE L*a*b* space using D65 as reference white.
        // WARNING: many combinations of `l`, `a`, and `b` values do not have corresponding
        // valid RGB values, check the FAQ in the README if (you're unsure.
        public fun Lab(l: Double, a: Double, b: Double): Color {
            val (x, y, z) = LabToXyz(l = l, a = a, b = b)
            return Xyz(x = x, y = y, z = z)
        }

        // Generates a color by using data given in CIE L*a*b* space, taking
        // into account a given reference white. (i.e. the monitor's white)
        public fun LabWhiteRef(l: Double, a: Double, b: Double, wref: WhiteReference): Color {
            val (x, y, z) = LabToXyzWhiteRef(l = l, a = a, b = b, wref = wref)
            return Xyz(x = x, y = y, z = z)
        }

        public  fun LuvLChToLuv(l: Double, c: Double, h: Double): LUV {
            val H = 0.01745329251994329576 * h // Deg2Rad
            val u = c * cos(H)
            val v = c * sin(H)
            val L = l
            return LUV(L, u, v)
        }

        // Generates a color by using data given in LuvLCh space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // h values are in [0..360], C and L values are in [0..1]
        public fun LuvLChWhiteRef(l: Double, c: Double, h: Double, wref: WhiteReference): Color {
            val (L, u, v) = LuvLChToLuv(l = l, c = c, h = h)
            return LuvWhiteRef(l = L, u = u, v = v, wref = wref)
        }

        // Generates a color by using data given in CIE L*u*v* space using D65 as reference white.
        // L* is in [0..1] and both u* and v* are in about [-1..1]
        // WARNING: many combinations of `l`, `u`, and `v` values do not have corresponding
        // valid RGB values, check the FAQ in the README if (you're unsure.
        public fun Luv(l: Double, u: Double, v: Double): Color {
            val (x, y, z) = LuvToXyz(l = l, u = u, v = v)
            return Xyz(x = x, y = y, z = z)
        }

        // Generates a color by using data given in CIE L*u*v* space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // L* is in [0..1] and both u* and v* are in about [-1..1]
        public fun LuvWhiteRef(l: Double, u: Double, v: Double, wref: WhiteReference): Color {
            val (x, y, z) = LuvToXyzWhiteRef(l = l, u = u, v = v, wref = wref)
            return Xyz(x = x, y = y, z = z)
        }

        public fun LabToHcl(L: Double, a: Double, b: Double): HCL {
            val h: Double
            // Oops, floating point workaround necessary if (a ~= b and both are very small (i.e. almost zero).
            if (abs(b - a) > 1e-4 && abs(a) > 1e-4) {
                h = (57.29577951308232087721 * atan2(b, a) + 360.0).mod(360.0) // Rad2Deg
            } else {
                h = 0.0
            }
            val c = sqrt(sq(a) + sq(b))
            val l = L
            return HCL(h, c, l)
        }

        // Generates a color by using data given in HCL space using D65 as reference white.
        // H values are in [0..360], C and L values are in [0..1]
        // WARNING: many combinations of `h`, `c`, and `l` values do not have corresponding
        // valid RGB values, check the FAQ in the README if (you're unsure.
        public  fun Hcl(h: Double, c: Double, l: Double): Color {
            return HclWhiteRef(h = h, c = c, l = l, wref = D65)
        }

        public fun HclToLab(h: Double, c: Double, l: Double): LAB {
            val H = 0.01745329251994329576 * h // Deg2Rad
            val a = c * cos(H)
            val b = c * sin(H)
            val L = l
            return LAB(L, a, b)
        }

        // Generates a color by using data given in HCL space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // H values are in [0..360], C and L values are in [0..1]
        public fun HclWhiteRef(h: Double, c: Double, l: Double, wref: WhiteReference): Color {
            val (L, a, b) = HclToLab(h = h, c = c, l = l)
            return LabWhiteRef(l = L, a = a, b = b, wref = wref)
        }

        public fun LuvToLuvLCh(L: Double, u: Double, v: Double): LCH {
            var l: Double
            var c: Double
            var h: Double
            // Oops, floating point workaround necessary if (u ~= v and both are very small (i.e. almost zero).
            if (abs(v - u) > 1e-4 && abs(u) > 1e-4) {
                h = (57.29577951308232087721 * atan2(v, u) + 360.0).mod(360.0) // Rad2Deg
            } else {
                h = 0.0
            }
            l = L
            c = sqrt(sq(u) + sq(v))
            return LCH(l, c, h)
        }

        public fun LuvToXyz(l: Double, u: Double, v: Double): XYZ {
            // D65 white (see above).
            return LuvToXyzWhiteRef(l = l, u = u, v = v, wref = D65)
        }

        public fun LuvToXyzWhiteRef(l: Double, u: Double, v: Double, wref: WhiteReference): XYZ {
            var x: Double
            var y: Double
            var z: Double
            z = 0.0

            // y = wref[1] * lab_finv((l + 0.16) / 1.16)
            if (l <= 0.08) {
                y = wref.wref[1] * l * 100.0 * 3.0 / 29.0 * 3.0 / 29.0 * 3.0 / 29.0
            } else {
                y = wref.wref[1] * cub((l + 0.16) / 1.16)
            }

            val (un, vn) = xyz_to_uv(x = wref.wref[0], y = wref.wref[1], z = wref.wref[2])
            if (l != 0.0) {
                val ubis = u / (13.0 * l) + un
                val vbis = v / (13.0 * l) + vn
                x = y * 9.0 * ubis / (4.0 * vbis)
                z = y * (12.0 - 3.0 * ubis - 20.0 * vbis) / (4.0 * vbis)
            } else {
                x = 0.0
                y = 0.0
            }

            return XYZ(x, y, z)
        }


        /// L*u*v* ///
        //////////////
        // http://en.wikipedia.org/wiki/CIELUV#XYZ_.E2.86.92_CIELUV_and_CIELUV_.E2.86.92_XYZ_conversions
        // For L*u*v*, we need to L*u*v*<->XYZ<->RGB and the first one is device dependent.

        public fun XyzToLuv(x: Double, y: Double, z: Double): LAB {
            // Use D65 white as reference point by default.
            // http://www.fredmiranda.com/forum/topic/1035332
            // http://en.wikipedia.org/wiki/Standard_illuminant
            val (l, u, v) = XyzToLuvWhiteRef(x = x, y = y, z = z, wref = D65)
            return LAB(l, u, v)
        }

        public fun XyzToLuvWhiteRef(x: Double, y: Double, z: Double, wref: WhiteReference): LUV {
            var l: Double
            var u: Double
            var v: Double
            if (y / wref.wref[1] <= 6.0 / 29.0 * 6.0 / 29.0 * 6.0 / 29.0) {
                l = y / wref.wref[1] * (29.0 / 3.0 * 29.0 / 3.0 * 29.0 / 3.0) / 100.0
            } else {
                l = 1.16 * cbrt(y / wref.wref[1]) - 0.16
            }
            val (ubis, vbis) = xyz_to_uv(x = x, y = y, z = z)
            val (un, vn) = xyz_to_uv(x = wref.wref[0], y = wref.wref[1], z = wref.wref[2])
            u = 13.0 * l * (ubis - un)
            v = 13.0 * l * (vbis - vn)
            return LUV(l, u, v)
        }

        // For this part, we do as R's graphics.hcl does, not as wikipedia does.
        // Or is it the same?
        public fun xyz_to_uv(x: Double, y: Double, z: Double): UV {
            var u: Double
            var v: Double
            val denom = x + 15.0 * y + 3.0 * z
            if (denom == 0.0) {
                u = 0.0
                v = 0.0
            } else {
                u = 4.0 * x / denom
                v = 9.0 * y / denom
            }
            return UV(u, v)
        }





        public fun LuvLChToHSLuv(l: Double, c: Double, h: Double): HSL {
            // [-1..1] but the code expects it to be [-100..100]
            val c: Double = c * 100.0
            val l: Double = l * 100.0

            var s: Double
            var max: Double
            if (l > 99.9999999 || l < 0.00000001) {
                s = 0.0
            } else {
                max = maxChromaForLH(l = l, h = h)
                s = c / max * 100.0
            }
            return HSL(h, clamp01(s / 100.0), clamp01(l / 100.0))
        }

        public fun HSLuvToLuvLCh(h: Double, s: Double, l: Double): LCH {
            val l: Double = l * 100.0
            val s: Double = s * 100.0

            var c: Double
            var max: Double
            if (l > 99.9999999 || l < 0.00000001) {
                c = 0.0
            } else {
                max = Color.maxChromaForLH(l = l, h = h)
                c = max / 100.0 * s
            }

            // c is [-100..100], but for LCh it's supposed to be almost [-1..1]
            return LCH(clamp01(l / 100.0), c / 100.0, h)
        }

        public fun LuvLChToHPLuv(l: Double, c: Double, h: Double): HSL {
            // [-1..1] but the code expects it to be [-100..100]
            val c: Double = c * 100.0
            val l: Double = l * 100.0

            var s: Double
            var max: Double
            if (l > 99.9999999 || l < 0.00000001) {
                s = 0.0
            } else {
                max = maxSafeChromaForL(l)
                s = c / max * 100.0
            }
            return HSL(h, s / 100.0, l / 100.0)
        }

        public fun HPLuvToLuvLCh(h: Double, s: Double, l: Double): LCH {
            // [-1..1] but the code expects it to be [-100..100]
            val l = l * 100.0
            val s = s * 100.0

            var c: Double
            var max: Double
            if (l > 99.9999999 || l < 0.00000001) {
                c = 0.0
            } else {
                max = maxSafeChromaForL(l)
                c = max / 100.0 * s
            }
            return LCH(l / 100.0, c / 100.0, h)
        }


        // HSLuv creates a new Color from values in the HSLuv color space.
        // Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1].
        //
        // The returned color values are clamped (using .Clamped), so this will never output
        // an invalid color.
        public fun HSLuv(h: Double, s: Double, l: Double): Color {
            // HSLuv -> LuvLCh -> CIELUV -> CIEXYZ -> Linear RGB -> sRGB
            var u: Double
            var v: Double
            var lch/*(l, c, h)*/ = HSLuvToLuvLCh(h = h, s = s, l = l)
            var luv/*(l, u, v)*/ = LuvLChToLuv(l = lch.l, c = lch.c, h = lch.h)
            val (x, y, z) = LuvToXyzWhiteRef(l = luv.l, u = luv.u, v = luv.v, wref = hSLuvD65)
            val (r, g, b) = XyzToLinearRgb(x = x, y = y, z = z)
            return LinearRgb(r = r, g = g, b = b).Clamped()
        }

        // HPLuv creates a new Color from values in the HPLuv color space.
        // Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1].
        //
        // The returned color values are clamped (using .Clamped), so this will never output
        // an invalid color.
        public fun HPLuv(h: Double, s: Double, l: Double): Color {
            // HPLuv -> LuvLCh -> CIELUV -> CIEXYZ -> Linear RGB -> sRGB
            var u: Double
            var v: Double
            var lch /*(l, c, h)*/ = HPLuvToLuvLCh(h = h, s = s, l = l)
            var luv /*(l, u, v)*/ = LuvLChToLuv(l = lch.l, c = lch.c, h = lch.h)
            val (x, y, z) = LuvToXyzWhiteRef(l = luv.l, u = luv.u, v = luv.v, wref = hSLuvD65)
            val (r, g, b) = XyzToLinearRgb(x = x, y = y, z = z)
            return LinearRgb(r = r, g = g, b = b).Clamped()
        }

        private var m = arrayOf(
            arrayOf(3.2409699419045214, -1.5373831775700935, -0.49861076029300328),
            arrayOf(-0.96924363628087983, 1.8759675015077207, 0.041555057407175613),
            arrayOf(0.055630079696993609, -0.20397695888897657, 1.0569715142428786),
        )

        private val kappa = 903.2962962962963
        private val epsilon = 0.0088564516790356308

        private fun maxChromaForLH(l: Double, h: Double): Double {
            val hRad = h / 360.0 * PI * 2.0
            var minLength = Double.MAX_VALUE
            for (line in getBounds(l)) {
                val length = lengthOfRayUntilIntersect(theta = hRad, x = line[0], y = line[1])
                if (length > 0.0 && length < minLength) {
                minLength = length
            }
            }
            return minLength
        }

        private fun getBounds(l: Double): Array<Array<Double>> {
            var sub2: Double
            var ret = (1..6).map { (1..2).map { 0.0 }.toTypedArray() }.toTypedArray()
            val sub1 = (l + 16.0).pow(3.0) / 1_560_896.0
            if (sub1 > epsilon) {
                sub2 = sub1
            } else {
                sub2 = l / kappa
            }
            for (i in m.indices) {
                for (k in 0..<2) {
                    val top1: Double = (284_517.0 * m[i][0] - 94839.0 * m[i][2]) * sub2
                    val top2: Double = (838_422.0 * m[i][2] + 769_860.0 * m[i][1] + 731_718.0 * m[i][0]) * l * sub2 - 769_860.0 * k.toDouble() * l
                    val bottom: Double = (632_260.0 * m[i][2] - 126_452.0 * m[i][1]) * sub2 + 126_452.0 * k.toDouble()
                    ret[i * 2 + k][0] = top1 / bottom
                    ret[i * 2 + k][1] = top2 / bottom
                }
            }
            return ret
        }

        private fun lengthOfRayUntilIntersect(theta: Double, x: Double, y: Double): Double {
            return y / (sin(theta) - x * cos(theta))
        }

        private fun maxSafeChromaForL(l: Double): Double {
            var minLength = Double.MAX_VALUE
            for (line in getBounds(l)) {
                val m1 = line[0]
                val b1 = line[1]
                val x = intersectLineLine(x1 = m1, y1 = b1, x2 = -1.0 / m1, y2 = 0.0)
                val dist = distanceFromPole(x = x, y = b1 + x * m1)
                if (dist < minLength) {
                    minLength = dist
                }
            }
            return minLength
        }

        private fun intersectLineLine(x1: Double, y1: Double, x2: Double, y2: Double): Double {
            return (y1 - y2) / (x2 - x1)
        }

        private fun distanceFromPole(x: Double, y: Double): Double {
            return sqrt(x.pow(2.0) + y.pow(2.0))
        }

    }
}
