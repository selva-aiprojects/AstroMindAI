import 'dart:ui' as ui;
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../../core/network/api_client.dart';
import '../../../../core/providers/auth_provider.dart';
import '../../../../core/providers/language_provider.dart';
import '../../../../core/theme/app_theme.dart';
import '../../../../core/theme/language_picker.dart';

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

      final langCode = context.read<LanguageProvider>().code;
      final apiClient = context.read<ApiClient>();
      final insights = _isYearly
          ? await apiClient.getYearlyProjection(authProvider.backendUserId!, language: langCode)
          : await apiClient.getCurrentSituation(authProvider.backendUserId!, language: langCode);

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
              child: _buildHeader(),
            ),
            if (_isLoading)
              SliverFillRemaining(
                hasScrollBody: false,
                child: Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      SizedBox(
                        width: 56,
                        height: 56,
                        child: CircularProgressIndicator(
                          color: AppColors.primary,
                          strokeWidth: 3,
                          backgroundColor: AppColors.primary.withOpacity(0.1),
                        ),
                      ),
                      const SizedBox(height: 24),
                      Text(
                        _isYearly ? 'Projecting your year ahead...' : 'Analyzing current transits...',
                        style: GoogleFonts.inter(color: Colors.white70, fontSize: 16),
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
                        Container(
                          padding: const EdgeInsets.all(20),
                          decoration: BoxDecoration(
                            color: AppColors.error.withOpacity(0.1),
                            shape: BoxShape.circle,
                          ),
                          child: const Icon(Icons.error_outline, color: AppColors.error, size: 48),
                        ),
                        const SizedBox(height: 16),
                        Text(
                          _error!,
                          textAlign: TextAlign.center,
                          style: GoogleFonts.inter(color: Colors.white, fontSize: 14, height: 1.5),
                        ),
                        const SizedBox(height: 24),
                        FilledButton.icon(
                          style: FilledButton.styleFrom(
                            backgroundColor: AppColors.primary,
                            foregroundColor: AppColors.textOnPrimary,
                          ),
                          onPressed: () => Navigator.pushNamed(context, '/birth-profile'),
                          icon: const Icon(Icons.person_add_outlined, size: 18),
                          label: const Text('Create Birth Profile'),
                        ),
                        const SizedBox(height: 12),
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
                padding: const EdgeInsets.fromLTRB(24, 8, 24, 40),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    _InsightCard(
                      title: 'Career & Ambition',
                      icon: Icons.work_outline,
                      gradient: const LinearGradient(
                        colors: [Color(0xFF3B82F6), Color(0xFF1D4ED8)],
                        begin: Alignment.topLeft, end: Alignment.bottomRight,
                      ),
                      content: _insights?['career'] ?? 'No data available',
                    ),
                    const SizedBox(height: 20),
                    _InsightCard(
                      title: 'Love & Relationships',
                      icon: Icons.favorite_outline,
                      gradient: const LinearGradient(
                        colors: [Color(0xFFFF4081), Color(0xFFBE185D)],
                        begin: Alignment.topLeft, end: Alignment.bottomRight,
                      ),
                      content: _insights?['marriage'] ?? 'No data available',
                    ),
                    const SizedBox(height: 20),
                    _InsightCard(
                      title: 'Wealth & Finance',
                      icon: Icons.account_balance_wallet_outlined,
                      gradient: AppColors.goldGradient,
                      content: _insights?['finance'] ?? 'No data available',
                    ),
                    const SizedBox(height: 20),
                    _InsightCard(
                      title: 'Health & Spiritual',
                      icon: Icons.self_improvement,
                      gradient: AppColors.auroraGradient,
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

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.fromLTRB(24, 60, 24, 28),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.tertiary.withOpacity(0.95),
            AppColors.backgroundDark,
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: const BorderRadius.only(
          bottomLeft: Radius.circular(36),
          bottomRight: Radius.circular(36),
        ),
        boxShadow: [
          BoxShadow(
            color: AppColors.tertiary.withOpacity(0.3),
            blurRadius: 30,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Title row
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Cosmic Insights',
                    style: GoogleFonts.playfairDisplay(
                      color: Colors.white,
                      fontSize: 34,
                      fontWeight: FontWeight.w800,
                      height: 1.1,
                    ),
                  ),
                  const SizedBox(height: 6),
                  Text(
                    _isYearly
                        ? '12-month AI forecast & planetary remedies'
                        : 'Live planetary transit analysis',
                    style: GoogleFonts.inter(
                      color: Colors.white.withOpacity(0.7),
                      fontSize: 13,
                      height: 1.4,
                    ),
                  ),
                ],
              ),
              // Language icon button
              GestureDetector(
                onTap: () async {
                  await showLanguagePicker(context);
                  if (mounted) _loadInsights();
                },
                child: Container(
                  padding: const EdgeInsets.all(10),
                  decoration: BoxDecoration(
                    color: AppColors.surfaceElevated,
                    borderRadius: BorderRadius.circular(14),
                    border: Border.all(color: AppColors.surfaceBorder),
                  ),
                  child: ShaderMask(
                    shaderCallback: (r) => AppColors.goldGradient.createShader(r),
                    child: const Icon(Icons.translate_rounded, color: Colors.white, size: 22),
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          // Language chip row
          LanguageChipRow(onLanguageChanged: _loadInsights),
          const SizedBox(height: 20),
          // Current / 1-Year toggle
          Container(
            width: double.infinity,
            decoration: BoxDecoration(
              color: const Color(0xFF0B0F19).withOpacity(0.5),
              borderRadius: BorderRadius.circular(16),
              border: Border.all(color: AppColors.surfaceBorder),
            ),
            child: Padding(
              padding: const EdgeInsets.all(4.0),
              child: Row(
                children: [
                  _buildToggle('Current', false),
                  _buildToggle('1-Year Projection', true),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildToggle(String label, bool isYearly) {
    final isActive = _isYearly == isYearly;
    return Expanded(
      child: GestureDetector(
        onTap: () {
          if (_isYearly != isYearly) {
            setState(() => _isYearly = isYearly);
            _loadInsights();
          }
        },
        child: AnimatedContainer(
          duration: const Duration(milliseconds: 200),
          padding: const EdgeInsets.symmetric(vertical: 12),
          decoration: BoxDecoration(
            gradient: isActive ? AppColors.fireGradient : null,
            color: isActive ? null : Colors.transparent,
            borderRadius: BorderRadius.circular(12),
            boxShadow: isActive
                ? [BoxShadow(color: AppColors.primary.withOpacity(0.3), blurRadius: 8, offset: const Offset(0, 2))]
                : null,
          ),
          alignment: Alignment.center,
          child: Text(
            label,
            style: GoogleFonts.inter(
              fontWeight: FontWeight.w700,
              fontSize: 13,
              color: isActive ? Colors.white : Colors.white60,
            ),
          ),
        ),
      ),
    );
  }
}

// ─── Insight Card ─────────────────────────────────────────────────────────────
class _InsightCard extends StatelessWidget {
  const _InsightCard({
    required this.title,
    required this.icon,
    required this.gradient,
    required this.content,
  });

  final String title;
  final IconData icon;
  final LinearGradient gradient;
  final String content;

  Color get _accentColor => gradient.colors.first;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.surfaceDark,
        borderRadius: BorderRadius.circular(24),
        border: Border.all(color: _accentColor.withOpacity(0.25)),
        boxShadow: [
          BoxShadow(
            color: _accentColor.withOpacity(0.08),
            blurRadius: 24,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: ClipRRect(
        borderRadius: BorderRadius.circular(24),
        child: BackdropFilter(
          filter: ui.ImageFilter.blur(sigmaX: 10, sigmaY: 10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Gradient header strip
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
                decoration: BoxDecoration(gradient: gradient),
                child: Row(
                  children: [
                    Container(
                      padding: const EdgeInsets.all(10),
                      decoration: BoxDecoration(
                        color: Colors.white.withOpacity(0.2),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Icon(icon, color: Colors.white, size: 22),
                    ),
                    const SizedBox(width: 14),
                    Text(
                      title,
                      style: GoogleFonts.playfairDisplay(
                        color: Colors.white,
                        fontSize: 20,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ],
                ),
              ),
              // Content
              Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: _parseMarkdownContent(content),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  List<Widget> _parseMarkdownContent(String markdown) {
    if (markdown.trim().isEmpty) return [];
    if (markdown.contains('- ') || markdown.contains('* ') || markdown.contains('1. ')) {
      return _parseLines(markdown.split('\n'));
    }
    final sentences = markdown.split('. ');
    final bulletLines = <String>[];
    for (var s in sentences) {
      s = s.trim();
      if (s.isNotEmpty) {
        if (!s.endsWith('.')) s += '.';
        bulletLines.add('- $s');
      }
    }
    return _parseLines(bulletLines);
  }

  List<Widget> _parseLines(List<String> lines) {
    final widgets = <Widget>[];
    for (final line in lines) {
      if (line.trim().isEmpty) { widgets.add(const SizedBox(height: 8)); continue; }
      if (RegExp(r'^\d+\. ').hasMatch(line)) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(top: 12, bottom: 6),
          child: Text(
            line.replaceFirst(RegExp(r'^\d+\. '), '').replaceAll('**', ''),
            style: GoogleFonts.inter(color: _accentColor, fontSize: 15, fontWeight: FontWeight.w700),
          ),
        ));
      } else if (line.startsWith('- ') || line.startsWith('* ')) {
        widgets.add(Padding(
          padding: const EdgeInsets.only(left: 4, bottom: 10),
          child: Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Container(
                margin: const EdgeInsets.only(top: 7, right: 10),
                width: 5,
                height: 5,
                decoration: BoxDecoration(color: _accentColor, shape: BoxShape.circle),
              ),
              Expanded(
                child: Text(
                  line.substring(2).replaceAll('**', ''),
                  style: GoogleFonts.inter(color: Colors.white.withOpacity(0.88), fontSize: 14, height: 1.6),
                ),
              ),
            ],
          ),
        ));
      } else {
        widgets.add(Padding(
          padding: const EdgeInsets.only(bottom: 8),
          child: Text(
            line.replaceAll('**', ''),
            style: GoogleFonts.inter(color: Colors.white.withOpacity(0.85), fontSize: 14, height: 1.6),
          ),
        ));
      }
    }
    return widgets;
  }
}
