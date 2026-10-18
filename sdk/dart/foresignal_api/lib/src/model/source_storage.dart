//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_storage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceStorage {
  /// Returns a new [SourceStorage] instance.
  SourceStorage({

     this.observationsPerMinute,

     this.versionsPerMinute,

     this.persistRatio,

     this.triggerEdgeShare,

     this.bytesPerDay,

     this.projectedBytes,

     this.lateLastHour,

     this.clampedLastHour,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'observationsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final num? observationsPerMinute;



          // minimum: 0
  @JsonKey(
    
    name: r'versionsPerMinute',
    required: false,
    includeIfNull: false,
  )


  final num? versionsPerMinute;



      /// versions / observations
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'persistRatio',
    required: false,
    includeIfNull: false,
  )


  final num? persistRatio;



      /// Share of versions persisted as TRIGGER_EDGE over the last hour (warning above 0.2)
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'triggerEdgeShare',
    required: false,
    includeIfNull: false,
  )


  final num? triggerEdgeShare;



      /// Version bytes per day (7-day average)
  @JsonKey(
    
    name: r'bytesPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? bytesPerDay;



      /// bytesPerDay × retention window
  @JsonKey(
    
    name: r'projectedBytes',
    required: false,
    includeIfNull: false,
  )


  final int? projectedBytes;



          // minimum: 0
  @JsonKey(
    
    name: r'lateLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? lateLastHour;



          // minimum: 0
  @JsonKey(
    
    name: r'clampedLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? clampedLastHour;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceStorage &&
      other.observationsPerMinute == observationsPerMinute &&
      other.versionsPerMinute == versionsPerMinute &&
      other.persistRatio == persistRatio &&
      other.triggerEdgeShare == triggerEdgeShare &&
      other.bytesPerDay == bytesPerDay &&
      other.projectedBytes == projectedBytes &&
      other.lateLastHour == lateLastHour &&
      other.clampedLastHour == clampedLastHour;

    @override
    int get hashCode =>
        observationsPerMinute.hashCode +
        versionsPerMinute.hashCode +
        persistRatio.hashCode +
        triggerEdgeShare.hashCode +
        bytesPerDay.hashCode +
        projectedBytes.hashCode +
        lateLastHour.hashCode +
        clampedLastHour.hashCode;

  factory SourceStorage.fromJson(Map<String, dynamic> json) => _$SourceStorageFromJson(json);

  Map<String, dynamic> toJson() => _$SourceStorageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

