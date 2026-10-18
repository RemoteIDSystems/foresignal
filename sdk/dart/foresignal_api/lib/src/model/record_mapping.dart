//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/record_mapping_pairs.dart';
import 'package:foresignal_api/src/model/mapping_value_spec.dart';
import 'package:foresignal_api/src/model/record_mapping_quality.dart';
import 'package:foresignal_api/src/model/record_mapping_filter_inner.dart';
import 'package:foresignal_api/src/model/record_mapping_spread.dart';
import 'package:foresignal_api/src/model/record_mapping_tombstone.dart';
import 'package:foresignal_api/src/model/record_mapping_geometry.dart';
import 'package:foresignal_api/src/model/mapping_field.dart';
import 'package:foresignal_api/src/model/mapping_time_spec.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_mapping.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordMapping {
  /// Returns a new [RecordMapping] instance.
  RecordMapping({

    required  this.version,

     this.select,

    required  this.externalId,

     this.kind,

    required  this.observedAt,

     this.eventTime,

     this.validUntil,

     this.geometry,

     this.tombstone,

     this.quality,

     this.fields,

     this.pairs,

     this.drop,

     this.filter,

     this.spread,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final RecordMappingVersionEnum version;



      /// Optional JSONPath producing an array; one record per item, other paths item-relative
  @JsonKey(
    
    name: r'select',
    required: false,
    includeIfNull: false,
  )


  final String? select;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final MappingValueSpec externalId;



      /// Required unless the source has exactly one kind
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final MappingValueSpec? kind;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final MappingTimeSpec observedAt;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final MappingTimeSpec? eventTime;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final MappingTimeSpec? validUntil;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingGeometry? geometry;



  @JsonKey(
    
    name: r'tombstone',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingTombstone? tombstone;



  @JsonKey(
    
    name: r'quality',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingQuality? quality;



      /// Payload fields, in order. A name may repeat: the first entry with a value wins (several sensors feeding one field, e.g. airrohr `pm25` from SDS011, PMS or SPS30). A value that cannot be coerced to `type`, or is NaN or infinite, leaves that field out with a FIELD_REJECTED warning.
  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final List<MappingField>? fields;



  @JsonKey(
    
    name: r'pairs',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingPairs? pairs;



      /// Input keys never stored (secrets, device identifiers)
  @JsonKey(
    
    name: r'drop',
    required: false,
    includeIfNull: false,
  )


  final List<String>? drop;



      /// Records failing any condition are skipped and counted
  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final List<RecordMappingFilterInner>? filter;



  @JsonKey(
    
    name: r'spread',
    required: false,
    includeIfNull: false,
  )


  final RecordMappingSpread? spread;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordMapping &&
      other.version == version &&
      other.select == select &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.observedAt == observedAt &&
      other.eventTime == eventTime &&
      other.validUntil == validUntil &&
      other.geometry == geometry &&
      other.tombstone == tombstone &&
      other.quality == quality &&
      other.fields == fields &&
      other.pairs == pairs &&
      other.drop == drop &&
      other.filter == filter &&
      other.spread == spread;

    @override
    int get hashCode =>
        version.hashCode +
        (select == null ? 0 : select.hashCode) +
        externalId.hashCode +
        (kind == null ? 0 : kind.hashCode) +
        observedAt.hashCode +
        (eventTime == null ? 0 : eventTime.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        (tombstone == null ? 0 : tombstone.hashCode) +
        (quality == null ? 0 : quality.hashCode) +
        fields.hashCode +
        (pairs == null ? 0 : pairs.hashCode) +
        drop.hashCode +
        filter.hashCode +
        (spread == null ? 0 : spread.hashCode);

  factory RecordMapping.fromJson(Map<String, dynamic> json) => _$RecordMappingFromJson(json);

  Map<String, dynamic> toJson() => _$RecordMappingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RecordMappingVersionEnum {
@JsonValue(1)
number1('1');

const RecordMappingVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


