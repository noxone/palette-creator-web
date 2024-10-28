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
                900: '#0B1D28',
                800: '#183344',
                700: '#254B61',
                600: '#346380',
                500: '#437DA0',
                400: '#5298C2',
                300: '#62B4E5',
                200: '#9CCCF0',
                100: '#D0E5F8'
            },
            'neutral': {
                950: '#101112',
                838: '#28282A',
                725: '#404143',
                613: '#5A5B5E',
                500: '#75777A',
                387: '#929498',
                275: '#B0B2B5',
                163: '#D0D1D3',
                50: '#F0F1F1'
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