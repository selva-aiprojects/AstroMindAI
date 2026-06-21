import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

enum ChartStyle { northIndian, southIndian }

class BirthChartWidget extends StatefulWidget {
  final Map<String, dynamic> chartData;

  const BirthChartWidget({
    super.key,
    required this.chartData,
  });

  @override
  State<BirthChartWidget> createState() => _BirthChartWidgetState();
}

class _BirthChartWidgetState extends State<BirthChartWidget> {
  ChartStyle currentStyle = ChartStyle.northIndian;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Colors.orange.withOpacity(0.06),
            blurRadius: 20,
            offset: const Offset(0, 8),
          ),
        ],
        border: Border.all(color: Colors.orange.shade50, width: 1),
      ),
      padding: const Offset(0, 16) == Offset.zero 
          ? const EdgeInsets.all(16) 
          : const EdgeInsets.symmetric(horizontal: 16, vertical: 20),
      child: Column(
        children: [
          _buildStyleToggle(),
          const SizedBox(height: 24),
          AnimatedSwitcher(
            duration: const Duration(milliseconds: 300),
            transitionBuilder: (child, animation) => FadeTransition(
              opacity: animation,
              child: ScaleTransition(scale: animation, child: child),
            ),
            child: currentStyle == ChartStyle.northIndian
                ? _buildNorthIndianChart()
                : _buildSouthIndianChart(),
          ),
        ],
      ),
    );
  }

  Widget _buildStyleToggle() {
    return Container(
      padding: const EdgeInsets.all(4),
      decoration: BoxDecoration(
        color: Colors.orange.shade50.withOpacity(0.5),
        borderRadius: BorderRadius.circular(16),
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
    final isSelected = currentStyle == style;
    return GestureDetector(
      onTap: () => setState(() => currentStyle = style),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 10),
        decoration: BoxDecoration(
          color: isSelected ? Colors.white : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: Colors.orange.withOpacity(0.12),
                    blurRadius: 8,
                    offset: const Offset(0, 2),
                  ),
                ]
              : null,
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              size: 16,
              color: isSelected ? Colors.orange.shade800 : Colors.grey.shade600,
            ),
            const SizedBox(width: 8),
            Text(
              label,
              style: GoogleFonts.inter(
                fontSize: 13,
                fontWeight: isSelected ? FontWeight.w600 : FontWeight.w500,
                color: isSelected ? Colors.orange.shade800 : Colors.grey.shade600,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildNorthIndianChart() {
    return AspectRatio(
      key: const ValueKey('north_indian'),
      aspectRatio: 1.0,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final size = constraints.maxWidth;
          return CustomPaint(
            size: Size(size, size),
            painter: NorthIndianChartPainter(chartData: widget.chartData),
          );
        },
      ),
    );
  }

  Widget _buildSouthIndianChart() {
    return AspectRatio(
      key: const ValueKey('south_indian'),
      aspectRatio: 1.0,
      child: LayoutBuilder(
        builder: (context, constraints) {
          final size = constraints.maxWidth;
          return CustomPaint(
            size: Size(size, size),
            painter: SouthIndianChartPainter(chartData: widget.chartData),
          );
        },
      ),
    );
  }
}

class NorthIndianChartPainter extends CustomPainter {
  final Map<String, dynamic> chartData;

  NorthIndianChartPainter({required this.chartData});

