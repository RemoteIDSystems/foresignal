//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_time_spec_one_of1.dart';
import 'package:foresignal_api/src/model/mapping_time_spec_one_of.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_time_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTimeSpec {
  /// Returns a new [MappingTimeSpec] instance.
  MappingTimeSpec({

    required  this.path,

    required  this.format,

     this.zone,

    required  this.received,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



      /// `iso`, `epochS`, `epochMs` or `pattern:<java DateTimeFormatter>`
  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final String format;



      /// Zone for zone-less inputs
  @JsonKey(
    
    name: r'zone',
    required: false,
    includeIfNull: false,
  )


  final String? zone;



  @JsonKey(
    
    name: r'received',
    required: true,
    includeIfNull: false,
  )


  final bool received;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTimeSpec &&
      other.path == path &&
      other.format == format &&
      other.zone == zone &&
      other.received == received;

    @override
    int get hashCode =>
        path.hashCode +
        format.hashCode +
        zone.hashCode +
        received.hashCode;

  factory MappingTimeSpec.fromJson(Map<String, dynamic> json) => _$MappingTimeSpecFromJson(json);

  Map<String, dynamic> toJson() => _$MappingTimeSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

