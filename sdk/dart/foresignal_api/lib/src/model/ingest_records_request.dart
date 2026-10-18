//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_records.dart';
import 'package:foresignal_api/src/model/feature.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_records_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestRecordsRequest {
  /// Returns a new [IngestRecordsRequest] instance.
  IngestRecordsRequest({

    required  this.type,

    required  this.features,

     this.nextCursor,

     this.attributions,

     this.watermarkSeq,

     this.asOfSeq,

     this.asOf,

     this.approximate,

     this.horizon,

     this.licenceFiltered,

    required  this.records,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final IngestRecordsRequestTypeEnum type;



  @JsonKey(
    
    name: r'features',
    required: true,
    includeIfNull: false,
  )


  final List<Feature> features;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



      /// Foreign member: aggregated attributions
  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



      /// ChangeBus.lastPublishedSeq() read before the query; open SSE with after=watermarkSeq
  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



      /// Echo of the pinned snapshot seq (asOfSeq or order=id)
  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// Echo of asOf
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// asOf only: true when a source is SAMPLED/LATEST or the range is downsampled
  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



      /// asOf only: earliest supported T over the involved sources
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;



  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestRecordsRequest &&
      other.type == type &&
      other.features == features &&
      other.nextCursor == nextCursor &&
      other.attributions == attributions &&
      other.watermarkSeq == watermarkSeq &&
      other.asOfSeq == asOfSeq &&
      other.asOf == asOf &&
      other.approximate == approximate &&
      other.horizon == horizon &&
      other.licenceFiltered == licenceFiltered &&
      other.records == records;

    @override
    int get hashCode =>
        type.hashCode +
        features.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        attributions.hashCode +
        watermarkSeq.hashCode +
        (asOfSeq == null ? 0 : asOfSeq.hashCode) +
        (asOf == null ? 0 : asOf.hashCode) +
        approximate.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        licenceFiltered.hashCode +
        records.hashCode;

  factory IngestRecordsRequest.fromJson(Map<String, dynamic> json) => _$IngestRecordsRequestFromJson(json);

  Map<String, dynamic> toJson() => _$IngestRecordsRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestRecordsRequestTypeEnum {
@JsonValue(r'FeatureCollection')
featureCollection(r'FeatureCollection');

const IngestRecordsRequestTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


