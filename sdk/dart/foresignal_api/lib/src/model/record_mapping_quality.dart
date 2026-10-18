//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_value_spec.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping_quality.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMappingQuality {
  /// Returns a new [RecordMappingQuality] instance.
  RecordMappingQuality({

     this.accuracyMeters,

     this.extra,
  });

  @JsonKey(
    
    name: r'accuracyMeters',
    required: false,
    includeIfNull: false,
  )


  final MappingValueSpec? accuracyMeters;



      /// Namespaced quality extras (Quality.extra), e.g. `lora.rssi`; scalar values only
  @JsonKey(
    
    name: r'extra',
    required: false,
    includeIfNull: false,
  )


  final Map<String, MappingValueSpec>? extra;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMappingQuality &&
      other.accuracyMeters == accuracyMeters &&
      other.extra == extra;

    @override
    int get hashCode =>
        accuracyMeters.hashCode +
        extra.hashCode;

  factory RecordMappingQuality.fromJson(Map<String, dynamic> json) => _$RecordMappingQualityFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingQualityToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

