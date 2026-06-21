import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:geolocator/geolocator.dart';

import '../../../core/network/api_client.dart';
import '../../../core/providers/auth_provider.dart';

class BirthProfileOnboarding extends StatefulWidget {
  const BirthProfileOnboarding({super.key});

  @override
  State<BirthProfileOnboarding> createState() => _BirthProfileOnboardingState();
}

class _BirthProfileOnboardingState extends State<BirthProfileOnboarding> {
  final PageController _pageController = PageController();
  int _currentStep = 0;

  // Form data
  final TextEditingController _nameController = TextEditingController();
  String _selectedGender = 'Male';
  DateTime? _selectedDate;
  TimeOfDay? _selectedTime;
  final TextEditingController _placeController = TextEditingController();
  double? _latitude;
  double? _longitude;
  String _timezone = 'UTC';
  bool _isSaving = false;

  final List<String> _genders = ['Male', 'Female', 'Other'];

  final Map<String, List<double>> _cityCoordinates = {
    'new delhi': [28.6139, 77.2090],
    'delhi': [28.6139, 77.2090],
    'mumbai': [19.0760, 72.8777],
    'bombay': [19.0760, 72.8777],
    'bangalore': [12.9716, 77.5946],
    'bengaluru': [12.9716, 77.5946],
    'chennai': [13.0827, 80.2707],
    'madras': [13.0827, 80.2707],
    'kolkata': [22.5726, 88.3639],
    'calcutta': [22.5726, 88.3639],
    'hyderabad': [17.3850, 78.4867],
    'pune': [18.5204, 73.8567],
    'ahmedabad': [23.0225, 72.5714],
    'jaipur': [26.9124, 75.7873],
    'lucknow': [26.8467, 80.9462],
    'patna': [25.5941, 85.1376],
    'bhopal': [23.2599, 77.4126],
    'indore': [22.7196, 75.8577],
    'chandigarh': [30.7333, 76.7794],
    'london': [51.5074, -0.1278],
    'new york': [40.7128, -74.0060],
    'san francisco': [37.7749, -122.4194],
  };

  @override
  void initState() {
    super.initState();
    _timezone = _getValidTimezone();
  }

  String _getValidTimezone() {
    final offset = DateTime.now().timeZoneOffset;
    if (offset.inMinutes == 330) {
      return 'Asia/Kolkata';
    }
    if (offset.inMinutes == 0) {
      return 'UTC';
    }
    final sign = offset.isNegative ? '-' : '+';
    final hours = offset.inHours.abs().toString().padLeft(2, '0');
    final minutes = (offset.inMinutes.abs() % 60).toString().padLeft(2, '0');
    return 'GMT$sign$hours:$minutes';
  }

  void _resolveCityCoordinates() {
    final text = _placeController.text.trim().toLowerCase();
    if (_cityCoordinates.containsKey(text)) {
      final coords = _cityCoordinates[text]!;
      _latitude = coords[0];
      _longitude = coords[1];
    } else {
      _latitude ??= 28.6139;
      _longitude ??= 77.2090;
    }
  }

