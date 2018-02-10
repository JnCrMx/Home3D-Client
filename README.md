# Home3D-Client
Client for Home3D system. Just a small private project.

## Update
To update the repository type in:<br>
<code>git pull</code><br>
<code>git submodule update --recursive --remote --force</code>

## Installation
Install the program after you'd updated the repository. For more information about updating see section "Update".<br>
<b>The program will be compiled automatically during installation. You don't have to do this manually!</b>

To start the installation just type in:<br>
<code>sudo ./gradlew install</code>

After installation the binaries are located in <code>/home/Home3D</code>. The program's config is saved as <code>/home/Home3D/config.txt</code>.
If this is your fist installation you have to update to configuration file. See section "Configuration" for more details.

If there's already an installed version present, it's binaries will be replaced but the config won't be modified.

## Configuration
<table>
    <tr>
        <th>Key</th>
        <th>Default</th>
        <th>Meaning</th>
    </tr>
    <tr>
        <td><code>username</code></td>
        <td><code>null</code></td>
        <td>The name of your account</td>
    </tr>
    <tr>
        <td><code>password</code></td>
        <td><code>null</code></td>
        <td>The password of your account</td>
    </tr>
    <tr>
        <td><code>printerId</code></td>
        <td><code>0</code></td>
        <td>The number assigned to your printer</td>
    </tr>
    <tr>
        <td><code>printerModel</code></td>
        <td><code>null</code></td>
        <td>The name of the printer model you have</td>
    </tr>
    <tr>
        <td><code>printerPort</code></td>
        <td><code>/dev/ttyUSB0</code></td>
        <td>The serial port your printer is connected to<br>On a raspberry pi the default value should be correct.</td>
    </tr>
    <tr>
        <td><code>hostname</code></td>
        <td><code>192.168.188.28</code></td>
        <td>The address of the server<br>At the moment the system is WIP so this is a <b>local</b> address and won't work.</td>
    </tr>
    <tr>
        <td><code>port</code></td>
        <td><code>819</code></td>
        <td>The port of the server</td>
    </tr>
</table>

## Compile
If you want to just compile the program or have to install it by yourself, type in:<br>
<code>./gradlew jar</code><br>
<code>./gradlew natives</code>

The binaries will be saved as <code>build/libs/Home3D.jar</code>, <code>print3d/Debug/print3d</code> and <code>strip_gcode/Debug/strip_gcode</code>. The default config is stored as <code>default/config.txt</code>.

To work, the three binary files must be saved in one folder together with <code>config.txt</code>. You have to make them <b>executable</b> with <code>chmod +x [file]</code>.
