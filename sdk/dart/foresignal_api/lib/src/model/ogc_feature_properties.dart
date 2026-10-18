//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_feature_properties.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcFeatureProperties {
  /// Returns a new [OgcFeatureProperties] instance.
  OgcFeatureProperties({

     this.title,

     this.sourceId,

     this.observedAt,

     this.eventTime,

     this.version,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcFeatureProperties &&
      other.title == title &&
      other.sourceId == sourceId &&
      other.observedAt == observedAt &&
      other.eventTime == eventTime &&
      other.version == version;

    @override
    int get hashCode =>
        (title == null ? 0 : title.hashCode) +
        sourceId.hashCode +
        observedAt.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        version.hashCode;

  factory OgcFeatureProperties.fromJson(Map<String, dynamic> json) => _$OgcFeaturePropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$OgcFeaturePropertiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

