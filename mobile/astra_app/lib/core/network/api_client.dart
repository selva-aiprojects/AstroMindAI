import 'dart:convert';

import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';

class ApiClient {
  ApiClient({String? baseUrl})
    : baseUrl = baseUrl ?? _defaultBaseUrl,
      _dio = Dio(
        BaseOptions(
          baseUrl: baseUrl ?? _defaultBaseUrl,
          connectTimeout: const Duration(seconds: 120),
          receiveTimeout: const Duration(seconds: 120),
          headers: {'Content-Type': 'application/json'},
        ),
      );

  static const String _configuredBaseUrl = String.fromEnvironment(
    'BACKEND_BASE_URL',
  );

  static String get _defaultBaseUrl {
    if (_configuredBaseUrl.isNotEmpty) {
      return _configuredBaseUrl;
    }
    // Hardcoding the production Render URL for the APK build
    return 'https://astromindai-backend.onrender.com/api/v1';
  }

  final String baseUrl;
  final Dio _dio;

  void setAuthToken(String? token) {
    if (token == null || token.isEmpty) {
      _dio.options.headers.remove('Authorization');
      return;
    }

    _dio.options.headers['Authorization'] = 'Bearer $token';
  }

  Future<Map<String, dynamic>> health() async {
    final response = await _dio.get<Map<String, dynamic>>('/health');
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> authenticateWithGoogle({
    required String googleId,
    required String email,
    required String name,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/google',
      data: {'googleId': googleId, 'email': email, 'name': name},
    );
    return response.data ?? {};
  }

  Future<void> sendOtp(String phone) async {
    await _dio.post<Map<String, dynamic>>(
      '/auth/phone/send-otp',
      data: {'phone': phone},
    );
  }

  Future<Map<String, dynamic>> verifyOtp({
    required String phone,
    required String otp,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/phone/verify-otp',
      data: {'phone': phone, 'otp': otp},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> createBirthProfile({
    required String userId,
    required String fullName,
    required String gender,
    required String birthDate,
    required String birthTime,
    required String birthPlace,
    required double latitude,
    required double longitude,
    required String timezone,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/users/birth-profile',
      data: {
        'user': {'userId': userId},
        'fullName': fullName,
        'gender': gender.toUpperCase(),
        'birthDate': birthDate,
        'birthTime': birthTime,
        'birthLatitude': latitude,
        'birthLongitude': longitude,
        'birthPlace': birthPlace,
        'timezone': timezone,
        'ayanamsa': 'LAHIRI',
      },
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> sendChatMessage({
    required String userId,
    required String query,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/astrology/chat',
      data: {'userId': userId, 'query': query},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> registerWithEmail({
    required String email,
    required String password,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/register',
      data: {'email': email, 'password': password},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> loginWithEmail({
    required String email,
    required String password,
  }) async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: {'email': email, 'password': password},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> demoLogin() async {
    final response = await _dio.post<Map<String, dynamic>>(
      '/auth/demo',
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getBirthChart(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/astrology/birth-chart',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getBirthProfile(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/users/birth-profile',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getLifeSummary(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/astrology/life-summary',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getDashaTimeline(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/astrology/dasha-timeline',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getTransits(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/astrology/transits',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  Future<Map<String, dynamic>> getDailyHoroscope(String userId) async {
    final response = await _dio.get<Map<String, dynamic>>(
      '/astrology/daily-horoscope',
      queryParameters: {'userId': userId},
    );
    return response.data ?? {};
  }

  static String? readUserIdFromJwt(String token) {
    final parts = token.split('.');
    if (parts.length != 3) {
      return null;
    }

    final payload = utf8.decode(
      base64Url.decode(base64Url.normalize(parts[1])),
    );
    final data = jsonDecode(payload) as Map<String, dynamic>;
    return data['userId'] as String?;
  }

  static String describeError(Object error) {
    if (error is DioException) {
      final data = error.response?.data;
      if (data is Map && data['error'] != null) {
        return data['error'].toString();
      }
      if (data is Map && data['message'] != null) {
        return data['message'].toString();
      }
      if (error.type == DioExceptionType.connectionError) {
        return 'Cannot reach backend. Start the API server or set BACKEND_BASE_URL.';
      }
      return error.message ?? 'Backend request failed';
    }

    return error.toString();
  }
}
