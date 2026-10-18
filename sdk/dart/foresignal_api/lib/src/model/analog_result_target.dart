//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog_result_target.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalogResultTarget {
  /// Returns a new [AnalogResultTarget] instance.
  AnalogResultTarget({

     this.situationId,

     this.regionCode,

     this.from,

     this.to,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final Object? regionCode;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalogResultTarget &&
      other.situationId == situationId &&
      other.regionCode == regionCode &&
      other.from == from &&
      other.to == to;

    @override
    int get hashCode =>
        (situationId == null ? 0 : situationId.hashCode) +
        (regionCode == null ? 0 : regionCode.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode);

  factory AnalogResultTarget.fromJson(Map<String, dynamic> json) => _$AnalogResultTargetFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogResultTargetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

