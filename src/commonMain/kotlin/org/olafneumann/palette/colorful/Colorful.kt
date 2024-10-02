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

data class WhiteReference(var whiteReference: DoubleArray = doubleArrayOf(0.0, 0.0, 0.0)) {
    constructor(x: Double, y: Double, z: Double) : this(doubleArrayOf(x, y, z))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class.js != other::class.js) return false

        other as WhiteReference

        return whiteReference.contentEquals(other.whiteReference)
    }

    override fun hashCode(): Int {
        return whiteReference.contentHashCode()
    }
}

private fun sq(value: Double): Double {
    return value * value
}

private fun cub(value: Double): Double {
    return value * value * value
}

// clamp01 clamps from 0 to 1.
private fun clamp01(value: Double): Double {
    return max(0.0, min(value, 1.0))
}

// This is the tolerance used when comparing colors using AlmostEqualRgb.
private const val Delta: Double = 1.0 / 255.0

// This is the default reference white point.
val D65: WhiteReference = WhiteReference(x = 0.95047, y = 1.00000, z = 1.08883)

// And another one.
@Suppress("Unused")
val D50: WhiteReference = WhiteReference(x = 0.96422, y = 1.00000, z = 0.82521)

var hSLuvD65: WhiteReference = WhiteReference(x = 0.95045592705167, y = 1.0, z = 1.089057750759878)

@Suppress("Unused", "MemberVisibilityCanBePrivate", "DuplicateCodeFragment")
data class Color(var r: Double = 0.0, var g: Double = 0.0, var b: Double = 0.0) : Comparable<Color> {

    fun RGBA(): RGBA {
        val r = (r * 65535.0 + 0.5).toInt()
        val g = (g * 65535.0 + 0.5).toInt()
        val b = (b * 65535.0 + 0.5).toInt()
        val a = 0xFFFF
        return RGBA(r, g, b, a)
    }

    // Might come in handy sometimes to reduce boilerplate code.
    fun RGB255(): RGB255 {
        val r = ((r * 255.0) + 0.5).toInt()
        val g = ((g * 255.0) + 0.5).toInt()
        val b = ((b * 255.0) + 0.5).toInt()
        return RGB255(r, g, b)
    }

    // Used to simplify HSLuv testing.
    fun values(): RGB {
        return RGB(r, g, b)
    }

    // Checks whether the color exists in RGB space, i.e. all values are in [0..1]
    fun isValid(): Boolean {
        return r in 0.0..1.0 && g in 0.0..1.0 && b in 0.0..1.0
    }

    // Returns Clamps the color into valid range, clamping each value to [0..1]
    // If the color is valid already, this is a no-op.
    fun clamped(): Color {
        return Color(r = clamp01(r), g = clamp01(g), b = clamp01(b))
    }

    // DistanceRgb computes the distance between two colors in RGB space.
    // This is not a good measure! Rather do it in Lab space.
    fun distanceRgb(color2: Color): Double {
        return sqrt(sq(r - color2.r) + sq(g - color2.g) + sq(b - color2.b))
    }

    // DistanceLinearRgb computes the distance between two colors in linear RGB
    // space. This is not useful for measuring how humans perceive color, but
    // might be useful for other things, like dithering.
    fun distanceLinearRgb(color2: Color): Double {
        val (r1, g1, b1) = linearRgb()
        val (r2, g2, b2) = color2.linearRgb()
        return sqrt(sq(r1 - r2) + sq(g1 - g2) + sq(b1 - b2))
    }


    // DistanceLinearRGB is deprecated in favour of DistanceLinearRgb.
    // They do the exact same thing.
    fun distanceLinearRGB(color2: Color): Double {
        return distanceLinearRgb(color2)
    }

    // DistanceRiemersma is a color distance algorithm developed by Thiadmer Riemersma.
    // It uses RGB coordinates, but he claims it has similar results to CIELUV.
    // This makes it both fast and accurate.
    //
    // Sources:
    //
    //     https://www.compuphase.com/cmetric.htm
    //     https://github.com/lucasb-eyer/go-colorful/issues/52
    fun distanceRiemersma(color2: Color): Double {
        val rAvg: Double = (this.r + color2.r) / 2.0
        // Deltas
        val dR: Double = this.r - color2.r
        val dG: Double = this.g - color2.g
        val dB: Double = this.b - color2.b

        return sqrt((2 + rAvg) * dR * dR + 4 * dG * dG + (2 + (1 - rAvg)) * dB * dB)
    }

    // Check for equality between colors within the tolerance Delta (1/255).
    fun almostEqualRgb(color2: Color): Boolean {
        return abs(this.r - color2.r) +
                abs(this.g - color2.g) +
                abs(this.b - color2.b) < (3.0 * Delta)
    }

    // You don't really want to use this, do you? Go for BlendLab, BlendLuv or BlendHcl.
    fun blendRgb(color2: Color, t: Double): Color {
        return Color(r = this.r + t * (color2.r - this.r), g = this.g + t * (color2.g - this.g), b = this.b + t * (color2.b - this.b))
    }


    /// HSV ///
    ///////////
    // From http://en.wikipedia.org/wiki/HSL_and_HSV
    // Note that h is in [0..360] and s,v in [0..1]

