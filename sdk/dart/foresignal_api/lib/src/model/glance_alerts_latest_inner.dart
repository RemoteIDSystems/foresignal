//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance_alerts_latest_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GlanceAlertsLatestInner {
  /// Returns a new [GlanceAlertsLatestInner] instance.
  GlanceAlertsLatestInner({

    required  this.id,

    required  this.severity,

     this.shortTitle,

    required  this.firedAt,

     this.near,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'severity',
    required: true,
    includeIfNull: false,
  )


  final GlanceAlertsLatestInnerSeverityEnum severity;



  @JsonKey(
    
    name: r'shortTitle',
    required: false,
    includeIfNull: false,
  )


  final String? shortTitle;



  @JsonKey(
    
    name: r'firedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firedAt;



      /// e.g. 12 km NNE of Austin, TX (040)
  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final String? near;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GlanceAlertsLatestInner &&
      other.id == id &&
      other.severity == severity &&
      other.shortTitle == shortTitle &&
      other.firedAt == firedAt &&
      other.near == near;

    @override
    int get hashCode =>
        id.hashCode +
        severity.hashCode +
        shortTitle.hashCode +
        firedAt.hashCode +
        (near == null ? 0 : near.hashCode);

  factory GlanceAlertsLatestInner.fromJson(Map<String, dynamic> json) => _$GlanceAlertsLatestInnerFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceAlertsLatestInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GlanceAlertsLatestInnerSeverityEnum {
@JsonValue(r'info')
info(r'info'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'critical')
critical(r'critical');

const GlanceAlertsLatestInnerSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


