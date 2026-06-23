import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:firebase_core/firebase_core.dart';
import 'firebase_options.dart';
import 'features/birth_profile/presentation/birth_profile_onboarding.dart';
import 'features/chat/presentation/chat_screen.dart';
import 'features/auth/presentation/auth_screen.dart';
import 'features/astrology/presentation/birth_chart_screen.dart';
import 'features/home/presentation/home_screen.dart';
import 'core/providers/auth_provider.dart';
import 'core/network/api_client.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  await Firebase.initializeApp(
    options: DefaultFirebaseOptions.currentPlatform,
  );
  runApp(const AstraApp());
}

class AstraApp extends StatelessWidget {
  const AstraApp({super.key});

  @override
  Widget build(BuildContext context) {
    final apiClient = ApiClient()..warmUpBackend();

    return MultiProvider(
      providers: [
        Provider<ApiClient>.value(value: apiClient),
        ChangeNotifierProvider(
          create: (_) => AuthProvider(
            apiClient: apiClient,
            firebaseEnabled: true,
          ),
        ),
      ],
      child: MaterialApp(
        title: 'AstroMindAI - AI Life Intelligence',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF2A0B4C), // Deep Purple
            primary: const Color(0xFF2A0B4C),
            secondary: const Color(0xFFFFD700), // Neon Gold
            tertiary: const Color(0xFF5E2CA5),  // Vibrant Purple
            background: const Color(0xFFF8F9FA),
            brightness: Brightness.light,
          ),
          textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme),
        ),
        darkTheme: ThemeData(
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF0B0F19), // Midnight Blue
            primary: const Color(0xFFFFD700),   // Neon Gold (pops on dark background)
            secondary: const Color(0xFF9D4EDD), // Bright Purple
            tertiary: const Color(0xFF5E2CA5),
            background: const Color(0xFF0B0F19),
            surface: const Color(0xFF131A2A),
            brightness: Brightness.dark,
          ),
          textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme).apply(
            bodyColor: Colors.white,
            displayColor: Colors.white,
          ),
        ),
        themeMode: ThemeMode.dark, // Defaulting to dark mode for the cosmic feel
        home: const AuthScreen(),
        routes: {
          '/home': (_) => const HomeScreen(),
          '/birth-profile': (_) => const BirthProfileOnboarding(),
          '/chat': (_) => const ChatScreen(),
          '/birth-chart': (_) => const BirthChartScreen(),
        },
      ),
    );
  }
}