  @override
  void paint(Canvas canvas, Size size) {
    final cx = size.width / 2;
    final cy = size.height / 2;
    final center = Offset(cx, cy);
    final radius = size.width / 2 - 8;

    // Draw background
    final bgPaint = Paint()..color = Colors.orange.shade50.withOpacity(0.1);
    canvas.drawRect(Rect.fromLTWH(8, 8, size.width - 16, size.height - 16), bgPaint);

    // Draw outer square
    final outerPaint = Paint()
      ..color = Colors.orange.shade300
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;
    final outerRect = Rect.fromCircle(center: center, radius: radius);
    canvas.drawRect(outerRect, outerPaint);

    // Draw inner lines (diagonals and diamond)
    final linePaint = Paint()
      ..color = Colors.orange.shade200
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5;

    // Diagonals
    final topLeft = Offset(center.dx - radius, center.dy - radius);
    final topRight = Offset(center.dx + radius, center.dy - radius);
    final bottomLeft = Offset(center.dx - radius, center.dy + radius);
    final bottomRight = Offset(center.dx + radius, center.dy + radius);
    canvas.drawLine(topLeft, bottomRight, linePaint);
    canvas.drawLine(topRight, bottomLeft, linePaint);

    // Diamond midpoints
    final topMid = Offset(center.dx, center.dy - radius);
    final rightMid = Offset(center.dx + radius, center.dy);
    final bottomMid = Offset(center.dx, center.dy + radius);
    final leftMid = Offset(center.dx - radius, center.dy);
    canvas.drawLine(topMid, rightMid, linePaint);
    canvas.drawLine(rightMid, bottomMid, linePaint);
    canvas.drawLine(bottomMid, leftMid, linePaint);
    canvas.drawLine(leftMid, topMid, linePaint);

    // Parse Ascendant sign
    final ascendant = chartData['ascendant'] as double?;
    final int ascSignIndex = ascendant != null ? (ascendant / 30).floor() : 0; // 0 = Aries, etc.

    // Draw house numbers (Vedic style: sign numbers clockwise from Ascendant)
    final houseCentroids = _getHouseCentroids(center, radius);
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    for (int h = 1; h <= 12; h++) {
      final signNumber = (ascSignIndex + h - 1) % 12 + 1;
      textPainter.text = TextSpan(
        text: '$signNumber',
        style: GoogleFonts.inter(
          fontSize: 13,
          fontWeight: FontWeight.bold,
          color: Colors.orange.shade900,
        ),
      );
      textPainter.layout();
      final centroid = houseCentroids[h - 1];
      
      // Draw sign number at the centroid
      textPainter.paint(
        canvas, 
        Offset(centroid.dx - textPainter.width / 2, centroid.dy - textPainter.height - 4),
      );
    }

    // Draw planets in houses
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    final housePlacements = chartData['housePlacements'] as Map<String, dynamic>?;

    if (planetaryPositions != null && housePlacements != null) {
      final Map<int, List<String>> planetsInHouse = {};
      for (var entry in planetaryPositions.entries) {
        final planetName = entry.key;
        final house = housePlacements[planetName] as int? ?? 1;
        planetsInHouse.putIfAbsent(house, () => []).add(planetName);
      }

      for (int h = 1; h <= 12; h++) {
        final list = planetsInHouse[h] ?? [];
        if (list.isEmpty) continue;

        final centroid = houseCentroids[h - 1];
        
        // Render planet abbreviations as a neat comma separated list
        final textSpans = <TextSpan>[];
        for (int i = 0; i < list.length; i++) {
          final planet = list[i];
          final pos = planetaryPositions[planet] as Map<String, dynamic>? ?? {};
          final abbrev = _getPlanetAbbreviations()[planet] ?? planet.substring(0, 2);
          final isRetro = pos['isRetrograde'] == true;
          final isCombust = pos['isCombust'] == true;
          
          textSpans.add(TextSpan(
            text: isRetro ? '$abbrev(R)' : abbrev,
            style: GoogleFonts.inter(
              fontSize: 10,
              fontWeight: FontWeight.bold,
              color: isCombust ? Colors.red.shade700 : Colors.grey.shade900,
            ),
          ));

          if (i < list.length - 1) {
            textSpans.add(TextSpan(
              text: ' ',
              style: GoogleFonts.inter(fontSize: 10, color: Colors.grey.shade600),
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
    final r = radius;

    // Centroids calculated for each house triangle
    return [
      Offset(cx, cy - r / 3.8), // House 1 (Ascendant, top central triangle)
      Offset(cx - r / 2.3, cy - r * 0.72), // House 2
      Offset(cx - r * 0.72, cy - r / 2.3), // House 3
      Offset(cx - r / 3.8, cy), // House 4 (left central triangle)
      Offset(cx - r * 0.72, cy + r / 2.3), // House 5
      Offset(cx - r / 2.3, cy + r * 0.72), // House 6
      Offset(cx, cy + r / 3.8), // House 7 (bottom central triangle)
      Offset(cx + r / 2.3, cy + r * 0.72), // House 8
      Offset(cx + r * 0.72, cy + r / 2.3), // House 9
      Offset(cx + r / 3.8, cy), // House 10 (right central triangle)
      Offset(cx + r * 0.72, cy - r / 2.3), // House 11
      Offset(cx + r / 2.3, cy - r * 0.72), // House 12
    ];
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

class SouthIndianChartPainter extends CustomPainter {
  final Map<String, dynamic> chartData;

  SouthIndianChartPainter({required this.chartData});

  @override
  void paint(Canvas canvas, Size size) {
    final w = size.width;
    final h = size.height;
    final cellSize = w / 4;

    // Draw outer border
    final outerPaint = Paint()
      ..color = Colors.orange.shade300
      ..style = PaintingStyle.stroke
      ..strokeWidth = 3;
    canvas.drawRect(Rect.fromLTWH(4, 4, w - 8, h - 8), outerPaint);

    // Draw cell grid lines
    final linePaint = Paint()
      ..color = Colors.orange.shade200
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1.5;

    // Horizontal grid lines
    canvas.drawLine(Offset(4, cellSize), Offset(w - 4, cellSize), linePaint);
    canvas.drawLine(Offset(4, cellSize * 2), Offset(cellSize, cellSize * 2), linePaint);
    canvas.drawLine(Offset(cellSize * 3, cellSize * 2), Offset(w - 4, cellSize * 2), linePaint);
    canvas.drawLine(Offset(4, cellSize * 3), Offset(w - 4, cellSize * 3), linePaint);

    // Vertical grid lines
    canvas.drawLine(Offset(cellSize, 4), Offset(cellSize, h - 4), linePaint);
    canvas.drawLine(Offset(cellSize * 2, 4), Offset(cellSize * 2, cellSize), linePaint);
    canvas.drawLine(Offset(cellSize * 2, cellSize * 3), Offset(cellSize * 2, h - 4), linePaint);
    canvas.drawLine(Offset(cellSize * 3, 4), Offset(cellSize * 3, h - 4), linePaint);

    // Center fill for brand info
    final centerRect = Rect.fromLTWH(cellSize + 1, cellSize + 1, cellSize * 2 - 2, cellSize * 2 - 2);
    final centerPaint = Paint()..color = Colors.orange.shade50.withOpacity(0.15);
    canvas.drawRect(centerRect, centerPaint);

    // Draw Center text
    final centerPainter = TextPainter(textDirection: TextDirection.ltr);
    centerPainter.text = TextSpan(
      text: 'AstraMindAI',
      style: GoogleFonts.inter(
        fontSize: 14,
        fontWeight: FontWeight.bold,
        color: Colors.orange.shade900,
        letterSpacing: 0.5,
      ),
    );
    centerPainter.layout();
    centerPainter.paint(
      canvas,
      Offset(w / 2 - centerPainter.width / 2, h / 2 - centerPainter.height - 2),
    );

    centerPainter.text = TextSpan(
      text: 'Birth Chart',
      style: GoogleFonts.inter(
        fontSize: 10,
        fontWeight: FontWeight.w500,
        color: Colors.orange.shade700,
      ),
    );
    centerPainter.layout();
    centerPainter.paint(
      canvas,
      Offset(w / 2 - centerPainter.width / 2, h / 2 + 4),
    );

    // Parse Ascendant sign
    final ascendant = chartData['ascendant'] as double?;
    final int ascSignIndex = ascendant != null ? (ascendant / 30).floor() : 0; // 0 = Aries, etc.

    // Signs list in fixed South Indian order (clockwise starting from Pisces at top-left)
    final signOrder = [
      11, // Pisces (Index 11, 12th sign)
      0,  // Aries (Index 0, 1st sign)
      1,  // Taurus
      2,  // Gemini
      3,  // Cancer (Row 1, Col 3)
      4,  // Leo (Row 2, Col 3)
      5,  // Virgo (Row 3, Col 3)
      6,  // Libra
      7,  // Scorpio
      8,  // Sagittarius
      9,  // Capricorn
      10, // Aquarius
    ];

    // Map each of the 12 signs to cell coordinates (row, col)
    final cellCoords = [
      const Point(0, 0), // Pisces
      const Point(0, 1), // Aries
      const Point(0, 2), // Taurus
      const Point(0, 3), // Gemini
      const Point(1, 3), // Cancer
      const Point(2, 3), // Leo
      const Point(3, 3), // Virgo
      const Point(3, 2), // Libra
      const Point(3, 1), // Scorpio
      const Point(3, 0), // Sagittarius
      const Point(2, 0), // Capricorn
      const Point(1, 0), // Aquarius
    ];

    final signNames = ['AR', 'TA', 'GE', 'CN', 'LE', 'VI', 'LI', 'SC', 'SG', 'CP', 'AQ', 'PI'];
    final textPainter = TextPainter(textDirection: TextDirection.ltr);

    // Render each sign cell
    for (int i = 0; i < 12; i++) {
      final signIdx = signOrder[i];
      final coord = cellCoords[i];
      final cellX = coord.y * cellSize;
      final cellY = coord.x * cellSize;

      // Draw sign name in the corner
      textPainter.text = TextSpan(
        text: signNames[signIdx],
        style: GoogleFonts.inter(
          fontSize: 9,
          fontWeight: FontWeight.bold,
          color: Colors.orange.shade200,
        ),
      );
      textPainter.layout();
      textPainter.paint(canvas, Offset(cellX + 6, cellY + 6));

      // Draw Ascendant highlight (ASC / diagonal slash)
      if (signIdx == ascSignIndex) {
        // Gold diagonal slash in Ascendant sign cell
        final slashPaint = Paint()
          ..color = Colors.amber.shade700.withOpacity(0.5)
          ..style = PaintingStyle.stroke
          ..strokeWidth = 2;
        canvas.drawLine(
          Offset(cellX + 8, cellY + cellSize - 8),
          Offset(cellX + cellSize - 8, cellY + 8),
          slashPaint,
        );

        // Draw 'ASC' text
        textPainter.text = TextSpan(
          text: 'ASC',
          style: GoogleFonts.inter(
            fontSize: 9,
            fontWeight: FontWeight.bold,
            color: Colors.amber.shade800,
          ),
        );
        textPainter.layout();
        textPainter.paint(canvas, Offset(cellX + cellSize - textPainter.width - 6, cellY + 6));
      }
    }

    // Draw planets in sign cells
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    if (planetaryPositions != null) {
      final Map<int, List<String>> planetsInSign = {};
      for (var entry in planetaryPositions.entries) {
        final planetName = entry.key;
        final pos = entry.value as Map<String, dynamic>? ?? {};
        final longitude = pos['longitude'] as double? ?? 0.0;
        final signIndex = (longitude / 30).floor() % 12;
        planetsInSign.putIfAbsent(signIndex, () => []).add(planetName);
      }

      for (int i = 0; i < 12; i++) {
        final signIdx = signOrder[i];
        final list = planetsInSign[signIdx] ?? [];
        if (list.isEmpty) continue;

        final coord = cellCoords[i];
        final cellX = coord.y * cellSize;
        final cellY = coord.x * cellSize;

        // Draw planets wrapped inside cell
        final textSpans = <TextSpan>[];
        for (int j = 0; j < list.length; j++) {
          final planet = list[j];
          final pos = planetaryPositions[planet] as Map<String, dynamic>? ?? {};
          final abbrev = _getPlanetAbbreviations()[planet] ?? planet.substring(0, 2);
          final isRetro = pos['isRetrograde'] == true;
          final isCombust = pos['isCombust'] == true;

          textSpans.add(TextSpan(
            text: isRetro ? '$abbrev(R)' : abbrev,
            style: GoogleFonts.inter(
              fontSize: 9.5,
              fontWeight: FontWeight.bold,
              color: isCombust ? Colors.red.shade700 : Colors.grey.shade900,
            ),
          ));

          if (j < list.length - 1) {
            textSpans.add(TextSpan(
              text: ' ',
              style: GoogleFonts.inter(fontSize: 9.5),
            ));
          }
        }

        textPainter.text = TextSpan(children: textSpans);
        textPainter.layout(maxWidth: cellSize - 12);
        
        // Center the list inside the cell vertically and horizontally
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
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

class Point {
  final int x;
  final int y;
  const Point(this.x, this.y);
}

Map<String, String> _getPlanetAbbreviations() {
  return {
    'Sun': 'Su',
    'Moon': 'Mo',
    'Mars': 'Ma',
    'Mercury': 'Me',
    'Jupiter': 'Ju',
    'Venus': 'Ve',
    'Saturn': 'Sa',
    'Rahu': 'Ra',
    'Ketu': 'Ke',
  };
}
