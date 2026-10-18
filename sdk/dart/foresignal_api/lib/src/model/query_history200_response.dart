//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/count_result.dart';
import 'package:foresignal_api/src/model/record_version.dart';
import 'package:foresignal_api/src/model/history_page.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'query_history200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QueryHistory200Response {
  /// Returns a new [QueryHistory200Response] instance.
  QueryHistory200Response({

    required  this.items,

     this.nextCursor,

     this.attributions,

     this.licenceFiltered,

    required  this.count,

    required  this.exact,

    required  this.watermarkSeq,
  });

      /// Versions ordered by the selected timeField
  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<RecordVersion> items;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



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



  @JsonKey(
    
    name: r'count',
    required: true,
    includeIfNull: false,
  )


  final int count;



      /// False above 1,000,000
  @JsonKey(
    
    name: r'exact',
    required: true,
    includeIfNull: false,
  )


  final bool exact;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QueryHistory200Response &&
      other.items == items &&
      other.nextCursor == nextCursor &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered &&
      other.count == count &&
      other.exact == exact &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        items.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        attributions.hashCode +
        licenceFiltered.hashCode +
        count.hashCode +
        exact.hashCode +
        watermarkSeq.hashCode;

  factory QueryHistory200Response.fromJson(Map<String, dynamic> json) => _$QueryHistory200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$QueryHistory200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

