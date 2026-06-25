import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../core/network/api_client.dart';
import '../../../core/providers/auth_provider.dart';
import 'birth_chart_widget.dart';
import '../../../core/theme/app_theme.dart';

class BirthChartScreen extends StatefulWidget {
  const BirthChartScreen({super.key});

  @override
  State<BirthChartScreen> createState() => _BirthChartScreenState();
}

class _BirthChartScreenState extends State<BirthChartScreen> {
  Map<String, dynamic>? chartData;
  Map<String, dynamic>? lifeSummaryData;
  Map<String, dynamic>? dashaData;
  Map<String, dynamic>? transitData;
  Map<String, dynamic>? horoscopeData;
  Map<String, dynamic>? birthProfileData;
  bool isLoading = true;
  String? errorMessage;

  @override
  void initState() {
    super.initState();
    _loadBirthChart();
  }

  Future<void> _loadBirthChart() async {
    final userId = context.read<AuthProvider>().backendUserId;
    if (userId == null) {
      setState(() {
        errorMessage = 'Please sign in first.';
        isLoading = false;
      });
      return;
    }

    setState(() {
      isLoading = true;
      errorMessage = null;
    });

    try {
      final apiClient = context.read<ApiClient>();
      
      final results = await Future.wait([
        apiClient.getBirthChart(userId),
        apiClient.getLifeSummary(userId),
        apiClient.getDashaTimeline(userId),
        apiClient.getTransits(userId),
        apiClient.getDailyHoroscope(userId),
        apiClient.getBirthProfile(userId),
      ]);

      setState(() {
        chartData = results[0];
        lifeSummaryData = results[1];
        dashaData = results[2];
        transitData = results[3];
        horoscopeData = results[4];
        birthProfileData = results[5];
        isLoading = false;
      });
    } catch (error) {
      final errorMsg = ApiClient.describeError(error);
      if (errorMsg.contains('Birth profile not found') || 
          errorMsg.contains('Failed to generate birth chart') ||
          error.toString().contains('400')) {
        setState(() {
          errorMessage = 'No birth profile found. Please create your birth profile first.';
          isLoading = false;
        });
        Future.delayed(const Duration(seconds: 2), () {
          if (mounted) {
            Navigator.pushReplacementNamed(context, '/birth-profile');
          }
        });
      } else {
        setState(() {
          errorMessage = 'Failed to load birth chart dashboard: $errorMsg';
          isLoading = false;
        });
      }
    }
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) return 'N/A';
    try {
      final date = DateTime.parse(dateStr);
      final months = [
        'Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun',
        'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'
      ];
      return '${months[date.month - 1]} ${date.day}, ${date.year}';
    } catch (_) {
      return dateStr;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (isLoading) {
      return Scaffold(
        backgroundColor: AppColors.backgroundDark,
        appBar: AppBar(
          backgroundColor: AppColors.surfaceDark,
          title: Text(
            'Birth Chart Dashboard',
            style: GoogleFonts.inter(fontWeight: FontWeight.w600, color: Colors.white),
          ),
          elevation: 0,
          iconTheme: const IconThemeData(color: Colors.white),
        ),
        body: const Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              CircularProgressIndicator(),
              SizedBox(height: 16),
              Text('Analyzing planetary positions...'),
            ],
          ),
        ),
      );
    }

    if (errorMessage != null) {
      return Scaffold(
        backgroundColor: const Color(0xFF0B0F19),
        appBar: AppBar(
          backgroundColor: const Color(0xFFFFD700),
          title: Text(
            'Birth Chart Dashboard',
            style: GoogleFonts.inter(fontWeight: FontWeight.w600, color: Colors.white),
          ),
          elevation: 0,
          iconTheme: const IconThemeData(color: Colors.white),
        ),
        body: Center(
          child: Padding(
            padding: const EdgeInsets.all(20.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.error_outline, size: 48, color: Colors.red),
                const SizedBox(height: 16),
                Text(
                  errorMessage!,
                  style: GoogleFonts.inter(fontSize: 16),
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _loadBirthChart,
                  child: const Text('Retry'),
                ),
              ],
            ),
          ),
        ),
      );
    }

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        backgroundColor: AppColors.backgroundDark,
        appBar: AppBar(
          backgroundColor: AppColors.surfaceDark,
          iconTheme: const IconThemeData(color: Colors.white),
          title: Text(
            'Astro Intelligence',
            style: GoogleFonts.playfairDisplay(fontWeight: FontWeight.w800, color: AppColors.primary, fontSize: 24),
          ),
          bottom: TabBar(
            labelColor: Colors.white,
            unselectedLabelColor: Colors.white70,
            indicatorColor: Colors.white,
            indicatorWeight: 3,
            labelStyle: GoogleFonts.inter(fontWeight: FontWeight.w700, fontSize: 14),
            unselectedLabelStyle: GoogleFonts.inter(fontWeight: FontWeight.w500, fontSize: 14),
            tabs: const [
              Tab(text: 'Kundli', icon: Icon(Icons.grid_3x3)),
              Tab(text: 'Life Summary', icon: Icon(Icons.analytics_outlined)),
              Tab(text: 'Periods', icon: Icon(Icons.access_time)),
            ],
          ),
          elevation: 0,
          actions: [
            IconButton(
              icon: const Icon(Icons.refresh, color: Colors.white),
              onPressed: _loadBirthChart,
            ),
          ],
        ),
        body: TabBarView(
          children: [
            _buildKundliTab(),
            _buildLifeSummaryTab(),
            _buildPeriodsTab(),
          ],
        ),
      ),
    );
  }

  Widget _buildKundliTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildProfileInfo(),
          const SizedBox(height: 16),
          _buildChartInfo(),
          const SizedBox(height: 16),
          BirthChartWidget(
            chartData: chartData ?? {},
          ),
          const SizedBox(height: 16),
          _buildPlanetaryDetails(),
        ],
      ),
    );
  }

  Widget _buildLifeSummaryTab() {
    if (lifeSummaryData == null) {
      return const Center(child: Text('No summary data available.'));
    }

    final categories = [
      {
        'key': 'career',
        'title': 'Career & Profession',
        'subtitle': 'Karma Sthana & Leadership',
        'icon': Icons.work_outline,
        'gradient': [const Color(0xFF3B82F6), const Color(0xFF1D4ED8)],
      },
      {
        'key': 'finance',
        'title': 'Wealth & Finance',
        'subtitle': 'Dhana Bhava & Prosperity',
        'icon': Icons.account_balance_wallet_outlined,
        'gradient': [const Color(0xFF10B981), const Color(0xFF047857)],
      },
      {
        'key': 'marriage',
        'title': 'Love & Marriage',
        'subtitle': 'Kalatra Sthana & Partnership',
        'icon': Icons.favorite_border,
        'gradient': [const Color(0xFFEC4899), const Color(0xFFBE185D)],
      },
      {
        'key': 'health',
        'title': 'Health & Wellness',
        'subtitle': 'Roga Bhava & Vitality',
        'icon': Icons.health_and_safety_outlined,
        'gradient': [const Color(0xFFF59E0B), const Color(0xFFB45309)],
      },
      {
        'key': 'spiritual',
        'title': 'Remedies & Spirituality',
        'subtitle': 'Dharma Sthana & Remedies',
        'icon': Icons.spa_outlined,
        'gradient': [const Color(0xFF8B5CF6), const Color(0xFF6D28D9)],
      },
    ];

    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: categories.length,
      itemBuilder: (context, index) {
        final cat = categories[index];
        final text = lifeSummaryData![cat['key']] ?? 'No details available.';
        final gradient = cat['gradient'] as List<Color>;

        return Card(
          margin: const EdgeInsets.only(bottom: 16),
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
          elevation: 3,
          clipBehavior: Clip.antiAlias,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    colors: gradient,
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                  ),
                ),
                child: Row(
                  children: [
                    Icon(cat['icon'] as IconData, color: Colors.white, size: 24),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            cat['title'] as String,
                            style: GoogleFonts.inter(
                              color: Colors.white,
                              fontSize: 16,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          Text(
                            cat['subtitle'] as String,
                            style: GoogleFonts.inter(
                              color: Colors.white70,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ),
              Padding(
                padding: const EdgeInsets.all(16),
                child: Text(
                  text,
                  style: GoogleFonts.inter(
                    fontSize: 14,
                    height: 1.5,
                    color: Theme.of(context).brightness == Brightness.dark
                        ? Colors.grey.shade300
                        : Colors.grey.shade800,
                  ),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildPeriodsTab() {
    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          _buildActiveDashaCard(),
          const SizedBox(height: 16),
          _buildDailyHoroscopeCard(),
          const SizedBox(height: 16),
          _buildActiveTransitsCard(),
          const SizedBox(height: 16),
          _buildDashaTimeline(),
        ],
      ),
    );
  }

  Widget _buildActiveDashaCard() {
    if (dashaData == null) return const SizedBox.shrink();
    
    final currentM = dashaData!['currentMahadasha'];
    final currentA = dashaData!['currentAntardasha'];
    
    if (currentM == null) return const SizedBox.shrink();
    
    final mLord = currentM['lord'] ?? 'N/A';
    final mEnd = _formatDate(currentM['endDate']);
    
    final aLord = currentA != null ? currentA['lord'] : 'N/A';
    final aEnd = currentA != null ? _formatDate(currentA['endDate']) : 'N/A';

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 4,
      child: Container(
        decoration: BoxDecoration(
          color: AppColors.surfaceDark,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(color: AppColors.primary.withValues(alpha: 0.3)),
        ),
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.star, color: AppColors.primary, size: 28),
                const SizedBox(width: 8),
                Text(
                  'Current Planetary Period',
                  style: GoogleFonts.inter(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const Divider(color: Colors.white24, height: 24),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Mahadasha (Major)',
                        style: GoogleFonts.inter(color: Colors.white70, fontSize: 12),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        mLord,
                        style: GoogleFonts.inter(
                          color: Colors.white,
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        'until $mEnd',
                        style: GoogleFonts.inter(color: Colors.white60, fontSize: 11),
                      ),
                    ],
                  ),
                ),
                Container(
                  width: 1,
                  height: 50,
                  color: Colors.white24,
                  margin: const EdgeInsets.symmetric(horizontal: 16),
                ),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'Antardasha (Sub)',
                        style: GoogleFonts.inter(color: Colors.white70, fontSize: 12),
                      ),
                      const SizedBox(height: 4),
                      Text(
                        aLord,
                        style: GoogleFonts.inter(
                          color: Colors.white,
                          fontSize: 22,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        'until $aEnd',
                        style: GoogleFonts.inter(color: Colors.white60, fontSize: 11),
                      ),
                    ],
                  ),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDailyHoroscopeCard() {
    if (horoscopeData == null) return const SizedBox.shrink();
    
    final horoscope = horoscopeData!['horoscope'] as String? ?? 'No horoscope available today.';
    final moonSign = horoscopeData!['moonSign'] as String? ?? 'N/A';
    final ascendant = horoscopeData!['ascendant'] as String? ?? 'N/A';

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.wb_sunny_outlined, color: Colors.orange, size: 24),
                const SizedBox(width: 8),
                Text(
                  'Daily Horoscope',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Row(
              children: [
                _buildHoroscopeBadge('Moon Sign', moonSign),
                const SizedBox(width: 8),
                _buildHoroscopeBadge('Ascendant', ascendant),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              horoscope,
              style: GoogleFonts.inter(
                fontSize: 14,
                height: 1.5,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHoroscopeBadge(String label, String value) {
    final isDark = Theme.of(context).brightness == Brightness.dark;
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 6),
      decoration: BoxDecoration(
        color: isDark ? Colors.orange.shade900.withOpacity(0.2) : Colors.orange.shade50,
        borderRadius: BorderRadius.circular(20),
        border: Border.all(color: isDark ? Colors.orange.shade800 : Colors.orange.shade200),
      ),
      child: Text(
        '$label: $value',
        style: GoogleFonts.inter(
          fontSize: 11,
          fontWeight: FontWeight.w600,
          color: isDark ? Colors.orange.shade200 : Colors.orange.shade900,
        ),
      ),
    );
  }

  Widget _buildActiveTransitsCard() {
    if (transitData == null) return const SizedBox.shrink();
    
    final majorTransits = transitData!['majorTransits'] as Map<String, dynamic>?;
    if (majorTransits == null || majorTransits.isEmpty) {
      return Card(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
        elevation: 2,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Icon(Icons.alt_route, color: Colors.blue, size: 24),
                  const SizedBox(width: 8),
                  Text(
                    'Active Planetary Transits',
                    style: GoogleFonts.inter(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 12),
              Text(
                'No major challenging transits are active currently. Enjoy the stable energetic flow!',
                style: GoogleFonts.inter(
                  fontSize: 14,
                  fontStyle: FontStyle.italic,
                  color: Colors.grey.shade600,
                ),
              ),
            ],
          ),
        ),
      );
    }

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.alt_route, color: Colors.blue, size: 24),
                const SizedBox(width: 8),
                Text(
                  'Active Planetary Transits',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            ...majorTransits.entries.map((entry) {
              return Padding(
                padding: const EdgeInsets.only(bottom: 8.0),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    const Icon(Icons.info_outline, color: Colors.blue, size: 18),
                    const SizedBox(width: 8),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            entry.key,
                            style: GoogleFonts.inter(
                              fontSize: 14,
                              fontWeight: FontWeight.bold,
                              color: Theme.of(context).brightness == Brightness.dark
                                  ? Colors.blue.shade300
                                  : Colors.blue.shade800,
                            ),
                          ),
                          Text(
                            entry.value.toString(),
                            style: GoogleFonts.inter(
                              fontSize: 13,
                              color: Theme.of(context).brightness == Brightness.dark
                                  ? Colors.grey.shade300
                                  : Colors.grey.shade700,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              );
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildDashaTimeline() {
    if (dashaData == null || dashaData!['mahadashas'] == null) {
      return const SizedBox.shrink();
    }

    final mahadashas = dashaData!['mahadashas'] as List<dynamic>;
    final currentLord = dashaData!['currentMahadasha']?['lord'];

    return Card(
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      elevation: 2,
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.timeline, color: Colors.orange, size: 24),
                const SizedBox(width: 8),
                Text(
                  'Vimshottari Dasha Timeline',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            ListView.builder(
              shrinkWrap: true,
              physics: const NeverScrollableScrollPhysics(),
              itemCount: mahadashas.length,
              itemBuilder: (context, index) {
                final dasha = mahadashas[index];
                final lord = dasha['lord'] ?? 'N/A';
                final start = _formatDate(dasha['startDate']);
                final end = _formatDate(dasha['endDate']);
                final years = dasha['years'] ?? 0;
                final isCurrent = lord == currentLord;

                return Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Column(
                      children: [
                        Container(
                          width: 14,
                          height: 14,
                          decoration: BoxDecoration(
                            shape: BoxShape.circle,
                            color: isCurrent ? const Color(0xFFE0A640) : Colors.orange.shade200,
                            border: isCurrent
                                ? Border.all(color: Colors.white, width: 2)
                                : null,
                            boxShadow: isCurrent
                                ? [
                                    BoxShadow(
                                      color: const Color(0xFFE0A640).withOpacity(0.5),
                                      blurRadius: 6,
                                      spreadRadius: 2,
                                    )
                                  ]
                                : null,
                          ),
                        ),
                        if (index < mahadashas.length - 1)
                          Container(
                            width: 2,
                            height: 50,
                            color: Colors.orange.shade100,
                          ),
                      ],
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Padding(
                        padding: const EdgeInsets.only(bottom: 12.0),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Text(
                              '$lord Period ($years Years)',
                              style: GoogleFonts.inter(
                                fontSize: 14,
                                fontWeight: isCurrent ? FontWeight.bold : FontWeight.w600,
                                color: isCurrent
                                    ? const Color(0xFFE0A640)
                                    : Theme.of(context).brightness == Brightness.dark
                                        ? Colors.white
                                        : Colors.white,
                              ),
                            ),
                            const SizedBox(height: 2),
                            Text(
                              '$start - $end',
                              style: GoogleFonts.inter(
                                fontSize: 12,
                                color: Theme.of(context).brightness == Brightness.dark
                                    ? Colors.grey.shade400
                                    : Colors.grey.shade600,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ],
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileInfo() {
    if (birthProfileData == null) return const SizedBox.shrink();

    final name = birthProfileData!['fullName']?.toString() ?? 'N/A';
    final dob = _formatDate(birthProfileData!['birthDate']?.toString());
    final time = birthProfileData!['birthTime']?.toString() ?? 'N/A';
    final place = birthProfileData!['birthPlace']?.toString() ?? 'N/A';
    final gender = birthProfileData!['gender']?.toString() ?? 'N/A';

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.person, color: AppColors.primary, size: 24),
                const SizedBox(width: 8),
                Text(
                  'Birth Details',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildInfoRow('Name', name),
            _buildInfoRow('Gender', gender),
            _buildInfoRow('Date of Birth', dob),
            _buildInfoRow('Time', time),
            _buildInfoRow('Place', place),
          ],
        ),
      ),
    );
  }

  Widget _buildChartInfo() {
    if (chartData == null) return const SizedBox.shrink();

    return Card(
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                const Icon(Icons.info_outline, color: Color(0xB3FFFFFF), size: 24),
                const SizedBox(width: 8),
                Text(
                  'Chart Information',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.bold,
                  ),
                ),
              ],
            ),
            const SizedBox(height: 16),
            _buildInfoRow('Ascendant', chartData!['ascendant']?.toString() ?? 'N/A'),
            _buildInfoRow('Ayanamsa', chartData!['ayanamsa'] ?? 'N/A'),
            _buildInfoRow('Julian Day', chartData!['julianDay']?.toString() ?? 'N/A'),
          ],
        ),
      ),
    );
  }

  Widget _buildPlanetaryDetails() {
    if (chartData == null || chartData!['planetaryPositions'] == null) {
      return const SizedBox.shrink();
    }

    final planetaryPositions = chartData!['planetaryPositions'] as Map<String, dynamic>;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              'Planetary Positions',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 12),
            ...planetaryPositions.entries.map((entry) {
              final planet = entry.key;
              final position = entry.value as Map<String, dynamic>;
              return _buildPlanetRow(planet, position);
            }),
          ],
        ),
      ),
    );
  }

  Widget _buildInfoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: GoogleFonts.inter(
              fontWeight: FontWeight.w500,
              color: Colors.grey.shade700,
            ),
          ),
          Text(
            value,
            style: GoogleFonts.inter(
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildPlanetRow(String planet, Map<String, dynamic> position) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            planet,
            style: GoogleFonts.inter(
              fontSize: 16,
              fontWeight: FontWeight.bold,
              color: Colors.orange,
            ),
          ),
          const SizedBox(height: 4),
          Row(
            children: [
              _buildPlanetBadge('Sign', position['sign'] ?? 'N/A'),
              const SizedBox(width: 8),
              _buildPlanetBadge('Nakshatra', position['nakshatra'] ?? 'N/A'),
              const SizedBox(width: 8),
              _buildPlanetBadge('House', chartData!['housePlacements']?[planet]?.toString() ?? 'N/A'),
            ],
          ),
          if (position['isRetrograde'] == true)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(
                'Retrograde',
                style: GoogleFonts.inter(
                  fontSize: 12,
                  color: Colors.orange,
                  fontStyle: FontStyle.italic,
                ),
              ),
            ),
          if (position['isCombust'] == true)
            Padding(
              padding: const EdgeInsets.only(top: 4),
              child: Text(
                'Combust',
                style: GoogleFonts.inter(
                  fontSize: 12,
                  color: Colors.red,
                  fontStyle: FontStyle.italic,
                ),
              ),
            ),
        ],
      ),
    );
  }

  Widget _buildPlanetBadge(String label, String value) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
      decoration: BoxDecoration(
        color: Colors.orange.shade50,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.orange.shade200),
      ),
      child: Text(
        '$label: $value',
        style: GoogleFonts.inter(
          fontSize: 12,
          color: Colors.orange.shade800,
        ),
      ),
    );
  }
}
