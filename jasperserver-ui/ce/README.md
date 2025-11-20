# Jasper Server OS Community WEB UI
## Set Up
- install [nodejs](https://nodejs.org). Build tested on version 14.x.x, but it might also work on version 10.x.x
- install [yarn](https://yarnpkg.com/getting-started/install). Build tested on version 1.22.x
- Additional requirements for Windows OS (it is required because build process uses unix-specific commands):
    - install [git for windows](https://git-scm.com/downloads).
    - run the following command to configure path to `git bash` 
    (change path to `bash.exe` if git installation path has been changed): 
    ```shell script
    npm config set script-shell "<path\\to\\bash.exe>"
    ```
    default path to `bash.exe` in 32 bit installation is: `C:\\Program Files (x86)\\git\\bin\\bash.exe`  
    default path to `bash.exe` in 64 bit installation is: `C:\\Program Files\\git\\bin\\bash.exe`
        
- run the following command in `UI sources root folder` (the folder which contains `lerna.json`):
```shell script
yarn install
``` 

## Build
run the following commands in this folder:
```shell script
yarn run clean
yarn run build
```
>After success build files will be placed here: `jrs-ui/build/overlay/scripts`.

## Deploy
To deploy build result to the Jasper Server OS instance, copy content of `jrs-ui/build/overlay`
to the deployment folder of your JRS instance. 

For example if your app server is **Tomcat** and **Jasper Server OS** deployed here:
`c:\tomcat\webapps\jasperserver`, then copy content of the `jrs-ui/build/overlay` 
folder to `c:\tomcat\webapps\jasperserver` (replace all target files)

*NOTE:* Read [./jrs-ui/README.md](./jrs-ui/README.md) for more options like
- automated deployment
- webpack development server



