//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tuning_suggestion_expected_effect.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestion {
  /// Returns a new [TuningSuggestion] instance.
  TuningSuggestion({

    required  this.id,

    required  this.kind,

    required  this.rationale,

    required  this.patch_,

     this.expectedEffect,

     this.caveats,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final TuningSuggestionKindEnum kind;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;



      /// Partial TriggerRuleInput ready to apply as an ordinary rule update
  @JsonKey(
    
    name: r'patch',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> patch_;



  @JsonKey(
    
    name: r'expectedEffect',
    required: false,
    includeIfNull: false,
  )


  final TuningSuggestionExpectedEffect? expectedEffect;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestion &&
      other.id == id &&
      other.kind == kind &&
      other.rationale == rationale &&
      other.patch_ == patch_ &&
      other.expectedEffect == expectedEffect &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        rationale.hashCode +
        patch_.hashCode +
        (expectedEffect == null ? 0 : expectedEffect.hashCode) +
        caveats.hashCode;

  factory TuningSuggestion.fromJson(Map<String, dynamic> json) => _$TuningSuggestionFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TuningSuggestionKindEnum {
@JsonValue(r'raise-threshold')
raiseThreshold(r'raise-threshold'),
@JsonValue(r'lower-threshold')
lowerThreshold(r'lower-threshold'),
@JsonValue(r'add-cooldown')
addCooldown(r'add-cooldown'),
@JsonValue(r'switch-to-enter')
switchToEnter(r'switch-to-enter'),
@JsonValue(r'add-edge-min-interval')
addEdgeMinInterval(r'add-edge-min-interval'),
@JsonValue(r'shrink-geofence')
shrinkGeofence(r'shrink-geofence'),
@JsonValue(r'lower-anomaly-sensitivity')
lowerAnomalySensitivity(r'lower-anomaly-sensitivity'),
@JsonValue(r'ACTIVE_WINDOW')
ACTIVE_WINDOW(r'ACTIVE_WINDOW'),
@JsonValue(r'SEVERITY_UP')
SEVERITY_UP(r'SEVERITY_UP'),
@JsonValue(r'SEVERITY_DOWN')
SEVERITY_DOWN(r'SEVERITY_DOWN');

const TuningSuggestionKindEnum(this.value);

final String value;

@override
String toString() => value;
}


