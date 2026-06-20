import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

enum ChartStyle { northIndian, southIndian }

class BirthChartWidget extends StatefulWidget {
  final Map<String, dynamic> chartData;
  final ChartStyle style;

  const BirthChartWidget({
    super.key,
    required this.chartData,
    this.style = ChartStyle.northIndian,
  });

  @override
  State<BirthChartWidget> createState() => _BirthChartWidgetState();
}

class _BirthChartWidgetState extends State<BirthChartWidget> {
  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        _buildStyleToggle(),
        const SizedBox(height: 16),
        widget.style == ChartStyle.northIndian
            ? _buildNorthIndianChart()
            : _buildSouthIndianChart(),
      ],
    );
  }

  Widget _buildStyleToggle() {
    return SegmentedButton<ChartStyle>(
      segments: const [
        ButtonSegment(
          value: ChartStyle.northIndian,
          label: Text('North Indian'),
          icon: Icon(Icons.north),
        ),
        ButtonSegment(
          value: ChartStyle.southIndian,
          label: Text('South Indian'),
          icon: Icon(Icons.south),
        ),
      ],
      selected: {widget.style},
      onSelectionChanged: (Set<ChartStyle> newSelection) {
        setState(() {
          // In production, update parent state
        });
      },
    );
  }

  Widget _buildNorthIndianChart() {
    return Container(
      width: 350,
      height: 350,
      child: CustomPaint(
        painter: NorthIndianChartPainter(chartData: widget.chartData),
      ),
    );
  }

  Widget _buildSouthIndianChart() {
    return Container(
      width: 350,
      height: 350,
      child: CustomPaint(
        painter: SouthIndianChartPainter(chartData: widget.chartData),
      ),
    );
  }
}

class NorthIndianChartPainter extends CustomPainter {
  final Map<String, dynamic> chartData;

  NorthIndianChartPainter({required this.chartData});

  @override
  void paint(Canvas canvas, Size size) {
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 20;

    // Draw outer square
    _drawSquare(canvas, center, radius);

    // Draw inner triangles
    _drawTriangles(canvas, center, radius);

    // Draw house numbers
    _drawHouseNumbers(canvas, center, radius);

    // Draw planets
    _drawPlanets(canvas, center, radius);
  }

  void _drawSquare(Canvas canvas, Offset center, double radius) {
    final paint = Paint()
      ..color = Colors.deepPurple
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;

    final rect = Rect.fromCircle(center: center, radius: radius);
    canvas.drawRect(rect, paint);
  }

  void _drawTriangles(Canvas canvas, Offset center, double radius) {
    final paint = Paint()
      ..color = Colors.deepPurple.withOpacity(0.5)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;

    // Draw diagonal lines
    final topLeft = Offset(center.dx - radius, center.dy - radius);
    final topRight = Offset(center.dx + radius, center.dy - radius);
    final bottomLeft = Offset(center.dx - radius, center.dy + radius);
    final bottomRight = Offset(center.dx + radius, center.dy + radius);

    canvas.drawLine(topLeft, bottomRight, paint);
    canvas.drawLine(topRight, bottomLeft, paint);

    // Draw horizontal and vertical lines
    canvas.drawLine(Offset(center.dx - radius, center.dy), Offset(center.dx + radius, center.dy), paint);
    canvas.drawLine(Offset(center.dx, center.dy - radius), Offset(center.dx, center.dy + radius), paint);
  }

  void _drawHouseNumbers(Canvas canvas, Offset center, double radius) {
    final textPainter = TextPainter(
      textDirection: TextDirection.ltr,
    );

    final housePositions = _getNorthIndianHousePositions(center, radius);

    for (int i = 0; i < 12; i++) {
      textPainter.text = TextSpan(
        text: '${i + 1}',
        style: GoogleFonts.inter(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.deepPurple,
        ),
      );
      textPainter.layout();
      textPainter.paint(canvas, housePositions[i]);
    }
  }

  void _drawPlanets(Canvas canvas, Offset center, double radius) {
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    if (planetaryPositions == null) return;

    final housePlacements = chartData['housePlacements'] as Map<String, dynamic>?;
    if (housePlacements == null) return;

    final textPainter = TextPainter(
      textDirection: TextDirection.ltr,
    );

    final housePositions = _getNorthIndianHousePositions(center, radius);
    final planetSymbols = _getPlanetSymbols();

    for (var entry in planetaryPositions.entries) {
      final planet = entry.key;
      final house = housePlacements[planet] as int? ?? 1;
      final houseIndex = house - 1;

      textPainter.text = TextSpan(
        text: planetSymbols[planet] ?? planet.substring(0, 2).toUpperCase(),
        style: GoogleFonts.inter(
          fontSize: 10,
          fontWeight: FontWeight.w600,
          color: Colors.black87,
        ),
      );
      textPainter.layout();

      // Position planet in the house
      final houseCenter = housePositions[houseIndex];
      textPainter.paint(canvas, Offset(houseCenter.dx + 10, houseCenter.dy + 10));
    }
  }

