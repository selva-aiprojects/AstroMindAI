import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:firebase_auth/firebase_auth.dart';

class AuthProvider with ChangeNotifier {
  final GoogleSignIn _googleSignIn = GoogleSignIn();
  final FirebaseAuth _auth = FirebaseAuth.instance;

  User? _user;
  bool _isLoading = false;
  String? _errorMessage;

  User? get user => _user;
  bool get isLoading => _isLoading;
  bool get isAuthenticated => _user != null;
  String? get errorMessage => _errorMessage;

  AuthProvider() {
    _checkAuthStatus();
  }

  void _checkAuthStatus() {
    _auth.authStateChanges().listen((User? user) {
      _user = user;
      notifyListeners();
    });
  }

  Future<bool> signInWithGoogle() async {
    _setLoading(true);
    _clearError();

    try {
      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) {
        _setLoading(false);
        return false;
      }

      final GoogleSignInAuthentication googleAuth = await googleUser.authentication;
      final AuthCredential credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      final UserCredential userCredential = await _auth.signInWithCredential(credential);
      _user = userCredential.user;
      
      // In production, call backend API to exchange for JWT token
      await _exchangeTokenWithBackend(userCredential.user!.uid, 'google');
      
      _setLoading(false);
      return true;
    } catch (e) {
      _setError('Google sign-in failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> signInWithPhone(String phoneNumber, String otp) async {
    _setLoading(true);
    _clearError();

    try {
      // In production, integrate with Firebase Phone Auth
      // For now, this is a placeholder
      await Future.delayed(const Duration(seconds: 2));
      
      // Simulate successful phone auth
      _setLoading(false);
      return true;
    } catch (e) {
      _setError('Phone sign-in failed: ${e.toString()}');
      _setLoading(false);
      return false;
    }
  }

  Future<void> sendOtp(String phoneNumber) async {
    _setLoading(true);
    _clearError();

    try {
      // In production, integrate with Firebase Phone Auth
      await Future.delayed(const Duration(seconds: 2));
      _setLoading(false);
    } catch (e) {
      _setError('Failed to send OTP: ${e.toString()}');
      _setLoading(false);
    }
  }

  Future<void> _exchangeTokenWithBackend(String userId, String provider) async {
    // In production, call backend API to exchange Firebase token for JWT
    // POST /api/v1/auth/google or /api/v1/auth/phone/verify-otp
    debugPrint('Exchanging token with backend for user: $userId, provider: $provider');
  }

  Future<void> signOut() async {
    _setLoading(true);
    try {
      await Future.wait([
        _googleSignIn.signOut(),
        _auth.signOut(),
      ]);
      _user = null;
      _setLoading(false);
    } catch (e) {
      _setError('Sign out failed: ${e.toString()}');
      _setLoading(false);
    }
  }

  void _setLoading(bool value) {
    _isLoading = value;
    notifyListeners();
  }

  void _setError(String message) {
    _errorMessage = message;
    notifyListeners();
  }

  void _clearError() {
    _errorMessage = null;
    notifyListeners();
  }
}
