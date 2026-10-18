//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_thread_item_gap.dart';
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_thread_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleThreadItem {
  /// Returns a new [ChronicleThreadItem] instance.
  ChronicleThreadItem({

    required  this.id,

    required  this.chronicleId,

    required  this.type,

    required  this.at,

     this.seq,

     this.layerId,

    required  this.title,

     this.summary,

     this.ref,

     this.envelope,

     this.snapshot,

     this.gap,

     this.backfilled,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleThreadItemTypeEnum type;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



      /// ULID identifier
  @JsonKey(
    
    name: r'layerId',
    required: false,
    includeIfNull: false,
  )


  final String? layerId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'envelope',
    required: false,
    includeIfNull: false,
  )


  final ObjectEnvelope? envelope;



      /// Snapshot kept in `chronicle_event` so the item survives its owner's retention
  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;



  @JsonKey(
    
    name: r'gap',
    required: false,
    includeIfNull: false,
  )


  final ChronicleThreadItemGap? gap;



  @JsonKey(
    
    name: r'backfilled',
    required: false,
    includeIfNull: false,
  )


  final bool? backfilled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleThreadItem &&
      other.id == id &&
      other.chronicleId == chronicleId &&
      other.type == type &&
      other.at == at &&
      other.seq == seq &&
      other.layerId == layerId &&
      other.title == title &&
      other.summary == summary &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.snapshot == snapshot &&
      other.gap == gap &&
      other.backfilled == backfilled;

    @override
    int get hashCode =>
        id.hashCode +
        chronicleId.hashCode +
        type.hashCode +
        at.hashCode +
        (seq == null ? 0 : seq.hashCode) +
        (layerId == null ? 0 : layerId.hashCode) +
        title.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        (ref == null ? 0 : ref.hashCode) +
        (envelope == null ? 0 : envelope.hashCode) +
        (snapshot == null ? 0 : snapshot.hashCode) +
        (gap == null ? 0 : gap.hashCode) +
        backfilled.hashCode;

  factory ChronicleThreadItem.fromJson(Map<String, dynamic> json) => _$ChronicleThreadItemFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleThreadItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleThreadItemTypeEnum {
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'situation-change')
situationChange(r'situation-change'),
@JsonValue(r'hypothesis-band-change')
hypothesisBandChange(r'hypothesis-band-change'),
@JsonValue(r'forecast-resolution')
forecastResolution(r'forecast-resolution'),
@JsonValue(r'first-appearance')
firstAppearance(r'first-appearance'),
@JsonValue(r'layer-edit')
layerEdit(r'layer-edit'),
@JsonValue(r'capture-gap')
captureGap(r'capture-gap'),
@JsonValue(r'cap-warning')
capWarning(r'cap-warning'),
@JsonValue(r'cap-fallback')
capFallback(r'cap-fallback'),
@JsonValue(r'status-change')
statusChange(r'status-change'),
@JsonValue(r'backfill')
backfill(r'backfill'),
@JsonValue(r'narrative-change')
narrativeChange(r'narrative-change'),
@JsonValue(r'comparison-change')
comparisonChange(r'comparison-change');

const ChronicleThreadItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


