import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../core/providers/auth_provider.dart';

class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> {
  static const _ink = Color(0xFF17151F);
  static const _muted = Color(0xFF6F6A7A);
  static const _surface = Color(0xFFF8F6F1);
  static const _panel = Color(0xFFFFFFFF);
  static const _teal = Color(0xFF087E8B);
  static const _amber = Color(0xFFE0A640);

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: _surface,
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final wide = constraints.maxWidth >= 860;

            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: Padding(
                  padding: EdgeInsets.symmetric(
                    horizontal: wide ? 56 : 20,
                    vertical: wide ? 40 : 24,
                  ),
                  child: wide
                      ? Row(
                          children: [
                            const Expanded(child: _BrandPane()),
                            const SizedBox(width: 48),
                            SizedBox(
                              width: 420,
                              child: _AuthPanel(
                                onGoogle: _handleGoogleSignIn,
                                onPhone: _handlePhoneSignIn,
                                onDemo: _handleDemoSignIn,
                              ),
                            ),
                          ],
                        )
                      : Column(
                          crossAxisAlignment: CrossAxisAlignment.stretch,
                          children: [
                            const _BrandPane(compact: true),
                            const SizedBox(height: 28),
                            _AuthPanel(
                              onGoogle: _handleGoogleSignIn,
                              onPhone: _handlePhoneSignIn,
                              onDemo: _handleDemoSignIn,
                            ),
                          ],
                        ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }

  Future<void> _handleGoogleSignIn(AuthProvider authProvider) async {
    final success = await authProvider.signInWithGoogle();
    if (success && mounted) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (mounted) {
      _showError(authProvider.errorMessage ?? 'Sign in failed');
    }
  }

  Future<void> _handleDemoSignIn(AuthProvider authProvider) async {
    final success = await authProvider.signInDemo();
    if (success && mounted) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (mounted) {
      _showError(authProvider.errorMessage ?? 'Demo login failed');
    }
  }

  Future<void> _handlePhoneSignIn(AuthProvider authProvider) async {
    final phoneController = TextEditingController();

    await showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Enter phone number'),
        content: TextField(
          controller: phoneController,
          keyboardType: TextInputType.phone,
          decoration: const InputDecoration(
            hintText: '+1 234 567 8900',
            prefixIcon: Icon(Icons.phone_outlined),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () async {
              Navigator.pop(dialogContext);
              await authProvider.sendOtp(phoneController.text);
              if (mounted) {
                _showOtpDialog(authProvider, phoneController.text);
              }
            },
            child: const Text('Send OTP'),
          ),
        ],
      ),
    );

    phoneController.dispose();
  }

  void _showOtpDialog(AuthProvider authProvider, String phoneNumber) {
    final otpController = TextEditingController();

    showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('Enter OTP'),
        content: TextField(
          controller: otpController,
          keyboardType: TextInputType.number,
          decoration: const InputDecoration(
            hintText: '123456',
            prefixIcon: Icon(Icons.sms_outlined),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel'),
          ),
          FilledButton(
            onPressed: () async {
              Navigator.pop(dialogContext);
              final success = await authProvider.signInWithPhone(
                phoneNumber,
                otpController.text,
              );
              otpController.dispose();
              if (success && mounted) {
                Navigator.pushReplacementNamed(context, '/home');
              }
            },
            child: const Text('Verify'),
          ),
        ],
      ),
    );
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: const Color(0xFFB42318),
        behavior: SnackBarBehavior.floating,
      ),
    );
  }
}

class _BrandPane extends StatelessWidget {
  const _BrandPane({this.compact = false});

  final bool compact;

  @override
  Widget build(BuildContext context) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        Row(
          children: [
            Container(
              width: 64,
              height: 64,
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    _AuthScreenState._teal,
                    _AuthScreenState._amber,
                    _AuthScreenState._ink,
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: _AuthScreenState._teal.withOpacity(0.3),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: const Icon(
                Icons.auto_awesome,
                color: Colors.white,
                size: 32,
              ),
            ),
            const SizedBox(width: 16),
            Flexible(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'AstroMindAI',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.playfairDisplay(
                      color: _AuthScreenState._ink,
                      fontSize: compact ? 32 : 36,
                      fontWeight: FontWeight.w800,
                      letterSpacing: -0.5,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '95%+ Accurate Astrology',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.inter(
                      color: _AuthScreenState._teal,
                      fontSize: 13,
                      fontWeight: FontWeight.w700,
                      letterSpacing: 0.8,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        SizedBox(height: compact ? 32 : 80),
        Text(
          'AI-Powered Vedic Astrology',
          style: GoogleFonts.playfairDisplay(
            color: _AuthScreenState._ink,
            fontSize: compact ? 44 : 68,
            height: 1.05,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 20),
        ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 560),
          child: Text(
            'Get personalized life insights powered by NASA-accurate planetary calculations and advanced AI.',
            style: GoogleFonts.inter(
              color: _AuthScreenState._muted,
              fontSize: compact ? 16 : 19,
              height: 1.7,
              fontWeight: FontWeight.w500,
            ),
          ),
        ),
        if (!compact) ...[const SizedBox(height: 48), const _SignalStrip()],
      ],
    );
  }
}

class _SignalStrip extends StatelessWidget {
  const _SignalStrip();

