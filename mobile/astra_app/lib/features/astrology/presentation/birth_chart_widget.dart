import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import '../../../core/theme/app_theme.dart';

enum ChartStyle { northIndian, southIndian }
enum ChartType  { rashi, navamsa, dasamsa }

// ─── Chart type description text ────────────────────────────────────────────
const _chartDescriptions = {
  ChartType.rashi:   'Rashi (D1) — The natal birth chart showing planetary positions at birth. Foundation of all Vedic analysis.',
  ChartType.navamsa: 'Navamsa (D9) — Divisional chart for marriage, dharma & spiritual strength. Each sign is split into 9 equal parts of 3°20\'.',
  ChartType.dasamsa: 'Dasamsa (D10) — Divisional chart for career, fame & professional life. Each sign is split into 10 equal parts of 3°.',
};

// ─── Navamsa sign calculation ────────────────────────────────────────────────
/// Returns the 0-based Navamsa sign index (0=Aries … 11=Pisces) for a given
/// ecliptic longitude (degrees, 0–360).
int _navamsaSign(double longitude) {
  final signIndex  = (longitude / 30).floor() % 12;      // 0–11
  final degree     = longitude % 30;                      // 0–30
  final pada       = (degree / (30 / 9)).floor();          // 0–8 (navamsa pada)
  // Start sign within the navamsa cycle depends on the triplicty of the natal sign:
  // Fire (0,4,8)  → start 0(Aries), Earth (1,5,9) → start 9(Capricorn),
  // Air (2,6,10)  → start 6(Libra),  Water (3,7,11) → start 3(Cancer)
  const starts = [0, 9, 6, 3, 0, 9, 6, 3, 0, 9, 6, 3];
  return (starts[signIndex] + pada) % 12;
}

// ─── Dasamsa sign calculation ────────────────────────────────────────────────
/// Returns the 0-based Dasamsa sign index for a given longitude.
int _dasamsaSign(double longitude) {
  final signIndex = (longitude / 30).floor() % 12;       // 0–11
  final degree    = longitude % 30;                       // 0–30
  final division  = (degree / 3).floor();                 // 0–9
  // Odd signs (1,3,5,7,9,11 i.e. index 0,2,4,6,8,10) start from own sign.
  // Even signs (2,4,6,8,10,12 i.e. index 1,3,5,7,9,11) start from 9th from own.
  final isOdd = signIndex % 2 == 0;
  final startSign = isOdd ? signIndex : (signIndex + 9) % 12;
  return (startSign + division) % 12;
}

// ─── Compute planet → sign map for a given divisional chart ─────────────────
Map<String, double> _computeDivisionalLongitudes(
  Map<String, dynamic> planetaryPositions,
  ChartType type,
) {
  final result = <String, double>{};
  for (final entry in planetaryPositions.entries) {
    final pos = entry.value as Map<String, dynamic>? ?? {};
    final raw = (pos['longitude'] as num?)?.toDouble() ?? 0.0;
    if (type == ChartType.navamsa) {
      result[entry.key] = _navamsaSign(raw).toDouble() * 30 + 15; // centroid of sign
    } else if (type == ChartType.dasamsa) {
      result[entry.key] = _dasamsaSign(raw).toDouble() * 30 + 15;
    } else {
      result[entry.key] = raw;
    }
  }
  return result;
}

