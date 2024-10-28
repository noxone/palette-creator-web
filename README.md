# Palette Creator

This projects aims at making it *really* easy for everybody to create a nice looking shades that can be included
in nearly any development project.

The idea is, to give a developer the possibility to use well fitting colors in a project.

## Motivation

Well, I'm not good at colors... but I can write code. And if I want to create some nice looking UI in my
projects, I might lack the knowledge of how to create good colors.  
That's why I created this project, to simplify this tasks - especially for myself.

## Usage

### Online

Go to [Shades](https://shades.olafneumann.org/) and try it online.

The page supports several search parameters to change the initial state of the options:

- ``primary``: hex value of the primary color
- ``neutral``: hex value of the neutral color
- ``accents``: comma separated list of all accent colors
- ``accent_names``: comma separated list of the names of accent colors. If the length does not match with the parameter above, this parameter will be ignored and default names will be generated.
- ``accent_seed``: counter for already generated accent colors
- ``enforce_color``: `true` if the selected primary (and accent) color shall be part of the generated list of shades. If `false`, the selected primary color is basically the starting point for all other computations.
- ``count``: The number of shades to generate

<!-- TODO: Example: [https://shades.olafneumann.org/](https://shades.olafneumann.org/)-->

### Docker

You can also start it via Docker. Please find the generated images in this [repository](https://hub.docker.com/r/noxone/shades). Just use the following command and use ``Shades`` via port 80 of your local machine:

```bash
docker run -d -p 80:80 noxone/shades
```

Of course the docker version supports the same search parameters as the actual website.

## Development

### Build

1. Clone the project
2. In the project's root directory execute

   ```bash
   gradlew clean build
   ```

3. Find the output in directory ``./build/dist/js/productionExecutable``

Of course, you can also build the project using Docker:

   ```bash
   docker build . -t noxone/shades
   ```

After that just use the image like described in part `Usage`.

### Live Development

For a nice development experience use

```bash
gradlew run --continuous
```

Using this command the page will reload automatically for every source file change.

## Contributing

For this project to become more and more useful, it depends on your help.

If you are missing anything (may it be a file format, a color generation algorithm, an output generator for your IDE, ...)
please create a [Github issue](https://github.com/noxone/palette-creator-web/issues) or directly create a pull request with your proposed changes.
