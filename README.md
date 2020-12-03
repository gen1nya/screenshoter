# screenshoter
Takes a screenshot every 5 seconds. For timelapses

Thanks to [Semper-Viventem](https://github.com/Semper-Viventem) for making it more readable ;)
## How to build:

Run gradle "jar" task.

Generated file will be placed in build/libs.

Alternatively, you can just download the build:
https://github.com/gen1nya/screenshoter/releases
## How to use:

```
java -jar SCREENSHOTER-1.1.jar
```

Captured screenshots and signature will be placed in screenshots_tiemstamp folder.

## Configuration:

After first run app also generate configuration file screenshoter.yaml with content like this:

```yaml
!!entities.Configuration {lastDir: screenshots_1606177049972\, screenshotInterval: 5}
```

If you want to change interval betveen capturing - change ```screenshotInterval``` field.

## Generating timelapse video from screenshots:
### Via ffmpeg (yes, ffmpeg required):

Run in screenshots folder:

```
ffmpeg -r 5 -f concat -safe 0 -i names.txt -vsync vfr output.mp4
```

```-r``` - images per second.