// ─── Build chartData compatible map for divisional charts ────────────────────
Map<String, dynamic> _buildDivisionalChartData(
  Map<String, dynamic> originalChartData,
  ChartType type,
) {
  if (type == ChartType.rashi) return originalChartData;

  final originalPositions =
      originalChartData['planetaryPositions'] as Map<String, dynamic>? ?? {};
  final divisionalLongitudes =
      _computeDivisionalLongitudes(originalPositions, type);

  // Rebuild planetaryPositions with new longitudes
  final newPositions = <String, dynamic>{};
  for (final planet in originalPositions.keys) {
    final orig = originalPositions[planet] as Map<String, dynamic>? ?? {};
    final newLon = divisionalLongitudes[planet] ?? 0.0;
    final signIdx = (newLon / 30).floor() % 12;
    const signNames = [
      'Aries','Taurus','Gemini','Cancer','Leo','Virgo',
      'Libra','Scorpio','Sagittarius','Capricorn','Aquarius','Pisces'
    ];
    newPositions[planet] = {
      ...orig,
      'longitude': newLon,
      'sign': signNames[signIdx],
    };
  }

  // Compute house placements from divisional ascendant
  final ascLon = (originalChartData['ascendant'] as num?)?.toDouble() ?? 0.0;
  final divAscLon = type == ChartType.navamsa
      ? _navamsaSign(ascLon).toDouble() * 30
      : _dasamsaSign(ascLon).toDouble() * 30;
  final ascSignIdx = (divAscLon / 30).floor() % 12;

  final housePlacements = <String, int>{};
  for (final planet in newPositions.keys) {
    final pLon = (newPositions[planet]['longitude'] as num).toDouble();
    final pSign = (pLon / 30).floor() % 12;
    housePlacements[planet] = ((pSign - ascSignIdx + 12) % 12) + 1;
  }

  return {
    ...originalChartData,
    'ascendant': divAscLon,
    'planetaryPositions': newPositions,
    'housePlacements': housePlacements,
  };
}

// ────────────────────────────────────────────────────────────────────────────
class BirthChartWidget extends StatefulWidget {
  final Map<String, dynamic> chartData;

  const BirthChartWidget({
    super.key,
    required this.chartData,
  });

  @override
  State<BirthChartWidget> createState() => _BirthChartWidgetState();
}

