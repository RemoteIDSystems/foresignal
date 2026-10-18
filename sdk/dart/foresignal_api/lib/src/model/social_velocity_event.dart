//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/finding_severity.dart';
import 'package:foresignal_api/src/model/social_velocity_event_scope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_velocity_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialVelocityEvent {
  /// Returns a new [SocialVelocityEvent] instance.
  SocialVelocityEvent({

    required  this.event,

    required  this.findingId,

    required  this.scope,

    required  this.platform,

     this.language,

    required  this.velocityRatio,

    required  this.uniqueAuthors,

     this.zRobust,

    required  this.severity,

    required  this.detectedAt,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final SocialVelocityEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



  @JsonKey(
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final SocialVelocityEventScope scope;



      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



  @JsonKey(
    
    name: r'velocityRatio',
    required: true,
    includeIfNull: false,
  )


  final num velocityRatio;



  @JsonKey(
    
    name: r'uniqueAuthors',
    required: true,
    includeIfNull: false,
  )


  final int uniqueAuthors;



  @JsonKey(
    
    name: r'zRobust',
    required: false,
    includeIfNull: false,
  )


  final num? zRobust;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final FindingSeverity severity;



  @JsonKey(
    
    name: r'detectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime detectedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialVelocityEvent &&
      other.event == event &&
      other.findingId == findingId &&
      other.scope == scope &&
      other.platform == platform &&
      other.language == language &&
      other.velocityRatio == velocityRatio &&
      other.uniqueAuthors == uniqueAuthors &&
      other.zRobust == zRobust &&
      other.severity == severity &&
      other.detectedAt == detectedAt;

    @override
    int get hashCode =>
        event.hashCode +
        findingId.hashCode +
        scope.hashCode +
        platform.hashCode +
        (language == null ? 0 : language.hashCode) +
        velocityRatio.hashCode +
        uniqueAuthors.hashCode +
        (zRobust == null ? 0 : zRobust.hashCode) +
        severity.hashCode +
        detectedAt.hashCode;

  factory SocialVelocityEvent.fromJson(Map<String, dynamic> json) => _$SocialVelocityEventFromJson(json);

  Map<String, dynamic> toJson() => _$SocialVelocityEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialVelocityEventEventEnum {
@JsonValue(r'social.velocity')
socialPeriodVelocity(r'social.velocity');

const SocialVelocityEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


