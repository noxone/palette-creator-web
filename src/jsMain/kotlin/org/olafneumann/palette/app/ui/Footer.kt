package org.olafneumann.palette.app.ui

import dev.fritz2.core.RenderContext
import dev.fritz2.core.href
import dev.fritz2.core.src
import dev.fritz2.core.target

fun RenderContext.footer() =
    footer {
        div("mx-auto w-full p-4 py-6 lg:py-8") {
            div("md:flex md:justify-between") {
                div("mb-6 md:mb-0") {
                    p("self-center text-2xl font-semibold whitespace-nowrap") {
                        +"Shade Generator"
                    }
                    p {
                        // TODO: Add description.
                        +"Some more text to describe stuff here. This will be a short summary of why you should use this page."
                    }
                }
                div("grid grid-cols-2 gap-8 sm:gap-6 sm:grid-cols-3") {
                    div {
                        h2("mb-6 text-sm font-semibold uppercase") {
                            +"Useful links"
                        }
                        ul("text-gray-500 dark:text-gray-400 font-medium") {
                            li("mb-4") {
                                a("hover:underline") {
                                    href("https://www.refactoringui.com/previews/building-your-color-palette")
                                    target("_blank")
                                    +"Refactoring UI"
                                }
                            }
                            li("mb-4") {
                                a("hover:underline") {
                                    href("https://www.canva.com/colors/color-wheel/")
                                    target("_blank")
                                    +"Canvas Color Wheel"
                                }
                            }
                            li {
                                a("hover:underline") {
                                    href("https://colorffy.com/color-scheme-generator")
                                    target("_blank")
                                    +"Colorffy Color scheme generator"
                                }
                            }
                        }
                    }
                    div {
                        h2("mb-6 text-sm font-semibold uppercase") {
                            +"Built using"
                        }
                        ul("text-gray-500 dark:text-gray-400 font-medium") {
                            li("mb-4") {
                                a("hover:underline ") {
                                    href("https://kotlinlang.org/")
                                    target("_blank")
                                    +"Kotlin"
                                }
                            }
                            li("mb-4") {
                                a("hover:underline") {
                                    href("https://www.fritz2.dev")
                                    target("_blank")
                                    +"fritz2"
                                }
                            }
                            li("mb-4") {
                                a("hover:underline") {
                                    href("https://tailwindcss.com")
                                    target("_blank")
                                    +"tailwind"
                                }
                            }
                            li("mb-4") {
                                a("hover:underline") {
                                    href("https://stuk.github.io/jszip/")
                                    target("_blank")
                                    +"JSZip"
                                }
                            }
                            li {
                                a("hover:underline") {
                                    href("https://github.com/eligrey/FileSaver.js")
                                    target("_blank")
                                    +"FileSaver"
                                }
                            }
                        }
                    }
                }
            }
            hr("my-6 border-gray-200 sm:mx-auto dark:border-gray-700 lg:my-8") {}
            div("sm:flex sm:items-center sm:justify-between") {
                span("text-sm text-gray-500 sm:text-center dark:text-gray-400") {
                    +"© 2024 "
                    a("hover:underline") {
                        href("https://github.com/noxone/")
                        target("_blank")
                        +"Olaf Neumann"
                    }
                    +"."
                }
                div("flex mt-4 sm:justify-center sm:mt-0") {
                    a("text-gray-500 hover:text-gray-900 dark:hover:text-white") {
                        href("https://github.com/noxone/palette-creator-web")
                        target("_blank")
                        +"Github"
                        span("sr-only") { +"Github link" }
                    }
                    a("text-gray-500 hover:text-gray-900 dark:hover:text-white ms-5") {
                        href("http://olafneumann.org")
                        target("_blank")
                        +"Olaf Neumann"
                        span("sr-only") { +"Website link" }
                    }
                    /*img("ms-5") {
                        // TODO: src("https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fshades.olafneumann.org&amp;count_bg=%23373A3D&amp;title_bg=%236C757D&amp;icon=&amp;icon_color=%23E7E7E7&amp;title=hits&amp;edge_flat=false")
                    }*/
                }
            }
        }
    }
