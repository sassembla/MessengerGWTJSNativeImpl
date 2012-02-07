<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="Content-Style-Type" content="text/css">
  <title></title>
  <meta name="Generator" content="Cocoa HTML Writer">
  <meta name="CocoaVersion" content="1138.23">
  <style type="text/css">
    p.p1 {margin: 0.0px 0.0px 0.0px 0.0px; font: 16.0px 'Hiragino Kaku Gothic ProN'}
    p.p2 {margin: 0.0px 0.0px 0.0px 0.0px; font: 12.0px 'Hiragino Kaku Gothic ProN'; min-height: 18.0px}
    p.p3 {margin: 0.0px 0.0px 0.0px 0.0px; font: 12.0px 'Hiragino Kaku Gothic ProN'}
    span.Apple-tab-span {white-space:pre}
  </style>
</head>
<body>
<p class="p1"><b>MessengerGWTJSNativeImpl</b></p>
<p class="p2"><b></b><br></p>
<p class="p3"><b>概要</b></p>
<p class="p3"><span class="Apple-tab-span">	</span>GWTで構築したJSでのMessagingに対して、単純なJSから通信を行う試みです。</p>
<p class="p3"><span class="Apple-tab-span">	</span>下記のプロジェクトが元になっています。</p>
<p class="p3"><span class="Apple-tab-span">	</span>・MessengerSystem</p>
<p class="p3"><span class="Apple-tab-span">	</span><span class="Apple-tab-span">	</span>https://github.com/sassembla/MessengerSystem</p>
<p class="p3"><span class="Apple-tab-span">	</span><span class="Apple-tab-span">	</span>Objective-Cでのメッセージングをハンドルしやすいようにラップしたライブラリ</p>
<p class="p2"><span class="Apple-tab-span">	</span><span class="Apple-tab-span">	</span></p>
<p class="p3"><span class="Apple-tab-span">	</span>・MessengerGWT</p>
<p class="p3"><span class="Apple-tab-span">	</span><span class="Apple-tab-span">	</span>https://github.com/sassembla/MessengerGWT</p>
<p class="p3"><span class="Apple-tab-span">	</span><span class="Apple-tab-span">	</span>Browser-Javascriptでのメッセージングをハンドルしやすいようにラップしたライブラリ</p>
<p class="p2"><br></p>
<p class="p3"><b>目的</b></p>
<p class="p3"><span class="Apple-tab-span">	</span>既存のJSライブラリから、Messagingでの他ライブラリとの通信を行う事。</p>
<p class="p3"><span class="Apple-tab-span">	</span>ライブラリ間の粗結合を実現するためのブリッジを、PureJSで作っている、という状態になります。</p>
<p class="p2"><b></b><br></p>
<p class="p2"><br></p>
<p class="p3"><b>現在の状態</b></p>
<p class="p3"><span class="Apple-tab-span">	</span>試験中です。</p>
<p class="p2"><br></p>
<p class="p3"><span class="Apple-tab-span">	</span>完成予定は未定。</p>
<p class="p2"><br></p>
<p class="p3"><span class="Apple-tab-span">	</span>MessengerGWTからのメッセージを受け付けることは完了。</p>
<p class="p3"><span class="Apple-tab-span">	</span>MessengerGWTへとメッセージを送付することは完了。</p>
<p class="p2"><span class="Apple-tab-span">	</span></p>
</body>
</html>
