import 'package:flutter/foundation.dart';
import 'package:google_sign_in/google_sign_in.dart';
import 'package:firebase_auth/firebase_auth.dart';
import 'package:shared_preferences/shared_preferences.dart';

import '../network/api_client.dart';

class AuthProvider with ChangeNotifier {
  AuthProvider({required ApiClient apiClient, bool firebaseEnabled = false})
    : _apiClient = apiClient,
      _auth = firebaseEnabled ? FirebaseAuth.instance : null {
    _checkAuthStatus();
    _loadSession();
  }

  final GoogleSignIn _googleSignIn = GoogleSignIn(
    clientId: const String.fromEnvironment(
      'GOOGLE_CLIENT_ID', 
      // This placeholder prevents the red screen assertion error on Web.
      // To get your real Client ID, go to Firebase Console -> Authentication -> Sign-in method -> Google -> Web SDK configuration.
      defaultValue: 'YOUR_WEB_CLIENT_ID.apps.googleusercontent.com',
    ),
  );
  final ApiClient _apiClient;
  final FirebaseAuth? _auth;

  User? _user;
  bool _isLoading = false;
  String? _errorMessage;
  String? _backendToken;
  String? _backendUserId;

  User? get user => _user;
  bool get isLoading => _isLoading;
  bool get isAuthenticated => _backendToken != null;
  String? get errorMessage => _errorMessage;
  String? get backendToken => _backendToken;
  String? get backendUserId => _backendUserId;

  void _checkAuthStatus() {
    final auth = _auth;
    if (auth == null) {
      return;
    }

    auth.authStateChanges().listen((User? user) {
      _user = user;
      notifyListeners();
    });
  }

  Future<bool> signInWithGoogle() async {
    _setLoading(true);
    _clearError();

    try {
      final auth = _auth;
      if (auth == null) {
        _setError('Firebase is not configured for this app yet.');
        _setLoading(false);
        return false;
      }

      final GoogleSignInAccount? googleUser = await _googleSignIn.signIn();
      if (googleUser == null) {
        _setLoading(false);
        return false;
      }

      final GoogleSignInAuthentication googleAuth =
          await googleUser.authentication;
      final AuthCredential credential = GoogleAuthProvider.credential(
        accessToken: googleAuth.accessToken,
        idToken: googleAuth.idToken,
      );

      final UserCredential userCredential = await auth.signInWithCredential(
        credential,
      );
      _user = userCredential.user;

      final firebaseUser = userCredential.user!;
      final response = await _apiClient.authenticateWithGoogle(
        googleId: firebaseUser.uid,
        email: firebaseUser.email ?? googleUser.email,
        name:
            firebaseUser.displayName ?? googleUser.displayName ?? 'Google User',
      );
      await _saveBackendSession(response['token']?.toString());

      _setLoading(false);
      return true;
    } catch (e) {
      _setError('Google sign-in failed: ${ApiClient.describeError(e)}');
      _setLoading(false);
      return false;
    }
  }

  Future<bool> signInWithPhone(String phoneNumber, String otp) async {
    _setLoading(true);
    _clearError();

    try {
      final response = await _apiClient.verifyOtp(phone: phoneNumber, otp: otp);
      await _saveBackendSession(response['token']?.toString());
      _setLoading(false);
      return true;
    } catch (e) {
      _setError('Phone sign-in failed: ${ApiClient.describeError(e)}');
      _setLoading(false);
      return false;
    }
  }

  Future<void> sendOtp(String phoneNumber) async {
    _setLoading(true);
    _clearError();

    try {
      await _apiClient.sendOtp(phoneNumber);
      _setLoading(false);
    } catch (e) {
      _setError('Failed to send OTP: ${ApiClient.describeError(e)}');
      _setLoading(false);
    }
  }

  Future<bool> signInDemo() async {
    _setLoading(true);
    _clearError();

    try {
      // Call backend demo endpoint for valid JWT token
      final response = await _apiClient.demoLogin();
      await _saveBackendSession(response['token']?.toString());
      
      _setLoading(false);
      return true;
    } catch (e) {
      _setError('Demo login failed: ${ApiClient.describeError(e)}');
      _setLoading(false);
      return false;
    }
  }

  Future<void> signOut() async {
    _setLoading(true);
    try {
      final auth = _auth;
      await Future.wait([
        _googleSignIn.signOut(),
        if (auth != null) auth.signOut(),
      ]);
      _user = null;
      await _saveBackendSession(null);
      _setLoading(false);
    } catch (e) {
      _setError('Sign out failed: ${ApiClient.describeError(e)}');
      _setLoading(false);
    }
  }

  Future<void> _loadSession() async {
    final preferences = await SharedPreferences.getInstance();
    final token = preferences.getString('backendToken');
    if (token == null) {
      return;
    }

    _backendToken = token;
    _backendUserId =
        preferences.getString('backendUserId') ??
        ApiClient.readUserIdFromJwt(token);
    _apiClient.setAuthToken(token);
    notifyListeners();
  }

  Future<void> _saveBackendSession(String? token) async {
    final preferences = await SharedPreferences.getInstance();
    _backendToken = token;
    _backendUserId = token == null ? null : ApiClient.readUserIdFromJwt(token);
    _apiClient.setAuthToken(token);

    if (token == null) {
      await preferences.remove('backendToken');
      await preferences.remove('backendUserId');
    } else {
      await preferences.setString('backendToken', token);
      if (_backendUserId != null) {
        await preferences.setString('backendUserId', _backendUserId!);
      }
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
