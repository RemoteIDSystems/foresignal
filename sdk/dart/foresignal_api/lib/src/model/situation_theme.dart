//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Situation theme (118 `ThemeTable`)
enum SituationTheme {
      @JsonValue(r'maritime')
      maritime(r'maritime'),
      @JsonValue(r'aviation')
      aviation(r'aviation'),
      @JsonValue(r'conflict')
      conflict(r'conflict'),
      @JsonValue(r'infrastructure')
      infrastructure(r'infrastructure'),
      @JsonValue(r'weather')
      weather(r'weather'),
      @JsonValue(r'cyber')
      cyber(r'cyber'),
      @JsonValue(r'markets')
      markets(r'markets'),
      @JsonValue(r'health')
      health(r'health'),
      @JsonValue(r'space')
      space(r'space'),
      @JsonValue(r'mixed')
      mixed(r'mixed');

  const SituationTheme(this.value);

  final String value;

  @override
  String toString() => value;
}
