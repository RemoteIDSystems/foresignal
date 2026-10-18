//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_antecedent_one_of2.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestAntecedentOneOf2 {
  /// Returns a new [PrecursorTestAntecedentOneOf2] instance.
  PrecursorTestAntecedentOneOf2({

    required  this.intelEventType,
  });

  @JsonKey(
    
    name: r'intelEventType',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestAntecedentOneOf2IntelEventTypeEnum intelEventType;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestAntecedentOneOf2 &&
      other.intelEventType == intelEventType;

    @override
    int get hashCode =>
        intelEventType.hashCode;

  factory PrecursorTestAntecedentOneOf2.fromJson(Map<String, dynamic> json) => _$PrecursorTestAntecedentOneOf2FromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestAntecedentOneOf2ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestAntecedentOneOf2IntelEventTypeEnum {
@JsonValue(r'maritime-disruption')
maritimeDisruption(r'maritime-disruption'),
@JsonValue(r'aviation-anomaly')
aviationAnomaly(r'aviation-anomaly'),
@JsonValue(r'infrastructure-disruption')
infrastructureDisruption(r'infrastructure-disruption'),
@JsonValue(r'weather-impact')
weatherImpact(r'weather-impact'),
@JsonValue(r'seismic')
seismic(r'seismic'),
@JsonValue(r'custom')
custom(r'custom');

const PrecursorTestAntecedentOneOf2IntelEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


