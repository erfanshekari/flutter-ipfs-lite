import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({Key? key}) : super(key: key);

  // This widget is the root of your application.
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        // This is the theme of your application.
        //
        // Try running your application with "flutter run". You'll see the
        // application has a blue toolbar. Then, without quitting the app, try
        // changing the primarySwatch below to Colors.green and then invoke
        // "hot reload" (press "r" in the console where you ran "flutter run",
        // or simply save your changes to "hot reload" in a Flutter IDE).
        // Notice that the counter didn't reset back to zero; the application
        // is not restarted.
        primarySwatch: Colors.blue,
      ),
      home: const App(),
    );
  }
}

class App extends StatelessWidget {
  const App({Key? key}) : super(key: key);

  static const platform = MethodChannel('ipfs.lite/node');

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            ElevatedButton(
              onPressed: () => _startNode(),
              child: const Text('init'),
            ),
            ElevatedButton(
              onPressed: () => _bootStrap(),
              child: const Text('bootstrap'),
            )
          ],
        ),
      ),
    );
  }

  Future<void> _startNode() async {
    try {
      final dynamic result = await platform.invokeMethod('init');
      print('result:');
      print(result);
      // final dynamic link = await platform.invokeMethod(
      //     'resolveLink', ['QmTs2oAH9kATpta9L94uQL27kPAKeMTEAxf27f9n46K3xn']);
      // print('link:');
      // print(link);
      // final dynamic file = await platform.invokeMethod(
      //     'getFile', ['QmTs2oAH9kATpta9L94uQL27kPAKeMTEAxf27f9n46K3xn']);
      // print('file:');
      // print(file);
    } on PlatformException catch (e) {
      print('PlatformException:');
      print(e);
    }
  }

  Future<void> _bootStrap() async {
    try {
      final dynamic result = await platform.invokeMethod('bootstrap');
      print('result:');
      print(result);
      // final dynamic link = await platform.invokeMethod(
      //     'resolveLink', ['QmTs2oAH9kATpta9L94uQL27kPAKeMTEAxf27f9n46K3xn']);
      // print('link:');
      // print(link);
      // final dynamic file = await platform.invokeMethod(
      //     'getFile', ['QmTs2oAH9kATpta9L94uQL27kPAKeMTEAxf27f9n46K3xn']);
      // print('file:');
      // print(file);
    } on PlatformException catch (e) {
      print('PlatformException:');
      print(e);
    }
  }
}
