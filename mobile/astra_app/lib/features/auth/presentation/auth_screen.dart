import 'dart:ui' as ui;
import 'dart:math' as math;
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../../../core/providers/auth_provider.dart';

class AuthScreen extends StatefulWidget {
  const AuthScreen({super.key});

  @override
  State<AuthScreen> createState() => _AuthScreenState();
}

class _AuthScreenState extends State<AuthScreen> with SingleTickerProviderStateMixin {
  late AnimationController _bgController;

  static const _ink = Color(0xFFFFFFFF); 
  static const _muted = Color(0xB3FFFFFF);
  static const _primary = Color(0xFFFFD700); 
  static const _accent = Color(0xFF9D4EDD); 

  @override
  void initState() {
    super.initState();
    _bgController = AnimationController(
      vsync: this,
      duration: const Duration(seconds: 20),
    )..repeat();
  }

  @override
  void dispose() {
    _bgController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFF0B0F19),
      body: Stack(
        children: [
          // Animated Background
          Positioned.fill(
            child: AnimatedBuilder(
              animation: _bgController,
              builder: (context, _) {
                return CustomPaint(
                  painter: _MysticBackgroundPainter(_bgController.value),
                );
              },
            ),
          ),
          
          // Foreground Content
          SafeArea(
            child: LayoutBuilder(
              builder: (context, constraints) {
                final wide = constraints.maxWidth >= 860;

                return SingleChildScrollView(
                  child: ConstrainedBox(
                    constraints: BoxConstraints(minHeight: constraints.maxHeight),
                    child: Center(
                      child: Padding(
                        padding: EdgeInsets.symmetric(
                          horizontal: wide ? 56 : 24,
                          vertical: 16,
                        ),
                      child: wide
                          ? Row(
                              children: [
                                const Expanded(child: _BrandPane()),
                                const SizedBox(width: 48),
                                SizedBox(
                                  width: 440,
                                  child: _AuthPanel(
                                    onEmailLogin: _handleEmailLogin,
                                    onEmailRegister: _handleEmailRegister,
                                    onDemo: _handleDemoSignIn,
                                  ),
                                ),
                              ],
                            )
                          : Column(
                              crossAxisAlignment: CrossAxisAlignment.stretch,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                const _BrandPane(compact: true),
                                const SizedBox(height: 24),
                                _AuthPanel(
                                  onEmailLogin: _handleEmailLogin,
                                  onEmailRegister: _handleEmailRegister,
                                  onDemo: _handleDemoSignIn,
                                ),
                              ],
                            ),
                      ),
                    ),
                  ),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _handleEmailLogin(AuthProvider authProvider) async {
    final emailController = TextEditingController();
    final passwordController = TextEditingController();

    await showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        backgroundColor: const Color(0xFF131A2A),
        title: Text('Sign In', style: GoogleFonts.inter(fontWeight: FontWeight.bold, color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: emailController,
              keyboardType: TextInputType.emailAddress,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Email',
                hintStyle: const TextStyle(color: Colors.white54),
                prefixIcon: const Icon(Icons.email_outlined, color: _primary),
                focusedBorder: OutlineInputBorder(
                  borderSide: const BorderSide(color: _primary, width: 2),
                  borderRadius: BorderRadius.circular(12),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: passwordController,
              obscureText: true,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Password',
                hintStyle: const TextStyle(color: Colors.white54),
                prefixIcon: const Icon(Icons.lock_outline, color: _primary),
                focusedBorder: OutlineInputBorder(
                  borderSide: const BorderSide(color: _primary, width: 2),
                  borderRadius: BorderRadius.circular(12),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel', style: TextStyle(color: Colors.white70)),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: _primary),
            onPressed: () async {
              Navigator.pop(dialogContext);
              final success = await authProvider.loginWithEmail(
                emailController.text,
                passwordController.text,
              );
              if (success && mounted) {
                Navigator.pushReplacementNamed(context, '/home');
              } else if (mounted) {
                _showError(authProvider.errorMessage ?? 'Login failed');
              }
            },
            child: const Text('Sign In'),
          ),
        ],
      ),
    );

    emailController.dispose();
    passwordController.dispose();
  }

  Future<void> _handleEmailRegister(AuthProvider authProvider) async {
    final emailController = TextEditingController();
    final passwordController = TextEditingController();

    await showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        backgroundColor: const Color(0xFF131A2A),
        title: Text('Create Account', style: GoogleFonts.inter(fontWeight: FontWeight.bold, color: Colors.white)),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            TextField(
              controller: emailController,
              keyboardType: TextInputType.emailAddress,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Email',
                hintStyle: const TextStyle(color: Colors.white54),
                prefixIcon: const Icon(Icons.email_outlined, color: _primary),
                focusedBorder: OutlineInputBorder(
                  borderSide: const BorderSide(color: _primary, width: 2),
                  borderRadius: BorderRadius.circular(12),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
            const SizedBox(height: 16),
            TextField(
              controller: passwordController,
              obscureText: true,
              style: const TextStyle(color: Colors.white),
              decoration: InputDecoration(
                hintText: 'Password',
                hintStyle: const TextStyle(color: Colors.white54),
                prefixIcon: const Icon(Icons.lock_outline, color: _primary),
                focusedBorder: OutlineInputBorder(
                  borderSide: const BorderSide(color: _primary, width: 2),
                  borderRadius: BorderRadius.circular(12),
                ),
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(dialogContext),
            child: const Text('Cancel', style: TextStyle(color: Colors.white70)),
          ),
          FilledButton(
            style: FilledButton.styleFrom(backgroundColor: _primary),
            onPressed: () async {
              Navigator.pop(dialogContext);
              final success = await authProvider.registerWithEmail(
                emailController.text,
                passwordController.text,
              );
              if (success && mounted) {
                Navigator.pushReplacementNamed(context, '/birth-profile');
              } else if (mounted) {
                _showError(authProvider.errorMessage ?? 'Registration failed');
              }
            },
            child: const Text('Create Account'),
          ),
        ],
      ),
    );

    emailController.dispose();
    passwordController.dispose();
  }

  Future<void> _handleDemoSignIn(AuthProvider authProvider) async {
    final success = await authProvider.signInDemo();
    if (success && mounted) {
      Navigator.pushReplacementNamed(context, '/home');
    } else if (mounted) {
      _showError(authProvider.errorMessage ?? 'Demo login failed');
    }
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: const Color(0xFFB42318),
        behavior: SnackBarBehavior.floating,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
        margin: const EdgeInsets.all(16),
      ),
    );
  }
}

class _MysticBackgroundPainter extends CustomPainter {
  _MysticBackgroundPainter(this.animationValue);

  final double animationValue;

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Offset.zero & size;

    // Base gradient
    final gradient = RadialGradient(
      center: Alignment(
        math.sin(animationValue * math.pi * 2) * 0.5, 
        math.cos(animationValue * math.pi * 2) * 0.5
      ),
      radius: 1.5,
      colors: const [
        Color(0xFF1A0B2E), // Deep cosmic purple (instead of beige)
        Color(0xFF0B0F19), // Midnight blue
      ],
      stops: const [0.0, 1.0],
    );

    canvas.drawRect(rect, Paint()..shader = gradient.createShader(rect));

    // Draw some subtle glowing orbs
    final orbPaint = Paint()
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 80)
      ..color = const Color(0xFF9D4EDD).withValues(alpha: 0.15); // Soft gold

    final center1 = Offset(
      size.width * (0.5 + math.cos(animationValue * math.pi * 2) * 0.3),
      size.height * (0.3 + math.sin(animationValue * math.pi * 2) * 0.2),
    );
    canvas.drawCircle(center1, 200, orbPaint);

    final orbPaint2 = Paint()
      ..maskFilter = const MaskFilter.blur(BlurStyle.normal, 100)
      ..color = const Color(0xFFFFD700).withValues(alpha: 0.1); // Soft saffron

    final center2 = Offset(
      size.width * (0.2 + math.sin(animationValue * math.pi * 2) * 0.4),
      size.height * (0.7 + math.cos(animationValue * math.pi * 2) * 0.3),
    );
    canvas.drawCircle(center2, 250, orbPaint2);
  }

  @override
  bool shouldRepaint(_MysticBackgroundPainter oldDelegate) => 
      oldDelegate.animationValue != animationValue;
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
              width: 56,
              height: 56,
              decoration: BoxDecoration(
                gradient: const LinearGradient(
                  colors: [Color(0xFFFFD700), Color(0xFF9D4EDD)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                borderRadius: BorderRadius.circular(16),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFFFD700).withValues(alpha: 0.3),
                    blurRadius: 20,
                    offset: const Offset(0, 8),
                  ),
                ],
              ),
              child: const Icon(
                Icons.auto_awesome,
                color: Colors.white,
                size: 28,
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
                      color: const Color(0xFFFFFFFF),
                      fontSize: compact ? 28 : 36,
                      fontWeight: FontWeight.w800,
                      letterSpacing: -0.5,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'PREMIUM VEDIC ASTROLOGY',
                    maxLines: 1,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.inter(
                      color: const Color(0xFFFFD700),
                      fontSize: 11,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 2.0,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
        SizedBox(height: compact ? 16 : 40),
        Text(
          'Discover Your Cosmic Blueprint.',
          style: GoogleFonts.playfairDisplay(
            color: const Color(0xFFFFFFFF),
            fontSize: compact ? 32 : 56,
            height: 1.1,
            fontWeight: FontWeight.w800,
          ),
        ),
        const SizedBox(height: 12),
        ConstrainedBox(
          constraints: const BoxConstraints(maxWidth: 500),
          child: Text(
            'Experience highly accurate, personalized life insights powered by NASA-precision planetary mathematics and advanced artificial intelligence.',
            style: GoogleFonts.inter(
              color: const Color(0xB3FFFFFF),
              fontSize: compact ? 14 : 16,
              height: 1.5,
              fontWeight: FontWeight.w400,
            ),
          ),
        ),
      ],
    );
  }
}

