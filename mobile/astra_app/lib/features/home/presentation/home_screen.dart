import 'package:flutter/material.dart';

import 'tabs/dashboard_tab.dart';
import 'tabs/insights_tab.dart';
import 'tabs/profile_tab.dart';
import '../../chat/presentation/chat_screen.dart';
import '../../../core/theme/app_theme.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  int _currentIndex = 0;

  final List<Widget> _tabs = [
    const DashboardTab(),
    const InsightsTab(),
    const ChatScreen(),
    const ProfileTab(),
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.backgroundDark, // Warm white background matching Auth
      body: _tabs[_currentIndex],
      bottomNavigationBar: Container(
        decoration: BoxDecoration(
          boxShadow: [
            BoxShadow(
              color: AppColors.primary.withValues(alpha: 0.05),
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
          backgroundColor: AppColors.backgroundDark,
          elevation: 0,
          indicatorColor: AppColors.primary.withValues(alpha: 0.1),
          destinations: const [
            NavigationDestination(
              icon: Icon(Icons.home_outlined, color: AppColors.textDarkMuted),
              selectedIcon: Icon(Icons.home, color: AppColors.primary),
              label: 'Home',
            ),
            NavigationDestination(
              icon: Icon(Icons.auto_awesome_outlined, color: AppColors.textDarkMuted),
              selectedIcon: Icon(Icons.auto_awesome, color: AppColors.primary),
              label: 'Insights',
            ),
            NavigationDestination(
              icon: Icon(Icons.chat_bubble_outline, color: AppColors.textDarkMuted),
              selectedIcon: Icon(Icons.chat_bubble, color: AppColors.primary),
              label: 'AI Chat',
            ),
            NavigationDestination(
              icon: Icon(Icons.person_outline, color: AppColors.textDarkMuted),
              selectedIcon: Icon(Icons.person, color: AppColors.primary),
              label: 'Profile',
            ),
          ],
        ),
      ),
    );
  }
}
