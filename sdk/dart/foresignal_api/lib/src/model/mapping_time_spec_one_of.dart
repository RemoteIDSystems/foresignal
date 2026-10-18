//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_time_spec_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingTimeSpecOneOf {
  /// Returns a new [MappingTimeSpecOneOf] instance.
  MappingTimeSpecOneOf({

    required  this.path,

    required  this.format,

     this.zone,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingTimeSpecOneOf &&
      other.path == path &&
      other.format == format &&
      other.zone == zone;

    @override
    int get hashCode =>
        path.hashCode +
        format.hashCode +
        zone.hashCode;

  factory MappingTimeSpecOneOf.fromJson(Map<String, dynamic> json) => _$MappingTimeSpecOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$MappingTimeSpecOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