class _AuthPanel extends StatelessWidget {
  const _AuthPanel({required this.onEmailLogin, required this.onEmailRegister, this.onDemo});

  final Future<void> Function(AuthProvider authProvider) onEmailLogin;
  final Future<void> Function(AuthProvider authProvider) onEmailRegister;
  final Future<void> Function(AuthProvider authProvider)? onDemo;

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        return ClipRRect(
          borderRadius: BorderRadius.circular(24),
          child: BackdropFilter(
            filter: ui.ImageFilter.blur(sigmaX: 20, sigmaY: 20),
            child: Container(
              padding: const EdgeInsets.all(32),
              decoration: BoxDecoration(
                color: const Color(0xFF131A2A).withValues(alpha: 0.7),
                borderRadius: BorderRadius.circular(24),
                border: Border.all(color: const Color(0xFF9D4EDD).withValues(alpha: 0.3), width: 1.5),
                boxShadow: [
                  BoxShadow(
                    color: const Color(0xFFFFD700).withValues(alpha: 0.05),
                    blurRadius: 40,
                    offset: const Offset(0, 20),
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
                      color: const Color(0xFFFFFFFF),
                      fontSize: 32,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Please sign in to continue your journey.',
                    style: GoogleFonts.inter(
                      color: const Color(0xB3FFFFFF),
                      fontSize: 15,
                      fontWeight: FontWeight.w400,
                    ),
                  ),
                  const SizedBox(height: 24),
                  _PremiumButton(
                    icon: authProvider.isLoading
                        ? const SizedBox(
                            width: 20,
                            height: 20,
                            child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                          )
                        : const Icon(Icons.email_outlined, size: 28),
                    label: authProvider.isLoading
                        ? (authProvider.isWakingUpServer ? 'Waking secure server (~50s)...' : 'Signing in...')
                        : 'Continue with Email',
                    onPressed: authProvider.isLoading ? null : () => onEmailLogin(authProvider),
                    isPrimary: true,
                  ),
                  const SizedBox(height: 16),
                  _PremiumButton(
                    icon: const Icon(Icons.person_add_outlined, size: 20),
                    label: authProvider.isLoading && authProvider.isWakingUpServer ? 'Please wait...' : 'Create Account',
                    onPressed: authProvider.isLoading ? null : () => onEmailRegister(authProvider),
                    isPrimary: false,
                  ),
                  if (onDemo != null) ...[
                    const SizedBox(height: 32),
                    const _DividerLabel(),
                    const SizedBox(height: 24),
                    _PremiumButton(
                      icon: authProvider.isLoading && authProvider.isWakingUpServer
                          ? const SizedBox(
                              width: 16,
                              height: 16,
                              child: CircularProgressIndicator(strokeWidth: 2, color: Color(0xFFFFD700)),
                            )
                          : const Icon(Icons.play_circle_outline, size: 20),
                      label: authProvider.isLoading
                          ? (authProvider.isWakingUpServer ? 'Waking server (~50s)...' : 'Starting...')
                          : 'Try Demo Mode',
                      onPressed: authProvider.isLoading ? null : () => onDemo!(authProvider),
                      isDemo: true,
                    ),
                  ],
                ],
              ),
            ),
          ),
        );
      },
    );
  }
}

