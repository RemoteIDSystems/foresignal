//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/search_result.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/count_result.dart';
import 'package:foresignal_api/src/model/search_results.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'search200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Search200Response {
  /// Returns a new [Search200Response] instance.
  Search200Response({

    required  this.mode,

    required  this.items,

     this.degraded,

     this.attributions,

     this.licenceFiltered,

     this.reason,

    required  this.count,

    required  this.exact,

    required  this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final Search200ResponseModeEnum mode;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SearchResult> items;



      /// True when semantic was requested but text was used
  @JsonKey(
    
    name: r'degraded',
    required: false,
    includeIfNull: false,
  )


  final bool? degraded;



  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;



      /// Why semantic results were not used, when `degraded` is true (027)
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



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
    bool operator ==(Object other) => identical(this, other) || other is Search200Response &&
      other.mode == mode &&
      other.items == items &&
      other.degraded == degraded &&
      other.attributions == attributions &&
      other.licenceFiltered == licenceFiltered &&
      other.reason == reason &&
      other.count == count &&
      other.exact == exact &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        mode.hashCode +
        items.hashCode +
        degraded.hashCode +
        attributions.hashCode +
        licenceFiltered.hashCode +
        (reason == null ? 0 : reason.hashCode) +
        count.hashCode +
        exact.hashCode +
        watermarkSeq.hashCode;

  factory Search200Response.fromJson(Map<String, dynamic> json) => _$Search200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$Search200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum Search200ResponseModeEnum {
@JsonValue(r'hybrid')
hybrid(r'hybrid'),
@JsonValue(r'semantic')
semantic(r'semantic'),
@JsonValue(r'text')
text(r'text');

const Search200ResponseModeEnum(this.value);

final String value;

@override
String toString() => value;
}