class _BirthChartWidgetState extends State<BirthChartWidget>
    with SingleTickerProviderStateMixin {
  ChartStyle _style = ChartStyle.northIndian;
  ChartType  _type  = ChartType.rashi;
  late TabController _typeController;

  @override
  void initState() {
    super.initState();
    _typeController = TabController(length: 3, vsync: this);
    _typeController.addListener(() {
      if (!_typeController.indexIsChanging) {
        setState(() {
          _type = ChartType.values[_typeController.index];
        });
      }
    });
  }

  @override
  void dispose() {
    _typeController.dispose();
    super.dispose();
  }

  Map<String, dynamic> get _activeChartData =>
      _buildDivisionalChartData(widget.chartData, _type);

  LinearGradient get _activeGradient {
    switch (_type) {
      case ChartType.rashi:   return AppColors.cosmicGradient;
      case ChartType.navamsa: return AppColors.navamsaGradient;
      case ChartType.dasamsa: return AppColors.dasamsaGradient;
    }
  }

  Color get _accentColor {
    switch (_type) {
      case ChartType.rashi:   return AppColors.primary;
      case ChartType.navamsa: return AppColors.secondary;
      case ChartType.dasamsa: return AppColors.accent;
    }
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            _activeGradient.colors.first.withOpacity(0.15),
            AppColors.backgroundDark,
          ],
          begin: Alignment.topCenter,
          end: Alignment.bottomCenter,
        ),
        borderRadius: BorderRadius.circular(24),
        border: Border.all(
          color: _accentColor.withOpacity(0.25),
          width: 1.5,
        ),
        boxShadow: [
          BoxShadow(
            color: _accentColor.withOpacity(0.1),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
      child: Column(
        children: [
          _buildChartTypeTabBar(),
          const SizedBox(height: 16),
          _buildChartDescription(),
          const SizedBox(height: 16),
          _buildStyleToggle(),
          const SizedBox(height: 20),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 400),
            transitionBuilder: (child, animation) => FadeTransition(
              opacity: animation,
              child: ScaleTransition(
                scale: Tween<double>(begin: 0.95, end: 1.0).animate(animation),
                child: child,
              ),
            ),
            child: _style == ChartStyle.northIndian
                ? _buildNorthIndianChart()
                : _buildSouthIndianChart(),
          ),
        ],
      ),
    );
  }

  Widget _buildChartTypeTabBar() {
    return Container(
      height: 44,
      decoration: BoxDecoration(
        color: AppColors.backgroundDark.withOpacity(0.6),
        borderRadius: BorderRadius.circular(22),
        border: Border.all(color: AppColors.surfaceBorder),
      ),
      child: TabBar(
        controller: _typeController,
        padding: const EdgeInsets.all(4),
        indicator: BoxDecoration(
          gradient: _activeGradient,
          borderRadius: BorderRadius.circular(18),
          boxShadow: [
            BoxShadow(
              color: _accentColor.withOpacity(0.4),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        indicatorSize: TabBarIndicatorSize.tab,
        labelColor: Colors.white,
        unselectedLabelColor: AppColors.textDarkMuted,
        dividerColor: Colors.transparent,
        labelStyle: GoogleFonts.inter(fontWeight: FontWeight.w700, fontSize: 12),
        unselectedLabelStyle: GoogleFonts.inter(fontWeight: FontWeight.w500, fontSize: 12),
        tabs: const [
          Tab(text: 'Rashi'),
          Tab(text: 'Navamsa'),
          Tab(text: 'Dasamsa'),
        ],
      ),
    );
  }

  Widget _buildChartDescription() {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
      decoration: BoxDecoration(
        color: _accentColor.withOpacity(0.08),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: _accentColor.withOpacity(0.2)),
      ),
      child: AnimatedSwitcher(
        duration: const Duration(milliseconds: 300),
        child: Text(
          _chartDescriptions[_type]!,
          key: ValueKey(_type),
          style: GoogleFonts.inter(
            fontSize: 12,
            color: Colors.white.withOpacity(0.8),
            height: 1.5,
          ),
          textAlign: TextAlign.center,
        ),
      ),
    );
  }

  Widget _buildStyleToggle() {
    return Container(
      padding: const EdgeInsets.all(3),
      decoration: BoxDecoration(
        color: AppColors.surfaceDark,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: AppColors.surfaceBorder),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          _buildToggleOption(ChartStyle.northIndian, 'North Indian', Icons.grid_goldenratio),
          _buildToggleOption(ChartStyle.southIndian, 'South Indian', Icons.grid_view_rounded),
        ],
      ),
    );
  }

  Widget _buildToggleOption(ChartStyle style, String label, IconData icon) {
    final isSelected = _style == style;
    return GestureDetector(
      onTap: () => setState(() => _style = style),
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 9),
        decoration: BoxDecoration(
          color: isSelected ? _accentColor.withOpacity(0.9) : Colors.transparent,
          borderRadius: BorderRadius.circular(11),
          boxShadow: isSelected
              ? [BoxShadow(color: _accentColor.withOpacity(0.3), blurRadius: 8, offset: const Offset(0, 2))]
              : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 14,
              color: isSelected ? Colors.white : AppColors.textDarkMuted,
            ),
            const SizedBox(width: 6),
            Text(
              label,
              style: GoogleFonts.inter(
                fontSize: 12,
                fontWeight: isSelected ? FontWeight.w700 : FontWeight.w500,
                color: isSelected ? Colors.white : AppColors.textDarkMuted,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNorthIndianChart() {
    return AspectRatio(
      key: ValueKey('north_${_type.name}'),
      aspectRatio: 1.0,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final size = constraints.maxWidth;
          return CustomPaint(
            size: Size(size, size),
            painter: NorthIndianChartPainter(
              chartData:   _activeChartData,
              accentColor: _accentColor,
              chartLabel:  _chartLabel,
            ),
          );
        },
      ),
    );
  }

  Widget _buildSouthIndianChart() {
    return AspectRatio(
      key: ValueKey('south_${_type.name}'),
      aspectRatio: 1.0,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final size = constraints.maxWidth;
          return CustomPaint(
            size: Size(size, size),
            painter: SouthIndianChartPainter(
              chartData:   _activeChartData,
              accentColor: _accentColor,
              chartLabel:  _chartLabel,
            ),
          );
        },
      ),
    );
  }

  String get _chartLabel {
    switch (_type) {
      case ChartType.rashi:   return 'Rashi (D1)';
      case ChartType.navamsa: return 'Navamsa (D9)';
      case ChartType.dasamsa: return 'Dasamsa (D10)';
    }
  }
}

// ─── North Indian Chart Painter ──────────────────────────────────────────────
class NorthIndianChartPainter extends CustomPainter {
  final Map<String, dynamic> chartData;
  final Color accentColor;
  final String chartLabel;

