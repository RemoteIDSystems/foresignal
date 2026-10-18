//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/timeline_bucket.dart';
import 'package:foresignal_api/src/model/timeline_alerts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'timeline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Timeline {
  /// Returns a new [Timeline] instance.
  Timeline({

    required  this.from,

    required  this.to,

    required  this.bucket,

    required  this.timeField,

     this.measure,

     this.groupBy,

    required  this.buckets,

     this.alerts,

     this.maxBucket,

     this.approximate,

     this.retainedFrom,

     this.backend,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'bucket',
    required: true,
    includeIfNull: false,
  )


  final String bucket;



  @JsonKey(
    
    name: r'timeField',
    required: true,
    includeIfNull: false,
  )


  final TimelineTimeFieldEnum timeField;



  @JsonKey(
    
    name: r'measure',
    required: false,
    includeIfNull: false,
  )


  final TimelineMeasureEnum? measure;



  @JsonKey(
    
    name: r'groupBy',
    required: false,
    includeIfNull: false,
  )


  final TimelineGroupByEnum? groupBy;



  @JsonKey(
    
    name: r'buckets',
    required: true,
    includeIfNull: false,
  )


  final List<TimelineBucket> buckets;



      /// Alert counts per bucket by severity, parallel to `buckets` (only with includeAlerts)
  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final List<TimelineAlertsInner>? alerts;



  @JsonKey(
    
    name: r'maxBucket',
    required: false,
    includeIfNull: false,
  )


  final int? maxBucket;



  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



  @JsonKey(
    
    name: r'retainedFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? retainedFrom;



      /// 038 serves ingested ranges older than retention from the rollup
  @JsonKey(
    
    name: r'backend',
    required: false,
    includeIfNull: false,
  )


  final TimelineBackendEnum? backend;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Timeline &&
      other.from == from &&
      other.to == to &&
      other.bucket == bucket &&
      other.timeField == timeField &&
      other.measure == measure &&
      other.groupBy == groupBy &&
      other.buckets == buckets &&
      other.alerts == alerts &&
      other.maxBucket == maxBucket &&
      other.approximate == approximate &&
      other.retainedFrom == retainedFrom &&
      other.backend == backend;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        bucket.hashCode +
        timeField.hashCode +
        measure.hashCode +
        groupBy.hashCode +
        buckets.hashCode +
        alerts.hashCode +
        maxBucket.hashCode +
        approximate.hashCode +
        (retainedFrom == null ? 0 : retainedFrom.hashCode) +
        backend.hashCode;

  factory Timeline.fromJson(Map<String, dynamic> json) => _$TimelineFromJson(json);

  Map<String, dynamic> toJson() => _$TimelineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TimelineTimeFieldEnum {
@JsonValue(r'effective')
effective(r'effective'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'ingested')
ingested(r'ingested');

const TimelineTimeFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TimelineMeasureEnum {
@JsonValue(r'versions')
versions(r'versions'),
@JsonValue(r'entities')
entities(r'entities');

const TimelineMeasureEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TimelineGroupByEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'kind')
kind(r'kind'),
@JsonValue(r'source')
source_(r'source');

const TimelineGroupByEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 038 serves ingested ranges older than retention from the rollup
enum TimelineBackendEnum {
@JsonValue(r'raw')
raw(r'raw'),
@JsonValue(r'rollup')
rollup(r'rollup'),
@JsonValue(r'mixed')
mixed(r'mixed');

const TimelineBackendEnum(this.value);

final String value;

@override
String toString() => value;
}


