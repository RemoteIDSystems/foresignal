//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'infra_impact_request_record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class InfraImpactRequestRecord {
  /// Returns a new [InfraImpactRequestRecord] instance.
  InfraImpactRequestRecord({

    required  this.sourceId,

    required  this.externalId,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is InfraImpactRequestRecord &&
      other.sourceId == sourceId &&
      other.externalId == externalId;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode;

  factory InfraImpactRequestRecord.fromJson(Map<String, dynamic> json) => _$InfraImpactRequestRecordFromJson(json);

  Map<String, dynamic> toJson() => _$InfraImpactRequestRecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

