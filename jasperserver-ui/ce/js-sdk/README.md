# js-sdk

see [../jrs-ui/README.md]() for basic doc
see [../jrs-ui/README_DEVELOPMENT.md]() for advanced development docs
  
## Generate CSS from SASS

    yarn run styles

## Watch for changes in SASS files 

    yarn run styles:watch

## Generate icons - fonts
First, place .svg icon(s) into `images/` folder, then run command:

    yarn run webfont

this generates 'icons' font files **.eot .svg .ttf .woff** in *themes/default/jasper-ui/fonts/*, and its css styles to *scss/**_icons**.scss*, which is setup to be imported into ***_jasper-ui.scss*** 

**note:** each .svg icon was designed in size (width: 40px, height: 32px)

## Developing CSS on working JRS instance

run the following command in `js-ui` or `jrs-ui-pro` 
(depends on what version version of Jasper Server OS you are using - community or commercial):

    yarn run start:themes
       
this will run webpack dev server which will also watch changes in css.
If you want to watch changes in SASS then run the following two commands:

- in `jrs-ui` or `jrs-ui-pro`:

```shell script
yarn run start:themes
``` 

- in `js-sdk`:

```shell script
yarn run styles:watch
```       
               
## Start Http Server 

Start tiny HTTP server in the root folder of the project to work with demos and samples

    yarn run connect
   
Then open [demos](http://localhost:8000/demos)