    // Hsv returns the Hue [0..360], Saturation and Value [0..1] of the color.
    fun hsv(): HSV {
        val min = min(min(this.r, this.g), this.b)
        val v = max(max(this.r, this.g), this.b)
        val c = v - min

        var s = 0.0
        if (v != 0.0) {
            s = c / v
        }

        var h = 0.0 // We use 0 instead of undefined as in wp.
        if (min != v) {
            if (v == this.r) {
                h = ((this.g - this.b) / c).mod(6.0)
            }
            if (v == this.g) {
                h = (this.b - this.r) / c + 2.0
            }
            if (v == this.b) {
                h = (this.r - this.g) / c + 4.0
            }
            h *= 60.0
            if (h < 0.0) {
                h += 360.0
            }
        }
        return HSV(h, s, v)
    }


    // You don't really want to use this, do you? Go for BlendLab, BlendLuv or BlendHcl.
    fun blendHsv(c2: Color, t: Double): Color {
        var (h1, s1, v1) = this.hsv()
        var (h2, s2, v2) = c2.hsv()

        // https://github.com/lucasb-eyer/go-colorful/pull/60
        if (s1 == 0.0 && s2 != 0.0) {
            h1 = h2
        } else if (s2 == 0.0 && s1 != 0.0) {
            h2 = h1
        }

        // We know that h are both in [0..360]
        return hsv(h = interpolateAngle(a0 = h1, a1 = h2, t = t), s = s1 + t * (s2 - s1), v = v1 + t * (v2 - v1))
    }

    /// HSL ///
    ///////////

