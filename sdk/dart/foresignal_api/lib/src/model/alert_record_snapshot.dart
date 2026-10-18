//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_record_snapshot.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertRecordSnapshot {
  /// Returns a new [AlertRecordSnapshot] instance.
  AlertRecordSnapshot({

    required  this.kind,

    required  this.version,

    required  this.observedAt,

     this.geometry,

     this.payload,

     this.truncated = false,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payload;



      /// True when the payload was dropped for size
  @JsonKey(
    defaultValue: false,
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertRecordSnapshot &&
      other.kind == kind &&
      other.version == version &&
      other.observedAt == observedAt &&
      other.geometry == geometry &&
      other.payload == payload &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        kind.hashCode +
        version.hashCode +
        observedAt.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (payload == null ? 0 : payload.hashCode) +
        truncated.hashCode;

  factory AlertRecordSnapshot.fromJson(Map<String, dynamic> json) => _$AlertRecordSnapshotFromJson(json);

  Map<String, dynamic> toJson() => _$AlertRecordSnapshotToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

