//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cluster_timeline_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClusterTimelineItem {
  /// Returns a new [ClusterTimelineItem] instance.
  ClusterTimelineItem({

    required  this.evidenceKey,

    required  this.at,

    required  this.type,

    required  this.title,

     this.ref,

     this.envelope,

    required  this.heldBy,
  });

      /// Global evidence key, e.g. `ev:{family}:{originator}:{upstreamId}`
  @JsonKey(
    
    name: r'evidenceKey',
    required: true,
    includeIfNull: false,
  )


  final String evidenceKey;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ClusterTimelineItemTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



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



      /// Visible chronicles holding this item
  @JsonKey(
    
    name: r'heldBy',
    required: true,
    includeIfNull: false,
  )


  final List<String> heldBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClusterTimelineItem &&
      other.evidenceKey == evidenceKey &&
      other.at == at &&
      other.type == type &&
      other.title == title &&
      other.ref == ref &&
      other.envelope == envelope &&
      other.heldBy == heldBy;

    @override
    int get hashCode =>
        evidenceKey.hashCode +
        at.hashCode +
        type.hashCode +
        title.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (envelope == null ? 0 : envelope.hashCode) +
        heldBy.hashCode;

  factory ClusterTimelineItem.fromJson(Map<String, dynamic> json) => _$ClusterTimelineItemFromJson(json);

  Map<String, dynamic> toJson() => _$ClusterTimelineItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClusterTimelineItemTypeEnum {
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
@JsonValue(r'record')
record(r'record');

const ClusterTimelineItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