    // Hsl returns the Hue [0..360], Saturation [0..1], and Luminance (lightness) [0..1] of the color.
    fun hsl(): HSL {
        val min: Double = min(min(this.r, this.g), this.b)
        val max: Double = max(max(this.r, this.g), this.b)

        val l: Double = (max + min) / 2

        val s: Double
        var h: Double

        if (min == max) {
            s = 0.0
            h = 0.0
        } else {
            s = if (l < 0.5) {
                (max - min) / (max + min)
            } else {
                (max - min) / (2.0 - max - min)
            }

            h = when (max) {
                this.r -> {
                    (this.g - this.b) / (max - min)
                }
                this.g -> {
                    2.0 + (this.b - this.r) / (max - min)
                }
                else -> {
                    4.0 + (this.r - this.g) / (max - min)
                }
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
    fun hex(): String = "#${r.asHexByte()}${g.asHexByte()}${b.asHexByte()}"

    // Add 0.5 for rounding
    private fun Double.asRoundedByte(): Int = (this * 255.0 + 0.5).toInt()
    @OptIn(ExperimentalStdlibApi::class)
    private fun Int.asHex(): String = this.toByte().toHexString(format = HexFormat.UpperCase).padStart(length = 2, padChar = '0')
    private fun Double.asHexByte(): String = asRoundedByte().asHex()

    // LinearRgb converts the color into the linear RGB space (see http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/).
    fun linearRgb(): RGB {
        val r = linearize(this.r)
        val g = linearize(this.g)
        val b = linearize(this.b)
        return RGB(r, g, b)
    }


    // FastLinearRgb is much faster than and almost as accurate as LinearRgb.
    // BUT it is important to NOTE that they only produce good results for valid colors r,g,b in [0,1].
    fun fastLinearRgb(): RGB {
        val r = linearizeFast(this.r)
        val g = linearizeFast(this.g)
        val b = linearizeFast(this.b)
        return RGB(r, g, b)
    }

    // BlendLinearRgb blends two colors in the Linear RGB color-space.
    // Unlike BlendRgb, this will not produce dark color around the center.
    // t == 0 results in c1, t == 1 results in c2
    fun blendLinearRgb(c2: Color, t: Double): Color {
        val (r1, g1, b1) = this.linearRgb()
        val (r2, g2, b2) = c2.linearRgb()
        return linearRgb(
            r = r1 + t * (r2 - r1),
            g = g1 + t * (g2 - g1),
            b = b1 + t * (b2 - b1)
        )
    }

    /// XYZ ///
    ///////////
    // http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/

    fun xyz(): XYZ {
        val (r, g, b) = this.linearRgb()
        return linearRgbToXyz(r = r, g = g, b = b)
    }

    // Converts the given color to CIE xyY space using D65 as reference white.
    // (Note that the reference white is only used for black input.)
    // x, y and Y are in [0..1]
    fun xyy(): XYYout {
        val xyz = this.xyz()
        val (x, y, yOut) = xyzToXyy(x = xyz.x, y = xyz.y, z = xyz.z)
        return XYYout(x, y, yOut)
    }

    // Converts the given color to CIE xyY space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // (Note that the reference white is only used for black input.)
    // x, y and Y are in [0..1]
    fun xyyWhiteRef(whiteReference: WhiteReference): XYYout {
        val xyz = this.xyz()
        val (x, y, yOut) = xyzToXyyWhiteRef(x = xyz.x, y = xyz.y, z = xyz.z, whiteReference = whiteReference)
        return XYYout(x, y, yOut)
    }

    // Converts the given color to CIE L*u*v* space using D65 as reference white.
    // L* is in [0..1] and both u* and v* are in about [-1..1]
    fun luv(): LUV {
        val (x, y, z) = this.xyz()
        val (l, u, v) = xyzToLuv(x = x, y = y, z = z)
        return LUV(l, u, v)
    }

    // Converts the given color to CIE L*u*v* space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // L* is in [0..1] and both u* and v* are in about [-1..1]
    fun luvWhiteRef(whiteReference: WhiteReference): LUV {
        val (x, y, z) = this.xyz()
        val (l, u, v) = xyzToLuvWhiteRef(x = x, y = y, z = z, whiteReference = whiteReference)
        return LUV(l, u, v)
    }

    // Converts the given color to CIE L*a*b* space using D65 as reference white.
    fun lab(): LAB {
        val (x, y, z) = this.xyz()
        return xyzToLab(x = x, y = y, z = z)
    }

    // Converts the given color to CIE L*a*b* space, taking into account
    // a given reference white. (i.e. the monitor's white)
    fun labWhiteRef(whiteReference: WhiteReference): LAB {
        val (x, y, z) = this.xyz()
        return xyzToLabWhiteReference(x= x, y = y, z = z, whiteReference = whiteReference)
    }

    // DistanceLab is a good measure of visual similarity between two colors!
    // A result of 0 would mean identical colors, while a result of 1 or higher
    // means the colors differ a lot.
    fun distanceLab(c2: Color): Double {
        val (l1, a1, b1) = lab()
        val (l2, a2, b2) = c2.lab()
        return sqrt(sq(l1 - l2) + sq(a1 - a2) + sq(b1 - b2))
    }

    // DistanceCIE76 is the same as DistanceLab.
    fun distanceCIE76(c2: Color): Double {
        return this.distanceLab(c2)
    }

    // Uses the CIE94 formula to calculate color distance. More accurate than
    // DistanceLab, but also more work.
    fun distanceCIE94(cr: Color): Double {
        var (l1, a1, b1) = this.lab()
        var (l2, a2, b2) = cr.lab()

        // NOTE: Since all those formulas expect L,a,b values 100x larger than we
        //       have them in this library, we either need to adjust all constants
        //       in the formula, or convert the ranges of L,a,b before, and then
        //       scale the distances down again. The latter is less error-prone.
        l1 *= 100.0
        a1 *= 100.0
        b1 *= 100.0
        l2 *= 100.0
        a2 *= 100.0
        b2 *= 100.0

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
    fun distanceCIEDE2000(cr: Color): Double {
        return this.distanceCIEDE2000klch(cr = cr, kl = 1.0, kc = 1.0, kh = 1.0)
    }

    // DistanceCIEDE2000klch uses the Delta E 2000 formula with custom values
    // for the weighting factors kL, kC, and kH.
    fun distanceCIEDE2000klch(cr: Color, kl: Double, kc: Double, kh: Double): Double {
        var (l1, a1, b1) = this.lab()
        var (l2, a2, b2) = cr.lab()

        // As with CIE94, we scale up the ranges of L,a,b beforehand and scale
        // them down again afterwards.
        l1 *= 100.0
        a1 *= 100.0
        b1 *= 100.0
        l2 *= 100.0
        a2 *= 100.0
        b2 *= 100.0

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
    fun blendLab(c2: Color, t: Double): Color {
        val (l1, a1, b1) = this.lab()
        val (l2, a2, b2) = c2.lab()
        return lab(l = l1 + t * (l2 - l1),
            a = a1 + t * (a2 - a1),
            b = b1 + t * (b2 - b1))
    }

    // DistanceLuv is a good measure of visual similarity between two colors!
    // A result of 0 would mean identical colors, while a result of 1 or higher
    // means the colors differ a lot.
    fun distanceLuv(c2: Color): Double {
        val (l1, u1, v1) = this.luv()
        val (l2, u2, v2) = c2.luv()
        return sqrt(sq(l1 - l2) + sq(u1 - u2) + sq(v1 - v2))
    }

    // BlendLuv blends two colors in the CIE-L*u*v* color-space, which should result in a smoother blend.
    // t == 0 results in c1, t == 1 results in c2
    fun blendLuv(c2: Color, t: Double): Color {
        val (l1, u1, v1) = this.luv()
        val (l2, u2, v2) = c2.luv()
        return luv(l = l1 + t * (l2 - l1),
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
    fun hcl(): HCL {
        return this.hclWhiteRef(whiteReference = D65)
    }

    // Converts the given color to HCL space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // H values are in [0..360], C and L values are in [0..1]
    fun hclWhiteRef(whiteReference: WhiteReference): HCL {
        val lab = this.labWhiteRef(whiteReference = whiteReference)
        return labToHcl(l = lab.l, a = lab.a, b = lab.b)
    }

    // BlendHcl blends two colors in the CIE-L*C*h° color-space, which should result in a smoother blend.
    // t == 0 results in c1, t == 1 results in c2
    fun blendHcl(color2: Color, t: Double): Color {
        var (h1, c1, l1) = hcl()
        var (h2, c2, l2) = color2.hcl()

        // https://github.com/lucasb-eyer/go-colorful/pull/60
        if (c1 <= 0.00015 && c2 >= 0.00015) {
            h1 = h2
        } else if (c2 <= 0.00015 && c1 >= 0.00015) {
            h2 = h1
        }

        // We know that h are both in [0..360]
        return hcl(h = interpolateAngle(a0 = h1, a1 = h2, t = t), c = c1 + t * (c2 - c1), l = l1 + t * (l2 - l1))
            .clamped()
    }

    // LuvLch

    // Converts the given color to LuvLCh space using D65 as reference white.
    // h values are in [0..360], C and L values are in [0..1] although C can overshoot 1.0
    fun luvLCh(): LCH {
        return luvLChWhiteRef(whiteReference = D65)
    }

    // Converts the given color to LuvLCh space, taking into account
    // a given reference white. (i.e. the monitor's white)
    // h values are in [0..360], c and l values are in [0..1]
    fun luvLChWhiteRef(whiteReference: WhiteReference): LCH {
        val (l, u, v) = luvWhiteRef(whiteReference = whiteReference)
        return luvToLuvLCh(l = l, u = u, v = v)
    }

    // Generates a color by using data given in LuvLCh space using D65 as reference white.
    // h values are in [0..360], C and L values are in [0..1]
    // WARNING: many combinations of `l`, `c`, and `h` values do not have corresponding
    // valid RGB values, check the FAQ in the README if you're unsure.
    fun luvLCh(l: Double, c: Double, h: Double): Color {
        return luvLChWhiteRef(l = l, c = c, h = h, whiteReference = D65)
    }

    // BlendLuvLCh blends two colors in the cylindrical CIELUV color space.
    // t == 0 results in c1, t == 1 results in c2
    fun blendLuvLCh(color2: Color, t: Double): Color {
        val (l1, c1, h1) = luvLCh()
        val (l2, c2, h2) = color2.luvLCh()

        // We know that h are both in [0..360]
        return luvLCh(l = l1 + t * (l2 - l1), c = c1 + t * (c2 - c1), h = interpolateAngle(a0 = h1, a1 = h2, t = t))
    }



    // HSLuv returns the Hue, Saturation and Luminance of the color in the HSLuv
    // color space. Hue in [0..360], a Saturation [0..1], and a Luminance
    // (lightness) in [0..1].
    fun hsluv(): HSL {
        // sRGB -> Linear RGB -> CIEXYZ -> CIELUV -> LuvLCh -> HSLuv
        val lch = luvLChWhiteRef(whiteReference = hSLuvD65)
        return luvLChToHSLuv(l = lch.l, c = lch.c, h = lch.h)
    }

    // HPLuv returns the Hue, Saturation and Luminance of the color in the HSLuv
    // color space. Hue in [0..360], a Saturation [0..1], and a Luminance
    // (lightness) in [0..1].
    //
    // Note that HPLuv can only represent pastel colors, and so the Saturation
    // value could be much larger than 1 for colors it can't represent.
    fun hpluv(): HSL {
        val lch = luvLChWhiteRef(whiteReference = hSLuvD65)
        return luvLChToHPLuv(l = lch.l, c = lch.c, h = lch.h)
    }

    // DistanceHPLuv calculates Euclidean distance in the HPLuv colorspace. No idea
    // how useful this is.

    // The Hue value is divided by 100 before the calculation, so that H, S, and L
    // have the same relative ranges.
    fun distanceHPLuv(c2: Color): Double {
        val (h1, s1, l1) = hpluv()
        val (h2, s2, l2) = c2.hpluv()
        return sqrt(sq((h1 - h2) / 100.0) + sq(s1 - s2) + sq(l1 - l2))
    }


    override fun toString(): String {
        return "R: $r, G: $g B: $b"
    }



    override fun compareTo(other: Color): Int {
        return when {
            this.r != other.r -> this.r.compareTo(other.r)
            this.g != other.g -> this.g.compareTo(other.g)
            else -> this.b.compareTo(other.b)
        }
    }

    companion object {
        // Utility used by Hxx color-spaces for interpolating between two angles in [0,360].
        private fun interpolateAngle(a0: Double, a1: Double, t: Double): Double {
            // Based on the answer here: http://stackoverflow.com/a/14498790/2366315
            // With potential proof that it works here: http://math.stackexchange.com/a/2144499
            val delta = ((a1 - a0).mod(360.0) + 540).mod(360.0) - 180.0
            return (a0 + t * delta + 360.0).mod(360.0)
        }


        // Hsv creates a new Color given a Hue in [0..360], a Saturation and a Value in [0..1]
        fun hsv(h: Double, s: Double, v: Double): Color {
            val hp = h / 60.0
            val c = v * s
            val x = c * (1.0 - abs(hp.mod(2.0) - 1.0))

            val m = v - c
            var r = 0.0
            var g = 0.0
            var b = 0.0

            if (hp >= 0.0 && hp < 1.0) {
                r = c
                g = x
            } else if (hp >= 1.0 && hp < 2.0) {
                r = x
                g = c
            } else if (hp >= 2.0 && hp < 3.0) {
                g = c
                b = x
            } else if (hp >= 3.0 && hp < 4.0) {
                g = x
                b = c
            } else if (hp >= 4.0 && hp < 5.0) {
                r = x
                b = c
            } else if (hp >= 5.0 && hp < 6.0) {
                r = c
                b = x
            }

            return Color(r = m + r, g = m + g, b = m + b)
        }

        // Hsl creates a new Color given a Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1]
        fun hsl(h: Double, s: Double, l: Double): Color {
            if (s == 0.0) {
                return Color(r = l, g = l, b = l)
            }

            val r: Double
            val g: Double
            val b: Double
            val t2: Double
            var tr: Double
            var tg: Double
            var tb: Double

            val t1: Double = if (l < 0.5) {
                l * (1.0 + s)
            } else {
                l + s - l * s
            }

            t2 = 2 * l - t1
            val computedH = h / 360
            tr = computedH + 1.0 / 3.0
            tg = computedH
            tb = computedH - 1.0 / 3.0

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
            r = if (6 * tr < 1) {
                t2 + (t1 - t2) * 6 * tr
            } else if (2 * tr < 1) {
                t1
            } else if (3 * tr < 2) {
                t2 + (t1 - t2) * (2.0 / 3.0 - tr) * 6
            } else {
                t2
            }

            // Green
            g = if (6 * tg < 1) {
                t2 + (t1 - t2) * 6 * tg
            } else if (2 * tg < 1) {
                t1
            } else if (3 * tg < 2) {
                t2 + (t1 - t2) * (2.0 / 3.0 - tg) * 6
            } else {
                t2
            }

            // Blue
            b = if (6 * tb < 1) {
                t2 + (t1 - t2) * 6 * tb
            } else if (2 * tb < 1) {
                t1
            } else if (3 * tb < 2) {
                t2 + (t1 - t2) * (2.0 / 3.0 - tb) * 6
            } else {
                t2
            }

            return Color(r = r, g = g, b = b)
        }

        // Hex parses a "html" hex color-string, either in the 3 "#f0c" or 6 "#ff1034" digits form.
        fun hex(colorString: String): Color? {
            val scol = if (colorString.startsWith("#")) colorString.substring(1) else colorString

            if (scol.length == 3) {
                val r = scol.substring(0, 1).toInt(16) / 255.0
                val g = scol.substring(1, 2).toInt(16) / 255.0
                val b = scol.substring(2, 3).toInt(16) / 255.0
                return Color(r = r, g = g, b = b)
            } else if (scol.length == 6) {
                val r = scol.substring(0, 2).toInt(16) / 255.0
                val g = scol.substring(2, 4).toInt(16) / 255.0
                val b = scol.substring(4, 6).toInt(16) / 255.0
                return Color(r = r, g = g, b = b)
            }

            return null
        }

        /// Linear ///
        //////////////
        // http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/
        // http://www.brucelindbloom.com/Eqn_RGB_to_XYZ.html

        fun linearize(v: Double): Double {
            if (v <= 0.04045) {
                return v / 12.92
            }
            return ((v + 0.055) / 1.055).pow(2.4)
        }

        // A much faster and still quite precise linearization using a 6th-order Taylor approximation.
        // See the accompanying Jupyter notebook for derivation of the constants.
        fun linearizeFast(v: Double): Double {
            val v1 = v - 0.5
            val v2 = v1 * v1
            val v3 = v2 * v1
            val v4 = v2 * v2
            // v5 := v3*v2
            return -0.248750514614486 + 0.925583310193438 * v + 1.16740237321695 * v2 + 0.280457026598666 * v3 - 0.0757991963780179 * v4 // + 0.0437040411548932*v5
        }

        fun delinearize(v: Double): Double {
            if (v <= 0.0031308) {
                return 12.92 * v
            }
            return 1.055 * v.pow(1.0 / 2.4) - 0.055
        }

        // LinearRgb creates an sRGB color out of the given linear RGB color (see http://www.sjbrown.co.uk/2004/05/14/gamma-correct-rendering/).
        fun linearRgb(r: Double, g: Double, b: Double): Color {
            return Color(r = delinearize(r), g = delinearize(g), b = delinearize(b))
        }

        fun delinearizeFast(v: Double): Double {
            val v1: Double
            val v2: Double
            val v3: Double
            val v4: Double
            val v5: Double
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
        fun fastLinearRgb(r: Double, g: Double, b: Double): Color {
            return Color(r = delinearizeFast(r), g = delinearizeFast(g), b = delinearizeFast(b))
        }

        // XyzToLinearRgb converts from CIE XYZ-space to Linear RGB space.
        fun xyzToLinearRgb(x: Double, y: Double, z: Double): RGB {
            val r = 3.2409699419045214 * x - 1.5373831775700935 * y - 0.49861076029300328 * z
            val g = -0.96924363628087983 * x + 1.8759675015077207 * y + 0.041555057407175613 * z
            val b = 0.055630079696993609 * x - 0.20397695888897657 * y + 1.0569715142428786 * z
            return RGB(r, g, b)
        }

        fun linearRgbToXyz(r: Double, g: Double, b: Double): XYZ {
            val x = 0.41239079926595948 * r + 0.35758433938387796 * g + 0.18048078840183429 * b
            val y = 0.21263900587151036 * r + 0.71516867876775593 * g + 0.072192315360733715 * b
            val z = 0.019330818715591851 * r + 0.11919477979462599 * g + 0.95053215224966058 * b
            return XYZ(x, y, z)
        }

        fun xyz(x: Double, y: Double, z: Double): Color {
            val (r, g, b) = xyzToLinearRgb(x = x, y = y, z = z)
            return linearRgb(r = r, g = g, b = b)
        }

        /// xyY ///
        ///////////
        // http://www.brucelindbloom.com/Eqn_XYZ_to_xyY.html

        // Well, the name is bad, since it's xyY but Golang needs me to start with a
        // capital letter to make the method public.
        fun xyzToXyy(x: Double, y: Double, z: Double): XYYout {
            return xyzToXyyWhiteRef(x = x, y = y, z = z, whiteReference = D65)
        }

        fun xyzToXyyWhiteRef(x: Double, y: Double, z: Double, whiteReference: WhiteReference): XYYout {
            val computedX: Double
            val computedY: Double
            val n = x + y + z
            if (abs(n) < 1e-14) {
                // When we have black, Bruce Lindbloom recommends to use
                // the reference white's chromacity for x and y.
                computedX = whiteReference.whiteReference[0] / (whiteReference.whiteReference[0] + whiteReference.whiteReference[1] + whiteReference.whiteReference[2])
                computedY = whiteReference.whiteReference[1] / (whiteReference.whiteReference[0] + whiteReference.whiteReference[1] + whiteReference.whiteReference[2])
            } else {
                computedX = x / n
                computedY = y / n
            }
            return XYYout(computedX, computedY, y)
        }

        fun xyyToXyz(x: Double, y: Double, yOut: Double): XYoutZ {
            val computedX: Double
            val computedZ: Double

            if (y > -1e-14 && y < 1e-14) {
                computedX = 0.0
                computedZ = 0.0
            } else {
                computedX = yOut / y * x
                computedZ = yOut / y * (1.0 - x - y)
            }

            return XYoutZ(computedX, yOut, computedZ)
        }


        /// L*a*b* ///
        //////////////
        // http://en.wikipedia.org/wiki/Lab_color_space#CIELAB-CIEXYZ_conversions
        // For L*a*b*, we need to L*a*b*<->XYZ->RGB and the first one is device dependent.

        fun labF(t: Double): Double {
            if (t > 6.0 / 29.0 * 6.0 / 29.0 * 6.0 / 29.0) {
                return cbrt(t)
            }
            return t / 3.0 * 29.0 / 6.0 * 29.0 / 6.0 + 4.0 / 29.0
        }

        fun xyzToLab(x: Double, y: Double, z: Double): LAB {
            // Use D65 white as reference point by default.
            // http://www.fredmiranda.com/forum/topic/1035332
            // http://en.wikipedia.org/wiki/Standard_illuminant
            return xyzToLabWhiteReference(x = x, y = y, z = z, whiteReference = D65)
        }

        fun xyzToLabWhiteReference(x: Double, y: Double, z: Double, whiteReference: WhiteReference): LAB {
            val fy = labF(y / whiteReference.whiteReference[1])
            val l = 1.16 * fy - 0.16
            val a = 5.0 * (labF(x / whiteReference.whiteReference[0]) - fy)
            val b = 2.0 * (fy - labF(z / whiteReference.whiteReference[2]))
            return LAB(l, a, b)
        }

        fun labFinv(t: Double): Double {
            if (t > 6.0 / 29.0) {
                return t * t * t
            }
            return 3.0 * 6.0 / 29.0 * 6.0 / 29.0 * (t - 4.0 / 29.0)
        }

        fun labToXyz(l: Double, a: Double, b: Double): XYZ {
            // D65 white (see above).
            return labToXyzWhiteRef(l = l, a = a, b = b, whiteReference = D65)
        }

        fun labToXyzWhiteRef(l: Double, a: Double, b: Double, whiteReference: WhiteReference): XYZ {
            val l2 = (l + 0.16) / 1.16
            val x = whiteReference.whiteReference[0] * labFinv(l2 + a / 5.0)
            val y = whiteReference.whiteReference[1] * labFinv(l2)
            val z = whiteReference.whiteReference[2] * labFinv(l2 - b / 2.0)
            return XYZ(x, y, z)
        }

        // Generates a color by using data given in CIE xyY space.
        // x, y and Y are in [0..1]
        fun xyy(x: Double, y: Double, yOut: Double): Color {
            val xyz = xyyToXyz(x = x, y = y, yOut = yOut)
            return xyz(x = xyz.x, y = xyz.yout, z = xyz.z)
        }

        // Generates a color by using data given in CIE L*a*b* space using D65 as reference white.
        // WARNING: many combinations of `l`, `a`, and `b` values do not have corresponding
        // valid RGB values, check the FAQ in the README if you're unsure.
        fun lab(l: Double, a: Double, b: Double): Color {
            val (x, y, z) = labToXyz(l = l, a = a, b = b)
            return xyz(x = x, y = y, z = z)
        }

        // Generates a color by using data given in CIE L*a*b* space, taking
        // into account a given reference white. (i.e. the monitor's white)
        fun labWhiteRef(l: Double, a: Double, b: Double, whiteReference: WhiteReference): Color {
            val (x, y, z) = labToXyzWhiteRef(l = l, a = a, b = b, whiteReference = whiteReference)
            return xyz(x = x, y = y, z = z)
        }

        fun luvLChToLuv(l: Double, c: Double, h: Double): LUV {
            val radH = 0.01745329251994329576 * h // Deg2Rad
            val u = c * cos(radH)
            val v = c * sin(radH)
            return LUV(l, u, v)
        }

        // Generates a color by using data given in LuvLCh space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // h values are in [0..360], C and L values are in [0..1]
        fun luvLChWhiteRef(l: Double, c: Double, h: Double, whiteReference: WhiteReference): Color {
            val luv = luvLChToLuv(l = l, c = c, h = h)
            return luvWhiteRef(l = luv.l, u = luv.u, v = luv.v, whiteReference = whiteReference)
        }

        // Generates a color by using data given in CIE L*u*v* space using D65 as reference white.
        // L* is in [0..1] and both u* and v* are in about [-1..1]
        // WARNING: many combinations of `l`, `u`, and `v` values do not have corresponding
        // valid RGB values, check the FAQ in the README if you're unsure.
        fun luv(l: Double, u: Double, v: Double): Color {
            val (x, y, z) = luvToXyz(l = l, u = u, v = v)
            return xyz(x = x, y = y, z = z)
        }

        // Generates a color by using data given in CIE L*u*v* space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // L* is in [0..1] and both u* and v* are in about [-1..1]
        fun luvWhiteRef(l: Double, u: Double, v: Double, whiteReference: WhiteReference): Color {
            val (x, y, z) = luvToXyzWhiteRef(l = l, u = u, v = v, whiteReference = whiteReference)
            return xyz(x = x, y = y, z = z)
        }

        fun labToHcl(l: Double, a: Double, b: Double): HCL {
            // Oops, floating point workaround necessary if (a ~= b and both are very small (i.e. almost zero)).
            val h: Double = if (abs(b - a) > 1e-4 && abs(a) > 1e-4) {
                (57.29577951308232087721 * atan2(b, a) + 360.0).mod(360.0) // Rad2Deg
            } else {
                0.0
            }
            val c = sqrt(sq(a) + sq(b))
            return HCL(h, c, l)
        }

        // Generates a color by using data given in HCL space using D65 as reference white.
        // H values are in [0..360], C and L values are in [0..1]
        // WARNING: many combinations of `h`, `c`, and `l` values do not have corresponding
        // valid RGB values, check the FAQ in the README if you're unsure.
        fun hcl(h: Double, c: Double, l: Double): Color {
            return hclWhiteRef(h = h, c = c, l = l, whiteReference = D65)
        }

        fun hclToLab(h: Double, c: Double, l: Double): LAB {
            val radH = 0.01745329251994329576 * h // Deg2Rad
            val a = c * cos(radH)
            val b = c * sin(radH)
            return LAB(l, a, b)
        }

        // Generates a color by using data given in HCL space, taking
        // into account a given reference white. (i.e. the monitor's white)
        // H values are in [0..360], C and L values are in [0..1]
        fun hclWhiteRef(h: Double, c: Double, l: Double, whiteReference: WhiteReference): Color {
            val lab = hclToLab(h = h, c = c, l = l)
            return labWhiteRef(l = lab.l, a = lab.a, b = lab.b, whiteReference = whiteReference)
        }

        fun luvToLuvLCh(l: Double, u: Double, v: Double): LCH {
            // Oops, floating point workaround necessary if (u ~= v and both are very small (i.e. almost zero)).
            val h: Double = if (abs(v - u) > 1e-4 && abs(u) > 1e-4) {
                (57.29577951308232087721 * atan2(v, u) + 360.0).mod(360.0) // Rad2Deg
            } else {
                0.0
            }
            val c: Double = sqrt(sq(u) + sq(v))
            return LCH(l, c, h)
        }

        fun luvToXyz(l: Double, u: Double, v: Double): XYZ {
            // D65 white (see above).
            return luvToXyzWhiteRef(l = l, u = u, v = v, whiteReference = D65)
        }

        fun luvToXyzWhiteRef(l: Double, u: Double, v: Double, whiteReference: WhiteReference): XYZ {
            val x: Double
            var y: Double
            var z: Double
            z = 0.0

            // y = whiteReference[1] * lab_finv((l + 0.16) / 1.16)
            y = if (l <= 0.08) {
                whiteReference.whiteReference[1] * l * 100.0 * 3.0 / 29.0 * 3.0 / 29.0 * 3.0 / 29.0
            } else {
                whiteReference.whiteReference[1] * cub((l + 0.16) / 1.16)
            }

            val (un, vn) = xyzToUv(x = whiteReference.whiteReference[0], y = whiteReference.whiteReference[1], z = whiteReference.whiteReference[2])
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

        fun xyzToLuv(x: Double, y: Double, z: Double): LAB {
            // Use D65 white as reference point by default.
            // http://www.fredmiranda.com/forum/topic/1035332
            // http://en.wikipedia.org/wiki/Standard_illuminant
            val (l, u, v) = xyzToLuvWhiteRef(x = x, y = y, z = z, whiteReference = D65)
            return LAB(l, u, v)
        }

        fun xyzToLuvWhiteRef(x: Double, y: Double, z: Double, whiteReference: WhiteReference): LUV {
            val l: Double = if (y / whiteReference.whiteReference[1] <= 6.0 / 29.0 * 6.0 / 29.0 * 6.0 / 29.0) {
                y / whiteReference.whiteReference[1] * (29.0 / 3.0 * 29.0 / 3.0 * 29.0 / 3.0) / 100.0
            } else {
                1.16 * cbrt(y / whiteReference.whiteReference[1]) - 0.16
            }
            val (ubis, vbis) = xyzToUv(x = x, y = y, z = z)
            val (un, vn) = xyzToUv(x = whiteReference.whiteReference[0], y = whiteReference.whiteReference[1], z = whiteReference.whiteReference[2])
            val u: Double = 13.0 * l * (ubis - un)
            val v: Double = 13.0 * l * (vbis - vn)
            return LUV(l, u, v)
        }

        // For this part, we do as R's graphics.hcl does, not as wikipedia does.
        // Or is it the same?
        fun xyzToUv(x: Double, y: Double, z: Double): UV {
            val u: Double
            val v: Double
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





        fun luvLChToHSLuv(l: Double, c: Double, h: Double): HSL {
            // [-1..1] but the code expects it to be [-100..100]
            val computedC: Double = c * 100.0
            val computedL: Double = l * 100.0

            val s: Double
            val max: Double
            if (computedL > 99.9999999 || computedL < 0.00000001) {
                s = 0.0
            } else {
                max = maxChromaForLH(l = computedL, h = h)
                s = computedC / max * 100.0
            }
            return HSL(h, clamp01(s / 100.0), clamp01(computedL / 100.0))
        }

        fun hsluvToLuvLCh(h: Double, s: Double, l: Double): LCH {
            val computedL: Double = l * 100.0
            val computedS: Double = s * 100.0

            val c: Double
            val max: Double
            if (computedL > 99.9999999 || computedL < 0.00000001) {
                c = 0.0
            } else {
                max = maxChromaForLH(l = computedL, h = h)
                c = max / 100.0 * computedS
            }

            // c is [-100..100], but for LCh it's supposed to be almost [-1..1]
            return LCH(clamp01(computedL / 100.0), c / 100.0, h)
        }

        fun luvLChToHPLuv(l: Double, c: Double, h: Double): HSL {
            // [-1..1] but the code expects it to be [-100..100]
            val computedC: Double = c * 100.0
            val computedL: Double = l * 100.0

            val s: Double
            val max: Double
            if (computedL > 99.9999999 || computedL < 0.00000001) {
                s = 0.0
            } else {
                max = maxSafeChromaForL(computedL)
                s = computedC / max * 100.0
            }
            return HSL(h, s / 100.0, computedL / 100.0)
        }

        fun hpluvToLuvLCh(h: Double, s: Double, l: Double): LCH {
            // [-1..1] but the code expects it to be [-100..100]
            val computedL = l * 100.0
            val computedS = s * 100.0

            val c: Double
            val max: Double
            if (computedL > 99.9999999 || computedL < 0.00000001) {
                c = 0.0
            } else {
                max = maxSafeChromaForL(computedL)
                c = max / 100.0 * computedS
            }
            return LCH(computedL / 100.0, c / 100.0, h)
        }


        // HSLuv creates a new Color from values in the HSLuv color space.
        // Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1].
        //
        // The returned color values are clamped (using .Clamped), so this will never output
        // an invalid color.
        fun hsluv(h: Double, s: Double, l: Double): Color {
            // HSLuv -> LuvLCh -> CIELUV -> CIEXYZ -> Linear RGB -> sRGB
            val lch = hsluvToLuvLCh(h = h, s = s, l = l)
            val luv = luvLChToLuv(l = lch.l, c = lch.c, h = lch.h)
            val (x, y, z) = luvToXyzWhiteRef(l = luv.l, u = luv.u, v = luv.v, whiteReference = hSLuvD65)
            val (r, g, b) = xyzToLinearRgb(x = x, y = y, z = z)
            return linearRgb(r = r, g = g, b = b).clamped()
        }

        // HPLuv creates a new Color from values in the HPLuv color space.
        // Hue in [0..360], a Saturation [0..1], and a Luminance (lightness) in [0..1].
        //
        // The returned color values are clamped (using .Clamped), so this will never output
        // an invalid color.
        fun hpluv(h: Double, s: Double, l: Double): Color {
            // HPLuv -> LuvLCh -> CIELUV -> CIEXYZ -> Linear RGB -> sRGB
            val lch = hpluvToLuvLCh(h = h, s = s, l = l)
            val luv = luvLChToLuv(l = lch.l, c = lch.c, h = lch.h)
            val (x, y, z) = luvToXyzWhiteRef(l = luv.l, u = luv.u, v = luv.v, whiteReference = hSLuvD65)
            val (r, g, b) = xyzToLinearRgb(x = x, y = y, z = z)
            return linearRgb(r = r, g = g, b = b).clamped()
        }

        private var m = arrayOf(
            arrayOf(3.2409699419045214, -1.5373831775700935, -0.49861076029300328),
            arrayOf(-0.96924363628087983, 1.8759675015077207, 0.041555057407175613),
            arrayOf(0.055630079696993609, -0.20397695888897657, 1.0569715142428786),
        )

        private const val KAPPA = 903.2962962962963
        private const val EPSILON = 0.0088564516790356308

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
            val sub2: Double
            val ret = (1..6).map { (1..2).map { 0.0 }.toTypedArray() }.toTypedArray()
            val sub1 = (l + 16.0).pow(3.0) / 1_560_896.0
            sub2 = if (sub1 > EPSILON) {
                sub1
            } else {
                l / KAPPA
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