  NorthIndianChartPainter({
    required this.chartData,
    required this.accentColor,
    required this.chartLabel,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final cx = size.width / 2;
    final cy = size.height / 2;
    final center = Offset(cx, cy);
    final radius = size.width / 2 - 8;

    // Background fill
    final bgPaint = Paint()..color = accentColor.withOpacity(0.04);
    canvas.drawRect(Rect.fromLTWH(8, 8, size.width - 16, size.height - 16), bgPaint);

    // Outer square
    final outerPaint = Paint()
      ..color = accentColor.withOpacity(0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.5;
    final outerRect = Rect.fromCircle(center: center, radius: radius);
    canvas.drawRect(outerRect, outerPaint);

    // Inner line grid
    final linePaint = Paint()
      ..color = accentColor.withOpacity(0.3)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.2;

    final topLeft     = Offset(center.dx - radius, center.dy - radius);
    final topRight    = Offset(center.dx + radius, center.dy - radius);
    final bottomLeft  = Offset(center.dx - radius, center.dy + radius);
    final bottomRight = Offset(center.dx + radius, center.dy + radius);
    canvas.drawLine(topLeft, bottomRight, linePaint);
    canvas.drawLine(topRight, bottomLeft, linePaint);

    final topMid    = Offset(center.dx, center.dy - radius);
    final rightMid  = Offset(center.dx + radius, center.dy);
    final bottomMid = Offset(center.dx, center.dy + radius);
    final leftMid   = Offset(center.dx - radius, center.dy);
    canvas.drawLine(topMid, rightMid, linePaint);
    canvas.drawLine(rightMid, bottomMid, linePaint);
    canvas.drawLine(bottomMid, leftMid, linePaint);
    canvas.drawLine(leftMid, topMid, linePaint);

    // Chart label in centre
    final centerPainter = TextPainter(textDirection: TextDirection.ltr);
    centerPainter.text = TextSpan(
      text: chartLabel,
      style: GoogleFonts.inter(
        fontSize: 10,
        fontWeight: FontWeight.w600,
        color: accentColor.withOpacity(0.6),
      ),
    );
    centerPainter.layout();
    centerPainter.paint(
      canvas,
      Offset(cx - centerPainter.width / 2, cy - centerPainter.height / 2),
    );

    // House numbers
    final ascendant = chartData['ascendant'] as double?;
    final int ascSignIndex = ascendant != null ? (ascendant / 30).floor() : 0;
    final houseCentroids = _getHouseCentroids(center, radius);
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    for (int h = 1; h <= 12; h++) {
      final signNumber = (ascSignIndex + h - 1) % 12 + 1;
      textPainter.text = TextSpan(
        text: '$signNumber',
        style: GoogleFonts.inter(
          fontSize: 13,
          fontWeight: FontWeight.bold,
          color: accentColor.withOpacity(0.85),
        ),
      );
      textPainter.layout();
      final centroid = houseCentroids[h - 1];
      textPainter.paint(
        canvas,
        Offset(centroid.dx - textPainter.width / 2, centroid.dy - textPainter.height - 4),
      );
    }

    // Planets
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    final housePlacements    = chartData['housePlacements']    as Map<String, dynamic>?;
    if (planetaryPositions != null && housePlacements != null) {
      final Map<int, List<String>> planetsInHouse = {};
      for (var entry in planetaryPositions.entries) {
        final house = housePlacements[entry.key] as int? ?? 1;
        planetsInHouse.putIfAbsent(house, () => []).add(entry.key);
      }
      for (int h = 1; h <= 12; h++) {
        final list = planetsInHouse[h] ?? [];
        if (list.isEmpty) continue;
        final centroid = houseCentroids[h - 1];
        final textSpans = <TextSpan>[];
        for (int i = 0; i < list.length; i++) {
          final planet = list[i];
          final pos    = planetaryPositions[planet] as Map<String, dynamic>? ?? {};
          final abbrev = _getPlanetAbbreviations()[planet] ?? planet.substring(0, 2);
          final isRetro = pos['isRetrograde'] == true;
          textSpans.add(TextSpan(
            text: isRetro ? '$abbrev®' : abbrev,
            style: GoogleFonts.inter(
              fontSize: 10,
              fontWeight: FontWeight.bold,
              color: (pos['isCombust'] == true) ? Colors.red.shade400 : Colors.white.withOpacity(0.95),
            ),
          ));
          if (i < list.length - 1) {
            textSpans.add(TextSpan(
              text: ' ',
              style: GoogleFonts.inter(fontSize: 10, color: Colors.white38),
            ));
          }
        }
        textPainter.text = TextSpan(children: textSpans);
        textPainter.layout(maxWidth: radius * 0.7);
        textPainter.paint(
          canvas,
          Offset(centroid.dx - textPainter.width / 2, centroid.dy + 2),
        );
      }
    }
  }

  List<Offset> _getHouseCentroids(Offset center, double radius) {
    final cx = center.dx;
    final cy = center.dy;
    final r  = radius;
    return [
      Offset(cx, cy - r / 3.8),
      Offset(cx - r / 2.3, cy - r * 0.72),
      Offset(cx - r * 0.72, cy - r / 2.3),
      Offset(cx - r / 3.8, cy),
      Offset(cx - r * 0.72, cy + r / 2.3),
      Offset(cx - r / 2.3, cy + r * 0.72),
      Offset(cx, cy + r / 3.8),
      Offset(cx + r / 2.3, cy + r * 0.72),
      Offset(cx + r * 0.72, cy + r / 2.3),
      Offset(cx + r / 3.8, cy),
      Offset(cx + r * 0.72, cy - r / 2.3),
      Offset(cx + r / 2.3, cy - r * 0.72),
    ];
  }

  @override
  bool shouldRepaint(covariant NorthIndianChartPainter old) =>
      old.chartData != chartData || old.accentColor != accentColor;
}

// ─── South Indian Chart Painter ──────────────────────────────────────────────
class SouthIndianChartPainter extends CustomPainter {
  final Map<String, dynamic> chartData;
  final Color accentColor;
  final String chartLabel;

  SouthIndianChartPainter({
    required this.chartData,
    required this.accentColor,
    required this.chartLabel,
  });

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;
    final cellSize = w / 4;

    // Outer border
    final outerPaint = Paint()
      ..color = accentColor.withOpacity(0.6)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2.5;
    canvas.drawRect(Rect.fromLTWH(4, 4, w - 8, h - 8), outerPaint);

    // Grid lines
    final linePaint = Paint()
      ..color = accentColor.withOpacity(0.25)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.2;

    canvas.drawLine(Offset(4, cellSize), Offset(w - 4, cellSize), linePaint);
    canvas.drawLine(Offset(4, cellSize * 2), Offset(cellSize, cellSize * 2), linePaint);
    canvas.drawLine(Offset(cellSize * 3, cellSize * 2), Offset(w - 4, cellSize * 2), linePaint);
    canvas.drawLine(Offset(4, cellSize * 3), Offset(w - 4, cellSize * 3), linePaint);
    canvas.drawLine(Offset(cellSize, 4), Offset(cellSize, h - 4), linePaint);
    canvas.drawLine(Offset(cellSize * 2, 4), Offset(cellSize * 2, cellSize), linePaint);
    canvas.drawLine(Offset(cellSize * 2, cellSize * 3), Offset(cellSize * 2, h - 4), linePaint);
    canvas.drawLine(Offset(cellSize * 3, 4), Offset(cellSize * 3, h - 4), linePaint);

    // Center fill
    final centerRect = Rect.fromLTWH(cellSize + 1, cellSize + 1, cellSize * 2 - 2, cellSize * 2 - 2);
    canvas.drawRect(centerRect, Paint()..color = accentColor.withOpacity(0.06));

    // Center text
    final centerPainter = TextPainter(textDirection: TextDirection.ltr);
    centerPainter.text = TextSpan(
      text: 'AstraMindAI',
      style: GoogleFonts.inter(
        fontSize: 13, fontWeight: FontWeight.bold,
        color: accentColor.withOpacity(0.8), letterSpacing: 0.5,
      ),
    );
    centerPainter.layout();
    centerPainter.paint(canvas, Offset(w / 2 - centerPainter.width / 2, h / 2 - centerPainter.height - 4));

    centerPainter.text = TextSpan(
      text: chartLabel,
      style: GoogleFonts.inter(fontSize: 9, fontWeight: FontWeight.w500, color: accentColor.withOpacity(0.6)),
    );
    centerPainter.layout();
    centerPainter.paint(canvas, Offset(w / 2 - centerPainter.width / 2, h / 2 + 4));

    // Sign layout (fixed South Indian — Pisces top-left)
    final ascendant  = chartData['ascendant'] as double?;
    final int ascIdx = ascendant != null ? (ascendant / 30).floor() : 0;

    const signOrder = [11, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
    final cellCoords = [
      _P(0, 0), _P(0, 1), _P(0, 2), _P(0, 3),
      _P(1, 3), _P(2, 3), _P(3, 3), _P(3, 2),
      _P(3, 1), _P(3, 0), _P(2, 0), _P(1, 0),
    ];
    const signNames = ['AR','TA','GE','CN','LE','VI','LI','SC','SG','CP','AQ','PI'];
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    // Build planet map by sign
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    final Map<int, List<String>> planetsInSign = {};
    if (planetaryPositions != null) {
      for (var entry in planetaryPositions.entries) {
        final pos     = entry.value as Map<String, dynamic>? ?? {};
        final lon     = (pos['longitude'] as num?)?.toDouble() ?? 0.0;
        final signIdx = (lon / 30).floor() % 12;
        planetsInSign.putIfAbsent(signIdx, () => []).add(entry.key);
      }
    }

    for (int i = 0; i < 12; i++) {
      final signIdx = signOrder[i];
      final coord   = cellCoords[i];
      final cellX   = coord.y * cellSize;
      final cellY   = coord.x * cellSize;

      // Sign label
      textPainter.text = TextSpan(
        text: signNames[signIdx],
        style: GoogleFonts.inter(
          fontSize: 9, fontWeight: FontWeight.bold,
          color: accentColor.withOpacity(0.7),
        ),
      );
      textPainter.layout();
      textPainter.paint(canvas, Offset(cellX + 6, cellY + 6));

      // Ascendant highlight
      if (signIdx == ascIdx) {
        final slashPaint = Paint()
          ..color = accentColor.withOpacity(0.5)
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        canvas.drawLine(
          Offset(cellX + 8, cellY + cellSize - 8),
          Offset(cellX + cellSize - 8, cellY + 8),
          slashPaint,
        );
        textPainter.text = TextSpan(
          text: 'ASC',
          style: GoogleFonts.inter(fontSize: 9, fontWeight: FontWeight.bold, color: accentColor),
        );
        textPainter.layout();
        textPainter.paint(canvas, Offset(cellX + cellSize - textPainter.width - 6, cellY + 6));
      }

      // Planets
      final list = planetsInSign[signIdx] ?? [];
      if (list.isNotEmpty && planetaryPositions != null) {
        final textSpans = <TextSpan>[];
        for (int j = 0; j < list.length; j++) {
          final planet  = list[j];
          final pos     = planetaryPositions[planet] as Map<String, dynamic>? ?? {};
          final abbrev  = _getPlanetAbbreviations()[planet] ?? planet.substring(0, 2);
          final isRetro = pos['isRetrograde'] == true;
          textSpans.add(TextSpan(
            text: isRetro ? '$abbrev®' : abbrev,
            style: GoogleFonts.inter(
              fontSize: 9.5, fontWeight: FontWeight.bold,
              color: (pos['isCombust'] == true) ? Colors.red.shade400 : Colors.white.withOpacity(0.95),
            ),
          ));
          if (j < list.length - 1) {
            textSpans.add(TextSpan(text: ' ', style: GoogleFonts.inter(fontSize: 9.5)));
          }
        }
        textPainter.text = TextSpan(children: textSpans);
        textPainter.layout(maxWidth: cellSize - 12);
        textPainter.paint(
          canvas,
          Offset(
            cellX + (cellSize - textPainter.width) / 2,
            cellY + (cellSize - textPainter.height) / 2 + 4,
          ),
        );
      }
    }
  }

  @override
  bool shouldRepaint(covariant SouthIndianChartPainter old) =>
      old.chartData != chartData || old.accentColor != accentColor;
}

class _P {
  final int x, y;
  const _P(this.x, this.y);
}

Map<String, String> _getPlanetAbbreviations() {
  return {
    'Sun': 'Su', 'Moon': 'Mo', 'Mars': 'Ma', 'Mercury': 'Me',
    'Jupiter': 'Ju', 'Venus': 'Ve', 'Saturn': 'Sa',
    'Rahu': 'Ra', 'Ketu': 'Ke',
  };
}