  @override
  Widget build(BuildContext context) {
    const items = [
      ('NASA-Accurate Charts', Icons.public, 'Swiss Ephemeris'),
      ('AI-Powered Insights', Icons.psychology, 'Advanced LLMs'),
      ('Vedic Wisdom', Icons.auto_awesome, 'Ancient Knowledge'),
      ('Personalized Guidance', Icons.person, 'Tailored for You'),
    ];

    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: items
          .map(
            (item) => Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 14),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [
                    Colors.white,
                    const Color(0xFFF8F6F1),
                  ],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                border: Border.all(color: const Color(0xFFE5DED2)),
                borderRadius: BorderRadius.circular(12),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withOpacity(0.04),
                    blurRadius: 12,
                    offset: const Offset(0, 4),
                  ),
                ],
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Container(
                    padding: const EdgeInsets.all(8),
                    decoration: BoxDecoration(
                      color: _AuthScreenState._teal.withOpacity(0.1),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Icon(item.$2, size: 20, color: _AuthScreenState._teal),
                  ),
                  const SizedBox(width: 12),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        item.$1,
                        style: GoogleFonts.inter(
                          color: _AuthScreenState._ink,
                          fontSize: 14,
                          fontWeight: FontWeight.w700,
                        ),
                      ),
                      Text(
                        item.$3,
                        style: GoogleFonts.inter(
                          color: _AuthScreenState._muted,
                          fontSize: 11,
                          fontWeight: FontWeight.w500,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
            ),
          )
          .toList(),
    );
  }
}

class _AuthPanel extends StatelessWidget {
  const _AuthPanel({required this.onGoogle, required this.onPhone, this.onDemo});

  final Future<void> Function(AuthProvider authProvider) onGoogle;
  final Future<void> Function(AuthProvider authProvider) onPhone;
  final Future<void> Function(AuthProvider authProvider)? onDemo;

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        return Container(
          padding: const EdgeInsets.all(32),
          decoration: BoxDecoration(
            color: _AuthScreenState._panel,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: const Color(0xFFE5DED2)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.06),
                blurRadius: 32,
                offset: const Offset(0, 16),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'Welcome Back',
                style: GoogleFonts.playfairDisplay(
                  color: _AuthScreenState._ink,
                  fontSize: 28,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Start your personalized astrology journey today',
                style: GoogleFonts.inter(
                  color: _AuthScreenState._muted,
                  fontSize: 15,
                  height: 1.5,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 32),
              _ActionButton(
                icon: authProvider.isLoading
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Icon(Icons.g_mobiledata, size: 28),
                label: authProvider.isLoading
                    ? 'Signing in...'
                    : 'Continue with Google',
                onPressed: authProvider.isLoading
                    ? null
                    : () => onGoogle(authProvider),
                filled: true,
              ),
              const SizedBox(height: 12),
              _ActionButton(
                icon: const Icon(Icons.phone_outlined, size: 20),
                label: 'Continue with Phone',
                onPressed: authProvider.isLoading
                    ? null
                    : () => onPhone(authProvider),
              ),
              if (onDemo != null) ...[
                const SizedBox(height: 12),
                _ActionButton(
                  icon: const Icon(Icons.play_circle_outline, size: 20),
                  label: 'Try Demo Mode',
                  onPressed: authProvider.isLoading
                      ? null
                      : () => onDemo!(authProvider),
                  isDemo: true,
                ),
              ],
              const SizedBox(height: 24),
              const _DividerLabel(),
              const SizedBox(height: 24),
              Text(
                'By continuing, you agree to the Terms of Service and Privacy Policy.',
                style: GoogleFonts.inter(
                  color: _AuthScreenState._muted,
                  fontSize: 12,
                  height: 1.5,
                ),
                textAlign: TextAlign.center,
              ),
            ],
          ),
        );
      },
    );
  }
}

class _ActionButton extends StatelessWidget {
  const _ActionButton({
    required this.icon,
    required this.label,
    required this.onPressed,
    this.filled = false,
    this.isDemo = false,
  });

  final Widget icon;
  final String label;
  final VoidCallback? onPressed;
  final bool filled;
  final bool isDemo;

  @override
  Widget build(BuildContext context) {
    Color background;
    Color foreground;
    BorderSide? borderSide;

    if (isDemo) {
      background = _AuthScreenState._teal.withOpacity(0.1);
      foreground = _AuthScreenState._teal;
      borderSide = BorderSide(color: _AuthScreenState._teal.withOpacity(0.3));
    } else if (filled) {
      background = _AuthScreenState._ink;
      foreground = Colors.white;
      borderSide = BorderSide(color: _AuthScreenState._ink);
    } else {
      background = Colors.white;
      foreground = _AuthScreenState._ink;
      borderSide = const BorderSide(color: Color(0xFFD8D0C3));
    }

    return SizedBox(
      height: 56,
      child: FilledButton.icon(
        onPressed: onPressed,
        icon: IconTheme.merge(
          data: IconThemeData(color: foreground),
          child: icon,
        ),
        label: Text(
          label,
          style: GoogleFonts.inter(
            fontSize: 16,
            fontWeight: FontWeight.w700,
            color: foreground,
          ),
        ),
        style: FilledButton.styleFrom(
          backgroundColor: background,
          disabledBackgroundColor: background.withValues(alpha: 0.55),
          foregroundColor: foreground,
          elevation: 0,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
            side: borderSide,
          ),
        ),
      ),
    );
  }
}

class _DividerLabel extends StatelessWidget {
  const _DividerLabel();

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        const Expanded(child: Divider(color: Color(0xFFE5DED2))),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 12),
          child: Text(
            'Secure access',
            style: GoogleFonts.inter(
              color: _AuthScreenState._muted,
              fontSize: 12,
              fontWeight: FontWeight.w600,
            ),
          ),
        ),
        const Expanded(child: Divider(color: Color(0xFFE5DED2))),
      ],
    );
  }
}
