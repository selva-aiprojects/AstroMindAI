import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../../core/network/api_client.dart';
import '../../../../core/providers/auth_provider.dart';
import '../../../../core/theme/app_theme.dart';

class InsightsTab extends StatefulWidget {
  const InsightsTab({super.key});

  @override
  State<InsightsTab> createState() => _InsightsTabState();
}

class _InsightsTabState extends State<InsightsTab> {
  Map<String, dynamic>? _insights;
  bool _isLoading = true;
  String? _error;
  bool _isYearly = false;

  @override
  void initState() {
    super.initState();
    _loadInsights();
  }

  Future<void> _loadInsights() async {
    try {
      setState(() {
        _isLoading = true;
        _error = null;
      });
      
      final authProvider = context.read<AuthProvider>();
      if (authProvider.backendUserId == null) {
        throw Exception('User not logged in');
      }

      final apiClient = context.read<ApiClient>();
      final insights = _isYearly 
          ? await apiClient.getYearlyProjection(authProvider.backendUserId!)
          : await apiClient.getCurrentSituation(authProvider.backendUserId!);
      
      if (mounted) {
        setState(() {
          _insights = insights;
          _isLoading = false;
        });
      }
    } catch (e) {
      if (mounted) {
        setState(() {
          _error = 'Failed to load planetary insights.\nError: $e\n\nPlease try again later.';
          _isLoading = false;
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Focus(
        autofocus: true,
        child: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: Container(
              padding: const EdgeInsets.fromLTRB(24, 60, 24, 40),
              decoration: const BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Color(0xFF5E2CA5), // Deep Purple
                    Color(0xFF131A2A), // Dark BG
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.only(
                  bottomLeft: Radius.circular(40),
                  bottomRight: Radius.circular(40),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Cosmic Insights',
                    style: GoogleFonts.playfairDisplay(
                      color: Colors.white,
                      fontSize: 36,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    _isYearly 
                      ? 'Detailed 12-month forecasts, expected events, and specific remedies.'
                      : 'AI predictions based on your live planetary transits.',
                    style: GoogleFonts.inter(
                      color: Colors.white.withValues(alpha: 0.8),
                      fontSize: 16,
                      height: 1.5,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  const SizedBox(height: 24),
                  Container(
                    width: double.infinity,
                    decoration: BoxDecoration(
                      color: const Color(0xFF0B0F19).withValues(alpha: 0.5),
                      borderRadius: BorderRadius.circular(16),
                    ),
                    child: Padding(
                      padding: const EdgeInsets.all(4.0),
                      child: Row(
                        children: [
                          Expanded(
                            child: GestureDetector(
                              onTap: () {
                                if (_isYearly) {
                                  setState(() { _isYearly = false; });
                                  _loadInsights();
                                }
                              },
                              child: Container(
                                padding: const EdgeInsets.symmetric(vertical: 12),
                                decoration: BoxDecoration(
                                  color: !_isYearly ? AppColors.primary : Colors.transparent,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                alignment: Alignment.center,
                                child: Text('Current', 
                                  style: GoogleFonts.inter(
                                    fontWeight: FontWeight.w600,
                                    color: !_isYearly ? AppColors.textOnPrimary : Colors.white70,
                                  )
                                ),
                              ),
                            ),
                          ),
                          Expanded(
                            child: GestureDetector(
                              onTap: () {
                                if (!_isYearly) {
                                  setState(() { _isYearly = true; });
                                  _loadInsights();
                                }
                              },
                              child: Container(
                                padding: const EdgeInsets.symmetric(vertical: 12),
                                decoration: BoxDecoration(
                                  color: _isYearly ? AppColors.primary : Colors.transparent,
                                  borderRadius: BorderRadius.circular(12),
                                ),
                                alignment: Alignment.center,
                                child: Text('1-Year Projection', 
                                  style: GoogleFonts.inter(
                                    fontWeight: FontWeight.w600,
                                    color: _isYearly ? AppColors.textOnPrimary : Colors.white70,
                                  )
                                ),
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
          if (_isLoading)
            SliverFillRemaining(
              hasScrollBody: false,
              child: Center(
                child: Column(
                  mainAxisAlignment: MainAxisAlignment.center,
                  children: [
                    const CircularProgressIndicator(color: Color(0xFFFFD700)),
                    const SizedBox(height: 24),
                    Text(
                      _isYearly ? 'Projecting your year ahead...' : 'Analyzing current transits...',
                      style: GoogleFonts.inter(
                        color: Colors.white70,
                        fontSize: 16,
                      ),
                    ),
                  ],
                ),
              ),
            )
          else if (_error != null)
            SliverFillRemaining(
              hasScrollBody: false,
              child: Center(
                child: Padding(
                  padding: const EdgeInsets.all(32),
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.error_outline, color: Colors.redAccent, size: 48),
                      const SizedBox(height: 16),
                      Text(
                        _error!,
                        textAlign: TextAlign.center,
                        style: GoogleFonts.inter(color: Colors.white),
                      ),
                      const SizedBox(height: 24),
                      const SizedBox(height: 16),
                      FilledButton(
                        style: FilledButton.styleFrom(
                          backgroundColor: const Color(0xFFFFD700),
                          foregroundColor: const Color(0xFF131A2A),
                        ),
                        onPressed: () {
                          Navigator.pushNamed(context, '/birth-profile');
                        },
                        child: const Text('Create Birth Profile'),
                      ),
                      const SizedBox(height: 16),
                      TextButton(
                        onPressed: _loadInsights,
                        child: const Text('Try Again', style: TextStyle(color: Colors.white70)),
                      ),
                    ],
                  ),
                ),
              ),
            )
          else
            SliverPadding(
              padding: const EdgeInsets.all(24),
              sliver: SliverList(
                delegate: SliverChildListDelegate([
                  _InsightCard(
                    title: 'Career & Ambition',
                    icon: Icons.work_outline,
                    color: const Color(0xFF4ECDC4),
                    content: _insights?['career'] ?? 'No data available',
                  ),
                  const SizedBox(height: 24),
                  _InsightCard(
                    title: 'Love & Relationships',
                    icon: Icons.favorite_outline,
                    color: const Color(0xFFFF6B6B),
                    content: _insights?['marriage'] ?? 'No data available',
                  ),
                  const SizedBox(height: 24),
                  _InsightCard(
                    title: 'Wealth & Finance',
                    icon: Icons.account_balance_wallet_outlined,
                    color: const Color(0xFFFFD700),
                    content: _insights?['finance'] ?? 'No data available',
                  ),
                  const SizedBox(height: 24),
                  _InsightCard(
                    title: 'Health & Spiritual',
                    icon: Icons.self_improvement,
                    color: const Color(0xFF9D4EDD),
                    content: _insights?['health'] ?? 'No data available',
                  ),
                  const SizedBox(height: 40),
                ]),
              ),
            ),
        ],
      ),
     ),
    );
  }
}

class _InsightCard extends StatelessWidget {
  const _InsightCard({
    required this.title,
    required this.icon,
    required this.color,
    required this.content,
  });

  final String title;
  final IconData icon;
  final Color color;
  final String content;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: const Color(0xFF131A2A),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: color.withValues(alpha: 0.3)),
        boxShadow: [
          BoxShadow(
            color: color.withValues(alpha: 0.05),
            blurRadius: 24,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: BackdropFilter(
          filter: ui.ImageFilter.blur(sigmaX: 10, sigmaY: 10),
          child: Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: color.withValues(alpha: 0.1),
                        shape: BoxShape.circle,
                      ),
                      child: Icon(icon, color: color, size: 24),
                    ),
                    const SizedBox(width: 16),
                    Expanded(
                      child: Text(
                        title,
                        style: GoogleFonts.playfairDisplay(
                          color: Colors.white,
                          fontSize: 24,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 20),
                const Divider(color: Colors.white12),
                const SizedBox(height: 20),
                ..._parseMarkdownContent(content),
              ],
            ),
          ),
        ),
      ),
    );
  }

  List<Widget> _parseMarkdownContent(String markdown) {
    final lines = markdown.split('\n');
    final widgets = <Widget>[];

    for (final line in lines) {
      if (line.trim().isEmpty) {
        widgets.add(const SizedBox(height: 8));
        continue;
      }

      if (line.startsWith('1. ') || line.startsWith('2. ') || line.startsWith('3. ')) {
        widgets.add(
          Padding(
            padding: const EdgeInsets.only(top: 12, bottom: 8),
            child: Text(
              line.substring(3).replaceAll('**', ''),
              style: GoogleFonts.inter(
                color: color,
                fontSize: 16,
                fontWeight: FontWeight.w700,
              ),
            ),
          ),
        );
      } else if (line.startsWith('- ') || line.startsWith('* ')) {
        widgets.add(
          Padding(
            padding: const EdgeInsets.only(left: 16, bottom: 6),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '• ',
                  style: TextStyle(color: color, fontSize: 16, fontWeight: FontWeight.bold),
                ),
                Expanded(
                  child: Text(
                    line.substring(2).replaceAll('**', ''),
                    style: GoogleFonts.inter(
                      color: Colors.white.withValues(alpha: 0.85),
                      fontSize: 14,
                      height: 1.5,
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      } else {
        widgets.add(
          Text(
            line.replaceAll('**', ''),
            style: GoogleFonts.inter(
              color: Colors.white.withValues(alpha: 0.85),
              fontSize: 14,
              height: 1.5,
            ),
          ),
        );
      }
    }

    return widgets;
  }
}
