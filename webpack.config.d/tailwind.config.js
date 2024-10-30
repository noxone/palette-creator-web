// must be in the jsMain/resource folder
const mainCssFile = 'styles.css';

// tailwind config (https://tailwindcss.com/docs/configuration)
const tailwind = {
    darkMode: 'media',
    plugins: [
        // require('@tailwindcss/forms') // optional
    ],
    variants: {},
    theme: {
        extend: {},
/*        colors: {
            'primary': {
                950: '#06121A',
                900: '#0B1D28',
                800: '#183344',
                700: '#254B61',
                600: '#346380',
                500: '#437DA0',
                400: '#5298C2',
                300: '#62B4E5',
                200: '#9CCCF0',
                100: '#D0E5F8',
                50: '#E8F2FB'
            },
            'neutral': {
                950: '#101112',
                900: '#1B1C1C',
                800: '#2F3032',
                700: '#454749',
                600: '#5D5E61',
                500: '#75777A',
                400: '#8E9194',
                300: '#A9ABAF',
                200: '#C5C6C9',
                100: '#E2E2E4',
                50: '#F0F1F1'
            },
            'error': {
                950: '#270505',
                900: '#3A0A09',
                800: '#5E1615',
                700: '#842321',
                600: '#AD302F',
                500: '#D83F3D',
                400: '#E96866',
                300: '#ED9392',
                200: '#F2B8B8',
                100: '#F8DCDC',
                50: '#FBEEEE'
            },
            'success': {
                950: '#05140D',
                900: '#0A2016',
                800: '#153628',
                700: '#224F3B',
                600: '#2F694F',
                500: '#3D8465',
                400: '#4CA07B',
                300: '#5BBD92',
                200: '#6ADBA9',
                100: '#87F8C4',
                50: '#CCFBE3'
            },
            'warning': {
                950: '#151004',
                900: '#221B09',
                800: '#3A2F15',
                700: '#534521',
                600: '#6F5C2E',
                500: '#8B753C',
                400: '#A88E4A',
                300: '#C7A859',
                200: '#E6C368',
                100: '#F8E0AF',
                50: '#FBEFDA'
            }
        }*/
    },
    content: {
        files: [
            '*.{js,html,css}',
            './kotlin/**/*.{js,html,css}'
        ],
        transform: {
            js: (content) => {
                return content.replaceAll(/(\\r)|(\\n)|(\\r\\n)/g,' ')
            }
        }
    },
};


// webpack tailwind css settings
((config) => {
    let entry = '/kotlin/' + mainCssFile;
    config.entry.main.push(entry);
    config.module.rules.push({
        test: /\.css$/,
        use: [
            {loader: 'style-loader'},
            {loader: 'css-loader'},
            {
                loader: 'postcss-loader',
                options: {
                    postcssOptions: {
                        plugins: [
                            require("tailwindcss")({config: tailwind}),
                            require("autoprefixer"),
                            require("cssnano")
                        ]
                    }
                }
            }
        ]
    });
})(config);