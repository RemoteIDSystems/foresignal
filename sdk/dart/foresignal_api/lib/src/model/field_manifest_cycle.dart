//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'field_manifest_cycle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FieldManifestCycle {
  /// Returns a new [FieldManifestCycle] instance.
  FieldManifestCycle({

    required  this.date,

    required  this.hour,

    required  this.forecastHour,

    required  this.runIso,

    required  this.validIso,
  });

  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final String date;



          // minimum: 0
          // maximum: 23
  @JsonKey(
    
    name: r'hour',
    required: true,
    includeIfNull: false,
  )


  final int hour;



          // minimum: 0
  @JsonKey(
    
    name: r'forecastHour',
    required: true,
    includeIfNull: false,
  )


  final int forecastHour;



  @JsonKey(
    
    name: r'runIso',
    required: true,
    includeIfNull: false,
  )


  final DateTime runIso;



  @JsonKey(
    
    name: r'validIso',
    required: true,
    includeIfNull: false,
  )


  final DateTime validIso;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FieldManifestCycle &&
      other.date == date &&
      other.hour == hour &&
      other.forecastHour == forecastHour &&
      other.runIso == runIso &&
      other.validIso == validIso;

    @override
    int get hashCode =>
        date.hashCode +
        hour.hashCode +
        forecastHour.hashCode +
        runIso.hashCode +
        validIso.hashCode;

  factory FieldManifestCycle.fromJson(Map<String, dynamic> json) => _$FieldManifestCycleFromJson(json);

  Map<String, dynamic> toJson() => _$FieldManifestCycleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

