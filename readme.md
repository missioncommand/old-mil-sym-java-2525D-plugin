2525D prototype plugin for the SEC renderer. (RETIRED)  
Modifiers only work for ground units.  
Cyber Units aren't working properly as there is no cyber frame among the SVGs ESRI developed.  

New Renderer Work for 2525D/E has been funded.  
Those projects can be found here:  
[Java](https://github.com/missioncommand/mil-sym-java)  
[Android](https://github.com/missioncommand/mil-sym-android)  
[TypeScript](https://github.com/missioncommand/mil-sym-ts)  ****

Makes use of the SVG files from the ESRI joint-military-symbology-xml project (Apache-2.0 License)
https://github.com/Esri/joint-military-symbology-xml

Makes use of the svgSalamander project for rendering SVGs (SVG Salamander is available both under the LGPL 3 and BSD licenses)
https://github.com/blackears/svgSalamander
Copyright (c) 2010, Mark McKay
All rights reserved.

The simlpest way to test this is to 
1. put the mil-sym-renderer-jar-with-dependencies, svgSalamander and 2525DRenderer jars in one spot
2. type "java -cp * sec.web.renderer.portable.PortableWrapper" in a command window.
3. local web service should load and say "Found renderer plugin: 2525D"
4. Type "http://127.0.0.1:6789/mil-sym-service/renderer/image/30031000002003000000?RENDERER=2525D&SIZE=100" into your web browser

You should be looking at the 2525D "Law Enforecement" symbol

If you're loading the WAR file in apache or something, you'll have to modify
"mil-sym-service.war\WEB-INF\classes\properties\prop.properties"
to enable the loading of plugins.

When enablePlugins is set to true, the "mil-sym-service/plugins" folder will be created at the same level as 
#webapps if it doesn't already exist.  You put the jars needed for the plugin there.

If you're using the renderer JARs in your project, call 
public PNGInfo getSymbolImage(String symbolId, Map<String, String> symbolInfoMap)

Add key "RENDERER", value "2525D" to the symbolInfoMap to make sure the render request goes to the 2525D plugin.
