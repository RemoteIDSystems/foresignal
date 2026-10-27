//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_gauge.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceGauge {
  /// Returns a new [ResourceGauge] instance.
  ResourceGauge({

     this.totalBytes,

     this.usedBytes,

     this.freeBytes,

     this.usedPercent,

    required  this.status,
  });

  @JsonKey(
    
    name: r'totalBytes',
    required: false,
    includeIfNull: false,
  )


  final int? totalBytes;



  @JsonKey(
    
    name: r'usedBytes',
    required: false,
    includeIfNull: false,
  )


  final int? usedBytes;



  @JsonKey(
    
    name: r'freeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? freeBytes;



  @JsonKey(
    
    name: r'usedPercent',
    required: false,
    includeIfNull: false,
  )


  final num? usedPercent;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final String status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceGauge &&
      other.totalBytes == totalBytes &&
      other.usedBytes == usedBytes &&
      other.freeBytes == freeBytes &&
      other.usedPercent == usedPercent &&
      other.status == status;

    @override
    int get hashCode =>
        (totalBytes == null ? 0 : totalBytes.hashCode) +
        (usedBytes == null ? 0 : usedBytes.hashCode) +
        (freeBytes == null ? 0 : freeBytes.hashCode) +
        (usedPercent == null ? 0 : usedPercent.hashCode) +
        status.hashCode;

  factory ResourceGauge.fromJson(Map<String, dynamic> json) => _$ResourceGaugeFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceGaugeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

