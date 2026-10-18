//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'record_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RecordPage {
  /// Returns a new [RecordPage] instance.
  RecordPage({

    required  this.items,

     this.nextCursor,

     this.watermarkSeq,

     this.asOfSeq,

     this.asOf,

     this.approximate,

     this.horizon,

     this.attributions,

     this.licenceFiltered,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Record> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



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



      /// Aggregated attributions of the records in this page
  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



      /// Records removed by the licence policy
          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RecordPage &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.watermarkSeq == watermarkSeq &&
      other.asOfSeq == asOfSeq &&
      other.asOf == asOf &&
      other.approximate == approximate &&
      other.horizon == horizon &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        watermarkSeq.hashCode +
        (asOfSeq == null ? 0 : asOfSeq.hashCode) +
        (asOf == null ? 0 : asOf.hashCode) +
        approximate.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        attributions.hashCode +
        licenceFiltered.hashCode;

  factory RecordPage.fromJson(Map<String, dynamic> json) => _$RecordPageFromJson(json);

  Map<String, dynamic> toJson() => _$RecordPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

