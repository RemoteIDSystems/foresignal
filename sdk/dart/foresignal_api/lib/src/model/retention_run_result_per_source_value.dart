//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_run_result_per_source_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionRunResultPerSourceValue {
  /// Returns a new [RetentionRunResultPerSourceValue] instance.
  RetentionRunResultPerSourceValue({

     this.versionsDeleted,

     this.floorSeq,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'versionsDeleted',
    required: false,
    includeIfNull: false,
  )


  final int? versionsDeleted;



  @JsonKey(
    
    name: r'floorSeq',
    required: false,
    includeIfNull: false,
  )


  final int? floorSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionRunResultPerSourceValue &&
      other.versionsDeleted == versionsDeleted &&
      other.floorSeq == floorSeq;

    @override
    int get hashCode =>
        versionsDeleted.hashCode +
        (floorSeq == null ? 0 : floorSeq.hashCode);

  factory RetentionRunResultPerSourceValue.fromJson(Map<String, dynamic> json) => _$RetentionRunResultPerSourceValueFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionRunResultPerSourceValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

