import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:provider/provider.dart';

import '../providers/language_provider.dart';
import 'app_theme.dart';

/// Shows a beautiful bottom-sheet language picker.
/// Uses the [callingContext] to ensure the LanguageProvider is resolved
/// from the correct widget tree ancestor.
Future<void> showLanguagePicker(BuildContext context) {
  final parentContext = context;
  return showModalBottomSheet(
    context: context,
    isScrollControlled: true,
    backgroundColor: Colors.transparent,
    builder: (_) => _LanguagePickerSheet(parentContext: parentContext),
  );
}

class _LanguagePickerSheet extends StatelessWidget {
  final BuildContext parentContext;

  const _LanguagePickerSheet({required this.parentContext});

  @override
  Widget build(BuildContext context) {
    // Watch from parent so we always get the correct provider scope
    final langProvider = parentContext.watch<LanguageProvider>();

    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [AppColors.backgroundMid, AppColors.backgroundDark],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(28)),
        border: Border(
          top: BorderSide(color: AppColors.surfaceBorder, width: 1),
        ),
      ),
      child: SafeArea(
        top: false,
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            // Handle
            Container(
              margin: const EdgeInsets.only(top: 12),
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: AppColors.surfaceBorder,
                borderRadius: BorderRadius.circular(2),
              ),
            ),
            const SizedBox(height: 20),
            // Title
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Row(
                children: [
                  ShaderMask(
                    shaderCallback: (r) => AppColors.goldGradient.createShader(r),
                    child: const Icon(Icons.translate_rounded, color: Colors.white, size: 26),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    'Select Language',
                    style: GoogleFonts.playfairDisplay(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 8),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: Text(
                'AI responses & astro reports will be in your chosen language.',
                style: GoogleFonts.inter(color: AppColors.textDarkMuted, fontSize: 13, height: 1.5),
              ),
            ),
            const SizedBox(height: 20),
            const Divider(color: AppColors.surfaceBorder, height: 1),
            // Language grid
            Padding(
              padding: const EdgeInsets.all(16),
              child: GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 2,
                  childAspectRatio: 3.2,
                  crossAxisSpacing: 10,
                  mainAxisSpacing: 10,
                ),
                itemCount: LanguageProvider.supportedLanguages.length,
                itemBuilder: (context, index) {
                  final lang = LanguageProvider.supportedLanguages[index];
                  final isSelected = lang == langProvider.selected;
                  return GestureDetector(
                    onTap: () {
                      // Use parentContext to ensure we write to the correct provider
                      parentContext.read<LanguageProvider>().setLanguage(lang);
                      Navigator.pop(context);
                    },
                    child: AnimatedContainer(
                      duration: const Duration(milliseconds: 200),
                      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
                      decoration: BoxDecoration(
                        gradient: isSelected ? AppColors.goldGradient : null,
                        color: isSelected ? null : AppColors.surfaceDark,
                        borderRadius: BorderRadius.circular(14),
                        border: Border.all(
                          color: isSelected
                              ? AppColors.primary.withOpacity(0.6)
                              : AppColors.surfaceBorder,
                          width: isSelected ? 1.5 : 1,
                        ),
                        boxShadow: isSelected
                            ? [BoxShadow(color: AppColors.primary.withOpacity(0.25), blurRadius: 12, offset: const Offset(0, 4))]
                            : null,
                      ),
                      child: Row(
                        children: [
                          Text(lang.flag, style: const TextStyle(fontSize: 18)),
                          const SizedBox(width: 8),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              mainAxisAlignment: MainAxisAlignment.center,
                              children: [
                                Text(
                                  lang.nativeName,
                                  style: GoogleFonts.inter(
                                    fontSize: 13,
                                    fontWeight: FontWeight.w700,
                                    color: isSelected ? AppColors.textOnPrimary : Colors.white,
                                  ),
                                  maxLines: 1,
                                  overflow: TextOverflow.ellipsis,
                                ),
                                Text(
                                  lang.name,
                                  style: GoogleFonts.inter(
                                    fontSize: 10,
                                    color: isSelected
                                        ? AppColors.textOnPrimary.withOpacity(0.7)
                                        : AppColors.textDarkMuted,
                                  ),
                                ),
                              ],
                            ),
                          ),
                          if (isSelected)
                            const Icon(Icons.check_circle_rounded, color: AppColors.textOnPrimary, size: 16),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 8),
          ],
        ),
      ),
    );
  }
}

/// A compact horizontal scrollable chip strip for language selection.
class LanguageChipRow extends StatelessWidget {
  final VoidCallback? onLanguageChanged;
  const LanguageChipRow({super.key, this.onLanguageChanged});

  @override
  Widget build(BuildContext context) {
    final langProvider = context.watch<LanguageProvider>();

    return SizedBox(
      height: 42,
      child: ListView.separated(
        scrollDirection: Axis.horizontal,
        padding: const EdgeInsets.symmetric(horizontal: 4),
        itemCount: LanguageProvider.supportedLanguages.length,
        separatorBuilder: (_, __) => const SizedBox(width: 8),
        itemBuilder: (context, index) {
          final lang = LanguageProvider.supportedLanguages[index];
          final isSelected = lang == langProvider.selected;
          return GestureDetector(
            onTap: () {
              context.read<LanguageProvider>().setLanguage(lang);
              onLanguageChanged?.call();
            },
            child: AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 8),
              decoration: BoxDecoration(
                gradient: isSelected ? AppColors.goldGradient : null,
                color: isSelected ? null : AppColors.surfaceDark,
                borderRadius: BorderRadius.circular(20),
                border: Border.all(
                  color: isSelected ? AppColors.primary : AppColors.surfaceBorder,
                  width: isSelected ? 1.5 : 1,
                ),
                boxShadow: isSelected
                    ? [BoxShadow(color: AppColors.primary.withOpacity(0.3), blurRadius: 8)]
                    : null,
              ),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Text(lang.flag, style: const TextStyle(fontSize: 14)),
                  const SizedBox(width: 6),
                  Text(
                    lang.nativeName,
                    style: GoogleFonts.inter(
                      fontSize: 12,
                      fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                      color: isSelected ? AppColors.textOnPrimary : AppColors.textDarkMuted,
                    ),
                  ),
                ],
              ),
            ),
          );
        },
      ),
    );
  }
}
