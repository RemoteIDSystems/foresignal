//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:json_annotation/json_annotation.dart';

/// Traffic Light Protocol 2.0 label
enum Tlp {
      @JsonValue(r'CLEAR')
      CLEAR(r'CLEAR'),
      @JsonValue(r'GREEN')
      GREEN(r'GREEN'),
      @JsonValue(r'AMBER')
      AMBER(r'AMBER'),
      @JsonValue(r'AMBER+STRICT')
      aMBERPlusSTRICT(r'AMBER+STRICT'),
      @JsonValue(r'RED')
      RED(r'RED');

  const Tlp(this.value);

  final String value;

  @override
  String toString() => value;
}