class _PremiumButton extends StatelessWidget {
  const _PremiumButton({
    required this.icon,
    required this.label,
    required this.onPressed,
    this.isPrimary = false,
    this.isDemo = false,
  });

  final Widget icon;
  final String label;
  final VoidCallback? onPressed;
  final bool isPrimary;
  final bool isDemo;

  @override
  Widget build(BuildContext context) {
    if (isDemo) {
      return TextButton.icon(
        onPressed: onPressed,
        icon: IconTheme.merge(
          data: const IconThemeData(color: Color(0xFFFFD700)),
          child: icon,
        ),
        label: Text(
          label,
          style: GoogleFonts.inter(
            fontSize: 15,
            fontWeight: FontWeight.w600,
            color: const Color(0xFFFFD700),
          ),
        ),
      );
    }

    return Container(
      height: 48,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(14),
        gradient: isPrimary
            ? const LinearGradient(
                colors: [Color(0xFFFFD700), Color(0xFF5E2CA5)],
                begin: Alignment.centerLeft,
                end: Alignment.centerRight,
              )
            : null,
        color: isPrimary ? null : const Color(0xFF131A2A),
        border: isPrimary ? null : Border.all(color: const Color(0x3DFFFFFF)),
        boxShadow: isPrimary
            ? [
                BoxShadow(
                  color: const Color(0xFFFFD700).withValues(alpha: 0.3),
                  blurRadius: 16,
                  offset: const Offset(0, 6),
                ),
              ]
            : [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.02),
                  blurRadius: 8,
                  offset: const Offset(0, 2),
                ),
              ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: onPressed,
          borderRadius: BorderRadius.circular(14),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              IconTheme.merge(
                data: IconThemeData(color: isPrimary ? Colors.white : const Color(0xFFFFFFFF)),
                child: icon,
              ),
              const SizedBox(width: 12),
              Text(
                label,
                style: GoogleFonts.inter(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: isPrimary ? Colors.white : const Color(0xFFFFFFFF),
                ),
              ),
            ],
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
        const Expanded(child: Divider(color: Color(0x3DFFFFFF))),
        Padding(
          padding: const EdgeInsets.symmetric(horizontal: 16),
          child: Text(
            'OR PREVIEW APP',
            style: GoogleFonts.inter(
              color: const Color(0xB3FFFFFF),
              fontSize: 11,
              letterSpacing: 1.5,
              fontWeight: FontWeight.w700,
            ),
          ),
        ),
        const Expanded(child: Divider(color: Color(0x3DFFFFFF))),
      ],
    );
  }
}
