// This is a basic Flutter widget test.
//
// To perform an interaction with a widget in your test, use the WidgetTester
// utility in the flutter_test package. For example, you can send tap and scroll
// gestures. You can also use WidgetTester to find child widgets in the widget
// tree, read text, and verify that the values of widget properties are correct.

import 'package:flutter_test/flutter_test.dart';

import 'package:astromindai/main.dart';

void main() {
  testWidgets('Astra app renders auth screen', (WidgetTester tester) async {
    // Build our app and trigger a frame.
    await tester.pumpWidget(const AstraApp());

    expect(find.text('ASTRA'), findsOneWidget);
    expect(find.text('Continue with Google'), findsOneWidget);
  });
}
