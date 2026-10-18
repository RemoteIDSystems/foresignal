//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetPatch {
  /// Returns a new [HypothesisSetPatch] instance.
  HypothesisSetPatch({

     this.description,

     this.cadence,

     this.status,

    required  this.version,
  });

  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'cadence',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetPatchCadenceEnum? cadence;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final HypothesisSetPatchStatusEnum? status;



      /// Optimistic lock (409 HYPOTHESIS_SET_VERSION_CONFLICT)
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetPatch &&
      other.description == description &&
      other.cadence == cadence &&
      other.status == status &&
      other.version == version;

    @override
    int get hashCode =>
        (description == null ? 0 : description.hashCode) +
        cadence.hashCode +
        status.hashCode +
        version.hashCode;

  factory HypothesisSetPatch.fromJson(Map<String, dynamic> json) => _$HypothesisSetPatchFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisSetPatchCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const HypothesisSetPatchCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisSetPatchStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'archived')
archived(r'archived');

const HypothesisSetPatchStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


