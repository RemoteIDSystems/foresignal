//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/regional_index_value_components_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'regional_index_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegionalIndexValue {
  /// Returns a new [RegionalIndexValue] instance.
  RegionalIndexValue({

    required  this.indexId,

    required  this.region,

    required  this.day,

    required  this.value,

    required  this.components,

     this.floorsApplied,

     this.delta24h,

     this.delta7d,

     this.weightsVersion,

    required  this.asOfSeq,

     this.manifestId,

     this.recordRef,
  });

  @JsonKey(
    
    name: r'indexId',
    required: true,
    includeIfNull: false,
  )


  final String indexId;



  @JsonKey(
    
    name: r'region',
    required: true,
    includeIfNull: false,
  )


  final String region;



  @JsonKey(
    
    name: r'day',
    required: true,
    includeIfNull: false,
  )


  final DateTime day;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<RegionalIndexValueComponentsInner> components;



  @JsonKey(
    
    name: r'floorsApplied',
    required: false,
    includeIfNull: false,
  )


  final List<String>? floorsApplied;



  @JsonKey(
    
    name: r'delta24h',
    required: false,
    includeIfNull: false,
  )


  final num? delta24h;



  @JsonKey(
    
    name: r'delta7d',
    required: false,
    includeIfNull: false,
  )


  final num? delta7d;



  @JsonKey(
    
    name: r'weightsVersion',
    required: false,
    includeIfNull: false,
  )


  final int? weightsVersion;



  @JsonKey(
    
    name: r'asOfSeq',
    required: true,
    includeIfNull: false,
  )


  final int asOfSeq;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegionalIndexValue &&
      other.indexId == indexId &&
      other.region == region &&
      other.day == day &&
      other.value == value &&
      other.components == components &&
      other.floorsApplied == floorsApplied &&
      other.delta24h == delta24h &&
      other.delta7d == delta7d &&
      other.weightsVersion == weightsVersion &&
      other.asOfSeq == asOfSeq &&
      other.manifestId == manifestId &&
      other.recordRef == recordRef;

    @override
    int get hashCode =>
        indexId.hashCode +
        region.hashCode +
        day.hashCode +
        value.hashCode +
        components.hashCode +
        floorsApplied.hashCode +
        (delta24h == null ? 0 : delta24h.hashCode) +
        (delta7d == null ? 0 : delta7d.hashCode) +
        weightsVersion.hashCode +
        asOfSeq.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        (recordRef == null ? 0 : recordRef.hashCode);

  factory RegionalIndexValue.fromJson(Map<String, dynamic> json) => _$RegionalIndexValueFromJson(json);

  Map<String, dynamic> toJson() => _$RegionalIndexValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

