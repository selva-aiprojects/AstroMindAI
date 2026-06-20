import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';

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

  final List<String> _genders = ['Male', 'Female', 'Other'];

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
      appBar: AppBar(
        title: Text(
          'Create Your Birth Profile',
          style: GoogleFonts.inter(fontWeight: FontWeight.w600),
        ),
        elevation: 0,
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
      padding: const EdgeInsets.all(16),
      child: Row(
        children: List.generate(6, (index) {
          return Expanded(
            child: Container(
              margin: EdgeInsets.only(right: index < 5 ? 8 : 0),
              height: 4,
              decoration: BoxDecoration(
                color: index <= _currentStep
                    ? Theme.of(context).colorScheme.primary
                    : Colors.grey.shade300,
                borderRadius: BorderRadius.circular(2),
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
      child: Column(
        children: [
          TextField(
            controller: _nameController,
            decoration: InputDecoration(
              hintText: 'Enter your full name',
              prefixIcon: const Icon(Icons.person),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildGenderStep() {
    return _buildStep(
      title: 'What\'s your gender?',
      child: Column(
        children: _genders.map((gender) {
          return Padding(
            padding: const EdgeInsets.only(bottom: 12),
            child: RadioListTile<String>(
              title: Text(gender),
              value: gender,
              groupValue: _selectedGender,
              onChanged: (value) {
                setState(() {
                  _selectedGender = value!;
                });
              },
              activeColor: Theme.of(context).colorScheme.primary,
            ),
          );
        }).toList(),
      ),
    );
  }

  Widget _buildDateStep() {
    return _buildStep(
      title: 'When were you born?',
      child: Column(
        children: [
          ListTile(
            leading: const Icon(Icons.calendar_today),
            title: Text(
              _selectedDate != null
                  ? DateFormat('MMMM d, yyyy').format(_selectedDate!)
                  : 'Select your birth date',
            ),
            trailing: const Icon(Icons.chevron_right),
            onTap: _selectDate,
          ),
        ],
      ),
    );
  }

  Widget _buildTimeStep() {
    return _buildStep(
      title: 'What time were you born?',
      child: Column(
        children: [
          ListTile(
            leading: const Icon(Icons.access_time),
            title: Text(
              _selectedTime != null
                  ? _selectedTime!.format(context)
                  : 'Select your birth time',
            ),
            trailing: const Icon(Icons.chevron_right),
            onTap: _selectTime,
          ),
          const SizedBox(height: 16),
          Text(
            'Exact time is important for accurate chart calculations',
            style: GoogleFonts.inter(
              fontSize: 12,
              color: Colors.grey.shade600,
            ),
            textAlign: TextAlign.center,
          ),
        ],
      ),
    );
  }

  Widget _buildPlaceStep() {
    return _buildStep(
      title: 'Where were you born?',
      child: Column(
        children: [
          TextField(
            controller: _placeController,
            decoration: InputDecoration(
              hintText: 'Enter your birth place',
              prefixIcon: const Icon(Icons.location_on),
              suffixIcon: IconButton(
                icon: const Icon(Icons.my_location),
                onPressed: _getCurrentLocation,
              ),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
          ),
          const SizedBox(height: 16),
          if (_latitude != null && _longitude != null)
            Text(
              'Location: ${_latitude!.toStringAsFixed(4)}, ${_longitude!.toStringAsFixed(4)}',
              style: GoogleFonts.inter(
                fontSize: 12,
                color: Colors.grey.shade600,
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildSummaryStep() {
    return _buildStep(
      title: 'Review your details',
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildSummaryRow('Name', _nameController.text),
          _buildSummaryRow('Gender', _selectedGender),
          _buildSummaryRow('Date', _selectedDate != null ? DateFormat('MMMM d, yyyy').format(_selectedDate!) : 'Not selected'),
          _buildSummaryRow('Time', _selectedTime != null ? _selectedTime!.format(context) : 'Not selected'),
          _buildSummaryRow('Place', _placeController.text),
        ],
      ),
    );
  }

  Widget _buildSummaryRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(
              label,
              style: GoogleFonts.inter(
                fontWeight: FontWeight.w600,
                color: Colors.grey.shade700,
              ),
            ),
          ),
          Expanded(
            child: Text(
              value,
              style: GoogleFonts.inter(),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildStep({required String title, required Widget child}) {
    return Padding(
      padding: const EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            title,
            style: GoogleFonts.playfairDisplay(
              fontSize: 28,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 32),
          Expanded(child: child),
        ],
      ),
    );
  }

  Widget _buildNavigationButtons() {
    return Container(
      padding: const EdgeInsets.all(24),
      child: Row(
        children: [
          if (_currentStep > 0)
            Expanded(
              child: OutlinedButton(
                onPressed: _previousStep,
                child: const Text('Back'),
              ),
            ),
          if (_currentStep > 0) const SizedBox(width: 16),
          Expanded(
            child: ElevatedButton(
              onPressed: _canProceed ? _nextStep : null,
              child: Text(_currentStep == 5 ? 'Create Profile' : 'Next'),
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
    // In production, use geolocator package to get current location
    // For now, this is a placeholder
    setState(() {
      _placeController.text = 'Current Location';
      _latitude = 40.7128;
      _longitude = -74.0060;
    });
  }

  void _createProfile() {
    // In production, send data to backend API
    // POST /api/v1/users/birth-profile
    
    final profileData = {
      'fullName': _nameController.text,
      'gender': _selectedGender,
      'birthDate': _selectedDate!.toIso8601String(),
      'birthTime': '${_selectedTime!.hour}:${_selectedTime!.minute}',
      'birthPlace': _placeController.text,
      'latitude': _latitude,
      'longitude': _longitude,
      'timezone': _timezone,
    };

    // Navigate to home screen
    Navigator.pushReplacementNamed(context, '/home');
  }
}
