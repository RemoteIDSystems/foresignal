//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'kinematic_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KinematicState {
  /// Returns a new [KinematicState] instance.
  KinematicState({

    required  this.sourceId,

    required  this.externalId,

     this.kind,

    required  this.position,

    required  this.observedAt,

     this.speedMps,

     this.courseDeg,

     this.altitudeM,

     this.derived,

     this.stale,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'position',
    required: true,
    includeIfNull: false,
  )


  final Geometry position;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'speedMps',
    required: false,
    includeIfNull: false,
  )


  final num? speedMps;



          // minimum: 0
          // maximum: 360
  @JsonKey(
    
    name: r'courseDeg',
    required: false,
    includeIfNull: false,
  )


  final num? courseDeg;



  @JsonKey(
    
    name: r'altitudeM',
    required: false,
    includeIfNull: false,
  )


  final num? altitudeM;



      /// Speed and course derived from the last two persisted positions
  @JsonKey(
    
    name: r'derived',
    required: false,
    includeIfNull: false,
  )


  final bool? derived;



      /// Older than 2 × the source TTL
  @JsonKey(
    
    name: r'stale',
    required: false,
    includeIfNull: false,
  )


  final bool? stale;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KinematicState &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.position == position &&
      other.observedAt == observedAt &&
      other.speedMps == speedMps &&
      other.courseDeg == courseDeg &&
      other.altitudeM == altitudeM &&
      other.derived == derived &&
      other.stale == stale;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        position.hashCode +
        observedAt.hashCode +
        (speedMps == null ? 0 : speedMps.hashCode) +
        (courseDeg == null ? 0 : courseDeg.hashCode) +
        (altitudeM == null ? 0 : altitudeM.hashCode) +
        derived.hashCode +
        stale.hashCode;

  factory KinematicState.fromJson(Map<String, dynamic> json) => _$KinematicStateFromJson(json);

  Map<String, dynamic> toJson() => _$KinematicStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

