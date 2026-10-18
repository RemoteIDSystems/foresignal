//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_record_version.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleRecordVersion {
  /// Returns a new [ChronicleRecordVersion] instance.
  ChronicleRecordVersion({

    required  this.ref,

    required  this.sourceId,

    required  this.externalId,

    required  this.kind,

    required  this.seq,

    required  this.observedAt,

     this.eventTime,

     this.ingestedAt,

     this.geometry,

     this.payload,

     this.persistReason,

    required  this.layerId,

     this.backfilled,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



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
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



  @JsonKey(
    
    name: r'ingestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ingestedAt;



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



  @JsonKey(
    
    name: r'persistReason',
    required: false,
    includeIfNull: false,
  )


  final String? persistReason;



      /// ULID identifier
  @JsonKey(
    
    name: r'layerId',
    required: true,
    includeIfNull: false,
  )


  final String layerId;



  @JsonKey(
    
    name: r'backfilled',
    required: false,
    includeIfNull: false,
  )


  final bool? backfilled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleRecordVersion &&
      other.ref == ref &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.seq == seq &&
      other.observedAt == observedAt &&
      other.eventTime == eventTime &&
      other.ingestedAt == ingestedAt &&
      other.geometry == geometry &&
      other.payload == payload &&
      other.persistReason == persistReason &&
      other.layerId == layerId &&
      other.backfilled == backfilled;

    @override
    int get hashCode =>
        ref.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        seq.hashCode +
        observedAt.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        ingestedAt.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        payload.hashCode +
        persistReason.hashCode +
        layerId.hashCode +
        backfilled.hashCode;

  factory ChronicleRecordVersion.fromJson(Map<String, dynamic> json) => _$ChronicleRecordVersionFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleRecordVersionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

