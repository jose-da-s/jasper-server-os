Path:        uxexperimental/prototype-libs/stdnav
Library:     Standard Navigation
Description: Library for consistent keyboard and mouse navigation across
             accessible user interface controls and standard HTML elements.
Lead Author: Matt Heck, UI Engineer (mheck@jaspersoft.com)

1. Overview
-----------
As of August, 2014, Jasper Server OS's web front-end had practically no
working keyboard navigation.  Even simple TAB/SHIFT-TAB navigation was bad or
non-existent for most of the application.

As a direct consequence of this neglect, serious issues were raised during an
earlier accesibility audit for so-called "section 508" compliance.  These
issues are significant enough to deter certain large customers from using the
product, as they are bound to attempt to comply with various legal
accessibility standards.  Even simple operations such as scrolling a list of
results were compromised by conflicts between keyboard focus and mouse
highlighting.

It turns out that much of what is good for impaired users is also good for
normal users, and keyboard navigation is an excellent example of this.  With
the new Dashboard Designer and other components slated for Amber and Jade,
keyboard navigation shortcuts will greatly aid the efficiency of both power
users and the partially sight-impaired.

StandardNavigation provides the concept of a "subfocus" state-- a sort of
"inner-focus" for a child element within a set of siblings, when its parent
element has the normal browser "TAB" focus-- to help manage navigation within
menus, lists, toolbars, and other controls.  As a result, the logic in other
UI/UX libraries such as JSAM should be considerably reduced.  Keys such as
TAB, arrows, and ESC are handled by stdnav according to sets of default rules
which are hierarchically overridden by element type and various JavaScript
attributes.

This "subfocus" state avoids significant problems with screen-reader software
(critical for many accessibility scenarios) that arise when elements that are
not expected to be "focusable" are given a TAB index.  These problems were
found by thorough experimentation with screen reader software such as Orca
and NVDA, and can be fairly reliably avoided with this mechanism.

For further implementation details, please "Proposal - Standard Navigation"
in the wiki.

It happens that JRS manages keyboard input from the top down, and an attempt
was made to create a "KeyManager" class some time ago.  It has very limited
functionality, but it should provide a good place to attach the new Standard
Navigation object.  This proof-of-concept will then be moved into js-sdk.


2. Requirements
---------------
Third-party:
jquery.js
lo-dash.js (or underscore.js)
require.js

Jaspersoft:
(none)

Other TIBCO:
(none)


3. Initialization
-----------------
The library is provided as an AMD module suitable for Require.js; include as
usual.  The preferred name, and the name of the global object, is "stdnav".

4. Usage
--------
Once the document has otherwise finished loading, call:

stdnav.activate();

to determine the current focus and set appropriate keyboard and mouse input
event callbacks.  The library can then be used immediately.

Using the library with Backbone is straightforward, as focus and blur events
will fire as expected.  However, additional events for subfocus and subblur
are also available.  These should be exposed to Backbone in a future version,
to simplify implementation.

Please see the wiki article "Proposal - Standard Navigation" for more details.


5. API
------
(section pending)

6. Limitations
--------------
Multi-touch and standoff-detection events are not yet supported, as browser
support is still emerging for these touchscreen events.