  @override
  void dispose() {
    _pageController.dispose();
    _nameController.dispose();
    _placeController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFFF3E0),
      appBar: AppBar(
        backgroundColor: Colors.white,
        elevation: 0,
        leading: IconButton(
          icon: const Icon(Icons.arrow_back, color: Color(0xFF3E2723)),
          onPressed: () => Navigator.pop(context),
        ),
        title: Text(
          'Create Birth Profile',
          style: GoogleFonts.playfairDisplay(
            fontWeight: FontWeight.w700,
            color: const Color(0xFF3E2723),
            fontSize: 22,
          ),
        ),
        centerTitle: true,
      ),
      body: Column(
        children: [
          _buildProgressIndicator(),
          Expanded(
            child: PageView(
              controller: _pageController,
              onPageChanged: (index) {
                setState(() {
                  _currentStep = index;
                });
              },
              children: [
                _buildNameStep(),
                _buildGenderStep(),
                _buildDateStep(),
                _buildTimeStep(),
                _buildPlaceStep(),
                _buildSummaryStep(),
              ],
            ),
          ),
          _buildNavigationButtons(),
        ],
      ),
    );
  }

  Widget _buildProgressIndicator() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 20),
      child: Row(
        children: List.generate(6, (index) {
          return Expanded(
            child: Container(
              margin: EdgeInsets.only(right: index < 5 ? 8 : 0),
              height: 6,
              decoration: BoxDecoration(
                gradient: index <= _currentStep
                    ? const LinearGradient(
                        colors: [Color(0xFFE65100), Color(0xFFFFB300)],
                        begin: Alignment.centerLeft,
                        end: Alignment.centerRight,
                      )
                    : null,
                color: index > _currentStep ? Colors.grey.shade300 : null,
                borderRadius: BorderRadius.circular(3),
              ),
            ),
          );
        }),
      ),
    );
  }

  Widget _buildNameStep() {
    return _buildStep(
      title: 'What\'s your name?',
      subtitle: 'This helps us personalize your astrology readings',
      icon: Icons.person,
      child: Column(
        children: [
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: TextField(
              controller: _nameController,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'Enter your full name',
                prefixIcon: const Icon(Icons.person, color: Color(0xFFFFB300)),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: BorderSide.none,
                ),
                filled: true,
                fillColor: Colors.white,
                contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              ),
              style: GoogleFonts.inter(fontSize: 16),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGenderStep() {
    return _buildStep(
      title: 'What\'s your gender?',
      subtitle: 'Gender influences certain astrological calculations',
      icon: Icons.wc,
      child: Column(
        children: _genders.map((gender) {
          final isSelected = _selectedGender == gender;
          return Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: GestureDetector(
              onTap: () {
                setState(() {
                  _selectedGender = gender;
                });
              },
              child: Container(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                decoration: BoxDecoration(
                  color: isSelected ? const Color(0xFFE65100) : Colors.white,
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(
                    color: isSelected ? const Color(0xFFE65100) : const Color(0xFFE5DED2),
                  ),
                  boxShadow: isSelected
                      ? [
                          BoxShadow(
                            color: const Color(0xFFE65100).withOpacity(0.2),
                            blurRadius: 12,
                            offset: const Offset(0, 4),
                          ),
                        ]
                      : [
                          BoxShadow(
                            color: Colors.black.withOpacity(0.04),
                            blurRadius: 8,
                            offset: const Offset(0, 2),
                          ),
                        ],
                ),
                child: Row(
                  children: [
                    Icon(
                      gender == 'Male' ? Icons.male : gender == 'Female' ? Icons.female : Icons.transgender,
                      color: isSelected ? Colors.white : const Color(0xFFFFB300),
                      size: 24,
                    ),
                    const SizedBox(width: 16),
                    Text(
                      gender,
                      style: GoogleFonts.inter(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                        color: isSelected ? Colors.white : const Color(0xFF3E2723),
                      ),
                    ),
                  ],
                ),
              ),
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildDateStep() {
    return _buildStep(
      title: 'When were you born?',
      subtitle: 'Birth date is essential for accurate chart calculations',
      icon: Icons.calendar_today,
      child: Column(
        children: [
          GestureDetector(
            onTap: _selectDate,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFFE5DED2)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.04),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFFFB300).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(Icons.calendar_today, color: Color(0xFFFFB300)),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      _selectedDate != null
                          ? DateFormat('MMMM d, yyyy').format(_selectedDate!)
                          : 'Select your birth date',
                      style: GoogleFonts.inter(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: _selectedDate != null ? const Color(0xFF3E2723) : const Color(0xFF6F6A7A),
                      ),
                    ),
                  ),
                  const Icon(Icons.chevron_right, color: Color(0xFF6F6A7A)),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTimeStep() {
    return _buildStep(
      title: 'What time were you born?',
      subtitle: 'Exact time is crucial for precise planetary positions',
      icon: Icons.access_time,
      child: Column(
        children: [
          GestureDetector(
            onTap: _selectTime,
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 20),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: const Color(0xFFE5DED2)),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.04),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Row(
                children: [
                  Container(
                    padding: const EdgeInsets.all(12),
                    decoration: BoxDecoration(
                      color: const Color(0xFFE0A640).withOpacity(0.1),
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: const Icon(Icons.access_time, color: Color(0xFFE0A640)),
                  ),
                  const SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      _selectedTime != null
                          ? _selectedTime!.format(context)
                          : 'Select your birth time',
                      style: GoogleFonts.inter(
                        fontSize: 16,
                        fontWeight: FontWeight.w500,
                        color: _selectedTime != null ? const Color(0xFF3E2723) : const Color(0xFF6F6A7A),
                      ),
                    ),
                  ),
                  const Icon(Icons.chevron_right, color: Color(0xFF6F6A7A)),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              color: const Color(0xFFE0A640).withOpacity(0.1),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                const Icon(Icons.info_outline, color: Color(0xFFE0A640), size: 20),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Exact time ensures 95%+ accuracy in your birth chart',
                    style: GoogleFonts.inter(
                      fontSize: 13,
                      fontWeight: FontWeight.w500,
                      color: const Color(0xFF3E2723),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPlaceStep() {
    return _buildStep(
      title: 'Where were you born?',
      subtitle: 'Location determines the ascendant and house positions',
      icon: Icons.location_on,
      child: Column(
        children: [
          Container(
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: TextField(
              controller: _placeController,
              onChanged: (_) => setState(() {}),
              decoration: InputDecoration(
                hintText: 'Enter your birth place',
                prefixIcon: const Icon(Icons.location_on, color: Color(0xFFFFB300)),
                suffixIcon: IconButton(
                  icon: const Icon(Icons.my_location, color: Color(0xFFE65100)),
                  onPressed: _getCurrentLocation,
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(16),
                  borderSide: BorderSide.none,
                ),
                filled: true,
                fillColor: Colors.white,
                contentPadding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
              ),
              style: GoogleFonts.inter(fontSize: 16),
            ),
          ),
          const SizedBox(height: 16),
          if (_latitude != null && _longitude != null)
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
              decoration: BoxDecoration(
                color: const Color(0xFFFFB300).withOpacity(0.1),
                borderRadius: BorderRadius.circular(12),
              ),
              child: Row(
                children: [
                  const Icon(Icons.check_circle, color: Color(0xFFFFB300), size: 20),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Text(
                      'Location captured: ${_latitude!.toStringAsFixed(4)}, ${_longitude!.toStringAsFixed(4)}',
                      style: GoogleFonts.inter(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: const Color(0xFF3E2723),
                      ),
                    ),
                  ),
                ],
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSummaryStep() {
    return _buildStep(
      title: 'Review your details',
      subtitle: 'Confirm your birth information before creating your profile',
      icon: Icons.check_circle,
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(
            padding: const EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: const Color(0xFFE5DED2)),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.04),
                  blurRadius: 12,
                  offset: const Offset(0, 4),
                ),
              ],
            ),
            child: Column(
              children: [
                _buildSummaryRow('Name', _nameController.text, Icons.person),
                const Divider(height: 24),
                _buildSummaryRow('Gender', _selectedGender, Icons.wc),
                const Divider(height: 24),
                _buildSummaryRow(
                  'Date',
                  _selectedDate != null
                      ? DateFormat('MMMM d, yyyy').format(_selectedDate!)
                      : 'Not selected',
                  Icons.calendar_today,
                ),
                const Divider(height: 24),
                _buildSummaryRow(
                  'Time',
                  _selectedTime != null
                      ? _selectedTime!.format(context)
                      : 'Not selected',
                  Icons.access_time,
                ),
                const Divider(height: 24),
                _buildSummaryRow('Place', _placeController.text, Icons.location_on),
              ],
            ),
          ),
          const SizedBox(height: 20),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: [const Color(0xFFE65100).withOpacity(0.1), const Color(0xFFFFB300).withOpacity(0.1)],
              ),
              borderRadius: BorderRadius.circular(12),
            ),
            child: Row(
              children: [
                const Icon(Icons.auto_awesome, color: Color(0xFFE65100), size: 20),
                const SizedBox(width: 12),
                Expanded(
                  child: Text(
                    'Your profile will be used to generate 95%+ accurate birth charts',
                    style: GoogleFonts.inter(
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                      color: const Color(0xFF3E2723),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, String value, IconData icon) {
    return Row(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Icon(icon, size: 20, color: const Color(0xFFFFB300)),
        const SizedBox(width: 12),
        SizedBox(
          width: 80,
          child: Text(
            label,
            style: GoogleFonts.inter(
              fontWeight: FontWeight.w600,
              color: const Color(0xFF6F6A7A),
              fontSize: 14,
            ),
          ),
        ),
        Expanded(
          child: Text(
            value,
            style: GoogleFonts.inter(
              fontWeight: FontWeight.w600,
              color: const Color(0xFF3E2723),
              fontSize: 15,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildStep({required String title, String? subtitle, IconData? icon, required Widget child}) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          if (icon != null) ...[
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [const Color(0xFFE65100), const Color(0xFFFFB300)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: Colors.white, size: 32),
            ),
            const SizedBox(height: 24),
          ],
          Text(
            title,
            style: GoogleFonts.playfairDisplay(
              fontSize: 32,
              fontWeight: FontWeight.w800,
              color: const Color(0xFF3E2723),
            ),
          ),
          if (subtitle != null) ...[
            const SizedBox(height: 8),
            Text(
              subtitle,
              style: GoogleFonts.inter(
                fontSize: 15,
                fontWeight: FontWeight.w500,
                color: const Color(0xFF6F6A7A),
                height: 1.5,
              ),
            ),
          ],
          const SizedBox(height: 32),
          Expanded(child: child),
        ],
      ),
    );
  }

  Widget _buildNavigationButtons() {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 20,
            offset: const Offset(0, -4),
          ),
        ],
      ),
      child: Row(
        children: [
          if (_currentStep > 0)
            Expanded(
              child: OutlinedButton(
                onPressed: _previousStep,
                style: OutlinedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  side: const BorderSide(color: Color(0xFFE5DED2)),
                ),
                child: Text(
                  'Back',
                  style: GoogleFonts.inter(
                    fontWeight: FontWeight.w600,
                    fontSize: 16,
                    color: const Color(0xFF3E2723),
                  ),
                ),
              ),
            ),
          if (_currentStep > 0) const SizedBox(width: 16),
          Expanded(
            child: ElevatedButton(
              onPressed: _canProceed && !_isSaving ? _nextStep : null,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                backgroundColor: const Color(0xFFE65100),
                foregroundColor: Colors.white,
                disabledBackgroundColor: const Color(0xFFE65100).withOpacity(0.5),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                elevation: 0,
              ),
              child: Text(
                _isSaving
                    ? 'Saving...'
                    : _currentStep == 5
                    ? 'Create Profile'
                    : 'Continue',
                style: GoogleFonts.inter(
                  fontWeight: FontWeight.w700,
                  fontSize: 16,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  bool get _canProceed {
    switch (_currentStep) {
      case 0:
        return _nameController.text.isNotEmpty;
      case 1:
        return _selectedGender.isNotEmpty;
      case 2:
        return _selectedDate != null;
      case 3:
        return _selectedTime != null;
      case 4:
        return _placeController.text.isNotEmpty;
      case 5:
        return true;
      default:
        return false;
    }
  }

  void _nextStep() {
    if (_currentStep == 4) {
      _resolveCityCoordinates();
    }
    if (_currentStep < 5) {
      _pageController.nextPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    } else {
      _createProfile();
    }
  }

  void _previousStep() {
    if (_currentStep > 0) {
      _pageController.previousPage(
        duration: const Duration(milliseconds: 300),
        curve: Curves.easeInOut,
      );
    }
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: DateTime.now().subtract(const Duration(days: 365 * 25)),
      firstDate: DateTime(1900),
      lastDate: DateTime.now(),
    );
    if (picked != null && mounted) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Future<void> _selectTime() async {
    final TimeOfDay? picked = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.now(),
    );
    if (picked != null && mounted) {
      setState(() {
        _selectedTime = picked;
      });
    }
  }

  Future<void> _getCurrentLocation() async {
    setState(() => _isSaving = true);
    try {
      bool serviceEnabled = await Geolocator.isLocationServiceEnabled();
      if (!serviceEnabled) {
        _showError('Location services are disabled.');
        return;
      }

      LocationPermission permission = await Geolocator.checkPermission();
      if (permission == LocationPermission.denied) {
        permission = await Geolocator.requestPermission();
        if (permission == LocationPermission.denied) {
          _showError('Location permissions are denied.');
          return;
        }
      }
      
      if (permission == LocationPermission.deniedForever) {
        _showError('Location permissions are permanently denied.');
        return;
      }

      final Position position = await Geolocator.getCurrentPosition(
        desiredAccuracy: LocationAccuracy.high,
      );

      setState(() {
        _latitude = position.latitude;
        _longitude = position.longitude;
        _placeController.text = 'My Location (${position.latitude.toStringAsFixed(4)}, ${position.longitude.toStringAsFixed(4)})';
      });
    } catch (e) {
      _showError('Failed to get current location: $e');
    } finally {
      setState(() => _isSaving = false);
    }
  }

  Future<void> _createProfile() async {
    final authProvider = context.read<AuthProvider>();
    final userId = authProvider.backendUserId;

    if (userId == null) {
      _showError('Please sign in again before creating a profile.');
      return;
    }

    setState(() => _isSaving = true);

    try {
      final formattedDate = DateFormat('yyyy-MM-dd').format(_selectedDate!);
      final formattedTime = '${_selectedTime!.hour.toString().padLeft(2, '0')}:${_selectedTime!.minute.toString().padLeft(2, '0')}:00';
      
      final lat = _latitude ?? 28.6139;
      final lng = _longitude ?? 77.2090;

      // Call the backend API
      final apiClient = context.read<ApiClient>();
      await apiClient.createBirthProfile(
        userId: userId,
        fullName: _nameController.text,
        gender: _selectedGender,
        birthDate: formattedDate,
        birthTime: formattedTime,
        birthPlace: _placeController.text,
        latitude: lat,
        longitude: lng,
        timezone: _timezone,
      );
      
      // Save profile data locally using SharedPreferences
      final prefs = await SharedPreferences.getInstance();
      await prefs.setString('birth_profile_name', _nameController.text);
      await prefs.setString('birth_profile_gender', _selectedGender);
      await prefs.setString('birth_profile_date', formattedDate);
      await prefs.setString('birth_profile_time', formattedTime);
      await prefs.setString('birth_profile_place', _placeController.text);
      await prefs.setDouble('birth_profile_latitude', lat);
      await prefs.setDouble('birth_profile_longitude', lng);
      await prefs.setString('birth_profile_timezone', _timezone);

      if (mounted) {
        Navigator.pushReplacementNamed(context, '/home');
      }
    } catch (error) {
      _showError('Failed to create profile: ${ApiClient.describeError(error)}');
    } finally {
      if (mounted) {
        setState(() => _isSaving = false);
      }
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(content: Text(message), backgroundColor: Colors.red.shade700),
    );
  }
}
