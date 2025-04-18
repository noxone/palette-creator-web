package org.olafneumann.palette.colorful

data class RGB(val r: Double, val g: Double, val b: Double)
data class RGB255(val r: Int, val g: Int, val b: Int)
data class RGBA(val r: Int, val g: Int, val b: Int, val a: Int)
data class HCL(val h: Double, val c: Double, val l: Double)
data class HSL(val h: Double, val s: Double, val l: Double)
data class HSLuv(val h: Double, val s: Double, val l: Double)
data class HSV(val h: Double, val s: Double, val v: Double)
data class LCH(val l: Double, val c: Double, val h: Double)
data class XYYout(val x: Double, val y: Double, val yout: Double)
data class XYoutZ(val x: Double, val yout: Double, val z: Double)
data class XYZ(val x: Double, val y: Double, val z: Double)
data class LAB(val l: Double, val a: Double, val b: Double)
data class LUV(val l: Double, val u: Double, val v: Double)
data class UV(val u: Double, val v: Double)
