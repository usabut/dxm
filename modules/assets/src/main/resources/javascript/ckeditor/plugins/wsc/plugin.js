﻿/*
 Copyright (c) 2003-2012, CKSource - Frederico Knabben. All rights reserved.
 For licensing, see LICENSE.html or http://ckeditor.com/license
*/
CKEDITOR.plugins.add("wsc",{requires:"dialog",lang:"af,ar,bg,bn,bs,ca,cs,cy,da,de,el,en-au,en-ca,en-gb,en,eo,es,et,eu,fa,fi,fo,fr-ca,fr,gl,gu,he,hi,hr,hu,is,it,ja,ka,km,ko,lt,lv,mk,mn,ms,nb,nl,no,pl,pt-br,pt,ro,ru,sk,sl,sr-latn,sr,sv,th,tr,ug,uk,vi,zh-cn,zh",icons:"spellchecker",init:function(a){a.addCommand("checkspell",new CKEDITOR.dialogCommand("checkspell")).modes={wysiwyg:!CKEDITOR.env.opera&&!CKEDITOR.env.air&&document.domain==window.location.hostname};"undefined"==typeof a.plugins.scayt&&a.ui.addButton&&
a.ui.addButton("SpellChecker",{label:a.lang.wsc.toolbar,command:"checkspell",toolbar:"spellchecker,10"});if(CKEDITOR.env.ie&&8>=CKEDITOR.env.version)a="dialogs/wsc_ie.js";else if(window.postMessage){var a="dialogs/wsc.js",b=document.location.protocol||"http:";CKEDITOR.scriptLoader.load(CKEDITOR.config.wsc_customLoaderScript||b+"//loader.webspellchecker.net/sproxy_fck/sproxy.php?plugin=fck2&customerid="+CKEDITOR.config.wsc_customerId+"&cmd=script&doc=wsc&schema=22")}else a="dialogs/wsc_ie.js";CKEDITOR.dialog.add("checkspell",
this.path+a)}});CKEDITOR.config.wsc_customerId=CKEDITOR.config.wsc_customerId||"1:ua3xw1-2XyGJ3-GWruD3-6OFNT1-oXcuB1-nR6Bp4-hgQHc-EcYng3-sdRXG3-NOfFk";CKEDITOR.config.wsc_customDictionaryIds=CKEDITOR.config.wsc_customDictionaryIds||"";CKEDITOR.config.wsc_userDictionaryName=CKEDITOR.config.wsc_userDictionaryName||"";CKEDITOR.config.wsc_cmd=CKEDITOR.config.wsc_cmd||"spell";