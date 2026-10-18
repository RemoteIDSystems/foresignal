//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_cloud_event_data.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_cloud_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkCloudEvent {
  /// Returns a new [SinkCloudEvent] instance.
  SinkCloudEvent({

    required  this.specversion,

    required  this.id,

    required  this.source_,

    required  this.type,

    required  this.subject,

    required  this.time,

     this.datacontenttype,

     this.foresignalseq,

     this.foresignalkind,

     this.foresignalreason,

    required  this.data,
  });

  @JsonKey(
    
    name: r'specversion',
    required: true,
    includeIfNull: false,
  )


  final SinkCloudEventSpecversionEnum specversion;



      /// {instanceId}:{seq}
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// urn:foresignal:{instanceId}:source:{sourceId}
  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final SinkCloudEventTypeEnum type;



      /// {sourceId}/{externalId}
  @JsonKey(
    
    name: r'subject',
    required: true,
    includeIfNull: false,
  )


  final String subject;



      /// observedAt
  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'datacontenttype',
    required: false,
    includeIfNull: false,
  )


  final SinkCloudEventDatacontenttypeEnum? datacontenttype;



  @JsonKey(
    
    name: r'foresignalseq',
    required: false,
    includeIfNull: false,
  )


  final int? foresignalseq;



  @JsonKey(
    
    name: r'foresignalkind',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalkind;



      /// persistReason of the version, or SNAPSHOT
  @JsonKey(
    
    name: r'foresignalreason',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalreason;



  @JsonKey(
    
    name: r'data',
    required: true,
    includeIfNull: false,
  )


  final SinkCloudEventData data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkCloudEvent &&
      other.specversion == specversion &&
      other.id == id &&
      other.source_ == source_ &&
      other.type == type &&
      other.subject == subject &&
      other.time == time &&
      other.datacontenttype == datacontenttype &&
      other.foresignalseq == foresignalseq &&
      other.foresignalkind == foresignalkind &&
      other.foresignalreason == foresignalreason &&
      other.data == data;

    @override
    int get hashCode =>
        specversion.hashCode +
        id.hashCode +
        source_.hashCode +
        type.hashCode +
        subject.hashCode +
        time.hashCode +
        datacontenttype.hashCode +
        foresignalseq.hashCode +
        foresignalkind.hashCode +
        foresignalreason.hashCode +
        data.hashCode;

  factory SinkCloudEvent.fromJson(Map<String, dynamic> json) => _$SinkCloudEventFromJson(json);

  Map<String, dynamic> toJson() => _$SinkCloudEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SinkCloudEventSpecversionEnum {
@JsonValue(r'1.0')
n1period0(r'1.0');

const SinkCloudEventSpecversionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkCloudEventTypeEnum {
@JsonValue(r'ai.foresignal.record.created')
aiPeriodForesignalPeriodRecordPeriodCreated(r'ai.foresignal.record.created'),
@JsonValue(r'ai.foresignal.record.updated')
aiPeriodForesignalPeriodRecordPeriodUpdated(r'ai.foresignal.record.updated'),
@JsonValue(r'ai.foresignal.record.tombstoned')
aiPeriodForesignalPeriodRecordPeriodTombstoned(r'ai.foresignal.record.tombstoned'),
@JsonValue(r'ai.foresignal.record.snapshot')
aiPeriodForesignalPeriodRecordPeriodSnapshot(r'ai.foresignal.record.snapshot');

const SinkCloudEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SinkCloudEventDatacontenttypeEnum {
@JsonValue(r'application/json')
applicationSlashJson(r'application/json');

const SinkCloudEventDatacontenttypeEnum(this.value);

final String value;

@override
String toString() => value;
}


