import 'package:flutter/material.dart';

import 'tabs/dashboard_tab.dart';
import 'tabs/profile_tab.dart';
import '../../chat/presentation/chat_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  final List<Widget> _tabs = [
    const DashboardTab(),
    const ChatScreen(),
    const ProfileTab(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFFDFBF7), // Warm white background matching Auth
      body: _tabs[_currentIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          boxShadow: [
            BoxShadow(
              color: const Color(0xFFE65100).withValues(alpha: 0.05),
              blurRadius: 24,
              offset: const Offset(0, -8),
            ),
          ],
        ),
        child: NavigationBar(
          selectedIndex: _currentIndex,
          onDestinationSelected: (index) {
            setState(() {
              _currentIndex = index;
            });
          },
          backgroundColor: Colors.white,
          elevation: 0,
          indicatorColor: const Color(0xFFE65100).withValues(alpha: 0.1),
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.home_outlined, color: Color(0xFF8D6E63)),
              selectedIcon: Icon(Icons.home, color: Color(0xFFE65100)),
              label: 'Home',
            ),
            NavigationDestination(
              icon: Icon(Icons.chat_bubble_outline, color: Color(0xFF8D6E63)),
              selectedIcon: Icon(Icons.chat_bubble, color: Color(0xFFE65100)),
              label: 'AI Chat',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline, color: Color(0xFF8D6E63)),
              selectedIcon: Icon(Icons.person, color: Color(0xFFE65100)),
              label: 'Profile',
            ),
          ],
        ),
      ),
    );
  }
}
