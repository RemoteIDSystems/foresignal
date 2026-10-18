//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_social_volume_selector_scopes_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_social_volume_selector.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSocialVolumeSelector {
  /// Returns a new [ChronicleSocialVolumeSelector] instance.
  ChronicleSocialVolumeSelector({

    required  this.type,

    required  this.scopes,

     this.platforms,

     this.metrics,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleSocialVolumeSelectorTypeEnum type;



  @JsonKey(
    
    name: r'scopes',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleSocialVolumeSelectorScopesInner> scopes;



  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<String>? platforms;



  @JsonKey(
    
    name: r'metrics',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSocialVolumeSelectorMetricsEnum>? metrics;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSocialVolumeSelector &&
      other.type == type &&
      other.scopes == scopes &&
      other.platforms == platforms &&
      other.metrics == metrics;

    @override
    int get hashCode =>
        type.hashCode +
        scopes.hashCode +
        platforms.hashCode +
        metrics.hashCode;

  factory ChronicleSocialVolumeSelector.fromJson(Map<String, dynamic> json) => _$ChronicleSocialVolumeSelectorFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSocialVolumeSelectorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSocialVolumeSelectorTypeEnum {
@JsonValue(r'social-volume')
socialVolume(r'social-volume');

const ChronicleSocialVolumeSelectorTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleSocialVolumeSelectorMetricsEnum {
@JsonValue(r'posts')
posts(r'posts'),
@JsonValue(r'uniqueAuthors')
uniqueAuthors(r'uniqueAuthors'),
@JsonValue(r'reposts')
reposts(r'reposts'),
@JsonValue(r'engagement')
engagement(r'engagement');

const ChronicleSocialVolumeSelectorMetricsEnum(this.value);

final String value;

@override
String toString() => value;
}


