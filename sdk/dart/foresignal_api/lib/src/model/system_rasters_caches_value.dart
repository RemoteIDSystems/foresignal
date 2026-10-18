//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_rasters_caches_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemRastersCachesValue {
  /// Returns a new [SystemRastersCachesValue] instance.
  SystemRastersCachesValue({

     this.sizeBytes,

     this.maxBytes,

     this.hitRate,

     this.evictionRate,
  });

  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



  @JsonKey(
    
    name: r'maxBytes',
    required: false,
    includeIfNull: false,
  )


  final int? maxBytes;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



      /// Evictions per minute
  @JsonKey(
    
    name: r'evictionRate',
    required: false,
    includeIfNull: false,
  )


  final num? evictionRate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemRastersCachesValue &&
      other.sizeBytes == sizeBytes &&
      other.maxBytes == maxBytes &&
      other.hitRate == hitRate &&
      other.evictionRate == evictionRate;

    @override
    int get hashCode =>
        sizeBytes.hashCode +
        maxBytes.hashCode +
        hitRate.hashCode +
        evictionRate.hashCode;

  factory SystemRastersCachesValue.fromJson(Map<String, dynamic> json) => _$SystemRastersCachesValueFromJson(json);

  Map<String, dynamic> toJson() => _$SystemRastersCachesValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

