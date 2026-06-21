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
    final apiClient = ApiClient();

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
          primarySwatch: Colors.orange,
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFFE65100),
            primary: const Color(0xFFE65100),
            secondary: const Color(0xFFFFB300),
            tertiary: const Color(0xFFF57C00),
            brightness: Brightness.light,
          ),
          textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme),
        ),
        darkTheme: ThemeData(
          primarySwatch: Colors.orange,
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFFE65100),
            primary: const Color(0xFFFF9800),
            secondary: const Color(0xFFFFC107),
            tertiary: const Color(0xFFFFB74D),
            brightness: Brightness.dark,
          ),
          textTheme: GoogleFonts.interTextTheme(Theme.of(context).textTheme),
        ),
        themeMode: ThemeMode.system,
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
