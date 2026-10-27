//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_threshold.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceThreshold {
  /// Returns a new [ResourceThreshold] instance.
  ResourceThreshold({

     this.warnFreePercent,

     this.warnFreeBytes,

     this.criticalFreePercent,

     this.criticalFreeBytes,
  });

      /// Warn when free drops below this percent
          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'warnFreePercent',
    required: false,
    includeIfNull: false,
  )


  final num? warnFreePercent;



      /// Warn when free drops below this many bytes
          // minimum: 0
  @JsonKey(
    
    name: r'warnFreeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? warnFreeBytes;



      /// Critical when free drops below this percent
          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'criticalFreePercent',
    required: false,
    includeIfNull: false,
  )


  final num? criticalFreePercent;



      /// Critical when free drops below this many bytes
          // minimum: 0
  @JsonKey(
    
    name: r'criticalFreeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? criticalFreeBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceThreshold &&
      other.warnFreePercent == warnFreePercent &&
      other.warnFreeBytes == warnFreeBytes &&
      other.criticalFreePercent == criticalFreePercent &&
      other.criticalFreeBytes == criticalFreeBytes;

    @override
    int get hashCode =>
        (warnFreePercent == null ? 0 : warnFreePercent.hashCode) +
        (warnFreeBytes == null ? 0 : warnFreeBytes.hashCode) +
        (criticalFreePercent == null ? 0 : criticalFreePercent.hashCode) +
        (criticalFreeBytes == null ? 0 : criticalFreeBytes.hashCode);

  factory ResourceThreshold.fromJson(Map<String, dynamic> json) => _$ResourceThresholdFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceThresholdToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

