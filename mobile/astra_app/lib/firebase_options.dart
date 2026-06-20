import 'package:firebase_core/firebase_core.dart';
import 'package:flutter/foundation.dart';

class DefaultFirebaseOptions {
  static FirebaseOptions get currentPlatform {
    if (kIsWeb) {
      return web;
    }

    return web;
  }

  static const FirebaseOptions web = FirebaseOptions(
    apiKey: 'AIzaSyAWKXiFqyq31UH48QZIkWrRQ553QRwJows',
    authDomain: 'education-apps-f2032.firebaseapp.com',
    projectId: 'education-apps-f2032',
    storageBucket: 'education-apps-f2032.firebasestorage.app',
    messagingSenderId: '273415327681',
    appId: '1:273415327681:web:7e583c24de46aecf422940',
    measurementId: 'G-3KBE51BG66',
  );
}
