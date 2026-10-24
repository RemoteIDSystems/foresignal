//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecaster_track_record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecaster.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Forecaster {
  /// Returns a new [Forecaster] instance.
  Forecaster({

    required  this.id,

    required  this.type,

    required  this.name,

     this.config,

    required  this.version,

    required  this.active,

     this.trackRecord,

    required  this.createdAt,
  });

      /// Slug, e.g. `base-rate`, `ensemble`, `operator:jsmith`, `llm:daily-analyst`
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Expected to grow (`x-extensible-enum`): a client must accept a value not listed here.
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'active',
    required: true,
    includeIfNull: false,
  )


  final bool active;



  @JsonKey(
    
    name: r'trackRecord',
    required: false,
    includeIfNull: false,
  )


  final ForecasterTrackRecord? trackRecord;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Forecaster &&
      other.id == id &&
      other.type == type &&
      other.name == name &&
      other.config == config &&
      other.version == version &&
      other.active == active &&
      other.trackRecord == trackRecord &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        name.hashCode +
        config.hashCode +
        version.hashCode +
        active.hashCode +
        (trackRecord == null ? 0 : trackRecord.hashCode) +
        createdAt.hashCode;

  factory Forecaster.fromJson(Map<String, dynamic> json) => _$ForecasterFromJson(json);

  Map<String, dynamic> toJson() => _$ForecasterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

