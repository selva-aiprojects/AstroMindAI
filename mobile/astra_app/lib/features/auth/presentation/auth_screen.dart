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
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: _AuthScreenState._ink,
                borderRadius: BorderRadius.circular(8),
              ),
              child: const Icon(
                Icons.auto_awesome,
                color: _AuthScreenState._amber,
                size: 26,
              ),
            ),
            const SizedBox(width: 14),
            Text(
              'ASTRA',
              style: GoogleFonts.playfairDisplay(
                color: _AuthScreenState._ink,
                fontSize: 26,
                fontWeight: FontWeight.w700,
              ),
            ),
          ],
        ),
        SizedBox(height: compact ? 28 : 72),
        Text(
          'AI Life Intelligence',
          style: GoogleFonts.playfairDisplay(
            color: _AuthScreenState._ink,
            fontSize: compact ? 42 : 64,
            height: 1.02,
            fontWeight: FontWeight.w700,
          ),
        ),
        const SizedBox(height: 18),
        ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 560),
          child: Text(
            'Personalized astrology, guidance, and daily decisions in one calm workspace.',
            style: GoogleFonts.inter(
              color: _AuthScreenState._muted,
              fontSize: compact ? 16 : 18,
              height: 1.6,
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
      ('Birth charts', Icons.blur_circular),
      ('Dasha insights', Icons.timeline),
      ('Private memory', Icons.lock_outline),
    ];

    return Wrap(
      spacing: 12,
      runSpacing: 12,
      children: items
          .map(
            (item) => Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 12),
              decoration: BoxDecoration(
                color: Colors.white,
                border: Border.all(color: const Color(0xFFE7E0D2)),
                borderRadius: BorderRadius.circular(8),
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Icon(item.$2, size: 18, color: _AuthScreenState._teal),
                  const SizedBox(width: 8),
                  Text(
                    item.$1,
                    style: GoogleFonts.inter(
                      color: _AuthScreenState._ink,
                      fontSize: 13,
                      fontWeight: FontWeight.w600,
                    ),
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
  const _AuthPanel({required this.onGoogle, required this.onPhone});

  final Future<void> Function(AuthProvider authProvider) onGoogle;
  final Future<void> Function(AuthProvider authProvider) onPhone;

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        return Container(
          padding: const EdgeInsets.all(28),
          decoration: BoxDecoration(
            color: _AuthScreenState._panel,
            borderRadius: BorderRadius.circular(8),
            border: Border.all(color: const Color(0xFFE5DED2)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.08),
                blurRadius: 28,
                offset: const Offset(0, 18),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            mainAxisSize: MainAxisSize.min,
            children: [
              Text(
                'Sign in',
                style: GoogleFonts.inter(
                  color: _AuthScreenState._ink,
                  fontSize: 24,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'Continue to your Astra account.',
                style: GoogleFonts.inter(
                  color: _AuthScreenState._muted,
                  fontSize: 14,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 28),
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
  });

  final Widget icon;
  final String label;
  final VoidCallback? onPressed;
  final bool filled;

  @override
  Widget build(BuildContext context) {
    final background = filled ? _AuthScreenState._ink : Colors.white;
    final foreground = filled ? Colors.white : _AuthScreenState._ink;

    return SizedBox(
      height: 52,
      child: FilledButton.icon(
        onPressed: onPressed,
        icon: IconTheme.merge(
          data: IconThemeData(color: foreground),
          child: icon,
        ),
        label: Text(
          label,
          style: GoogleFonts.inter(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            color: foreground,
          ),
        ),
        style: FilledButton.styleFrom(
          backgroundColor: background,
          disabledBackgroundColor: background.withValues(alpha: 0.55),
          foregroundColor: foreground,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(8),
            side: BorderSide(
              color: filled ? _AuthScreenState._ink : const Color(0xFFD8D0C3),
            ),
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
