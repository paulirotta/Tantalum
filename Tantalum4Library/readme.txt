Netbeans 7.0.1 OSX project for Tantalum 2 J2ME Library

Pre-built stable versions available from
https://developer.forum.nokia.com/Tantalum

You likely need to customize your Netbeans project properties after checkout.
Under Application Descriptor section,
Add the 3 demo MIDlets
Add the following custom JAD properties:
   RSS-Feed-Url: http://feeds.bbci.co.uk/news/rss.xml
   Nokia-UI-Enhancement: CanvasHasBackground

Select the path on your machine to the Nokia libraries included with their
latest SDK emulator. With this, you can compile and build (but not emulate)
on OSX. If you are not using Nokia UIs, simply delete that example.

Project contact: paul.houghton@futurice.com
