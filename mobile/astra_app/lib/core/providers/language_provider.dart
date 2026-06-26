import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

/// Holds the user's selected language for AI responses and astro reports.
/// Persists the selection to SharedPreferences so it survives app restarts.
class LanguageProvider extends ChangeNotifier {
  static const String _prefKey = 'selected_language_code';

  static const List<AppLanguage> supportedLanguages = [
    AppLanguage(code: 'en', name: 'English',    nativeName: 'English',      flag: '🇬🇧'),
    AppLanguage(code: 'hi', name: 'Hindi',      nativeName: 'हिन्दी',        flag: '🇮🇳'),
    AppLanguage(code: 'ta', name: 'Tamil',      nativeName: 'தமிழ்',         flag: '🌐'),
    AppLanguage(code: 'te', name: 'Telugu',     nativeName: 'తెలుగు',        flag: '🌐'),
    AppLanguage(code: 'mr', name: 'Marathi',    nativeName: 'मराठी',         flag: '🌐'),
    AppLanguage(code: 'bn', name: 'Bengali',    nativeName: 'বাংলা',         flag: '🌐'),
    AppLanguage(code: 'gu', name: 'Gujarati',   nativeName: 'ગુજરાતી',       flag: '🌐'),
    AppLanguage(code: 'kn', name: 'Kannada',    nativeName: 'ಕನ್ನಡ',         flag: '🌐'),
    AppLanguage(code: 'ml', name: 'Malayalam',  nativeName: 'മലയാളം',        flag: '🌐'),
    AppLanguage(code: 'pa', name: 'Punjabi',    nativeName: 'ਪੰਜਾਬੀ',        flag: '🌐'),
    AppLanguage(code: 'or', name: 'Odia',       nativeName: 'ଓଡ଼ିଆ',          flag: '🌐'),
    AppLanguage(code: 'as', name: 'Assamese',   nativeName: 'অসমীয়া',        flag: '🌐'),
    AppLanguage(code: 'ur', name: 'Urdu',       nativeName: 'اردو',           flag: '🌐'),
    AppLanguage(code: 'sa', name: 'Sanskrit',   nativeName: 'संस्कृत',        flag: '🌐'),
  ];

  AppLanguage _selected = supportedLanguages.first;
  bool _isLoaded = false;

  LanguageProvider() {
    _loadSavedLanguage();
  }

  AppLanguage get selected => _selected;
  String get code => _selected.code;
  String get displayName => _selected.nativeName;
  bool get isLoaded => _isLoaded;

  /// Loads the persisted language code from SharedPreferences on startup.
  Future<void> _loadSavedLanguage() async {
    final prefs = await SharedPreferences.getInstance();
    final savedCode = prefs.getString(_prefKey);
    if (savedCode != null) {
      final lang = supportedLanguages.firstWhere(
        (l) => l.code == savedCode,
        orElse: () => supportedLanguages.first,
      );
      _selected = lang;
    }
    _isLoaded = true;
    notifyListeners();
  }

  /// Sets the selected language and persists it to SharedPreferences.
  Future<void> setLanguage(AppLanguage lang) async {
    if (_selected.code == lang.code) return;
    _selected = lang;
    notifyListeners();

    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(_prefKey, lang.code);
  }

  /// Sets the selected language by its code string.
  Future<void> setLanguageByCode(String code) async {
    final lang = supportedLanguages.firstWhere(
      (l) => l.code == code,
      orElse: () => supportedLanguages.first,
    );
    await setLanguage(lang);
  }
}

class AppLanguage {
  final String code;
  final String name;
  final String nativeName;
  final String flag;

  const AppLanguage({
    required this.code,
    required this.name,
    required this.nativeName,
    required this.flag,
  });

  @override
  bool operator ==(Object other) =>
      identical(this, other) || other is AppLanguage && other.code == code;

  @override
  int get hashCode => code.hashCode;
}
