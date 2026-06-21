import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'features/birth_profile/presentation/birth_profile_onboarding.dart';
import 'features/chat/presentation/chat_screen.dart';
import 'features/auth/presentation/auth_screen.dart';
import 'features/astrology/presentation/birth_chart_screen.dart';
import 'core/providers/auth_provider.dart';
import 'core/network/api_client.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
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
            firebaseEnabled: false,
          ),
        ),
      ],
      child: MaterialApp(
        title: 'AstroMindAI - AI Life Intelligence',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          primarySwatch: Colors.deepPurple,
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF6B21A8),
            brightness: Brightness.light,
          ),
        ),
        darkTheme: ThemeData(
          primarySwatch: Colors.deepPurple,
          useMaterial3: true,
          colorScheme: ColorScheme.fromSeed(
            seedColor: const Color(0xFF6B21A8),
            brightness: Brightness.dark,
          ),
        ),
        themeMode: ThemeMode.system,
        home: const AuthScreen(),
        routes: {
          '/home': (_) => const _HomeScreen(),
          '/birth-profile': (_) => const BirthProfileOnboarding(),
          '/chat': (_) => const ChatScreen(),
          '/birth-chart': (_) => const BirthChartScreen(),
        },
      ),
    );
  }
}

class _HomeScreen extends StatelessWidget {
  const _HomeScreen();

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    return Scaffold(
      appBar: AppBar(title: const Text('AstroMindAI')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          Text(
            'Welcome to AstroMindAI',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          const SizedBox(height: 8),
          Text(
            authProvider.backendUserId == null
                ? 'Backend session is not available.'
                : 'Backend session connected.',
          ),
          const SizedBox(height: 24),
          FilledButton.icon(
            onPressed: () => Navigator.pushNamed(context, '/birth-profile'),
            icon: const Icon(Icons.person_add_alt_1),
            label: const Text('Create Birth Profile'),
          ),
          const SizedBox(height: 12),
          FilledButton.icon(
            onPressed: () => Navigator.pushNamed(context, '/birth-chart'),
            icon: const Icon(Icons.auto_graph),
            label: const Text('View Birth Chart'),
          ),
          const SizedBox(height: 12),
          OutlinedButton.icon(
            onPressed: () => Navigator.pushNamed(context, '/chat'),
            icon: const Icon(Icons.chat_bubble_outline),
            label: const Text('Open AI Chat'),
          ),
          const SizedBox(height: 12),
          TextButton.icon(
            onPressed: () => authProvider.signOut(),
            icon: const Icon(Icons.logout),
            label: const Text('Sign out'),
          ),
        ],
      ),
    );
  }
}