  List<Offset> _getNorthIndianHousePositions(Offset center, double radius) {
    // North Indian chart house positions (simplified)
    return [
      Offset(center.dx - radius * 0.7, center.dy - radius * 0.7), // 1
      Offset(center.dx, center.dy - radius * 0.7), // 2
      Offset(center.dx + radius * 0.7, center.dy - radius * 0.7), // 3
      Offset(center.dx + radius * 0.7, center.dy), // 4
      Offset(center.dx + radius * 0.7, center.dy + radius * 0.7), // 5
      Offset(center.dx, center.dy + radius * 0.7), // 6
      Offset(center.dx - radius * 0.7, center.dy + radius * 0.7), // 7
      Offset(center.dx - radius * 0.7, center.dy), // 8
      Offset(center.dx - radius * 0.7, center.dy - radius * 0.7), // 9
      Offset(center.dx - radius * 0.35, center.dy - radius * 0.35), // 10
      Offset(center.dx, center.dy - radius * 0.35), // 11
      Offset(center.dx + radius * 0.35, center.dy - radius * 0.35), // 12
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
    final center = Offset(size.width / 2, size.height / 2);
    final radius = size.width / 2 - 20;

    // Draw outer circle
    _drawCircle(canvas, center, radius);

    // Draw inner lines
    _drawInnerLines(canvas, center, radius);

    // Draw house numbers
    _drawHouseNumbers(canvas, center, radius);

    // Draw planets
    _drawPlanets(canvas, center, radius);
  }

  void _drawCircle(Canvas canvas, Offset center, double radius) {
    final paint = Paint()
      ..color = Colors.deepPurple
      ..style = PaintingStyle.stroke
      ..strokeWidth = 2;

    canvas.drawCircle(center, radius, paint);
  }

  void _drawInnerLines(Canvas canvas, Offset center, double radius) {
    final paint = Paint()
      ..color = Colors.deepPurple.withOpacity(0.5)
      ..style = PaintingStyle.stroke
      ..strokeWidth = 1;

    // Draw 12 lines from center to edge
    for (int i = 0; i < 12; i++) {
      final angle = (i * 30) * 3.14159 / 180;
      final x = center.dx + radius * cos(angle);
      final y = center.dy + radius * sin(angle);
      canvas.drawLine(center, Offset(x, y), paint);
    }

    // Draw concentric circles
    canvas.drawCircle(center, radius * 0.66, paint);
    canvas.drawCircle(center, radius * 0.33, paint);
  }

  void _drawHouseNumbers(Canvas canvas, Offset center, double radius) {
    final textPainter = TextPainter(
      textDirection: TextDirection.ltr,
    );

    for (int i = 0; i < 12; i++) {
      final angle = ((i * 30) + 15) * 3.14159 / 180;
      final x = center.dx + radius * 0.85 * cos(angle);
      final y = center.dy + radius * 0.85 * sin(angle);

      textPainter.text = TextSpan(
        text: '${i + 1}',
        style: GoogleFonts.inter(
          fontSize: 14,
          fontWeight: FontWeight.bold,
          color: Colors.deepPurple,
        ),
      );
      textPainter.layout();
      textPainter.paint(canvas, Offset(x - textPainter.width / 2, y - textPainter.height / 2));
    }
  }

  void _drawPlanets(Canvas canvas, Offset center, double radius) {
    final planetaryPositions = chartData['planetaryPositions'] as Map<String, dynamic>?;
    if (planetaryPositions == null) return;

    final housePlacements = chartData['housePlacements'] as Map<String, dynamic>?;
    if (housePlacements == null) return;

    final textPainter = TextPainter(
      textDirection: TextDirection.ltr,
    );

    final planetSymbols = _getPlanetSymbols();

    for (var entry in planetaryPositions.entries) {
      final planet = entry.key;
      final house = housePlacements[planet] as int? ?? 1;
      final houseIndex = house - 1;

      // Calculate position based on house
      final angle = ((houseIndex * 30) + 15) * 3.14159 / 180;
      final x = center.dx + radius * 0.5 * cos(angle);
      final y = center.dy + radius * 0.5 * sin(angle);

      textPainter.text = TextSpan(
        text: planetSymbols[planet] ?? planet.substring(0, 2).toUpperCase(),
        style: GoogleFonts.inter(
          fontSize: 10,
          fontWeight: FontWeight.w600,
          color: Colors.black87,
        ),
      );
      textPainter.layout();
      textPainter.paint(canvas, Offset(x - textPainter.width / 2, y - textPainter.height / 2));
    }
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) => true;
}

double cos(double radians) {
  return 0.0; // Placeholder - use dart:math
}

double sin(double radians) {
  return 0.0; // Placeholder - use dart:math
}

Map<String, String> _getPlanetSymbols() {
  return {
    'Sun': '☉',
    'Moon': '☽',
    'Mars': '♂',
    'Mercury': '☿',
    'Jupiter': '♃',
    'Venus': '♀',
    'Saturn': '♄',
    'Rahu': '☊',
    'Ketu': '☋',
  };
}
