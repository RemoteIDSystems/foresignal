//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// ICD 203 likelihood term, derived from the numeric probability
enum IcdTerm {
      @JsonValue(r'almost no chance')
      almostNoChance(r'almost no chance'),
      @JsonValue(r'very unlikely')
      veryUnlikely(r'very unlikely'),
      @JsonValue(r'unlikely')
      unlikely(r'unlikely'),
      @JsonValue(r'roughly even chance')
      roughlyEvenChance(r'roughly even chance'),
      @JsonValue(r'likely')
      likely(r'likely'),
      @JsonValue(r'very likely')
      veryLikely(r'very likely'),
      @JsonValue(r'almost certain')
      almostCertain(r'almost certain');

  const IcdTerm(this.value);

  final String value;

  @override
  String toString() => value;
}
