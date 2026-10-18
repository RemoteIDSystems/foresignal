//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'derived_evidence_ref.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DerivedEvidenceRef {
  /// Returns a new [DerivedEvidenceRef] instance.
  DerivedEvidenceRef({

    required  this.sourceId,

    required  this.externalId,

    required  this.observedAt,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DerivedEvidenceRef &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.observedAt == observedAt;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        observedAt.hashCode;

  factory DerivedEvidenceRef.fromJson(Map<String, dynamic> json) => _$DerivedEvidenceRefFromJson(json);

  Map<String, dynamic> toJson() => _$DerivedEvidenceRefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

